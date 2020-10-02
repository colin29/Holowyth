package com.mygdx.holowyth.gamedata.skillsandeffects;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.game.MapInstance;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

public class Effects {

	static class CircleOutlineVfx extends Effect {

		private static int vfxDuration = 80;

		private float x, y;
		private float aoeRadius;
		private int framesElapsed = 0;

		private Color color;

		public CircleOutlineVfx(float x, float y, float aoeRadius, Color vfxColor, MapInstance world) {
			super(world);
			this.aoeRadius = aoeRadius;

			this.x = x;
			this.y = y;
			this.color = vfxColor;
		}

		@Override
		public void tick() {
			if (framesElapsed >= vfxDuration) {
				markAsComplete();
			}
			framesElapsed += 1;
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(color, getOpacity());

			batch.begin();
			shapeDrawer.circle(x, y, aoeRadius);
			batch.end();
		}

		private float getOpacity() {
			return 0.9f * (1 - framesElapsed / (float) vfxDuration);
		}

	}

}
