package com.mygdx.holowyth.pathfinding.demo;

import java.util.ArrayList;
import com.mygdx.holowyth.pathfinding.PathingModule;

public interface PFWorld { 
	//contains information which units can query
	public ArrayList<PFDemoUnit> getUnits();
	public PathingModule getPathingModule();	
}
