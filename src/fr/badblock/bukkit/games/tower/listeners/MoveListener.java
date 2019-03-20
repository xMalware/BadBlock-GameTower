package fr.badblock.bukkit.games.tower.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.entities.TowerTeamData;
import fr.badblock.bukkit.games.tower.runnables.GameRunnable;
import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.BadblockMode;
import fr.badblock.gameapi.players.BadblockTeam;

public class MoveListener extends BadListener {
	@EventHandler
	public void onMove(PlayerMoveEvent e){
		BadblockPlayer player = (BadblockPlayer) e.getPlayer();
		
		if (StartRunnable.time <= 3 && GameRunnable.freezeOnBlocks.containsKey(player))
		{
			if (!GameRunnable.freezeOnBlocks.get(player).getBlock().equals(e.getTo().getBlock()))
			{
				e.setTo(GameRunnable.freezeOnBlocks.get(player));
			}
			return;
		}
		
		if(e.getTo().getY() <= 0.0d && !inGame()){
			Location spawn = PluginTower.getInstance().spawn.getHandle();

			Entity vehicle = null;

			if(e.getPlayer().isInsideVehicle()){
				vehicle = e.getPlayer().getVehicle();
				vehicle.eject();
				vehicle.teleport(spawn);
			}

			e.setCancelled(true);
			e.getPlayer().teleport(spawn);

			if(vehicle != null)
				vehicle.setPassenger(e.getPlayer());
		}else if(e.getTo().getY() <= 0.0d && inGame()){
			if (player.getBadblockMode().equals(BadblockMode.PLAYER)) {
				@SuppressWarnings("deprecation")
				EntityDamageEvent event = new EntityDamageEvent(player, EntityDamageEvent.DamageCause.VOID, player.getHealth());
				player.setLastDamageCause(event);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}
		} else if(inGame()){
			if(player.getTeam() == null || player.getBadblockMode() != BadblockMode.PLAYER) return;

			for(BadblockTeam team : GameAPI.getAPI().getTeams()){
				if(team.teamData(TowerTeamData.class).getPool().isInSelection(e.getTo()) && !team.equals(player.getTeam())){
					player.getTeam().teamData(TowerTeamData.class).addMark(player);
				}
			}
		}
	}
}
