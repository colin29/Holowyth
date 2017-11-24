package com.mygdx.holowyth.polygon;

public class Polygon implements java.io.Serializable {
	
	private static final long serialVersionUID = 1;
	/**
	 * Number of floats, to get number of vertexes, divide by 2
	 */
	public int count; 
	public float[] floats;
	
	/**
	 * 
	 * @param floats
	 * @param count The number of float points
	 */
	public Polygon(float[] floats, int count){
		this.count = count;
		this.floats = floats.clone();
	}
}