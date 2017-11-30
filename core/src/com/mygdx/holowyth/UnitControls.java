package com.mygdx.holowyth;

import java.util.ArrayList;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.holowyth.pathfinding.Unit;
import com.mygdx.holowyth.pathfinding.UnitOrderer;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Point;

/**
 * Accepts player input to select and order units to move (and other behaviour later on).
 * 
 * @author Colin Ta
 */
public class UnitControls implements InputProcessor {

	Camera camera;
	ShapeRenderer shapeRenderer;

	ArrayList<Unit> units;

	ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
	boolean leftMouseKeyDown = false;

	Unit prospectUnit; // In order to single-select a unit, the user must mouse down on a unit, and mouse up on the same
						// unit.
	UnitOrderer command;

	public UnitControls(Camera camera, ShapeRenderer shapeRenderer, ArrayList<Unit> units, UnitOrderer orderer) {
		this.camera = camera;
		this.shapeRenderer = shapeRenderer;
		this.units = units;
		this.command = orderer;
	}

	float clickX, clickY; // world location of the last recorded click.
	float clickX2, clickY2; // location of opposite point (of the selection box)

	@Override
	public boolean keyDown(int keycode) {
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

			Point p1 = new Point(vec.x, vec.y);
			Point p2 = new Point();
			float dist;
			// select a unit if there is one underneath this point. If there are multiple units, select the one that
			// occurs last (on top)
			Unit lastResult = null;

			for (Unit u : units) {
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

		// Handle Right Click
		if (button == Input.Buttons.RIGHT && pointer == 0) {
			Point p = new Point(vec.x, vec.y);
			for (Unit u : selectedUnits) {
				command.orderMoveTo(u, p.x, p.y);
			}
		}

		return false;
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

				ArrayList<Unit> newlySelected = new ArrayList<Unit>();

				for (Unit u : units) {
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
		for (Unit u : selectedUnits) {
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 2.5f, shapeRenderer, Color.GREEN);
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 4, shapeRenderer, Color.GREEN);
		}
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
