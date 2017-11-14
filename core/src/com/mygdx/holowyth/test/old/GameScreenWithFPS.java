package com.mygdx.holowyth.test.old;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.holowyth.Holowyth;

public class GameScreenWithFPS implements Screen {
    final Holowyth game;

    /*Rendering and pipeline variables*/
    OrthographicCamera camera;
    ShapeRenderer shapeRenderer;
    SpriteBatch batch;
    
    /*Resource variables*/
    
    
    public GameScreenWithFPS(final Holowyth game) {
        this.game = game;
        loadImages();
        loadAndConfigureAudio();

        // create a camera for this screen.	
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 960, 640);
        
    	shapeRenderer = game.shapeRenderer;
    	batch = game.batch;
    	
        initializeGameLogic();

    }

    Texture playerSprite;
	private void loadImages() {
		//playerSprite = new Texture(Gdx.files.internal("icons\\200px\\blue.png"));
	}

	private void loadAndConfigureAudio() {
	}
	
	private void initializeGameLogic() { 
	}
	
	/* Variables for enforcing fixed fps */
	final private long INITIAL_TIME = System.nanoTime();
	private long timeElapsed = 0;
	private long ticksPerSecond = 60;
	private long timeBetweenTicks = 1000000000/ticksPerSecond; 
	private long timeTillNextTick=0;
	private int maxConsecutiveTicks = 1; //normally is 3

	private void runLogicAtFixedFPS() {
		/* Game logic tick timing control */
        
        //Run game logic if enough time has elapsed. If render is slow, run game logic up to three times
        timeElapsed = System.nanoTime() - INITIAL_TIME;
        timeTillNextTick -= Gdx.graphics.getRawDeltaTime()*1000000000;
        
        int i;
        for(i=0; (timeTillNextTick<=0 && i<maxConsecutiveTicks); i++){
        	timeTillNextTick += timeBetweenTicks;
        }
        
        calculateLogicFPS(i);
//        if(timeTillNextTick <=0){ //don't attempt to continue catching up after a long render or streak of renders.
//        	timeTillNextTick = 0;
//        }
	}

	private long lastTime =  INITIAL_TIME;
    private double logicFps = 0;
    
	private void calculateLogicFPS(int i) {
		if(i>0){
        logicFps = -1000000000.0 / (lastTime - (lastTime = System.nanoTime()) * i); 
        }
	}
    
    @Override
    public void render(float delta) {
    	
        // Clear the screen
        Gdx.gl.glClearColor(0.8f, 1f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));

        
        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the coordinate system specified by the camera.
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        batch.begin();
        batch.end();
 

        runLogicAtFixedFPS();
    }

	
    
	/* Drawing helper functions */
	
	private void drawFPSCounter() {
		//Draw FPS Counter
        String fps = "FPS: " + String.valueOf(Math.round(logicFps));
        game.font.setColor(Color.BLACK);
        game.font.draw(batch, fps , game.resX-60 , 40/*, 0, fps.length(), 60, Align.left, false, ""*/);
	}

	//Screen Override functions
	@Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }
    @Override
    public void dispose() {
    }

    //pause() and resume() are unused (only for Android OS)
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

 

}