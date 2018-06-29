package com.mygdx.holowyth.combatDemo.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.debug.DebugStore;

public class SparksEffectManager {
	
	ParticleEmitter emit;
	ParticleEffect effect;
	
	SparksEffectManager(Holowyth game, OrthographicCamera camera, DebugStore debugStore){
		effect = new ParticleEffect();
		effect.load(Gdx.files.internal("vfx/sparks.p"), Gdx.files.internal(""));
	}
}
