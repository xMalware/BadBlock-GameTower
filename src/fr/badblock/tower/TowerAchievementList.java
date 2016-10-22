package fr.badblock.tower;

import fr.badblock.gameapi.achievements.AchievementList;
import fr.badblock.gameapi.achievements.PlayerAchievement;
import fr.badblock.gameapi.run.BadblockGame;

public class TowerAchievementList {
	public static AchievementList instance = new AchievementList(BadblockGame.TOWER);
	
	/*
	 * Tuer X personnes
	 */
	public static final PlayerAchievement TOWER_KILL_1 = instance.addAchievement(new PlayerAchievement("tower_kill_1", 10, 5, 10));
	public static final PlayerAchievement TOWER_KILL_2 = instance.addAchievement(new PlayerAchievement("tower_kill_2", 50, 25, 100));
	public static final PlayerAchievement TOWER_KILL_3 = instance.addAchievement(new PlayerAchievement("tower_kill_3", 250, 100, 1000));
	public static final PlayerAchievement TOWER_KILL_4 = instance.addAchievement(new PlayerAchievement("tower_kill_4", 500, 250, 10000));
	
	/*
	 * Marquer X points
	 */
	public static final PlayerAchievement TOWER_MARK_1  = instance.addAchievement(new PlayerAchievement("tower_mark_1", 10, 5, 5));
	public static final PlayerAchievement TOWER_MARK_2  = instance.addAchievement(new PlayerAchievement("tower_mark_2", 50, 25, 500));
	public static final PlayerAchievement TOWER_MARK_3  = instance.addAchievement(new PlayerAchievement("tower_mark_3", 250, 100, 5000));
	public static final PlayerAchievement TOWER_MARK_4  = instance.addAchievement(new PlayerAchievement("tower_mark_4", 500, 250, 20000));

    /*
	 * Marquer 5 points en une partie sur X parties
	 */
	public static final PlayerAchievement TOWER_MARKER_1  = instance.addAchievement(new PlayerAchievement("tower_marker_1", 10, 5, 5));
	public static final PlayerAchievement TOWER_MARKER_2  = instance.addAchievement(new PlayerAchievement("tower_marker_2", 50, 25, 50));
	public static final PlayerAchievement TOWER_MARKER_3  = instance.addAchievement(new PlayerAchievement("tower_marker_3", 250, 100, 500));
	public static final PlayerAchievement TOWER_MARKER_4  = instance.addAchievement(new PlayerAchievement("tower_marker_4", 500, 250, 5000));

    
	/*
	 * Gagner X parties
	 */
	public static final PlayerAchievement TOWER_WIN_1  = instance.addAchievement(new PlayerAchievement("tower_win_1", 10, 2, 1));
	public static final PlayerAchievement TOWER_WIN_2  = instance.addAchievement(new PlayerAchievement("tower_win_2", 50, 25, 100));
	public static final PlayerAchievement TOWER_WIN_3  = instance.addAchievement(new PlayerAchievement("tower_win_3", 250, 100, 1000));
	public static final PlayerAchievement TOWER_WIN_4  = instance.addAchievement(new PlayerAchievement("tower_win_4", 500, 250, 10000));
	
	/*
	 * Tuer 10 joueurs dans une même partie
	 */
	public static final PlayerAchievement TOWER_KILLER = instance.addAchievement(new PlayerAchievement("tower_killer", 100, 50, 10, true));
	/*
	 * Tuer 20 joueurs dans une même partie
	 */
	public static final PlayerAchievement TOWER_UKILLER = instance.addAchievement(new PlayerAchievement("tower_ukiller", 250, 100, 25, true));

	/*
	 * Tuer 15 à l'arc joueurs dans une même partie
	 */
	public static final PlayerAchievement TOWER_SHOOTER = instance.addAchievement(new PlayerAchievement("tower_shooter", 100, 50, 15, true));
	
	/*
	 * Ne frapper les adverseraires qu'à l'arc et faire 20 kills
	 */
	public static final PlayerAchievement TOWER_USHOOTER = instance.addAchievement(new PlayerAchievement("tower_ushooter", 250, 150, 25, true));
	
	/**
	 * Marquer 10 points dans la même partie
	 */
	public static final PlayerAchievement TOWER_MARKER = instance.addAchievement(new PlayerAchievement("tower_umarker", 100, 50, 10, true));

	/**
	 * Exploser 3 lits dans une même partie
	 */
	public static final PlayerAchievement TOWER_ALLKITS = instance.addAchievement(new PlayerAchievement("tower_allkits", 300, 150, 3, true));
}
