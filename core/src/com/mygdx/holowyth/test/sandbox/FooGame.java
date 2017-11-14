package com.mygdx.holowyth.test.sandbox;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kotcrab.vis.ui.VisUI;
import com.mygdx.holowyth.util.HoloUI;

public class FooGame extends Game {

	public final int resX = 960;
	public final int resY = 640;

	/* Rendering and pipeline variables */
	public SpriteBatch batch;
	public ShapeRenderer shapeRenderer;

	public BitmapFont font;
	public BitmapFont font_goth36;

	public AssetManager assets;

	@Override
	public void create() {

		initFonts();
		
		VisUI.load();
		initializeSharedResources();

		this.assets = new AssetManager();
		loadCommonAssets();
		this.setScreen(new FooScreen2(this));
	}

	@Override
	public void render() {
		super.render(); // Calls Game.render, which will render the screens
	}

	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
	}

	private void initializeSharedResources() {
		batch = new SpriteBatch();

		shapeRenderer = new ShapeRenderer();
		font = new BitmapFont(); // Use LibGDX's default Arial font.
	}
	private void initFonts() {
		FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
		FreeTypeFontGenerator generator	= new FreeTypeFontGenerator(Gdx.files.internal("fonts/MS_Gothic.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 36;
		HoloUI.addJapaneseCharacters(parameter);
		parameter.color = Color.WHITE;
		this.font_goth36 = generator.generateFont(parameter);

	}
	
	private void loadCommonAssets(){
		assets.load("ui/uiskin.atlas", TextureAtlas.class);
		assets.finishLoading();
	}
}
