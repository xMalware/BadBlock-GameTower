package fr.badblock.bukkit.games.tower.configuration;

import java.util.List;

import fr.badblock.gameapi.configuration.values.MapLocation;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TowerConfiguration {
	public String 	   		   fallbackServer   = "lobby";
	public String			   defaultKit		= "defaultKit";
	public int				   neededPoints		= 10;
	public boolean			   enabledAutoTeamManager = false;
	public int				   minPlayersAutoTeam = 1;
	public int				   maxPlayersAutoTeam = 4;
	public int    	   		   maxPlayersInTeam = 4;
	public int    	   		   minPlayers		= 2;
	public int    	   		   time				= 15;
	public List<MapLocation>   spawn			= null;
}
