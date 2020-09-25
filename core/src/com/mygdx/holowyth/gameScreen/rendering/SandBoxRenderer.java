package com.mygdx.holowyth.gameScreen.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.holowyth.graphics.HoloSprite;
import com.mygdx.holowyth.unit.Unit;

class SandBoxRenderer extends SubRenderer {

	public SandBoxRenderer(GameScreenRenderer renderer) {
		super(renderer);
	}

	void renderUnitsWithTestSprites() {

		TextureRegion witchTex = new TextureRegion(game.assets.get("img/witch.png", Texture.class));
		witchTex.getTexture().setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Nearest);

		for (Unit unit : getWorld().getUnits()) {
			if (unit.isAPlayerCharacter()) {
				HoloSprite player = new HoloSprite(witchTex, unit.x, unit.y, 30, 0, 20);
				player.draw(batch);
			} else {
				HoloSprite player = new HoloSprite(witchTex, unit.x, unit.y, 30, 0, 20);
				player.draw(batch);
			}
		}

	}
}
