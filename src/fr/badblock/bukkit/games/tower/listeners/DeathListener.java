package fr.badblock.bukkit.games.tower.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.TowerAchievementList;
import fr.badblock.bukkit.games.tower.entities.TowerTeamData;
import fr.badblock.bukkit.games.tower.players.TowerData;
import fr.badblock.bukkit.games.tower.players.TowerScoreboard;
import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.achievements.PlayerAchievement;
import fr.badblock.gameapi.events.fakedeaths.FakeDeathEvent;
import fr.badblock.gameapi.events.fakedeaths.FightingDeathEvent;
import fr.badblock.gameapi.events.fakedeaths.FightingDeathEvent.FightingDeaths;
import fr.badblock.gameapi.events.fakedeaths.NormalDeathEvent;
import fr.badblock.gameapi.events.fakedeaths.PlayerFakeRespawnEvent;
import fr.badblock.gameapi.game.rankeds.RankedManager;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.data.PlayerAchievementState;
import fr.badblock.gameapi.utils.i18n.messages.GameMessages;

public class DeathListener extends BadListener {
	@EventHandler
	public void onDeath(NormalDeathEvent e){
		death(e, e.getPlayer(), null, e.getLastDamageCause());
		e.setDeathMessage(GameMessages.deathEventMessage(e));
	}

	@EventHandler
	public void onDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (event.getDamager().getType().equals(EntityType.PLAYER))
		{
			BadblockPlayer player = (BadblockPlayer) event.getDamager();
			TowerData towerData = player.inGameData(TowerData.class);
			if (towerData == null)
			{
				return;
			}
			towerData.givenDamages += event.getDamage();
		}
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
		if (e.getPlayer().getOpenInventory() != null && e.getPlayer().getOpenInventory().getCursor() != null)
			e.getPlayer().getOpenInventory().setCursor(null);
		if (GameAPI.getServerName().startsWith("towerE_"))
		{
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
		}
		PluginTower.getInstance().giveDefaultKit(e.getPlayer());
	}

	private Map<String, Long> lastDeath = new HashMap<>();

	private void death(FakeDeathEvent e, BadblockPlayer player, Entity killer, DamageCause last){
		if(player.getTeam() == null) return; //WTF
		if (lastDeath.containsKey(player.getName())) {
			if (lastDeath.get(player.getName()) > System.currentTimeMillis()) {
				e.setCancelled(true);
				return;
			}
		}
		lastDeath.put(player.getName(), System.currentTimeMillis() + 1000L);

		if (player.getOpenInventory() != null && player.getOpenInventory().getCursor() != null)
			player.getOpenInventory().setCursor(null);
		Location respawnPlace = null;
		player.getPlayerData().incrementStatistic("tower", TowerScoreboard.DEATHS);
		player.getPlayerData().incrementTempRankedData(RankedManager.instance.getCurrentRankedGameName(), TowerScoreboard.DEATHS, 1);
		player.inGameData(TowerData.class).deaths++;
		player.getCustomObjective().generate();

		e.setLightning(true);
		respawnPlace = player.getTeam().teamData(TowerTeamData.class).getRespawnLocation();

		if(killer != null){
			e.setWhileRespawnPlace(killer.getLocation());
		} else if(last == DamageCause.VOID){
			e.setWhileRespawnPlace(respawnPlace);
		}

		if(killer != null && killer.getType() == EntityType.PLAYER){
			BadblockPlayer bKiller = (BadblockPlayer) killer;
			bKiller.getPlayerData().incrementStatistic("tower", TowerScoreboard.KILLS);
			bKiller.getPlayerData().incrementTempRankedData(RankedManager.instance.getCurrentRankedGameName(), TowerScoreboard.KILLS, 1);
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
		player.saveGameData();
	}
}
