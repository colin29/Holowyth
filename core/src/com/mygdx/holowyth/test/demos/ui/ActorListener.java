package com.mygdx.holowyth.test.demos.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ActorListener extends ClickListener {

	public Actor a;
	public ActorListener(Actor a){
		this.a = a;
	}
}
