package net.arnx.wmf2svg.gdi.svg;

import net.arnx.wmf2svg.gdi.GdiBrush;

public class SvgPatternBrush extends SvgObject implements GdiBrush {
	private byte[] bmp;
	
	public SvgPatternBrush(SvgGdi gdi, byte[] bmp) {
		super(gdi);
		this.bmp = bmp;
	}
}
