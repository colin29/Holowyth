package com.mygdx.holowyth.combatDemo.rendering;

import java.util.function.Predicate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.Controls;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.skill.Skill.Status;
import com.mygdx.holowyth.skill.SkillInfo;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.util.Holo;

/**
 * Handles all of CombatDemo's rendering <br>
 * 
 * Should carry very little state except for which modules to render and some rendering flags
 * 
 * Has Screen lifetime
 * 
 * @author Colin Ta
 *
 */
public class Renderer {

	Holowyth game;

	// Rendering pipeline

	Camera worldCamera;
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;

	// Screen lifetime components
	private Stage stage;
	private PathingModule pathingModule;

	// Map lifetime components
	private World world;
	private Field map;
	private EffectsHandler effects;

	// Graphics options

	private Color clearColor = Color.BLACK;

	// Graphic flags:

	private Controls unitControls;

	// Sub-components
	DebugRenderer debug;

	private PathfindingRenderer pathfinding;
	private UnitMotionRenderer unitMotion;
	private SandBoxRenderer sandbox;

	/**
	 * The game, worldCamera, and other screen-lifetime modules are passed in.
	 * 
	 * @param game
	 * @param worldCamera
	 * @param stage
	 * @param pathingModule
	 *            // May be null;
	 */
	public Renderer(Holowyth game, Camera worldCamera, Stage stage, PathingModule pathingModule) {

		batch = game.batch;
		shapeRenderer = game.shapeRenderer;

		this.game = game;

		this.worldCamera = worldCamera;
		this.stage = stage;
		this.pathingModule = pathingModule;

		debug = new DebugRenderer(this);
		pathfinding = new PathfindingRenderer(this, pathingModule);
		unitMotion = new UnitMotionRenderer(this);
		sandbox = new SandBoxRenderer(this);
	}

	/*
	 * Methods call should not set projection matrixes (it is assumed to be the world matrix). If they do they should
	 * restore the old state
	 */
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);

		worldCamera.update();

		batch.setProjectionMatrix(worldCamera.combined);

		renderUnitHpSpBars();
		renderCastingBars();

		// 1: Render Map

		// if (this.map != null) {
		// renderMapPolygons();
		// pathingModule.renderExpandedMapPolygons();
		// renderMapBoundaries();
		// }

		// 2: Render unit paths
		pathfinding.renderPaths(false);

		unitMotion.renderUnitDestinations(Color.GREEN);

		// 3: Render units and selection indicators

		if (unitControls != null) {
			unitControls.clearDeadUnitsFromSelection();
			unitControls.renderCirclesOnSelectedUnits();
			pathfinding.renderUnitExpandedHitBodies();
		}

		renderUnits();

		if (unitControls != null) {
			unitControls.renderSelectionBox(Controls.defaultSelectionBoxColor);
			renderOutlineAroundBusyRetreatingUnits();
			renderOutlineAroundBusyCastingUnits();
		}

		renderOutlineAroundKnockbackedUnits();

		// 3.5: Render arrows

		debug.renderUnitKnockbackVelocities();

		for (Unit u : world.getUnits()) {
			u.renderAttackingArrow();
		}

		// 1: Render Map

		if (this.map != null) {
			renderMapObstacles();
			pathingModule.renderExpandedMapPolygons();
			renderMapBoundaries();
		}

		// Render effects

		effects.renderDamageEffects();
		effects.renderBlockEffects(delta);

		// UI
		stage.act(delta);
		stage.draw();

	}

	private void renderUnits() {
		if (Holo.useTestSprites) {
			sandbox.renderUnitsWithTestSprites();
		} else {

			// Render unit circles
			for (Unit unit : world.getUnits()) {

				shapeRenderer.begin(ShapeType.Filled);

				if (unit.isAPlayerCharacter()) {
					shapeRenderer.setColor(Color.PURPLE);
				} else {
					shapeRenderer.setColor(Color.YELLOW);
				}
				shapeRenderer.getColor().a = unit.stats.isDead() ? 0.5f : 1;

				shapeRenderer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);

				shapeRenderer.end();
			}

			// Render an outline around the unit
			for (Unit unit : world.getUnits()) {
				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.getColor().a = unit.stats.isDead() ? 0.5f : 1;
				shapeRenderer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);
				shapeRenderer.end();
			}
		}
	}

	private void renderMapObstacles() {
		shapeRenderer.setProjectionMatrix(worldCamera.combined);
		shapeRenderer.setColor(Color.BLACK);
		HoloGL.renderPolygons(map.polys);
	}

	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(worldCamera.combined);
		HoloGL.renderMapBoundaries(map);
	}

	private void renderUnitHpSpBars() {

		final float hpBarVertSpacing = 5; // space between the bottom of the unit and the top of the hpbar
		final float hpBarHeight = 4;
		final float spBarHeight = 3f;

		shapeRenderer.setProjectionMatrix(worldCamera.combined);

		for (UnitInfo unit : world.getUnits()) {

			UnitStatsInfo unitStats = unit.getStats();
			float maxHp = unitStats.getMaxHp();
			float hpRatio = unitStats.getHpRatio();
			float spRatio = unitStats.getSpRatio();

			float hpBarWidth = (float) Math.sqrt((maxHp / 100)) * hpBarWidthBase;
			hpBarWidth = Math.max(hpBarWidth, hpBarWidthMin);
			hpBarWidth = Math.min(hpBarWidth, hpBarWidthMax);

			float x = unit.getX() - hpBarWidth / 2;
			float y = unit.getY() - unit.getRadius() - hpBarHeight - hpBarVertSpacing;

			// Draw the hp bar

			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.RED);

			shapeRenderer.rect(x, y, hpBarWidth * hpRatio, hpBarHeight); // draws from bottom left corner.
			shapeRenderer.end();

			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.BLACK);

			shapeRenderer.rect(x, y, hpBarWidth, hpBarHeight);

			shapeRenderer.end();

			float sX = x;
			float sY = y - spBarHeight;

			// Draw the sp bar

			if (unit.getSide() == Side.PLAYER) {
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.setColor(spBarColor);

				shapeRenderer.rect(sX, sY, hpBarWidth * spRatio, spBarHeight); // draws from bottom left corner.
				shapeRenderer.end();

				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.BLACK);

				shapeRenderer.rect(sX, sY, hpBarWidth, spBarHeight);

				shapeRenderer.end();
			}
		}

	}

	private void renderCastingBars() {

		for (UnitInfo unit : world.getUnits()) {

			SkillInfo skill = unit.getActiveSkill();
			if (skill != null && skill.getStatus() == Status.CASTING) {
				final float castBarVertSpacing = 8; // space between the bottom of the unit and the top of the hpbar
				final float castBarHeight = 4;
				final float castBarWidth = 30;

				float x = unit.getX() - castBarWidth / 2;
				float y = unit.getY() + unit.getRadius() + castBarVertSpacing;

				// Draw the hp bar

				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.setColor(castBarColor);

				shapeRenderer.rect(x, y, castBarWidth * skill.getCasting().getProgress(), castBarHeight); // draws from
																											// bottom
																											// left
																											// corner.
				shapeRenderer.end();

				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.BLACK);

				shapeRenderer.rect(x, y, castBarWidth, castBarHeight);

				shapeRenderer.end();
			}
		}
	}

	private void renderOutlineAroundBusyRetreatingUnits() {
		renderThickOutlineIfTrueForAllUnits(HoloGL.rbg(30, 144, 255), (UnitInfo u) -> {
			return u.isBusyRetreating();
		});
	}

	private void renderOutlineAroundBusyCastingUnits() {
		renderThickOutlineIfTrueForAllUnits(HoloGL.rbg(30, 144, 255), (UnitInfo u) -> {
			return u.isCastingOrChanneling();
		});
	}

	private void renderOutlineAroundKnockbackedUnits() {
		renderModerateWidthOutlineIfTrueForAllUnits(Color.ORANGE, (UnitInfo u) -> {
			return u.getMotion().isBeingKnockedBack();
		});
	}

	private float hpBarWidthBase = 30;
	private float hpBarWidthMax = 2 * hpBarWidthBase;
	private float hpBarWidthMin = 0.5f * hpBarWidthBase;

	private Color spBarColor = HoloGL.rbg(204, 224, 255);

	private Color castBarColor = HoloGL.rbg(204, 224, 255);

	public void setUnitControls(Controls unitControls) {
		this.unitControls = unitControls;
	}

	/*
	 * Sets the world that Renderer should render
	 */
	public void setWorld(World world) {
		this.world = world;
		map = world.getMap();
	}

	public World getWorld() {
		return world;
	}

	public void setClearColor(Color color) {
		if (color == null) {
			System.out.println("Color given was null");
			return;
		}
		clearColor = color;
	}

	public void setEffectsHandler(EffectsHandler effects) {
		this.effects = effects;
	}

	void renderThickOutlineIfTrueForAllUnits(Color color, Predicate<UnitInfo> predicate) {
		world.doIfTrueForAllUnits(predicate, (UnitInfo u) -> {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 2.5f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 3.25f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 4, color);
		});
	}

	void renderModerateWidthOutlineIfTrueForAllUnits(Color color, Predicate<UnitInfo> predicate) {
		world.doIfTrueForAllUnits(predicate, (UnitInfo u) -> {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 2.5f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 3.0f, color);
		});
	}

}
