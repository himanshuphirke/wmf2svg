package net.arnx.wmf2svg.gdi.wmf;

import net.arnx.wmf2svg.gdi.GdiBrush;

public class WmfGdiBrush extends WmfGdiObject implements GdiBrush {
	private int style;
	private int color;
	private int hatch;
	
	public WmfGdiBrush(int id, int style, int color, int hatch) {
		super(id);
		this.style = style;
		this.color = color;
		this.hatch = hatch;
	}
	
	public int getStyle() {
		return style;
	}
	
	public int getColor() {
		return color;
	}
	
	public int getHatch() {
		return hatch;
	}
}
