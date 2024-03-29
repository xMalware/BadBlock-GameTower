package fr.badblock.bukkit.games.tower;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.badblock.bukkit.games.tower.commands.GameCommand;
import fr.badblock.bukkit.games.tower.commands.TowerCommand;
import fr.badblock.bukkit.games.tower.configuration.TowerConfiguration;
import fr.badblock.bukkit.games.tower.configuration.TowerMapConfiguration;
import fr.badblock.bukkit.games.tower.listeners.DeathListener;
import fr.badblock.bukkit.games.tower.listeners.HostListener;
import fr.badblock.bukkit.games.tower.listeners.JoinListener;
import fr.badblock.bukkit.games.tower.listeners.MoveListener;
import fr.badblock.bukkit.games.tower.listeners.PartyJoinListener;
import fr.badblock.bukkit.games.tower.listeners.PlayerMountListener;
import fr.badblock.bukkit.games.tower.listeners.QuitListener;
import fr.badblock.bukkit.games.tower.listeners.TowerMapProtector;
import fr.badblock.bukkit.games.tower.players.TowerScoreboard;
import fr.badblock.bukkit.games.tower.runnables.PreStartRunnable;
import fr.badblock.gameapi.BadblockPlugin;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.achievements.AchievementList;
import fr.badblock.gameapi.configuration.values.MapLocation;
import fr.badblock.gameapi.game.GameServer.WhileRunningConnectionTypes;
import fr.badblock.gameapi.game.rankeds.RankedManager;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.kits.PlayerKit;
import fr.badblock.gameapi.run.BadblockGame;
import fr.badblock.gameapi.run.BadblockGameData;
import fr.badblock.gameapi.run.RunType;
import fr.badblock.gameapi.utils.BukkitUtils;
import fr.badblock.gameapi.utils.GameRules;
import fr.badblock.gameapi.utils.general.JsonUtils;
import fr.badblock.gameapi.utils.threading.TaskManager;
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
	public HashMap<String, Boolean> bow = new HashMap<>();

	@Getter
	private Map<String, PlayerKit> kits;

	public MapLocation spawn;

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
			TaskManager.scheduleSyncRepeatingTask("weather_manager", new Runnable()
			{
				@Override
				public void run()
				{
					Bukkit.getWorlds().forEach(world -> {
						world.setTime(2000L);
						world.setStorm(false);
						world.setThundering(false);
						world.setThunderDuration(0);
						world.setWeatherDuration(0);
						System.out.println("Set weather sun!");
					});
				}
			}, 20 * 10, 20 * 30);

			File configFile    = new File(getDataFolder(), CONFIG);
			this.configuration = JsonUtils.load(configFile, TowerConfiguration.class);

			JsonUtils.save(configFile, configuration, true);

			spawn = configuration.spawn.get(new Random().nextInt(configuration.spawn.size()));
			
			HashSet<Chunk> chunks = new HashSet<>();
			int radius = 64;
			for (int x = -radius; x < radius; x++)
				for (int z = -radius; z < radius; z++)
				{
					Location location = spawn.getHandle().clone().add(x, 0, z);
					chunks.add(location.getChunk());
				}

			File 			  teamsFile 	= new File(getDataFolder(), TEAMS_CONFIG);
			FileConfiguration teams 		= YamlConfiguration.loadConfiguration(teamsFile);

			getAPI().registerTeams(configuration.maxPlayersInTeam, teams);
			try {
				BukkitUtils.setMaxPlayers(GameAPI.getAPI().getTeams().size() * configuration.maxPlayersInTeam);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
			getAPI().setDefaultKitContentManager(false);

			maxPlayers = getAPI().getTeams().size() * configuration.maxPlayersInTeam;
			kits	   = getAPI().loadKits(GameAPI.getInternalGameName());
			if (TowerScoreboard.run)
			{
				// dégueu mais bon
				for (String string : kits.keySet())
				{
					if (!string.equalsIgnoreCase("tower_vip") && !string.equalsIgnoreCase("defaultkit"))
					{
						kits.remove(string);
					}
				}
			}

			try { teams.save(teamsFile); } catch (IOException unused){}

			// Chargement des fonctionnalités de l'API non utilisées par défaut

			getAPI().getBadblockScoreboard().doBelowNameHealth();
			getAPI().getBadblockScoreboard().doTabListHealth();
			getAPI().getBadblockScoreboard().doTeamsPrefix();
			getAPI().getBadblockScoreboard().doOnDamageHologram();

			getAPI().formatChat(true, true);
			
			String text = "";

			text = text.replace("·ice", "");
			text = text.replace("·e", "");
			
			getAPI().getJoinItems().registerKitItem(0, kits, new File(getDataFolder(), KITS_CONFIG_INVENTORY));
			getAPI().getJoinItems().registerTeamItem(3, new File(getDataFolder(), TEAMS_CONFIG_INVENTORY));
			getAPI().getJoinItems().registerAchievementsItem(4, BadblockGame.TOWER);
			getAPI().getJoinItems().registerVoteItem(5);
			getAPI().getJoinItems().registerLeaveItem(8, configuration.fallbackServer);

			getAPI().setMapProtector(new TowerMapProtector());
			if (!TowerScoreboard.run)
			{
				getAPI().enableAntiSpawnKill();
			}
			//getAPI().enableAntiBowSpam(500);

			getAPI().getGameServer().whileRunningConnection(WhileRunningConnectionTypes.BACKUP);

			new MoveListener();
			new DeathListener();
			new JoinListener();
			new QuitListener();
			new PartyJoinListener();
			new PlayerMountListener();		// G�re les moutons en d�but de partie :3
			new HostListener();

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
				world.setStorm(false);
				world.setThundering(false);
				world.setThunderDuration(0);
				world.setWeatherDuration(0);
				System.out.println("Set weather sun!");
				world.getEntities().forEach(entity -> entity.remove());
			});

			// Ranked
			RankedManager.instance.initialize(RankedManager.instance.getCurrentRankedGameName(), 
					TowerScoreboard.KILLS, TowerScoreboard.DEATHS, TowerScoreboard.MARKS, TowerScoreboard.WINS, TowerScoreboard.LOOSES);

		} catch(Throwable e){
			e.printStackTrace();
		}
	}

	public void saveJsonConfig(){
		File configFile = new File(getDataFolder(), CONFIG);
		JsonUtils.save(configFile, configuration, true);
	}

	public int getMinPlayers() {
		if (!configuration.enabledAutoTeamManager) return configuration.minPlayers;
		return configuration.minPlayersAutoTeam * getAPI().getTeams().size();
	}

}
