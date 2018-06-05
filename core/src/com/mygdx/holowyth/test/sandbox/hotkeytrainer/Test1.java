package com.mygdx.holowyth.test.sandbox.hotkeytrainer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.holowyth.util.HoloUI;

public class Test1 extends ApplicationAdapter {

	ShapeRenderer r;
	OrthographicCamera c;
	
	SpriteBatch batch;

	Stage stage;

	Skin skin;
	BitmapFont font;
	

	@Override
	public void create() {
		r = new ShapeRenderer();
		c = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		batch = new SpriteBatch();
		
		initFonts();
		
		this.skin = new Skin();
		this.skin.addRegions(new TextureAtlas("ui/uiskin.atlas"));		
		this.skin.add("default-font", this.font);
		this.skin.load(Gdx.files.internal("ui/uiskin.json"));
		
		createStage();
		Gdx.input.setInputProcessor(stage);
		
		loadImages();
	}
	
	Texture fairy01;
	TextureRegion fairyRegion;
	private void loadImages(){
		fairy01 = new Texture("img/igyo-boushi01.png");
		fairyRegion = new TextureRegion(fairy01);
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
		
		
		batch.begin();
		batch.draw(fairyRegion, 0, 0);
		batch.draw(fairyRegion, 200, 0, 100, 100);
		batch.draw(fairyRegion, 300, 0, fairyRegion.getRegionWidth()/2, fairyRegion.getRegionHeight()/2, fairyRegion.getRegionWidth(), fairyRegion.getRegionHeight(), scale, scale, 0);
		batch.end();
		
	}
	
	private void createStage(){
		stage = new Stage(new ScreenViewport());
		

		createTestTable();
		
		
	}
	
	float originX = 0;
	float originY = 0;
	float scale = 1;
	private void createTestTable(){
		Label n1 = new Label("Name:", skin);
		TextField n1TextField = new TextField("", skin);
		
		Label n2 = new Label("Name2--", skin);
		
		
		
		TextButton b1 = new TextButton("Test", skin);
		b1.addListener(new ClickListener(){

			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				System.out.println("Button was clicked");
			}

			
			
		});
		

		Table table = new Table();
		stage.addActor(table);
		
		table.setFillParent(true);
	
		
//		table.row();//.padTop(20);
//		table.add(n1);
//		table.add(n1TextField).height(60).uniform();
//		table.row();
//		table.add(n2).uniform();
//		table.row();
//		
//		table.add(b1);
		
		table.row();
		HoloUI.parameterSlider(0, 200, "originX", table, skin, (Float f) ->{originX=f;});
		HoloUI.parameterSlider(0.1f, 5, "scale", table, skin, (Float f) ->{scale=f;});
		
		System.out.println(table.getRows());
		
		
		table.left().top();
		table.pad(30);
		table.debug();
		
		
//		table.setPosition(190, 142);
		
		
	}
	
	private void initFonts() {

		FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MS_Gothic.ttf"));

		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 24;
		// HoloUI.addJapaneseCharacters(parameter);
		parameter.borderColor = Color.BLACK;
		parameter.borderWidth = 1;
		parameter.color = Color.WHITE;
		this.font = generator.generateFont(parameter);
	}

}









