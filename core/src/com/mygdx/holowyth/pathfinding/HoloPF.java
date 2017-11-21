package com.mygdx.holowyth.pathfinding;

import java.awt.geom.Line2D;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.util.data.Point;

import de.lighti.clipper.Point.LongPoint;
import de.lighti.clipper.Clipper.EndType;
import de.lighti.clipper.Clipper.JoinType;
import de.lighti.clipper.ClipperOffset;

/**
 * Contains helper functions related to pathfinding and geometry
 *
 */
public class HoloPF {

	public static boolean isEdgePathable(float x, float y, float x2, float y2, Polygons polys) {
		boolean intersects = false;
		for (Polygon polygon : polys) {
			for (int i = 0; i <= polygon.count - 2; i += 2) { // for each polygon edge
				if (Line2D.linesIntersect(x, y, x2, y2, polygon.floats[i], polygon.floats[i + 1],
						polygon.floats[(i + 2) % polygon.count], polygon.floats[(i + 3) % polygon.count])) {
					intersects = true;
				}
			}
		}
		return !intersects;
	}
	
	public static void renderPath(Path path, Color color, boolean renderPoints, float thickness,  ShapeRenderer shapeRenderer) {
		if (path != null) {
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(color);
			for (int i = 0; i < path.size() - 1; i++) {

				if (i == 1) {
					shapeRenderer.setColor(color);
				}

				if (i == path.size() - 2) {
					shapeRenderer.setColor(color);
				}
				Point v = path.get(i);
				Point next = path.get(i + 1);
				
				shapeRenderer.rectLine(v.x, v.y, next.x, next.y, thickness);

			}
			shapeRenderer.end();

			// Draw points
			float pointSize = 4f;
			shapeRenderer.setColor(Color.GREEN);
			shapeRenderer.begin(ShapeType.Filled);
			if (renderPoints) {
				for (Point p : path) {

					shapeRenderer.circle(p.x, p.y, pointSize);

				}
			}
			shapeRenderer.end();
			shapeRenderer.setColor(Color.BLACK);
			shapeRenderer.begin(ShapeType.Line);
			if (renderPoints) {
				for (Point p : path) {

					shapeRenderer.circle(p.x, p.y, pointSize);

				}
			}
			shapeRenderer.end();

		}
	}
	
	
	/**
	 * @return returns a new set of polygons which are expanded 
	 */
	public static Polygons expandPolygons(Polygons origPolys, float delta){
		

		Polygons polys = new Polygons();
		
		de.lighti.clipper.Paths solution = new de.lighti.clipper.Paths();
		
		de.lighti.clipper.Path solPath = new de.lighti.clipper.Path();
		

		for(Polygon poly: origPolys){
			ClipperOffset co = new ClipperOffset(1.4,0.25f);

			de.lighti.clipper.Path path = new de.lighti.clipper.Path();
			//load the polygon data into the path
			for(int i=0; i<poly.count; i+=2){
				path.add(new LongPoint((long) poly.floats[i], (long) poly.floats[i+1]));
			}
			co.addPath(path, JoinType.MITER, EndType.CLOSED_POLYGON);
			co.execute(solution, delta);
			
			solPath = solution.get(0);
//			Polygon newPoly = new Polygon();
//			vertexes
			
			//Reload the result data back into a polygon
			float[] polyData = new float[solPath.size()*2];
			
			for(int i=0; i<solPath.size(); i++){
				polyData[2*i] = solPath.get(i).getX();
				polyData[2*i+1] = solPath.get(i).getY();
			}
			
			polys.add(new Polygon(polyData, solPath.size()*2));
		}
		
		return polys;
		
		
	}
}
