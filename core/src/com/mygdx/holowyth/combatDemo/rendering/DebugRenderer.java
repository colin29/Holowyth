package com.mygdx.holowyth.combatDemo.rendering;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;

class DebugRenderer extends SubRenderer {

	public DebugRenderer(Renderer renderer) {
		super(renderer);
	}

	void renderUnitKnockbackVelocities() {
		getWorld().doIfTrueForAllUnits(
				(UnitInfo u) -> (u.getMotion().isBeingKnockedBack()
						&& u.getMotion().getKnockBackVelocity().len() > 0.01f),
				(UnitInfo u) -> {
					float scale = 15;
					HoloGL.renderArrow(u.getPos(),
							u.getMotion().getKnockBackVelocity().setLength(scale), Color.ORANGE);
				});
	}
}
