package com.mygdx.holowyth.gamedata.skillsandeffects;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.holowyth.game.MapInstance;
import com.mygdx.holowyth.gamedata.skillsandeffects.mageProjectiles.ArcaneBoltBolt;
import com.mygdx.holowyth.gamedata.skillsandeffects.mageProjectiles.FireballBolt;
import com.mygdx.holowyth.gamedata.skillsandeffects.mageProjectiles.MagicMissileBolt;
import com.mygdx.holowyth.gamedata.skillsandeffects.mageProjectiles.ProjectileBase;
import com.mygdx.holowyth.gamedata.skillsandeffects.mageProjectiles.WindBladeBolt;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.skill.effect.CasterGroundEffect;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.dataobjects.Line;
import com.mygdx.holowyth.util.dataobjects.Point;

public class MageEffects {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(MageEffects.class);

	static class MagicMissileEffect extends CasterUnitEffect {
		public MagicMissileEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		public static int damage = 5;
		List<MagicMissileBolt> missiles;

		float missileVfxRadius = 5;

		@Override
		public void begin() {
			missiles = new ArrayList<MagicMissileBolt>();
			for (int i = 0; i < 3; i++) {
				missiles.add(new MagicMissileBolt(caster.x, caster.y + 18 * i, damage, target, caster, mapInstance.getUnits()));
			}
		}

		@Override
		public void tick() {
			for (var m : missiles) {
				m.tick();
			}
			missiles.removeIf(m -> m.isExpired() || m.isCollided());

			if (missiles.isEmpty())
				markAsComplete();
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.RED, 0.8f);
			batch.begin();
			for (var m : missiles) {
				shapeDrawer.filledCircle(m.getX(), m.getY(), missileVfxRadius);
			}
			batch.end();
		}

	};

	static class ArcaneBoltEffect extends CasterUnitEffect {

		protected ArcaneBoltEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		ProjectileBase projectile;

		public static int damage = 24;
		private static float missileVfxRadius = 6;

		@Override
		public void begin() {
			projectile = new ArcaneBoltBolt(damage, caster, target);
		}

		@Override
		public void tick() {
			projectile.tick();
			if (projectile.isExpired() || projectile.isCollided()) {
				markAsComplete();
			}

		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.VIOLET, 1f);
			batch.begin();
			shapeDrawer.filledCircle(projectile.getX(), projectile.getY(), missileVfxRadius);
			batch.end();
		}

	}

	static class WindBladesEffect extends CasterUnitEffect {
		public WindBladesEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		int damage = 6;
		int numMissiles = 4;
		int missilesLeftToFire = numMissiles;
		int missileFiringInterval = 10;

		List<ProjectileBase> missiles = new ArrayList<ProjectileBase>();

		float missileVfxRadius = 4;

		int framesElapsed = 0;

		@Override
		public void tick() {

			if (framesElapsed % missileFiringInterval == 0 && missilesLeftToFire > 0) {
				missilesLeftToFire = Math.max(0, missilesLeftToFire - 1);
				missiles.add(new WindBladeBolt(caster.x, caster.y, damage, caster, target)); // space the missiles out
			}

			for (var m : missiles) {
				m.tick();
			}
			missiles.removeIf(m -> m.isExpired() || m.isCollided());

			if (missilesLeftToFire == 0 && missiles.isEmpty())
				markAsComplete();

			framesElapsed += 1;
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(HoloGL.rgb(179, 255, 25), 0.8f);
			batch.begin();
			for (var m : missiles) {
				shapeDrawer.filledCircle(m.getX(), m.getY(), missileVfxRadius);
			}
			batch.end();
		}
	}

	static class FireBallEffect extends CasterUnitEffect {

		static int damage = 18;

		protected FireBallEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		ProjectileBase projectile;

		private static float missileVfxRadius = 6;
		private static float explosionRadius = 30;

		@Override
		public void begin() {
			projectile = new FireballBolt(damage, explosionRadius, caster, target);
		}

		@Override
		public void tick() {
			projectile.tick();
			if (projectile.isExpired() || projectile.isCollided()) {
				mapInstance.addEffect(new FireBallVfx(projectile.getX(), projectile.getY(), explosionRadius, mapInstance));
				markAsComplete();
			}

		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.ORANGE, 1f);
			batch.begin();
			shapeDrawer.filledCircle(projectile.getX(), projectile.getY(), missileVfxRadius);
			batch.end();
		}

	}

	static class FireBallVfx extends Effect {

		private float x, y;

		private static int explosionVfxDuration = 40;
		private float explosionRadius;
		private int framesElapsed = 0;

		FireBallVfx(float x, float y, float explosionRadius, MapInstance world) {
			super(world);
			this.x = x;
			this.y = y;

			this.explosionRadius = explosionRadius;
		}

		@Override
		public void tick() {
			if (framesElapsed >= explosionVfxDuration) {
				markAsComplete();
			}
			framesElapsed += 1;
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.ORANGE, getOpacity());
			batch.begin();
			shapeDrawer.filledCircle(x, y, explosionRadius);
			batch.end();
		}

		private float getOpacity() {
			return 0.7f * (1 - framesElapsed / (float) explosionVfxDuration);
		}

	}

	public static class HydroblastEffect extends CasterGroundEffect {

		protected HydroblastEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		static float coneWidthDegrees = 50;
		static float coneLength = 150; // how far does the cone extend.
		static float coneInnerLength = coneLength / 2; // there is an inner cone
		float coneAngle; // angle that the skill was aimed at

		static float slowAmount = 0.6f;
		static int slowDuration = 60 * 4;

		@Override
		public void begin() {
			coneAngle = Point.getAngleInDegrees(caster.getPos(), ground);
		}

		@Override
		public void tick() {

			for (Unit u : mapInstance.getUnits()) {
				if (u == caster)
					continue;
				if (isUnitTouchingCone(u)) {
					if (isUnitWithinInnerRadius(u)) {
						Vector2 knockbackVec = new Vector2(u.x, u.y).sub(caster.x, caster.y).setLength(0.8f);
						u.stats.doKnockBackRollAgainst(20, 0, knockbackVec);
					}
					u.status.applySlow(slowAmount, slowDuration);

				}
			}
			mapInstance.addEffect(
					new HydroBlastVfx(caster.x, caster.y, coneAngle, coneWidthDegrees, coneLength, coneInnerLength, mapInstance));
			markAsComplete();
		}

		private boolean isUnitTouchingCone(Unit unit) {
			Vector2 calc = new Vector2();

			calc.set(1, 0);
			calc.rotate(coneAngle + coneWidthDegrees / 2);
			Line leftLine = new Line(caster.x, caster.y, calc.x, calc.y);

			calc.set(1, 0);
			calc.rotate(coneAngle - coneWidthDegrees / 2);
			Line rightLine = new Line(caster.x, caster.y, calc.x, calc.y);

			// we need to displace these lines to account for the size of the targets

			Vector2 displaceVec;
			displaceVec = leftLine.getV().rotate(90).setLength(unit.getRadius());
			leftLine.displace(displaceVec);

			displaceVec = rightLine.getV().rotate(-90).setLength(unit.getRadius());
			rightLine.displace(displaceVec);

			var unitPos = unit.getPos();

			boolean unitInRange = Point.dist(caster.getPos(), unitPos) <= coneLength + unit.getRadius();
			return !leftLine.doesPointLieOnTheLeft(unitPos) &&
					rightLine.doesPointLieOnTheLeft(unitPos) &&
					unitInRange;
		}

		private boolean isUnitWithinInnerRadius(Unit unit) {
			return Point.dist(caster.getPos(), unit.getPos()) <= coneInnerLength + unit.getRadius();
		}
	}

	static class HydroBlastVfx extends Effect {

		private float x, y;

		private static int vfxDuration = 100;
		private int framesElapsed = 0;

		private final Vector2 conePointLeft;
		private final Vector2 conePointMiddle; // the point at the middle of the far end of the cone
		private final Vector2 conePointRight;

		private final Vector2 coneInnerPointLeft;
		private final Vector2 coneInnerPointMiddle;
		private final Vector2 coneInnerPointRight;

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
		HydroBlastVfx(float x, float y, float coneAngle, float coneWidthDegrees, float coneLength, float coneInnerLength, MapInstance world) {
			super(world);
			this.x = x;
			this.y = y;

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
			shapeDrawer.setColor(Color.BLUE, getOpacity());

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

		private float getOpacity() {
			return 0.9f * (1 - framesElapsed / (float) vfxDuration);
		}

	}

	static class ThunderclapEffect extends CasterGroundEffect {

		static float aoeRadius = 80f;

		static float stunDuration = 60 * 1.5f;
		static int forceValue = 18;

		protected ThunderclapEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		@Override
		public void tick() {
			for (Unit unit : mapInstance.getUnits()) {
				if (Point.dist(ground, unit.getPos()) <= aoeRadius + unit.getRadius()) {
					if (unit.getSide() != caster.getSide()) {
						unit.stats.doStunRollAgainst(forceValue, stunDuration);
					} else {
						unit.stats.doStunRollAgainst(forceValue-10, stunDuration);
					}
				}
			}
			markAsComplete();
			mapInstance.addEffect(new Effects.CircleOutlineVfx(ground.x, ground.y, aoeRadius, Color.ORANGE, mapInstance));
		}

	}

	static class BlindingFlashEffect extends CasterGroundEffect {

		static float aoeRadius = 80f;
		static float blindDuration = 60 * 4f;

		protected BlindingFlashEffect(Unit caster, float x, float y) {
			super(caster, x, y);
		}

		@Override
		public void tick() {
			for (Unit unit : mapInstance.getUnits()) {
				if (Point.dist(ground, unit.getPos()) <= aoeRadius + unit.getRadius()) {
					if (unit.getSide() != caster.getSide()) {
						unit.status.applyBlind(blindDuration);
					} else {
						unit.status.applyBlind(blindDuration / 2);
					}
				}
			}
			markAsComplete();
			mapInstance.addEffect(new Effects.CircleOutlineVfx(ground.x, ground.y, aoeRadius, Color.YELLOW, mapInstance));
		}

	}

}
