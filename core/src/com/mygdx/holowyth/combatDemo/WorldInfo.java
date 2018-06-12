package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;

import com.mygdx.holowyth.pathfinding.PathingModule;

public interface WorldInfo { 
	//contains information which units can query
	public ArrayList<Unit> getUnits();
	public PathingModule getPathingModule();	
}
