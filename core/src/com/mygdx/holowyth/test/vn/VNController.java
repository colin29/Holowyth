package com.mygdx.holowyth.test.vn;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.wizered67.game.VNHubManager;
import com.wizered67.game.conversations.Conversation;
import com.wizered67.game.conversations.ConversationController;
import com.wizered67.game.gui.GUIManager;
import com.wizered67.game.inputs.ControlInputAdapter;

/**
 * Is not actually a screen
 * 
 * Only one instance should be used as the underlying VN library uses static manager methods
 * 
 * @author Colin
 *
 */




public class VNController {
	
	
	private BitmapFont font;

	
	private final InputMultiplexer parentMultiplexer;
	private final InputMultiplexer vnMultiplexer = new InputMultiplexer();
	
	/**
	 * VN modules
	 */
	GUIManager guiManager;
	ConversationController conversationController;


	private boolean visible = false;
	
	/**
	 * Adds its own processors to multiplexer
	 */
	public VNController(Stage vnstage, SpriteBatch batch, OrthographicCamera camera, InputMultiplexer parentMultiplexer) {
		
		this.parentMultiplexer = parentMultiplexer;
		
		new VNHubImpl(vnstage, batch, camera, vnMultiplexer); // VNHubManager is active after this
		
		vnMultiplexer.addProcessor(VNHubManager.guiManager().getStage());
		vnMultiplexer.addProcessor(new ControlInputAdapter(VNHubManager.conversationController()));
		vnMultiplexer.addProcessor(new ControlInputAdapter(VNHubManager.guiManager()));
		
		initFont();
		
		guiManager = VNHubManager.guiManager();
		conversationController = guiManager.conversationController();
	}
	


	private void initFont() {
		font = new BitmapFont(false);
		font.setColor(Color.WHITE);
	}
	
	public void startConversation(String convoName, String branch) {
		conversationController.setConv(VNHubManager.assetManager().get(convoName, Conversation.class));
		conversationController.setBranch(branch);
	}
	
	/**
	 * Note: The VN instance uses and draws on its OWN stage
	 */
	public void updateAndRenderIfVisible(float delta) {
		if(visible ) {
			VNHubManager.mainViewport().apply();
			VNHubManager.guiManager().updateAndRender(delta);	
		}
	}
	public void resize(int width, int height) {
		Camera viewportCamera = VNHubManager.mainViewport().getCamera();
		Vector3 centerVector = new Vector3(viewportCamera.viewportWidth / 2, viewportCamera.viewportHeight / 2,
				viewportCamera.position.z);
		Vector3 offset = viewportCamera.position.cpy().sub(centerVector);
		
		VNHubManager.mainViewport().update(width, height, true);
		// todo make sure cameras get updated when changing scene
		// Centers the camera and then offsets it by the difference between the previous viewport center and
		// the camera position
		viewportCamera.position.add(offset);
		viewportCamera.update();
		
		VNHubManager.guiViewport().update(width, height);
		VNHubManager.guiManager().resize(width, height);
	}
	
	/** Use this to control priority of input, if you want more fine grained control
	 */
	public InputMultiplexer getInputMultiplexer() {
		return vnMultiplexer;
	}
	
	/**
	 * Adds the input processing to the parent multiplexer and cause the VN to start updating and rendering
	 */
	public void show() {
		parentMultiplexer.addProcessor(0, vnMultiplexer); //adds this vn's controls at the highest priority
		this.visible = true;
	}
	
	public void hide() {
		parentMultiplexer.removeProcessor(vnMultiplexer);
		this.visible = false;
	}
	
	public void setConvoExitListener(Runnable r) {
		conversationController.setConvoExitListener(r);
	}
	
}
