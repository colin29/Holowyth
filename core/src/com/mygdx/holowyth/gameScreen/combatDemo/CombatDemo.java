package com.mygdx.holowyth.gameScreen.combatDemo;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.ai.AIModule;
import com.mygdx.holowyth.gameScreen.GameScreenBase;
import com.mygdx.holowyth.gameScreen.baseScreens.TiledMapLoadingScreen;
import com.mygdx.holowyth.gameScreen.combatDemo.prototyping.CombatPrototyping;
import com.mygdx.holowyth.gameScreen.rendering.Renderer;
import com.mygdx.holowyth.gameScreen.ui.GameBaseUI;
import com.mygdx.holowyth.gameScreen.ui.GameLogDisplay;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.sprite.Animations;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.tools.FunctionBindings;
import com.mygdx.holowyth.util.tools.Timer;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;

/**
 * The main class that runs the game.
 * 
 * Is responsible for setting up the all the other components.
 * 
 * @author Colin Ta
 *
 */
public class CombatDemo extends GameScreenBase implements Screen, InputProcessor {

	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	

	private CombatPrototyping testing;
	

	private enum GameState {
		PLAYING, VICTORY, DEFEAT;
		public boolean isComplete() {
			return this != PLAYING;
		}
	}

	private GameState gameState = GameState.PLAYING;

	public CombatDemo(final Holowyth game) {
		super(game);
		loadGameMapByName("foo");
	}

	@Override
	public void render(float delta) {
		super.render(delta);
	}
	
	@Override
	protected void tickGame() {
		super.tickGame();
		handleGameOver();
	}

	private void handleGameOver() {
		var units = world.getUnits();

		if (gameState == GameState.PLAYING) {
			if (!IterableUtils.matchesAny(units, u -> u.getSide().isPlayer())) {
				onDefeat();
			} else if (!IterableUtils.matchesAny(units, u -> u.getSide().isEnemy())) {
				onVictory();
			}
		}

	}

	private void onVictory() {
		gameState = GameState.VICTORY;
		showVictoryPanel();
	}

	private void onDefeat() {
		gameState = GameState.DEFEAT;
		showDefeatPanel();
	}

	private final Table victoryPanel = new Table();
	private final Table defeatPanel = new Table();
	private final Table instructionsPanel = new Table();
	{
		createVictoryPanel();
		createDefeatPanel();
		createInstructionsPanel();
	}

	private void createVictoryPanel() {
		var largeStyle = new LabelStyle(Holowyth.fonts.borderedLargeFont(), Color.WHITE);
		var medStyle = new LabelStyle(Holowyth.fonts.borderedMediumFont(), Color.WHITE);

		Table frame = new Table();
		Label mainText = new Label("Victory!", largeStyle);
		victoryPanel.add(mainText).size(275, 200);
		victoryPanel.row();
		victoryPanel.add(new Label("Press T to restart", medStyle));

		victoryPanel.setBackground(HoloUI.getSolidBG(Color.DARK_GRAY, 0.9f));
		// victoryPanel.setVisible(false);
		mainText.setAlignment(Align.center);
		victoryPanel.center();
		frame.add(victoryPanel);
		stage.addActor(frame);
		frame.setFillParent(true);

		victoryPanel.setVisible(false);
	}

	private void createDefeatPanel() {
		var largeStyle = new LabelStyle(Holowyth.fonts.borderedLargeFont(), Color.WHITE);
		var medStyle = new LabelStyle(Holowyth.fonts.borderedMediumFont(), Color.WHITE);

		Table frame = new Table();
		Label mainText = new Label("Defeat", largeStyle);
		defeatPanel.add(mainText).size(275, 200);
		defeatPanel.row();
		defeatPanel.add(new Label("Press T to retry", medStyle));

		defeatPanel.setBackground(HoloUI.getSolidBG(Color.DARK_GRAY, 0.9f));
		// victoryPanel.setVisible(false);
		mainText.setAlignment(Align.center);
		defeatPanel.center();
		frame.add(defeatPanel);
		stage.addActor(frame);
		frame.setFillParent(true);

		defeatPanel.setVisible(false);
	}

	private final String instructionsText = "Controls:\n" +
			"Select Units: Left-Click or Left-click drag\n" +
			"Confirm Order Location/Target: Left-click\n" +
			"Order Move: Right-click\n" +
			"Order Attack: A\n" +
			"Order Retreat: R\n" +
			"Order Stop: S\n" +
			"\n" +
			"Use Skills: 1-9, 0\n" +
			"\n" +
			"Pause Time: Space   (important!)\n" +
			"Pan Camera: Arrow Keys\n" +
			"\n" +
			"Status effects:\n" +
			"Stun: Major atk/def penalty, cannot take any action\n" +
			"Reel: Moderate atk/def penalty, is slowed.\n" +
			"Blind: Prevents units from casting skills, and also interrupts ranged skills.";

	private void createInstructionsPanel() {
		var style = new LabelStyle(Holowyth.fonts.debugFont(), Color.WHITE);
		var panel = instructionsPanel;

		Table frame = new Table();

		var l1 = new Label("Instructions  (Press Q to bring up at any time)", style);
		panel.add(l1);
		panel.row();
		var l2 = new Label("", style);
		panel.add(l2);
		panel.row();

		Label mainText = new Label(instructionsText, style);
		mainText.setWrap(true);
		panel.add(mainText).size(400, 400);

		panel.setBackground(HoloUI.getSolidBG(Color.DARK_GRAY, 0.9f));
		// victoryPanel.setVisible(false);
		mainText.setAlignment(Align.left);
		panel.center();

		frame.add(panel);
		stage.addActor(frame);
		frame.setFillParent(true);

		panel.pad(20);
		panel.setVisible(false);
	}

	/**
	 * Show victory panel after a short delay
	 */
	private void showVictoryPanel() {
		victoryPanel.addAction(sequence(delay(2), run(() -> victoryPanel.setVisible(true))));
	}

	/**
	 * Show victory panel after a short delay
	 */
	private void showDefeatPanel() {
		defeatPanel.addAction(sequence(delay(2), run(() -> defeatPanel.setVisible(true))));
	}

	private void showInstructionsPanel() {
		instructionsPanel.setVisible(true);
		pauseGame();
	}

	private void hideInstructionsPanel() {
		instructionsPanel.setVisible(false);
		unpauseGame();
	}

	private void restartLevel() {

		logger.debug("Restarted level!");

		gameState = GameState.PLAYING;

		victoryPanel.setVisible(false);
		defeatPanel.setVisible(false);

		world.clearAllUnits();
		controls.clearSelectedUnits();

		testing.setupPlannedScenario();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
	}


	@Override
	protected final void mapStartup() {
		super.mapStartup();
		testing = new CombatPrototyping(world, controls); // have to initialize here because world module only exists after gameScreen startsup the map.
		testing.setupPlannedScenario();
		showInstructionsPanel();
	}

	@Override
	protected final void mapShutdown() {
		super.mapShutdown();
	}

	/* Input methods */

	@Override
	public boolean keyDown(int keycode) {
		super.keyDown(keycode);
		
		if (keycode == Keys.T) {
			if (gameState.isComplete()) {
				restartLevel();
			}
		}
		if (keycode == Keys.Q) {
			if (instructionsPanel.isVisible()) {
				hideInstructionsPanel();
			} else {
				showInstructionsPanel();
			}
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.RIGHT && pointer == 0) {
			if (instructionsPanel.isVisible())
				hideInstructionsPanel();
		}
		return false;
	}



}
