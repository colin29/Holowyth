package com.mygdx.holowyth.graphics.effects.texteffect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * An effect that displays a number that slides upwards and eventually fades. Also used for showing healing numbers and misses
 * 
 * @author Colin Ta
 *
 */
public class DamageEffect {
	
	private static int standardDuration =  100; //130;
	private static float standardInitialSpeed = 2f; //0.4f;

	public int fullOpacityDuration;
	
	protected float initialSpeed = standardInitialSpeed;
	protected int duration = standardDuration;
	protected int durationLeft = duration;

	public String text;
	float origX, origY;
	public float x;
	public float y;
	
	public BitmapFont font;

	public Color color = Color.WHITE; // color only applies if presetType is null

	DamageEffect(String text, float x, float y, BitmapFont font) {
		this.text = text;
		this.origX = x;
		this.origY = y;
		this.x = x;
		this.y = y;
		fullOpacityDuration = duration / 4;
		this.font = font;
	}

	public DamageEffect(String text, Point pos, BitmapFont font) {
		this(text, pos.x, pos.y, font);
	}

	DamageEffect(String text, Point pos, Color color, BitmapFont font) {
		this(text, pos, font);
		this.color = color;
	}

	public DamageEffect(String text, float x, float y, Color color, BitmapFont font) {
		this(text, x, y, font);
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

	public void setDuration(int value) {
		duration = value;
		durationLeft = duration;
	}

	public void setInitialSpeed(float value) {
		initialSpeed = value;
	}

	public float getInitialSpeed() {
		return initialSpeed;
	}

}
