package fr.badblock.bukkit.games.tower.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.TowerAchievementList;
import fr.badblock.bukkit.games.tower.entities.TowerTeamData;
import fr.badblock.bukkit.games.tower.players.TowerScoreboard;
import fr.badblock.bukkit.games.tower.runnables.PreStartRunnable;
import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.events.PlayerGameInitEvent;
import fr.badblock.gameapi.events.api.PlayerLoadedEvent;
import fr.badblock.gameapi.events.api.SpectatorJoinEvent;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.BadblockMode;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.players.data.InGameKitData;
import fr.badblock.gameapi.players.data.PlayerAchievementState;
import fr.badblock.gameapi.players.kits.PlayerKit;
import fr.badblock.gameapi.utils.BukkitUtils;
import fr.badblock.gameapi.utils.i18n.TranslatableString;

public class JoinListener extends BadListener {

	public long time = -1;

	/*public static Map<Location, Block> placedTotalBlocks = new HashMap<>();
	public Map<Location, Integer> uniqueLocationId = new HashMap<>();
	public Map<Long, Block> placedBlocks = new HashMap<>();
	public Map<Block, Material> placedBlockTypes = new HashMap<>();
	public Map<Block, Material> brokenBlockTypes = new HashMap<>();
	public Map<Long, Block> brokenBlocks = new HashMap<>();*/

	public JoinListener()
	{
		if (!TowerScoreboard.run)
		{
			return;
		}
/*
		Bukkit.getScheduler().runTaskTimer(PluginTower.getInstance(), new Runnable()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void run()
			{
				Iterator<Entry<Long, Block>> iterator = placedBlocks.entrySet().iterator();
				while (iterator.hasNext())
				{
					Entry<Long, Block> entry = iterator.next();

					if (!placedBlockTypes.containsKey(entry.getValue()))
					{
						sendBreakPacket(entry.getValue().getWorld(), entry.getValue().getLocation(), (int) 0, entry.getValue());
						iterator.remove();
						continue;
					}

					Block newBlock = entry.getValue().getWorld().getBlockAt(entry.getValue().getLocation());
					if (!newBlock.getType().equals(placedBlockTypes.get(entry.getValue())))
					{
						sendBreakPacket(entry.getValue().getWorld(), entry.getValue().getLocation(), (int) 0, entry.getValue());
						iterator.remove();
						placedBlockTypes.remove(entry.getValue());
						continue;
					}

					if (entry.getKey() < System.currentTimeMillis())
					{
						entry.getValue().setType(Material.AIR);
						entry.getValue().setType(Material.AIR);
						sendBreakPacket(entry.getValue().getWorld(), entry.getValue().getLocation(), (int) -1, entry.getValue());
						iterator.remove();
						placedBlockTypes.remove(entry.getValue());
						continue;
					}

					double id = (10 - ((int) (entry.getKey() - System.currentTimeMillis()) / 1000)) * 0.8;
					sendBreakPacket(entry.getValue().getWorld(), entry.getValue().getLocation(), (int) id, entry.getValue());
				}

				iterator = brokenBlocks.entrySet().iterator();
				while (iterator.hasNext())
				{
					Entry<Long, Block> entry = iterator.next();

					if (!brokenBlockTypes.containsKey(entry.getValue()))
					{
						iterator.remove();
						continue;
					}

					if (entry.getKey() < System.currentTimeMillis())
					{
						sendBreakPacket(entry.getValue().getWorld(), entry.getValue().getLocation(), (int) -1, entry.getValue());
						entry.getValue().getWorld().getBlockAt(entry.getValue().getLocation()).setType(brokenBlockTypes.get(entry.getValue()));
						iterator.remove();
						brokenBlockTypes.remove(entry.getValue());
					}
					else
					{
						for (Player player : BukkitUtils.getAllPlayers())
						{
							player.playEffect(entry.getValue().getLocation(), Effect.SMOKE, 0);
						}
					}
				}
			}
		}, 20, 20);*/
	} 
	
/*	public void sendBreakPacket(World world, Location location, int data, Block block)
	{
		int dimension = ((CraftWorld)block.getWorld()).getHandle().dimension;
		BlockPosition bp = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		int id = 0;
		if (uniqueLocationId.containsKey(location))
		{
			id = uniqueLocationId.get(location);
		}
		else
		{
			id = new Random().nextInt(Integer.MAX_VALUE);
			uniqueLocationId.put(location, id);
		}
		
		PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(id, bp, data);
		((CraftServer)Bukkit.getServer()).getHandle().sendPacketNearby(block.getX(), block.getY(), block.getZ(), 24 * 6, dimension, packet);
	}*/

	@EventHandler
	public void onSpectatorJoin(SpectatorJoinEvent e){
		e.getPlayer().teleport(PluginTower.getInstance().getMapConfiguration().getSpawnLocation());

		new TowerScoreboard(e.getPlayer());
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlace(BlockPlaceEvent e)
	{
		if (!TowerScoreboard.run)
		{
			return;
		}
/*
		sendBreakPacket(e.getBlock().getWorld(), e.getBlock().getLocation(), (int) 0, e.getBlock());
		placedBlockTypes.put(e.getBlock(), e.getBlock().getType());
		
		long randGenerator = new Random().nextInt(600) + 10000;
		long time = System.currentTimeMillis() + randGenerator;
		while (brokenBlocks.containsKey(time))
		{
			randGenerator = new Random().nextInt(600) + 10000;
			time = System.currentTimeMillis() + randGenerator;
		}
		
		placedBlocks.put(time, e.getBlock());
		placedTotalBlocks.put(e.getBlock().getLocation(), e.getBlock());*/
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBreak(BlockBreakEvent e)
	{
		if (!TowerScoreboard.run)
		{
			return;
		}

	/*	if (placedBlocks.containsValue(e.getBlock()))
		{
			sendBreakPacket(e.getBlock().getWorld(), e.getBlock().getLocation(), (int) 0, e.getBlock());
			placedBlocks.values().remove(e.getBlock());
			return;
		}

		long randGenerator = new Random().nextInt(600) + 10000;
		long time = System.currentTimeMillis() + randGenerator;
		while (brokenBlocks.containsKey(time))
		{
			randGenerator = new Random().nextInt(600) + 10000;
			time = System.currentTimeMillis() + randGenerator;
		}
		
		brokenBlocks.put(time, e.getBlock());
		brokenBlockTypes.put(e.getBlock(), e.getBlock().getType());*/
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage(null);
	}
	
	@EventHandler
	public void onLoad(PlayerLoadedEvent e)
	{
		BadblockPlayer player = e.getPlayer();

		if (player.getBadblockMode().equals(BadblockMode.SPECTATOR))
		{
			return;
		}
		
		if (!inGame()) {
			player.setGameMode(GameMode.SURVIVAL);
			player.sendTranslatedTitle("tower.join.title");
			player.teleport(PluginTower.getInstance().spawn.getHandle());
			player.sendTimings(0, 80, 20);
			player.sendTranslatedTabHeader(new TranslatableString("tower.tab.header"), new TranslatableString("tower.tab.footer"));

			String display = player.getTabGroupPrefix().getAsLine(player) + player.getName();
			BukkitUtils.getAllPlayers().forEach(plo ->
			{
				plo.sendTranslatedMessage("tower.joined", display, Bukkit.getOnlinePlayers().size(), PluginTower.getInstance().getMaxPlayers());
				plo.playSound(Sound.CLICK);
			});
		}
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent e){

		if(inGame()){
			return;
		}

		PreStartRunnable.doJob();
		StartRunnable.joinNotify(Bukkit.getOnlinePlayers().size(), PluginTower.getInstance().getMaxPlayers());
		PluginTower tower = PluginTower.getInstance();
		if (Bukkit.getOnlinePlayers().size() + 1 >= tower.getMaxPlayers()) {
			if (tower.getConfiguration().enabledAutoTeamManager) {
				int max = tower.getConfiguration().maxPlayersAutoTeam * tower.getAPI().getTeams().size();
				if (tower.getMaxPlayers() < max) {
					tower.getAPI().getTeams().forEach(team -> team.setMaxPlayers(team.getMaxPlayers() + 1));
					tower.setMaxPlayers(tower.getMaxPlayers() + tower.getAPI().getTeams().size());
					try {
						BukkitUtils.setMaxPlayers(tower.getMaxPlayers());
					} catch (Exception err) {
						err.printStackTrace();
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerGameInit(PlayerGameInitEvent event) {
		handle(event.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		e.setQuitMessage(null);
	}

	@EventHandler
	public void craftItem(PrepareItemCraftEvent e) {
		if (!PluginTower.getInstance().getMapConfiguration().getAllowBows()) {
			Material itemType = e.getRecipe().getResult().getType();
			if (itemType == Material.BOW || itemType == Material.ARROW) {
				e.getInventory().setResult(new ItemStack(Material.AIR));
			}
		}
	}

	public static void handle(BadblockPlayer player) {
		BadblockTeam team = player.getTeam();
		if (team == null) return;
		Location location = team.teamData(TowerTeamData.class).getRespawnLocation();
		player.leaveVehicle();
		player.eject();
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 4, 5));
		player.changePlayerDimension(BukkitUtils.getEnvironment( PluginTower.getInstance().getMapConfiguration().getDimension() ));
		player.teleport(location);
		player.setGameMode(GameMode.SURVIVAL);
		player.getCustomObjective().generate();
		if (GameAPI.getServerName().startsWith("towerf_"))
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
		}

		boolean good = true;

		for(PlayerKit toUnlock : PluginTower.getInstance().getKits().values()){
			if(!toUnlock.isVIP()){
				if(player.getPlayerData().getUnlockedKitLevel(toUnlock) < 2){
					good = false; break;
				}
			}
		}

		if(good){
			PlayerAchievementState state = player.getPlayerData().getAchievementState(TowerAchievementList.TOWER_ALLKITS);

			if(!state.isSucceeds()){
				state.succeed();
				TowerAchievementList.TOWER_ALLKITS.reward(player);
			}
		}

		PlayerKit kit = player.inGameData(InGameKitData.class).getChoosedKit();

		if(kit != null){
			if (PluginTower.getInstance().getMapConfiguration().getAllowBows())
				kit.giveKit(player);
			else
				kit.giveKit(player, Material.BOW, Material.ARROW);
		} else {
			PluginTower.getInstance().giveDefaultKit(player);
		}
	}

}
