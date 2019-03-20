package fr.badblock.bukkit.games.tower.commands;

import org.bukkit.command.CommandSender;

import fr.badblock.bukkit.games.tower.runnables.StartRunnable;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.command.AbstractCommand;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.GamePermission;
import fr.badblock.gameapi.utils.i18n.TranslatableString;

public class GameCommand extends AbstractCommand {
	public GameCommand() {
		super("game", new TranslatableString("commands.gtower.usage"), GamePermission.PLAYER);
		allowConsole(false);
	}

	@Override
	public boolean executeCommand(CommandSender sender, String[] args) {
		if(args.length == 0) {
			return false;
		}

		BadblockPlayer player = (BadblockPlayer) sender;

		switch(args[0].toLowerCase()){
		case "start":
			if (GameAPI.getAPI().isHoster(player))
			{
				String msge = "commands.gtower.start";

				if(!StartRunnable.started()){
					StartRunnable.startGame(25);
					StartRunnable.time = 25;
				} else msge += "-fail";

				player.sendTranslatedMessage(msge);
			}
			else
			{
				player.sendMessage("§cVous n'avez pas la permission de faire cela.");
			}

			break;
		case "stop":
			if (player.hasPermission(GamePermission.ADMIN))
			{
				String msg = "commands.gtower.stop";

				if(StartRunnable.started()){
					StartRunnable.stopGame();
				} else msg += "-fail";

				player.sendTranslatedMessage(msg);
			}
			else
			{
				player.sendMessage("§cVous n'avez pas la permission de faire cela.");
			}
			break;
			/*case "points":
			try {
				PluginTower.getInstance().getConfiguration().neededPoints = Integer.parseInt(args[1]);
			} catch(Exception e){ return false; }

			player.sendTranslatedMessage("commands.gtower.points");
			break;
		case "playersperteam":
			if(args.length != 2)
				return false;

			int perTeam = 4;

			try {
				perTeam = Integer.parseInt(args[1]);
			} catch(Exception e){
				return false;
			}

			for(BadblockTeam team : GameAPI.getAPI().getTeams())
				team.setMaxPlayers(perTeam);

			plug.getConfiguration().maxPlayersInTeam = perTeam;
			plug.setMaxPlayers(GameAPI.getAPI().getTeams().size() * perTeam);
			try {
				BukkitUtils.setMaxPlayers(GameAPI.getAPI().getTeams().size() * perTeam);
			} catch (Exception e) {
				e.printStackTrace();
			}

			player.sendTranslatedMessage("commands.gtower.modifycount");
			break;*/
		default: return false;
		}

		return true;
	}
}