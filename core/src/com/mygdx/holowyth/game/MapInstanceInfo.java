package com.mygdx.holowyth.game;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;

import com.badlogic.gdx.assets.AssetManager;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.unit.sprite.Animations;

public interface MapInstanceInfo {

	/**
	 * The list itself is read-only, though elements can be modified
	 */
	public List<@NonNull Unit> getUnits();

	public PathingModule getPathingModule();

	public EffectsHandler getGfx();

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

	Set<Unit> getUnitsAttackingThis(UnitOrderable u);

	AssetManager getAssets();

	Animations getAnimations();
}
