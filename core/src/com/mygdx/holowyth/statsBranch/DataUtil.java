package com.mygdx.holowyth.statsBranch;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Add methods sparingly
 * Allows one point of control for how data should be displayed
 * @author Colin Ta
 *
 */
public class DataUtil {

	public static String getRoundedString(float value) {
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(value);
	}
	public static String getAsPercentage(float value) {
		return getRoundedString(value*100) + "%";
	}
}
