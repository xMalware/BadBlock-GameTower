package fr.badblock.tower;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.badblock.gameapi.BadblockPlugin;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.achievements.AchievementList;
import fr.badblock.gameapi.game.GameServer.WhileRunningConnectionTypes;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.kits.PlayerKit;
import fr.badblock.gameapi.run.BadblockGame;
import fr.badblock.gameapi.run.BadblockGameData;
import fr.badblock.gameapi.run.RunType;
import fr.badblock.gameapi.utils.GameRules;
import fr.badblock.gameapi.utils.general.JsonUtils;
import fr.badblock.tower.commands.GameCommand;
import fr.badblock.tower.commands.TowerCommand;
import fr.badblock.tower.configuration.TowerConfiguration;
import fr.badblock.tower.configuration.TowerMapConfiguration;
import fr.badblock.tower.listeners.DeathListener;
import fr.badblock.tower.listeners.JoinListener;
import fr.badblock.tower.listeners.MoveListener;
import fr.badblock.tower.listeners.PlayerMountListener;
import fr.badblock.tower.listeners.TowerMapProtector;
import fr.badblock.tower.runnables.PreStartRunnable;
import lombok.Getter;
import lombok.Setter;

public class PluginTower extends BadblockPlugin {
	@Getter private static PluginTower instance;
	
	public static 	     File   MAP;
	
	private static final String CONFIG 		   		   = "config.json";
	private static final String TEAMS_CONFIG 		   = "teams.yml";
	private static final String TEAMS_CONFIG_INVENTORY = "teamsInventory.yml";
	private static final String VOTES_CONFIG 		   = "votes.json";
	private static final String KITS_CONFIG_INVENTORY  = "kitInventory.yml";
	private static final String MAPS_CONFIG_FOLDER     = "maps";

	@Getter@Setter
	private int 			      maxPlayers;
	@Getter
	private TowerConfiguration    configuration;
	@Getter@Setter
	private TowerMapConfiguration mapConfiguration;
	
	@Getter
	private Map<String, PlayerKit> kits;

	public void giveDefaultKit(BadblockPlayer player){
		PlayerKit kit = kits.get(configuration.defaultKit);
		
		if(kit == null){
			player.clearInventory();
			return;
		}
		
		player.getPlayerData().unlockNextLevel(kit);
		kit.giveKit(player);
	}
	
	@Override
	public void onEnable(RunType runType){
		AchievementList list = TowerAchievementList.instance;
		
		BadblockGame.TOWER.setGameData(new BadblockGameData() {
			@Override
			public AchievementList getAchievements() {
				return list;
			}
		});
		
		instance = this;
		
		if(runType == RunType.LOBBY)
			return;
		
		try {
			if(!getDataFolder().exists()) getDataFolder().mkdir();

			/**
			 * Chargement de la configuration du jeu
			 */

			// Modification des GameRules
			GameRules.doDaylightCycle.setGameRule(false);
			GameRules.spectatorsGenerateChunks.setGameRule(false);
			GameRules.doFireTick.setGameRule(false);

			// Lecture de la configuration du jeu

			BadblockGame.TOWER.use();

			File configFile    = new File(getDataFolder(), CONFIG);
			this.configuration = JsonUtils.load(configFile, TowerConfiguration.class);
			
			JsonUtils.save(configFile, configuration, true);
			
			File 			  teamsFile 	= new File(getDataFolder(), TEAMS_CONFIG);
			FileConfiguration teams 		= YamlConfiguration.loadConfiguration(teamsFile);

			getAPI().registerTeams(configuration.maxPlayersInTeam, teams);
			getAPI().setDefaultKitContentManager(false);
			
			maxPlayers = getAPI().getTeams().size() * configuration.maxPlayersInTeam;
			kits	   = getAPI().loadKits(GameAPI.getInternalGameName());
			
			try { teams.save(teamsFile); } catch (IOException unused){}

			// Chargement des fonctionnalités de l'API non utilisées par défaut

			getAPI().getBadblockScoreboard().doBelowNameHealth();
			getAPI().getBadblockScoreboard().doTabListHealth();
			getAPI().getBadblockScoreboard().doTeamsPrefix();
			getAPI().getBadblockScoreboard().doOnDamageHologram();

			getAPI().formatChat(true, true);
			
			getAPI().getJoinItems().registerKitItem(0, kits, new File(getDataFolder(), KITS_CONFIG_INVENTORY));
			getAPI().getJoinItems().registerTeamItem(3, new File(getDataFolder(), TEAMS_CONFIG_INVENTORY));
			getAPI().getJoinItems().registerAchievementsItem(4, BadblockGame.TOWER);
			getAPI().getJoinItems().registerVoteItem(5);
			getAPI().getJoinItems().registerLeaveItem(8, configuration.fallbackServer);
			
			getAPI().setMapProtector(new TowerMapProtector());
			getAPI().enableAntiSpawnKill();
			
			getAPI().getGameServer().whileRunningConnection(WhileRunningConnectionTypes.BACKUP);
			
			new MoveListener();
			new DeathListener();
			new JoinListener();
			new PlayerMountListener();		// Gère les moutons en début de partie :3
			
			File votesFile = new File(getDataFolder(), VOTES_CONFIG);

			if(!votesFile.exists())
				votesFile.createNewFile();

			getAPI().getBadblockScoreboard().beginVote(JsonUtils.loadArray(votesFile));
			
			new PreStartRunnable().runTaskTimer(GameAPI.getAPI(), 0, 30L);
			
			MAP = new File(getDataFolder(), MAPS_CONFIG_FOLDER);
			
			new TowerCommand(MAP);
			new GameCommand();
			
			Bukkit.getWorlds().forEach(world -> {
				world.setTime(2000L);
				world.getEntities().forEach(entity -> entity.remove());
			});
		} catch(Throwable e){
			e.printStackTrace();
		}
	}
	
	public void saveJsonConfig(){
		File configFile = new File(getDataFolder(), CONFIG);
		JsonUtils.save(configFile, configuration, true);
	}
}
