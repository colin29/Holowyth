package com.mygdx.holowyth.test.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SceneTest extends Game implements InputProcessor {

	private Stage stage;
	private Table table;

	public void create () {
//		camera = new OrthographicCamera();
//		camera.setToOrtho(false, game.resX, game.resY);
		
		
		
		stage = new Stage(new ScreenViewport());
		
		InputMultiplexer im = new InputMultiplexer(stage);
		Gdx.input.setInputProcessor(im);
		

		Skin skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));

		
		table = new Table();//.debug();
		
		table.align(Align.top|Align.right);
		
		final int tableWidth = Gdx.graphics.getWidth()/4;
		final int tableHeight = Gdx.graphics.getHeight()/2;
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
		
		 class MainMenuLabelListener extends ActorListener{
			public MainMenuLabelListener(Actor a) {
				super(a);
			}
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor){
				if(pointer == -1){
				Label l = (Label) a;
				l.setColor(Color.CHARTREUSE);
				}
			}
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor){
				if(pointer == -1){
					Label l = (Label) a;
					l.setColor(Color.WHITE);
				}
			}
			 
		 }
		lGameStart.addListener(new MainMenuLabelListener(lGameStart));
		lLoad.addListener(new MainMenuLabelListener(lLoad));
		lExit.addListener(new MainMenuLabelListener(lExit));
		
		g.addActor(lGameStart);
		g.addActor(lLoad);
		g.addActor(lExit);
		table.add(g);
		
		
	
		stage.addActor(table);
		
		
		
		
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void dispose() {
		stage.dispose();
	}

	//Input processing
	
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
