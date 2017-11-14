package com.mygdx.holowyth.map;

import java.io.Serializable;
import java.util.ArrayList;

import com.mygdx.holowyth.exception.ErrorCode;
import com.mygdx.holowyth.exception.HoloException;
import com.mygdx.holowyth.polygon.Polygon;

public class Field implements Serializable {


	private static final long serialVersionUID = 1L;
	
	public String name; //blank string okay, null is not okay.
	private int width, height;
	public final ArrayList<Polygon> polys = new ArrayList<Polygon>();
	
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
