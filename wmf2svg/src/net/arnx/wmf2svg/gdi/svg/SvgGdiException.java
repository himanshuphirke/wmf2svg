package net.arnx.wmf2svg.gdi.svg;

import net.arnx.wmf2svg.gdi.GdiException;

public class SvgGdiException extends GdiException {
	/**
	 * The class fingerprint that is set to indicate serialization compatibility
	 * with a previous version of the class.
	 */
	private static final long serialVersionUID = -2096332410542422534L;

	public SvgGdiException() {
		super();
	}

	public SvgGdiException(String message) {
		super(message);
	}
	
	public SvgGdiException(String message, Throwable t) {
		super(message, t);
	}
	
	public SvgGdiException(Throwable t) {
		super(t);
	}
}
