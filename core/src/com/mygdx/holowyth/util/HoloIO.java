package com.mygdx.holowyth.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;

import com.mygdx.holowyth.map.simplemap.SimpleMap;
import com.mygdx.holowyth.util.exceptions.HoloException;
/**
 * Utility methods related to disk access and paths
 * 
 * @author Colin Ta
 *
 */
public class HoloIO {

	public static SimpleMap getMapFromDisk(String pathname) {
		SimpleMap map;
		
		try (FileInputStream fileIn = new FileInputStream(pathname);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				){
			map = (SimpleMap) in.readObject();
			return map;
		} catch (IOException e) {
			throw new HoloException("Couldn't read map from disk", e);
		} catch (ClassNotFoundException e) {
			throw new HoloException(e);
		} 

	}

	public static void saveMapToDisk(String pathname, SimpleMap map) {
		try (FileOutputStream fout = new FileOutputStream(pathname);
			ObjectOutputStream oos = new ObjectOutputStream(fout);){
			oos.writeObject(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.hasUnsavedChanges = false;
		System.out.println("Writing Map to disk finished");
	}

	// Package utility functions
	static String getCanonicalPathElseNull(String string) {
		try {
			return getCanonicalPath(string);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Utility functions

	public static String getCanonicalPath(String string) throws IOException {
		return Paths.get(string).toRealPath().toString();
	}

	public static void printCanonicalPath(String s) {
		try {
			System.out.println(getCanonicalPath(s));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
