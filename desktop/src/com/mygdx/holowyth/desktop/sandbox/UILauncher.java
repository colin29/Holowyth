package com.mygdx.holowyth.desktop.sandbox;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.holowyth.test.demos.ui.UIDemo;

public class UILauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Holowyth";
		config.width = 960;
		config.height = 640;
		config.samples = 5;
		new LwjglApplication(new UIDemo(), config);
	}
}