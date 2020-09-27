package com.mygdx.holowyth.test.sandbox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class FileHandleTest {
	
	
	public static void main(String[] args) {
		FileHandle dir = Gdx.files.internal("");
		System.out.println(dir.readString());
	}

}
