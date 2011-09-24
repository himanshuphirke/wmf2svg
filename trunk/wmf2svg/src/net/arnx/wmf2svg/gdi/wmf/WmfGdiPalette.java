package net.arnx.wmf2svg.gdi.wmf;

import net.arnx.wmf2svg.gdi.GdiPalette;

class WmfGdiPalette extends WmfGdiObject implements GdiPalette {
	private int version;
	private int[] entries;
	
	public WmfGdiPalette(int id, int version, int[] entries) {
		super(id);
		this.version = version;
		this.entries = entries;
	}
	
	public int getVersion() {
		return version;
	}
	
	public int[] getEntries() {
		return entries;
	}
}