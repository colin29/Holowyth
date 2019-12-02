package com.mygdx.holowyth.graphics.effects;

import java.util.ArrayList;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.effects.particle.ParticleEffect;
import com.mygdx.holowyth.graphics.effects.particle.ParticleEffectPool;
import com.mygdx.holowyth.graphics.effects.particle.ParticleEffectPool.PooledEffect;
import com.mygdx.holowyth.graphics.effects.particle.ParticleEmitter;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

public class SparksEffectHandler {

	ParticleEmitter emit;
	ParticleEffect effectBlueprint;
	private ParticleEffectPool effectPool;
	private ArrayList<PooledEffect> effects = new ArrayList<PooledEffect>();

	SpriteBatch batch;
	OrthographicCamera camera;

	SparksEffectHandler(SpriteBatch batch, OrthographicCamera camera, DebugStore debugStore) {
		effectBlueprint = new ParticleEffect();
		effectBlueprint.load(Gdx.files.internal(Holowyth.ASSETS_PATH + "vfx/sparks.p"),
				Gdx.files.internal(Holowyth.ASSETS_PATH + "vfx"));
		effectBlueprint.setPosition(300, 300);
		effectPool = new ParticleEffectPool(effectBlueprint, 20, 20);

		this.batch = batch;
		this.camera = camera;

		// DebugValues debugValues = debugStore.registerComponent("Sparks Manager");
		// debugValues.add("Sparks count: ", ()->effects.size());
	}

	public void render() {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		ListIterator<PooledEffect> iter = effects.listIterator();
		while (iter.hasNext()) {
			PooledEffect e = iter.next();
			if (e.isComplete()) {
				effectPool.free(e);
				iter.remove();
			} else {
				e.draw(batch, Gdx.graphics.getDeltaTime());
			}
		}
		batch.end();
	}

	public void addSparkEffect(float x, float y) {
		PooledEffect effect = effectPool.obtain();
		effect.setPosition(x, y);
		effects.add(effect);
		effect.start();
	}

}
