package fr.badblock.bukkit.games.tower.configuration;

import java.io.File;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;

import fr.badblock.bukkit.games.tower.entities.TowerTeamData;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.configuration.BadConfiguration;
import fr.badblock.gameapi.configuration.values.MapBoolean;
import fr.badblock.gameapi.configuration.values.MapLocation;
import fr.badblock.gameapi.configuration.values.MapNumber;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.utils.selections.CuboidSelection;
import lombok.Data;

@Data
public class TowerMapConfiguration {
	private int				  time;
	private int				  dimension;
	private int				  maxY;
	
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
	private Location		  beacon;
	
	/**
	 * La map est autorisé en arcs
	 */
	private Boolean			  allowBows;
	private Boolean			  lottery;
	
	private BadConfiguration  config;
	public List<MapLocation>   spawn			= null;
	
	private int												beaconItemCount = 0;
	public List<MapCustomRecipeResult>   beacons			= null;
	
	public TowerMapConfiguration(BadConfiguration config){
		this.config = config;
		
		time			= config.getValue("time", MapNumber.class, new MapNumber(2000)).getHandle().intValue();
		dimension		= config.getValue("dimension", MapNumber.class, new MapNumber(0)).getHandle().intValue();
		maxY			= config.getValue("maxY", MapNumber.class, new MapNumber(0)).getHandle().intValue();
		//mapBounds 	    = config.getValue("mapBounds", MapSelection.class, new MapSelection()).getHandle();
		spawnLocation   = config.getValue("spawnLocation", MapLocation.class, new MapLocation()).getHandle();
		iron			= config.getValue("iron", MapLocation.class, new MapLocation()).getHandle();
		xpbottle		= config.getValue("xpbottle", MapLocation.class, new MapLocation()).getHandle();
		allowBows		= config.getValue("allowBows", MapBoolean.class, new MapBoolean(true)).getHandle();
		lottery		= config.getValue("lottery", MapBoolean.class, new MapBoolean(false)).getHandle();
		beacon		= config.getValue("beacon", MapLocation.class, new MapLocation()).getHandle();

		beaconItemCount = config.getValue("beaconItemCount", MapNumber.class, new MapNumber(5)).getHandle().intValue();
		beacons = config.getSimpleValueList("beaconItems", MapCustomRecipeResult.class);
		
		for(BadblockTeam team : GameAPI.getAPI().getTeams()){
			team.teamData(TowerTeamData.class).load(config.getSection(team.getKey()));
		}
	}
	
	public void save(File file){
		config.save(file);
	}
	
	@Data
	public class MapCustomEnchantment {
		
		public String	enchantment;
		public int			level;
		
		public Enchantment toEnchantment()
		{
			return Enchantment.getByName(enchantment);
		}
		
	}
	
	@Data
	public class MapCustomRecipeResult {
		
		public String name;
		public int amount;
		public int data;
		public int	 probability;
		public MapCustomEnchantment[] enchantments;
		
	}
	
}
