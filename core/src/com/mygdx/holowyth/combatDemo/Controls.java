package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.skill.GroundSkill;
import com.mygdx.holowyth.skill.NoneSkill;
import com.mygdx.holowyth.skill.Skill;
import com.mygdx.holowyth.skill.Skills;
import com.mygdx.holowyth.skill.UnitGroundSkill;
import com.mygdx.holowyth.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;
import com.mygdx.holowyth.util.tools.FunctionBindings;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

/**
 * Accepts player input to select and order units to move (and other behaviour later on). <br>
 * 
 * Has Map Lifetime
 * 
 * @author Colin Ta
 */
public class Controls extends InputProcessorAdapter {

	Holowyth game;

	Camera camera;
	Camera fixedCam;
	private ShapeRenderer shapeRenderer;

	World world;
	List<Unit> units;

	private FunctionBindings functionBindings = new FunctionBindings();

	public SelectedUnits selectedUnits = new SelectedUnits();
	boolean leftMouseKeyDown = false;

	public enum Context {
		NONE, ATTACK, RETREAT, SKILL_GROUND, SKILL_UNIT, SKILL_UNIT_GROUND_1, SKILL_UNIT_GROUND_2
	}

	Context context = Context.NONE;

	// For displaying Debug info
	BitmapFont font;
	Skin skin;
	LabelStyle labelStyle;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public Controls(Holowyth game, Camera camera, Camera fixedCam, List<Unit> units, DebugStore debugStore,
			World world) {
		this.shapeRenderer = game.shapeRenderer;
		this.camera = camera;
		this.fixedCam = fixedCam;
		this.world = world;
		this.units = units;

		this.font = Holowyth.fonts.debugFont();
		this.skin = game.skin;

		labelStyle = new LabelStyle(Holowyth.fonts.debugFont(), Holo.debugFontColor);

		DebugValues debugValues = debugStore.registerComponent("Controls");
		debugValues.add("Order Context", () -> getCurrentContextText());

		debugValues.add("# of units selected", () -> selectedUnits.size());
		debugValues.add("Clearance between two units", () -> {
			if (selectedUnits.size() == 2) {
				Iterator<Unit> iter = selectedUnits.iterator();
				Unit u1 = iter.next();
				Unit u2 = iter.next();
				return Unit.getDist(u1, u2) - (u1.getRadius() + u2.getRadius());
			} else {
				return 0;
			}
		});

		debugValues.add("Movement speed of one unit", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.iterator().next();
				return DataUtil.getAsPercentage(u.stats.getMoveSpeedRatio());
			} else {
				return "0";
			}
		});

		// functionBindings.bindFunctionToKey(() -> useSkillInSlot(1), Keys.NUM_1);
		// functionBindings.bindFunctionToKey(() -> useSkillInSlot(2), Keys.NUM_2);
		// functionBindings.bindFunctionToKey(() -> useSkillInSlot(3), Keys.NUM_3);

		bindNumberKeysToSkills();

		functionBindings.bindFunctionToKey(() -> setSPToMax(), Keys.Q);

	}

	private void bindNumberKeysToSkills() {

		for (int offset = 0; offset < 9; offset++) { // bind keys 1-9
			final int slotNumber = 1 + offset;
			functionBindings.bindFunctionToKey(() -> useSkillInSlot(slotNumber), Keys.NUM_1 + offset);
		}
		functionBindings.bindFunctionToKey(() -> useSkillInSlot(10), Keys.NUM_0); // also bind the 0 key
	}

	float clickX, clickY; // Current click in world coordinates

	Skill skillToUse = new Skills.Explosion();
	Skill curSkill = null;

	/**
	 * Slot 0 is unused atm, but can be used.
	 */
	Skill[] skills = new Skill[11];
	{
		skills[1] = new Skills.Explosion();
		skills[2] = new Skills.ExplosionLongCast();
		skills[3] = new Skills.NovaFlare();
		skills[4] = new Skills.Implosion();
		skills[5] = new Skills.ForcePush();
	}

	private void setSPToMax() {
		for (Unit unit : selectedUnits) {
			unit.stats.setSp(unit.stats.getMaxSp());
		}
	}

	/**
	 * Input from 1-9, and 0.
	 */
	private void useSkillInSlot(int slotNumber) {

		if (slotNumber < 0 || slotNumber > 10) {
			return;
		}

		if (skills[slotNumber] == null) {
			logger.debug("Tried to use skill slot [{}] but no skill was assigned", slotNumber);
			return;
		}

		if (selectedUnits.size() == 1) {
			Unit unit = selectedUnits.iterator().next();
			try {

				curSkill = (Skill) skills[slotNumber].clone();
				System.out.println("Using " + curSkill.name);

				if (unit.areSkillsOnCooldown()) {
					System.out.println(unit.stats.getName() + ": Skills are on cooldown");
					return;
				}

				if (!curSkill.hasEnoughSp(unit)) {
					System.out.println("not enough sp");
					return;
				}

				switch (curSkill.getTargeting()) {
				case GROUND:
					context = Context.SKILL_GROUND;
					break;
				case NONE:
					handleSkillNone();
					break;
				case UNIT:
					context = Context.SKILL_UNIT;
					break;
				case UNIT_GROUND:
					context = Context.SKILL_UNIT_GROUND_1;
					break;
				default:
					break;

				}

			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.A && selectedUnits.size() > 0) {
			clearContext();
			context = Context.ATTACK;
			return true;
		} else if (keycode == Keys.R && selectedUnits.size() > 0) {
			clearContext();
			context = Context.RETREAT;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		functionBindings.runBoundFunction(keycode);
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		leftMouseKeyDown = false;

		Vector3 vec = new Vector3(); // World coordinates of the click.
		vec = camera.unproject(vec.set(screenX, screenY, 0));

		// Handle Left Click
		if (button == Input.Buttons.LEFT && pointer == 0) {

			switch (context) {

			case ATTACK:
				handleAttackCommand(vec.x, vec.y);
				break;
			case RETREAT:
				handleRetreatCommand(vec.x, vec.y);
				break;
			case SKILL_GROUND:
				handleSkillGround(vec.x, vec.y);
				break;
			case SKILL_UNIT:
				handleSkillUnit(vec.x, vec.y);
				break;
			case SKILL_UNIT_GROUND_1:
				handleSkillUnitGroundPart1(vec.x, vec.y);
				break;
			case SKILL_UNIT_GROUND_2:
				handleSkillUnitGroundPart2(vec.x, vec.y);
				break;
			default:
				handleLeftClick(vec.x, vec.y, screenX, screenY);
			}
			return true;
		}

		// Handle Right Click
		/**
		 * Right click clears context if there is one, otherwise it's a move command.
		 */
		if (button == Input.Buttons.RIGHT && pointer == 0) {
			if (context != Context.NONE) {
				clearContext();
			} else {
				handleRightClick(vec.x, vec.y);
			}

			return true;
		}

		return false;
	}

	private void handleSkillUnit(float x, float y) {
		assertExactlyOneUnitSelected();
		Unit clickedUnit = selectUnitAtClickedPoint(x, y);

		if (clickedUnit != null) {
			UnitSkill skill = (UnitSkill) this.curSkill;
			Unit caster = selectedUnits.iterator().next();
			skill.pluginTargeting(caster, clickedUnit);
			caster.orderUseSkill(skill);
			clearContext();
		}
	}

	private Unit curSkillUnit; // used purely for storing skill parameters in multi-part targetings

	private void handleSkillUnitGroundPart1(float x, float y) {
		assertExactlyOneUnitSelected();
		Unit target = selectUnitAtClickedPoint(x, y);

		if (target != null) {
			curSkillUnit = target;
			context = Context.SKILL_UNIT_GROUND_2;
		}

	}

	private void handleSkillUnitGroundPart2(float x, float y) {
		assertExactlyOneUnitSelected();

		UnitGroundSkill skill = (UnitGroundSkill) this.curSkill;
		Unit caster = selectedUnits.iterator().next();
		skill.pluginTargeting(caster, curSkillUnit, x, y);
		caster.orderUseSkill(skill);
		clearContext();
	}

	private void handleSkillGround(float x, float y) {
		assertExactlyOneUnitSelected();

		GroundSkill skill = (GroundSkill) this.curSkill;
		Unit caster = selectedUnits.iterator().next();
		skill.pluginTargeting(caster, x, y);
		caster.orderUseSkill(skill);
		clearContext();
	}

	private void handleSkillNone() {
		assertExactlyOneUnitSelected();
		NoneSkill skill = (NoneSkill) this.curSkill;
		Unit caster = selectedUnits.iterator().next();
		skill.pluginTargeting(caster);
		caster.orderUseSkill(skill);
		clearContext();
	}

	private void assertExactlyOneUnitSelected() {
		if (selectedUnits.size() != 1) {
			new Exception("Selected units is not exactly one: " + selectedUnits.size()).printStackTrace();
			return;
		}
	}

	private void handleRightClick(float x, float y) {

		// Attack command if click is over an enemy unit.

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
			for (UnitOrderable u : selectedUnits) {
				boolean valid = u.orderAttackUnit(target);
				if (!valid) {
					u.orderMove(x, y);
				}
			}
		} else {
			handleMoveCommand(x, y);
		}

	}

	private void handleRetreatCommand(float x, float y) {
		clearContext();
		for (UnitOrderable u : selectedUnits) {
			u.orderRetreat(x, y);
		}
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
	 * Makes it so when you are part-way through an order, and then the start of a separate order, the game will stop waiting for the first one
	 */
	private void clearContext() {
		context = Context.NONE;
		curSkill = null;
		curSkillUnit = null;
	}

	private String getCurrentContextText() {
		switch (context) {
		case ATTACK:
			return "Select Attack Target";
		case RETREAT:
			return "Select retreat location";
		case NONE:
			return "Idle";
		default:
			return context.toString();
		}
	}

	private void handleMoveCommand(float x, float y) {
		clearContext();
		for (UnitOrderable u : selectedUnits) {
			u.orderMove(x, y);
		}
	}

	/**
	 * Handle a left-click following being in the "attack" context
	 * 
	 * @param x
	 * @param y
	 */
	private void handleAttackCommand(float x, float y) {
		clearContext();
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
			for (UnitOrderable u : selectedUnits) {
				u.orderAttackUnit(target);
			}
		} else { // if no unit is under the cursor, then treat as an attackMove
			for (UnitOrderable u : selectedUnits) {
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
	 * Like the majority of Control methods, accepts world coordinates
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
		Iterator<Unit> iter = selectedUnits.iterator();
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
		vec.y = Gdx.graphics.getHeight() - vec.y; // must reverse because project produces a bottom-left coordinated
													// vector
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
			for (Unit u : newlySelected) {
				u.stats.printInfo();
			}
		}
	}

	/**
	 * Renders circles around selected units
	 */
	public void renderCirclesOnSelectedUnits() {
		for (Unit u : selectedUnits) {
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 2.5f, Color.GREEN);
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 3.25f, Color.GREEN);
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + 4, Color.GREEN);
		}
	}

	public static Color defaultSelectionBoxColor = Color.BLUE;

	/**
	 * We restore the old projection matrix to avoid side effects.
	 * 
	 * @param color
	 */
	public void renderSelectionBox(Color color) {
		Matrix4 old = shapeRenderer.getProjectionMatrix().cpy();
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

	/**
	 * Is guaranteed to be called when selectedUnits is modified Is called immediately after a modifiying action, if that action actually changed the
	 * set.
	 */
	private void onSelectedUnitsModified() {
		// System.out.println("selectedUnits modified");
		if (context == Context.SKILL_GROUND || context == Context.SKILL_UNIT) {
			context = Context.NONE;
		}
	}

	/**
	 * Same as set, but tracks when the set is modified.
	 * 
	 * @author Colin Ta
	 *
	 */
	public class SelectedUnits extends HashSet<Unit> {

		private static final long serialVersionUID = 1L;
		private final Set<Unit> selected = new HashSet<Unit>();

		@Override
		public boolean remove(Object u) {
			if (selected.remove(u)) {
				onSelectedUnitsModified();
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean add(Unit u) {
			if (selected.add(u)) {
				onSelectedUnitsModified();
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int size() {
			return selected.size();
		}

		@Override
		public Iterator<Unit> iterator() {
			return new Iterator<Unit>() {
				private final Iterator<Unit> iter = selected.iterator();

				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}

				@Override
				public Unit next() {
					return iter.next();
				}

				@Override
				public void remove() {
					iter.remove();
					onSelectedUnitsModified();
				}
			};
		}

		@Override
		public void clear() {
			if (!selected.isEmpty()) {
				selected.clear();
				onSelectedUnitsModified();
			} else {
				selected.clear();
			}
		}
	}

}
