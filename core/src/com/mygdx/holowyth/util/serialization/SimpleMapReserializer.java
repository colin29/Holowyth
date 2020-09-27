package com.mygdx.holowyth.util.serialization;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.files.FileHandle;
import com.mygdx.holowyth.map.simplemap.SimpleMap;
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
//		FileHandle dir = new FileHandle(simpleMapsDir);
//		var contents = dir.list();
//		for(var file : contents) {
//			if(file.extension().equals("map")) {
//				logger.debug(file.name());
//				Field oldMap = HoloIO.getMapFromDisk(simpleMapsDir + file.name());
//				logger.debug("Got oldmap: {} " , oldMap);
//				SimpleMap map = fieldToSimpleMap(oldMap);
//				map.name = file.name();
//				saveMapToDisk(simpleMapsDir + "output/" + file.name(), map);
//			}
//		}
//	}
//	/**
//	 * Identical class, just need to transfer info  
//	 */
//	public SimpleMap fieldToSimpleMap(Field field) {
//		SimpleMap map = new SimpleMap(field.width(), field.height());
//		map.name = field.name;
//		for(Polygon p : field.polys) {
//			map.polys.add(new com.mygdx.holowyth.map.obstacledata.Polygon(p.floats, p.count));
//		}
//		return map;
//	}
//	
//	public void saveMapToDisk(String pathname, SimpleMap map) {
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
