package com.mygdx.holowyth.map.obstacledata;

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
	 * @param count
	 *            The number of float points
	 */
	public Polygon(float[] floats, int count) {
		this.count = count;
		this.floats = floats.clone();
	}

	public Polygon(float[] floats) {
		this.count = floats.length;
		this.floats = floats.clone();
	}

	public int vertexCount() {
		return count / 2;
	}
}