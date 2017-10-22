package fr.badblock.bukkit.games.tower.listeners;

import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.badblock.bukkit.games.tower.players.TowerData;
import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.utils.entities.CustomCreature;
import fr.badblock.gameapi.utils.entities.CustomCreature.CreatureBehaviour;
import fr.badblock.gameapi.utils.entities.CustomCreature.CreatureFlag;

public class PlayerMountListener extends BadListener {
	@EventHandler
	public void onRightClick(PlayerInteractEntityEvent e){
		if(inGame()) return;
		
		BadblockPlayer player = (BadblockPlayer) e.getPlayer();
		TowerData	   data   = player.inGameData(TowerData.class);
		
		if(e.getRightClicked().getType() != EntityType.PLAYER)
			return;
		if(data.cooldown > System.currentTimeMillis())
			return;
		
		data.cooldown = System.currentTimeMillis() + 1_000;
		mount(e.getRightClicked(), player);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if(!inGame() && e.getAction() == Action.LEFT_CLICK_AIR && e.getPlayer().getPassenger() != null){
			unmount(e.getPlayer(), e.getPlayer().getLocation().getDirection().multiply(2.0d));
		}
	}
	
	private Entity getTop(Entity e){
		while(e.getPassenger() != null){
			e = e.getPassenger();
		}
		
		return e;
	}
	
	private void mount(Entity base, Entity to){
		if (StartRunnable.time <= 5)
		{
			if (to.getType().equals(EntityType.PLAYER))
			{
				BadblockPlayer player = (BadblockPlayer) to;
				player.sendTranslatedMessage("game.youcantmountfornow");
			}
			return;
		}
		Entity e = null;
		
		int count = 0;
		
		while((e = to.getPassenger()) != null){
			count++;
			
			if(base.equals(e))
				return;
		
			if(count == 10) return;
		}
		
		to = getTop(to);
		
		CustomCreature bat    = GameAPI.getAPI().spawnCustomEntity(to.getLocation(), EntityType.BAT);
		Bat			   entity = (Bat) bat.getBukkit();
		
		bat.setCreatureBehaviour(CreatureBehaviour.MOTIONLESS);
		bat.addCreatureFlags(CreatureFlag.INVINCIBLE, CreatureFlag.FIREPROOF);
		
		entity.setPassenger(base);
		to.setPassenger(entity);
		
		new BukkitRunnable() {
			private int time = 10;
			
			@Override
			public void run() {
				time--;
				
				bat.addCreatureFlag(CreatureFlag.INVISIBLE);
			
				if(time == 0){
					cancel();
				}
			}
		}.runTaskTimer(GameAPI.getAPI(), 2L, 2L);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(entity.getVehicle() == null || entity.getPassenger() == null)
					entity.remove();
			}
		}.runTaskTimer(GameAPI.getAPI(), 2L, 2L);
	}
	
	private void unmount(Entity entity, Vector vel){
		if(entity.getPassenger() != null){
			
			Entity passenger = entity.getPassenger();
			entity.eject();
			
			passenger.setVelocity(vel);
			
			if(passenger.getType() != EntityType.PLAYER){
				unmount(passenger, vel);
				
				if(passenger.getType() == EntityType.BAT)
					passenger.remove();
			}
			
		}
	}
}
