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
	public static String simpleMapsDirectory = "resources/assets/maps/old/";
	public static String mapsDirectory = "assets/maps/";

	// Demos
	public static String initialSimpleMap = "/complexMap.map"; // "/blankMap.map"; // only for demos that use simple maps

	// Initial Settings (only apply at startup)
	public static boolean enableCursorGrabbing = false;
	public static boolean mouseScrollEnabled = true;
	
	public static String titleName = "Holowyth";

	public static String defaultMapName = "UntitledMap";
	public static int defaultMapWidth = 800;
	public static int defaultMapHeight = 600;
	public static final int GAME_FPS = 60;

	// Pathfinding and collision detection
	public static float defaultUnitMoveSpeed = 2; //6f; //0.8 // world units per frame
	public static float collisionClearanceDistance = defaultUnitMoveSpeed / 5; // extra distance "pushed out" by the
																				// collision detection upon collision
	public static float epsilon = 0.001f;

	public static final int CELL_SIZE = 15; // size in world units

	public static float UNIT_RADIUS = 17; // 13

	public static boolean debugRenderMapObstaclesEdges = true;

	// Gameplay Cheats
	public static boolean debugFastCastEnabled = false;
	public static boolean debugSkillCooldownDisabled = true;
	public static boolean debugHighHpUnits = false;
	public static boolean debugNoManaCost = false;
	public static boolean debugAllowSelectEnemyUnits = true;
	
	public static boolean debugDisplayEnemyCastingProgress = true;

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

	// Debug display
	public static boolean debugPanelShowAtStartup = false;
	public static Color debugFontColor = Color.BLACK;
	public static boolean debugShowMouseLocationText = true;

	// Debug settings
	public static boolean debugRenderEnemyPath = false;
	public static boolean continueShowingPathAfterArrival = false;
	public static boolean debugPathfindingIgnoreUnits = false;

	// Play Mode (overrides debug settings to false)
	public static boolean releaseMode = false;
	
	static {
		if (releaseMode) {
			debugFastCastEnabled = false;
			debugSkillCooldownDisabled = false;
			debugHighHpUnits = false;
			debugNoManaCost = false;

			debugAllowSelectEnemyUnits = false;
			debugDisplayEnemyCastingProgress = false;
			debugRenderEnemyPath = false;

			debugPanelShowAtStartup = false;
			debugShowMouseLocationText = false;
			// Rendering
			debugRenderMapObstaclesEdges = false;

		}

	}

}
