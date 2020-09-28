package com.mygdx.holowyth.map;

import com.mygdx.holowyth.map.maps.Forest1;
import com.mygdx.holowyth.map.maps.Forest2;

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
