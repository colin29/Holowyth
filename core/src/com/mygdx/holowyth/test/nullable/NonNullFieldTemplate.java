package com.mygdx.holowyth.test.nullable;

import org.eclipse.jdt.annotation.NonNull;

import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

public class NonNullFieldTemplate {
	
	@NonNull
	private String field;
	public NonNullFieldTemplate() {
		field = "value";
	}
	String getField() {
		return field;
	}
	void setField(String field) {
		if(field==null)
			throw new HoloIllegalArgumentsException("Cannot set {} to null");
		this.field = field;
	}
	
	void getMapItem(@NonNull String param) {
		
	}
	
	public static void main(String[] args) {
		var n = new NonNullFieldTemplate();
		n.getMapItem(null);
		
	}
	
	
}
