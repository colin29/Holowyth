package com.wizered67.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.wizered67.game.assets.Assets;
import com.wizered67.game.conversations.ConversationController;
import com.wizered67.game.gui.GUIManager;
import com.wizered67.game.inputs.Controls;
import com.wizered67.game.scripting.lua.LuaGameMethods;

public class VNHubManager {
	public static VNHub game;
	public static LuaGameMethods utils;
	public static void init(VNHub g){
		game = g;
		utils = new LuaGameMethods();
	}

	public static void addInputProcessor(InputProcessor processor) {
		game.inputMultiplexer().addProcessor(processor);
	}

	public static void addInputProcessor(InputProcessor processor, int index) {
		game.inputMultiplexer().addProcessor(index, processor);
	}

	public static void removeInputProcessor(InputProcessor processor) {
		game.inputMultiplexer().removeProcessor(processor);
	}

	public static Controls getControls() {
		return game.controls();
	}

	public static Assets assetManager() {
		return game.assetManager();
	}

	public static SpriteBatch mainBatch() {
		return game.mainBatch();
	}

	public static OrthographicCamera mainCamera() {
		return game.mainCamera();
	}

	public static Viewport mainViewport() {
		return game.mainViewport();
	}

	public static Viewport guiViewport() {
		return game.guiViewport();
	}

	public static ConversationController conversationController() {
		return game.conversationController();
	}

	public static GUIManager guiManager() {
		return game.guiManager();
	}

	public static void error(String error) {
		Gdx.app.error("ERROR", error);
	}

    public static MusicManager musicManager() {
        return game.musicManager();
    }

}
