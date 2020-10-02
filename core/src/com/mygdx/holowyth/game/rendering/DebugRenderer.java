package com.mygdx.holowyth.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;

class DebugRenderer extends SubRenderer {

	public DebugRenderer(GameScreenRenderer renderer) {
		super(renderer);
	}

	void renderUnitKnockbackVelocities() {
		getMapInstance().doIfTrueForAllUnits(
				(UnitInfo u) -> (u.getMotion().isBeingKnockedBack()
						&& u.getMotion().getKnockbackVelocity().len() > 0.01f),
				(UnitInfo u) -> {
					float scale = 15;
					HoloGL.renderArrow(u.getPos(),
							u.getMotion().getKnockbackVelocity().setLength(scale), Color.ORANGE);
				});
	}

	final private Color LIGHT_MINT = HoloGL.rgb(236, 255, 229);

	void renderUnitIdsOnUnits() {
		batch.begin();
		Holowyth.fonts.borderedMediumFont().setColor(LIGHT_MINT);
		getMapInstance().doForAllUnits((UnitInfo u) -> {
			Holowyth.fonts.borderedMediumFont().draw(batch, String.valueOf(u.getID()), u.getX(), u.getY());
		});
		batch.end();
	}
}
