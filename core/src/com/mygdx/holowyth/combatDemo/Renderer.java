package com.mygdx.holowyth.combatDemo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.combatDemo.effects.EffectsHandler;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.pathfinding.HoloPF;
import com.mygdx.holowyth.pathfinding.Path;
import com.mygdx.holowyth.pathfinding.PathingModule;
import com.mygdx.holowyth.unit.Unit;
import com.mygdx.holowyth.unit.Unit.Side;
import com.mygdx.holowyth.unit.UnitInfo;
import com.mygdx.holowyth.unit.UnitStatsInfo;
import com.mygdx.holowyth.util.Holo;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.data.Point;
import com.mygdx.holowyth.util.tools.Timer;

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

	public boolean renderUnitExpandedHitBodies = false;

	private UnitControls unitControls;

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
	}

	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		worldCamera.update();

		batch.setProjectionMatrix(worldCamera.combined);

		renderUnitHpBars();
		
		// 1: Render Map
		
		if (this.map != null) {
			renderMapPolygons();
			// pathingModule.renderExpandedMapPolygons();
			renderMapBoundaries();
		}

		// 2: Render unit paths
		// renderPaths(false);

		renderUnitDestinations(Color.GREEN);

		// 3: Render units and selection indicators
		
		if (unitControls == null) {
			renderUnits();
		} else {
			unitControls.renderCirclesOnSelectedUnits();
			renderUnits();
			unitControls.renderSelectionBox(UnitControls.defaultSelectionBoxColor);
		}

		for (Unit u : world.units) {
			u.renderAttackingLine(shapeRenderer);
		}
		
		// Render effects
		
		effects.renderDamageEffects();
		
		effects.renderBlockEffects(delta);
		

		// UI
		stage.act(delta);
		stage.draw();

	}

	private float pathThickness = 2f;

	private void renderPaths(boolean renderIntermediatePaths) {
		// Render Path

		if (renderIntermediatePaths) {
			pathingModule.renderIntermediateAndFinalPaths(world.units);
		} else {
			for (Unit unit : world.units) {
				if (unit.motion.path != null) {
					renderPath(unit.motion.path, Color.GRAY, false);
				}
			}
		}

		for (Unit u : world.units) {
			u.motion.renderNextWayPoint(shapeRenderer);
		}

	}

	private void renderUnitDestinations(Color color) {

		for (Unit unit : world.units) {
			if (unit.isPlayerCharacter() && unit.motion.path != null) {

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
			for (Unit unit : world.units) {
				shapeRenderer.begin(ShapeType.Filled);

				if (unit.isPlayerCharacter()) {
					shapeRenderer.setColor(Color.PURPLE);
				} else {
					shapeRenderer.setColor(Color.YELLOW);
				}

				shapeRenderer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);

				shapeRenderer.end();
			}

			// Render an outline around the unit
			for (Unit unit : world.units) {
				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.circle(unit.x, unit.y, Holo.UNIT_RADIUS);
				shapeRenderer.end();
			}
		}

	}

	private void renderUnitExpandedHitBodies() {
		for (Unit u : world.units) {
			HoloGL.renderCircleOutline(u.x, u.y, u.getRadius() + Holo.UNIT_RADIUS, shapeRenderer, Color.GRAY);
		}
	}

	private void renderUnitsWithTestSprites() {

		TextureRegion witchTex = new TextureRegion(game.assets.get("img/witch.png", Texture.class));
		witchTex.getTexture().setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Nearest);

		for (Unit unit : world.units) {
			if (unit.isPlayerCharacter()) {
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
		HoloGL.renderPolygons(map.polys, shapeRenderer);
	}

	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(worldCamera.combined);
		HoloGL.renderMapBoundaries(map, shapeRenderer);
	}

	private float hpBarWidthBase = 30;
	private float hpBarWidthMax = 2 * hpBarWidthBase;
	private float hpBarWidthMin = 0.5f * hpBarWidthBase;
	
	
	private void renderUnitHpBars() {
		
		final float hpBarVertSpacing = 5;
		final float hpBarHeight = 4;
		
		shapeRenderer.setProjectionMatrix(worldCamera.combined);
		
		
		for (UnitInfo unit : world.units) {
			
			UnitStatsInfo unitStats = unit.getStats();
			float maxHp = unitStats.getMaxHp();
			float hpRatio = unitStats.getHpRatio();
			
			float hpBarWidth = (float) Math.sqrt((maxHp / 100)) * hpBarWidthBase;
			hpBarWidth = Math.max(hpBarWidth, hpBarWidthMin);
			hpBarWidth = Math.min(hpBarWidth, hpBarWidthMax);
			
			
			float x = unit.getX() - hpBarWidth/2;
			float y = unit.getY() - unit.getRadius() - hpBarHeight - hpBarVertSpacing;
			
			
			// Draw the hp bar

			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(Color.RED);
			
			shapeRenderer.rect(x, y, hpBarWidth * hpRatio, hpBarHeight);
			shapeRenderer.end();
			
			
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.BLACK);
						
			shapeRenderer.rect(x, y, hpBarWidth, hpBarHeight);
			
			shapeRenderer.end();
			
		}
		
	}

	public void setUnitControls(UnitControls unitControls) {
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
