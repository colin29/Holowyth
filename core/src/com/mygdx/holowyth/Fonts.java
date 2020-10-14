package com.mygdx.holowyth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.mygdx.holowyth.graphics.HoloGL;

public class Fonts {

	public final BitmapFont debugFont = normal("fonts/OpenSans.ttf", Color.WHITE, 16);

	// UI
	public final BitmapFont mediumUIFont = border("fonts/OpenSans.ttf", Color.WHITE, 16, Color.BLACK, 1.5f);
	public final BitmapFont largeUIFont = border("fonts/OpenSans.ttf", Color.WHITE, 25, Color.BLACK, 1.5f);

	// Damage effects
//	private static Color PALE_RED = HoloGL.rgb(255, 200, 200);
	private static Color DARK_RED = HoloGL.rgb(227, 20, 0);

	public final BitmapFont regularDamageEffectFont = border("fonts/OpenSans.ttf", Color.WHITE, 18, Color.BLACK, 1.5f);
	public final BitmapFont alliedDamageEffectFont = border("fonts/OpenSans.ttf", DARK_RED, 19, Color.BLACK, 1.5f);
	public final BitmapFont missEffectFont = border("fonts/OpenSans.ttf", Color.WHITE, 17, Color.GRAY, 0.5f);
	public final BitmapFont healEffectFont = border("fonts/OpenSans.ttf", Color.LIME, 19, Color.BLACK, 0.8f);
	private static Color PURPLE = HoloGL.rgb(205, 71, 193);
	public final BitmapFont dmgOverTimeEffectFont = border("fonts/OpenSans.ttf", PURPLE, 19, Color.BLACK, 0.8f);
	

	BitmapFont normal(String path, Color color, int size) {
		FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Holowyth.ASSETS_PATH + path));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = size;
		// HoloUI.addJapaneseCharacters(parameter);
		parameter.color = color;
		BitmapFont font = generator.generateFont(parameter);
		generator.dispose();
		return font;
	}

	BitmapFont border(String path, Color color, int size, Color borderColor, float borderWidth) {
		FreeTypeFontGenerator.setMaxTextureSize(FreeTypeFontGenerator.NO_MAXIMUM);
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Holowyth.ASSETS_PATH + path));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = size;
		parameter.borderWidth = borderWidth;
		parameter.borderColor = borderColor;
		parameter.color = color;
		BitmapFont font = generator.generateFont(parameter);
		generator.dispose();
		return font;
	}

}
