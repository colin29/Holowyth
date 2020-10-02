package com.wizered67.game;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.wizered67.game.assets.Assets;
import com.wizered67.game.conversations.ConversationController;
import com.wizered67.game.gui.GUIManager;
import com.wizered67.game.inputs.Controls;

/**
 * Central hub that holds references to modules and resources.
 * @author Colin
 *
 */
public interface VNHub {

	OrthographicCamera mainCamera();

	SpriteBatch mainBatch();

	InputMultiplexer inputMultiplexer();

	MusicManager musicManager();

	Assets assetManager();

	Controls controls();

	ConversationController conversationController();

	GUIManager guiManager();

	Viewport guiViewport();

	Viewport mainViewport();

}
