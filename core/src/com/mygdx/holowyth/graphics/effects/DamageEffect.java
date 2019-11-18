package com.mygdx.holowyth.graphics.effects;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * An effect that displays a number that slides upwards and eventually fades
 * 
 * @author Colin Ta
 *
 */
public class DamageEffect {

	private static int duration = 110;
	private static float initialVelocity = 0.4f;

	private int durationLeft = duration;

	String text;
	float origX, origY;
	float x, y;

	Color color = Color.WHITE; // color only applies if presetType is null

	enum PresetType {
		PLAYER, ENEMY;
	}

	PresetType presetType;

	DamageEffect(String text, float x, float y) {
		this.text = text;
		this.origX = x;
		this.origY = y;
		this.x = x;
		this.y = y;
	}

	DamageEffect(String text, Point pos) {
		this(text, pos.x, pos.y);
	}

	DamageEffect(String text, Point pos, PresetType presetType) {
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

	private float getCurrentVelocity() {
		float ratio = Math.max(0, (durationLeft - 25.0f) / duration);
		return initialVelocity * ratio;
	}

	private static float fullOpacityDuration = duration / 3.0f;

	public float getCurrentOpacity() {
		if (durationLeft >= duration - fullOpacityDuration) {
			return 1;
		} else {
			return durationLeft / (duration - fullOpacityDuration);
		}
	}

	public boolean isExpired() {
		return durationLeft <= 0;
	}

	public boolean isUsingPreset() {
		return presetType != null;
	}

}
