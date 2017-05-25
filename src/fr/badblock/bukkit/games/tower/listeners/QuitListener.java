package fr.badblock.bukkit.games.tower.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.utils.BukkitUtils;
import fr.badblock.gameapi.utils.i18n.TranslatableString;

public class QuitListener extends BadListener {
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		PluginTower tower = PluginTower.getInstance();
		if (StartRunnable.gameTask == null && BukkitUtils.getPlayers().size() - 1 < tower.getConfiguration().minPlayers) {
			StartRunnable.stopGame();
			StartRunnable.time = StartRunnable.time > 60 ? StartRunnable.time : 60;
		}
		if (BukkitUtils.getPlayers().size() - 1 < tower.getMaxPlayers() - tower.getAPI().getTeams().size()) {			
			if (tower.getConfiguration().enabledAutoTeamManager) {
				tower.getAPI().getTeams().forEach(team -> team.setMaxPlayers(team.getMaxPlayers() - 1));
				tower.setMaxPlayers(tower.getMaxPlayers() - tower.getAPI().getTeams().size());
				try {
					BukkitUtils.setMaxPlayers(tower.getMaxPlayers());
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
		}
		if(!inGame()) return;
		
		BadblockPlayer player = (BadblockPlayer) e.getPlayer();
		BadblockTeam   team   = player.getTeam();

		if(team == null) return;
		
		if(team.getOnlinePlayers().size() == 0){
			GameAPI.getAPI().getGameServer().cancelReconnectionInvitations(team);
			GameAPI.getAPI().unregisterTeam(team);
			
			new TranslatableString("tower.team-loose", team.getChatName()).broadcast();
		}
	}
}
