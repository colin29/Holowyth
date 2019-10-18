package com.mygdx.holowyth.test.menu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;

public class MenuApp extends Game implements AppWithResources {

	private ShapeRenderer shapeRenderer;
	private SpriteBatch spriteBatch;

	private Skin skin;

	@Override
	public void create() {
		VisUI.load();
		skin = VisUI.getSkin();
		setScreen(new MainMenuScreen(this));
	}

	@Override
	public ShapeRenderer getShapeRenderer() {
		return shapeRenderer;
	}

	@Override
	public SpriteBatch getBatch() {
		return spriteBatch;
	}

	@Override
	public Skin getSkin() {
		return skin;
	}

}
