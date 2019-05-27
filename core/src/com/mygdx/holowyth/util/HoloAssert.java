package com.mygdx.holowyth.util;

import com.mygdx.holowyth.util.exceptions.HoloAssertException;
import com.mygdx.holowyth.util.exceptions.HoloException;

public class HoloAssert extends HoloException {
	public static void assertEquals(int v1, int v2) {
		if (v1 != v2)
			throw new HoloAssertException("Expected v1 and v2 to be equal, actual values: " + v1 + " " + v2);
	}
}
