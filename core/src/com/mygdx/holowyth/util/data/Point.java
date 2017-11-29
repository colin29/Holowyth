package com.mygdx.holowyth.util.data;

public class Point {
	public float x, y;
	public Point(){
	}
	public Point(float x, float y){
		this.x = x;
		this.y = y;
	}

	public Point (Point p) {
		this.x = p.x;
		this.y = p.y;
	}
	
	public void set(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public static float calcDistance(Point p1, Point p2){
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		return (float) Math.sqrt(dx*dx + dy*dy);
	}
}
