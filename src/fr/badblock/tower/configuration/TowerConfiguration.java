package fr.badblock.tower.configuration;

import org.bukkit.Bukkit;

import fr.badblock.gameapi.configuration.values.MapLocation;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TowerConfiguration {
	public String 	   		   fallbackServer   = "lobby";
	public String			   defaultKit		= "defaultKit";
	public int				   neededPoints		= 10;
	public int    	   		   maxPlayersInTeam = 4;
	public MapLocation 		   spawn			= new MapLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
}
