package com.mygdx.holowyth.polygon;

public class Polygon implements java.io.Serializable {
	
	private static final long serialVersionUID = 1;
	public int count;
	public float[] vertexes;
	
	public Polygon(float[] vertexes, int count){
		this.count = count;
		this.vertexes = vertexes.clone();
	}	
}