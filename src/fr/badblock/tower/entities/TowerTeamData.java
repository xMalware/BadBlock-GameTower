package fr.badblock.tower.entities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.badblock.gameapi.achievements.PlayerAchievement;
import fr.badblock.gameapi.configuration.BadConfiguration;
import fr.badblock.gameapi.configuration.values.MapLocation;
import fr.badblock.gameapi.configuration.values.MapSelection;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.data.PlayerAchievementState;
import fr.badblock.gameapi.players.data.TeamData;
import fr.badblock.gameapi.utils.selections.CuboidSelection;
import fr.badblock.tower.PluginTower;
import fr.badblock.tower.TowerAchievementList;
import fr.badblock.tower.players.TowerData;
import fr.badblock.tower.players.TowerScoreboard;
import lombok.Getter;

public class TowerTeamData implements TeamData {
	@Getter private Location 	   	  respawnLocation;
	@Getter private CuboidSelection	  pool, teamzone, spawnzone;
	@Getter	private int				  marks;	

	public void load(BadConfiguration config){
		respawnLocation = config.getValue("respawnLocation", MapLocation.class, new MapLocation()).getHandle();
		pool 	    	= config.getValue("pool", MapSelection.class, new MapSelection()).getHandle();
		teamzone 	    = config.getValue("teamzone", MapSelection.class, new MapSelection()).getHandle();
		spawnzone 	    = config.getValue("spawnzone", MapSelection.class, new MapSelection()).getHandle();
	}

	public void save(BadConfiguration config){
		config.setValue("respawnLocation", new MapLocation(respawnLocation));
		config.setValue("pool", new MapSelection(pool));
		config.setValue("teamzone", new MapSelection(teamzone));
		config.setValue("spawnzone", new MapSelection(spawnzone));
	}

	public void addMark(BadblockPlayer player){
		if(marks >= PluginTower.getInstance().getConfiguration().neededPoints){
			return;
		}

		if(player.inGameData(TowerData.class).nextMark > System.currentTimeMillis())
			return;

		player.inGameData(TowerData.class).marks++;
		player.inGameData(TowerData.class).nextMark = System.currentTimeMillis() + 10_000L;
		player.getPlayerData().incrementStatistic("tower", TowerScoreboard.MARKS);

		incrementAchievements(player, TowerAchievementList.TOWER_MARK_1, TowerAchievementList.TOWER_MARK_2, TowerAchievementList.TOWER_MARK_3, TowerAchievementList.TOWER_MARK_4);

		if(player.inGameData(TowerData.class).marks == PluginTower.getInstance().getConfiguration().neededPoints / 2){
			incrementAchievements(player, TowerAchievementList.TOWER_MARKER_1, TowerAchievementList.TOWER_MARKER_2, TowerAchievementList.TOWER_MARKER_3, TowerAchievementList.TOWER_MARKER_4);
		} else if(player.inGameData(TowerData.class).marks == PluginTower.getInstance().getConfiguration().neededPoints){
			PlayerAchievement	   achievement = TowerAchievementList.TOWER_MARKER;
			PlayerAchievementState state 	   = player.getPlayerData().getAchievementState(achievement);

			if(!state.isSucceeds()){
				state.succeed();
				achievement.reward(player);
			}
		}



		if(player.getVehicle() != null){
			player.getVehicle().eject();
		}

		player.teleport(player.getTeam().teamData(TowerTeamData.class).respawnLocation);
		player.heal();

		marks++;

		for(Player bukkitPlayer : Bukkit.getOnlinePlayers()){
			BadblockPlayer bPlayer = (BadblockPlayer) bukkitPlayer;
			
			if(bPlayer.getCustomObjective() != null)
				bPlayer.getCustomObjective().generate();

			bPlayer.sendTranslatedTitle("tower.mark-title", marks, player.getName(), player.getTeam().getChatName());
			bPlayer.sendTimings(10, 40, 10);
		}
	}

	private void incrementAchievements(BadblockPlayer player, PlayerAchievement... achievements){
		for(PlayerAchievement achievement : achievements){
			PlayerAchievementState state = player.getPlayerData().getAchievementState(achievement);
			state.progress(1.0d);
			state.trySucceed(player, achievement);
		}
	}
}
