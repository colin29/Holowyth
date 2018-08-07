package com.mygdx.holowyth.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;

import com.mygdx.holowyth.map.Field;
import com.mygdx.holowyth.util.exception.ErrorCode;
import com.mygdx.holowyth.util.exception.HoloException;

/**
 * Utility methods related to disk access and paths
 * 
 * @author Colin Ta
 *
 */
public class HoloIO {

	public static Field getMapFromDisk(String pathname) {
		Field map;
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		try {
			fileIn = new FileInputStream(pathname);
			in = new ObjectInputStream(fileIn);
			map = (Field) in.readObject();
			return map;

		} catch (IOException i) {
			i.printStackTrace();
			throw new HoloException(ErrorCode.IO_EXCEPTION);
		} catch (ClassNotFoundException c) {
			System.out.println("Field class not found");
			c.printStackTrace();
			throw new HoloException(ErrorCode.IO_EXCEPTION);
		} finally {
			try {
				in.close();
				fileIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void saveMapToDisk(String pathname, Field map) {
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try {

			fout = new FileOutputStream(pathname);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(map);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					fout.close();
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
