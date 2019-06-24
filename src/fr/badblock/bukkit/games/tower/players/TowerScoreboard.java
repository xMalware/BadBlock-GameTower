package fr.badblock.bukkit.games.tower.players;

import java.io.File;

import org.bukkit.ChatColor;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.entities.TowerTeamData;
import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.game.rankeds.RankedManager;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.players.scoreboard.BadblockScoreboardGenerator;
import fr.badblock.gameapi.players.scoreboard.CustomObjective;
import fr.badblock.gameapi.utils.general.Callback;

public class TowerScoreboard extends BadblockScoreboardGenerator {
	public static final String WINS 	  = "wins",
			KILLS 	  = "kills",
			DEATHS 	  = "deaths",
			LOOSES 	  = "looses",
			MARKS	  = "marks";

	private CustomObjective objective;
	private BadblockPlayer  player;

	private int monthRank	= -1;

	public static boolean run = false;

	static
	{
		File file = new File(PluginTower.getInstance().getDataFolder(), "run.json");
		if (file.exists())
		{
			run = true;
		}
	}

	public TowerScoreboard(BadblockPlayer player){
		this.objective = GameAPI.getAPI().buildCustomObjective("tower");
		this.player    = player;

		objective.showObjective(player);

		String rankedGameName = RankedManager.instance.getCurrentRankedGameName();
		
		RankedManager.instance.getMonthRank(rankedGameName, player, new Callback<Integer>()
		{

			@Override
			public void done(Integer result, Throwable error) {
				monthRank = result.intValue();
			}

		});

		String gameName = GameAPI.getGameName();
		if (run)
		{
			gameName = "§6§lTowerRun";
		}
		else
		{
			gameName = "§6§lTower";
		}
		objective.setDisplayName(gameName);
		objective.setGenerator(this);

		objective.generate();
	}

	@Override
	public void generate(){
		if (StartRunnable.gameTask != null)
		{
			String gameName = GameAPI.getGameName();
			if (run)
			{
				gameName = "TowerRun";
			}
			else
			{
				gameName = "Tower";
			}
			objective.setDisplayName("§l§6§ §b§l" + gameName + " §l§6§");
		}

		int i = 16;

		/*int teams = 0;
		for(BadblockTeam team : GameAPI.getAPI().getTeams()){
			TowerTeamData data = team.teamData(TowerTeamData.class);
			String prefix = "";
			if (team.getOnlinePlayers().contains(player))
			{
				prefix += ColorConverter.dyeToChat(team.getDyeColor()) + "§l➔ ";
			}
			objective.changeLine(i, prefix + team.getChatName().getAsLine(player) + "§d(" + team.getOnlinePlayers().size() + ") §8> §b" + data.getMarks());
			i--;
			teams++;
		}*/
		String groupColor = player.getGroupPrefix().getAsLine(player);
		groupColor = groupColor.replace(ChatColor.stripColor(groupColor), "");
		i--;
		objective.changeLine(i, "§6 " + groupColor + "§n§l" + player.getName());
		i--;
		objective.changeLine(i, "§6 ");
		i--;
		objective.changeLine(i, "§6§a§l Rang: §6" + player.getGroupPrefix().getAsLine(player));
		i--;
		objective.changeLine(i, "§6§a§l Niveau: §6" + player.getPlayerData().getLevel());
		i--;
		if (monthRank == -1)
		{
			objective.changeLine(i, "§6§a§l Classement: §6?");
		}
		else
		{
			objective.changeLine(i, "§6§a§l Classement: §6" + monthRank + " ");

		}
		i--;
		objective.changeLine(i, "§6 ");
		i--;
		BadblockTeam currentTeam = player.getTeam();
		String teamName = "§7Inconnu";
		if (currentTeam != null)
		{
			teamName = currentTeam.getChatPrefix().getAsLine(player);
		}
		objective.changeLine(i, "§6§b§l Map: §6" + GameAPI.getAPI().getBadblockScoreboard().getWinner().getDisplayName());
		i--;
		objective.changeLine(i, "§6§b§l Points maximum: §6" + PluginTower.getInstance().getConfiguration().neededPoints);
		i--;
		objective.changeLine(i, "§6§b§l Equipe: " + teamName);
		i--;
		if (StartRunnable.gameTask != null)
		{
			objective.changeLine(i, "§6§b§l En cours: §6" + time(StartRunnable.gameTask.getTime()));
		}
		else
		{
			objective.changeLine(i, "§6§b§l Lancement...");
		}
		i--;
		objective.changeLine(i, "§6  ");
		i--;
		for(BadblockTeam team : GameAPI.getAPI().getTeams()){
			TowerTeamData data = team.teamData(TowerTeamData.class);
			objective.changeLine(i, "§6§b Team " + team.getChatName().getAsLine(player) + "§e> §a" + data.getMarks() + "pts");
			i--;
		}
		objective.changeLine(i, "§6 ");
		i--;
		objective.changeLine(i, "§6 " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "IP: play.badblock.fr");
		i--;
	}

	public static String time(int time){
		String res = "m";
		int    sec = time % 60;

		res = (time / 60) + res;
		if(sec < 10){
			res += "0";
		}

		return res + sec + "s";
	}

}
