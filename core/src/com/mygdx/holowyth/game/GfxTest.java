package com.mygdx.holowyth.game;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.graphics.effects.animated.EffectCenteredOnUnit;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.ShapeDrawerPlus;


@NonNullByDefault
public class GfxTest {
	
	private @Nullable EffectCenteredOnUnit effect;
	private ShapeDrawerPlus shapeDrawer;
	private AssetManager assets;
	private SpriteBatch batch;

	public GfxTest(SpriteBatch batch, Animations animations, ShapeDrawerPlus shapeDrawer, AssetManager assets) {
		this.batch = batch;
		this.shapeDrawer = shapeDrawer;
		this.assets = assets;
	}

	public void playEffectOverUnit(UnitInfo unit, String effectName, MapInstanceInfo mapInstance, Animations anims) {
		effect = new EffectCenteredOnUnit(unit, effectName, mapInstance, anims);	
		effect.setSize(72, 72);
		effect.setAlpha(0.85f);
		effect.begin();
	}
	
	@SuppressWarnings("null")
	public void render() {
		if(effect != null && !effect.isComplete()) {
			effect.render(batch, shapeDrawer, assets);
		}
	}
}
