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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.kotcrab.vis.ui.VisUI;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.template.HoloBaseScreen;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;

public class ButtonsDemo extends HoloBaseScreen {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	InputMultiplexer multiplexer = new InputMultiplexer();
	private Label coordInfo;

	public final BitmapFont goth24;
	
	public ButtonsDemo(Holowyth game) {
		super(game);
		goth24 = normal("fonts/MS_Gothic.ttf", Color.WHITE, 24);
		
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

		stage.draw();
	}

	private void createUIElements() {
		root.debugAll();

		root.bottom().left();
		root.pad(10);

		root.row().space(20);

		TextButtonStyle style = new TextButtonStyle(VisUI.getSkin().get(TextButtonStyle.class));

		style.font = goth24;
		style.fontColor = Color.FOREST;

		TextButton b1 = new TextButton("test button", game.skin);
		TextButton b2 = new TextButton("test button", game.skin);
		TextButton b3 = new TextButton("test button", game.skin);

		float topPad = 3;
		float leftPad = 10;
		float rightPad = 10;
		float botPad = 5;

		b1.pad(topPad, leftPad, botPad, rightPad);
		b2.pad(topPad, leftPad, botPad, rightPad);
		b3.pad(topPad, leftPad, botPad, rightPad);

		Cell cell = HoloUI.textButton(root, "hi", style, (ChangeEvent event, Actor actor) -> {
			logger.debug("msg");
		});

		root.add(b1, b2, b3);

		b1.addListener((Event e) -> {
			e.getListenerActor();
			return true;
		});
	}

	
	BitmapFont normal(String path, Color color, int size) {
		FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Holo.ASSETS_PATH + path));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = size;
		// HoloUI.addJapaneseCharacters(parameter);
		parameter.color = color;
		BitmapFont font = generator.generateFont(parameter);
		generator.dispose();
		return font;
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
