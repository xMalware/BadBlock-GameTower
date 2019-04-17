package fr.badblock.bukkit.games.tower.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

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
import fr.badblock.gameapi.utils.BukkitUtils;
import fr.badblock.gameapi.utils.i18n.messages.GameMessages;

public class DeathListener extends BadListener {
	@EventHandler
	public void onDeath(NormalDeathEvent e){
		death(e, e.getPlayer(), null, e.getLastDamageCause());
		e.setDeathMessage(GameMessages.deathEventMessage(e));
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onDamageCheck(EntityDamageByEntityEvent event)
	{
		work(event);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onDamageCheckHighest(EntityDamageByEntityEvent event)
	{
		work(event);
	}
	

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onDamageCheck(EntityDamageEvent event)
	{
		if (event.getEntity().getType() != EntityType.PLAYER)
		{
			return;
		}
		
		if (event.getCause().equals(DamageCause.SUFFOCATION) || event.getCause().equals(DamageCause.FALLING_BLOCK)
				|| event.getCause().equals(DamageCause.CONTACT))
		{
			event.setCancelled(true);
			return;
		}
	}
	
	private void work(EntityDamageByEntityEvent event)
	{
		
		BadblockPlayer damager = null;
		
		if (!event.getDamager().getType().equals(EntityType.PLAYER))
		{
			ProjectileSource entity = null;
			if (event.getDamager() instanceof Projectile)
			{
				entity = ((Projectile) event.getDamager()).getShooter();
			}
			
			if (entity == null || !(entity instanceof Player))
			{
				return;
			}
			
			damager = (BadblockPlayer) entity;
		}
		else
		{
			damager = (BadblockPlayer) event.getDamager();
		}

		BadblockPlayer damaged = (BadblockPlayer) event.getEntity();
		
		if (damager.getTeam() != null && damaged.getTeam() != null && damager.getTeam().equals(damaged.getTeam()))
		{
			event.setCancelled(true);
		}
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
		if (GameAPI.getServerName().startsWith("towerf_"))
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
		TowerData victimData = player.inGameData(TowerData.class);
		victimData.deaths++;

		int tValid = 0;
		for (long l : victimData.lastKills.values())
		{
			if (l > System.currentTimeMillis())
			{
				tValid++;
			}
		}

		if (tValid >= 3)
		{
			final int finalValid = tValid;
			if(killer != null)
			{
				Sound sound = tValid == 2 ? Sound.ZOMBIE_IDLE : tValid == 3 ? Sound.ZOMBIE_PIG_ANGRY :
					tValid == 4 ? Sound.HORSE_ANGRY : tValid == 5 ? Sound.DONKEY_ANGRY : Sound.ENDERMAN_SCREAM;
				BukkitUtils.forEachPlayers(plo -> plo.sendTranslatedMessage("game.killserie.stopped", player.getName(), finalValid, killer.getName()));
				BukkitUtils.forEachPlayers(plo -> plo.playSound(sound));
			}
			else
			{
				BukkitUtils.forEachPlayers(plo -> plo.sendTranslatedMessage("game.killserie.stoppeddeath", player.getName(), finalValid));
			}
		}

		victimData.lastKills.clear();
		player.getCustomObjective().generate();

		e.setLightning(true);

		if (TowerScoreboard.run)
		{
			if (e.getDrops() != null)
			{
				Iterator<ItemStack> iterator = e.getDrops().iterator();
				while (iterator.hasNext())
				{
					ItemStack item = iterator.next();

					if (!Material.GOLDEN_APPLE.equals(item.getType()))
					{
						iterator.remove();
					}
				}
			}
		}

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
			TowerData data = bKiller.inGameData(TowerData.class);
			data.kills++;

			if (!data.lastKills.containsKey(player.getName()) || data.lastKills.get(player.getName()) < System.currentTimeMillis())
			{
				bKiller.inGameData(TowerData.class).lastKills.put(player.getName(), System.currentTimeMillis() + 8000);

				int valid = 0;
				for (long l : data.lastKills.values())
				{
					if (l > System.currentTimeMillis())
					{
						valid++;
					}
				}

				if (valid > 1)
				{
					final int finalValid = valid;

					String killName = valid == 2 ? "double" : valid == 3 ? "triple" : valid == 4 ? "quadruple" : valid == 5 ?
							"quintuple" : valid == 6 ? "sextuple" : valid == 7 ? "septuple" : valid == 8 ? "octuple" : valid == 9 ? "nonuple":
								valid == 10 ? "decuple" : "extra";
					Sound sound = valid == 2 ? Sound.ZOMBIE_IDLE : valid == 3 ? Sound.ZOMBIE_PIG_ANGRY :
						valid == 4 ? Sound.HORSE_ANGRY : valid == 5 ? Sound.DONKEY_ANGRY : Sound.ENDERMAN_SCREAM;
					BukkitUtils.forEachPlayers(plo -> plo.sendTranslatedMessage("game.killserie." + killName, bKiller.getName(), finalValid));
					BukkitUtils.forEachPlayers(plo -> plo.playSound(sound));
				}
			}
			else
			{
				data.lastKills.put(player.getName(), System.currentTimeMillis() + 120000);
			}

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
