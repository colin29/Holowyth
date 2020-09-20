package com.mygdx.holowyth.gameScreen.ui;

import java.util.List;

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
import com.mygdx.holowyth.gameScreen.GameScreenBase;
import com.mygdx.holowyth.gameScreen.Controls.UnitSelectionListener;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugStoreUI;


/**
 * This overlying UI class facilitates several tasks for its componenets:
 * 
 * Setup (i.e create and add) <br>
 * Update <br>
 * Render <br>
 * Remove <br>
 * Exposes other functionality <br><br>
 * 
 * There are App-lifetime and Map-lifetime UI components <br><br>
 * 
 * Update is carried out in a few different ways: <br>
 * - Register listener <br>
 * - Expose update method <br>
 * - Update on Render <br>
 * 
 * 
 * @author Colin
 *
 */
public class GameBaseUI extends InputProcessorAdapter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	// App-lifetime UI components
	private final DebugStoreUI debugStoreUI;
	private final MouseCoordLabel mouseCoordLabel;
	private final GameLogDisplay gameLogDisplay;

	// Map-lifetime UI components
	private SkillBarUI skillBarUI;
	private StatsPanelUI statsPanelUI;
	
	/**
	 * Used to update skillBar and statsPanel
	 */
	UnitSelectionListener unitSelectionListener;
	
	// Debugging
	private final DebugStore debugStore;

	/**
	 * Reference to game screen's stage
	 */
	private final Stage stage;
	private Table root = new Table();
	private Skin skin;

	private GameScreenBase game;
	
	

	public GameBaseUI(Stage stage, DebugStore debugStore, Skin skin, GameScreenBase self) {
		this.skin = skin;
		this.stage = stage;
		this.debugStore = debugStore;
		this.game = self;

		prepStage();
		
		/* Setup App lifetime UI Elements */
		
		debugStoreUI = new DebugStoreUI(debugStore);
		root.add(debugStoreUI.getDebugValuesTable());
		
		mouseCoordLabel = new MouseCoordLabel(stage, skin);
		gameLogDisplay = new GameLogDisplay(stage);
	}

	private void prepStage() {
		root.setFillParent(true);
		stage.addActor(root);
		root.top().left();
		stage.addActor(root);
	}


	public void onMapStartup() {
		debugStoreUI.populateDebugValueDisplay();
		setupMapLifetimeUIElements();
	}
	
	private void setupMapLifetimeUIElements() {
		skillBarUI = new SkillBarUI(stage, debugStore, skin, game.getControls());
		statsPanelUI = new StatsPanelUI(stage, skin);
		
		game.getControls().addListener(unitSelectionListener = (List<UnitInfo> selectedUnits) -> {
			UnitInfo u = selectedUnits.size() == 1 ? selectedUnits.get(0) : null; 
			skillBarUI.setUnit(u);
			statsPanelUI.setUnit(u);
		});
		
	}

	public void onMapShutdown() {
		skillBarUI.remove();
		statsPanelUI.remove();
		game.getControls().removeListener(unitSelectionListener);
	}

	public void render() {
		onRenderUpdate();
		skillBarUI.draw(game.getCameras(), game.batch, game.shapeDrawer, game.assets);
		
	}
	
	private void onRenderUpdate() {
		stage.setDebugAll(Gdx.input.isKeyPressed(Keys.C));
		
		debugStoreUI.updateDebugValueDisplay();
	
		skillBarUI.update();
		statsPanelUI.update();
	}
	
	/* Exposed update methods */ 

	public void setVisibleDebugValues(boolean visible) {
		debugStoreUI.getDebugValuesTable().setVisible(visible);
	}
	
	/* Exposed non-update methods (only functionality should be exposed, not UI details) */ 
	public boolean isDebugValuesVisible() {
		return debugStoreUI.getDebugValuesTable().isVisible();
	}

	public void toggleStatsPanelDetailedView() {
		statsPanelUI.toggleDetailedView();
	}
	
	/**
	 * Gets the game log which can be used to display messages
	 */
	public GameLogDisplay getGameLog() {
		return gameLogDisplay; 
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		mouseCoordLabel.update(game.getCameras().worldCamera);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		mouseCoordLabel.update(game.getCameras().worldCamera);
		return false;
	}

}
