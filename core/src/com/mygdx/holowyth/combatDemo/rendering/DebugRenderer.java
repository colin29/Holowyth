package com.mygdx.holowyth.combatDemo.rendering;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
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

	final private Color LIGHT_MINT = HoloGL.rgb(236, 255, 229);

	void renderUnitIdsOnUnits() {
		batch.begin();
		Holowyth.fonts.borderedDebugFont().setColor(LIGHT_MINT);
		getWorld().doForAllUnits((UnitInfo u) -> {
			Holowyth.fonts.borderedDebugFont().draw(batch, String.valueOf(u.getID()), u.getX(), u.getY());
		});
		batch.end();
	}
}
