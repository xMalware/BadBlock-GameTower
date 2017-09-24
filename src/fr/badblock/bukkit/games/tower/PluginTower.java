package fr.badblock.bukkit.games.tower;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.badblock.bukkit.games.tower.commands.GameCommand;
import fr.badblock.bukkit.games.tower.commands.TowerCommand;
import fr.badblock.bukkit.games.tower.configuration.TowerConfiguration;
import fr.badblock.bukkit.games.tower.configuration.TowerMapConfiguration;
import fr.badblock.bukkit.games.tower.listeners.DeathListener;
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
import fr.badblock.gameapi.game.GameServer.WhileRunningConnectionTypes;
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
					World world = Bukkit.getWorlds().get(0);
					world.setWeatherDuration(0);
					world.setThundering(false);
				}
			}, 0, 1000);

			File configFile    = new File(getDataFolder(), CONFIG);
			this.configuration = JsonUtils.load(configFile, TowerConfiguration.class);

			JsonUtils.save(configFile, configuration, true);

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
			//ItemStackFactory item = GameAPI.getAPI().createItemStackFactory().displayName(new TranslatableString("vote.bowinventory")).type(Material.BOW);
			/*getAPI().getJoinItems().registerCustomItem(1, item, new ItemEvent() {

				@Override
				public boolean call(ItemAction action, BadblockPlayer player) {
					CustomInventory inventory = GameAPI.getAPI().createCustomInventory(kits.size() / 9, GameAPI.i18n().get(player, "vote.bowinventory")[0]);
					int yes = 0;
					int no = 0;
					for (boolean bool : bow.values())
						if (bool) yes++;
						else no++;
					inventory.addClickableItem(0, GameAPI.getAPI().createItemStackFactory()
					.type(Material.BOW)
					.displayName(GameAPI.getAPI().getI18n().get(player.getPlayerData().getLocale(), "vote.bowyes")[0])
					.lore(new TranslatableString("vote.bowvotes", yes))
					.asExtra(1).listenAs(new ItemEvent(){
						@Override
						public boolean call(ItemAction action, BadblockPlayer player) {
							if (bow.containsKey(player.getName())) {
								boolean bool = bow.get(player.getName());
								if (bool) {
									player.sendTranslatedMessage("vote.bowalready");
									return true;
								}
							}
							bow.put(player.getName(), true);
							player.sendTranslatedMessage("vote.bowvoted", player.getTranslatedMessage("vote.bowvotedyes")[0]);
							return true;
						}
					}, ItemPlaces.INVENTORY_CLICKABLE));
					inventory.addClickableItem(1, GameAPI.getAPI().createItemStackFactory()
					.type(Material.BOW)
					.displayName(GameAPI.getAPI().getI18n().get(player.getPlayerData().getLocale(), "vote.bowno")[0])
					.lore(new TranslatableString("vote.bowvotes", no))
					.asExtra(1).listenAs(new ItemEvent(){
						@Override
						public boolean call(ItemAction action, BadblockPlayer player) {
							if (bow.containsKey(player.getName())) {
								boolean bool = bow.get(player.getName());
								if (!bool) {
									player.sendTranslatedMessage("vote.bowalready");
									return true;
								}
							}
							bow.put(player.getName(), false);
							player.sendTranslatedMessage("vote.bowvoted", player.getTranslatedMessage("vote.bowvotedno")[0]);
							return true;
						}
					}, ItemPlaces.INVENTORY_CLICKABLE));
					inventory.openInventory(player);
					return true;
				}

			});*/
			getAPI().getJoinItems().registerKitItem(0, kits, new File(getDataFolder(), KITS_CONFIG_INVENTORY));
			getAPI().getJoinItems().registerTeamItem(3, new File(getDataFolder(), TEAMS_CONFIG_INVENTORY));
			getAPI().getJoinItems().registerAchievementsItem(4, BadblockGame.TOWER);
			getAPI().getJoinItems().registerVoteItem(5);
			getAPI().getJoinItems().registerLeaveItem(8, configuration.fallbackServer);

			getAPI().setMapProtector(new TowerMapProtector());
			getAPI().enableAntiSpawnKill();
			//getAPI().enableAntiBowSpam(500);

			getAPI().getGameServer().whileRunningConnection(WhileRunningConnectionTypes.SPECTATOR);

			new MoveListener();
			new DeathListener();
			new JoinListener();
			new QuitListener();
			new PartyJoinListener();
			new PlayerMountListener();		// G�re les moutons en d�but de partie :3

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

	public int getMinPlayers() {
		if (!configuration.enabledAutoTeamManager) return configuration.minPlayers;
		return configuration.minPlayersAutoTeam * getAPI().getTeams().size();
	}

}
