package com.mygdx.holowyth.pathfinding;

import com.mygdx.holowyth.util.data.Pair;
import com.mygdx.holowyth.util.data.Point;

public class Unit {

	public static float waypointMinDistance = 0.01f;
	
	public float x, y;
	public float vx, vy;
	float speed = 3; //world units per frame
	
	Path path;
	
	Unit(){
	}
	
	public Unit(float x, float y){
		this();
		this.x = x;
		this.y = y;
	}
	
	public void tickLogic(){
		setMovement();
	}
	
	int waypointIndex;
	
	public void setPath(Path path){
		this.path = path;
		waypointIndex = 0;
	}
	
	private void setMovement(){
		if(path != null){

			Point curWaypoint = path.get(waypointIndex);

			float wx = curWaypoint.x;
			float wy = curWaypoint.y;
			
			float dx = wx-x;
			float dy = wy-y;
			
			float dist = (float) Math.sqrt(dx*dx + dy*dy);
			
			//Check if reached waypoint
			if(dist < Unit.waypointMinDistance){
				waypointIndex +=1;
				//check if completed path
				if(waypointIndex == path.size()){
					path = null;
					waypointIndex = -1;
				}
			}
			
			//Determine unit movement
			if(dist>speed){
				float sin = dy/dist;
				float cos = dx/dist;
				
				this.vx = cos * speed;
				this.vy = sin * speed;
			}else{
				this.vx = dx;
				this.vy = dy;
			}
			
		}else{
			vx=0;
			vy=0;
		}
	}
	public void move(){
		
	}
}
