package com.mygdx.holowyth.util.data;

public class Segment {
	public float sx, sy, dx, dy;
	public Segment(float sx, float sy, float dx,  float dy){
		this.sx = sx;
		this.sy = sy;
		this.dx = dx;
		this.dy = dy;
	}
	
	public Segment(Point a, Point b){
		this.sx = a.x;
		this.sy = a.y;
		this.dx = b.x;
		this.dy = b.y;
	}
	
	public float getLength(){
		return (float) Math.sqrt((dx-sx)*(dx-sx) + (dy-sy)*(dy-sy));
	}
}
