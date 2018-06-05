package com.mygdx.holowyth.test.sandbox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class FooScreen2 implements Screen {

	private final FooGame game;
	private Camera camera;

	private Stage stage;
	private Skin skin;
	private Table root;

	
	public FooScreen2(final FooGame game) {
		this.game = game;
		this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));
		initFonts();
		this.skin = new Skin();
		this.skin.addRegions(game.assets.get("ui/uiskin.atlas", TextureAtlas.class));
		this.skin.add("default-font", this.font);
		this.skin.load(Gdx.files.internal("ui/uiskin.json"));
		createStage();

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		this.font.draw(game.batch,
				"We walked the skies of this crumbling realm. Across the lands we found dust, dust, and trees.", -400,
				260, 700, Align.left, true);
		game.batch.end();

		stage.draw();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(this.stage);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		this.font.dispose();
	}

	private void createStage() {
		stage = new Stage(new ScreenViewport());

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();

		// Add Widgets here

		//
		root.debug();
		
		TextButton b1 = new TextButton("Test", skin);
		stage.addActor(b1);
		b1.remove();
		stage.addActor(b1);
	}

	BitmapFont font;

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