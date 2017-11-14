package com.mygdx.holowyth.util.misc;

import com.badlogic.gdx.files.FileHandle;

public class HoloMisc {
	public static void printDirectory(String dir){
		System.out.println("Directory\n---");
		FileHandle h = new FileHandle(dir);
		FileHandle[] files = h.list();
		for (FileHandle f : files){
			System.out.println(f.name());
		}
		System.out.println(""
				+ "---");
	}

}
