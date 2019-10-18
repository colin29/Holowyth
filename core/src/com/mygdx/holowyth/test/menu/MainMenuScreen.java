package com.mygdx.holowyth.test.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;

public class MainMenuScreen extends BaseScreen {

	private Skin skin;

	InputMultiplexer multiplexer = new InputMultiplexer();

	public MainMenuScreen(AppWithResources app) {
		super(app);
		skin = app.getSkin();

		createUI();
	}

	private void createUI() {
		root.add(new Label("Test", skin));
	}

	@Override
	public void render(float delta) {
		glClear(Color.BLACK);

		stage.act();
		stage.draw();
	}

	InputProcessorAdapter input = new InputProcessorAdapter() {
		@Override
		public boolean keyDown(int keycode) {
			stage.setDebugAll(!stage.isDebugAll());
			return false;
		}
	};

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
		multiplexer.clear();
		multiplexer.addProcessor(input);
		multiplexer.addProcessor(stage);
	}
}
