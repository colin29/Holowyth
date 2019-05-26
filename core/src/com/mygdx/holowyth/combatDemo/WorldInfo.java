package com.mygdx.holowyth.combatDemo;

import java.util.List;

import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;

public interface WorldInfo {
	// contains information which units can query
	public List<Unit> getUnits();

	public PathingModule getPathingModule();

	public EffectsHandler getEffectsHandler();
}
