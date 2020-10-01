package com.wizered67.vnlib;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.wizered67.vnlib.assets.Assets;
import com.wizered67.vnlib.conversations.Conversation;
import com.wizered67.vnlib.conversations.ConversationController;
import com.wizered67.vnlib.gui.GUIManager;
import com.wizered67.vnlib.inputs.Controls;
import com.wizered67.vnlib.saving.SaveManager;
import com.wizered67.vnlib.screens.LoadingScreen;
import com.wizered67.vnlib.screens.MainGameScreen;

public class MainGame extends Game implements VNHub{
    
    //Modules
    private GUIManager guiManager;
    private ConversationController conversationController;
    private Controls controls;
    private Assets assetManager;
	private MusicManager musicManager;
    
	private MainGameScreen gameScreen; 
	
	//Resources
	private InputMultiplexer inputMultiplexer;
	private SpriteBatch mainBatch;
	private OrthographicCamera mainCamera;
	private Viewport mainViewport;
	private Viewport guiViewport;

	@Override
	public void create() {
		VNHubManager.init(this);

		initInput();

		assetManager = new Assets();
        musicManager = new MusicManager();
        mainBatch = new SpriteBatch();

        mainCamera = new OrthographicCamera();
        mainCamera.setToOrtho(false);
        mainViewport = new ExtendViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT,
				Constants.MAX_VIEWPORT_WORLD_WIDTH, Constants.MAX_VIEWPORT_WORLD_HEIGHT);
        guiViewport = new ScreenViewport();

        guiManager = new GUIManager(new Stage(guiViewport));
        conversationController = guiManager.conversationController();
		SaveManager.init();
		VNHubManager.assetManager().loadGroup("common");
		gameScreen = new MainGameScreen();
		setScreen(new LoadingScreen(new LoadingScreen.LoadResult() {
			@Override
			public void finishLoading() {
				setScreen(gameScreen);
				
				final String convoToLoad = "myConv.conv";  //"demonstration.conv"
				
				conversationController.setConv(VNHubManager.assetManager().get(convoToLoad, Conversation.class));
				conversationController.setBranch("default");
			}
		}));
	}

	private void initInput() {
		controls = new Controls();
		inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);
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
		return inputMultiplexer;
	}

	@Override
	public SpriteBatch mainBatch() {
		return mainBatch;
	}

	@Override
	public OrthographicCamera mainCamera() {
		return mainCamera;
	}
	
}
