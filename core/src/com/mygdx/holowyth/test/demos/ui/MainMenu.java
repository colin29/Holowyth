package com.mygdx.holowyth.test.demos.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;

public class MainMenu implements Screen {

	Skin skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));

	private Stage stage;
	private Table table;
	
	private FileChooser loadFileChooser;
	private FileChooser saveFileChooser;

	public MainMenu() {
		// camera = new OrthographicCamera();
		// camera.setToOrtho(false, game.resX, game.resY);

		stage = new Stage(new ScreenViewport());

		loadFileChoosers();
		
		// Add Main Menu Buttons
		table = new Table();// .debug();

		table.align(Align.top | Align.right);

		final int tableWidth = Gdx.graphics.getWidth() / 4;
		final int tableHeight = Gdx.graphics.getHeight() / 2;
		table.setWidth(tableWidth);
		table.setHeight(tableHeight);
		table.setPosition(Gdx.graphics.getWidth() - tableWidth, 0); //

		VerticalGroup g = new VerticalGroup().space(30).pad(10, 10, 10, 50).fill();
		final float mainMenuLabelScale = 2f;

		Label lGameStart = new Label("Game Start", skin);
		Label lLoad = new Label("Load", skin);
		Label lExit = new Label("Exit", skin);
		
		lGameStart.setFontScale(mainMenuLabelScale);
		lLoad.setFontScale(mainMenuLabelScale);
		lExit.setFontScale(mainMenuLabelScale);

		//Add Behavior for buttons
		//Implements highlighting the buttons on mouseOver
		class MainMenuLabelListener extends ActorListener {
			public MainMenuLabelListener(Actor a) {
				super(a);
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				if (pointer == -1) {
					Label l = (Label) a;
					l.setColor(Color.CHARTREUSE);
				}
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (pointer == -1) {
					Label l = (Label) a;
					l.setColor(Color.WHITE);
				}
			}

		}

		lGameStart.addListener(new MainMenuLabelListener(lGameStart) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				System.out.println("clicked start button");
				((Game) Gdx.app.getApplicationListener()).setScreen(new GameScreen());
			}
		});
		lLoad.addListener(new MainMenuLabelListener(lLoad){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				stage.addActor(loadFileChooser.fadeIn());
			}
		});
		lExit.addListener(new MainMenuLabelListener(lExit) {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});

		g.addActor(lGameStart);
		g.addActor(lLoad);
		g.addActor(lExit);
		table.add(g);

		stage.addActor(table);

	}
	
	void loadFileChoosers(){
		//Load file chooser
		FileChooser.setDefaultPrefsName("holowyth.test.ui.filechooser");
		
		loadFileChooser = new FileChooser(Mode.OPEN);
		loadFileChooser.setSelectionMode(SelectionMode.FILES);
		loadFileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> file) {
				System.out.println("Selected file: " + file.get(0).file().getAbsolutePath());
			}
		});
		
		saveFileChooser = new FileChooser(Mode.SAVE);
		saveFileChooser.setSelectionMode(SelectionMode.FILES);
		saveFileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected (Array<FileHandle> file) {
				System.out.println("Selected file to save to to " + file.get(0).file().getAbsolutePath());
			}
		});
		
	}

	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render(float deltaTime) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void dispose() {
		stage.dispose();
	}

	@Override
	public void show() {
		InputMultiplexer im = new InputMultiplexer(stage);
		Gdx.input.setInputProcessor(im);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

}
