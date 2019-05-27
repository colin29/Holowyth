package com.mygdx.holowyth.util.exception;

/**
 * Eventually want to remove this code and replace it with util.exceptions (note the s)
 */
@Deprecated
public enum ErrorCode {
	INVALID_DIMENSIONS(100), INVALID_INPUT(101), IO_EXCEPTION(102);

	private ErrorCode(int num) {
	}

}
