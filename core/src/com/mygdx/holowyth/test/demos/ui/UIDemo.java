package com.mygdx.holowyth.test.demos.ui;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.VisUI;

public class UIDemo extends Game {
	
	public void create () {
		
		VisUI.load();
		
		MainMenu mainMenu = new MainMenu();
		GameScreen gameScreen = new GameScreen();
		this.setScreen(mainMenu);
		
	}

	public void render () {
		super.render();
	}

	public void dispose() {
		VisUI.dispose();
	}


}

