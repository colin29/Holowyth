package com.mygdx.holowyth.pathfinding;

/**
 * Interface of the implementation neccesary to use as a unit for the pathfinding module
 * @author Colin Ta
 *
 */
public interface UnitInter {
	public float getRadius();
	public float getX();
	public float getY();
	public Path getPath();
}
