package net.arnx.wmf2svg.gdi.svg;

import org.w3c.dom.Element;

import net.arnx.wmf2svg.gdi.GdiRegion;

abstract class SvgStyleRegion extends SvgStyleObject implements GdiRegion {
	public SvgStyleRegion(SvgGdi gdi) {
		super(gdi);
	}
	
	public abstract Element createRegion();
}
