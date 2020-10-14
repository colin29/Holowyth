package com.mygdx.holowyth.graphics.effects.texteffect;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * An effect that displays a number that slides upwards and eventually fades. Used for displaying damage numbers, but also healing and misses
 * 
 * @author Colin Ta
 *
 */
public class DamageEffect {

	private static int standardDuration = 110;
	private static float standardInitialSpeed = 0.4f;

	protected int fullOpacityDuration;

	protected float initialSpeed;
	protected int duration;
	protected int durationLeft;

	public String text;
	float origX, origY;
	public float x;
	public float y;

	public Color color = Color.WHITE; // color only applies if presetType is null

	public enum PresetType {
		PLAYER, ENEMY;
	}

	public PresetType presetType;

	DamageEffect(String text, float x, float y) {
		this.text = text;
		this.origX = x;
		this.origY = y;
		this.x = x;
		this.y = y;
		setStartingDuration(standardDuration);
		setInitialSpeed(standardInitialSpeed);
		fullOpacityDuration = duration / 3;
	}

	DamageEffect(String text, Point pos) {
		this(text, pos.x, pos.y);
	}

	public DamageEffect(String text, Point pos, PresetType presetType) {
		this(text, pos);
		this.presetType = presetType;
	}

	DamageEffect(String text, Point pos, Color color) {
		this(text, pos);
		this.color = color;
	}

	public DamageEffect(String text, float x, float y, Color color) {
		this(text, x, y);
		this.color = color;
	}

	public void tick() {
		durationLeft -= 1;
		y += getCurrentVelocity();
	}

	protected float getCurrentVelocity() {
		float ratio = Math.max(0, (durationLeft - 25.0f) / standardDuration);
		return initialSpeed * ratio;
	}

	public float getCurrentOpacity() {
		if (durationLeft >= standardDuration - fullOpacityDuration) {
			return 1;
		} else {
			return durationLeft / (float) (standardDuration - fullOpacityDuration);
		}
	}

	public boolean isExpired() {
		return durationLeft <= 0;
	}

	public boolean isUsingPreset() {
		return presetType != null;
	}

	protected void setStartingDuration(int value) {
		duration = value;
		durationLeft = duration;
	}

	protected void setInitialSpeed(float value) {
		initialSpeed = value;
	}

}
