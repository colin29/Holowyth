package com.mygdx.holowyth.combatDemo;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.HoloSprite;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.skill.Skill.Status;
import com.mygdx.holowyth.skill.SkillInfo;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.dataobjects.Point;

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
		renderPaths(false);

		renderUnitDestinations(Color.GREEN);

		// 3: Render units and selection indicators

		if (unitControls != null) {
			unitControls.clearDeadUnitsFromSelection();
			unitControls.renderCirclesOnSelectedUnits();
			renderUnitExpandedHitBodies();

			renderUnits();
			unitControls.renderSelectionBox(Controls.defaultSelectionBoxColor);
			renderCirclesAroundBusyRetreatingUnits();
			renderCirclesAroundBusyCastingUnits();
		} else {
			renderUnits();
		}
		renderCirclesAroundKnockbackedUnits();

		// 3.5: Render arrows

		renderUnitKnockbackVelocities();

		for (Unit u : world.getUnits()) {
			u.renderAttackingArrow();
		}

		// 1: Render Map

		if (this.map != null) {
			renderMapPolygons();
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

	private void renderCirclesAroundBusyRetreatingUnits() {
		renderThickOutlineAroundCertainUnits(HoloGL.rbg(30, 144, 255), (UnitInfo u) -> {
			return u.isRetreatCooldownActive();
		});
	}

	private void renderCirclesAroundBusyCastingUnits() {
		renderThickOutlineAroundCertainUnits(HoloGL.rbg(30, 144, 255), (UnitInfo u) -> {
			return u.isCastingOrChanneling();
		});
	}

	private void renderCirclesAroundKnockbackedUnits() {
		renderModerateWidthOutlineAroundCertainUnits(Color.ORANGE, (UnitInfo u) -> {
			return u.getMotion().isBeingKnockedBack();
		});
	}

	private void renderThickOutlineAroundCertainUnits(Color color, Predicate<UnitInfo> predicate) {
		doForAllUnits(predicate, (UnitInfo u) -> {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 2.5f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 3.25f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 4, color);
		});
	}

	private void renderModerateWidthOutlineAroundCertainUnits(Color color, Predicate<UnitInfo> predicate) {
		doForAllUnits(predicate, (UnitInfo u) -> {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 2.5f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 3.0f, color);
		});
	}

	private void doForAllUnits(Predicate<UnitInfo> predicate, Consumer<UnitInfo> task) {
		for (UnitInfo unit : world.getUnits()) {
			if (predicate.test(unit)) {
				task.accept(unit);
			}
		}
	}

	private float pathThickness = 2f;

	@SuppressWarnings("unused")
	private void renderPaths(boolean renderIntermediatePaths) {
		// Render Path

		if (renderIntermediatePaths) {
			pathingModule.renderIntermediateAndFinalPaths(world.getUnits());
		} else {
			for (Unit unit : world.getUnits()) {
				if (unit.motion.getPath() != null) {
					renderPath(unit.motion.getPath(), Color.GRAY, false);
				}
			}
		}

		for (Unit u : world.getUnits()) {
			u.motion.renderNextWayPoint();
		}

	}

	private void renderPlayerUnreachedWaypoints(Color color) {
		for (Unit unit : world.getUnits()) {
			if (unit.isAPlayerCharacter() && unit.motion.getPath() != null) {
				Path path = unit.motion.getPath();
				for (int i = unit.motion.getWayPointIndex(); i < path.size(); i++) {
					Point waypoint = path.get(i);
					shapeRenderer.begin(ShapeType.Filled);
					shapeRenderer.setColor(color);
					shapeRenderer.circle(waypoint.x, waypoint.y, 4f);
					shapeRenderer.end();

					shapeRenderer.begin(ShapeType.Line);
					shapeRenderer.setColor(Color.BLACK);
					shapeRenderer.circle(waypoint.x, waypoint.y, 4f);
					shapeRenderer.end();
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void renderPlayerVelocityArrow() {
		doForAllUnits((UnitInfo u) -> (u.isAPlayerCharacter() && u.getMotion().getVelocityMagnitude() > 0.01f),
				(UnitInfo u) -> {
					float scale = 15;
					HoloGL.renderArrow(u.getPos(), u.getMotion().getVelocity().setLength(scale), Color.GREEN);
				});
	}

	private void renderUnitKnockbackVelocities() {
		doForAllUnits(
				(UnitInfo u) -> (u.getMotion().isBeingKnockedBack()
						&& u.getMotion().getKnockBackVelocity().len() > 0.01f),
				(UnitInfo u) -> {
					float scale = 15;
					HoloGL.renderArrow(u.getPos(),
							u.getMotion().getKnockBackVelocity().setLength(scale), Color.ORANGE);
				});
	}

	private void renderUnitDestinations(Color color) {

		for (Unit unit : world.getUnits()) {
			if (unit.isAPlayerCharacter() && unit.motion.getPath() != null) {

				Path path = unit.motion.getPath();
				Point finalPoint = path.get(path.size() - 1);
				shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.setColor(color);
				shapeRenderer.circle(finalPoint.x, finalPoint.y, 4f);
				shapeRenderer.end();

				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.circle(finalPoint.x, finalPoint.y, 4f);
				shapeRenderer.end();
			}

		}
	}

	private void renderPath(Path path, Color color, boolean renderPoints) {
		HoloPF.renderPath(path, color, renderPoints, pathThickness, shapeRenderer);
	}

	private void renderUnits() {
		if (Holo.useTestSprites) {
			renderUnitsWithTestSprites();
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

	@SuppressWarnings("unused")
	private void renderUnitExpandedHitBodies() {
		for (Unit u : world.getUnits()) {
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + Holo.UNIT_RADIUS, Color.GRAY);
		}
	}

	private void renderUnitsWithTestSprites() {

		TextureRegion witchTex = new TextureRegion(game.assets.get("img/witch.png", Texture.class));
		witchTex.getTexture().setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Nearest);

		for (Unit unit : world.getUnits()) {
			if (unit.isAPlayerCharacter()) {
				HoloSprite player = new HoloSprite(witchTex, unit.x, unit.y, 30, 0, 20);
				player.draw(batch);
			} else {
				HoloSprite player = new HoloSprite(witchTex, unit.x, unit.y, 30, 0, 20);
				player.draw(batch);
			}
		}

	}

	private void renderMapPolygons() {
		shapeRenderer.setProjectionMatrix(worldCamera.combined);
		shapeRenderer.setColor(Color.BLACK);
		HoloGL.renderPolygons(map.polys);
	}

	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(worldCamera.combined);
		HoloGL.renderMapBoundaries(map);
	}

	private float hpBarWidthBase = 30;
	private float hpBarWidthMax = 2 * hpBarWidthBase;
	private float hpBarWidthMin = 0.5f * hpBarWidthBase;

	private Color spBarColor = HoloGL.rbg(204, 224, 255);

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

	private Color castBarColor = HoloGL.rbg(204, 224, 255);

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

	public void setUnitControls(Controls unitControls) {
		this.unitControls = unitControls;
	}

	/*
	 * Sets the world that Renderer should render
	 */
	public void setWorld(World world) {
		this.world = world;
		this.map = world.getMap();
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
}
