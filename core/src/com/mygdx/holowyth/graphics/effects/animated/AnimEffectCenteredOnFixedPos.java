package com.mygdx.holowyth.graphics.effects.animated;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.sprite.Animations;

@NonNullByDefault
public class AnimEffectCenteredOnFixedPos extends AnimEffectCenteredOnPos {

	public AnimEffectCenteredOnFixedPos(float x, float y, String animName, MapInstanceInfo world, Animations animations) {

		super(new FixedPosSource(x, y), animName, world, animations);
	}

	private static class FixedPosSource implements PosSource {
		float x, y;

		FixedPosSource(float x, float y) {
			this.x = x;
			this.y = y;
		}
		@Override
		public float getX() {
			return x;
		}
		@Override
		public float getY() {
			return y;
		}
	}

}
