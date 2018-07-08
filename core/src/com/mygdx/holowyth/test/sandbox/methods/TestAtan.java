package com.mygdx.holowyth.test.sandbox.methods;

public class TestAtan {
	
	
	
	static float eps = 0.00000000000000001f;
	
	public static void main(String[] args) {
		System.out.println(Math.atan2(0, 3));
		System.out.println(Math.atan2(3, 0));
		System.out.println(Math.atan2(eps, 0));
		System.out.println(Math.atan2(0, eps));
		System.out.println(Math.atan2(3, eps));
		System.out.println(Math.atan2(eps, 3));
		
		//Outputs fine. Looks good. Atan doesn't choke on very small numbers or zero.
	}
}
