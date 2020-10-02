package com.mygdx.holowyth.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.MiscUtil;
import com.mygdx.holowyth.util.dataobjects.Point;

public class MouseCoordLabel {

	MouseCoordLabel(Stage stage, Skin skin){
		setupCoordinateText(stage, skin);
	}
	
	private Label mouseCoordLabel;

	/**
	 * Adds a small coordinate text that displays the mouse cursor position in world
	 * coordinates
	 */
	private void setupCoordinateText(Stage stage, Skin skin) {
		mouseCoordLabel = new Label("(000, 000)\n (0, 0)", skin);
		mouseCoordLabel.setColor(Color.BLACK);
		
		stage.addActor(mouseCoordLabel);
		mouseCoordLabel.setPosition(Gdx.graphics.getWidth() - mouseCoordLabel.getWidth() - 4, 4);
		mouseCoordLabel.setVisible(Holo.debugShowMouseLocationText);
	}

	/**
	 * Doesn't need mouse pos cause it can query it statically.
	 */
	public void update(Camera worldCamera) {
		Point p = MiscUtil.getCursorInWorldCoords(worldCamera);
		mouseCoordLabel.setText("(" + (int) (p.x) + ", " + (int) (p.y) + ")\n" + "(" + (int) (p.x) / Holo.CELL_SIZE + ", "
				+ (int) (p.y) / Holo.CELL_SIZE + ")");

	}
	
}
