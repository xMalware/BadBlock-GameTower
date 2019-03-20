package fr.badblock.bukkit.games.tower.runnables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Lists;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.utils.BukkitUtils;

public class KickRunnable extends BukkitRunnable
{
	
	private int time = 15;
	private int rebootTime = -15;

	private Map<Integer, List<BadblockPlayer>> send = new HashMap<>();

	@Override
	public void run(){
		if (time == rebootTime)
		{
			Bukkit.shutdown();
		}
		else if(time == 8)
		{
			if (BukkitUtils.getAllPlayers().size() <= 8)
			{
				BukkitUtils.getAllPlayers().forEach(player -> player.sendPlayer(PluginTower.getInstance().getConfiguration().fallbackServer));
				rebootTime = 0;
			}
			else
			{
				int partNumbers = BukkitUtils.getAllPlayers().size() / 3;
				List<List<BadblockPlayer>> parts = Lists.partition(BukkitUtils.getAllPlayers(), partNumbers);

				int elapsed = 0;
				int s = 7;
				for (List<BadblockPlayer> part : parts)
				{
					s--;
					elapsed++;
					if (elapsed > 2)
					{
						final int finalElapsed = elapsed;
						part.forEach(player -> player.sendTranslatedMessage("game.rebootpart", finalElapsed));
					}
					send.put(s, part);
				}
				
				rebootTime = s - 5;
			}
		}
		else if (time <= 8)
		{
			if (send.isEmpty())
			{
				BukkitUtils.getAllPlayers().forEach(player -> player.sendPlayer(PluginTower.getInstance().getConfiguration().fallbackServer));
			}
			else if (send.containsKey(time))
			{
				send.get(time).forEach(player -> player.sendPlayer(PluginTower.getInstance().getConfiguration().fallbackServer));
			}
			else if (time > rebootTime && time <= rebootTime + 5)
			{
				BukkitUtils.getAllPlayers().forEach(player -> player.sendPlayer(PluginTower.getInstance().getConfiguration().fallbackServer));
			}
		}

		time--;
	}
	
}