package com.mygdx.holowyth.units;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.holowyth.units.Item.EquipType;
import com.mygdx.holowyth.units.Item.ItemType;
import com.mygdx.holowyth.units.Unit.UnitType;
import com.mygdx.holowyth.util.misc.HoloMisc;
import com.mygdx.holowyth.util.tools.FunctionBindings;

public class StatsDemo extends ApplicationAdapter implements InputProcessor {

	ShapeRenderer r;
	OrthographicCamera c;

	Stage stage;

	Skin skin;
	private InputMultiplexer multiplexer;

	static Unit unit;
	static Item myItem;

	@Override
	public void create() {
		r = new ShapeRenderer();
		c = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		createStage();

		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);
		
		createTestUnits();
		
		// Bind hotkeys
		functionBindings.bindFunctionToKey(()-> unit.attack(unitB), Keys.G);
	}
	

	private void createTestUnits() {
		unit = new Unit("Arthur");

		loadUnitStats(unit);
		loadSomeEquipment(unit);
		unit.prepareUnit();

		unit.printInfo();
		

		unitB = new Unit("Bob");
		loadUnitStats2(unitB);
		loadArmor(unitB);
		
		unitB.prepareUnit();
		unitB.printInfo();
	}

	public static void loadUnitStats(Unit unit) {
	
		unit.baseStr = 7;
		unit.baseAgi = 5;
		unit.baseFort = 6;
		unit.basePercep = 6;
	
		unit.baseMaxHp = 100;
		unit.baseMaxSp = 50;
	
		unit.baseMoveSpeed = 2.3f;
	
		unit.level = 3;
	
		unit.unitType = UnitType.PLAYER;
	}

	public static void loadUnitStats2(Unit unit) {
	
		unit.baseStr = 5;
		unit.baseAgi = 5;
		unit.baseFort = 5;
		unit.basePercep = 5;
	
		unit.baseMaxHp = 100;
		unit.baseMaxSp = 50;
	
		unit.baseMoveSpeed = 2.3f;
	
		unit.level = 1;
	
		unit.unitType = UnitType.PLAYER;
	}

	public static void loadSomeEquipment(Unit unit) {
		Item sword = new Item("Red Sword");
	
		sword.damage = 8;
	
		sword.atkBonus = 2;
		sword.defBonus = 2;
		sword.accBonus = 4;
		sword.armorNegationBonus = 0.1f;
	
		sword.itemType = ItemType.EQUIPMENT;
		sword.equipType = EquipType.WEAPON;
	
		Item ring = new Item("Stone Ring");
		ring.itemType = ItemType.EQUIPMENT;
		ring.equipType = EquipType.ACCESSORY;
		ring.fortBonus = 2;
		ring.percepBonus = 1;
	
		unit.getEquip().mainHand = sword;
		unit.getEquip().accessory1 = ring;
	}
	
	public static void loadArmor(Unit unit) {
		Item armor = new Item("Steel Plate", EquipType.ARMOR);
		
		armor.armorBonus = 6;
		armor.dmgReductionBonus = 0.35f;
		
		unit.getEquip().torso = armor;
	}

	@Override
	public void render() {
		// Preparatory tasks
		c.update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Normal rendering activity

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();

	}

	private void createStage() {
		stage = new Stage(new ScreenViewport());

		skin = new Skin(Gdx.files.internal("myskin\\uiskin.json"));
		createRootTable();
	}

	private void testMethod() {
		System.out.println("foobar method");
	}

	// Make a table that fills to whole stage and add some buttons to it.
	private void createRootTable() {
		Table root = new Table();
		root.setFillParent(true);
		stage.addActor(root);

		root.debug();

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts\\fantasy_one.ttf"));

		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 24;
		BitmapFont font24 = generator.generateFont(parameter);
		// Want to use this font in a textButton
		LabelStyle l = new LabelStyle(font24, Color.WHITE);

		generator.dispose(); // don't forget to dispose to avoid memory leaks!

		// atlas = new TextureAtlas(Gdx.files.internal("packedimages/pack.atlas"));

	}

	private FunctionBindings functionBindings = new FunctionBindings();
	private Unit unitB;

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return functionBindings.runBoundFunction(keycode);
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
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

}
