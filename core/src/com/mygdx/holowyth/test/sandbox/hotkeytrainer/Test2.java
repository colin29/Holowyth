package com.mygdx.holowyth.test.sandbox.hotkeytrainer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.holowyth.util.misc.HoloMisc;

public class Test2 extends ApplicationAdapter {

	ShapeRenderer r;
	OrthographicCamera c;

	Stage stage;

	Skin skin;

	@Override
	public void create() {
		r = new ShapeRenderer();
		c = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		createStage();
	}
	@Override
	public void render() {
		// Preparatory tasks
		c.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Normal rendering activity

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
		
		
	}
	
	private void createStage(){
		stage = new Stage(new ScreenViewport());
		
				
		skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));
		createRootTable();
	}
	
	
	//Make a table that fills to whole stage and add some buttons to it.
	private void createRootTable(){
		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		
		root.debug();
		root.setPosition(50, -50);
		
		TextButton b1 = new TextButton("Load", skin);
		TextButton b2 = new TextButton("Save", skin);
		
		root.add(b1);
		root.add(b2);
		
		HoloMisc.printDirectory(".\\");
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts\\fantasy_one.ttf"));
		
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 24;
		BitmapFont font24 = generator.generateFont(parameter);
		//Want to use this font in a textButton
		LabelStyle l = new LabelStyle(font24, Color.WHITE);
		
		generator.dispose(); // don't forget to dispose to avoid memory leaks!
		
		
		//atlas = new TextureAtlas(Gdx.files.internal("packedimages/pack.atlas"));
		
	}
	
	
}









