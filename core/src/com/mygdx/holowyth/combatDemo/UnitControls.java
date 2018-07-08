package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.ListIterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.debug.DebugStore;
import com.mygdx.holowyth.util.debug.DebugValue;
import com.mygdx.holowyth.util.debug.DebugValues;

/**
 * Accepts player input to select and order units to move (and other behaviour later on). <br>
 * 
 * Has Map Lifetime
 * 
 * @author Colin Ta
 */
public class UnitControls extends InputProcessorAdapter {

	Holowyth game;

	Camera camera;
	Camera fixedCam;
	ShapeRenderer shapeRenderer;

	ArrayList<Unit> units;

	public ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
	boolean leftMouseKeyDown = false;

	Unit prospectUnit; // In order to single-select a unit, the user must mouse down on a unit, and mouse up on the same
						// unit.

	boolean attackClickWaiting;

	// For displaying Debug info
	BitmapFont font;
	Skin skin;
	LabelStyle labelStyle;

	String currentStateText;

	public UnitControls(Holowyth game, Camera camera, Camera fixedCam, ArrayList<Unit> units, DebugStore debugStore) {
		this.shapeRenderer = game.shapeRenderer;
		this.camera = camera;
		this.fixedCam = fixedCam;
		this.units = units;

		this.font = game.debugFont;
		this.skin = game.skin;

		labelStyle = new LabelStyle(game.debugFont, Holo.debugFontColor);

		DebugValues debugValues = debugStore.registerComponent("Unit Controls");
		debugValues.add("Order Context", () -> currentStateText);
//		debugValues.add("SelectX1", () -> selectionX1);
//		debugValues.add("SelectY1", () -> selectionY1);
//		debugValues.add("SelectX2", () -> selectionX2);
//		debugValues.add("SelectY2", () -> selectionY2);

	}

	float clickX, clickY; // Current click in world coordinates

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.A && selectedUnits.size() > 0) {
			clearAwaitingOrders();
			currentStateText = "Select Attack Target";
			attackClickWaiting = true;
			return true;
		}
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
				handleAttackLeftClick(vec.x, vec.y);
			} else {
				handleLeftClick(vec.x, vec.y, screenX, screenY);
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

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {

		if (button == Input.Buttons.LEFT && pointer == 0) {
			if (leftMouseKeyDown) {
				selectAllInSelectionBox(screenX, screenY);
			}
			leftMouseKeyDown = false;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (pointer == 0 && leftMouseKeyDown) {
			Vector3 vec = new Vector3(); // obtain window coordinates of the click.
			vec = fixedCam.unproject(vec.set(screenX, screenY, 0));
			selectionX2 = vec.x;
			selectionY2 = vec.y;
		}
		return false;

	}

	/**
	 * Makes it so when you are part-way through an order, and then the start of a separate order, the game will stop
	 * waiting for the first one
	 */
	private void clearAwaitingOrders() {
		attackClickWaiting = false;
		currentStateText = "Idle";
	}

	private void handleRightClickMove(float x, float y) {
		clearAwaitingOrders();
		Point p = new Point(x, y);
		for (Unit u : selectedUnits) {
			u.orderMove(p.x, p.y);
		}
	}

	/**
	 * Handle a left-click following being in the "attack" context
	 * 
	 * @param x
	 * @param y
	 */
	private void handleAttackLeftClick(float x, float y) {
		clearAwaitingOrders();
		Point p1 = new Point(x, y);
		Point p2 = new Point();
		float dist;
		// select a unit if there is one underneath this point. If there are multiple units, select the one that
		// occurs last (on top)
		Unit target = null;

		for (Unit u : units) {
			p2.set(u.x, u.y);
			dist = Point.calcDistance(p1, p2);
			if (dist <= u.getRadius()) {
				target = u;
			}
			// check distance of the click to the center of the circle
		}
		if (target != null) {
			for (Unit u : selectedUnits) {
				u.orderAttackUnit(target);
			}
		} else { // if no unit is under the cursor, then treat as an attackMove
			for (Unit u : selectedUnits) {
				u.orderAttackMove(x, y);
			}
		}
	}

	/**
	 * The current selection box coordinates, in window coordinates
	 */
	float selectionX1, selectionY1;
	float selectionX2, selectionY2;

	/**
	 * Handle a left click following a default context
	 */
	private void handleLeftClick(float worldX, float worldY, float screenX, float screenY) {

		leftMouseKeyDown = true;

		Unit selected = selectUnitAtClickedPoint(worldX, worldY);
		if (selected != null) {
			selectedUnits.clear();
			selectedUnits.add(selected);
		}

		// Set up a new selection box, initially zero-sized at the point of click
		Vector3 vec = new Vector3();
		vec = fixedCam.unproject(vec.set(screenX, screenY, 0));

		selectionX1 = vec.x;
		selectionY1 = vec.y;

		selectionX2 = vec.x;
		selectionY2 = vec.y;
	}

	/**
	 * Select a unit if there is one underneath this point. If there are multiple units, select the one that occurs last
	 * 
	 * @param x
	 * @param y
	 */
	private Unit selectUnitAtClickedPoint(float x, float y) {
		Unit selected = null;

		Point p1 = new Point(x, y);
		Point p2 = new Point();

		for (Unit u : units) {
			p2.set(u.x, u.y);
			if (Point.calcDistance(p1, p2) <= u.getRadius()) {
				selected = u;
			}
		}

		if (selected != null) {
			return selected;
		} else {
			return null;
		}
	}

	public void clearDeadUnitsFromSelection() {
		ListIterator<Unit> iter = selectedUnits.listIterator();
		while (iter.hasNext()) {
			Unit unit = iter.next();
			if (unit.stats.isDead()) {
				iter.remove();
			}
		}
	}


	/**
	 * Selects all units inside the selection box
	 * 
	 * @param finalX
	 * @param finalY
	 */
	public void selectAllInSelectionBox(float mouseX, float mouseY) {

		Vector3 vec = new Vector3();
		// Set selectionX2, Y2

		vec = fixedCam.unproject(vec.set(mouseX, mouseY, 0));
		selectionX2 = vec.x;
		selectionY2 = vec.y;

		// Obtain world coordinates of the start and end of the selection box

		Point world1, world2;

		// get world cordinates

		vec = fixedCam.project(vec.set(selectionX1, selectionY1, 0)); // convert first to screen coords, then to world
		vec.y = Gdx.graphics.getHeight() - vec.y; //must reverse because project produces a bottom-left coordinated vector
		vec = camera.unproject(vec);
		world1 = new Point(vec.x, vec.y);

		vec = fixedCam.project(vec.set(selectionX2, selectionY2, 0));
		vec.y = Gdx.graphics.getHeight() - vec.y;
		vec = camera.unproject(vec);
		world2 = new Point(vec.x, vec.y);

		float x = Math.min(world1.x, world2.x);
		float y = Math.min(world1.y, world2.y);
		float x2 = Math.max(world1.x, world2.x);
		float y2 = Math.max(world1.y, world2.y);


		// check if unit circles are inside or touching the selection box.

		ArrayList<Unit> newlySelected = new ArrayList<Unit>();

		for (Unit u : units) {
			if (u.x >= x - u.getRadius() && u.x <= x2 + u.getRadius() && u.y >= y - u.getRadius()
					&& u.y <= y2 + u.getRadius() && !u.stats.isDead()) {
				newlySelected.add(u);
			}
		}

		if (!newlySelected.isEmpty()) {
			selectedUnits.clear();
			selectedUnits.addAll(newlySelected);
		}
	}

	/**
	 * Renders circles around selected units
	 */
	public void renderCirclesOnSelectedUnits() {
		for (Unit u : selectedUnits) {
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 2.5f, shapeRenderer, Color.GREEN);
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 3.25f, shapeRenderer, Color.GREEN);
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 4, shapeRenderer, Color.GREEN);
		}
	}

	public static Color defaultSelectionBoxColor = Color.BLUE;

	private static final Matrix4 IDENTITY = new Matrix4();

	/**
	 * We restore the old projection matrix to avoid side effects.
	 * 
	 * @param color
	 */
	public void renderSelectionBox(Color color) {
		Matrix4 old = shapeRenderer.getTransformMatrix();
		shapeRenderer.setProjectionMatrix(fixedCam.combined);
		if (leftMouseKeyDown) {
			shapeRenderer.setColor(color);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.rect(Math.min(selectionX1, selectionX2), Math.min(selectionY1, selectionY2),
					Math.abs(selectionX2 - selectionX1), Math.abs(selectionY2 - selectionY1));
			shapeRenderer.end();
		}
		shapeRenderer.setProjectionMatrix(old);
	}

}
