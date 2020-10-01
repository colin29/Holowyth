package com.wizered67.game.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.wizered67.game.VNHubManager;
import com.wizered67.game.inputs.ControlInputAdapter;
import com.wizered67.game.inputs.Controls;

/**
 * Main Game Screen for initializing and updating GUIManager.
 * @author Adam Victor
 */
public class MainGameScreen implements Screen {
    private BitmapFont font;
    private InputMultiplexer inputMultiplexer;

    public MainGameScreen() {
        initRendering();
        initInput();
    }

    private void initRendering() {
        font = new BitmapFont(false);
        font.setColor(Color.WHITE);
    }

    private void initInput() {
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(VNHubManager.guiManager().getStage());
        inputMultiplexer.addProcessor(new ControlInputAdapter(VNHubManager.conversationController()));
        inputMultiplexer.addProcessor(new ControlInputAdapter(VNHubManager.guiManager()));
    }


    @Override
    public void show() {
        VNHubManager.addInputProcessor(inputMultiplexer);
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        updateCameras(delta);
        updateAndRenderGUI(delta);
    }

    private void updateCameras(float delta) {

    }

    private void updateAndRenderGUI(float delta){
        //hudViewport.apply(true);
        VNHubManager.mainViewport().apply();
        VNHubManager.guiManager().updateAndRender(delta);
    }

    @Override
    public void resize(int width, int height) {
        //myViewport.update(width, height);
        //hudViewport.update(width, height);
        //debugCamera.viewportWidth = Constants.toMeters(myViewport.getWorldWidth());//Constants.toMeters(width / myViewport.getScale());
        //debugCamera.viewportHeight = Constants.toMeters(myViewport.getWorldHeight());//Constants.toMeters(height / myViewport.getScale());
        //debugCamera.update();
        //GUIManager.resize(width, height);
        Camera viewportCamera = VNHubManager.mainViewport().getCamera();
        Vector3 centerVector = new Vector3(viewportCamera.viewportWidth / 2, viewportCamera.viewportHeight / 2, viewportCamera.position.z);
        Vector3 offset = viewportCamera.position.cpy().sub(centerVector);
        VNHubManager.mainViewport().update(width, height, true);
        //todo make sure cameras get updated when changing scene
        //Centers the camera and then offsets it by the difference between the previous viewport center and the camera position
        viewportCamera.position.add(offset);
        viewportCamera.update();
        //viewportCamera.position.
        VNHubManager.guiViewport().update(width, height);
        VNHubManager.guiManager().resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        VNHubManager.removeInputProcessor(inputMultiplexer);
    }

    @Override
    public void dispose() {
    }
}
