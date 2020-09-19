package com.mygdx.holowyth.combatDemo.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.mygdx.holowyth.util.template.GameScreenBase;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugStoreUI;

public class GameScreenBaseUI {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

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

	private GameScreenBase self;

	public GameScreenBaseUI(Stage stage, DebugStore debugStore, Skin skin, GameScreenBase self) {
		this.skin = skin;
		this.stage = stage;
		this.debugStore = debugStore;
		this.self = self;

		debugStoreUI = new DebugStoreUI(debugStore);
		gameLog = new GameLog(stage);

		createUI();
	}

	private void createUI() {
		setupStage();
		createCoordinateText();
		root.add(debugStoreUI.getDebugValuesTable());
	}

	private void setupStage() {
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();
		stage.addActor(root);
	}

	/**
	 * Adds a small coordinate text that displays the mouse cursor position in world coordinates
	 */
	private void createCoordinateText() {
		coordText = new Label("(000, 000)\n (0, 0)", skin);
		coordText.setColor(Color.BLACK);
		stage.addActor(coordText);
		coordText.setPosition(Gdx.graphics.getWidth() - coordText.getWidth() - 4, 4);

		coordText.setVisible(Holo.debugShowMouseLocationText);
	}

	public Label getCoordInfo() {
		return coordText;
	}


	public void setVisibleDebugValues(boolean visible) {
		debugStoreUI.getDebugValuesTable().setVisible(visible);
	}
	public boolean isDebugValuesVisible() {
		return debugStoreUI.getDebugValuesTable().isVisible();
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

	public void render() {
		stage.setDebugAll(Gdx.input.isKeyPressed(Keys.C));
		debugStoreUI.updateDebugValueDisplay();

		skillBarUI.update();
		skillBarUI.render(self.getCameras(), self.batch, self.shapeDrawer, self.assets);
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
