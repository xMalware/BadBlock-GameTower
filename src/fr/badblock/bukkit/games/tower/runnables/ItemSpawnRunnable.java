package fr.badblock.bukkit.games.tower.runnables;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.badblock.gameapi.GameAPI;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor public class ItemSpawnRunnable extends BukkitRunnable {
	private final Material   material;
	private final long	     ticks;
	private final Location	 location;

	private Item item  = null;

	@Override
	public void run(){
		if(item != null && !item.isDead()){
			item.remove();
		}

		item = location.getWorld().dropItem(location, new ItemStack(material, 1));
		item.setVelocity(new Vector(0, 0, 0));

	}

	public void start(){
		runTaskTimer(GameAPI.getAPI(), ticks, ticks);
	}
}
