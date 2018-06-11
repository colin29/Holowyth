package com.mygdx.holowyth.desktop.selectLauncher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.holowyth.test.sandbox.FooGame;
import com.mygdx.holowyth.test.sandbox.holowyth.Test1;
import com.mygdx.holowyth.test.sandbox.holowyth.Test2;
import com.mygdx.holowyth.test.ui.UIDemo;
import com.mygdx.holowyth.units.StatsDemo;

public class SelectLauncher {

	private static Map<String, AppLaunch> apps = new TreeMap<String, AppLaunch>();
	
	private static LwjglApplicationConfiguration defaultConfig;
	static {
		defaultConfig = new LwjglApplicationConfiguration();
		defaultConfig.width = 960;
		defaultConfig.height = 640;
		defaultConfig.samples = 5;
	}
	
	/**
	 *  Add apps to be served here.
	 */
	public static void addApps(){
		addApp(Test1.class);
		addApp(Test2.class);
		
		LwjglApplicationConfiguration uiConfig;
		
		uiConfig = new LwjglApplicationConfiguration();
		uiConfig.width = 380;
		uiConfig.height = 300;
		uiConfig.samples = 5;
		addApp(StatsDemo.class, uiConfig);
		
		uiConfig = new LwjglApplicationConfiguration();
		uiConfig.title = "CustomLaunchTitle";
		uiConfig.width = 960;
		uiConfig.height = 640;
		uiConfig.samples = 5;
		addApp(UIDemo.class, uiConfig);
	}

	private static String openThis = "statsdemo"; //Set this to skip the selection screen and open an app immediately.
	public static void main(String[] arg) throws InstantiationException, IllegalAccessException {

		addApps();
		printAllApps();
		System.out.println("Enter an app to launch:");

		Scanner s = new Scanner(System.in);

		AppLaunch app = null;
		while (app == null) {

			
			
			String input = null;
			
			if(openThis !=null){
				input = openThis;
				openThis = null;
			}else{
				input = s.nextLine();
			}
			
			app = apps.get(input.toLowerCase());
			


			if (app == null) {
				System.out.println("App does not exist. Please try again.");
				continue;
			}
		}
		s.close();
		
		Class<? extends ApplicationListener> type = app.type;
		
		System.out.printf("Starting up app: '%s'%n", type.getSimpleName());
		
		if(app.config != null){
			new LwjglApplication(type.newInstance(), app.config);
		}else{
			defaultConfig.title = type.getSimpleName();
			new LwjglApplication(type.newInstance(), defaultConfig);
		}
		
	}

	public static void addApp(Class<? extends ApplicationListener> type) {
		apps.put(type.getSimpleName().toLowerCase(), new AppLaunch(type));
	}
	
	/**
	 * Specifies a custom config
	 */
	public static void addApp(Class<? extends ApplicationListener> type, LwjglApplicationConfiguration config) {
		AppLaunch app = new AppLaunch(type);
		app.config = config;
		apps.put(type.getSimpleName().toLowerCase(), app);
	}
	

	/**
	 * Prints all the apps for the user to choose
	 */
	public static void printAllApps() {
		System.out.printf("Listing all apps: (%d apps)%n", apps.size());

		Set<Entry<String, AppLaunch>> appsSet = apps.entrySet();
		for (Entry<String, AppLaunch> entry : appsSet) {
			Class<? extends ApplicationListener> appType = entry.getValue().type;
			System.out.printf("> %s%n", appType.getSimpleName());
		}
	}
	

}