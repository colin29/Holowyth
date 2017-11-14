package com.mygdx.holowyth.test.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;

public class GameScreen implements Screen {

	// Game Layer
	Sprite bg;
	SpriteBatch batch;
	ArrayList<Sprite> skillIcons = new ArrayList<Sprite>();

	ShapeRenderer shapeRenderer;

	// UI Layer
	private Stage stage;
	private Table table;
	Skin skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));

	

	
	public GameScreen() {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		loadSprites();
		
		setupUI();
	}

	private void loadSprites() {
		bg = new Sprite(new Texture(Gdx.files.internal("watercity.jpg")));
		// center the image in the middle of the window
		// bg.setCenter(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		assert true;
	}

	Table gameMenu;

	private void setupUI() {
		stage = new Stage(new ScreenViewport());
		table = new Table();
		table.debug();
		// .debug();

		table.align(Align.bottom | Align.left);
		table.setWidth(Gdx.graphics.getWidth());
		table.setHeight(Gdx.graphics.getHeight());
		table.setPosition(0, 0);

		HorizontalGroup g = new HorizontalGroup().space(30).pad(10, 10, 10, 10).fill();

		Image icon1 = new Image(new Texture(Gdx.files.internal("icons//staff01.PNG")));
		Image icon2 = new Image(new Texture(Gdx.files.internal("icons//sword18.PNG")));
		Image icon3 = new Image(new Texture(Gdx.files.internal("icons//sword25.PNG")));

		g.addActor(icon1);
		g.addActor(icon2);
		g.addActor(icon3);

		table.add().width(Gdx.graphics.getWidth() / 3);
		Cell<HorizontalGroup> cell = table.add(g);
		table.add().width(Math.max(0, Gdx.graphics.getWidth() * 1 / 3 - cell.getMinWidth()));

		TextButton menuButton = new TextButton("Menu", skin);
		menuButton.setSize(30, 70);

		menuButton.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				gameMenu.setVisible(true);
			}
		});

		table.add(menuButton).align(Align.bottom).padLeft(Gdx.graphics.getWidth() / 3 - menuButton.getMinWidth());

		stage.addActor(table);
		

		// Create the in-game menu
		gameMenu = new Table();
		
		TextButton bSettings = new TextButton("Settings", skin);
		TextButton bTitle = new TextButton("Exit to Title", skin);
		TextButton bCancel = new TextButton("Cancel", skin);

		
		bTitle.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				dispose();
				((Game)Gdx.app.getApplicationListener()).setScreen(new MainMenu());
			}
			
		});
		
		bCancel.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				gameMenu.setVisible(false);
			}
		});
//		
		List<TextButton> buttons = Arrays.asList(bSettings, bTitle, bCancel);

		gameMenu.pad(20);
		for (int i = 0; i < buttons.size(); i++) {
			TextButton b = buttons.get(i);
			Cell<TextButton> c = gameMenu.add(b);
			if (i != buttons.size() - 1) {
				c.padBottom(20);
			}
			c.size(90, 35);
			gameMenu.row();
		}

		
		gameMenu.align(Align.top | Align.left);
		gameMenu.pack();
		gameMenu.setPosition(Gdx.graphics.getWidth() / 2 - gameMenu.getMinWidth()/2
				, Gdx.graphics.getHeight() / 2 - gameMenu.getMinHeight()/2);
		
		stage.addActor(gameMenu);
		gameMenu.setVisible(false);
	}

	private void createGameMenu() {

	}

	@Override
	public void show() {
		InputMultiplexer im = new InputMultiplexer(stage);
		Gdx.input.setInputProcessor(im);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		bg.draw(batch);
		// for(Sprite icon: skillIcons){
		// icon.draw(batch);
		// }
		batch.end();

		

		//Draw background for gameMenu
		if(gameMenu.isVisible()){
			Gdx.gl.glEnable(GL20.GL_BLEND);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(new Color(0.5f, 0.5f, 0.5f, 0.8f));
			shapeRenderer.rect(gameMenu.getX(), gameMenu.getY(), gameMenu.getPrefWidth(), gameMenu.getMinHeight());
			shapeRenderer.end();
		}
		
		// Draw UI
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

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
		stage.dispose();
	}

}
