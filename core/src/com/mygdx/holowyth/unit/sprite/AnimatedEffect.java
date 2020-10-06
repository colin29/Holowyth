package com.mygdx.holowyth.unit.sprite;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


@NonNullByDefault
public class AnimatedEffect {

	public Animation<@NonNull TextureRegion> anim;
	public boolean additiveBlending = false;

	public AnimatedEffect(Animation<@NonNull TextureRegion> anim) {
		this.anim = anim;
	}
	
}
