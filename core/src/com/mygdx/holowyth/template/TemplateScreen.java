package com.mygdx.holowyth.template;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.test.sandbox.FooGame;

public class TemplateScreen implements Screen {

	private final FooGame game;
	private Camera camera;
	
	private Stage stage;
	private Skin skin;
	private Table root;

	public TemplateScreen(final FooGame game) {
		this.game = game;
		this.camera =  new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		//skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));
		this.skin = new Skin();
		this.skin.addRegions(game.assets.get("ui/uiskin.atlas", TextureAtlas.class));
		this.skin.add("default-font", game.font_goth36);
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
		game.batch.end();
		
		stage.draw();
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
	}

	private void createStage(){
		stage = new Stage(new ScreenViewport());
		
		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();
		
		//Add Widgets here
		

		root.debug();
		
		
	}
}
