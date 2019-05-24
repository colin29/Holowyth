package com.mygdx.holowyth.pathfinding.demo;

import java.util.ArrayList;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

/**
 *
 * Simpler UnitControls class for pathfinding Demo. Units can be ordered to move and attack, but no additional commands.
 *
 * @author Colin Ta
 */
public class PFUnitControls implements InputProcessor {

	Holowyth game;

	Camera camera;
	ShapeRenderer shapeRenderer;

	ArrayList<PFDemoUnit> units;

	public ArrayList<PFDemoUnit> selectedUnits = new ArrayList<PFDemoUnit>();
	boolean leftMouseKeyDown = false;

	PFDemoUnit prospectUnit; // In order to single-select a unit, the user must mouse down on a unit, and mouse up on
								// the same
	// unit.

	boolean attackClickWaiting;

	// For displaying Debug info
	BitmapFont font;
	Skin skin;
	LabelStyle labelStyle;

	Table table;
	Label currentStateText;

	public PFUnitControls(Holowyth game, Camera camera, ArrayList<PFDemoUnit> units) {
		this.shapeRenderer = game.shapeRenderer;
		this.camera = camera;
		this.units = units;

		this.font = game.fonts.debugFont();
		this.skin = game.skin;

		labelStyle = new LabelStyle(game.fonts.debugFont(), Holo.debugFontColor);

		createDebugTable();
	}

	private void createDebugTable() {
		table = new Table();
		currentStateText = new Label("Initial Value", labelStyle);
		table.add(currentStateText);
	}

	/** Gets debug info for this module, which the parent is free to display how they wish */
	public Table getDebugTable() {
		return table;
	}

	float clickX, clickY; // world location of the last recorded click.
	float clickX2, clickY2; // location of opposite point (of the selection box)

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.A && selectedUnits.size() > 0) {
			clearAwaitingOrders();
			currentStateText.setText("Select Attack Target");
			attackClickWaiting = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		leftMouseKeyDown = false;

		Vector3 vec = new Vector3(); // obtain world coordinates of the click.
		vec = camera.unproject(vec.set(screenX, screenY, 0));

		// Handle Left Click
		if (button == Input.Buttons.LEFT && pointer == 0) {
			System.out.println("Left click detected");
			if (attackClickWaiting) {
				handleLeftClickAttack(vec.x, vec.y);
			} else {
				handleLeftClickSelect(vec.x, vec.y);
			}
			return true;
		}

		// Handle Right Click
		if (button == Input.Buttons.RIGHT && pointer == 0) {
			handleRightClickMove(vec.x, vec.y);
			return true;
		}

		return false;
	}

	/**
	 * Makes it so when you are part-way through an order, and then the start of a separate order, the game will stop
	 * waiting for the first one
	 */
	private void clearAwaitingOrders() {
		attackClickWaiting = false;
		currentStateText.setText("Idle");
	}

	private void handleRightClickMove(float x, float y) {
		clearAwaitingOrders();
		Point p = new Point(x, y);
		for (PFDemoUnit u : selectedUnits) {
			u.orderMove(p.x, p.y);
		}
	}

	private void handleLeftClickAttack(float x, float y) {
		clearAwaitingOrders();
		Point p1 = new Point(x, y);
		Point p2 = new Point();
		float dist;
		// select a unit if there is one underneath this point. If there are multiple units, select the one that
		// occurs last (on top)
		PFDemoUnit lastResult = null;

		for (PFDemoUnit u : units) {
			p2.set(u.x, u.y);
			dist = Point.calcDistance(p1, p2);
			if (dist <= u.getRadius()) {
				lastResult = u;
			}
			// check distance of the click to the center of the circle
		}

		PFDemoUnit target = null;
		if (lastResult != null) {
			// select this unit
			target = lastResult;
		}
		if (target != null) {
			for (PFDemoUnit u : selectedUnits) {
				u.orderAttackUnit(target);
			}
		}

	}

	private void handleLeftClickSelect(float x, float y) {
		Point p1 = new Point(x, y);
		Point p2 = new Point();
		float dist;
		// select a unit if there is one underneath this point. If there are multiple units, select the one that
		// occurs last (on top)
		PFDemoUnit lastResult = null;

		for (PFDemoUnit u : units) {
			p2.set(u.x, u.y);
			dist = Point.calcDistance(p1, p2);
			if (dist <= u.getRadius()) {
				lastResult = u;
			}
			// check distance of the click to the center of the circle
		}

		if (lastResult != null) {
			// select this unit
			selectedUnits.clear();
			selectedUnits.add(lastResult);
		}

		// Mark point of click for dragging purposes.
		clickX = p1.x;
		clickY = p1.y;

		clickX2 = p1.x;
		clickY2 = p1.y;

		leftMouseKeyDown = true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {

		if (button == Input.Buttons.LEFT && pointer == 0) {
			Vector3 vec = new Vector3(); // obtain world coordinates of the click.
			vec = camera.unproject(vec.set(screenX, screenY, 0));

			if (leftMouseKeyDown) {
				Point p = new Point(vec.x, vec.y);

				clickX2 = p.x;
				clickY2 = p.y;

				float x = Math.min(clickX, clickX2);
				float y = Math.min(clickY, clickY2);
				float x2 = Math.max(clickX, clickX2);
				float y2 = Math.max(clickY, clickY2);

				// check if unit circles are inside or touching the selection box.

				ArrayList<PFDemoUnit> newlySelected = new ArrayList<PFDemoUnit>();

				for (PFDemoUnit u : units) {
					if (u.x >= x - u.getRadius() && u.x <= x2 + u.getRadius() && u.y >= y - u.getRadius()
							&& u.y <= y2 + u.getRadius()) {
						newlySelected.add(u);
					}
				}

				if (!newlySelected.isEmpty()) {
					selectedUnits.clear();
					selectedUnits.addAll(newlySelected);
				}
			}

			leftMouseKeyDown = false;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (pointer == 0 && leftMouseKeyDown) {
			Vector3 vec = new Vector3(); // obtain world coordinates of the click.
			vec = camera.unproject(vec.set(screenX, screenY, 0));
			Point p = new Point(vec.x, vec.y);
			clickX2 = p.x;
			clickY2 = p.y;
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Renders circles around selected units
	 */
	public void renderCirclesOnSelectedUnits() {
		for (PFDemoUnit u : selectedUnits) {
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 2.5f, Color.GREEN);
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 4, Color.GREEN);
		}
	}

	public void renderDebuggingText() {
		// TODO:
	}

	public static Color defaultSelectionBoxColor = Color.BLUE;

	public void renderSelectionBox(Color color) {
		if (leftMouseKeyDown) {
			shapeRenderer.setColor(color);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.rect(Math.min(clickX, clickX2), Math.min(clickY, clickY2), Math.abs(clickX2 - clickX),
					Math.abs(clickY2 - clickY));
			shapeRenderer.end();
		}
	}

}
