package com.mygdx.holowyth.util.serialization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mygdx.holowyth.util.HoloIO;
import com.mygdx.holowyth.util.exceptions.HoloIOException;

@SuppressWarnings("unused")
public class SimpleMapReserializer {
	
//	private Logger logger = LoggerFactory.getLogger(this.getClass());
//	
//	private static final String simpleMapsDir = "./../desktop/resources/assets/maps/old/";
//
//	public void listDir() {
//		logger.debug("Listing dir");
//		FileHandle dir = new FileHandle(simpleMapsDir);
//		var contents = dir.list();
//		for(var file : contents) {
//			logger.debug(file.name());
//		}
//	}
//	public void reserializeAllMaps() {
//		File dirPath = new File(simpleMapsDir + "output");
//		dirPath.mkdir();
//		FileHandle dir = new FileHandle(simpleMapsDir);
//		var contents = dir.list();
//		for(var file : contents) {
//			if(file.extension().equals("map")) {
//				logger.debug(file.name());
//				SimpleMap oldMap = HoloIO.getMapFromDisk(simpleMapsDir + file.name());
//				logger.debug("Got old map: {} " , oldMap);
//				com.mygdx.holowyth.world.map.simplemap.SimpleMap map = fieldToSimpleMap(oldMap);
//				map.name = file.name();
//				saveMapToDisk(simpleMapsDir + "output/" + file.name(), map);
//			}
//		}
//	}
//	/**
//	 * Identical class, just need to transfer info  
//	 */
//	public com.mygdx.holowyth.world.map.simplemap.SimpleMap fieldToSimpleMap(SimpleMap field) {
//		com.mygdx.holowyth.world.map.simplemap.SimpleMap map = new com.mygdx.holowyth.world.map.simplemap.SimpleMap(field.width(), field.height());
//		map.name = field.name;
//		for(Polygon p : field.polys) {
//			map.polys.add(new com.mygdx.holowyth.world.map.obstacle.Polygon(p.floats, p.count));
//		}
//		return map;
//	}
//	
//	public void saveMapToDisk(String pathname, com.mygdx.holowyth.world.map.simplemap.SimpleMap map) {
//		try(FileOutputStream fout = new FileOutputStream(pathname);
//		ObjectOutputStream oos =	new ObjectOutputStream(fout);
//			) {
//			oos.writeObject(map);
//		} catch (IOException e) {
//			throw new HoloIOException("Error while saving map", e);
//		}
//		map.hasUnsavedChanges = false;
//		logger.debug("Wrote map {} to disk", map.name);
//	}
//	
//	
//	public static void main(String[] args) {
//		
//		new SimpleMapReserializer().reserializeAllMaps();
//		
////		Field oldMap = getMapFromDisk()
//	}

}
