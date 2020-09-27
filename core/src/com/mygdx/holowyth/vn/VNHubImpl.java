package com.mygdx.holowyth.vn;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.wizered67.game.Constants;
import com.wizered67.game.VNHubManager;
import com.wizered67.game.VNHub;
import com.wizered67.game.MusicManager;
import com.wizered67.game.assets.Assets;
import com.wizered67.game.conversations.ConversationController;
import com.wizered67.game.gui.GUIManager;
import com.wizered67.game.inputs.Controls;
import com.wizered67.game.saving.SaveManager;

/**
 * Responsible for init'ing modules and resources, and loading all the assets on construction.
 * 
 * After creating an instance of this, the static VNHubManager is used to access resources. Don't need to keep a reference to this.
 * 
 * @author Colin
 *
 */
public class VNHubImpl implements VNHub {
	   
    //Modules
    private GUIManager guiManager;
    private ConversationController conversationController;
    private Controls controls;
    private Assets assetManager;
	private MusicManager musicManager;
	
	//Resources to be inherited from my main game / stage
	private InputMultiplexer multiplexer;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	// Resources not sure if shared.
	private Viewport mainViewport;
	private Viewport guiViewport;

	
	public VNHubImpl(Stage stage, SpriteBatch batch, OrthographicCamera camera, InputMultiplexer multiplexer){
		

        // Fetch our shared resources
        this.batch = batch;
        this.camera = camera;
        this.multiplexer = multiplexer;
        
        //  Init modules
        
        VNHubManager.init(this);
        
        controls = new Controls();

		assetManager = new Assets();
        musicManager = new MusicManager();
        
        mainViewport = new ExtendViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT,
				Constants.MAX_VIEWPORT_WORLD_WIDTH, Constants.MAX_VIEWPORT_WORLD_HEIGHT);
        guiViewport = new ScreenViewport();

        guiManager = new GUIManager(stage);  // use our pre-existing stage
        conversationController = guiManager.conversationController();
		SaveManager.init();
		VNHubManager.assetManager().loadGroup("common");
		
		assetManager.finishLoading();
		
		// VNHub's job is finished here. All it does is init modules and load resources.
		
	}

	@Override
	public Viewport mainViewport() {
		return mainViewport;
	}

	@Override
	public Viewport guiViewport() {
		return guiViewport;
	}

	@Override
	public GUIManager guiManager() {
		return guiManager;
	}

	@Override
	public ConversationController conversationController() {
		return conversationController;
	}

	@Override
	public Controls controls() {
		return controls;
	}

	@Override
	public Assets assetManager() {
		return assetManager;
	}

	@Override
	public MusicManager musicManager() {
		return musicManager;
	}

	@Override
	public InputMultiplexer inputMultiplexer() {
		return multiplexer;
	}

	@Override
	public SpriteBatch mainBatch() {
		return batch;
	}

	@Override
	public OrthographicCamera mainCamera() {
		return camera;
	}
}
