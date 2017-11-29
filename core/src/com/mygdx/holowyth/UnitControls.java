package com.mygdx.holowyth;

import java.util.ArrayList;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.holowyth.pathfinding.Unit;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Point;

/**
 * Accepts player input to select and order units to move (and other behaviour later on).
 * @author Colin Ta
 */
public class UnitControls implements InputProcessor {

	Camera camera;
	ShapeRenderer shapeRenderer;
	
	ArrayList<Unit> units;
	
	ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
	boolean isDragSelection = false;
	
	Unit prospectUnit; // In order to single-select a unit, the user must mouse down on a unit, and mouse up on the same unit.
	
	public UnitControls(Camera camera, ShapeRenderer shapeRenderer, ArrayList<Unit> units){
		this.camera = camera;
		this.shapeRenderer = shapeRenderer;
		this.units = units;
	}
	
	float clickX, clickY; // location of the last recorded click.
	
	
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
		isDragSelection = false;
		Vector3 vec = new Vector3(); //obtain world coordinates of the click.
		vec = camera.unproject(vec.set(screenX, screenY, 0));
		
		
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT && pointer == 0) {
			
			Vector3 vec = new Vector3(); //obtain world coordinates of the click.
			vec = camera.unproject(vec.set(screenX, screenY, 0));
			//Point click = new Point(vec.x, vec.y);
			
			Point p1 = new Point(vec.x, vec.y);
			Point p2 = new Point();
			float dist;
			
			System.out.println("Left MButton up!");
			if (!isDragSelection){
				//select a unit if there is one underneath this point. If there are multiple units, select the one that occurs last (on top)
				Unit lastResult = null;
				
				for(Unit u: units){
					p2.set(u.x, u.y);
					dist = Point.calcDistance(p1, p2);
					if(dist <= u.getRadius()){
						lastResult = u;
					}
					//check distance of the click to the center of the circle
				}
				
				if(lastResult != null){
					//select this unit
					selectedUnits.clear();
					selectedUnits.add(lastResult);
				}
				
			}
		}
//		if (button == Input.Buttons.RIGHT && pointer == 0) {
//			System.out.println("Right MButton up!");
//		}
		isDragSelection = false;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		isDragSelection = true;
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
	public void renderCirclesOnSelectedUnits(){
		for(Unit u: selectedUnits){
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 2.5f, shapeRenderer, Color.GREEN);
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 4, shapeRenderer, Color.GREEN);
		}
	}

}
