package fr.badblock.bukkit.games.tower.players;

import java.util.HashMap;
import java.util.Map;

import fr.badblock.gameapi.players.data.InGameData;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TowerData implements InGameData {
	
	public Map<String, Long> lastKills = new HashMap<>();
	public int  kills 		= 0;
	public int  deaths		= 0;
	public int  marks	    = 0;
	
	public long nextMark    = 0;

	public long cooldown	= 0;
	
	public double givenDamages;
	
	public int getScore(){
		return (kills * 20 + marks * 20) / (deaths == 0 ? 1 : (deaths));
	}
	
}