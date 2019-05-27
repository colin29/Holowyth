package com.mygdx.holowyth.util.exception;

@Deprecated
public class HoloException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ErrorCode code;

	public HoloException(ErrorCode code) {
		this.code = code;
	}

}
