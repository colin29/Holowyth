package com.mygdx.holowyth.unit;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.holowyth.unit.UnitOrders.Order;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitStatusInfo;
import com.mygdx.holowyth.unit.statuseffect.BasicAttackSlowEffect;
import com.mygdx.holowyth.unit.statuseffect.BleedEffect;
import com.mygdx.holowyth.unit.statuseffect.SlowEffect;
import com.mygdx.holowyth.unit.statuseffect.SpeedIncreaseEffect;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Mantains a unit's status effects (e.g. slow/stun)
 * Unit-Lifetime component
 *
 */
public class UnitStatus implements UnitStatusInfo {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final float REEL_SLOW_AMOUNT = 0.4f;
	
	static float defaultStunProration = 60; // means a duration of x gets prorated around a max of x+60 frames
	static float defaultReelProration = 120;
	
	private final Unit self;
	
	// Status effects
	private final List<SlowEffect> slowEffects = new LinkedList<SlowEffect>();
	private final List<SpeedIncreaseEffect> speedIncreaseEffects = new LinkedList<SpeedIncreaseEffect>();
	private final List<BleedEffect> bleedEffects = new LinkedList<BleedEffect>();
	
	private float blindDurationRemaining;

	private float tauntDurationRemaining = 0;
	private Unit tauntedTowards = null;

	private UnitStun stun; 
	
	public UnitStatus(Unit self) {
		this.self = self;
		stun = new UnitStun(self);
	}
	
	void reinitializeforMapInstance() {
		stun = new UnitStun(self);
	}
	
	void clearMapLifetimeData() {
		stun = null;
		
		slowEffects.clear();
		blindDurationRemaining = 0;

		tauntDurationRemaining = 0;
		tauntedTowards = null;
	}
	
	

	public void applyBleed(float damagePerTick, int totalTicks, int totalFrames) {
		bleedEffects.add(new BleedEffect(self, damagePerTick, totalTicks, totalFrames));
	}
	
	/**
	 * @param slowAmount
	 *            from 0 to 1, 1 being a total slow
	 * @param duration
	 *            in frames
	 */
	public void applySlow(float slowAmount, int duration) {
		slowEffects.add(new SlowEffect(duration, slowAmount));
	}
	
	/**
	 * @param speedIncreaseAmount
	 *            A non-negative number. 0.3f represents a 30% speed increase.
	 * @param duration
	 *            in frames
	 */
	public void applySpeedIncrease(float speedIncreaseAmount, float duration) {
		speedIncreaseEffects.add(new SpeedIncreaseEffect(duration, speedIncreaseAmount));
	}

	/**
	 * Same as applySlow but uses a marker sub-class so that certain maneuvers can nullify this slow.
	 */
	public void applyBasicAttackSlow(float slowAmount, int duration) {
		if (self.getOrder() != Order.RETREAT) { // units are unaffected by basic attack slow while retreating
			slowEffects.add(new BasicAttackSlowEffect(duration, slowAmount));
		}
	}

	public void applyBlind(float duration) {

		if (duration < 0) {
			throw new HoloIllegalArgumentsException("duration must be non-negative");
		}
		if (!isBlinded()) {
			self.interruptRangedSkills();
			blindDurationRemaining = duration;
		} else {
			blindDurationRemaining += duration;
		}

	}
	public void applyTaunt(int duration, Unit tauntSource) {
		if (tauntSource == null) {
			logger.warn("tauntSource is null");
			return;
		}
		tauntDurationRemaining = duration;
		tauntedTowards = tauntSource;
	}

	public void removeAllBasicAttackSlows() {
		slowEffects.removeIf((effect) -> effect instanceof BasicAttackSlowEffect);
	}

	public void applyStun(float duration) {
		stun.applyStun(duration, duration + 60); // thus a 0.5 sec stun will prorate around a 1.5sec max, 3sec -> 4 sec max
	}

	public void applyStun(float duration, float maxStunDuration) {
		stun.applyStun(duration, maxStunDuration);
	}

	/**
	 * Unlike doKnockbackRoll, this method has no concept of force/stability
	 * 
	 * @param dv
	 */
	public void applyKnockbackStun(float duration, float maxStunDuration, Vector2 dv) {
		applyKnockbackStun(duration, maxStunDuration, dv, dv.len() + 1);
	}

	/**
	 * 
	 * @param duration
	 * @param maxStunDuration
	 *            stun contribution by this effect is prorated around this value
	 * @param dv
	 * @param maxKnockbackVel
	 *            velocity contribution by this effect is prorated around this value
	 */
	public void applyKnockbackStun(float duration, float maxStunDuration, Vector2 dv, float maxKnockbackVel) {
		stun.applyKnockbackStun(duration, dv, maxKnockbackVel, maxStunDuration);
	}

	/**
	 * Apply a knockback stun without adding any minimum stun time
	 * 
	 * @param dv
	 */
	public void applyKnockbackStunWithoutVelProrate(Vector2 dv) {
		applyKnockbackStunWithoutVelProrate(0, dv);
	}

	/**
	 * Use default stun proration
	 */
	public void applyKnockbackStunWithoutVelProrate(float duration, Vector2 dv) {
		applyKnockbackStunWithoutVelProrate(duration, duration + defaultStunProration, dv);
	}

	public void applyKnockbackStunWithoutVelProrate(float duration, float maxStunDuration, Vector2 dv) {
		stun.applyKnockbackStunWithoutVelProrate(duration, maxStunDuration, dv);
	}

	public void applyReel(float duration) {
		applyReel(duration, duration + defaultReelProration);
	}

	public void applyReel(float duration, float maxReelDuration) {
		stun.applyReel(duration, maxReelDuration);
	}

	@Override
	public boolean isSlowed() {
		return !slowEffects.isEmpty();
	}

	@Override
	public boolean isSlowedIgnoringBasicAttackSlow() {
		for (var slowEffect : slowEffects) {
			if (!(slowEffect instanceof BasicAttackSlowEffect))
				return true;
		}
		return false;
	}

	@Override
	public boolean isBlinded() {
		return blindDurationRemaining > 0;
	}

	@Override
	public boolean isTaunted() {
		return tauntDurationRemaining > 0;
	}

	@Override
	public UnitInfo getTauntAttackTarget() {
		return tauntedTowards;
	}

	@Override
	public boolean isStunned() {
		return stun.isStunned();
	}

	@Override
	public float getStunDurationRemaining() {
		return stun.getStunDurationRemaining();
	}

	@Override
	public boolean isReeled() {
		return stun.isReeled();
	}

	@Override
	public float getReeledDurationRemaining() {
		return stun.getReelDurationRemaining();
	}

	@Override
	public float getMoveSpeed() {
	
		float reelingMoveSlow = isReeled() ? REEL_SLOW_AMOUNT : 0;
		return self.stats.getBaseMoveSpeed() * ((1 -  getLargestSlow()) * (1 - reelingMoveSlow) + getLargestSpeedIncrease());
	}
	private float getLargestSlow() {
		float largestSlow = 0;
		for (var effect : slowEffects) {
			largestSlow = Math.max(largestSlow, effect.getSlowAmount());
		}
		return largestSlow;
	}

	private float getLargestSpeedIncrease() {
		float largestSpeedIncrease = 0;
		for (var effect : speedIncreaseEffects) {
			largestSpeedIncrease = Math.max(largestSpeedIncrease, effect.getSpeedIncrease());
		}
		return largestSpeedIncrease;
	}
	
	/**
	 * @returns movespeed / baseMoveSpeed. For example if a unit was slowed by 30%, it would return 0.7
	 */
	@Override
	public float getMoveSpeedRatio() {
		float baseMoveSpeed = self.stats.getBaseMoveSpeed();
		if (baseMoveSpeed == 0) {
			return 0;
		}
	
		return getMoveSpeed() / baseMoveSpeed;
	}

	public void tick() {
		slowEffects.forEach((effect) -> effect.tickDuration());
		slowEffects.removeIf((effect) -> effect.isExpired());
		speedIncreaseEffects.forEach((effect) -> effect.tickDuration());
		speedIncreaseEffects.removeIf((effect) -> effect.isExpired());
		bleedEffects.forEach((effect) -> effect.tick());
		bleedEffects.removeIf((effect) -> effect.isExpired());
	
		blindDurationRemaining = Math.max(0, blindDurationRemaining - 1);
	
		boolean wasTaunted = isTaunted();
		tauntDurationRemaining = Math.max(0, tauntDurationRemaining - 1);
		if (tauntDurationRemaining == 0) {
			tauntedTowards = null;
			if (wasTaunted)
				onTauntEnd();
		}
		
		stun.tick();
	}
	
	private void onTauntEnd() {
		self.clearOrder();
	}



}
