package com.mygdx.holowyth.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.math.Vector2;

/**
 * Add methods sparingly Allows one point of control for how data should be displayed project-wide
 * 
 * @author Colin Ta
 *
 */
public class DataUtil {

	public static String getRoundedString(Vector2 point) {
		return String.format("%s %s", DataUtil.getRoundedString(point.x), DataUtil.getRoundedString(point.y));
	}

	/**
	 * Rounds to 2 places
	 */
	public static String getRoundedString(float value) {
		if (isNotFinite(value))
			return getNonFiniteString(value);
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(value);
	}

	public static String round(float value, int decimalPlaces) {
		if (isNotFinite(value))
			return getNonFiniteString(value);
		String marker = "";
		for (int i = 0; i < decimalPlaces; i++) {
			marker += "#";
		}
		DecimalFormat df = new DecimalFormat("#." + marker);
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(value);
	}

	public static String roundFully(float value) {
		if (isNotFinite(value))
			return getNonFiniteString(value);
		String str = String.valueOf(Math.round(value));
		return str;
	}

	public static String percentage(float value) {
		if (isNotFinite(value))
			return getNonFiniteString(value);
		return getRoundedString(value * 100) + "%";
	}

	private static boolean isNotFinite(float f) {
		return Float.isInfinite(f) || Float.isNaN(f);
	}

	private static String getNonFiniteString(float value) {
		if (Float.isInfinite(value)) {
			return "Infinity";
		}
		if (Float.isNaN(value)) {
			return "NaN";
		}
		LoggerFactory.getLogger(DataUtil.class).warn("getNonFiniteString is returning null, improper use?");
		return null;
	}

	public static String asSeconds(int frames) {
		return round(((float) frames) / Holo.GAME_FPS, 1);
	}

	public static String asSeconds(float frames) {
		return round(frames / Holo.GAME_FPS, 1);
	}
}
