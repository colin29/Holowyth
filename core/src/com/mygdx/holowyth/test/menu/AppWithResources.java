package com.mygdx.holowyth.test.menu;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public interface AppWithResources {
	SpriteBatch getBatch();

	ShapeRenderer getShapeRenderer();

	Skin getSkin();

}
