package com.mygdx.holowyth.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * ShapeDrawer class with a single extra convenience method for controlling alpha
 */
public class ShapeDrawerPlus extends ShapeDrawer {

	public ShapeDrawerPlus(Batch batch, TextureRegion region) {
		super(batch, region);
	}

	public void filledCircle(float x, float y, float radius) {
		float radiusStep = 1f;
		for (float i = radius; i > 0; i -= radiusStep) {
			circle(x, y, i);
		}
		circle(x, y, 0.5f);
	}

	public float setColor(Color src, float alpha) {
		var color = new Color(src);
		color.a = alpha;
		return setColor(color);
	}

	public float setAlpha(float alpha) {
		var color = new Color();
		Color.abgr8888ToColor(color, getPackedColor());
		color.a = alpha;
		return setColor(color);
	}

}
