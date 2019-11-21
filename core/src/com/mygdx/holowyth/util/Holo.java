package com.mygdx.holowyth.util;

import com.badlogic.gdx.graphics.Color;

/**
 * Contains all the global settings for Holowyth
 * 
 * @author Colin Ta
 *
 */
public class Holo {

	// Paths
	public static String mapsDirectory = HoloIO.getCanonicalPathElseNull("./saveFiles/");

	// Initial Settings (only apply at startup)
	public static boolean enableCursorGrabbing = false;

	public static String titleName = "Holowyth";

	public static String defaultMapName = "UntitledMap";
	public static int defaultMapWidth = 800;
	public static int defaultMapHeight = 600;

	// Pathfinding and collision detection
	public static float defaultUnitMoveSpeed = 0.8f; // world units per frame
	public static float collisionClearanceDistance = defaultUnitMoveSpeed / 5; // extra distance "pushed out" by the
																				// collision detection upon collision

	public static final int CELL_SIZE = 15; // size in world units
	public static float UNIT_RADIUS = 13;

	public static boolean largeSize = false; // use larger sized units and faster movement speed for easier debugging in
												// some cases
	static {
		if (largeSize) {
			defaultUnitMoveSpeed = 10f;
			collisionClearanceDistance = defaultUnitMoveSpeed / 5;
			UNIT_RADIUS = 20;
		}
	}

	// Unit behaviour
	public static boolean idleEnemyUnitsAggro = true;

	// Debug Cheats
	public static boolean debugFastCastEnbabled = false;
	public static boolean debugHighHpUnits = true;
	public static boolean debugNoManaCost = true;

	// Combat movement
	/**
	 * You should add this.radius and target.radius to this value to get the actual distance
	 */
	public static float defaultUnitEngageRange = 5;
	public static float defaultUnitSwitchEngageRange = defaultUnitEngageRange + 5;
	public static float defaultUnitDisengageRange = defaultUnitEngageRange + 10; // the distance the enemy must travel
																					// before it stops receiving attacks
																					// from the unit.
	public static float defaultAggroRange = 150;
	public static float alliedUnitsAggroRange = 100;

	public static float defaultUnitAttackChaseRange = 225;
	public static float alliedUnitsAttackChaseRange = 175;

	// Editor
	public static String editorInitialMap = "/complexMap.map"; // "/blankMap.map";

	// Debug display
	public static boolean debugPanelShowAtStartup = true;
	public static Color debugFontColor = Color.BLACK;

	// Debug settings
	public static boolean continueShowingPathAfterArrival = true;
	public static boolean debugPathfindingIgnoreUnits = false;

	// Rendering Testing
	public static boolean useTestSprites = false;

}
