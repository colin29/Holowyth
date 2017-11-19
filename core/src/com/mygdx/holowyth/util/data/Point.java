package com.mygdx.holowyth.util.data;

public class Point {
	public float x, y;
	public Point(float x, float y){
		this.x = x;
		this.y = y;
	}

	public Point (Point p) {
		this.x = p.x;
		this.y = p.y;
	}
}
