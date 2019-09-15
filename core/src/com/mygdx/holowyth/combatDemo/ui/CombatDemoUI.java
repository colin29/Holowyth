package com.mygdx.holowyth.combatDemo.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugStoreUI;

public class CombatDemoUI {

	// Sub-components
	DebugStoreUI debugStoreUI;

	// Widgets
	Window parameterWindow;
	Label coordInfo;

	private Stage stage;
	private Table root = new Table();

	Skin skin;

	private GameLog gameLog;

	public CombatDemoUI(Stage stage, DebugStore debugStore, Skin skin) {
		this.skin = skin;
		this.stage = stage;
		debugStoreUI = new DebugStoreUI(debugStore);

		gameLog = new GameLog(stage);

		createUI();
	}

	private void createUI() {
		setupStage();
		createCoordinateText();

		root.add(debugStoreUI.getDebugInfo());
	}

	private void setupStage() {
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();
		stage.addActor(root);

		// root.debug();
	}

	@SuppressWarnings("unused")
	/**
	 * A parameter window produces a slider which can be used to adjust variables dynamically
	 */
	private void createParameterWindow() {

		parameterWindow = new Window("Parameters", skin);
		parameterWindow.setPosition(0, 100);

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

	/**
	 * Purely exposed for testing purposes, do not use otherwise
	 */
	public Table getDebugInfo() {
		return debugStoreUI.getDebugInfo();
	}

	public void onMapStartup() {
		debugStoreUI.populateDebugValueDisplay();
	}

	public void onRender() {
		debugStoreUI.updateDebugValueDisplay();
	}

	public void updateMouseCoordLabel(int screenX, int screenY, Camera camera) {
		Point p = MiscUtil.getCursorInWorldCoords(camera);
		getCoordInfo().setText(
				"(" + (int) (p.x) + ", " + (int) (p.y) + ")\n" + "(" + (int) (p.x) / Holo.CELL_SIZE + ", "
						+ (int) (p.y) / Holo.CELL_SIZE + ")");

	}

	public GameLog getGameLog() {
		return gameLog;
	}

}
