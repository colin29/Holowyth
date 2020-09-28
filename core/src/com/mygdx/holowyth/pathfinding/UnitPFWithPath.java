package com.mygdx.holowyth.pathfinding;

/**
 * Same as {@link UnitPF}, but also provides info about a unit's path. Only is needed for rendering paths.
 * 
 * @author Colin Ta
 *
 */
public interface UnitPFWithPath extends UnitPF{
	public float getRadius();

	public float getX();

	public float getY();

	public Path getPath();
}
