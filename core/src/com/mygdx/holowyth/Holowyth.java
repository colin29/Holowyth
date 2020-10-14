package com.mygdx.holowyth;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.annotation.NonNull;
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
import com.mygdx.holowyth.game.session.SessionData;
import com.mygdx.holowyth.gamedata.maps.HolowythWorld;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.test.demos.combatdemo.CombatDemo;
import com.mygdx.holowyth.town.TownScreen;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.world.World;

public class Holowyth extends Game {

	public final int resX = 960;
	public final int resY = 640;

	/* Rendering and pipeline variables */
	public SpriteBatch batch;
	public ShapeRenderer shapeRenderer;

	public static final String ASSETS_DISK_PATH = "./resources/assets/";
	public static final String ASSETS_PATH = "assets/";
	public @NonNull AssetManager assets;

	/* Fonts */

	public static Fonts fonts;
	/* Skins */
	public Skin skin;

	/* IO */
	public FileChooser fileChooser;
	
	/* Game */
	public final World world = new HolowythWorld();
	
	/* Sprite resources */
	public Animations animations;
	

	Class<? extends Screen> screenClassToLoad;
	public ShapeDrawerPlus shapeDrawer;

	public Holowyth() {
		this(CombatDemo.class);
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
		
		animations = new Animations();
		

		new LoadingScreen(this);
		this.assets.finishLoading();

		createAndSetToScreen(screenClassToLoad);
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
			throw e;
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
		fonts = new Fonts();
	}

	private void createAndSetToScreen(Class<? extends Screen> clazz) {
		Constructor<?> cons;
		try {
			if(clazz == TownScreen.class) {
			cons = screenClassToLoad.getConstructor(Holowyth.class, SessionData.class);
			}else {
				cons = screenClassToLoad.getConstructor(Holowyth.class);
			}
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
			return;
		}
		try {
			if(clazz == TownScreen.class) {
				var townScreen = new TownScreen(this, new SessionData.DummySessionData(), null);
				townScreen.loadTown(world.getNewTownInstance("testTown"));
				this.setScreen(townScreen);
				
			}else {
				this.setScreen((Screen) cons.newInstance(this));	
			}
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
