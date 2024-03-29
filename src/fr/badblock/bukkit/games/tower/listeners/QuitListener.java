package fr.badblock.bukkit.games.tower.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.players.TowerScoreboard;
import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.game.rankeds.RankedCalc;
import fr.badblock.gameapi.game.rankeds.RankedManager;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.BadblockMode;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.utils.i18n.TranslatableString;
import fr.badblock.gameapi.utils.i18n.messages.GameMessages;

public class QuitListener extends BadListener {
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		PluginTower tower = PluginTower.getInstance();
		if (StartRunnable.gameTask == null && Bukkit.getOnlinePlayers().size() - 1 < tower.getConfiguration().minPlayers) {
			StartRunnable.stopGame();
			StartRunnable.time = StartRunnable.time > 30 ? StartRunnable.time : 30;
		}

		BadblockPlayer player = (BadblockPlayer) e.getPlayer();
		if (!player.getGameMode().equals(GameMode.SPECTATOR) && !player.getBadblockMode().equals(BadblockMode.SPECTATOR))
		{
			GameMessages.quitMessage(GameAPI.getGameName(), player.getTabGroupPrefix().getAsLine(player) + player.getName(), Bukkit.getOnlinePlayers().size(), PluginTower.getInstance().getMaxPlayers()).broadcast();
		}
		
		if (!inGame()) return;
		
		BadblockTeam   team   = player.getTeam();

		if(team == null) return;

		String rankedGameName = RankedManager.instance.getCurrentRankedGameName();
		player.getPlayerData().incrementTempRankedData(rankedGameName, TowerScoreboard.LOOSES, 1);
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
		RankedManager.instance.fill(rankedGameName);
		
		if(team.getOnlinePlayers().size() == 0){
			GameAPI.getAPI().getGameServer().cancelReconnectionInvitations(team);
			GameAPI.getAPI().unregisterTeam(team);
			
			new TranslatableString("tower.team-loose", team.getChatName()).broadcast();
		}
	}
}
