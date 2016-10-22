package fr.badblock.tower.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.achievements.PlayerAchievement;
import fr.badblock.gameapi.events.fakedeaths.FakeDeathEvent;
import fr.badblock.gameapi.events.fakedeaths.FightingDeathEvent;
import fr.badblock.gameapi.events.fakedeaths.FightingDeathEvent.FightingDeaths;
import fr.badblock.gameapi.events.fakedeaths.NormalDeathEvent;
import fr.badblock.gameapi.events.fakedeaths.PlayerFakeRespawnEvent;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.data.PlayerAchievementState;
import fr.badblock.gameapi.utils.i18n.messages.GameMessages;
import fr.badblock.tower.PluginTower;
import fr.badblock.tower.TowerAchievementList;
import fr.badblock.tower.entities.TowerTeamData;
import fr.badblock.tower.players.TowerData;
import fr.badblock.tower.players.TowerScoreboard;

public class DeathListener extends BadListener {
	@EventHandler
	public void onDeath(NormalDeathEvent e){
		death(e, e.getPlayer(), null, e.getLastDamageCause());
		e.setDeathMessage(GameMessages.deathEventMessage(e));
	}

	@EventHandler
	public void onDeath(FightingDeathEvent e){
		death(e, e.getPlayer(), e.getKiller(), e.getLastDamageCause());
		e.setDeathMessage(GameMessages.deathEventMessage(e));

		if(e.getKiller().getType() == EntityType.PLAYER){
			BadblockPlayer killer = (BadblockPlayer) e.getKiller();
			incrementAchievements(killer, TowerAchievementList.TOWER_KILL_1, TowerAchievementList.TOWER_KILL_2, TowerAchievementList.TOWER_KILL_3, TowerAchievementList.TOWER_KILL_4, TowerAchievementList.TOWER_KILLER, TowerAchievementList.TOWER_UKILLER);

			if(e.getFightType() == FightingDeaths.BOW){
				incrementAchievements(killer, TowerAchievementList.TOWER_SHOOTER, TowerAchievementList.TOWER_USHOOTER);
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerFakeRespawnEvent e){
		PluginTower.getInstance().giveDefaultKit(e.getPlayer());
	}

	private void death(FakeDeathEvent e, BadblockPlayer player, Entity killer, DamageCause last){
		if(player.getTeam() == null) return; //WTF

		Location respawnPlace = null;

		player.getPlayerData().incrementStatistic("tower", TowerScoreboard.DEATHS);
		player.inGameData(TowerData.class).deaths++;
		player.getCustomObjective().generate();

		e.setTimeBeforeRespawn(3);
		respawnPlace = player.getTeam().teamData(TowerTeamData.class).getRespawnLocation();

		if(killer != null){
			e.setWhileRespawnPlace(killer.getLocation());
		} else if(last == DamageCause.VOID){
			e.setWhileRespawnPlace(respawnPlace);
		}

		if(killer != null && killer.getType() == EntityType.PLAYER){
			BadblockPlayer bKiller = (BadblockPlayer) killer;
			bKiller.getPlayerData().incrementStatistic("tower", TowerScoreboard.KILLS);
			bKiller.inGameData(TowerData.class).kills++;

			bKiller.getCustomObjective().generate();
		}

		player.getCustomObjective().generate();
		e.setRespawnPlace(respawnPlace);
	}

	private void incrementAchievements(BadblockPlayer player, PlayerAchievement... achievements){
		for(PlayerAchievement achievement : achievements){
			PlayerAchievementState state = player.getPlayerData().getAchievementState(achievement);
			state.progress(1.0d);
			state.trySucceed(player, achievement);
		}
	}
}
