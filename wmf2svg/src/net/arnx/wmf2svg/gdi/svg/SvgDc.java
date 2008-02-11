/*
 * Copyright 2007-2008 Hidekatsu Izuno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package net.arnx.wmf2svg.gdi.svg;

import java.awt.Dimension;
import java.awt.Point;
import org.w3c.dom.*;

import net.arnx.wmf2svg.gdi.*;

/**
 * @author Hidekatsu Izuno
 */
public class SvgDc implements Cloneable {
	private SvgGdi gdi;

	private int dpi = 1440;

	// window
	private int wx = 0;
	private int wy = 0;
	private int ww = 0;
	private int wh = 0;
	
	// window offset
	private int wox = 0;
	private int woy = 0;
	
	// window scale
	private double wsx = 1.0;
	private double wsy = 1.0;
	
	// mapping scale
	private double mx = 1.0;
	private double my = 1.0;
	
	// current location
	private int cx = 0;
	private int cy = 0;

	private int mapMode = Gdi.MM_TEXT;
	private int bkColor = 0x00FFFFFF;
	private int bkMode = Gdi.OPAQUE;
	private int textColor = 0x00000000;
	private int textSpace = 0;
	private int textAlign = Gdi.TA_TOP | Gdi.TA_LEFT;
	private int textDx = 0;
	private int polyFillMode = Gdi.ALTERNATE;
	private int rop2Mode = Gdi.R2_COPYPEN;
	private int stretchBltMode = Gdi.STRETCH_ANDSCANS;
	
	private SvgStyleBrush brush = null;
	private SvgStyleFont font = null;
	private SvgStylePen pen = null;

	public SvgDc(SvgGdi gdi) {
		this.gdi = gdi;
	}

	public int getDpi() {
		return dpi;
	}
	
	public void setWindowOrgEx(int x, int y, Point old) {
		if (old != null) {
			old.x = wx;
			old.y = wy;
		}
		wx = x;
		wy = y;
	}
	
	public void setWindowExtEx(int width, int height, Dimension old) {
		if (old != null) {
			old.width = ww;
			old.height = wh;
		}
		ww = width;
		wh = height;
	}
	
	public void offsetWindowOrgEx(int x, int y, Point old) {
		if (old != null) {
			old.x = wox;
			old.y = woy;
		}
		wox += x;
		woy += y;
	}
	
	public void scaleWindowExtEx(int x, int xd, int y, int yd, Point old) {
		wsx = (wsx * x)/xd;
		wsy = (wsy * y)/yd;
	}
	
	public int getWindowX() {
		return wx;
	}
	
	public int getWindowY() {
		return wy;
	}
	
	public int getWindowWidth() {
		return ww;
	}
	
	public int getWindowHeight() {
		return wh;
	}
	
	public void setViewportOrgEx(int x, int y, Point old) {
	}
	
	public void setViewportExtEx(int width, int height, Dimension old) {
	}
	
	public void offsetViewportOrgEx(int x, int y, Point old) {
	}
	
	public void scaleViewportExtEx(int x, int xd, int y, int yd, Point old) {
	}
	
	public int getMapMode() {
		return mapMode;
	}
	
	public void setMapMode(int mode) {
		mapMode = mode;
		switch (mode) {
			case Gdi.MM_HIENGLISH :
				mx = 0.09;
				my = -0.09;
				break;
			case Gdi.MM_LOENGLISH :
				mx = 0.9;
				my = -0.9;
				break;
			case Gdi.MM_HIMETRIC :
				mx = 0.03543307;
				my = -0.03543307;
				break;
			case Gdi.MM_LOMETRIC :
				mx = 0.3543307;
				my = -0.3543307;
				break;
			case Gdi.MM_TWIPS :
				mx = 0.0625;
				my = -0.0625;
				break;
			default :
				mx = 1.0;
				my = 1.0;
		}
	}
	
	public int getCurrentX() {
		return cx;
	}
	
	public int getCurrentY() {
		return cy;
	}

	public void moveToEx(int x, int y, Point old) {
		if (old != null) {
			old.x = cx;
			old.y = cy;
		}
		cx = x;
		cy = y;
	}
	
	public int toAbsoluteX(int x) {
		if (ww > 0) {
			return (int)((mx * x / wsx) - (wx + wox)/wsx);
		} else {
			return (int)((wx + wox)/wsx - (mx * x / wsx));
		}
	}
	
	public int toAbsoluteY(int y) {
		if (wh > 0) {
			return (int)((my * y / wsy) - (wy + woy)/wsy);
		} else {
			return (int)((wy + woy)/wsy - (my * y / wsy));
		}
	}
	
	public int toRelativeX(int x) {
		if (ww > 0) {
			return (int)(mx * x / wsx);
		} else {
			return (int)(-mx * x / wsx);
		}
	}
	
	public int toRelativeY(int y) {
		if (wh > 0) {
			return (int)(my * y /wsy);
		} else {
			return (int)(-my * y /wsy);
		}
	}

	public void setDpi(int dpi) {
		this.dpi = (dpi > 0) ? dpi : 1440;
	}
	
	public int getBkColor() {
		return bkColor;
	}

	public void setBkColor(int color) {
		bkColor = color;
	}
	
	public int getBkMode() {
		return bkMode;
	}

	public void setBkMode(int mode) {
		bkMode = mode;
	}
	
	public int getTextColor() {
		return textColor;
	}
	
	public void setTextColor(int color) {
		textColor = color;
	}
	
	public int getPolyFillMode() {
		return polyFillMode;
	}
	
	public void setPolyFillMode(int mode) {
		polyFillMode = mode;
	}
	
	public int getROP2() {
		return rop2Mode;
	}
	
	public void setROP2(int mode) {
		rop2Mode = mode;
	}
	
	public int getStretchBltMode() {
		return stretchBltMode;
	}
	
	public void setStretchBltMode(int mode) {
		stretchBltMode = mode;
	}
	
	public int getTextSpace() {
		return textSpace;
	}
	
	public void setTextSpace(int space) {
		textSpace = space;
	}
	
	public int getTextAlign() {
		return textAlign;
	}
	
	public void setTextAlign(int align) {
		textAlign = align;
	}
	
	public int getTextCharacterExtra() {
		return textDx;
	}
	
	public void setTextCharacterExtra(int extra) {
		textDx = extra;
	}
	
	public SvgStyleBrush getBrush() {
		return brush;
	}
	
	public void setBrush(SvgStyleBrush brush) {
		this.brush = brush;
	}

	public SvgStyleFont getFont() {
		return font;
	}
	
	public void setFont(SvgStyleFont font) {
		this.font = font;
	}

	public SvgStylePen getPen() {
		return pen;
	}
	
	public void setPen(SvgStylePen pen) {
		this.pen = pen;
	}

	public Element createFillBk(int[] rect) {
		Element bk = null;
		if (getBkMode() == Gdi.OPAQUE) {
			bk = gdi.getDocument().createElement("rect");
			bk.setAttribute("fill", SvgStyleObject.toColor(getBkColor()));
			bk.setAttribute("x", "" + toAbsoluteX(rect[0]));
			bk.setAttribute("y", "" + toAbsoluteY(rect[1]));
			bk.setAttribute("width", "" + toRelativeX(rect[2]));
			bk.setAttribute("height", "" + toRelativeY(rect[3]));
		}
		return bk;
	}

	public Object clone() {
		try {
			return (super.clone());
		} catch (CloneNotSupportedException e) {
			throw (new InternalError(e.getMessage()));
		}
	}
}