package com.mygdx.holowyth.util.exceptions;

public class HoloException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public HoloException() {
		super();
	}

	public HoloException(String msg) {
		super(msg);
	}

	/**
	 * Gets the string as we wish to display it project-wide
	 */
	public String getFormattedMsgAndStackTrace() {
		return this.getMessage() + "\n" + getFromMessage();
	}

	public String getFromMessage() {
		return "From:\n" + this.getFormattedStackTrace();
	}

	public String getFormattedStackTrace() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.getStackTrace().length; i++) {
			sb.append("    ");
			sb.append(this.getStackTrace()[i]).toString();
			if (i != this.getStackTrace().length - 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

}
