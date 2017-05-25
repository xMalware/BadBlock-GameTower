package fr.badblock.bukkit.games.tower.configuration;

import org.bukkit.Bukkit;

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
	public MapLocation 		   spawn			= new MapLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
}
