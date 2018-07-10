package com.mygdx.holowyth.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.CombatDemo;
import com.mygdx.holowyth.pathfinding.demo.PathfindingDemo;
import com.mygdx.holowyth.test.gdx.SceneTest;
import com.mygdx.holowyth.test.sandbox.holowyth.Test2;
import com.mygdx.holowyth.test.ui.UIDemo;

public class CombatLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Holowyth";
		config.width = 960;
		config.height = 640;
		config.samples = 5;
		config.vSyncEnabled = false;
		config.foregroundFPS = 0;
		new LwjglApplication(new Holowyth(CombatDemo.class), config);
	}
}