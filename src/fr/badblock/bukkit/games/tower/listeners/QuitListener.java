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
		if (StartRunnable.gameTask == null && BukkitUtils.getPlayers().size() - 1 < PluginTower.getInstance().getConfiguration().minPlayers) {
			StartRunnable.stopGame();
			StartRunnable.time = 60;
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
