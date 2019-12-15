package com.mygdx.holowyth.combatDemo.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.combatDemo.CombatDemo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugStoreUI;

public class CombatDemoUI {

	// Sub-components
	private DebugStoreUI debugStoreUI;
	private SkillBarUI skillBarUI;
	private StatsPanelUI statsPanelUI;

	// Widgets
	private Label coordText;

	// Debugging
	private DebugStore debugStore;

	private Stage stage;
	private Table root = new Table();

	private Skin skin;

	private GameLog gameLog;

	private CombatDemo self;

	public CombatDemoUI(Stage stage, DebugStore debugStore, Skin skin, CombatDemo combatDemo) {
		this.skin = skin;
		this.stage = stage;
		this.debugStore = debugStore;
		this.self = combatDemo;

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

	/**
	 * Adds a small coordinate text that displays the mouse cursor position in world coordinates
	 */
	private void createCoordinateText() {
		coordText = new Label("(000, 000)\n", skin);
		coordText.setColor(Color.BLACK);
		stage.addActor(coordText);
		coordText.setPosition(Gdx.graphics.getWidth() - coordText.getWidth() - 4, 4);
	}

	public Label getCoordInfo() {
		return coordText;
	}

	/**
	 * Purely exposed for testing purposes, do not use otherwise
	 */
	public Table getDebugInfo() {
		return debugStoreUI.getDebugInfo();
	}

	public void onMapStartup() {
		debugStoreUI.populateDebugValueDisplay();
		skillBarUI = new SkillBarUI(stage, debugStore, skin, self.getControls());
		statsPanelUI = new StatsPanelUI(stage, skin);
		self.getControls().addListener(getStatsPanelUI());
	}

	public void onMapShutdown() {
		skillBarUI.remove();
		getStatsPanelUI().remove();
	}

	public void onRender() {
		stage.setDebugAll(Gdx.input.isKeyPressed(Keys.B));
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

	public StatsPanelUI getStatsPanelUI() {
		return statsPanelUI;
	}

}
