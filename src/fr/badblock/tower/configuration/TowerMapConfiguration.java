package fr.badblock.tower.configuration;

import java.io.File;

import org.bukkit.Location;

import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.configuration.BadConfiguration;
import fr.badblock.gameapi.configuration.values.MapLocation;
import fr.badblock.gameapi.configuration.values.MapNumber;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.utils.selections.CuboidSelection;
import fr.badblock.tower.entities.TowerTeamData;
import lombok.Data;

@Data
public class TowerMapConfiguration {
	private int				  time;
	private int				  dimension;
	
	/*
	 * Pour empêcher les joueurs de sortir
	 */
	private CuboidSelection   mapBounds;
	
	/*
	 * Spawn pour les joueurs morts ou arrivant après le début
	 */
	private Location 		  spawnLocation;
	private Location		  iron;
	private Location		  xpbottle;
	
	private BadConfiguration  config;
	
	public TowerMapConfiguration(BadConfiguration config){
		this.config = config;
		
		time			= config.getValue("time", MapNumber.class, new MapNumber(2000)).getHandle().intValue();
		dimension		= config.getValue("dimension", MapNumber.class, new MapNumber(0)).getHandle().intValue();
		//mapBounds 	    = config.getValue("mapBounds", MapSelection.class, new MapSelection()).getHandle();
		spawnLocation   = config.getValue("spawnLocation", MapLocation.class, new MapLocation()).getHandle();
		iron			= config.getValue("iron", MapLocation.class, new MapLocation()).getHandle();
		xpbottle		= config.getValue("xpbottle", MapLocation.class, new MapLocation()).getHandle();

		
		for(BadblockTeam team : GameAPI.getAPI().getTeams()){
			team.teamData(TowerTeamData.class).load(config.getSection(team.getKey()));
		}
	}
	
	public void save(File file){
		config.save(file);
	}
}
