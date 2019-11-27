package com.mygdx.holowyth.skill.skillsandeffects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.skill.effect.CasterUnitEffect;
import com.mygdx.holowyth.skill.effect.projectiles.MagicMissileBolt;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

public class MageEffects {
	static class MagicMissileEffect extends CasterUnitEffect {
		public MagicMissileEffect(Unit caster, Unit target) {
			super(caster, target);
		}

		int damage = 8;
		List<MagicMissileBolt> missiles;

		float missileVfxRadius = 5;

		@Override
		public void begin() {
			missiles = new ArrayList<MagicMissileBolt>();
			for (int i = 0; i < 3; i++) {
				missiles.add(new MagicMissileBolt(caster.x, caster.y + 18 * i, damage, target, caster, world.getUnits())); // space the missiles out
			}
		}

		@Override
		public void tick() {

			for (var m : missiles) {
				m.tick();
			}
			missiles.removeIf(m -> m.duration <= 0 || m.collided);

			if (missiles.isEmpty())
				markAsComplete();
		}

		@Override
		public void render(SpriteBatch batch, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
			shapeDrawer.setColor(Color.RED, 0.8f);
			batch.begin();
			for (var m : missiles) {
				shapeDrawer.filledCircle(m.x, m.y, missileVfxRadius);
			}
			batch.end();
		}

	};
}
