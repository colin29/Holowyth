package com.mygdx.holowyth.util.data;

public class Segment {
	public float x1, y1, x2, y2;
	public Segment(float sx, float sy, float dx,  float dy){
		this.x1 = sx;
		this.y1 = sy;
		this.x2 = dx;
		this.y2 = dy;
	}
	
	public Segment(Point a, Point b){
		this.x1 = a.x;
		this.y1 = a.y;
		this.x2 = b.x;
		this.y2 = b.y;
	}
	
	public float getLength(){
		return (float) Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}
}
