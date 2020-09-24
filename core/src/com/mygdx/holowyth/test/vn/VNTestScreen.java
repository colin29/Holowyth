package com.mygdx.holowyth.test.vn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.util.template.HoloBaseScreen;
import com.wizered67.game.VNHubManager;
import com.wizered67.game.conversations.Conversation;
import com.wizered67.game.inputs.ControlInputAdapter;

/**
 * Screen for testing out running a VN in my project. The main difference in requirments is that I already have a game (Holowyth)
 * @author Colin
 *
 */
public class VNTestScreen extends HoloBaseScreen {
	
	private InputMultiplexer multiplexer;
	
	private final VNController vn;

	public VNTestScreen(Holowyth game) {
		super(game);
		
		
		multiplexer = new InputMultiplexer();
		
		Stage vnStage = new Stage(); 
		
		vn = new VNController(vnStage, batch, camera, multiplexer);
		multiplexer.addProcessor(stage);
		
		vn.setConvoExitListener(()->{
			vn.hide();
		});
		vn.startConversation("myConv.conv", "default");
		vn.show();
		
	}
	



	@Override
	public void render(float delta) {

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		
		stage.act(delta);
		stage.draw();
		
		// Note: vn uses and draws its OWN stage
		vn.updateAndRenderIfVisible(delta);
		
		

	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void resize(int width, int height) {
		vn.resize(width, height);
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

	@Override
	public void dispose() {
	}

}
