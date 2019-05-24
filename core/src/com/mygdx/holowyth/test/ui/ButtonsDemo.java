package com.mygdx.holowyth.test.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.VisUI;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.template.BaseScreen;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;

public class ButtonsDemo extends BaseScreen {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	InputMultiplexer multiplexer = new InputMultiplexer();
	private Label coordInfo;

	public ButtonsDemo(Holowyth game) {
		super(game);
		createUIElements();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(inputProcessor);

		createCoordinateText();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);

		batch.begin();

		BitmapFont font = Holowyth.fonts.font();
		font.setColor(Color.ORANGE);

		Holowyth.fonts.font().draw(batch, "Screen: Loading", MiscUtil.getCursorInWorldCoords(camera).x,
				MiscUtil.getCursorInWorldCoords(camera).y);
		batch.end();

		stage.draw();
	}

	private void createUIElements() {
		HoloUI.textButton(root, "hello", VisUI.getSkin(), () -> logger.debug("hello"));
	}

	/**
	 * Adds a small coordinate text that displays the mouse cursor position in world coordinates
	 */
	private void createCoordinateText() {
		coordInfo = new Label("(000, 000)\n", game.skin);
		coordInfo.setColor(Color.WHITE);
		stage.addActor(coordInfo);
		coordInfo.setPosition(Gdx.graphics.getWidth() - coordInfo.getWidth() - 4, 4);
	}

	public void updateMouseCoordLabel(int screenX, int screenY, Camera camera) {
		Point p = MiscUtil.getCursorInWorldCoords(camera);
		coordInfo.setText(
				"(" + (int) (p.x) + ", " + (int) (p.y) + ")\n" + "(" + (int) (p.x) / Holo.CELL_SIZE + ", "
						+ (int) (p.y) / Holo.CELL_SIZE + ")");

	}

	private InputProcessor inputProcessor = new InputProcessorAdapter() {
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			updateMouseCoordLabel(screenX, screenY, camera);
			return false;
		}
	};

}
