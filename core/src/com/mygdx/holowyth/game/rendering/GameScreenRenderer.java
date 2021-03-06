package com.mygdx.holowyth.game.rendering;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.collections4.iterators.PeekingIterator;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.game.Controls;
import com.mygdx.holowyth.game.MapInstanceInfo;
import com.mygdx.holowyth.game.rendering.tiled.TileObject;
import com.mygdx.holowyth.game.rendering.tiled.YSortedCell;
import com.mygdx.holowyth.game.rendering.tiled.YSortingTiledMapRenderer;
import com.mygdx.holowyth.game.Controls.Context;
import com.mygdx.holowyth.graphics.HoloGL;
import com.mygdx.holowyth.graphics.HoloSprite;
import com.mygdx.holowyth.graphics.effects.EffectsHandler;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.pathfinding.PathingRenderer;
import com.mygdx.holowyth.skill.ActiveSkill.Status;
import com.mygdx.holowyth.skill.SkillInfo;
import com.mygdx.holowyth.skill.effect.Effect;
import com.mygdx.holowyth.skill.skill.GroundSkill;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.interfaces.UnitInfo;
import com.mygdx.holowyth.unit.interfaces.UnitOrderable;
import com.mygdx.holowyth.unit.interfaces.UnitStatsInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.ShapeDrawerPlus;
import com.mygdx.holowyth.util.dataobjects.Point;
import com.mygdx.holowyth.world.map.GameMap;

/**
 * Handles all of GameScreenBase's rendering <br>
 * 
 * Should carry very little state except for which modules to render and some rendering flags
 * 
 * Has App lifetime
 * 
 * @author Colin Ta
 *
 */
public class GameScreenRenderer {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	Holowyth game;

	// Rendering resources (app-life time)
	Camera worldCamera;
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;
	ShapeDrawerPlus shapeDrawer;

	// Sub-components (app life-time)
	private DebugRenderer debug;
	private GamePathingRenderer gamePathing;
	private UnitMotionRenderer unitMotion;
	private YSortingTiledMapRenderer tiled;
	private PathingRenderer basicPathing;

	// Screen lifetime components
	private Stage stage;
	private PathingModule pathingModule;

	// Map
	private GameMap map;

	// Map Info
	/**
	 * In world units (not tiles)
	 */
	private int mapWidth;
	private int mapHeight;
	private int tileWidth;
	private int tileHeight;

	// Map lifetime components
	private MapInstanceInfo mapInstance;
	private Controls controls;
	private EffectsHandler gfx;

	// Graphics options
	private Color clearColor = Color.BLACK;

	private final int showMapPathingGraphKey = Keys.M;
	private final int renderMapLocationsKey = Keys.B;
	private final int renderMapRegionsKey = Keys.N;
	private final int renderTileGridKey = Keys.COMMA;

	private boolean renderMapRegions;
	private boolean renderMapLocations;
	private boolean renderTileGrid;

	/**
	 * The game, worldCamera, and other screen-lifetime modules are passed in.
	 * 
	 * @param game
	 * @param worldCamera
	 * @param stage
	 * @param pathingModule // May be null;
	 */
	public GameScreenRenderer(Holowyth game, Camera worldCamera, Stage stage, PathingModule pathingModule) {

		batch = game.batch;
		shapeRenderer = game.shapeRenderer;
		shapeDrawer = game.shapeDrawer;

		this.game = game;

		this.worldCamera = worldCamera;
		this.stage = stage;
		this.pathingModule = pathingModule;

		debug = new DebugRenderer(this);
		basicPathing = new PathingRenderer(pathingModule, shapeRenderer);
		gamePathing = new GamePathingRenderer(this, basicPathing);
		unitMotion = new UnitMotionRenderer(this);
	}

	/*
	 * Sub-methods should not set projection matrixes (it is assumed to be the world matrix). If they do
	 * they should restore the old state
	 */
	public void render(float delta) {
		HoloGL.clearScreenAndSetGLBlending(clearColor);
		worldCamera.update();
		batch.setProjectionMatrix(worldCamera.combined);

		if (map != null) {
			tiled.setView((OrthographicCamera) worldCamera);
			// Tiled map
			tiled.renderBaseLayers();

			renderUnitHpSpBars(); // render unit bars low as to not obscure more important info

			// Unit paths
			gamePathing.renderPaths(false);
			unitMotion.renderUnitDestinations(Color.GREEN);

			// Units
			renderUnitOutlines();
			renderUnitsAndYSortedTiles(delta);

			controls.renderUnitUnderCursor(Color.GREEN, Color.RED);

			renderSelectionBox();

			// debug.renderUnitIdsOnUnits();

			// Skill Aiming Graphics
			renderCastingCircleAimingHelperForGroundSkillThatDefineRadius();
			renderCustomAimingHelperForGroundSkillThatDefine();
			controls.renderMaxRangeIndicator();
			controls.renderLOSIndicator();

			// Misc. Combat Related
			debug.renderUnitKnockbackVelocities();
			renderUnitAttackingArrows();
			renderCastingBars();

			// Obstacle Edges
			renderMapObstacleEdgesIfKeyPressed();

			// Debug Map Visualizations
			renderMapDebugVisualizationsIfKeysPressed();
			renderTileGridIfToggled();

			// Effects
			renderInGameEffects();
			gfx.renderBlockEffects(delta);
			gfx.renderAnimatedEffects(delta);
			gfx.renderDamageTextEffects();
		}

		// UI
		stage.draw();

	}

	private void renderTileGridIfToggled() {
		if (Gdx.input.isKeyJustPressed(renderTileGridKey)) {
			renderTileGrid ^= true;
		}
		if (renderTileGrid)
			renderTileGrid();
	}

	private void renderSelectionBox() {
		if (controls != null) {
			controls.renderSelectionBox(Controls.defaultSelectionBoxColor);
		}
	}

	private void renderTileGrid() {
		shapeDrawer.setColor(Color.BLACK);
		shapeDrawer.setAlpha(1);
		float lineWidth = 0.5f;
		batch.begin();
		for (int x = 0; x <= mapWidth; x += tileWidth) { // draw columns
			shapeDrawer.line(x, 0, x, mapHeight, lineWidth);
		}
		for (int y = 0; y <= mapHeight; y += tileHeight) {
			shapeDrawer.line(0, y, mapWidth, y, lineWidth);
		}
		batch.end();
	}

	private void renderUnitsAndYSortedTiles(float delta) {

		int tileWidth = map.getTilemap().getProperties().get("tilewidth", Integer.class);
		int tileHeight = map.getTilemap().getProperties().get("tileheight", Integer.class);

		// Sort units in descending Y order
		@NonNull
		ArrayList<@NonNull Unit> sortedByY = new ArrayList<>(mapInstance.getUnits());
		sortedByY.sort((u1, u2) -> u1.getY() < u2.getY() ? 1 : -1);
		var units = new PeekingIterator<Unit>(sortedByY.iterator());

		var tileObjects = tiled.getTileObjects().iterator();
		List<Unit> renderedUnits = new ArrayList<>();
		while (tileObjects.hasNext()) {
			var tileObject = tileObjects.next();
			while (units.hasNext() && units.peek().y - units.peek().getRadius() + 4 > tileObject.baseYIndex * tileHeight
					+ tileHeight / 2) {
				var u = units.next();
				renderUnit(u, delta);
				renderedUnits.add(u);
			}
			tileObject.opacity = determineTileObjectOpacity(tileObject, renderedUnits, tileWidth, tileHeight);
			tileObject.tickFade();
			tiled.renderTileObject(tileObject, tileObject.getFadingOpacity());
		}

		// Render remaining units
		while (units.hasNext())
			renderUnit(units.next(), delta);
	}

	private float determineTileObjectOpacity(TileObject object, List<Unit> renderedUnits, int tileWidth,
			int tileHeight) {

		for (var unit : renderedUnits) {
			// ignore units that are not a full tile within the bounding box (this prevents hiding of small
			// objects too)
			float factor = 0.6f;
			if (!((unit.x > object.x1 + tileWidth * factor) && (unit.x < object.x2 - tileWidth * factor)
					&& (unit.y > object.y1 + tileHeight * factor) && (unit.y < object.y2 - tileHeight * factor))) {
				continue;
			}

			// If unit center is inside tile bounds
			for (var cell : object.cells) {
				if (Math.abs(unit.x - cell.getX()) <= tileWidth / 2
						&& Math.abs(unit.y - cell.getY()) <= tileHeight / 2) {
					return 0.5f;
				}
			}
		}
		return 1;
	}

	private void renderUnitOutlines() {
		renderOutlineAroundSlowedUnits(); // lower priority indicators are drawn first
		if (controls != null) {
			controls.renderCirclesOnSelectedUnits();
		}
		renderOutlineAroundBusyRetreatingUnits();
		renderOutlineAroundBusyCastingUnits();

		renderOutlineAroundKnockbackedUnits();
		renderOutlineAroundReeledUnits();
		renderOutlineAroundBlindedUnits();
		renderOutlineAroundTauntedUnits();
		renderOutlineAroundStunnedUnits();
	}

	private void renderUnit(Unit unit, float delta) {
		if (unit.graphics.getAnimatedSprite() != null) {
			unit.graphics.updateAndRender(delta, batch);
		} else {
			renderUnitCircleAsFallBack(unit);
		}
	}

	private void renderUnitCircleAsFallBack(Unit unit) {
		batch.begin();
		shapeDrawer.setColor(unit.isAPlayerCharacter() ? Color.PURPLE : Color.YELLOW);
		shapeDrawer.setAlpha(unit.stats.isDead() ? 0.5f : 1);
		shapeDrawer.filledCircle(unit.x, unit.y, Holo.UNIT_RADIUS);

		shapeDrawer.setColor(Color.BLACK);
		shapeDrawer.setAlpha(unit.stats.isDead() ? 0.5f : 1);
		shapeDrawer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);
		batch.end();
	}

	private void renderMapDebugVisualizationsIfKeysPressed() {
		if (Gdx.input.isKeyJustPressed(renderMapRegionsKey))
			renderMapRegions ^= true;

		if (Gdx.input.isKeyJustPressed(renderMapLocationsKey))
			renderMapLocations ^= true;

		if (renderMapRegions)
			GameMapRenderer.renderMapRegions(Holowyth.fonts.debugFont, map, shapeDrawer, batch);
		if (renderMapLocations)
			GameMapRenderer.renderLocations(Holowyth.fonts.debugFont, map, shapeDrawer, batch);
		GameMapRenderer.renderEntrances(Holowyth.fonts.debugFont, map, shapeDrawer, batch);

		if (Gdx.input.isKeyPressed(showMapPathingGraphKey)) {
			basicPathing.renderGraph(true);
			HoloGL.renderSegs(pathingModule.getObstacleExpandedSegs(), Color.PINK);
			renderCircles(pathingModule.getObstaclePoints(), Holo.UNIT_RADIUS, Color.PINK);
		}
	}

	private void renderUnitAttackingArrows() {
		for (UnitOrderable u : mapInstance.getUnits()) {
			if (u.isAttacking()) {
				HoloGL.renderArrow(u, u.getAttacking(), Color.RED);
			}
		}
	}

	/**
	 * Draws simple circle
	 */
	@SuppressWarnings("unused")
	private void renderAimingCircleForSkillGround() {
		if (controls.getContext() == Context.SKILL_GROUND) {
			var curSkill = (GroundSkill) controls.getCurSkill();
			var cursorPos = getWorldCoordinatesOfMouseCursor();
			HoloGL.renderCircleOutline(cursorPos.x, cursorPos.y, curSkill.defaultAimingHelperRadius, Color.RED);
		}
	}

	private void renderCustomAimingHelperForGroundSkillThatDefine() {

		if (controls.getContext() == Context.SKILL_GROUND) {
			var curSkill = (GroundSkill) controls.getCurSkill();
			if (curSkill.aimingGraphic == null)
				return;

			var cursorPos = getWorldCoordinatesOfMouseCursor();
			UnitOrderable unit = controls.getSelectedUnits().first();
			curSkill.aimingGraphic.render(cursorPos, unit, mapInstance, batch, shapeDrawer, game.assets);
		}

	}

	private void renderCastingCircleAimingHelperForGroundSkillThatDefineRadius() {
		if (controls.getContext() == Context.SKILL_GROUND) {

			var curSkill = (GroundSkill) controls.getCurSkill();
			var cursorPos = getWorldCoordinatesOfMouseCursor();

			if (curSkill.defaultAimingHelperRadius == 0)
				return;

			TextureRegion magicCircle = new TextureRegion(
					game.assets.get("img/effects/magicCircle_blue.png", Texture.class));
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
				float alphaScalingPercentage = Math.min(minAlphaPercentage, 100 - (width - lowerWidthInterval)
						/ (upperWidthInterval - lowerWidthInterval) * (100 - minAlphaPercentage));
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

	private void renderInGameEffects() {
		for (Effect effect : mapInstance.getEffects()) {
			effect.render(batch, shapeDrawer, game.assets);
		}
	}

	private void renderMapObstacleEdgesIfKeyPressed() {
		if (Holo.debugRenderMapObstaclesEdges || Gdx.input.isKeyPressed(showMapPathingGraphKey)) {
			renderMapObstacleEdges();
		}
	}

	private void renderMapObstacleEdges() {
		if (this.map != null) {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderSegs(pathingModule.getObstacleSegs(), Color.GRAY);
			HoloGL.renderPoints(pathingModule.getObstaclePoints(), Color.GRAY);
		}
	}

	@SuppressWarnings("unused")
	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(worldCamera.combined);
		HoloGL.renderMapBoundaries(mapWidth, mapHeight);
	}

	private void renderUnitHpSpBars() {

		final float hpBarVertSpacing = 5; // space between the bottom of the unit and the top of the hpbar
		final float hpBarHeight = 4;
		final float spBarHeight = 3f;

		shapeRenderer.setProjectionMatrix(worldCamera.combined);

		for (UnitInfo unit : mapInstance.getUnits()) {

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

		for (UnitInfo unit : mapInstance.getUnits()) {

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
			return u.getStatus().isStunned();
		});
	}

	private void renderOutlineAroundReeledUnits() {
		renderThickOutlineIfTrueForAllUnits(Color.ORANGE, (UnitInfo u) -> {
			return u.getStatus().isReeled();
		});
	}

	private void renderOutlineAroundSlowedUnits() {
		renderThickOutlineIfTrueForAllUnits(Color.SKY, (UnitInfo u) -> {
			return u.getStatus().isSlowedIgnoringBasicAttackSlow();
		});
	}

	private void renderOutlineAroundBlindedUnits() {
		renderThickOutlineIfTrueForAllUnits(Color.YELLOW, (UnitInfo u) -> {
			return u.getStatus().isBlinded();
		});
	}

	private void renderOutlineAroundTauntedUnits() {
		renderThickOutlineIfTrueForAllUnits(Color.PURPLE, (UnitInfo u) -> {
			return u.getStatus().isTaunted();
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

	public void setMap(GameMap map, int mapWidth, int mapHeight) {
		this.map = map;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		tileWidth = map.getTilemap().getProperties().get("tilewidth", Integer.class);
		tileHeight = map.getTilemap().getProperties().get("tileheight", Integer.class);

		tiled = new YSortingTiledMapRenderer(map.getTilemap());
	}

	public void setMapLifeTimeComponentsRefs(MapInstanceInfo world, Controls controls, EffectsHandler gfx) {
		this.mapInstance = world;
		this.controls = controls;
		this.gfx = gfx;
	}

	/*
	 * Sets the world that Renderer should render
	 */
	public void setMapInstance(MapInstanceInfo world) {
		this.mapInstance = world;
	}

	public MapInstanceInfo getMapInstance() {
		return mapInstance;
	}

	public void onMapClose() {
		map = null;

		mapWidth = 0;
		mapHeight = 0;

		mapInstance = null;
		controls = null;
		gfx = null;
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
		mapInstance.doIfTrueForAllUnits(predicate, (UnitInfo u) -> {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 0.5f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 1.25f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 2, color);
		});
	}

	void renderModerateOutlineIfTrueForAllUnits(Color color, Predicate<UnitInfo> predicate) {
		mapInstance.doIfTrueForAllUnits(predicate, (UnitInfo u) -> {
			shapeRenderer.setProjectionMatrix(worldCamera.combined);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 2.5f, color);
			HoloGL.renderCircleOutline(u.getX(), u.getY(), u.getRadius() + 3.0f, color);
		});
	}

}
