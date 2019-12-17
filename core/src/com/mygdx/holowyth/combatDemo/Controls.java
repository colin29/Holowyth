package com.mygdx.holowyth.combatDemo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.ui.GameLog;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.skill.skill.UnitGroundSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.skill.skillsandeffects.Skills;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.unit.item.Equip;
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

	private SelectedUnits selectedUnits = new SelectedUnits();
	boolean leftMouseKeyDown = false;

	public enum Context {
		NONE, ATTACK, RETREAT, SKILL_GROUND, SKILL_UNIT, SKILL_UNIT_GROUND_1, SKILL_UNIT_GROUND_2;

		boolean isUsingSkill() {
			switch (this) {
			case SKILL_GROUND:
			case SKILL_UNIT:
			case SKILL_UNIT_GROUND_1:
			case SKILL_UNIT_GROUND_2:
				return true;
			default:
				return false;
			}
		}
	}

	Context context = Context.NONE;

	// For displaying Debug info
	BitmapFont font;
	Skin skin;
	LabelStyle labelStyle;

	private GameLog gameLog;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public Controls(Holowyth game, Camera camera, Camera fixedCam, List<Unit> units, DebugStore debugStore,
			World world, GameLog gameLog) {
		this.shapeRenderer = game.shapeRenderer;
		this.camera = camera;
		this.fixedCam = fixedCam;
		this.world = world;
		this.units = units;

		this.font = Holowyth.fonts.debugFont();
		this.game = game;
		this.skin = game.skin;

		this.gameLog = gameLog;

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
				Unit u = selectedUnits.first();
				return DataUtil.getAsPercentage(u.stats.getMoveSpeedRatio());
			} else {
				return "0";
			}
		});
		debugValues.add("Current order of one unit", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.first();
				return u.getCurrentOrder().toString();
			} else {
				return "N/A";
			}
		});
		debugValues.add("Target ID of one unit", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.first();
				return u.getTarget() != null ? String.valueOf(u.getTarget().getID()) : "null";
			} else {
				return "N/A";
			}
		});
		debugValues.add("Attacking of one unit", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.first();
				return u.getAttacking() != null ? u.getAttacking().getName() : null;
			} else {
				return "N/A";
			}
		});

		debugValues.add("Num equip slots filled of one unit", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.first();
				int count = 0;
				for (Equip e : u.equip.getEquipSlots().values()) {
					if (e != null) {
						count += 1;
					}
				}
				return count;
			} else {
				return 0;
			}
		});

		bindNumberKeysToSkills();
		functionBindings.bindFunctionToKey(() -> setSPToMax(), Keys.Q);
		functionBindings.bindFunctionToKey(() -> orderSelectedUnitsToStop(), Keys.S);

	}

	private void bindNumberKeysToSkills() {

		for (int offset = 0; offset < 9; offset++) { // bind keys 1-9
			final int slotNumber = 1 + offset;
			functionBindings.bindFunctionToKey(() -> orderSelectedUnitToUseSkillInSlot(slotNumber), Keys.NUM_1 + offset);
		}
		functionBindings.bindFunctionToKey(() -> orderSelectedUnitToUseSkillInSlot(10), Keys.NUM_0); // also bind the 0 key
	}

	float clickX, clickY; // Current click in world coordinates

	ActiveSkill skillToUse = new Skills.Explosion();
	ActiveSkill curSkill = null;

	private void setSPToMax() {
		for (Unit unit : selectedUnits) {
			unit.stats.setSp(unit.stats.getMaxSp());
		}
	}

	private void orderSelectedUnitsToStop() {
		for (Unit unit : selectedUnits) {
			unit.orderStop();
		}
	}

	/**
	 * This order will bring up the targeting UI, so it only makes sense for the player targeting
	 */
	public void orderSelectedUnitToUseSkillInSlot(int slotNumber) {

		if (selectedUnits.size() == 1) {

			Unit unit = selectedUnits.iterator().next();

			curSkill = unit.skills.getSkillInSlot(slotNumber);
			if (curSkill == null) {
				return;
			}

			if (unit.areSkillsOnCooldown() && !Holo.debugSkillCooldownDisabled) {
				logger.info("{}: Skills are on cooldown", unit.getName());
				return;
			}
			if (curSkill.getParent().curCooldown > 0 && !Holo.debugSkillCooldownDisabled) {
				logger.info("{} is on cooldown", curSkill.name);
				gameLog.addErrorMessage(String.format("%s is on cooldown (%s seconds remaining}", curSkill.name,
						DataUtil.round(curSkill.curCooldown / Holo.GAME_FPS, 1)));
				return;
			}

			if (!curSkill.hasEnoughSp(unit)) {
				logger.info("{} not enough sp", curSkill.name);
				gameLog.addErrorMessage("Not enough sp!");
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
			updateCursorIcon();
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		functionBindings.runBoundFunction(keycode);

		if (keycode == Keys.A && selectedUnits.size() > 0) {
			beginAttackContext();
			return true;
		} else if (keycode == Keys.R && selectedUnits.size() > 0) {
			beginRetreatContext();
			return true;
		}
		return false;
	}

	private void beginAttackContext() {
		clearContext();
		context = Context.ATTACK;
		updateCursorIcon();
	}

	private void beginRetreatContext() {
		clearContext();
		context = Context.RETREAT;
		updateCursorIcon();
	}

	/**
	 * Set cursor based on the current context
	 */
	private void updateCursorIcon() {
		setCursor(context);
	}

	private void setCursor(Context context) {

		String cursorPath = null;
		int offsetX = 0, offsetY = 0;

		if (context == Context.ATTACK) {
			cursorPath = "icons/cursors/AttackCursor.png";
		} else if (context.isUsingSkill()) {
			cursorPath = "icons/cursors/MagicCursor.png";
		} else if (context == Context.RETREAT) {
			cursorPath = "icons/cursors/RetreatCursor.png";
			offsetX = 5;
			offsetY = 5;
		}

		if (cursorPath == null) {
			Gdx.graphics.setSystemCursor(SystemCursor.Crosshair);
		} else {
			Texture texture = (Texture) game.assets.get(cursorPath);
			if (!texture.getTextureData().isPrepared())
				texture.getTextureData().prepare();
			Pixmap pixmap = texture.getTextureData().consumePixmap();
			Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, offsetX, offsetY));
		}
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

		if (skill.pluginTargeting(caster)) {
			caster.orderUseSkill(skill);
		} else {
			logger.info("Skill '{}' could not be used.", skill.name);
		}

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
			if (u.getRetreatCooldown() > 0) { // specifically catch this condition and notify the user
				logger.info("Unit {} can't retreat for another {} seconds", u.getStats().getName(),
						DataUtil.round(u.getRetreatCooldown() / 60, 1));
				gameLog.addErrorMessage(String.format("Unit \"%s\" can't retreat for another %s seconds", u.getStats().getName(),
						DataUtil.round(u.getRetreatCooldown() / 60, 1)));
			} else {
				u.orderRetreat(x, y);
			}
		}
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {

		if (button == Input.Buttons.LEFT && pointer == 0) {
			if (leftMouseKeyDown) {
				selectUnitsInSelectionBox(screenX, screenY);
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
		updateCursorIcon();
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
				u.orderAttackUnit(target, true);
			}
		} else { // if no unit is under the cursor, then treat as an attackMove
			for (UnitOrderable u : selectedUnits) {
				logger.debug("Attack move ordered");
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
	 * Selects all units inside the selection box, following these rules: <br>
	 * If the group consists of a mixed group, only select the player units.
	 * 
	 * Actually makes the click-select case obsolete, as a zero-size selection box does the same thing.
	 * 
	 * @param finalX
	 * @param finalY
	 */
	public void selectUnitsInSelectionBox(float mouseX, float mouseY) {

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

		var newlySelected = new ArrayList<Unit>();

		for (Unit u : units) {
			if (u.x >= x - u.getRadius() && u.x <= x2 + u.getRadius() && u.y >= y - u.getRadius()
					&& u.y <= y2 + u.getRadius() && !u.stats.isDead()) {
				newlySelected.add(u);
			}
		}

		boolean containsPlayerUnits = newlySelected.stream().anyMatch((unit) -> unit.getSide() == Side.PLAYER);
		if (containsPlayerUnits) {
			newlySelected.removeIf((unit) -> unit.getSide() != Side.PLAYER);
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
	private void onUnitSelectionModified() {
		if (context == Context.SKILL_GROUND || context == Context.SKILL_UNIT) {
			context = Context.NONE;
		}
		listeners.forEach((l) -> l.unitSelectionModified(Collections.unmodifiableList(getSelectedUnitReadOnly())));
	}

	private Set<ControlsListener> listeners = new LinkedHashSet<ControlsListener>();

	public void addListener(ControlsListener l) {
		listeners.add(l);
		logger.debug("Added listener");
	}

	public void removeListener(ControlsListener l) {
		listeners.remove(l);
	}

	public interface ControlsListener {
		/**
		 * @param list
		 *            of units is unmodifiable
		 */
		public abstract void unitSelectionModified(List<UnitInfo> selectedUnits);
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

		public Unit first() {
			return selected.iterator().next();
		}

		@Override
		public boolean remove(Object u) {
			if (selected.remove(u)) {
				onUnitSelectionModified();
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean add(Unit u) {
			if (selected.add(u)) {
				onUnitSelectionModified();
				return true;
			} else {
				return false;
			}
		}

		@Override
		/**
		 * custom addAll method to reduce duplicate calls of selection being modified
		 */
		public boolean addAll(Collection<? extends Unit> units) {
			if (selected.addAll(units)) {
				onUnitSelectionModified();
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
					onUnitSelectionModified();
				}
			};
		}

		@Override
		public void clear() {
			if (!selected.isEmpty()) {
				selected.clear();
				onUnitSelectionModified();
			} else {
				selected.clear();
			}
		}
	}

	public Context getContext() {
		return context;
	}

	public ActiveSkill getCurSkill() {
		return curSkill;
	}

	public SelectedUnits getSelectedUnits() {
		return selectedUnits;
	}

	/**
	 * @return A seperate copy
	 */
	public List<UnitInfo> getSelectedUnitReadOnly() {
		return new ArrayList<>(selectedUnits);
	}

}
