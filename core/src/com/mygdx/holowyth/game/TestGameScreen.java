package com.mygdx.holowyth.game;

import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gamedata.items.Weapons;
import com.mygdx.holowyth.unit.Unit;

public class TestGameScreen extends StandardGameScreen {

	GfxTest gfxTest;
	
	public TestGameScreen(Holowyth game) {
		super(game);
		
		addTestWeaponsToInventory();
		
		gfxTest = new GfxTest(batch, animations, shapeDrawer, assets);
		Unit unit = session.playerUnits.get(0);
		gfxTest.playEffectOverUnit(unit, "holy_cross.png", mapInstance, animations);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		gfxTest.render();
	}
	private void addTestWeaponsToInventory() {
		session.ownedItems.addItem(Weapons.spear.cloneObject());
		session.ownedItems.addItem(Weapons.club.cloneObject());
		session.ownedItems.addItem(Weapons.dagger.cloneObject());
		session.ownedCurrency.add(50);
	}


}
