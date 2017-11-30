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
	public static float defaultUnitMoveSpeed = 3f; //0.8f; //world units per frame
	public static int CELL_SIZE = 15; //size in world units
	public static float UNIT_RADIUS = 13;
	
	//Editor
	public static String editorInitialMap = "/complexMap.map";
	
	//Debug settings
	public static boolean continueShowingPathAfterArrival = true;
	
	private static String getCanonicalPath(String string){
		try {
			return Paths.get(string).toRealPath().toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	
}
