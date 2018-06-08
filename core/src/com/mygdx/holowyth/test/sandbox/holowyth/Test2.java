package com.mygdx.holowyth.test.sandbox.holowyth;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
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
import com.mygdx.holowyth.util.tools.FunctionBindings;

public class Test2 extends ApplicationAdapter implements InputProcessor {

	ShapeRenderer r;
	OrthographicCamera c;

	Stage stage;

	Skin skin;
	private InputMultiplexer multiplexer;

	@Override
	public void create() {
		r = new ShapeRenderer();
		c = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		createStage();
		
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);
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
		
		//Test function binding
		
		functionBindings.bindFunctionToKey(()-> {System.out.println("foobar lambda");}, Keys.G);
		functionBindings.bindFunctionToKey(()-> {testMethod();}, Keys.H);
	}
	
	private void testMethod() {
		System.out.println("foobar method");
	}
	
	
	//Make a table that fills to whole stage and add some buttons to it.
	private void createRootTable(){
		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		
		root.debug();
		
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
	
	private FunctionBindings functionBindings = new FunctionBindings();
	

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean keyUp(int keycode) {
		return functionBindings.runBoundFunction(keycode);
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









