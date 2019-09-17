package com.mygdx.holowyth.unit.statuseffect;

/**
 * Normal attacks cause a slow which applies even if the attack is blocked (though not if dodged).
 *
 */
public class BasicAttackSlowEffect extends SlowEffect {

	public BasicAttackSlowEffect(int duration, float slowAmount) {
		super(duration, slowAmount);
	}
}
