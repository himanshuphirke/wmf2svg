package net.arnx.wmf2svg.gdi.wmf;

import net.arnx.wmf2svg.gdi.GdiRegion;

class WmfGdiRectRegion extends WmfGdiObject implements GdiRegion {
	private int left;
	private int top;
	private int right;
	private int bottom;
	
	public WmfGdiRectRegion(int id, int left, int top, int right, int bottom) {
		super(id);
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public int getLeft() {
		return left;
	}
	
	public int getTop() {
		return top;
	}
	
	public int getRight() {
		return right;
	}
	
	public int getBottom() {
		return bottom;
	}
}
