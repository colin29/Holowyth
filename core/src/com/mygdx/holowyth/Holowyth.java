package com.mygdx.holowyth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.mygdx.holowyth.util.HoloUI;

public class Holowyth extends Game {

	public final int resX = 960;
	public final int resY = 640;

	/* Rendering and pipeline variables */
	public SpriteBatch batch;
	public ShapeRenderer shapeRenderer;

	public BitmapFont font;
	public BitmapFont font_goth36;

	public AssetManager assets;

	// IO
	public FileChooser fileChooser;

	public Holowyth() {
		screenToLoad = PolyMapEditor.class;
	}

	Class<?> screenToLoad;

	/* vvvvvvv User Methods vvvvvvv */
	public Holowyth(Class c) { // takes in screen to open
		if (Screen.class.isAssignableFrom(c)) {
			screenToLoad = c;
		} else {
			throw new RuntimeException("Holowyth: class given is not a sub-class of Screen");
		}
	}

	@Override
	public void create() {

		VisUI.load();

		initFileChoosers();
		initializeSharedResources();
		initFonts();

		this.assets = new AssetManager();
		LoadingScreen loadingScreen = new LoadingScreen(this);
		loadingScreen.queueAssets();
		this.assets.finishLoading();
		setScreenToGivenClass();

	}

	private void initFileChoosers() {
		FileChooser.setDefaultPrefsName("holowyth.test.ui.filechooser");
		fileChooser = new FileChooser(Mode.OPEN);
	}

	@Override
	public void render() {
		super.render(); // Calls Game.render, which will render the screens
	}

	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
		font_goth36.dispose();
	}

	private void initializeSharedResources() {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		font = new BitmapFont(); // Use LibGDX's default Arial font.
	}

	private void initFonts() {
		FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MS_Gothic.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 36;
		// HoloUI.addJapaneseCharacters(parameter);
		parameter.color = Color.WHITE;
		this.font_goth36 = generator.generateFont(parameter);
	}

	private void setScreenToGivenClass() {
		Constructor<?> cons;
		try {
			cons = screenToLoad.getConstructor(Holowyth.class);
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			this.setScreen((Screen) cons.newInstance(this));
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
