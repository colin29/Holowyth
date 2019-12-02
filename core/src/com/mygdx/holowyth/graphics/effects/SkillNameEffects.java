package com.mygdx.holowyth.graphics.effects;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mygdx.holowyth.unit.interfaces.UnitInfo;

public class SkillNameEffects {

	private Map<UnitInfo, SkillNameEffect> effects = new HashMap<>();

	public void tick() {
		for (var effect : effects.values()) {
			effect.tick();
		}
		effects = filterByValue(effects, (e) -> !e.isComplete());
	}

	private static <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate) {
		return map.entrySet()
				.stream()
				.filter(x -> predicate.test(x.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Does not call begin on the effect
	 * 
	 * If there skill text already exists on the unit, removes the previous effect first.
	 */
	public void addSkillTextOn(UnitInfo unit, SkillNameEffect effect) {
		removeSkillTextOn(unit);
		effects.put(unit, effect);
	}

	public void removeSkillTextOn(UnitInfo unit) {
		if (effects.containsKey(unit)) {
			if (effects.get(unit) != null) {
				effects.get(unit).forceEnd();
			}
			effects.remove(unit);
		}
	}
}
