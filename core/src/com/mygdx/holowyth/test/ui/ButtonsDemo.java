package com.mygdx.holowyth.test.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.kotcrab.vis.ui.VisUI;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.template.BaseScreen;

public class ButtonsDemo extends BaseScreen {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	InputMultiplexer multiplexer = new InputMultiplexer();

	public ButtonsDemo(Holowyth game) {
		super(game);
		createUIElements();
		multiplexer.addProcessor(stage);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void render(float delta) {
		stage.draw();
	}

	private void createUIElements() {
		HoloUI.textButton(root, "hello", VisUI.getSkin(), () -> logger.debug("hello"));
	}

}
