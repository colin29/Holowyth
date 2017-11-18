package com.mygdx.holowyth.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.exception.ErrorCode;
import com.mygdx.holowyth.util.exception.HoloException;

public class HoloIO {

	public static Field getMapFromDisk(String pathname) {
		Field map;
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		try {
			fileIn = new FileInputStream(pathname);
			in = new ObjectInputStream(fileIn);
			map = (Field) in.readObject();
			return map;

		} catch (IOException i) {
			i.printStackTrace();
			throw new HoloException(ErrorCode.IO_EXCEPTION);
		} catch (ClassNotFoundException c) {
			System.out.println("Field class not found");
			c.printStackTrace();
			throw new HoloException(ErrorCode.IO_EXCEPTION);
		} finally{
			try {
				in.close();
				fileIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void saveMapToDisk(String pathname, Field map) {
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try {

			fout = new FileOutputStream(pathname);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(map);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					fout.close();
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		map.hasUnsavedChanges = false;
		System.out.println("Writing Map to disk finished");
	}

	/**
	 * Make sure to set the renderer's matrixes before calling these and similar methods. 
	 */
	public static void renderMapPolygons(Field map, ShapeRenderer shapeRenderer){
		shapeRenderer.begin(ShapeType.Line);
		for (Polygon p : map.polys) {
			shapeRenderer.polygon(p.vertexes, 0, p.count);
		}
		shapeRenderer.end();
	}
	
	public static void renderMapBoundaries(Field map, ShapeRenderer shapeRenderer){
		shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
		shapeRenderer.begin(ShapeType.Line);

		Vector2 topRight = new Vector2(map.width(), map.height());
		Vector2 topLeft = new Vector2(0, map.height());
		Vector2 botRight = new Vector2(map.width(), 0);
		Vector2 botLeft = new Vector2(0, 0);
		shapeRenderer.line(topLeft, topRight);
		shapeRenderer.line(botLeft, botRight);
		shapeRenderer.line(topLeft, botLeft);
		shapeRenderer.line(topRight, botRight);

		shapeRenderer.end();
	}
}
