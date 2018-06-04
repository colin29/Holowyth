package com.mygdx.holowyth.test.sandbox.structure;

import com.mygdx.holowyth.units.Unit;
import com.mygdx.holowyth.units.UnitInfo;

public class AccessTest {
	
	public void foo(UnitHandler i){
		
		
		//Unit is public here, but we have the choice to instead use a more limited interface
		
		UnitInfo u = i.getUnitInfo();
		
		u.getAgi();
		
	}
}
