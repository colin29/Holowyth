package com.mygdx.holowyth.util;

import com.mygdx.holowyth.util.exceptions.HoloAssertException;

public class HoloAssert {
	public static void assertEquals(int v1, int v2) {
		if (v1 != v2)
			throw new HoloAssertException("Expected v1 and v2 to be equal, actual values: " + v1 + " " + v2);
	}

	public static void assertEquals(float v1, float v2) {
		if (v1 != v2)
			throw new HoloAssertException("Expected v1 and v2 to be equal, actual values: " + v1 + " " + v2);
	}

	public static void assertEquals(Object o1, Object o2) {
		if (o1 != o2)
			throw new HoloAssertException("Expected objects o1 and o2 to be equal, actual values: " + o1 + " " + o2);
	}

	public static void assertIsNull(Object o) {
		if (o != null)
			throw new HoloAssertException("Expected object to be null");
	}
}
