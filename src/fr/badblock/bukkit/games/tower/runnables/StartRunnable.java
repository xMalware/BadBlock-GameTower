package fr.badblock.bukkit.games.tower.runnables;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.configuration.TowerMapConfiguration;
import fr.badblock.bukkit.games.tower.players.TowerScoreboard;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.utils.i18n.TranslatableString;
import fr.badblock.gameapi.utils.i18n.messages.GameMessages;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StartRunnable extends BukkitRunnable {
	public static final int 		  TIME_BEFORE_START = PluginTower.getInstance().getConfiguration().time;
	public static     	StartRunnable task 		        = null;
	public static    	GameRunnable  gameTask		    = null;

	public static int time	= TIME_BEFORE_START;
	
	public static TowerMapConfiguration config;

	@Override
	public void run() {
		GameAPI.setJoinable(time >= 10);
		if(time == 0){
			for(Player player : Bukkit.getOnlinePlayers()){
				BadblockPlayer bPlayer = (BadblockPlayer) player;
				bPlayer.playSound(Sound.ORB_PICKUP);
			}

			cancel();
		} else if(time % 10 == 0 || time <= 5 || time == 15){
			sendTime(time);
		}

		if (time == 4)
		{
			GameAPI.getAPI().balanceTeams(true);
			GameAPI.getAPI().getBadblockScoreboard().endVote();
			
			for(Player player : Bukkit.getOnlinePlayers()){
				new TowerScoreboard((BadblockPlayer) player);
			}
			
			String winner = GameAPI.getAPI().getBadblockScoreboard().getWinner().getInternalName();
			File   file   = new File(PluginTower.MAP, winner + ".json");

			TowerMapConfiguration tmpConfig = new TowerMapConfiguration(GameAPI.getAPI().loadConfiguration(file));
			tmpConfig.save(file);
			PluginTower.getInstance().setMapConfiguration(tmpConfig);
			
			config = tmpConfig;

			gameTask = new GameRunnable(config);
			gameTask.runTaskTimer(GameAPI.getAPI(), 3 * 20L, 20L);
		}

		sendTimeHidden(time);

		time--;
	}

	protected void start(int t){
		time = t;
		
		sendTime(time);

		runTaskTimer(GameAPI.getAPI(), 0, 20L);
	}
	
	protected void start(){
		start(time);
	}

	private void sendTime(int time){
		ChatColor color = getColor(time);

		for(Player player : Bukkit.getOnlinePlayers()){
			BadblockPlayer bPlayer = (BadblockPlayer) player;

			bPlayer.playSound(Sound.NOTE_PLING);

			bPlayer.sendTranslatedMessage("tower.startingtimeleft", color + ""  + time, time > 1 ? "s" : "");
			bPlayer.sendTitle(color + "" + ChatColor.BOLD + "" + time, "");
			bPlayer.sendTimings(0, 20 * 5, 0);
		}
	}

	private void sendTimeHidden(int time){
		ChatColor color = getColor(time);
		TranslatableString actionbar = GameMessages.startInActionBar(time, color);

		for(Player player : Bukkit.getOnlinePlayers()){
			BadblockPlayer bPlayer = (BadblockPlayer) player;

			if(time > 0)
				bPlayer.sendTranslatedActionBar(actionbar.getKey(), actionbar.getObjects());
			bPlayer.setLevel(time);
			bPlayer.setExp(0.0f);
		}
	}

	private ChatColor getColor(int time){
		if(time == 1)
			return ChatColor.DARK_RED;
		else if(time <= 5)
			return ChatColor.RED;
		else return ChatColor.AQUA;
	}

	public static void joinNotify(int currentPlayers, int maxPlayers){
		if ((!GameAPI.getAPI().isHostedGame() && currentPlayers + 1 < PluginTower.getInstance().getMinPlayers())
				|| (GameAPI.getAPI().isHostedGame() && currentPlayers + 1 < PluginTower.getInstance().getMaxPlayers())) return;

		startGame();
		int t = 30;
		if (time >= t && (Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers() || 
				(PluginTower.getInstance().getConfiguration().enabledAutoTeamManager && Bukkit.getOnlinePlayers().size() 
						>= PluginTower.getInstance().getConfiguration().maxPlayersAutoTeam * PluginTower.getInstance().getAPI().getTeams().size()))) {
			time = t;
		}
	}

	public static void startGame(int t){
		if(task == null){
			task = new StartRunnable();
			task.start(t);
		}
	}
	
	public static void startGame(){
		if(task == null){
			task = new StartRunnable();
			task.start();
		}
	}

	public static void stopGame(){
		if(gameTask != null){
			gameTask.forceEnd = true;
			time = TIME_BEFORE_START;
		} else if(task != null){
			task.cancel();
			time = time > 15 ? time : 15;
			GameAPI.setJoinable(true);
		}

		task = null;
		gameTask = null;
	}

	public static boolean started(){
		return task != null;
	}
}
