package com.mygdx.holowyth.util;

import com.badlogic.gdx.files.FileHandle;

/**
 * Random functions that may be helpful, purely for debugging
 * 
 * @author Colin Ta
 *
 */
public class DebugUtil {
	/**
	 * Print out the contents of the current directory
	 * 
	 * @param dir
	 */
	public static void printDirectory(String dir) {
		System.out.println("Directory Contents:\n---");
		FileHandle h = new FileHandle(dir);
		FileHandle[] files = h.list();
		for (FileHandle f : files) {
			System.out.println(f.name());
		}
		System.out.println(""
				+ "---");
	}

}
