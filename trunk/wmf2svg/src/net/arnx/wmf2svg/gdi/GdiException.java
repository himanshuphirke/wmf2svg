package net.arnx.wmf2svg.gdi;

public class GdiException extends Exception {
	/**
	 * The class fingerprint that is set to indicate serialization compatibility
	 * with a previous version of the class.
	 */
	private static final long serialVersionUID = 6015311832170522581L;

	public GdiException() {
		super();
	}

	public GdiException(String message) {
		super(message);
	}
	
	public GdiException(String message, Throwable t) {
		super(message, t);
	}
	
	public GdiException(Throwable t) {
		super(t);
	}
}
