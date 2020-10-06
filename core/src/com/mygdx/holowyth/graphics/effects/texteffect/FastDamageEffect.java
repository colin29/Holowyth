package com.mygdx.holowyth.graphics.effects.texteffect;

import org.apache.commons.lang3.RandomUtils;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.util.dataobjects.Point;

public class FastDamageEffect extends DamageEffect {

	float angleOfMotion = RandomUtils.nextFloat(90, 90);

	public FastDamageEffect(String text, Point pos, PresetType presetType) {
		super(text, pos, presetType);
		setStartingDuration(80);
		setInitialSpeed(3);
		fullOpacityDuration = 50;
	}

	private Vector2 calc = new Vector2();

	@Override
	public void tick() {
		durationLeft -= 1;

		calc.set(getCurrentVelocity(), 0);
		calc.rotate(angleOfMotion);

		x += calc.x;
		y += calc.y;
	}

}
