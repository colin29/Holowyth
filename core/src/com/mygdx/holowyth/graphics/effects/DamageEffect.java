package com.mygdx.holowyth.graphics.effects;

import com.badlogic.gdx.graphics.Color;

/**
 * The effect that displays a number when damage is dealt.
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
	Color color = Color.WHITE;
	
	DamageEffect(String text, float x, float y){
		this.text = text;
		this.origX = x;
		this.origY = y;
		this.x = x;
		this.y = y;
	}
	DamageEffect(String text, float x, float y, Color color){
		this(text, x, y);
		this.color = color;
	}
	
	public void tick(){
		durationLeft -=1;
		y += getCurrentVelocity();
	}
	private float getCurrentVelocity() {
		float ratio = Math.max(0, (durationLeft-25.0f) /duration);
		return initialVelocity * ratio;
	}
	
	private static float fullOpacityDuration = duration/3.0f;
	public float getCurrentOpacity() {
		if(durationLeft >= duration - fullOpacityDuration) {
			return 1;
		}else {
			return durationLeft / (duration - fullOpacityDuration);
		}
	}
	
	public boolean isExpired() {
		return durationLeft <= 0;
	}
	
	
	
	
	
}
