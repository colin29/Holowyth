package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.game.MapInstance;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.util.dataobjects.Point;

public class Projectile {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public final static float DEFAULT_MAX_DURATION = 100f;
	
	protected final @NonNull Point pos;
	private float duration;
	protected final @NonNull Side side;


	protected ProjectileMotion motion;
	protected ProjectileCollision collision;

	/** While it's possible to have projectiles without a unit caster, for now we say that all do */
	protected @NonNull Unit caster;
	protected @NonNull final MapInstance mapInstance;
	protected @NonNull final EffectsHandler gfx;

	public Projectile(float x, float y, @NonNull Unit caster) {
		pos = new Point(x, y);
		duration = DEFAULT_MAX_DURATION;
		this.caster = caster;
		side = caster.getSide();
		mapInstance = caster.getMapInstanceMutable();
		gfx = mapInstance.getGfx();
	}
	public void tick() {
		motion.tick();
		collision.tick();
	}
	public final boolean isExpired() {
		return duration <= 0;
	}
	public final boolean isCollided() {
		return collision.isCollided();
	}
	public final boolean isDone() {
		return isExpired() || isCollided();
	}
	public final void setMotion(ProjectileMotion motion) {
		if(this.motion!=null) {
			logger.warn("Projectile already has a motion component. Re-setting is possible but not intended");
		}
		this.motion = motion;
	}
	public final void setCollision(ProjectileCollision collision) {
		if(this.collision!=null) {
			logger.warn("Projectile already has a collision component. Re-setting is possible but not intended");
		}
		this.collision= collision;
	}
	
	public final float getX() {
		return pos.x;
	}

	public final float getY() {
		return pos.y;
	}
	public final float getVx() {
		return motion.getVx();
	}
	public final float getVy() {
		return motion.getVy();
	}
	public ProjectileMotion getMotion() {
		return motion;
	}
	public ProjectileCollision getCollision() {
		return collision;
	}
}
