package com.mygdx.holowyth.graphics.effects.animated;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.sprite.Animations;

@NonNullByDefault
public class EffectCenteredOnUnit extends EffectCenteredOnPos {

	public EffectCenteredOnUnit(UnitInfo unit, String animName, MapInstanceInfo world, Animations animations) {

		super(new UnitPosSource(unit), animName, world, animations);
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
