package com.mygdx.holowyth.combatDemo.effects;

import java.util.ArrayList;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.effects.particle.ParticleEffect;
import com.mygdx.holowyth.combatDemo.effects.particle.ParticleEffectPool;
import com.mygdx.holowyth.combatDemo.effects.particle.ParticleEffectPool.PooledEffect;
import com.mygdx.holowyth.util.debug.DebugStore;
import com.mygdx.holowyth.combatDemo.effects.particle.ParticleEmitter;

public class SparksEffectHandler {

	ParticleEmitter emit;
	ParticleEffect effectBlueprint;
	private ParticleEffectPool effectPool;
	private ArrayList<PooledEffect> effects = new ArrayList<PooledEffect>();
	
	SpriteBatch batch;
	OrthographicCamera camera;
	

	SparksEffectHandler(Holowyth game, OrthographicCamera camera, DebugStore debugStore) {
		effectBlueprint = new ParticleEffect();
		effectBlueprint.load(Gdx.files.internal("vfx/sparks.p"), Gdx.files.internal("vfx"));
		effectBlueprint.setPosition(300, 300);
		effectPool = new ParticleEffectPool(effectBlueprint, 20, 20);

		this.batch = game.batch;
		this.camera = camera;
		
//		DebugValues debugValues = debugStore.registerComponent("Sparks Manager");
//		debugValues.add("Sparks count: ", ()->effects.size());
	}

	public void render() {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		ListIterator<PooledEffect> iter = effects.listIterator();
		while(iter.hasNext()) {
			PooledEffect e = iter.next();
			if(e.isComplete()) {
				effectPool.free(e);
				iter.remove();
			}else {
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
