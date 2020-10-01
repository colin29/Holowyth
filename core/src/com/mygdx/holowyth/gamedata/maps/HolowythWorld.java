package com.mygdx.holowyth.gamedata.maps;

import com.mygdx.holowyth.world.World;

/**
 * Stores a bunch of 'template' maps. To use the maps you should copy construct a new instance to use.
 *
 */
public class HolowythWorld extends World {
	
	{
		setName("Holowyth");
		setAuthor("Rimilel");
		putMap(new Forest1());
		putMap(new Forest2());
	}

	
}
