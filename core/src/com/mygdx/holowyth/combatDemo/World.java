package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.combatDemo.Unit.Side;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.CBInfo;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.data.Segment;


/**
 * Has map lifetime
 * @author Colin Ta
 *
 */

public class World implements WorldInfo {
	
	PathingModule pathingModule;
	Field map; // Each world instance is tied to a single map (specifically, is loaded from a single map)
	
	public World(Field map, PathingModule pathingModule){
		this.map = map;
		this.pathingModule = pathingModule;
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
			u.handleGeneralLogic();
		}
	}

	private void handleCombatLogic() {
		for (Unit u : units) {
			u.handleCombatLogic();
		}
	}
	
	/**
	 * Moves units according to their velocity. Rejects any illegal movements based on collision detection.
	 */
	private void moveUnits() {
		for (Unit u : units) {

			// Validate the motion by checking against other colliding bodies.

			float dx = u.x + u.vx;
			float dy = u.y + u.vy;

			if (u.vx == 0 && u.vy == 0) {
				continue;
			}

			Segment motion = new Segment(u.x, u.y, dx, dy);

			ArrayList<CBInfo> colBodies = new ArrayList<CBInfo>();
			for (Unit a : units) {
				if (u.equals(a)) { // don't consider the unit's own collision body
					continue;
				}

				CBInfo c = new CBInfo();
				c.x = a.x;
				c.y = a.y;
				c.unitRadius = a.getRadius();
				colBodies.add(c);
			}
			ArrayList<CBInfo> collisions = HoloPF.getUnitCollisions(motion.x1, motion.y1, motion.x2, motion.y2,
					colBodies, u.getRadius());
			if (collisions.isEmpty()) {
				u.x += u.vx;
				u.y += u.vy;
			} else {

				// if line intersects with one or more other units. (should be max two, since units should not be
				// overlapped)
				if (collisions.size() > 2) {
					System.out.println("Wierd case, unit colliding with more than 2 units");
				}

				float curDestx = u.x + u.vx;
				float curDesty = u.y + u.vy;
				// Vector2 curVel = new Vector2(u.vx, u.vy);

				// System.out.format("%s is colliding with %s bodies%n", u, collisions.size());

				for (CBInfo cb : collisions) {
					Vector2 dist = new Vector2(curDestx - cb.x, curDesty - cb.y);

					// At first, dist is smaller than the combined radius, but previous push outs might have changed
					// this.
					if (dist.len() > cb.unitRadius + u.getRadius()) {
						continue;
					}

					// expand
					Vector2 pushedOut = new Vector2(dist)
							.setLength(cb.unitRadius + u.getRadius() + Holo.collisionClearanceDistance);
					// TODO: handle edge case here dist is 0

					curDestx = cb.x + pushedOut.x;
					curDesty = cb.y + pushedOut.y;
				}

				// Take this motion if it's valid, otherwise don't move unit.
				if (HoloPF.isEdgePathable(u.x, u.y, curDestx, curDesty, pathingModule.getExpandedMapPolys(), colBodies,
						u.getRadius())) {
					u.x = curDestx;
					u.y = curDesty;
				}

			}

		}

	}

	void spawnSomeEnemyUnits() {
		spawnUnit(406, 253, Unit.Side.ENEMY);
		spawnUnit(550, 122 - Holo.UNIT_RADIUS, Unit.Side.ENEMY);
		spawnUnit(750, 450, Unit.Side.ENEMY);
	}
	
	public Unit spawnUnit(float x, float y, Side side) {
		Unit newUnit = new Unit(x, y, this, side);
		units.add(newUnit);
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
	public Field getMap() {
		return map;
	}

}
