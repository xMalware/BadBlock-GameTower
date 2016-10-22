package fr.badblock.tower.commands;

import java.io.File;

import org.bukkit.command.CommandSender;

import fr.badblock.gameapi.command.MapAbstractCommand;
import fr.badblock.gameapi.configuration.values.MapLocation;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.GamePermission;
import fr.badblock.gameapi.utils.i18n.TranslatableString;
import fr.badblock.tower.PluginTower;

public class TowerCommand extends MapAbstractCommand {
	public TowerCommand(File folder) {
		super("tower", new TranslatableString("commands.tower.usage"), GamePermission.ADMIN, new String[]{}, folder);
		allowConsole(false);
	}

	@Override
	public boolean executeCommand(CommandSender sender, String[] args) {
		if(args.length == 0) return false;
		
		BadblockPlayer player = (BadblockPlayer) sender;
		PluginTower plug = PluginTower.getInstance();

		
		switch(args[0].toLowerCase()){
			case "mainspawn":
				plug.getConfiguration().spawn = new MapLocation(player.getLocation());
				plug.saveJsonConfig();
			break;
			case "spawnlocation":
				if(args.length < 2)
					return false;
				
				setLocation(false, args[1], null, "spawnLocation", player);
			break;
			case "iron":
				if(args.length < 2)
					return false;
				
				setLocation(false, args[1], null, "iron", player);
			break;
			case "xpbottle":
				if(args.length < 2)
					return false;
				
				setLocation(false, args[1], null, "xpbottle", player);
			break;
			case "pool":
				if(args.length < 2)
					return false;
				
				setSelection(false, args[1], args[2], "pool", player);
			break;
			case "teamzone":
				if(args.length < 3)
					return false;
				
				setSelection(false, args[1], args[2], "teamzone", player);
			break;
			case "spawnzone":
				if(args.length < 3)
					return false;
				
				setSelection(false, args[1], args[2], "spawnzone", player);
			break;
			case "respawnlocation":
				if(args.length < 3)
					return false;
				
				setLocation(false, args[1], args[2], "respawnLocation", player);
			break;
			default: return false;
		}
		
		player.sendTranslatedMessage("commands.tower.modified");
		
		return true;
	}
}
