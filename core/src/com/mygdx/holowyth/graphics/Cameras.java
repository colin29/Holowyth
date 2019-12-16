package com.mygdx.holowyth.graphics;

import com.badlogic.gdx.graphics.Camera;

public class Cameras {
	public final Camera worldCamera;
	public final Camera fixedCamera;

	public Cameras(Camera worldCamera, Camera fixedCamera) {
		this.worldCamera = worldCamera;
		this.fixedCamera = fixedCamera;
	}
}
