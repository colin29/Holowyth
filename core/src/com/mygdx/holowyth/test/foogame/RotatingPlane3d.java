package com.mygdx.holowyth.test.foogame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class RotatingPlane3d implements Screen {

	private final FooGame game;
	private Camera camera;

	private Stage stage;
	private Skin skin;
	private Table root;

	/**
	 * Uses a perspective camera to look at a shape that is transformed or rotated
	 */
	public RotatingPlane3d(final FooGame game) {
		this.game = game;
		this.camera = new PerspectiveCamera(67
				, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));
		this.skin = new Skin();
		this.skin.addRegions(game.assets.get("ui/uiskin.atlas", TextureAtlas.class));
		this.skin.add("default-font", game.font_goth36);
		this.skin.load(Gdx.files.internal("ui/uiskin.json"));
		createStage();	

	}

	
	float timeElapsed = 0;
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		
		camera.far = 200;
		camera.update();
		game.shapeRenderer.setProjectionMatrix(camera.combined);
		
		float width = 100;
		float height = 60;
		
		timeElapsed += delta;
		game.shapeRenderer.begin(ShapeType.Filled);
		
		game.shapeRenderer.setColor(Color.RED);
		game.shapeRenderer.identity();
		game.shapeRenderer.translate(0, 0, -100);
		game.shapeRenderer.rect(-width / 2, -height / 2, width, height);
		
		game.shapeRenderer.setColor(Color.WHITE);
		game.shapeRenderer.identity();
		game.shapeRenderer.translate(0*timeElapsed, 0*timeElapsed, -100 + -0*timeElapsed);
//		game.shapeRenderer.translate(0*timeElapsed, 0*timeElapsed, -100 + -0*timeElapsed);
//		game.shapeRenderer.rotate(0, 0, 1, 4*timeElapsed);
		game.shapeRenderer.rotate(0, 0, 1, 20*timeElapsed);
		game.shapeRenderer.rect(0, 0, width, height);
		game.shapeRenderer.setColor(Color.BROWN);
		game.shapeRenderer.rect(-width / 20, -height / 2, width/10, height);
		
		
		
		game.shapeRenderer.end();

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

	private void createStage() {
		stage = new Stage(new ScreenViewport());

		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();

		// Add Widgets here

		root.debug();
	}
}
