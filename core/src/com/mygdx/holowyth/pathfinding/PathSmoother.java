package com.mygdx.holowyth.pathfinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.data.Point;

public class PathSmoother {
	
	public PathSmoother(){
	}
	
	float SEGMENT_LENGTH = 5;
	public Path smoothPath(Path origPath, ArrayList<Polygon> polys){

		// Path is at least length 3
		if(origPath.size() <= 1){
			return origPath;
		}

		
		Path path = new Path();
		for(Point p : origPath) {
		    path.add(new Point(p));
		}
		
		
		
		//create a more sectioned version of the path.
		ListIterator<Point> iter = path.listIterator();
		Point prev, cur, next;
		
		prev = iter.next();
		cur = iter.next();
		
		while(iter.hasNext()){
			next = iter.next();
			
			if(HoloPF.isEdgePathable(prev.x, prev.y, next.x, next.y, polys)){
				//	Can connect prev directly to next, so can remove cur from list.
				
//				System.out.println("Size of array: " + path.size());
//				System.out.println(iter.nextIndex());
				iter.previous();
				iter.previous();
//				System.out.println(iter.nextIndex());
//				System.out.println(iter.previousIndex());
				iter.remove();
//				System.out.println("Size of array: " + path.size());
//				System.out.println(iter.nextIndex());
				iter.next();
				cur = next;
			}else{
				prev = cur;
				cur = next;
			}
		}
		
//		Path pathFinal = new Path();
//		for(Point p : path) {
//			pathFinal.add(new Point(p));
//		}
		
		return path;
	}

}
