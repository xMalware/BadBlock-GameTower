package fr.badblock.bukkit.games.tower.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.bukkit.games.tower.configuration.TowerMapConfiguration;
import fr.badblock.bukkit.games.tower.configuration.TowerMapConfiguration.MapCustomEnchantment;
import fr.badblock.bukkit.games.tower.configuration.TowerMapConfiguration.MapCustomRecipeResult;
import fr.badblock.bukkit.games.tower.entities.TowerTeamData;
import fr.badblock.bukkit.games.tower.players.TowerScoreboard;
import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.game.GameState;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.servers.MapProtector;
import fr.badblock.gameapi.utils.BukkitUtils;

public class TowerMapProtector implements MapProtector {

	public static BukkitTask lottery;

	private boolean inGame(){
		return GameAPI.getAPI().getGameServer().getGameState() == GameState.RUNNING;
	}

	@Override
	public boolean blockPlace(BadblockPlayer player, Block block) {
		if(block != null && inGame()){

			if(block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)
				return player.hasAdminMode();

			for(BadblockTeam team : GameAPI.getAPI().getTeams()){
				if(team.teamData(TowerTeamData.class).getSpawnzone().isInSelection(block)){
					return player.hasAdminMode();
				}
			}

		}

		if (TowerScoreboard.run)
		{
			if (block.getLocation().getY() >= PluginTower.getInstance().getMapConfiguration().getMaxY())
			{
				player.sendTranslatedMessage("tower.heightlimit");
				return player.hasAdminMode();
			}
		}

		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean blockBreak(BadblockPlayer player, Block block) {
		if (player.hasAdminMode()) return true;

		if (block.getType().equals(Material.BEACON))
		{
			return false;
		}

		if (block != null && block.getType() != null && (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST) || block.getType().equals(Material.ENDER_CHEST))) return false;
		if(inGame() && block != null){

			for(BadblockTeam team : GameAPI.getAPI().getTeams()){
				if (team.teamData(TowerTeamData.class).getSpawnzone().isInSelection(block))
				{
					/*if (!JoinListener.placedTotalBlocks.containsKey(block.getLocation()))
					{*/
						return player.hasAdminMode();
					//}
				}
				else if(block.getType() == Material.CHEST){
					return player.hasAdminMode();
				}
			}

		}

		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean modifyItemFrame(BadblockPlayer player, Entity itemFrame) {
		return player.hasAdminMode();
	}

	@Override
	public boolean canLostFood(BadblockPlayer player) {
		return inGame() && !TowerScoreboard.run;
	}

	@Override
	public boolean canUseBed(BadblockPlayer player, Block bed) {
		return false;
	}

	@Override
	public boolean canUsePortal(BadblockPlayer player) {
		return false;
	}

	@Override
	public boolean canDrop(BadblockPlayer player) {
		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canPickup(BadblockPlayer player) {
		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canFillBucket(BadblockPlayer player) {
		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canEmptyBucket(BadblockPlayer player) {
		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canInteract(BadblockPlayer player, Action action, Block block) {

		if(block != null && block.getType() == Material.CHEST && inGame() && action == Action.RIGHT_CLICK_BLOCK){

			for(BadblockTeam team : GameAPI.getAPI().getTeams()){
				if(team.teamData(TowerTeamData.class).getTeamzone().isInSelection(block)){
					return player.hasAdminMode() || team.equals(player.getTeam());
				}
			}

		}
		else if (block != null && block.getType().equals(Material.BEACON) && action.name().contains("CLICK_BLOCK"))
		{
			if (PluginTower.getInstance().getMapConfiguration() != null && PluginTower.getInstance().getMapConfiguration().getLottery().booleanValue())
			{
				if (StartRunnable.gameTask.beacon > System.currentTimeMillis())
				{
					long diff = System.currentTimeMillis() - StartRunnable.gameTask.beacon;
					int d = (int) (diff / 1000);
					player.sendTranslatedMessage("tower.lottery_pleasewait", TowerScoreboard.time(d));
					return false;
				}

				if (lottery != null)
				{
					player.sendTranslatedMessage("tower.lottery_alreadyinuse");
					return false;
				}

				lottery = Bukkit.getScheduler().runTaskTimer(PluginTower.getInstance(), new Runnable()
				{

					private int ticks = 0;

					@Override
					public void run()
					{
						if (!player.isOnline())
						{
							lottery.cancel();
							lottery = null;
							return;
						}

						if (!player.getWorld().equals(block.getWorld()) || player.getLocation().distance(block.getLocation()) < 5)
						{
							player.sendTranslatedMessage("tower.lottery_toofar");
							lottery.cancel();
							lottery = null;
						}

						if (ticks >= 100)
						{
							player.playSound(Sound.EAT);
							player.sendTranslatedMessage("tower.lottery_given");
							BukkitUtils.getAllPlayers().forEach(p -> p.sendTranslatedMessage("tower.lottery_broadcast", player.getTabGroupPrefix().getAsLine(p) + player.getName()));
							StartRunnable.gameTask.beacon = System.currentTimeMillis() + 300_000L;

							TowerMapConfiguration c = PluginTower.getInstance().getMapConfiguration();

							HashSet<MapCustomRecipeResult> l = new HashSet<>();

							for (int i = 0; i < c.getBeaconItemCount(); i++)
							{
								int rt = c.getBeacons().stream().filter(t -> !l.contains(t)).mapToInt(u -> u.getProbability()).sum();
								int rand = new Random().nextInt(rt);
								int index = 0;
								MapCustomRecipeResult result = null;

								for (MapCustomRecipeResult r : c.getBeacons())
								{
									if (l.contains(r))
									{
										continue;
									}

									index += r.getProbability();

									if (rand <= index)
									{
										result = r;
									}
								}

								if (result != null)
								{
									Material material = getFrom(result.getName());
									ItemStack item = new ItemStack(material, result.getAmount(), (byte) result.getData());

									for (MapCustomEnchantment e : result.getEnchantments())
									{
										item.addEnchantment(e.toEnchantment(), e.getLevel());
									}

									player.getInventory().addItem(item);
								}

							}

							lottery.cancel();
							lottery = null;
							return;
						}

						ticks++;
					}
				}, 1, 1);

				return false;
			}
		}

		return inGame() || player.hasAdminMode();
	}

	private static Material getFrom(String raw)
	{
		for (Material material : Material.values())
		{
			if (material.name().equalsIgnoreCase(raw))
			{
				return material;
			}
		}

		return null;
	}

	@Override
	public boolean canInteractArmorStand(BadblockPlayer player, ArmorStand entity) {
		return false;
	}

	@Override
	public boolean canInteractEntity(BadblockPlayer player, Entity entity) {
		return true; // � priori rien � bloquer ... :o
	}

	@Override
	public boolean canEnchant(BadblockPlayer player, Block table) {
		return true;
	}

	@Override
	public boolean canBeingDamaged(BadblockPlayer player) {
		return inGame();
	}

	@Override
	public boolean healOnJoin(BadblockPlayer player) {
		return !inGame();
	}

	@Override
	public boolean canBlockDamage(Block block) {
		return true;
	}

	@Override
	public boolean allowFire(Block block) {
		return false;
	}

	@Override
	public boolean allowMelting(Block block) {
		return false;
	}

	@Override
	public boolean allowBlockFormChange(Block block) {
		return true; //TODO test
	}

	@Override
	public boolean allowPistonMove(Block block) {
		Map<Integer, String> map = new HashMap<>();
		map.entrySet().stream().sorted((e1, e2) -> { return Integer.compare(e2.getKey(), e1.getKey()); }).collect(Collectors.toList());


		return false;
	}

	@Override
	public boolean allowBlockPhysics(Block block) {
		return true;
	}

	@Override
	public boolean allowLeavesDecay(Block block) {
		return false;
	}

	@Override
	public boolean allowRaining() {
		return true;
	}

	@Override
	public boolean modifyItemFrame(Entity itemframe) {
		return false;
	}

	@Override
	public boolean canSoilChange(Block soil) {
		return false;
	}

	@Override
	public boolean canSpawn(Entity entity) {
		return true;
	}

	@Override
	public boolean canCreatureSpawn(Entity creature, boolean isPlugin) {
		return isPlugin;
	}

	@Override
	public boolean canItemSpawn(Item item) {
		return true;
	}

	@Override
	public boolean canItemDespawn(Item item) {
		return true;
	}

	@Override
	public boolean allowExplosion(Location location) {
		return inGame();
	}

	@Override
	public boolean allowInteract(Entity entity) {
		return false;
	}

	@Override
	public boolean canCombust(Entity entity) {
		return true;
	}

	@Override
	public boolean canEntityBeingDamaged(Entity entity) {
		return !inGame();
	}

	@Override
	public boolean destroyArrow() {
		return true;
	}

	@Override
	public boolean canEntityBeingDamaged(Entity entity, BadblockPlayer badblockPlayer) {
		return false;
	}

}
