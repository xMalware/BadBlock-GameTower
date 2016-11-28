package fr.badblock.tower.runnables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.scheduler.BukkitRunnable;

import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.achievements.PlayerAchievement;
import fr.badblock.gameapi.game.GameState;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.BadblockMode;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.players.data.InGameKitData;
import fr.badblock.gameapi.players.data.PlayerAchievementState;
import fr.badblock.gameapi.players.kits.PlayerKit;
import fr.badblock.gameapi.players.scoreboard.CustomObjective;
import fr.badblock.gameapi.utils.BukkitUtils;
import fr.badblock.gameapi.utils.general.TimeUnit;
import fr.badblock.gameapi.utils.i18n.TranslatableString;
import fr.badblock.tower.PluginTower;
import fr.badblock.tower.TowerAchievementList;
import fr.badblock.tower.configuration.TowerMapConfiguration;
import fr.badblock.tower.entities.TowerTeamData;
import fr.badblock.tower.players.TowerData;
import fr.badblock.tower.players.TowerScoreboard;
import fr.badblock.tower.result.TowerResults;
import lombok.Getter;

public class GameRunnable extends BukkitRunnable {
	public static final int MAX_TIME = 60 * 30;
	public static boolean damage = false;

	public boolean forceEnd 		 = false;
	@Getter
	private int    time 			 = MAX_TIME;

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
				p.changePlayerDimension(BukkitUtils.getEnvironment( config.getDimension() ));
				p.teleport(location);
				p.setGameMode(GameMode.SURVIVAL);

				boolean good = true;

				for(PlayerKit toUnlock : PluginTower.getInstance().getKits().values()){
					if(!toUnlock.isVIP()){
						if(p.getPlayerData().getUnlockedKitLevel(toUnlock) < 2){
							good = false; break;
						}
					}
				}

				if(good){
					PlayerAchievementState state = p.getPlayerData().getAchievementState(TowerAchievementList.TOWER_ALLKITS);

					if(!state.isSucceeds()){
						state.succeed();
						TowerAchievementList.TOWER_ALLKITS.reward(p);
					}
				}

				PlayerKit kit = p.inGameData(InGameKitData.class).getChoosedKit();

				if(kit != null){
					if (PluginTower.getInstance().getMapConfiguration().getAllowBows())
						kit.giveKit(p);
					else
						kit.giveKit(p, Material.BOW, Material.ARROW);
				} else {
					PluginTower.getInstance().giveDefaultKit(p);
				}
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
		if(time == MAX_TIME - 2){
			damage = true;

			for(Player player : Bukkit.getOnlinePlayers()){
				BadblockPlayer bp = (BadblockPlayer) player;
				bp.pseudoJail(bp.getTeam().teamData(TowerTeamData.class).getRespawnLocation(), 300.0d);
			}
		} else if(time == 0){
			forceEnd = true;
		}

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

		if(size == 1 || forceEnd || (max != null && max.teamData(TowerTeamData.class).getMarks() == PluginTower.getInstance().getConfiguration().neededPoints)){
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

					incrementAchievements(bp, TowerAchievementList.TOWER_WIN_1, TowerAchievementList.TOWER_WIN_2, TowerAchievementList.TOWER_WIN_3, TowerAchievementList.TOWER_WIN_4);
				} else {
					bp.getPlayerData().addRankedPoints(-2);
					badcoins = ((double) badcoins) / 1.5d;

					bp.jailPlayerAt(looserLocation);
					bp.sendTranslatedTitle("tower.title-loose", winner.getChatName());

					if(bp.getBadblockMode() == BadblockMode.PLAYER)
						bp.getPlayerData().incrementStatistic("tower", TowerScoreboard.LOOSES);
				}

				if(badcoins > 20)
					badcoins = 20;
				if(xp > 50)
					xp = 50;

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

			new TowerResults(TimeUnit.SECOND.toShort(time, TimeUnit.SECOND, TimeUnit.HOUR), winner);
			new EndEffectRunnable(winnerLocation, winner).runTaskTimer(GameAPI.getAPI(), 0, 1L);
			new KickRunnable().runTaskTimer(GameAPI.getAPI(), 0, 20L);

		} else if(size == 0){
			cancel();
			Bukkit.shutdown();
			return;
		}

		time--;
	}

	private static void incrementAchievements(BadblockPlayer player, PlayerAchievement... achievements){
		for(PlayerAchievement achievement : achievements){
			PlayerAchievementState state = player.getPlayerData().getAchievementState(achievement);
			state.progress(1.0d);
			state.trySucceed(player, achievement);
		}
	}
}
