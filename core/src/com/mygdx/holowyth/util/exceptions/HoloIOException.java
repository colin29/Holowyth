package com.mygdx.holowyth.util.exceptions;

public class HoloIOException extends HoloException {

	private static final long serialVersionUID = 1L;

	public HoloIOException(String msg) {
		super(msg);
	}
	public HoloIOException(Throwable cause) {
		super(cause);
	}

	public HoloIOException(String message, Throwable cause) {
		super(message, cause);
	}

}
