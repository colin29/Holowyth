package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.combatDemo.effects.EffectsHandler;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.CBInfo;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.unit.PresetUnits;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.data.Segment;
import com.mygdx.holowyth.util.debug.DebugStore;
import com.mygdx.holowyth.util.debug.DebugValues;

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

	EffectsHandler effects;
	DebugStore debugStore;

	public World(Field map, PathingModule pathingModule, DebugStore debugStore, EffectsHandler effects) {
		this.map = map;
		this.pathingModule = pathingModule;
		this.effects = effects;

		this.debugStore = debugStore;

		@SuppressWarnings("unused")
		DebugValues debugValues = debugStore.registerComponent("World");
	}

	/**
	 * Evaluates one frame of the game world
	 */
	public void tick() {
		tickLogicForUnits();
		moveUnits();
		handleCombatLogic();

	}

	ArrayList<Unit> units = new ArrayList<Unit>();

	private void tickLogicForUnits() {
		for (Unit u : units) {
			u.handleLogic();
		}
	}

	private void handleCombatLogic() {
		for (Unit u : units) {
			u.handleCombatLogic();
		}
	}

	/**
	 * Moves units according to their velocity. Rejects any illegal or conflicting movements based on collision
	 * detection.
	 */
	private void moveUnits() {

		Polygons expandedMapPolys = HoloPF.expandPolygons(map.polys, Holo.UNIT_RADIUS);

		for (Unit u : units) {

			ArrayList<CBInfo> colBodies = new ArrayList<CBInfo>();
			for (Unit other : units) {
				if (u.equals(other)) { // don't consider the unit's own collision body
					continue;
				}

				CBInfo c = new CBInfo(other);
				colBodies.add(c);
			}

			if (u.motion.isBeingKnockBacked()) {
				resolveKnockbackMotion(u, expandedMapPolys, colBodies);
				continue;
			}

			// Validate the motion by checking against other colliding bodies.

			float destX = u.x + u.motion.vx;
			float destY = u.y + u.motion.vy;

			if (u.motion.vx == 0 && u.motion.vy == 0) {
				continue;
			}

			Segment motion = new Segment(u.x, u.y, destX, destY);

			ArrayList<CBInfo> collisions = HoloPF.getUnitCollisions(motion.x1, motion.y1, motion.x2, motion.y2,
					colBodies, u.getRadius());
			if (collisions.isEmpty()) {
				u.x += u.motion.vx;
				u.y += u.motion.vy;
			} else { // Push this unit outwards and try to keep going towards the waypoint

				// The majority of the time a unit should only be colliding with one unit at a time since we are drawing
				// a line segment starting from outside any expanded geometry. So assuming the speed is not fast enough
				// to enter and exit one shape, it should only collide with one
				if (collisions.size() > 2) {
					System.out.println("Wierd case, unit colliding with more than 2 units");
				}

				float curDestx = u.x + u.motion.vx;
				float curDesty = u.y + u.motion.vy;

				// System.out.format("%s is colliding with %s bodies%n", u,
				// collisions.size());

				for (CBInfo cb : collisions) {
					Vector2 dist = new Vector2(curDestx - cb.x, curDesty - cb.y);

					// We do a "push out" for every unit. Subsequent push outs may lead to a suggested location that is
					// collides with an earlier unit, the alternative location is simply rejected
					if (dist.len() > cb.unitRadius + u.getRadius()) {
						continue;
					}

					// expand
					Vector2 pushedOut = new Vector2(dist)
							.setLength(cb.unitRadius + u.getRadius() + getCollisionClearanceDistance(u));
					// TODO: handle edge case here dist is 0

					curDestx = cb.x + pushedOut.x;
					curDesty = cb.y + pushedOut.y;
				}

				// Take this motion if it's valid, otherwise don't move unit.

				// If the "alternative destination" is no different from the starting location, the unit is still
				// effectively blocked.

				/**
				 * Determines how "impatient" the algorithm will be for signalling blocked for a slow-resolving or
				 * non-resolving push situation
				 */
				final float minProgress = 0.20f * u.motion.getVelocity();

				if (HoloPF.isEdgePathable(u.x, u.y, curDestx, curDesty, pathingModule.getExpandedMapPolys(), colBodies,
						u.getRadius()) && new Segment(u.x, u.y, curDestx, curDesty).getLength() > 0.05 * minProgress) {
					// System.out.println("block distance" + new Segment(u.x, u.y, curDestx, curDesty).getLength());
					u.x = curDestx;
					u.y = curDesty;
				} else {
					u.motion.onBlocked(); // notify the unit that it is blocked
				}

			}

		}

	}

	/**
	 * Resolves motion for a single unit that is being knocked back
	 * 
	 * @param u
	 * @param expandedMapPolys
	 * @param colBodies
	 */
	private void resolveKnockbackMotion(Unit u, Polygons expandedMapPolys, ArrayList<CBInfo> colBodies) {

		// Unit should bounce off walls

		// Find the first target the unit hits. Basically do a raycast.

	}

	private float getCollisionClearanceDistance(Unit u) {
		return Holo.collisionClearanceDistance * (u.motion.getVelocity() / Holo.defaultUnitMoveSpeed);
	}

	void spawnSomeEnemyUnits() {
		ArrayList<Unit> someUnits = new ArrayList<Unit>();
		someUnits.add(spawnUnit(480, 253, Unit.Side.ENEMY));
		someUnits.add(spawnUnit(450, 300, Unit.Side.ENEMY));
		someUnits.add(spawnUnit(400, 350, Unit.Side.ENEMY));

		for (Unit unit : someUnits) {
			PresetUnits.loadUnitStats2(unit.stats);
			PresetUnits.loadBasicWeapon(unit.stats);
			unit.setName("Goblin");
			unit.stats.prepareUnit();
		}
	}

	Unit playerUnit;

	/**
	 * Create a custom player unit. Calling multiple times will just return the existing player unit.
	 * 
	 * @return
	 */
	public Unit createPlayerUnit() {
		if (playerUnit == null) {
			playerUnit = spawnUnit(320, 220, Unit.Side.PLAYER, "Elvin");
			PresetUnits.loadUnitStats(playerUnit.stats);
			PresetUnits.loadSomeEquipment(playerUnit.stats);
			PresetUnits.loadArmor(playerUnit.stats);
			playerUnit.stats.prepareUnit();
			playerUnit.stats.printInfo();

			playerUnit.motion.setSpeedAndRelatedVars(Holo.defaultUnitMoveSpeed * 5);

			return playerUnit;
		} else {
			return playerUnit;
		}
	}

	public Unit spawnUnit(float x, float y, Side side) {
		Unit newUnit = new Unit(x, y, this, side);
		units.add(newUnit);
		return newUnit;
	}

	public Unit spawnUnit(float x, float y, Side side, String name) {
		Unit newUnit = spawnUnit(x, y, side);
		newUnit.setName(name);
		return newUnit;
	}

	@Override
	public ArrayList<Unit> getUnits() {
		return units;
	}

	@Override
	public PathingModule getPathingModule() {
		return pathingModule;
	}

	@Override
	public EffectsHandler getEffectsHandler() {
		return effects;
	}

	public Field getMap() {
		return map;
	}

	public DebugStore getDebugStore() {
		return debugStore;
	}

}
