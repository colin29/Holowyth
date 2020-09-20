package com.mygdx.holowyth.map;

import java.io.Serializable;

import com.mygdx.holowyth.polygon.Polygons;
import com.mygdx.holowyth.util.exception.ErrorCode;
import com.mygdx.holowyth.util.exception.HoloException;

/**
 * The old map class. The practical counterpart is a TMX map
 * @author Colin
 *
 */
public class Field implements Serializable {


	private static final long serialVersionUID = 2L;
	
	/**
	 * Can be blank but not null
	 */
	public String name;
	private int width, height;
	public final Polygons polys = new Polygons();
	
	public transient boolean hasUnsavedChanges;

	public Field() throws HoloException {
		this(2000,1200);
	}

	public Field(int width, int height) throws HoloException {
		
		this.setDimensions(width, height);
		
		this.name = "";
		this.width = width;
		this.height = height;
		
		System.out.println("New map created");
	}
	
	public void setDimensions(int width, int height) throws HoloException{
		if(width <= 0 ||  height <= 0){
			throw new HoloException(ErrorCode.INVALID_DIMENSIONS);
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
