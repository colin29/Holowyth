package com.mygdx.holowyth.game.rendering.aiminggraphic;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 * Skills with custom aim helpers go here
 * 
 * @author Colin Ta
 *
 */
public class SkillsAimingGraphics {

	public static class HydroblastAimingGraphic extends AimingGraphic {

		private float coneLength;
		private float coneInnerLength;
		private float coneWidthDegrees;

		/**
		 * @param x
		 * @param y
		 *            casting location of the spell
		 * @param coneAngle
		 * @param coneWidthDegrees
		 * @param coneLength
		 * @param coneInnerLength
		 * @param world
		 */
		public HydroblastAimingGraphic(float coneLength, float coneInnerLength, float coneWidthDegrees, MapInstanceInfo world) {
			super(world);

			this.coneLength = coneLength;
			this.coneInnerLength = coneInnerLength;
			this.coneWidthDegrees = coneWidthDegrees;

		}

		@Override
		public void render(Vector2 cursorPos, UnitInfo caster, MapInstanceInfo world, SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.BLUE, 0.7f);

			Point cursorPoint = new Point(cursorPos);

			float x = caster.getX();
			float y = caster.getY();
			float coneAngle = Point.getAngleInDegrees(caster.getPos(), cursorPoint);

			Vector2 conePointLeft;
			Vector2 conePointMiddle; // the point at the middle of the far end of the cone
			Vector2 conePointRight;

			Vector2 coneInnerPointLeft;
			Vector2 coneInnerPointMiddle;
			Vector2 coneInnerPointRight;

			var calc = new Vector2();

			calc.set(coneLength, 0).rotate(coneAngle + coneWidthDegrees / 2);
			conePointLeft = new Vector2(x, y).add(calc);

			calc.set(coneLength, 0).rotate(coneAngle - coneWidthDegrees / 2);
			conePointRight = new Vector2(x, y).add(calc);

			calc.set(coneLength, 0).rotate(coneAngle);
			conePointMiddle = new Vector2(x, y).add(calc);

			calc.set(coneInnerLength, 0).rotate(coneAngle + coneWidthDegrees / 2);
			coneInnerPointLeft = new Vector2(x, y).add(calc);

			calc.set(coneInnerLength, 0).rotate(coneAngle - coneWidthDegrees / 2);
			coneInnerPointRight = new Vector2(x, y).add(calc);

			calc.set(coneInnerLength, 0).rotate(coneAngle);
			coneInnerPointMiddle = new Vector2(x, y).add(calc);

			// approximate the actual conical hitbox
			float[] vertices = new float[8];
			vertices[0] = x;
			vertices[1] = y;
			vertices[2] = conePointLeft.x;
			vertices[3] = conePointLeft.y;
			vertices[4] = conePointMiddle.x;
			vertices[5] = conePointMiddle.y;
			vertices[6] = conePointRight.x;
			vertices[7] = conePointRight.y;

			Array<Vector2> innerConePath = new Array<Vector2>();
			innerConePath.add(coneInnerPointLeft, coneInnerPointMiddle, coneInnerPointRight);

			Polygon cone = new Polygon(vertices);

			batch.begin();
			shapeDrawer.polygon(cone, 1.8f);
			shapeDrawer.path(innerConePath, 1.3f, true);
			batch.end();
		}
	}

}
