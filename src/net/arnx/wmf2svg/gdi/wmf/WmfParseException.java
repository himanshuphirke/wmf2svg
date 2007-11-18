package net.arnx.wmf2svg.gdi.wmf;

public class WmfParseException extends Exception {
	/**
	 * The class fingerprint that is set to indicate serialization compatibility
	 * with a previous version of the class.
	 */
	private static final long serialVersionUID = 42724981894237705L;

	public WmfParseException() {
		super();
	}

	public WmfParseException(String message) {
		super(message);
	}
	
	public WmfParseException(String message, Throwable t) {
		super(message, t);
	}
	
	public WmfParseException(Throwable t) {
		super(t);
	}
}
