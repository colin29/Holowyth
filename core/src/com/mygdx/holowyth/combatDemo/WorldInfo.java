package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;

import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;

public interface WorldInfo { 
	//contains information which units can query
	public ArrayList<Unit> getUnits();
	public PathingModule getPathingModule();	
	public EffectsHandler getEffectsHandler();
}
