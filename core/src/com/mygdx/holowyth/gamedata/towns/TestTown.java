package com.mygdx.holowyth.gamedata.towns;


import com.mygdx.holowyth.gamedata.items.Weapons;
import com.mygdx.holowyth.town.model.Town;

public class TestTown extends Town {
	{
	setName("testTown");
	shop.addItemStock(Weapons.longSword, 350);
	shop.addItemStock(Weapons.shortSword, 120);
	shop.addItemStock(Weapons.staff, 50);
	shop.addItemStock(Weapons.dagger, 50);
	}
	
}
