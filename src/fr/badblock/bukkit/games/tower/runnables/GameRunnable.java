package fr.badblock.bukkit.games.tower.runnables;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.scheduler.BukkitRunnable;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.TowerAchievementList;
import fr.badblock.bukkit.games.tower.configuration.TowerMapConfiguration;
import fr.badblock.bukkit.games.tower.entities.TowerTeamData;
import fr.badblock.bukkit.games.tower.listeners.JoinListener;
import fr.badblock.bukkit.games.tower.players.TowerData;
import fr.badblock.bukkit.games.tower.players.TowerScoreboard;
import fr.badblock.bukkit.games.tower.result.TowerResults;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.achievements.PlayerAchievement;
import fr.badblock.gameapi.game.GameState;
import fr.badblock.gameapi.game.rankeds.RankedCalc;
import fr.badblock.gameapi.game.rankeds.RankedManager;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.BadblockMode;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.players.data.PlayerAchievementState;
import fr.badblock.gameapi.players.scoreboard.CustomObjective;
import fr.badblock.gameapi.utils.BukkitUtils;
import fr.badblock.gameapi.utils.general.MathsUtils;
import fr.badblock.gameapi.utils.general.TimeUnit;
import fr.badblock.gameapi.utils.i18n.TranslatableString;
import lombok.Getter;

public class GameRunnable extends BukkitRunnable {
	//public static final int MAX_TIME = 60 * 60 * 24;

	public boolean forceEnd 		 = false;
	@Getter
	private int    time 			 = 0;

	public GameRunnable(TowerMapConfiguration config){
		GameAPI.getAPI().getGameServer().setGameState(GameState.RUNNING);
		GameAPI.getAPI().getGameServer().saveTeamsAndPlayersForResult();

		new ItemSpawnRunnable(Material.IRON_INGOT, 60, PluginTower.getInstance().getMapConfiguration().getIron()).start();
		new ItemSpawnRunnable(Material.DIAMOND, 800, PluginTower.getInstance().getMapConfiguration().getIron()).start();
		new ItemSpawnRunnable(Material.EXP_BOTTLE, 30, PluginTower.getInstance().getMapConfiguration().getXpbottle()).start();

		if (!PluginTower.getInstance().getMapConfiguration().getAllowBows()) {
			remove(Material.BOW);
			remove(Material.ARROW);
		}

		Bukkit.getWorlds().forEach(world -> {
			world.setTime(config.getTime());
		});

		for(BadblockTeam team : GameAPI.getAPI().getTeams()){

			Location location = team.teamData(TowerTeamData.class).getRespawnLocation();
			location.getChunk().load();

			for(BadblockPlayer p : team.getOnlinePlayers()){
				JoinListener.handle(p);
			}

		}

		GameAPI.getAPI().getJoinItems().doClearInventory(false);
		GameAPI.getAPI().getJoinItems().end();
	}

	public void remove(Material m) {
		Iterator<Recipe> it = Bukkit.getServer().recipeIterator();
		Recipe recipe;
		while(it.hasNext())
		{
			recipe = it.next();
			if (recipe != null && recipe.getResult().getType() == m)
			{
				it.remove();
			}
		}
	}

	@Override
	public void run() {
		time++;
		GameAPI.setJoinable(time > 900);

		int size = GameAPI.getAPI().getTeams().size();

		List<BadblockTeam> to  = new ArrayList<>();
		BadblockTeam	   max = null;

		Bukkit.getOnlinePlayers().forEach(player -> {
			CustomObjective obj = ((BadblockPlayer) player).getCustomObjective();
			if(obj != null)
				obj.generate();
		});

		for(BadblockTeam team : GameAPI.getAPI().getTeams()){
			if(team.getOnlinePlayers().size() == 0){
				GameAPI.getAPI().getGameServer().cancelReconnectionInvitations(team);
				to.add(team);

				new TranslatableString("tower.team-loose", team.getChatName()).broadcast();;
			} else if(max == null || max.teamData(TowerTeamData.class).getMarks() < team.teamData(TowerTeamData.class).getMarks())
				max = team;
		}

		to.forEach(GameAPI.getAPI()::unregisterTeam);

		if(GameAPI.getAPI().getTeams().stream().filter(team -> team.playersCurrentlyOnline() > 0).count() <= 1 || forceEnd || (max != null && max.teamData(TowerTeamData.class).getMarks() == PluginTower.getInstance().getConfiguration().neededPoints)){
			cancel();
			BadblockTeam winner = max;

			GameAPI.getAPI().getGameServer().setGameState(GameState.FINISHED);

			Location winnerLocation = PluginTower.getInstance().getMapConfiguration().getSpawnLocation();
			Location looserLocation = winnerLocation.clone().add(0d, 7d, 0d);

			for(Player player : Bukkit.getOnlinePlayers()){
				BadblockPlayer bp = (BadblockPlayer) player;
				bp.heal();
				bp.clearInventory();
				bp.setInvulnerable(true);

				double badcoins = bp.inGameData(TowerData.class).getScore() / 4;
				double xp	    = bp.inGameData(TowerData.class).getScore() / 2;

				if(winner != null && winner.equals(bp.getTeam())){
					bp.getPlayerData().addRankedPoints(3);
					bp.teleport(winnerLocation);
					bp.setAllowFlight(true);
					bp.setFlying(true);

					new BukkitRunnable() {
						int count = 5;

						@Override
						public void run() {
							count--;

							bp.teleport(winnerLocation);
							bp.setAllowFlight(true);
							bp.setFlying(true);

							if(count == 0)
								cancel();
						}
					}.runTaskTimer(GameAPI.getAPI(), 5L, 5L);
					bp.sendTranslatedTitle("tower.title-win", winner.getChatName());
					bp.getPlayerData().incrementStatistic("tower", TowerScoreboard.WINS);
					bp.getPlayerData().incrementTempRankedData(RankedManager.instance.getCurrentRankedGameName(), TowerScoreboard.WINS, 1);

					incrementAchievements(bp, TowerAchievementList.TOWER_WIN_1, TowerAchievementList.TOWER_WIN_2, TowerAchievementList.TOWER_WIN_3, TowerAchievementList.TOWER_WIN_4);
				} else {
					bp.getPlayerData().addRankedPoints(-2);
					badcoins = ((double) badcoins) / 1.5d;

					bp.jailPlayerAt(looserLocation);
					bp.sendTranslatedTitle("tower.title-loose", winner.getChatName());

					if(bp.getBadblockMode() == BadblockMode.PLAYER)
					{
						bp.getPlayerData().incrementStatistic("tower", TowerScoreboard.LOOSES);
						bp.getPlayerData().incrementTempRankedData(RankedManager.instance.getCurrentRankedGameName(), TowerScoreboard.LOOSES, 1);
					}
				}

				int rbadcoins = badcoins < 2 ? 2 : (int) badcoins;
				int rxp		  = xp < 5 ? 5 : (int) xp;

				bp.getPlayerData().addBadcoins(rbadcoins, true);
				bp.getPlayerData().addXp(rxp, true);

				new BukkitRunnable(){

					@Override
					public void run(){
						if(bp.isOnline()){
							bp.sendTranslatedActionBar("tower.win", rbadcoins, rxp);
						}
					}

				}.runTaskTimer(GameAPI.getAPI(), 0, 30L);

				if(bp.getCustomObjective() != null)
					bp.getCustomObjective().generate();
			}

			// Work with rankeds
			String rankedGameName = RankedManager.instance.getCurrentRankedGameName();
			for (BadblockPlayer player : BukkitUtils.getPlayers())
			{
				RankedManager.instance.calcPoints(rankedGameName, player, new RankedCalc()
				{

					@Override
					public long done() {
						double kills = RankedManager.instance.getData(rankedGameName, player, TowerScoreboard.KILLS);
						double deaths = RankedManager.instance.getData(rankedGameName, player, TowerScoreboard.DEATHS);
						double wins = RankedManager.instance.getData(rankedGameName, player, TowerScoreboard.WINS);
						double looses = RankedManager.instance.getData(rankedGameName, player, TowerScoreboard.LOOSES);
						double marks = RankedManager.instance.getData(rankedGameName, player, TowerScoreboard.MARKS);
						double total = 
								( (kills / 0.5D) + (wins * 4) + 
										( (kills * marks) + (marks * 2) * (kills / (deaths > 0 ? deaths : 1) ) ) )
								/ (1 + looses);
						return (long) total;
					}

				});
			}
			RankedManager.instance.fill(rankedGameName);

			// Infos de fin de partie
			Entry<String, Double> mostDamager = null;
			Entry<String, Integer> mostDeath = null;
			Entry<String, Integer> mostObjective = null;
			for (BadblockPlayer pl : BukkitUtils.getPlayers())
			{
				TowerData towerData = pl.inGameData(TowerData.class);
				if (towerData == null)
				{
					continue;
				}
				if (mostDamager == null || (mostDamager != null && mostDamager.getValue() < towerData.givenDamages))
				{
					mostDamager = new AbstractMap.SimpleEntry<String, Double>(pl.getName(), towerData.givenDamages);
				}
				if (mostDeath == null || (mostDeath != null && mostDeath.getValue() < towerData.deaths))
				{
					mostDeath = new AbstractMap.SimpleEntry<String, Integer>(pl.getName(), towerData.deaths);
				}
				if (mostObjective == null || (mostObjective != null && mostObjective.getValue() < towerData.marks))
				{
					mostObjective = new AbstractMap.SimpleEntry<String, Integer>(pl.getName(), towerData.marks);
				}
			}
			for (BadblockPlayer pl : BukkitUtils.getPlayers())
			{
				pl.sendMessage(" ");
				String mDamager = mostDamager == null ? pl.getTranslatedMessage("tower.infos.damager_no")[0] :
						pl.getTranslatedMessage("tower.infos.damager", mostDamager.getKey(), MathsUtils.round(mostDamager.getValue(), 2))[0];
				String mDeath = mostDeath == null ? pl.getTranslatedMessage("tower.infos.death_no")[0] :
						pl.getTranslatedMessage("tower.infos.death", mostDeath.getKey(), mostDeath.getValue())[0];
				String mObjective = mostObjective == null ? pl.getTranslatedMessage("tower.infos.objective_no")[0] :
						pl.getTranslatedMessage("tower.infos.objective", mostObjective.getKey(), mostObjective.getValue())[0];
				pl.sendMessage(mDamager);
				pl.sendMessage(mDeath);
				pl.sendMessage(mObjective);
				pl.sendMessage(" ");
			}

			new TowerResults(TimeUnit.SECOND.toShort(time, TimeUnit.SECOND, TimeUnit.HOUR), winner);
			new EndEffectRunnable(winnerLocation, winner).runTaskTimer(GameAPI.getAPI(), 0, 1L);
			new KickRunnable().runTaskTimer(GameAPI.getAPI(), 0, 20L);

		} else if(size == 0){
			cancel();
			Bukkit.shutdown();
			return;
		}

	}

	private static void incrementAchievements(BadblockPlayer player, PlayerAchievement... achievements){
		for(PlayerAchievement achievement : achievements){
			PlayerAchievementState state = player.getPlayerData().getAchievementState(achievement);
			state.progress(1.0d);
			state.trySucceed(player, achievement);
		}
		player.saveGameData();
	}
}
