package com.mygdx.holowyth.combatDemo;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;

public interface WorldInfo {
	// contains information which units can query
	public List<Unit> getUnits();

	public PathingModule getPathingModule();

	public EffectsHandler getEffectsHandler();

	default public void doIfTrueForAllUnits(Predicate<UnitInfo> predicate, Consumer<UnitInfo> task) {
		for (UnitInfo unit : getUnits()) {
			if (predicate.test(unit)) {
				task.accept(unit);
			}
		}
	}

	default public void doForAllUnits(Consumer<UnitInfo> task) {
		for (UnitInfo unit : getUnits()) {
			task.accept(unit);
		}
	}

	List<Effect> getEffects();
}
