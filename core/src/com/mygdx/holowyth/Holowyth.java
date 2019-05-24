package com.mygdx.holowyth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.mygdx.holowyth.editor.PolyMapEditor;
import com.mygdx.holowyth.graphics.HoloGL;

public class Holowyth extends Game {

	public final int resX = 960;
	public final int resY = 640;

	/* Rendering and pipeline variables */
	public SpriteBatch batch;
	public ShapeRenderer shapeRenderer;

	public static final String ASSETS_PATH = "assets/";
	public final AssetManager assets;

	/* Fonts */

	public final static MyFonts fonts = new MyFonts();

	/* Skins */
	public Skin skin;

	// IO
	public FileChooser fileChooser;

	Class<?> screenToLoad;

	public Holowyth() {
		this(PolyMapEditor.class);
	}

	public Holowyth(Class<? extends Screen> clazz) {

		assets = new PrefixingAssetManager(ASSETS_PATH);

		screenToLoad = clazz;
	}

	@Override
	public void create() {

		VisUI.load();
		skin = VisUI.getSkin();

		initRendering();
		initFileChoosers();
		initFonts();

		LoadingScreen loadingScreen = new LoadingScreen(this);

		loadingScreen.queueAssets();
		this.assets.finishLoading();
		setScreenToGivenClass();

	}

	private void initRendering() {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		HoloGL.setShapeRenderer(shapeRenderer);
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
	}

	private void initFonts() {
		fonts.init();
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
