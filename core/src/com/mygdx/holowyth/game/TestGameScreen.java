package com.mygdx.holowyth.game;

import com.mygdx.holowyth.Holowyth;
import com.mygdx.holowyth.gamedata.items.Weapons;
import com.mygdx.holowyth.graphics.effects.animated.EffectCenteredOnUnit;
import com.mygdx.holowyth.unit.Unit;

public class TestGameScreen extends StandardGameScreen {

	public TestGameScreen(Holowyth game) {
		super(game);
		
		addTestWeaponsToInventory();
	}

	@Override
	public void render(float delta) {
		super.render(delta);
	}
	private void addTestWeaponsToInventory() {
		session.ownedItems.addItem(Weapons.spear.cloneObject());
		session.ownedItems.addItem(Weapons.club.cloneObject());
		session.ownedItems.addItem(Weapons.dagger.cloneObject());
		session.ownedCurrency.add(50);
	}


}
