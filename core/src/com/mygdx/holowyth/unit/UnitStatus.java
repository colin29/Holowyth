package com.mygdx.holowyth.unit;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.UnitOrders.Order;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitStatusInfo;
import com.mygdx.holowyth.unit.statuseffect.BasicAttackSlowEffect;
import com.mygdx.holowyth.unit.statuseffect.SlowEffect;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.exceptions.HoloIllegalArgumentsException;

/**
 * Mantains a unit's status effects (e.g. slow/stun)
 * Unit-Lifetime component
 *
 */
public class UnitStatus implements UnitStatusInfo {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final float REEL_SLOW_AMOUNT = 0.4f;
	
	private final Unit self;
	
	// Status effects
	private final List<SlowEffect> slowEffects = new LinkedList<SlowEffect>();
	private float blindDurationRemaining;

	private float tauntDurationRemaining = 0;
	private Unit tauntedTowards = null;
	
	private float baseMoveSpeed = Holo.defaultUnitMoveSpeed;
	

	public UnitStatus(Unit self) {
		this.self = self;
	}
	
	void clearMapLifetimeData() {
		slowEffects.clear();
		blindDurationRemaining = 0;

		tauntDurationRemaining = 0;
		tauntedTowards = null;
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
	public float getMoveSpeed() {
	
		float largestSlow = 0;
		for (var effect : slowEffects) {
			largestSlow = Math.max(largestSlow, effect.getSlowAmount());
		}
	
		float reelingMoveSlow = self.stats.isReeled() ? REEL_SLOW_AMOUNT : 0;
	
		return baseMoveSpeed * (1 - largestSlow) * (1 - reelingMoveSlow);
	}

	/**
	 * @returns movespeed / baseMoveSpeed. For example if a unit was slowed by 30%, it would return 0.7
	 */
	@Override
	public float getMoveSpeedRatio() {
		if (baseMoveSpeed == 0) {
			return 0;
		}
	
		return getMoveSpeed() / baseMoveSpeed;
	}

	public void tick() {
		slowEffects.forEach((effect) -> effect.tickDuration());
		slowEffects.removeIf((effect) -> effect.isExpired());
	
		blindDurationRemaining = Math.max(0, blindDurationRemaining - 1);
	
		boolean wasTaunted = isTaunted();
		tauntDurationRemaining = Math.max(0, tauntDurationRemaining - 1);
		if (tauntDurationRemaining == 0) {
			tauntedTowards = null;
			if (wasTaunted)
				onTauntEnd();
		}
	}
	
	private void onTauntEnd() {
		self.clearOrder();
	}



}
