package com.mygdx.holowyth.graphics.effects.animated;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;

@NonNullByDefault
public class AnimEffectOnUnit extends AnimEffectOnPos {

	public AnimEffectOnUnit(UnitInfo unit, String animName, MapInstanceInfo mapInstance) {
		super(new UnitPosSource(unit), animName, mapInstance);
	}

	private static class UnitPosSource implements PosSource {
		UnitInfo unit;

		UnitPosSource(UnitInfo unit) {
			this.unit = unit;
		}
		@Override
		public float getX() {
			return unit.getX();
		}
		@Override
		public float getY() {
			return unit.getY();
		}
	}

}
