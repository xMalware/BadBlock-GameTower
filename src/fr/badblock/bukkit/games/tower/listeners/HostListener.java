package fr.badblock.bukkit.games.tower.listeners;

import java.util.Map;

import org.bukkit.event.EventHandler;

import fr.badblock.bukkit.games.tower.PluginTower;
import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.events.HostUpdateEvent;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.utils.BukkitUtils;

public class HostListener extends BadListener
{

	public static boolean randomTeams = false;
	
	@EventHandler
	public void onHostUpdate(HostUpdateEvent event)
	{
		Map<String, Object> parameters = event.getParameters();

		if (parameters.containsKey("points"))
		{
			PluginTower.getInstance().getConfiguration().neededPoints = Integer.parseInt((String) parameters.get("points"));
		}

		if (parameters.containsKey("randomTeams"))
		{
			randomTeams = Boolean.parseBoolean((String) parameters.get("randomTeams"));
		}
		
		if (parameters.containsKey("playersperteam"))
		{
			int perTeam = Integer.parseInt((String) parameters.get("playersperteam"));
			for (BadblockTeam team : GameAPI.getAPI().getTeams())
			{
				team.setMaxPlayers(perTeam);
			}

			PluginTower.getInstance().getConfiguration().maxPlayersInTeam = perTeam;
			PluginTower.getInstance().setMaxPlayers(GameAPI.getAPI().getTeams().size() * perTeam);
			try {
				BukkitUtils.setMaxPlayers(GameAPI.getAPI().getTeams().size() * perTeam);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
