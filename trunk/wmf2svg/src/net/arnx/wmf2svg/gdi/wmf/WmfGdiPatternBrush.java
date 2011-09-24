package net.arnx.wmf2svg.gdi.wmf;

import net.arnx.wmf2svg.gdi.GdiPatternBrush;

class WmfGdiPatternBrush extends WmfGdiObject implements GdiPatternBrush {
	private byte[] image;
	
	public WmfGdiPatternBrush(int id, byte[] image) {
		super(id);
		this.image = image;
	}
	
	public byte[] getPattern() {
		return image;
	}
}
