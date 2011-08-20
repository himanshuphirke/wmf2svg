package net.arnx.wmf2svg.gdi.wmf;

import net.arnx.wmf2svg.gdi.GdiObject;

public class WmfGdiObject implements GdiObject {
	public int id;
	
	public WmfGdiObject(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
}
