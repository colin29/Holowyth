package com.mygdx.holowyth;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.mygdx.holowyth.pathfinding.PathingModule;

public interface World { 
	//contains information which units can query
	public ArrayList<Unit> getUnits();
	public PathingModule getPathingModule();	
}
