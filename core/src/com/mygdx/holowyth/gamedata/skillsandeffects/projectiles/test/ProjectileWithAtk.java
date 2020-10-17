package com.mygdx.holowyth.gamedata.skillsandeffects.projectiles.test;


import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;

public class ProjectileWithAtk extends Projectile {

	public int atk;
	public float damage;
	public ProjectileWithAtk(float x, float y, Unit caster, UnitOrderable target) {
		super(x, y, caster);
	}

}
