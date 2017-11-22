package com.mygdx.holowyth.editor;

import java.io.IOException;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.polygon.Polygon;
import com.mygdx.holowyth.util.HoloGL;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.HoloUI;
import com.mygdx.holowyth.util.constants.Holo;
import com.mygdx.holowyth.util.data.Pair;
import com.mygdx.holowyth.util.exception.ErrorCode;
import com.mygdx.holowyth.util.exception.HoloException;
import com.mygdx.holowyth.util.tools.KeyTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class PolyMapEditor implements Screen, InputProcessor {

	final Holowyth game;

	// Rendering and pipeline variables
	OrthographicCamera camera;
	OrthographicCamera fixedCam;
	ShapeRenderer shapeRenderer;
	SpriteBatch batch;

	// Input
	InputMultiplexer multiplexer = new InputMultiplexer();

	// Scene2D
	Stage stage;
	Table root;

	Skin skin = VisUI.getSkin();

	// Modes
	private MODE current_mode;

	private enum MODE {
		VIEWING, DRAWING
	}

	// Appearance
	Color defaultClearColor = new Color(0.5f, 0.8f, 1f, 1);
	Color drawingClearColor = new Color(0.8f, 1f, 0.8f, 1);
	Color clearColor = defaultClearColor;

	// App Fields
	MapPolygonDrawer drawer;

	/**
	 * Note: Don't directly reassign this reference, instead use setMap
	 */
	private Field map;

	// Normal Variables

	public PolyMapEditor(final Holowyth game) {
		this.game = game;
		this.map = null;
		// Initialize stage + rendering tools
		stage = new Stage(new ScreenViewport());

		camera = new OrthographicCamera();
		camera.setToOrtho(false, game.resX, game.resY);
		fixedCam = new OrthographicCamera();
		fixedCam.setToOrtho(false, game.resX, game.resY); // will always stay at this position.

		shapeRenderer = game.shapeRenderer;
		batch = game.batch;

		// App Logic
		drawer = new MapPolygonDrawer(this.camera);
		this.current_mode = MODE.VIEWING;

		createUI();

		// Input and Controls
		Gdx.input.setCursorCatched(Holo.enableCursorGrabbing);
	}

	FPSLogger fps = new FPSLogger();

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT
				| (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		camera.update();

		if (this.map != null) {
			renderMapPolygons();
			renderMapBoundaries();
		}

		// UI
		stage.act(delta);
		stage.draw();

		displayTitleBarInformation();
		disableInvalidOptions();

		// Cursor related
		keepCursorInScreen();
		renderCursor();

		// User Controls
		handleMouseCameraScroll(delta);
		handleKeyboardCameraScroll(delta);

		// fps.log();

		// Testing area
	}

	@Override
	public void show() {
		System.out.println("Screen showed");

		multiplexer.clear();
		multiplexer.addProcessor(keyTracker);
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		System.out.println("Screen hidden");
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	private void createUI() {
		root = new Table();
		root.setFillParent(true);

		createOptionButtons();

		// Testing Area

		// End of Testing Area

		root.left().bottom();
		// root.debug();
		stage.addActor(root);
	}

	// Rendering the map
	private void renderMapPolygons() {
		shapeRenderer.setProjectionMatrix(camera.combined); // Render as seen by the main camera
		shapeRenderer.setColor(0, 0, 0, 1);
		HoloGL.renderPolygons(map.polys, shapeRenderer);
		if (this.current_mode == MODE.DRAWING) {
			this.drawer.render(shapeRenderer);
		}
	}

	private void renderMapBoundaries() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		HoloGL.renderMapBoundaries(map, shapeRenderer);
	}

	// Editor UI
	private void createOptionButtons() {

		root.row().space(10);

		Cell<TextButton> c1 = HoloUI.textButton(root, "Save PolyMap", skin, () -> openFileChooserToSaveMap());
		Cell<TextButton> c2 = HoloUI.textButton(root, "Load PolyMap", skin, () -> confirmLoadMap());
		Cell<TextButton> c3 = HoloUI.textButton(root, "New Map", skin, () -> confirmNewMap());
		Cell<TextButton> c4 = HoloUI.textButton(root, "Map Properties", skin, () -> createMapPropertiesDialog());

		disableWhenInvalid(c1.getActor(), () -> this.current_mode == MODE.VIEWING && this.map != null);
		disableWhenInvalid(c2.getActor(), () -> this.current_mode == MODE.VIEWING && true);
		disableWhenInvalid(c3.getActor(), () -> this.current_mode == MODE.VIEWING && true);
		disableWhenInvalid(c4.getActor(), () -> this.current_mode == MODE.VIEWING && this.map != null);

	}

	ArrayList<Pair<Button, BooleanSupplier>> monitoredButtons = new ArrayList<Pair<Button, BooleanSupplier>>();

	private void disableWhenInvalid(Button button, BooleanSupplier condition) {
		monitoredButtons.add(new Pair<Button, BooleanSupplier>(button, condition));
	}

	private void disableInvalidOptions() {
		for (Pair<Button, BooleanSupplier> p : monitoredButtons) {
			Button b = p.first();
			BooleanSupplier condition = p.second();
			if (condition.getAsBoolean()) { // If button is valid:
				b.setDisabled(false);
			} else {
				b.setDisabled(true);
			}
		}
	}

	private void confirmLoadMap() {
		HoloUI.confirmationDialog(this.map != null && this.map.hasUnsavedChanges, stage, skin, "Confirm Load Map",
				"Unsaved Map Data. Do you want to save map?", "Save", "Don't Save", () -> {
					openFileChooserToSaveMap();
				}, () -> {
					openFileChooserToLoadMap();
				});
	}

	private void confirmNewMap() {
		HoloUI.confirmationDialog(this.map != null && this.map.hasUnsavedChanges, stage, skin, "Confirm New Map",
				"Unsaved Map Data. Do you want to save map?", "Save", "Don't Save", () -> {
					openFileChooserToSaveMap();
				}, () -> {
					createNewMapDialog();
				});
	}

	private void createNewMapDialog() {

		final Dialog dialog = new Dialog("New Map Settings", skin);
		stage.addActor(dialog);

		Table header = dialog.getTitleTable();
		Table contents = dialog.getContentTable();
		Table buttons = dialog.getButtonTable();

		HoloUI.exitButton(header, skin, dialog).width(10);

		header.align(Align.center);

		final TextField nameField = new TextField(Holo.defaultMapName, skin);
		final TextField widthField = new TextField("", skin);
		final TextField heightField = new TextField("", skin);

		contents.add(new Label("Map name: ", skin), nameField);
		contents.row();
		contents.add(new Label("Width: ", skin), widthField);
		contents.row();
		contents.add(new Label("Height: ", skin), heightField);

		TextButton defaultButton = new TextButton("Default", skin);
		TextButton confirmButton = new TextButton("Confirm", skin);
		final Label info = new Label("", skin);

		buttons.add(info).expandX().align(Align.left);
		buttons.add(defaultButton, confirmButton);

		buttons.align(Align.center);

		defaultButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				nameField.setText(Holo.defaultMapName);
				widthField.setText(Integer.toString(Holo.defaultMapWidth));
				heightField.setText(Integer.toString(Holo.defaultMapHeight));
			}
		});
		confirmButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int width, height;
				try {
					width = Integer.parseInt(widthField.getText());
					height = Integer.parseInt(heightField.getText());
				} catch (NumberFormatException e) {
					info.setText("Invalid input");
					info.setColor(Color.RED);
					return;
				}

				// Create the new map
				try {
					Field newMap = new Field(width, height);
					newMap.name = nameField.getText();
					PolyMapEditor.this.loadMap(newMap);
				} catch (HoloException e) {
					if (e.code == ErrorCode.INVALID_DIMENSIONS) {
						info.setText("Invalid Map Dimensions");
						info.setColor(Color.RED);
					}
				}
				dialog.remove();
			}
		});

		dialog.pack();
		dialog.setPosition(Gdx.graphics.getWidth() / 2 - dialog.getWidth() / 2,
				Gdx.graphics.getHeight() / 2 - dialog.getHeight() / 2);
	}

	private void createMapPropertiesDialog() {

		final Dialog dialog = new Dialog("Map Properties", skin);
		stage.addActor(dialog);

		Table header = dialog.getTitleTable();
		Table contents = dialog.getContentTable();
		Table buttons = dialog.getButtonTable();

		HoloUI.exitButton(header, skin, dialog).width(10);

		header.align(Align.center);

		// Add Property fields
		final TextField nameField = new TextField(map.name, skin);
		final TextField widthField = new TextField(Integer.toString(map.width()), skin);
		final TextField heightField = new TextField(Integer.toString(map.height()), skin);

		contents.add(new Label("Map name: ", skin), nameField);
		contents.row();
		contents.add(new Label("Width: ", skin), widthField);
		contents.row();
		contents.add(new Label("Height: ", skin), heightField);

		// Add Buttons
		TextButton confirmButton = new TextButton("Confirm", skin);
		final Label info = new Label("", skin);

		buttons.add(info).expandX().align(Align.left);
		buttons.add(confirmButton);

		buttons.align(Align.center);

		confirmButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				// Validate input and set map values
				int width, height;
				try {
					width = Integer.parseInt(widthField.getText());
					height = Integer.parseInt(heightField.getText());
				} catch (NumberFormatException e) {
					info.setText("Invalid input");
					info.setColor(Color.RED);
					return;
				}

				try {
					PolyMapEditor.this.map.setDimensions(width, height);
				} catch (HoloException e) {
					if (e.code == ErrorCode.INVALID_DIMENSIONS) {
						info.setText("Invalid Map Dimensions");
						info.setColor(Color.RED);
						return;
					} else {
						throw (e);
					}
				}

				PolyMapEditor.this.map.name = nameField.getText();
				map.hasUnsavedChanges = true;
				dialog.remove();
			}
		});

		dialog.pack();
		dialog.setPosition(Gdx.graphics.getWidth() / 2 - dialog.getWidth() / 2,
				Gdx.graphics.getHeight() / 2 - dialog.getHeight() / 2);
	}

	private void displayTitleBarInformation() {
		if (this.map == null) {
			Gdx.graphics.setTitle(Holo.titleName + " --- " + "No map loaded");
		} else {
			String starText;
			starText = (this.map.hasUnsavedChanges) ? "*" : "";
			Gdx.graphics.setTitle(
					Holo.titleName + " --- " + map.name + " [" + map.width() + "x" + map.height() + "] " + starText);
		}

	}

	// Editor tools
	private void enterDrawPolygonMode() {
		if (this.current_mode == MODE.DRAWING) {
			return;
		} else {
			this.current_mode = MODE.DRAWING;
		}
		drawer.parentCamera = this.camera;
		drawer.setMap(this.map);

		this.clearColor = drawingClearColor;

		System.out.println("Entering draw polygon mode");

		multiplexer.addProcessor(0, drawer);
	}

	private void exitDrawPolygonMode() {
		if (this.current_mode != MODE.DRAWING) {
			return;
		} else {
			this.current_mode = MODE.VIEWING;
		}
		drawer.clearPartiallyDrawnPolygons();
		multiplexer.removeProcessor(drawer);

		this.clearColor = defaultClearColor;

		System.out.println("Exited draw polygon mode");
	}

	// Saving and Loading Maps

	private void loadMapFromDisk(String pathname) {
		try{
		Field loadedMap = HoloIO.getMapFromDisk(pathname);
		loadMap(loadedMap);
		} catch (HoloException e){
			if(e.code == ErrorCode.IO_EXCEPTION){
				System.out.println("IO Error, map not loaded");
				return;
			}
		}

	}
	
	private void saveMapToDisk(String pathname) throws IOException{
		HoloIO.saveMapToDisk(pathname, this.map);
	}

	// Cursor Related
	/**
	 * Manually renders a cursor sprite if the cursor grabbing is enabled (which hides the default cursor).
	 */
	private void renderCursor() {
		batch.setProjectionMatrix(fixedCam.combined);
		if (Gdx.input.isCursorCatched()) {
			batch.begin();
			Texture cursorImg = game.assets.get("icons/cursors/cursor.png", Texture.class);

			batch.draw(cursorImg, Gdx.input.getX(),
					Gdx.graphics.getHeight() - Gdx.input.getY() - cursorImg.getHeight());

			batch.end();
		}
	}

	private void keepCursorInScreen() {
		if (Gdx.input.isCursorCatched()) {
			int x, y;
			x = Gdx.input.getX();
			y = Gdx.input.getY();

			int maxX, maxY, minX, minY;
			maxX = Gdx.graphics.getWidth();
			maxY = Gdx.graphics.getHeight();
			minX = minY = 0;

			if (x > maxX) {
				x = maxX;
			} else if (x < minX) {
				x = minX;
			}
			if (y > maxY) {
				y = maxY;
			} else if (y < minY) {
				y = minY;
			}

			Gdx.input.setCursorPosition(x, y);
		}
	}

	private float mouseScrollSpeed = 450; // in pixels per second

	/**
	 * Scroll the Camera if the mouse is on the edge of the screen
	 */
	private void handleMouseCameraScroll(float delta) {
		if (Gdx.input.isCursorCatched()) {
			int mouseX, mouseY;
			mouseX = Gdx.input.getX();
			mouseY = Gdx.input.getY();

			if (mouseX == Gdx.graphics.getWidth()) {
				camera.position.x += mouseScrollSpeed * delta;
			} else if (mouseX == 0) {
				camera.position.x -= mouseScrollSpeed * delta;
			}
			if (mouseY == Gdx.graphics.getHeight()) {
				camera.position.y -= mouseScrollSpeed * delta;
			} else if (mouseY == 0) {
				camera.position.y += mouseScrollSpeed * delta;
			}
		}
	}

	// Keyboard Related

	private float keyboardScrollSpeed = 450;

	private void handleKeyboardCameraScroll(float delta) {
		if (keyTracker.isKeyDown(Keys.RIGHT)) {
			camera.position.x += keyboardScrollSpeed * delta;
		}
		if (keyTracker.isKeyDown(Keys.LEFT)) {
			camera.position.x -= keyboardScrollSpeed * delta;
		}
		if (keyTracker.isKeyDown(Keys.UP)) {
			camera.position.y += keyboardScrollSpeed * delta;
		}
		if (keyTracker.isKeyDown(Keys.DOWN)) {
			camera.position.y -= keyboardScrollSpeed * delta;
		}

	}

	// Input Related
	
	final int[] TRACKED_KEYS = new int[] { Keys.LEFT, Keys.RIGHT, Keys.UP, Keys.DOWN };
	KeyTracker keyTracker = new KeyTracker(TRACKED_KEYS, multiplexer);
	
	// Misc. Functions
	
	

	/* ^^^^^^ End of User Methods ^^^^^^ */

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.A) {
			if (this.map != null) {
				enterDrawPolygonMode();
				return true;
			}
		}
		if (keycode == Keys.D) {
			exitDrawPolygonMode();
			return true;
		}
		if (keycode == Keys.L) {
			loadMapFromDisk("../saveFiles/polySaveFile.ser");
			return true;
			
		}

		// toggle cursor-lock (and ability to mouse scroll)
		if (keycode == Keys.P) {
			if (Gdx.input.isCursorCatched()) {
				int mx = Gdx.input.getX();
				int my = Gdx.input.getY();
				Gdx.input.setCursorCatched(false);
				Gdx.input.setCursorPosition(mx, my);

			} else {
				Gdx.input.setCursorCatched(true);
			}
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
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

	public Field getMap() {
		return this.map;
	}

	/**
	 * Logically speaking, this should be called whenever a new map is loaded.
	 * 
	 */
	private void loadMap(Field map) {
		System.out.println("New map loaded");
		this.map = map;
		map.hasUnsavedChanges = false;

		camera.position.set(map.width() / 2, map.height() / 2, 0);
	}

	private void openFileChooserToSaveMap() {
		System.out.println("Opening Save Dialog");
		
		game.fileChooser.setMode(Mode.SAVE);
		game.fileChooser.setSelectionMode(SelectionMode.FILES);
		game.fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				System.out.println("Selected file to save to to " + file.get(0).file().getAbsolutePath());
				try {
					saveMapToDisk(file.get(0).file().getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		stage.addActor(game.fileChooser);
		game.fileChooser.setDirectory(Holo.mapsDirectory);
	}
	
	private void openFileChooserToLoadMap() {
		System.out.println("Opening Load Dialog");
		stage.addActor(game.fileChooser);
		
		game.fileChooser.setMode(Mode.OPEN);
		game.fileChooser.setSelectionMode(SelectionMode.FILES);
		game.fileChooser.setListener(new FileChooserAdapter() {
			@Override
			public void selected(Array<FileHandle> file) {
				System.out.println("Selected file: " + file.get(0).file().getAbsolutePath());

				System.out.println("Removed Load Dialog");
				loadMapFromDisk(file.get(0).file().getAbsolutePath());
			}
		});
		
		game.fileChooser.setDirectory(Holo.mapsDirectory);
	}

}