package fr.badblock.tower.players;

import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.BadblockMode;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.players.scoreboard.BadblockScoreboardGenerator;
import fr.badblock.gameapi.players.scoreboard.CustomObjective;
import fr.badblock.tower.entities.TowerTeamData;
import fr.badblock.tower.runnables.StartRunnable;

public class TowerScoreboard extends BadblockScoreboardGenerator {
	public static final String WINS 	  = "wins",
							   KILLS 	  = "kills",
							   DEATHS 	  = "deaths",
							   LOOSES 	  = "looses",
							   MARKS	  = "marks";
	
	private CustomObjective objective;
	private BadblockPlayer  player;

	public TowerScoreboard(BadblockPlayer player){
		this.objective = GameAPI.getAPI().buildCustomObjective("tower");
		this.player    = player;

		objective.showObjective(player);
		objective.setDisplayName("&b&o" + GameAPI.getGameName());
		objective.setGenerator(this);

		objective.generate();
	}
	
	@Override
	public void generate(){
		objective.changeLine(15, "&8&m----------------------");

		int i = 14;
		
		if(StartRunnable.gameTask != null){
			objective.changeLine(i--,  i18n("tower.scoreboard.time-desc"));
			objective.changeLine(i--,  i18n("tower.scoreboard.time", time(StartRunnable.gameTask.getTime()) ));
		}
		
		objective.changeLine(i--, "");
		
		for(BadblockTeam team : GameAPI.getAPI().getTeams()){
			TowerTeamData data = team.teamData(TowerTeamData.class);
			objective.changeLine(i, team.getChatName().getAsLine(player) + " > &7" + data.getMarks());
			i--;
		}

		if(player.getBadblockMode() != BadblockMode.SPECTATOR){
			objective.changeLine(i,  ""); i--;

			objective.changeLine(i,  i18n("tower.scoreboard.wins", stat(WINS))); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.kills", stat(KILLS))); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.deaths", stat(DEATHS))); i--;
			objective.changeLine(i,  i18n("tower.scoreboard.marks", stat(MARKS))); i--;
		}

		for(int a=3;a<=i;a++)
			objective.removeLine(a);

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
