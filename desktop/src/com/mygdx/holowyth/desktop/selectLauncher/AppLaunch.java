package com.mygdx.holowyth.desktop.selectLauncher;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/*
 * Carries the information needed to launch a libgdx app.
 * Contains the type of the Libgdx app and an optional config. 
 */


public class AppLaunch {
	
	public String name; //actual name of the class
	
	public LwjglApplicationConfiguration config = null;
	public Class<? extends ApplicationListener> type;
	
	public AppLaunch(Class<? extends ApplicationListener> appType){
		this.type = appType;
	}
	
	
}
