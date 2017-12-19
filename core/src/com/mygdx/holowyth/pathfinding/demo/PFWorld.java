package com.mygdx.holowyth.pathfinding.demo;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.mygdx.holowyth.pathfinding.PathingModule;

public interface PFWorld { 
	//contains information which units can query
	public ArrayList<PFDemoUnit> getUnits();
	public PathingModule getPathingModule();	
}
