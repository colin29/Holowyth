package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.collision.CircleCBInfo;
import com.mygdx.holowyth.collision.CollisionDetection;
import com.mygdx.holowyth.collision.CollisionInfo;
import com.mygdx.holowyth.collision.ObstaclePoint;
import com.mygdx.holowyth.collision.ObstacleSeg;
import com.mygdx.holowyth.collision.UnitAdapterCircleCB;
import com.mygdx.holowyth.collision.wallcollisiondemo.OrientedPoly;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.CBInfo;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.unit.PresetUnits;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloAssert;
import com.mygdx.holowyth.util.dataobjects.Segment;
import com.mygdx.holowyth.util.exceptions.HoloOperationException;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

/**
 * Has map lifetime
 * 
 * @author Colin Ta
 *
 */

public class World implements WorldInfo {

	PathingModule pathingModule;
	Field map; // Each world instance is tied to a single map (specifically, is
				// loaded from a single map)

	EffectsHandler gfx;
	DebugStore debugStore;

	private UnitCollection units = new UnitCollection();

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private float knockBackCollisionElasticityDefault = 0.06f;
	private float knockBackCollisionElasticityReboundsOffUnit = 0.33f;
	private float knockBackUnitFriction = 0.015f; // 0.01-0.02 is a realistic number
	private float velocityThresholdToEndKnockback = 0.01f; // 0.03f;

	private List<Effect> effects = new ArrayList<Effect>();

	public World(Field map, PathingModule pathingModule, DebugStore debugStore, EffectsHandler effects) {
		this.map = map;
		this.pathingModule = pathingModule;
		this.gfx = effects;

		this.debugStore = debugStore;

		@SuppressWarnings("unused")
		DebugValues debugValues = debugStore.registerComponent("World");
	}

	/**
	 * Evaluates one frame of the game world
	 */
	public void tick() {

		tickLogicForUnits();
		moveNormallyMovingUnits();
		moveKnockedBackedUnitsAndResolveCollisions();
		tickAttacking();

		tickEffects();
	}

	public void addEffect(Effect e) {
		effects.add(e);
	}

	private void tickEffects() {
		for (Effect e : effects) {
			e.tick();
		}
		effects.removeIf((Effect e) -> e.isComplete());
	}

	/**
	 * Moves units according to their velocity. Rejects any illegal or conflicting movements based on collision detection.
	 */
	private void moveNormallyMovingUnits() {

		/**
		 * For this processing, each unit on its iteration should only modify its x,y and no others. vx,vy should not be modified.
		 */
		for (Unit thisUnit : units.getUnits()) {

			// Validate the motion by checking against other colliding bodies.

			Unit debugUnit = Unit.getUnitByID(3);

			if (thisUnit.motion.isBeingKnockedBack()) {
				// knocked back units do not have voluntary motion, skip
				HoloAssert.assertEquals(thisUnit.motion.getVx(), 0);
				HoloAssert.assertEquals(thisUnit.motion.getVy(), 0);
				HoloAssert.assertIsNull(thisUnit.motion.getPath());
				continue;
			}

			if (thisUnit.motion.getVx() == 0 && thisUnit.motion.getVy() == 0) {
				continue;
			}

			// Get all other colliding bodies
			ArrayList<CBInfo> colBodies = new ArrayList<CBInfo>();
			for (Unit u : units.getUnits()) {
				if (!thisUnit.equals(u))
					colBodies.add(new CBInfo(u));
			}

			float destX = thisUnit.x + thisUnit.motion.getVx();
			float destY = thisUnit.y + thisUnit.motion.getVy();

			Segment motion = new Segment(thisUnit.x, thisUnit.y, destX, destY);

			ArrayList<CBInfo> collisions = HoloPF.getUnitCollisions(motion.x1, motion.y1, motion.x2, motion.y2,
					colBodies, thisUnit.getRadius());
			thisUnit.debugNumOfUnitsCollidingWith = collisions.size();
			if (collisions.size() > 0) {
				int x;
				x = 3 + 4;
			}

			if (collisions.isEmpty()) {
				thisUnit.x += thisUnit.motion.getVx();
				thisUnit.y += thisUnit.motion.getVy();
			} else { // Push this unit outwards and try to keep going towards the waypoint

				// The majority of the time a unit should only be colliding with one unit at a time since we are drawing
				// a line segment starting from outside any expanded geometry. So assuming the speed is not fast enough
				// to enter and exit one shape, it should only collide with one
				if (collisions.size() > 2) {
					System.out.println("Wierd case, unit colliding with more than 2 units");
				}

				float curDestx = thisUnit.x + thisUnit.motion.getVx();
				float curDesty = thisUnit.y + thisUnit.motion.getVy();

				// System.out.format("%s is colliding with %s bodies%n", u,
				// collisions.size());

				for (CBInfo cb : collisions) {
					Vector2 dist = new Vector2(curDestx - cb.x, curDesty - cb.y);

					// We do a "push out" for every unit. Subsequent push outs may lead to a suggested location that
					// collides with an earlier unit, if so we reject the alternative location
					if (dist.len() > cb.unitRadius + thisUnit.getRadius()) {
						continue;
					}

					// expand
					Vector2 pushedOut = new Vector2(dist)
							.setLength(cb.unitRadius + thisUnit.getRadius() + getCollisionClearanceDistance(thisUnit));
					// TODO: handle edge case here dist is 0

					curDestx = cb.x + pushedOut.x;
					curDesty = cb.y + pushedOut.y;
				}

				// Take this motion if it's valid, otherwise don't move unit.

				// If the "alternative destination" is no different from the starting location, the unit is still
				// effectively blocked.

				/**
				 * Determines how "impatient" the algorithm will be for signalling blocked for a slow-resolving or non-resolving push situation
				 */
				final float minProgress = 0.20f * thisUnit.motion.getVelocityMagnitude();

				if (HoloPF.isEdgePathable(thisUnit.x, thisUnit.y, curDestx, curDesty,
						pathingModule.getExpandedMapPolys(), colBodies,
						thisUnit.getRadius())
						&& new Segment(thisUnit.x, thisUnit.y, curDestx, curDesty).getLength() > 0.05 * minProgress) {
					// System.out.println("block distance" + new Segment(u.x, u.y, curDestx, curDesty).getLength());
					thisUnit.x = curDestx;
					thisUnit.y = curDesty;
				} else {
					thisUnit.motion.onBlocked(); // notify the unit that it is blocked
				}

			}

		}

	}

	/**
	 * Note: If a knockedback unit collides, only its velocity is changed: it's position doesn't change. Thus, no movement validation needs to be
	 * done.
	 */
	private void moveKnockedBackedUnitsAndResolveCollisions() {
		for (Unit thisUnit : units.getUnits()) {
			if (thisUnit.motion.isBeingKnockedBack()) {

				final float x, y, vx, vy;

				x = thisUnit.getX();
				y = thisUnit.getY();
				vx = thisUnit.motion.getKnockbackVx();
				vy = thisUnit.motion.getKnockbackVy();

				CircleCBInfo curBody = units.unitToColBody().get(thisUnit);

				List<CircleCBInfo> allOtherBodies = new ArrayList<CircleCBInfo>();
				allOtherBodies.addAll(units.getColBodies());
				allOtherBodies.remove(curBody);

				Segment motion = new Segment(x, y, x + vx, y + vy);

				List<CircleCBInfo> objectCollisions = CollisionDetection.getCircleBodyCollisionsAlongLineSegment(motion,
						curBody.getRadius(), allOtherBodies);

				List<OrientedPoly> polys = Polygons.calculateOrientedPolygons(map.polys);
				List<CircleCBInfo> obstaclePoints = new ArrayList<CircleCBInfo>();
				for (var poly : polys) {
					for (var seg : poly.segments) {
						obstaclePoints.add(new ObstaclePoint(seg.x1, seg.y1));
					}
				}

				List<CollisionInfo> obstacleCollisions = new ArrayList<CollisionInfo>();
				obstacleCollisions.addAll(CollisionDetection.getCircleSegCollisionInfos(curBody,
						curBody.getRadius(),
						polys));
				obstacleCollisions.addAll(getCirclePointCollisionInfos(curBody, obstaclePoints));

				for (CircleCBInfo colidee : objectCollisions) {
					logger.debug("Collision between units id [{} {}]", thisUnit.getID(),
							units.colBodyToUnit().get(colidee).getID());
				}

				if (objectCollisions.isEmpty() && obstacleCollisions.isEmpty()) {
					// Move object normally
					thisUnit.x += vx;
					thisUnit.y += vy;
				} else {

					try {
						CollisionInfo collision = CollisionDetection.getFirstCollisionInfo(curBody, objectCollisions, obstacleCollisions,
								null);
						resolveUnitKnockbackCollision(collision);
					} catch (HoloOperationException e) {
						logger.warn(e.getMessage());
						logger.warn("from: " + e.getFormattedStackTrace());
						logger.warn("Skipping resolving this object's collision");
						// Skip resolving this collision
					}
				}
				applyFriction(thisUnit);
				endKnockbackForUnitsBelowVelocityThreshold();

			}
		}
	}

	/**
	 * Finds and calculates collision infos of a circle body vs wall point, while moving along the body's motion
	 */
	private List<CollisionInfo> getCirclePointCollisionInfos(CircleCBInfo curBody, List<CircleCBInfo> obstaclePoints) {

		var collidingPoints = CollisionDetection.getCircleBodyCollisionsAlongLineSegment(curBody, obstaclePoints);

		var infos = new ArrayList<CollisionInfo>();

		Segment motion = new Segment(curBody.getX(), curBody.getY(), curBody.getX() + curBody.getVx(), curBody.getY() + curBody.getVy());
		for (var point : collidingPoints) {
			var info = CollisionDetection.getCircleCircleCollisionInfo(motion, curBody, point, null);
			if (info != null) {
				infos.add(info);
			}
		}
		return infos;
	}

	private void applyFriction(Unit unit) {
		if (unit.motion.isBeingKnockedBack()) {

			Vector2 newVelocity = unit.motion.getKnockbackVelocity();
			newVelocity.setLength(Math.max(0, newVelocity.len() - knockBackUnitFriction));
			unit.motion.setKnockbackVelocity(newVelocity);
		}
	}

	private void endKnockbackForUnitsBelowVelocityThreshold() {
		for (Unit unit : units.getUnits()) {
			if (unit.motion.isBeingKnockedBack()) {
				if (unit.motion.getKnockbackVelocity().len() < velocityThresholdToEndKnockback) {
					unit.motion.endKnockback();
				}
			}
		}
	}

	private enum CollisionType {
		UNIT, OBSTACLE_SEG, OBSTACLE_POINT
	}

	private void resolveUnitKnockbackCollision(CollisionInfo collision) {
		// The collision info takes
		// the opposite's vx/vy is either their normal movement speed, or their knockback
		// at the end, if the target wasn't in knockback state, automatically turn it into knockback state (stub
		// functionality)

		// 1. Determine unit's velocities in terms of the normalized collision normal vector. Set aside the
		// perpendicular components. We now have 2 1-d velocity vectors

		final CircleCBInfo thisBody = collision.cur;
		CircleCBInfo otherBody = null;

		CollisionType collisionType;

		Vector2 v1 = new Vector2(thisBody.getVx(), thisBody.getVy());
		Vector2 v2;

		// Based on type of collision, set the velocity of the second body
		if (collision.other instanceof UnitAdapterCircleCB) {
			otherBody = (CircleCBInfo) collision.other;
			collisionType = CollisionType.UNIT;
			v2 = new Vector2(otherBody.getVx(), otherBody.getVy());
		} else if (collision.other instanceof ObstacleSeg) {
			collisionType = CollisionType.OBSTACLE_SEG; // only thing that needs to be set is mass to VERY_HIGH and velocity to 0
			v2 = new Vector2(0, 0);
		} else if (collision.other instanceof ObstaclePoint) {
			collisionType = CollisionType.OBSTACLE_POINT;
			v2 = new Vector2(0, 0);
		} else {
			throw new RuntimeException("Unsupported Collidable type: " + collision.other.getClass().getName());
		}

		Unit thisUnit = units.colBodyToUnit().get(thisBody);
		Unit otherUnit = collisionType == CollisionType.UNIT ? units.colBodyToUnit().get(otherBody) : null;

		Vector2 normalNorm = new Vector2((float) Math.cos(collision.collisionSurfaceNormalAngle),
				(float) Math.sin(collision.collisionSurfaceNormalAngle));
		Vector2 v1Norm = new Vector2(v1).nor();

		Vector2 v2Norm = new Vector2(v2).nor();

		float v1ColAxis = v1.len() * v1Norm.dot(normalNorm);
		float v2ColAxis = v2.len() * v2Norm.dot(normalNorm);

		float collisionMagnitudeOfUnit1 = Math.abs(v1ColAxis);

		float elasticity;

		final float MASS_BODY = 3; // system supports mass but game doesn't use it yet
		final float VERY_HIGH_MASS = 9999;

		final float m1 = MASS_BODY;
		final float m2;

		boolean unitReboundsOffSecondUnit = false;

		// Based on type of collision, set the elasticity and mass of the bodies (angle is already provided)

		switch (collisionType) {
		case UNIT:
			if (!otherUnit.motion.isBeingKnockedBack() && collisionMagnitudeOfUnit1 < 1) {
				unitReboundsOffSecondUnit = true;
			}
			elasticity = unitReboundsOffSecondUnit ? knockBackCollisionElasticityReboundsOffUnit
					: knockBackCollisionElasticityDefault;

			m2 = unitReboundsOffSecondUnit ? VERY_HIGH_MASS
					: MASS_BODY;
			break;
		case OBSTACLE_SEG: // intentional grouping
		case OBSTACLE_POINT:
			elasticity = 0.25f;
			m2 = VERY_HIGH_MASS;
			break;
		default:
			throw new RuntimeException("Unsupported Collision type: " + collisionType.name());

		}

		// 2. Transform the problem into the zero momentum frame

		float M1ColAxis = v1ColAxis * m1;
		float M2ColAxis = v2ColAxis * m2;

		float MSystemColAxis = M1ColAxis + M2ColAxis;
		float VSystemColAxis = MSystemColAxis / (m1 + m2);

		float v1ZeroFrame = v1ColAxis - VSystemColAxis;
		float v2ZeroFrame = v2ColAxis - VSystemColAxis;

		// 3. Solve the problem with the zero momentum frame
		// 1. Use derived formula to compute v1'

		float v1FinalZeroFrame = (float) Math
				.sqrt(elasticity
						* (m1 * (v1ZeroFrame * v1ZeroFrame) + m2 * (v2ZeroFrame * v2ZeroFrame))
						/ (m1 * (1 + m1 / m2)));

		// 2. Plugin to momentum equation to get v2'

		float v2FinalZeroFrame = -1 * v1FinalZeroFrame * m1 / m2;

		// 3. Subtract from initial velocities to get the change in velocity along the collision normal vector

		float dv1ColAxis = v1FinalZeroFrame - v1ZeroFrame;
		float dv2ColAxis = v2FinalZeroFrame - v2ZeroFrame;

		// 4. Convert change in velocity into standard coordinates and modify both body's velocities by that much,
		// respectively.

		Vector2 dv1 = new Vector2(normalNorm).scl(dv1ColAxis);
		Vector2 dv2 = new Vector2(normalNorm).scl(dv2ColAxis);

		// If collision was a unit
		switch (collisionType) {
		case UNIT:
			if (unitReboundsOffSecondUnit) {
				thisUnit.motion.applyKnockBackVelocity(dv1.x, dv1.y);
			} else {
				thisUnit.motion.applyKnockBackVelocity(dv1.x, dv1.y);
				otherUnit.motion.applyKnockBackVelocity(dv2.x, dv2.y);
			}
			break;
		case OBSTACLE_SEG:
		case OBSTACLE_POINT:
			thisUnit.motion.applyKnockBackVelocity(dv1.x, dv1.y);
			break;
		default:
			throw new RuntimeException("Unsupported Collision type: " + collisionType.name());

		}
	}

	private void tickLogicForUnits() {
		for (Unit u : units.getUnits()) {
			u.tickLogic();
		}
	}

	private void tickAttacking() {
		for (Unit u : units.getUnits()) {
			u.tickAttacking();
		}

	}

	private float getCollisionClearanceDistance(Unit u) {
		return Holo.collisionClearanceDistance * (u.motion.getVelocityMagnitude() / Holo.defaultUnitMoveSpeed);
	}

	public void spawnSomeEnemyUnits() {
		ArrayList<Unit> someUnits = new ArrayList<Unit>();
		someUnits.add(spawnUnit(500, 237, Unit.Side.ENEMY));
		someUnits.add(spawnUnit(450, 300, Unit.Side.ENEMY));
		someUnits.add(spawnUnit(400, 350, Unit.Side.ENEMY));
		someUnits.add(spawnUnit(350, 283, Unit.Side.ENEMY));
		someUnits.add(spawnUnit(402, 259, Unit.Side.ENEMY));
		someUnits.add(spawnUnit(442, 239, Unit.Side.ENEMY));

		for (Unit unit : someUnits) {
			PresetUnits.loadUnitStats2(unit.stats);
			PresetUnits.loadBasicWeapon(unit.stats);
			unit.setName("Goblin");
			unit.stats.prepareUnit();
		}
		return;
	}

	public Unit spawnUnit(float x, float y, Side side) {
		return spawnUnit(x, y, side, "Unnamed Unit");
	}

	public Unit spawnUnit(float x, float y, Side side, String name) {
		Unit u = new Unit(x, y, this, side, name);
		units.addUnit(u);
		return u;
	}

	/**
	 * The list itself is read-only, though elements can be modified
	 */
	@Override
	public List<Unit> getUnits() {
		return units.getUnits();
	}

	@Override
	public PathingModule getPathingModule() {
		return pathingModule;
	}

	@Override
	public EffectsHandler getEffectsHandler() {
		return gfx;
	}

	public Field getMap() {
		return map;
	}

	public DebugStore getDebugStore() {
		return debugStore;
	}

	@Override
	public List<Effect> getEffects() {
		return effects;
	}

}
