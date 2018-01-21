package fr.badblock.bukkit.games.tower.players;

import java.io.File;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.entities.TowerTeamData;
import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.game.rankeds.RankedManager;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.BadblockMode;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.players.scoreboard.BadblockScoreboardGenerator;
import fr.badblock.gameapi.players.scoreboard.CustomObjective;
import fr.badblock.gameapi.utils.ColorConverter;
import fr.badblock.gameapi.utils.general.Callback;
import fr.badblock.gameapi.utils.general.MathsUtils;

public class TowerScoreboard extends BadblockScoreboardGenerator {
	public static final String WINS 	  = "wins",
			KILLS 	  = "kills",
			DEATHS 	  = "deaths",
			LOOSES 	  = "looses",
			MARKS	  = "marks";

	private CustomObjective objective;
	private BadblockPlayer  player;

	private int totalRank	= -1;
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
		RankedManager.instance.getTotalRank(rankedGameName, player, new Callback<Integer>()
		{

			@Override
			public void done(Integer result, Throwable error) {
				totalRank = result.intValue();
			}

		});
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
		doBadblockFooter(objective);
	}

	@Override
	public void generate(){
		if (StartRunnable.gameTask != null)
		{
			String gameName = GameAPI.getGameName();
			if (run)
			{
				gameName = "§6§lTowerRun";
			}
			else
			{
				gameName = "§6§lTower";
			}
			objective.setDisplayName("§6" + gameName + " §d>> §b" + time(StartRunnable.gameTask.getTime()));
		}
		objective.changeLine(16, "&8&m----------------------");
		objective.changeLine(15, " ");

		int i = 14;

		int teams = 0;
		for(BadblockTeam team : GameAPI.getAPI().getTeams()){
			TowerTeamData data = team.teamData(TowerTeamData.class);
			String prefix = "";
			if (team.getOnlinePlayers().contains(player))
			{
				prefix += ColorConverter.dyeToChat(team.getDyeColor()) + "§l➔ ";
			}
			objective.changeLine(i, prefix + team.getChatName().getAsLine(player) + "§d(" + team.getOnlinePlayers().size() + ") §8> &b" + data.getMarks());
			i--;
			teams++;
		}

		if(player.getBadblockMode() != BadblockMode.SPECTATOR){
			objective.changeLine(i, " "); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.monthrank", monthRank)); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.totalrank", totalRank)); i--;
			objective.changeLine(i, " "); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.kills", stat(KILLS))); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.deaths", stat(DEATHS))); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.ratio", MathsUtils.round((double) stat(KILLS) / (double) Math.max(1, (double) stat(DEATHS)), 2))); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.marks", stat(MARKS))); i--;
			if (teams != 4)
			{
				objective.changeLine(i,  i18n("tower.scoreboard.wins", stat(WINS))); i--;
				objective.changeLine(i,  i18n("tower.scoreboard.looses", stat(LOOSES))); i--;
			}
		}


		objective.changeLine(2,  "&8&m----------------------");
	}

	private String time(int time){
		String res = "m";
		int    sec = time % 60;

		res = (time / 60) + res;
		if(sec < 10){
			res += "0";
		}

		return res + sec + "s";
	}

	private int stat(String name){
		return (int) player.getPlayerData().getStatistics("tower", name);
	}

	private String i18n(String key, Object... args){
		return GameAPI.i18n().get(player.getPlayerData().getLocale(), key, args)[0];
	}
}
