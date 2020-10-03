package com.mygdx.holowyth.game;

import java.util.ArrayList;
import java.util.Collection;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.game.ui.GameLogDisplay;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.skill.ActiveSkill;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.skill.skill.NoneSkill;
import com.mygdx.holowyth.skill.skill.UnitGroundSkill;
import com.mygdx.holowyth.skill.skill.UnitSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.util.DataUtil;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.util.template.adapters.InputProcessorAdapter;
import com.mygdx.holowyth.util.tools.FunctionBindings;
import com.mygdx.holowyth.util.tools.debugstore.DebugStore;
import com.mygdx.holowyth.util.tools.debugstore.DebugValues;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Accepts player input to select and order units to move (and other behaviour later on). <br>
 * 
 * Has Map Lifetime
 * 
 * @author Colin Ta
 */
public class Controls extends InputProcessorAdapter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	Holowyth game;

	Camera camera;
	Camera fixedCam;
	private ShapeRenderer shapeRenderer;

	MapInstanceInfo mapInstance;
	List<@NonNull Unit> units;

	private FunctionBindings functionBindings = new FunctionBindings();

	/**
	 * The current selection box coordinates, in window coordinates
	 */
	SelectionBox selectionBox = new SelectionBox();

	private SelectedUnits selectedUnits = new SelectedUnits();
	boolean selectionBoxDragActive = false;

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

	private GameLogDisplay gameLog;

	public Controls(Holowyth game, Camera camera, Camera fixedCam, List<@NonNull Unit> units, DebugStore debugStore,
			MapInstanceInfo world, GameLogDisplay gameLog) {
		this.shapeRenderer = game.shapeRenderer;
		this.camera = camera;
		this.fixedCam = fixedCam;
		this.mapInstance = world;
		this.units = units;

		this.font = Holowyth.fonts.debugFont();
		this.game = game;
		this.skin = game.skin;

		this.gameLog = gameLog;

		labelStyle = new LabelStyle(Holowyth.fonts.debugFont(), Holo.debugFontColor);
		
		logger.debug("Controls initialized");

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
				return DataUtil.percentage(u.status.getMoveSpeedRatio());
			} else {
				return "0";
			}
		});
		debugValues.add("Current order of one unit", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.first();
				return u.getOrder().toString();
			} else {
				return "N/A";
			}
		});
		debugValues.add("Target ID of one unit", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.first();
				return u.getOrderTarget() != null ? String.valueOf(u.getOrderTarget().getID()) : "null";
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
		debugValues.add("curGlobalCooldown of one unit", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.first();
				return u.skills.getCurGlobalCooldown();
			} else {
				return 0;
			}
		});
		debugValues.add("Btree last task", () -> {
			if (selectedUnits.size() == 1) {
				Unit u = selectedUnits.first();
				return u.ai.lastRanOrder;
			} else {
				return "N/A";
			}
		});

		bindNumberKeysToSkills();
		functionBindings.bindFunctionToKey(() -> setSPToMax(), Keys.Q);
		functionBindings.bindFunctionToKey(() -> orderSelectedUnitsToStop(), Keys.S);
		functionBindings.bindFunctionToKey(() -> printInfoOfSelectedUnits(), Keys.I);

	}
	private void printInfoOfSelectedUnits() {
		for (Unit unit : selectedUnits) {
			unit.stats.printInfo(true);
		}
	}

	private void bindNumberKeysToSkills() {

		for (int offset = 0; offset < 9; offset++) { // bind keys 1-9
			final int slotNumber = 1 + offset;
			functionBindings.bindFunctionToKey(() -> orderSelectedUnitToUseSkillInSlot(slotNumber), Keys.NUM_1 + offset);
		}
		functionBindings.bindFunctionToKey(() -> orderSelectedUnitToUseSkillInSlot(10), Keys.NUM_0); // also bind the 0 key
	}

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

			curSkill = unit.skills.copySkillInSlot(slotNumber);
			if (curSkill == null) {
				return;
			}

			if (unit.isSkillsOnCooldown() && !Holo.debugSkillCooldownDisabled) {
				logger.info("{}: Skills are on cooldown", unit.getName());
				return;
			}
			if (unit.isCasting()) {
				logger.info("{}: Is busy casting", unit.getName());
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
			cursorPath = "img/cursors/AttackCursor.png";
		} else if (context.isUsingSkill()) {
			cursorPath = "img/cursors/MagicCursor.png";
		} else if (context == Context.RETREAT) {
			cursorPath = "img/cursors/RetreatCursor.png";
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
	public boolean touchDown(int touchX, int touchY, int pointer, int button) {
		selectionBoxDragActive = false;

		Vector3 world = new Vector3(); // World coordinates of the click.
		world = camera.unproject(world.set(touchX, touchY, 0));

		// Handle Left Click
		if (button == Input.Buttons.LEFT && pointer == 0) {

			switch (context) {

			case ATTACK:
				handleAttackCommand(world.x, world.y);
				break;
			case RETREAT:
				handleRetreatCommand(world.x, world.y);
				break;
			case SKILL_GROUND:
				handleSkillGround(world.x, world.y);
				break;
			case SKILL_UNIT:
				handleSkillUnit(world.x, world.y);
				break;
			case SKILL_UNIT_GROUND_1:
				handleSkillUnitGroundPart1(world.x, world.y);
				break;
			case SKILL_UNIT_GROUND_2:
				handleSkillUnitGroundPart2(world.x, world.y);
				break;
			default:
				startSelectionBox(touchX, Gdx.graphics.getHeight() - touchY);
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
				handleRightClick(world.x, world.y);
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

			if (skill.setTargeting(caster, clickedUnit)) {
				caster.orderUseSkill(skill);
			} else {
				logger.info("Skill '{}' could not be used.", skill.name);
			}
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
			dist = Point.dist(p1, p2);
			if (dist <= u.getRadius()) {
				target = u;
			}
			// check distance of the click to the center of the circle
		}

		if (target != null) {
			for (UnitOrderable u : selectedUnits) {
				if (u.getSide() != target.getSide()) {
					u.orderAttackUnit(target);
				} else {
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
			if (u.getRetreatCooldownRemaining() > 0) { // specifically catch this condition and notify the user
				logger.info("Unit {} can't retreat for another {} seconds", u.getStats().getName(),
						DataUtil.round(u.getRetreatCooldownRemaining() / 60, 1));
				gameLog.addErrorMessage(String.format("Unit \"%s\" can't retreat for another %s seconds", u.getStats().getName(),
						DataUtil.round(u.getRetreatCooldownRemaining() / 60, 1)));
			} else {
				u.orderRetreat(x, y);
			}
		}
	}

	@Override
	public boolean touchUp(int touchX, int touchY, int pointer, int button) {

		if (button == Input.Buttons.LEFT && pointer == 0) {
			if (selectionBoxDragActive) {
				selectionBox.x2 = touchX;
				selectionBox.y2 = Gdx.graphics.getHeight() - touchY;
				selectUnitsInSelectionBox();
			}
			selectionBoxDragActive = false;
		}
		return false;
	}

	@Override
	public boolean touchDragged(int touchX, int touchY, int pointer) {
		if (pointer == 0 && selectionBoxDragActive) {
			// Vector3 vec = new Vector3(); // obtain window coordinates of the click.
			// vec = fixedCam.unproject(vec.set(screenX, screenY, 0));
			// selectionX2 = vec.x;
			// selectionY2 = vec.y;

			selectionBox.x2 = touchX;
			selectionBox.y2 = Gdx.graphics.getHeight() - touchY;
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
			dist = Point.dist(p1, p2);
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

	private void startSelectionBox(float screenX, float screenY) {
		// Set up a new selection box, initially zero-sized at the point of click
		selectionBox.x1 = screenX;
		selectionBox.y1 = screenY;
		selectionBox.x2 = screenX;
		selectionBox.y2 = screenY;

		selectionBoxDragActive = true;
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
			if (Point.dist(p1, p2) <= u.getRadius()) {
				selected = u;
			}
		}

		if (selected != null) {
			return selected;
		} else {
			return null;
		}
	}

	public void tick() {
		clearDeadUnitsFromSelection();
	}
	
	private void clearDeadUnitsFromSelection() {
		Iterator<Unit> iter = selectedUnits.iterator();
		while (iter.hasNext()) {
			Unit unit = iter.next();
			if (unit.stats.isDead()) {
				iter.remove();
			}
		}
	}

	/** Has no effect if unit is not in selection */
	public boolean removeUnitFromSelection(UnitInfo u) {
		return selectedUnits.remove(u);
	}
	
	public void clearSelectedUnits() {
		selectedUnits.clear();
	}

	private Vector3 temp = new Vector3();

	/**
	 * Selects all units inside the selection box, following these rules: <br>
	 * If the group consists of a mixed group, only select the player units.
	 * 
	 * Actually makes the click-select case obsolete, as a zero-size selection box does the same thing.
	 * 
	 * @param finalX
	 * @param finalY
	 */
	public void selectUnitsInSelectionBox() {

		Vector2 world1 = screenToWorldCoordinates(selectionBox.x1, selectionBox.y1);
		Vector2 world2 = screenToWorldCoordinates(selectionBox.x2, selectionBox.y2);

		// Get bottom left and top right corner of the box

		float x1 = Math.min(world1.x, world2.x);
		float y1 = Math.min(world1.y, world2.y);
		float x2 = Math.max(world1.x, world2.x);
		float y2 = Math.max(world1.y, world2.y);

		// Get all units that are touching the rectangular area
		var newlySelected = new ArrayList<Unit>();
		for (Unit u : units) {
			if (u.x >= x1 - u.getRadius() && u.x <= x2 + u.getRadius() && u.y >= y1 - u.getRadius()
					&& u.y <= y2 + u.getRadius() && !u.stats.isDead()) {
				newlySelected.add(u);
			}
		}
		if (Holo.debugAllowSelectEnemyUnits) {
			ifMixedSelectionFilterOutNonPlayerUnits(newlySelected);
		} else {
			newlySelected.removeIf((u) -> u.getSide().isEnemy());
		}

		if (!newlySelected.isEmpty()) {
			selectedUnits.clear();
			selectedUnits.addAll(newlySelected);
		}
	}

	private Vector2 screenToWorldCoordinates(float x, float y) {
		temp.set(x, Gdx.graphics.getHeight() - y, 0); // convert to touch coordinates first
		temp = camera.unproject(temp);
		return new Vector2(temp.x, temp.y);
	}

	private void ifMixedSelectionFilterOutNonPlayerUnits(List<Unit> units) {
		boolean containsPlayerUnits = units.stream().anyMatch((unit) -> unit.getSide() == Side.PLAYER);
		if (containsPlayerUnits) {
			units.removeIf((unit) -> unit.getSide() != Side.PLAYER);
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
		if (selectionBoxDragActive) {
			shapeRenderer.setColor(color);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.rect(Math.min(selectionBox.x1, selectionBox.x2), Math.min(selectionBox.y1, selectionBox.y2),
					Math.abs(selectionBox.x2 - selectionBox.x1), Math.abs(selectionBox.y2 - selectionBox.y1));
			shapeRenderer.end();
		}
		shapeRenderer.setProjectionMatrix(old);
	}

	public void renderUnitUnderCursor(Color allyColor, Color enemyColor) {
		if (unitUnderCursor != null) {
			Color color = unitUnderCursor.isAPlayerCharacter() ? allyColor : enemyColor;
			shapeRenderer.setColor(color);
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.circle(unitUnderCursor.x, unitUnderCursor.y, unitUnderCursor.getRadius() + 3);
			shapeRenderer.end();
		}
	}

	/**
	 * Is guaranteed to be called when selectedUnits is modified Is called immediately after a modifiying action, if that action actually changed the
	 * set.
	 */
	private void onUnitSelectionModified() {
		if (context == Context.SKILL_GROUND || context == Context.SKILL_UNIT) {
			context = Context.NONE;
		}
		listeners.forEach((l) -> l.unitSelectionModified(getSelectedUnitsSnapshot()));
	}

	private Set<UnitSelectionListener> listeners = new LinkedHashSet<UnitSelectionListener>();

	public void addListener(UnitSelectionListener l) {
		listeners.add(l);
	}

	public void removeListener(UnitSelectionListener l) {
		listeners.remove(l);
	}

	public interface UnitSelectionListener {
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
	public class SelectedUnits implements Set<Unit> {

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
		 * custom implementation to reduce duplicate calls of selection being modified
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
		
		@Override
		public boolean isEmpty() {
			return selected.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return selected.contains(o);
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return selected.toArray(a);
			}
		
		@Override
		public Object[] toArray() {
			return selected.toArray();
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return selected.containsAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException();
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
	public List<UnitInfo> getSelectedUnitsSnapshot() {
		return new ArrayList<UnitInfo>(selectedUnits);
	}

	private static class SelectionBox {
		public float x1;
		public float y1;
		public float x2;
		public float y2;
	}

	private Unit unitUnderCursor;

	@Override
	public boolean mouseMoved(int touchX, int touchY) {
		Vector3 world = new Vector3(); // World coordinates of the click.
		world = camera.unproject(world.set(touchX, touchY, 0));

		unitUnderCursor = selectUnitAtClickedPoint(world.x, world.y);
		return false;
	}

}
