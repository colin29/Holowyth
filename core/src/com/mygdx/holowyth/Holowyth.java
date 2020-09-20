package com.mygdx.holowyth;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.mygdx.holowyth.editor.PolyMapEditor;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.map.GameMapRepo;
import com.mygdx.holowyth.util.ShapeDrawerPlus;

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

	/* IO */
	public FileChooser fileChooser;
	
	/* Game */
	public final GameMapRepo mapRepo = new GameMapRepo(); 
	

	Class<? extends Screen> screenClassToLoad;
	public ShapeDrawerPlus shapeDrawer;

	public Holowyth() {
		this(PolyMapEditor.class);
	}

	public Holowyth(Class<? extends Screen> clazz) {

		assets = new PrefixingAssetManager(ASSETS_PATH);

		screenClassToLoad = clazz;
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

		setScreenToClass(screenClassToLoad);

	}

	private void initRendering() {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		HoloGL.setShapeRenderer(shapeRenderer);

		shapeDrawer = new ShapeDrawerPlus(batch, get1PixelWhiteTextureRegion());
	}

	private void initFileChoosers() {
		FileChooser.setDefaultPrefsName("holowyth.test.ui.filechooser");
		fileChooser = new FileChooser(Mode.OPEN);
	}

	@Override
	public void render() {
		try {
			super.render(); // Calls Game.render, which will render the screens
		} catch (Exception e) {
			writeException(e);
			dispose();
			System.exit(-1);
		}
	}

	public static void writeException(Exception e) {
		try (FileWriter fs = new FileWriter("exception.txt", true);
				BufferedWriter out = new BufferedWriter(fs);
				PrintWriter pw = new PrintWriter(out, true);) {
			pw.append("------------\n");
			e.printStackTrace(pw);
			pw.append("\n\n\n");
			pw.flush();
		} catch (Exception ie) {
			throw new RuntimeException("Could not write Exception to file", ie);
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	private void initFonts() {
		fonts.init();
	}

	private void setScreenToClass(Class<? extends Screen> clazz) {
		Constructor<?> cons;
		try {
			cons = screenClassToLoad.getConstructor(Holowyth.class);
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

	private static TextureRegion get1PixelWhiteTextureRegion() {
		var labelColor = new Pixmap(1, 1, Pixmap.Format.RGB888);
		labelColor.setColor(Color.WHITE);
		labelColor.fill();
		return new TextureRegion(new Texture(labelColor));
	}

}
