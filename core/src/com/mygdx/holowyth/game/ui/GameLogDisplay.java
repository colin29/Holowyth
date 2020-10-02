package com.mygdx.holowyth.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.mygdx.holowyth.Holowyth;

/**
 * A UI component for displaying messages
 * @author Colin
 *
 */
public class GameLogDisplay {

	private Table root = new Table(); // uses its seperate root

	private VerticalGroup logPanel = new VerticalGroup();

	private static LabelStyle debugStyleNormal;
	private static LabelStyle debugStyleError;

	/**
	 * Creates and attaches table to stage
	 */
	public GameLogDisplay(Stage stage) {
		root.setFillParent(true);
		stage.addActor(root);

		root.left().bottom();

		debugStyleNormal = new LabelStyle(Holowyth.fonts.debugFont(), Color.WHITE);
		debugStyleError = new LabelStyle(Holowyth.fonts.debugFont(), Color.RED);

		logPanel.left();
		logPanel.fill();

		root.add(logPanel);
	}
	

	public void addMessage(String str, boolean isErrorMessage) {
		var msg = new GameLogMessage(str, isErrorMessage);
		logPanel.addActor(msg);
		msg.addAction(Actions.sequence(Actions.delay(7f), Actions.fadeOut(0.5f), Actions.removeActor()));
	}

	public void addErrorMessage(String str) {
		addMessage(str, true);
	}

	public void addMessage(String str) {
		addMessage(str, false);
	}

	static class GameLogMessage extends Label {
		static final float timeToDisplay = 2; // in seconds
		float timeLeft = timeToDisplay;

		public GameLogMessage(CharSequence text, boolean isErrorMessage) {
			super(text, isErrorMessage ? debugStyleError : debugStyleNormal);
		}

	}

}
