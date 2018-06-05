package com.mygdx.holowyth.test.sandbox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class FooScreen implements Screen {

	private final FooGame game;
	private Camera camera;
	
	private Stage stage;
	private Skin skin;
	private Table root;

	/**
	 * 
	 * @param game
	 *            Renders some English and Japanese text directly on to the
	 *            screen. Renders text in a Scene2D Textbox
	 */
	public FooScreen(final FooGame game) {
		this.game = game;
		this.camera =  new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f,0);
		
		loadOtherAssets();
		
		//skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));
		this.skin = new Skin();
		this.skin.addRegions(game.assets.get("ui/uiskin.atlas", TextureAtlas.class));
		this.skin.add("default-font", game.font_goth36);
		this.skin.load(Gdx.files.internal("ui/uiskin.json"));
		
		createStage();
		Gdx.input.setInputProcessor(stage);
		
	}


	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		
		renderSomeJPText();
		
		
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

	Cell<Table> c2;
	private Table dialog;
	private void createStage(){
		stage = new Stage(new ScreenViewport());
		
		root = new Table();
		root.setFillParent(true);
		root.top().left();
		stage.addActor(root);
		
		
		//Make a button which opens up a dialog with options
		
		TextButton b1 = new TextButton("Pause Menu", this.skin);
		dialog = new Table(this.skin);
		
		b1.setScale(2);
		root.add(b1).size(200,60);
		b1.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				System.out.println("b1 pressed");
				dialog.setVisible(true);
			}
			
		});
		createBackToMenuDialog(b1);

		//Testing using Label Styles
		
		LabelStyle labelStyle = new LabelStyle(game.font_goth36, Color.BROWN);
		Label l1 = new Label("label text", labelStyle);
		root.add(l1);
		
		
		
		
		root.debug();
	}
	
	
	private void createBackToMenuDialog(TextButton b1){
				//Make the pop-up dialog
				dialog.setVisible(false);
				//dialog.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
				dialog.center();
				TextButton c1 = new TextButton("Exit to Menu", this.skin);
				c1.addListener(new ChangeListener(){
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						dialog.setVisible(false);
					}
				});
				dialog.add(c1);
				Texture img = game.assets.get("img/test/touhou_kagerou.jpg", Texture.class);
				Drawable bg = new TextureRegionDrawable(new TextureRegion(img));
				dialog.setBackground(bg);
//				dialog;
				dialog.debug();
				
				//Use a nested table to align dialog to center
				final Table root2 = new Table();
				c2 = root2.add(dialog).size(img.getWidth()/2,img.getHeight()/2);
				b1.addListener(new ChangeListener(){
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						c2.size(400,400);
						root2.layout();
					}
					
				});

				root2.setFillParent(true);
				root2.center();
				stage.addActor(root2);
				
				root2.debug();
	}
	
	private void renderSomeJPText(){
		game.batch.begin();
		game.font_goth36.draw(game.batch, "I kinda found relatable:\r\n" + 
				"\r\n" + 
				"-Tiat's struggle and being lost. The process of looking back on your young self. Being so focused on one thing that it becomes a large fixture in your life (Kutori for her)\r\n" + 
				"-Kutori's ˆÓ’n. Her process of finding out her feelings and moving forward herself.\r\n" + 
				"-Nephelm's ‹•–³Š´. Not being attached to much, the thought that every thing could just disappear the moment your turn away from it.\r\n" + 
				"", 50, 600, 700, Align.left,  true);
		game.batch.end();
	}
	private void loadOtherAssets(){
		game.assets.load("img/test/touhou_kagerou.jpg", Texture.class);
		game.assets.finishLoading();
	}
}
