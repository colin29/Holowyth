package com.mygdx.holowyth.combatDemo.rendering;

import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.Controls;
import com.mygdx.holowyth.combatDemo.Controls.Context;
import com.mygdx.holowyth.combatDemo.World;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.HoloSprite;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.skill.ActiveSkill.Status;
import com.mygdx.holowyth.skill.SkillInfo;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
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

	Logger logger = LoggerFactory.getLogger(this.getClass());

	Holowyth game;

	// Rendering pipeline

	Camera worldCamera;
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;

	ShapeDrawerPlus shapeDrawer;

	// Screen lifetime components
	private Stage stage;
	private PathingModule pathingModule;

	// Map lifetime components
	private World world;
	private EffectsHandler gfx;

	// Map info
	private int mapWidth;
	private int mapHeight;

	// Graphics options

	private Color clearColor = Color.BLACK;

	private Controls controls;

	// Sub-components
	DebugRenderer debug;

	private PathfindingRenderer pathfinding;
	private UnitMotionRenderer unitMotion;
	private SandBoxRenderer sandbox;

	private TiledMapRenderer tiled;

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
		shapeDrawer = game.shapeDrawer;

		this.game = game;

		this.worldCamera = worldCamera;
		this.stage = stage;
		this.pathingModule = pathingModule;

		debug = new DebugRenderer(this);
		pathfinding = new PathfindingRenderer(this, pathingModule);
		unitMotion = new UnitMotionRenderer(this);
		sandbox = new SandBoxRenderer(this);
		tiled = new TiledMapRenderer(this);
	}

	/*
	 * Methods call should not set projection matrixes (it is assumed to be the world matrix). If they do they should restore the old state
	 */
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		worldCamera.update();

		batch.setProjectionMatrix(worldCamera.combined);

		tiled.renderMap();
		renderUnitHpSpBars();

		// Debug pathing
		if (Gdx.input.isKeyPressed(Keys.M)) {
			pathingModule.renderGraph(true);
			HoloGL.renderSegs(pathingModule.getObstacleExpandedSegs(), Color.PINK);
			renderCircles(pathingModule.getObstaclePoints(), Holo.UNIT_RADIUS, Color.PINK);

		}

		// 2: Render unit paths
		pathfinding.renderPaths(false);

		unitMotion.renderUnitDestinations(Color.GREEN);

		// 3: Render units and selection/status indicators

		renderOutlineAroundSlowedUnits(); // lower priority indicators are drawn first

		if (controls != null) {
			controls.clearDeadUnitsFromSelection();
			controls.renderCirclesOnSelectedUnits();
			pathfinding.renderUnitExpandedHitBodies();
		}

		renderUnits(delta);

		if (controls != null) {
			controls.renderSelectionBox(Controls.defaultSelectionBoxColor);
			renderOutlineAroundBusyRetreatingUnits();
			renderOutlineAroundBusyCastingUnits();
		}

		renderOutlineAroundKnockbackedUnits();
		renderOutlineAroundReeledUnits();
		renderOutlineAroundBlindedUnits();
		renderOutlineAroundStunnedUnits();

		// debug.renderUnitIdsOnUnits();

		// Render Aiming graphics
		renderCastingCircleIfSkill();
		renderAimingHelperIfSkillHas();
		// renderCircleIfAimingSkillGround();

		// 3.5: Render arrows

		debug.renderUnitKnockbackVelocities();

		for (Unit u : world.getUnits()) {
			u.renderAttackingArrow();
		}

		renderCastingBars();

		// 1: Render Obstacle Lines

		if (tiled.isMapLoaded()) {
			renderMapObstacles();
			// renderMapBoundaries();
		}

		// Render effects

		renderEffects();

		gfx.renderDamageEffects();
		gfx.renderBlockEffects(delta);

		// UI
		stage.act(delta);
		stage.draw();

	}

	@SuppressWarnings("unused")
	private void renderCircleIfAimingSkillGround() {
		if (controls.getContext() == Context.SKILL_GROUND) {
			var curSkill = (GroundSkill) controls.getCurSkill();
			var cursorPos = getWorldCoordinatesOfMouseCursor();
			HoloGL.renderCircleOutline(cursorPos.x, cursorPos.y, curSkill.defaultAimingHelperRadius, Color.RED);
		}
	}

	private void renderAimingHelperIfSkillHas() {

		if (controls.getContext() == Context.SKILL_GROUND) {
			var curSkill = (GroundSkill) controls.getCurSkill();
			if (curSkill.aimingGraphic == null)
				return;

			var cursorPos = getWorldCoordinatesOfMouseCursor();
			var unit = controls.getSelectedUnits().first();
			curSkill.aimingGraphic.render(cursorPos, unit, world, batch, shapeDrawer, game.assets);
		}

	}

	private void renderCastingCircleIfSkill() {
		if (controls.getContext() == Context.SKILL_GROUND) {

			var curSkill = (GroundSkill) controls.getCurSkill();
			var cursorPos = getWorldCoordinatesOfMouseCursor();

			if (curSkill.defaultAimingHelperRadius == 0)
				return;

			TextureRegion magicCircle = new TextureRegion(game.assets.get("img/effects/magicCircle_blue.png", Texture.class));
			magicCircle.getTexture().setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Nearest);

			// Sprite sprite = new Sprite(magicCircle);
			// sprite.set

			float width = curSkill.defaultAimingHelperRadius * 2 * 1.04f;
			HoloSprite sprite = new HoloSprite(magicCircle, cursorPos.x, cursorPos.y, width, 0, 0);

			float revsPerSecond = 1f / 40;
			int msForOneRotation = (int) (1000 / revsPerSecond);
			float rotationProgress = (System.currentTimeMillis() % msForOneRotation) / (float) msForOneRotation;
			sprite.rotation = rotationProgress * 360;

			float upperWidthInterval = 500;
			float lowerWidthInterval = 200;
			float minAlphaPercentage = 50;

			if (width > lowerWidthInterval) {
				float alphaScalingPercentage = Math.min(minAlphaPercentage,
						100 - (width - lowerWidthInterval) / (upperWidthInterval - lowerWidthInterval) * (100 - minAlphaPercentage));
				sprite.alphaScaling = alphaScalingPercentage / 100;
			}

			sprite.draw(batch);
		}
	}

	private Vector2 getWorldCoordinatesOfMouseCursor() {
		Vector3 vec = new Vector3(); // World coordinates of the click.
		vec = worldCamera.unproject(vec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
		return new Vector2(vec.x, vec.y);
	}

	private void renderCircles(List<Point> points, float radius, Color color) {
		batch.begin();
		shapeDrawer.setColor(color);
		for (var point : pathingModule.getObstaclePoints()) {
			shapeDrawer.circle(point.x, point.y, radius);
		}
		batch.end();
	}

	private void renderEffects() {
		for (Effect effect : world.getEffects()) {
			effect.render(batch, shapeDrawer, game.assets);
		}
	}

	private void renderUnits(float delta) {
		if (Holo.useTestSprites) {
			sandbox.renderUnitsWithTestSprites();
		} else {

			batch.begin();

			// Render unit circles
			for (Unit unit : world.getUnits()) {

				shapeDrawer.setColor(Color.PURPLE);

				if (unit.isAPlayerCharacter()) {
					shapeDrawer.setColor(Color.PURPLE);
				} else {
					shapeDrawer.setColor(Color.YELLOW);
				}

				shapeDrawer.setAlpha(unit.stats.isDead() ? 0.5f : 1);
				shapeDrawer.filledCircle(unit.x, unit.y, Holo.UNIT_RADIUS);

			}
			for (Unit unit : world.getUnits()) {
				shapeDrawer.setColor(Color.BLACK);
				shapeDrawer.setAlpha(unit.stats.isDead() ? 0.5f : 1);
				shapeDrawer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);
			}
			batch.end();

			for (Unit unit : world.getUnits()) {
				unit.graphics.updateAndRender(delta, batch);
			}

		}
	}

	private void renderMapObstacles() {
		shapeRenderer.setProjectionMatrix(worldCamera.combined);

		HoloGL.renderPoints(pathingModule.getObstaclePoints(), Color.GRAY);
		HoloGL.renderSegs(pathingModule.getObstacleSegs(), Color.GRAY);
	}

	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(worldCamera.combined);
		HoloGL.renderMapBoundaries(mapWidth, mapHeight);
	}

	private void renderUnitHpSpBars() {

		final float hpBarVertSpacing = 5; // space between the bottom of the unit and the top of the hpbar
		final float hpBarHeight = 4;
		final float spBarHeight = 3f;

		shapeRenderer.setProjectionMatrix(worldCamera.combined);

		for (UnitInfo unit : world.getUnits()) {

			UnitStatsInfo unitStats = unit.getStats();
			float virtualMaxHp = Holo.debugHighHpUnits ? unitStats.getMaxHp() / 10 : unitStats.getMaxHp();
			float hpRatio = unitStats.getHpRatio();
			float spRatio = unitStats.getSpRatio();

			float hpBarWidth = (float) Math.sqrt((virtualMaxHp / 100)) * hpBarWidthBase;
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

			if (unit.getSide() != Side.PLAYER && !Holo.debugDisplayEnemyCastingProgress) {
				continue;
			}

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
		renderThickOutlineIfTrueForAllUnits(HoloGL.rgb(30, 144, 255), (UnitInfo u) -> {
			return u.isBusyRetreating();
		});
	}

	private void renderOutlineAroundBusyCastingUnits() {
		renderThickOutlineIfTrueForAllUnits(HoloGL.rgb(30, 144, 255), (UnitInfo u) -> {
			return u.isCastingOrChanneling();
		});
	}

	private void renderOutlineAroundKnockbackedUnits() {
		renderModerateOutlineIfTrueForAllUnits(Color.ORANGE, (UnitInfo u) -> {
			return u.getMotion().isBeingKnockedBack();
		});
	}

	private void renderOutlineAroundStunnedUnits() {
		renderThickOutlineIfTrueForAllUnits(Color.SCARLET, (UnitInfo u) -> {
			return u.getStats().isStunned();
		});
	}

	private void renderOutlineAroundReeledUnits() {
		renderThickOutlineIfTrueForAllUnits(Color.ORANGE, (UnitInfo u) -> {
			return u.getStats().isReeled();
		});
	}

	private void renderOutlineAroundSlowedUnits() {
		renderThickOutlineIfTrueForAllUnits(Color.SKY, (UnitInfo u) -> {
			return u.getStats().isSlowed();
		});
	}

	private void renderOutlineAroundBlindedUnits() {
		renderThickOutlineIfTrueForAllUnits(Color.YELLOW, (UnitInfo u) -> {
			return u.getStats().isBlinded();
		});
	}

	private float hpBarWidthBase = 30;
	private float hpBarWidthMax = 2 * hpBarWidthBase;
	private float hpBarWidthMin = 0.5f * hpBarWidthBase;

	private Color spBarColor = HoloGL.rgb(204, 224, 255);

	private Color castBarColor = HoloGL.rgb(204, 224, 255);

	public void setUnitControls(Controls unitControls) {
		this.controls = unitControls;
	}

	public void setTiledMap(TiledMap tiledMap, int mapWidth, int mapHeight) {
		tiled.setMap(tiledMap);
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
	}

	/*
	 * Sets the world that Renderer should render
	 */
	public void setWorld(World world) {
		this.world = world;
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
		this.gfx = effects;
	}

	void renderThickOutlineIfTrueForAllUnits(Color color, Predicate<UnitInfo> predicate) {
		world.doIfTrueForAllUnits(predicate, (UnitInfo u) -> {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 2.5f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 3.25f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 4, color);
		});
	}

	void renderModerateOutlineIfTrueForAllUnits(Color color, Predicate<UnitInfo> predicate) {
		world.doIfTrueForAllUnits(predicate, (UnitInfo u) -> {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 2.5f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 3.0f, color);
		});
	}

}
