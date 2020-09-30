package com.mygdx.holowyth.pathfinding.demo;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.pathfinding.PathingModule;

public interface PFWorld { 
	//contains information which units can query
	public ArrayList<@NonNull PFDemoUnit> getUnits();
	public PathingModule getPathingModule();	
}
