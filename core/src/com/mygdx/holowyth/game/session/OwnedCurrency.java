package com.mygdx.holowyth.game.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwnedCurrency {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private int sp;  // silver pieces
		
	public boolean canAfford(int sp) {
		return this.sp >= sp;
	}
	public void add(int spAmount) {
		logger.debug("Added {} sp, new balance: {} sp", spAmount, sp + spAmount);
		this.sp += spAmount;
	}
	public void subtract(int spAmount) {
		logger.debug("Subtracted {} sp, new balance: {} sp", spAmount, sp-spAmount);
		sp -= spAmount;
	}
	public int getBalance() {
		return sp;
	}
}
