package com.mygdx.holowyth.test.menu;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class MenuAppLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Holowyth";
		config.width = 960;
		config.height = 640;
		config.samples = 5;
		config.vSyncEnabled = false;
		config.foregroundFPS = 0;
		new LwjglApplication(new MenuApp(), config);
	}
}