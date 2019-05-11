package com.mygdx.holowyth.combatDemo.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.mygdx.holowyth.util.debug.DebugStore;

public class CombatDemoUI {

	// Sub-components
	DebugStoreUI debugStoreUI;

	// Widgets
	Window parameterWindow;
	Label coordInfo;

	private Stage stage;
	private Table root = new Table();

	Skin skin;

	public CombatDemoUI(Stage stage, DebugStore debugStore, Skin skin) {
		this.skin = skin;
		this.stage = stage;
		debugStoreUI = new DebugStoreUI(stage, debugStore);
		createUI();
	}

	private void createUI() {
		setupStage();

		createCoordinateText();
		debugStoreUI.createDebugInfoDisplay();
	}

	private void setupStage() {
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();
		stage.addActor(root);

		root.debug();
	}

	@SuppressWarnings("unused")
	/**
	 * A parameter window produces a slider which can be used to adjust variables dynamically
	 */
	private void createParameterWindow() {

		parameterWindow = new Window("Parameters", skin);
		parameterWindow.setPosition(0, 100);

		// root.add(new TextButton("test", skin));
		stage.addActor(parameterWindow);
		// HoloUI.parameterSlider(0, Holo.defaultUnitMoveSpeed,
		// "initialMoveSpeed", testing, skin,
		// (Float f) -> playerUnit.initialMoveSpeed = f);
		parameterWindow.pack();
	}

	/**
	 * Adds a small coordinate text that displays the mouse cursor position in world coordinates
	 */
	private void createCoordinateText() {
		coordInfo = new Label("(000, 000)\n", skin);
		coordInfo.setColor(Color.BLACK);
		stage.addActor(coordInfo);
		coordInfo.setPosition(Gdx.graphics.getWidth() - coordInfo.getWidth() - 4, 4);
	}

	public Window getParameterWindow() {
		return parameterWindow;
	}

	public Label getCoordInfo() {
		return coordInfo;
	}

	public Table getDebugInfo() {
		return debugStoreUI.getDebugInfo();
	}

	public void onMapStartup() {
		debugStoreUI.populateDebugValueDisplay();
	}

	public void onRender() {
		debugStoreUI.updateDebugValueDisplay();
	}

}
