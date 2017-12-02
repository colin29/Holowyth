package com.mygdx.holowyth.util.constants;

import java.io.IOException;
import java.nio.file.Paths;

public class Holo {
	
	// Paths
	public static String mapsDirectory = getCanonicalPath("./../saveFiles/");
	
	// Initial Settings (only apply at startup)
	public static boolean enableCursorGrabbing = false;
	
	public static String titleName = "Holowyth";
	
	public static String defaultMapName = "UntitledMap";
	public static int defaultMapWidth = 800;
	public static int defaultMapHeight = 600;
	
	
	//Parameters for testing
	public static float defaultUnitMoveSpeed = 0.8f; //world units per frame
	public static float collisionClearanceDistance = defaultUnitMoveSpeed/5; //extra distance "pushed out" by the collision detection upon collision
	
	public static int CELL_SIZE = 15; //size in world units
	public static float UNIT_RADIUS = 13;
	
	public static boolean largeSize = false; //use larger sized units and faster movement speed for easier debugging in some cases
	static{
		if(largeSize){
			defaultUnitMoveSpeed = 15f;
			collisionClearanceDistance = defaultUnitMoveSpeed/5;
			UNIT_RADIUS = 30;
		}
	}
	
	//Editor
	public static String editorInitialMap = "/complexMap.map";//"/blankMap.map";
	
	//Debug settings
	public static boolean continueShowingPathAfterArrival = true;
 	public static boolean debugPathfindingIgnoreUnits = false;
	
	private static String getCanonicalPath(String string){
		try {
			return Paths.get(string).toRealPath().toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	


	
}
