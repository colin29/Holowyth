package com.mygdx.holowyth.map.simplemap;

import java.io.Serializable;

import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * The old map class. The practical counterpart is a TMX map
 * @author Colin
 *
 */
public class SimpleMap implements Serializable {


	private static final long serialVersionUID = 2L;
	
	/**
	 * Can be blank but not null
	 */
	public String name;
	private int width, height;
	public final Polygons polys = new Polygons();
	
	public transient boolean hasUnsavedChanges;

	public SimpleMap() {
		this(2000,1200);
	}

	public SimpleMap(int width, int height) {
		
		this.setDimensions(width, height);
		
		this.name = "";
		this.width = width;
		this.height = height;
	}
	
	public void setDimensions(int width, int height) {
		if(width <= 0 ||  height <= 0){
			throw new HoloIllegalArgumentsException("width and height must be positive, got: " + width + " " + height);
		}
		this.width = width;
		this.height = height;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

}
