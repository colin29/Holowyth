package com.mygdx.holowyth.knockback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

/**
 * Responsible for creation and update of UI
 * 
 * @author Colin Ta
 *
 */
public class KnockBackDemoUI {

	// Sub-components
	DebugStoreUI debugStoreUI;

	// Widgets

	/**
	 * A small text that displays the mouse cursor position in world coordinates
	 */
	Label coordInfo;

	private Stage stage;
	private Table root = new Table();

	Skin skin;

	Camera worldCamera;

	/**
	 * UI class is responsible for creation and updating of UI elements
	 */
	public KnockBackDemoUI(Stage stage, DebugStore debugStore, Skin skin, Camera worldCamera) {
		this.skin = skin;
		this.stage = stage;
		debugStoreUI = new DebugStoreUI(stage, debugStore);
		createUI();

		this.worldCamera = worldCamera;
	}

	private void createUI() {
		setupStage();

		createCoordinateText();
		debugStoreUI.populateDebugValueDisplay();
		root.add(debugStoreUI.getDebugInfo());
	}

	private void setupStage() {
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();
		stage.addActor(root);
	}

	private void createCoordinateText() {
		coordInfo = new Label("(000, 000)\n", skin);
		coordInfo.setColor(Color.BLACK);
		stage.addActor(coordInfo);
		coordInfo.setPosition(Gdx.graphics.getWidth() - coordInfo.getWidth() - 4, 4);
	}

	public void onRender() {
		debugStoreUI.updateDebugValueDisplay();
	}

	public void updateMouseCoordLabel(int screenX, int screenY, Camera camera) {
		Point p = MiscUtil.getCursorInWorldCoords(camera);
		coordInfo.setText("(" + (int) (p.x) + ", " + (int) (p.y) + ")\n" + "(" + (int) (p.x) / Holo.CELL_SIZE + ", "
				+ (int) (p.y) / Holo.CELL_SIZE + ")");

	}

	public void onMapStartup() {
	}

	InputProcessor getInputProcessor() {
		return inputProcessor;
	}

	public Table getDebugInfo() {
		return debugStoreUI.getDebugInfo();
	}

	private final InputProcessor inputProcessor = new InputProcessorAdapter() {
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			updateMouseCoordLabel(screenX, screenY, worldCamera);
			return false;
		}
	};

}
