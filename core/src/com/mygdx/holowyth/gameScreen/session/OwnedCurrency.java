package com.mygdx.holowyth.gameScreen.session;



public class OwnedCurrency {
	
	private int sp;  // silver pieces
		
	public boolean canAfford(int sp) {
		return this.sp >= sp;
	}
	public void add(int sp) {
		this.sp += sp;
	}
	public void subtract(int sp) {
		this.sp -= sp;
	}
}
