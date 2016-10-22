package fr.badblock.tower.players;

import fr.badblock.gameapi.players.data.InGameData;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TowerData implements InGameData {
	public int  kills 		= 0;
	public int  deaths		= 0;
	public int  marks	    = 0;
	
	public long nextMark    = 0;

	public long cooldown	= 0;
	
	public int getScore(){
		return (kills * 20 + marks * 20) / (deaths == 0 ? 1 : (10 * deaths));
	}
}
