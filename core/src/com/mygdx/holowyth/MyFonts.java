package com.mygdx.holowyth;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class MyFonts {
	BitmapFont debugFont;
	BitmapFont borderedSmallFont;
	BitmapFont borderedMediumFont;
	BitmapFont damageEffectFont;
	BitmapFont missEffectFont;

	BitmapFont font;
	BitmapFont font_goth12;
	BitmapFont font_goth24;

	MyFonts() {
	}

	public void init() {

		debugFont = generateFont("fonts/OpenSans.ttf", Color.WHITE, 16);
		borderedSmallFont = generateFontWithBorder("fonts/OpenSans.ttf", Color.WHITE, 10, Color.BLACK, 0.8f);
		borderedMediumFont = generateFontWithBorder("fonts/OpenSans.ttf", Color.WHITE, 16, Color.BLACK, 1.5f);

		damageEffectFont = generateFontWithBorder("fonts/OpenSans.ttf", Color.WHITE, 16, Color.BLACK, 1.5f);
		missEffectFont = generateFontWithBorder("fonts/OpenSans.ttf", Color.WHITE, 15, Color.GRAY, 0.5f);

		font = new BitmapFont(); // Default Arial font.
		font_goth12 = generateFont("fonts/MS_Gothic.ttf", Color.WHITE, 12);
		font_goth24 = generateFont("fonts/MS_Gothic.ttf", Color.WHITE, 24);
	}

	BitmapFont generateFontWithBorder(String path, Color color, int size, Color borderColor,
			float borderWidth) {
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

	BitmapFont generateFont(String path, Color color, int size) {

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

	public BitmapFont debugFont() {
		return debugFont;
	}

	public BitmapFont borderedMediumFont() {
		return borderedMediumFont;
	}

	public BitmapFont borderedSmallFont() {
		return borderedMediumFont;
	}

	public BitmapFont damageEffectFont() {
		return damageEffectFont;
	}

	public BitmapFont missEffectFont() {
		return missEffectFont;
	}

	public BitmapFont font() {
		return font;
	}

	public BitmapFont font_goth12() {
		return font_goth12;
	}

	public BitmapFont moderateFont() {
		return font_goth24;
	}

}
