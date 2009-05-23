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
	
	// viewport
	private int vx = 0;
	private int vy = 0;
	private int vw = 0;
	private int vh = 0;
	
	// viewport offset
	private int vox = 0;
	private int voy = 0;
	
	// viewport scale
	private double vsx = 1.0;
	private double vsy = 1.0;
	
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
	private int relAbsMode = 0;
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
	
	public void setWindowExtEx(int width, int height, Size old) {
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
	
	public void scaleWindowExtEx(int x, int xd, int y, int yd, Size old) {
		// TODO
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
		if (old != null) {
			old.x = vx;
			old.y = vy;
		}
		vx = x;
		vy = y;
	}
	
	public void setViewportExtEx(int width, int height, Size old) {
		if (old != null) {
			old.width = vw;
			old.height = vh;
		}
		vw = width;
		vh = height;
	}
	
	public void offsetViewportOrgEx(int x, int y, Point old) {
		if (old != null) {
			old.x = vox;
			old.y = voy;
		}
		vox = x;
		voy = y;
	}
	
	public void scaleViewportExtEx(int x, int xd, int y, int yd, Size old) {
		// TODO
		vsx = (vsx * x)/xd;
		vsy = (vsy * y)/yd;
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
		// TODO Handle Viewport
		return ((ww >= 0) ? 1 : -1) * (int)((mx * x - (wx + wox)) / wsy);
	}
	
	public int toAbsoluteY(int y) {
		// TODO Handle Viewport
		return ((wh >= 0) ? 1 : -1) * (int)((my * y - (wy + woy)) / wsy);
	}
	
	public int toRelativeX(int x) {
		// TODO Handle Viewport
		return ((ww >= 0) ? 1 : -1) * (int)(mx * x / wsx);
	}
	
	public int toRelativeY(int y) {
		// TODO Handle Viewport
		return ((wh >= 0) ? 1 : -1) * (int)(my * y / wsy);
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
	
	public int getRelAbs() {
		return relAbsMode;
	}
	
	public void setRelAbs(int mode) {
		relAbsMode = mode;
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
			bk.setAttribute("x", Integer.toString(toAbsoluteX(rect[0])));
			bk.setAttribute("y", Integer.toString(toAbsoluteY(rect[1])));
			bk.setAttribute("width", Integer.toString(toRelativeX(rect[2])));
			bk.setAttribute("height", Integer.toString(toRelativeY(rect[3])));
		}
		return bk;
	}
	
	public String getRopFilter(long rop) {
		String name = null;
		Document doc = gdi.getDocument();
		
		if (rop == Gdi.BLACKNESS) {
			name = "BLACKNESS_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);
				
				Element feColorMatrix = doc.createElement("feColorMatrix");
				feColorMatrix.setAttribute("in", "SourceGraphic");
				feColorMatrix.setAttribute("values", "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0");
				filter.appendChild(feColorMatrix);
				
				gdi.getDefsElement().appendChild(filter);
			}
		} else if (rop == Gdi.NOTSRCERASE) {
			name = "NOTSRCERASE_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);
				
				Element feComposite = doc.createElement("feComposite");
				feComposite.setAttribute("in", "SourceGraphic");
				feComposite.setAttribute("in2", "BackgroundImage");
				feComposite.setAttribute("operator", "arithmetic");
				feComposite.setAttribute("k1", "1");
				feComposite.setAttribute("result", "result0");
				filter.appendChild(feComposite);

				Element feColorMatrix = doc.createElement("feColorMatrix");
				feColorMatrix.setAttribute("in", "result0");
				feColorMatrix.setAttribute("values", "-1 0 0 0 1 0 -1 0 0 1 0 0 -1 0 1 0 0 0 1 0");
				filter.appendChild(feColorMatrix);
				
				gdi.getDefsElement().appendChild(filter);
			}			
		} else if (rop == Gdi.NOTSRCCOPY) {
			name = "NOTSRCCOPY_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);
				
				Element feColorMatrix = doc.createElement("feColorMatrix");
				feColorMatrix.setAttribute("in", "SourceGraphic");
				feColorMatrix.setAttribute("values", "-1 0 0 0 1 0 -1 0 0 1 0 0 -1 0 1 0 0 0 1 0");
				filter.appendChild(feColorMatrix);
				
				gdi.getDefsElement().appendChild(filter);
			}
		} else if (rop == Gdi.SRCERASE) {
			name = "SRCERASE_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);

				Element feColorMatrix = doc.createElement("feColorMatrix");
				feColorMatrix.setAttribute("in", "BackgroundImage");
				feColorMatrix.setAttribute("values", "-1 0 0 0 1 0 -1 0 0 1 0 0 -1 0 1 0 0 0 1 0");
				feColorMatrix.setAttribute("result", "result0");
				filter.appendChild(feColorMatrix);
				
				Element feComposite = doc.createElement("feComposite");
				feComposite.setAttribute("in", "SourceGraphic");
				feComposite.setAttribute("in2", "result0");
				feComposite.setAttribute("operator", "arithmetic");
				feComposite.setAttribute("k2", "1");
				feComposite.setAttribute("k3", "1");
				filter.appendChild(feComposite);
				
				gdi.getDefsElement().appendChild(filter);
			}
		} else if (rop == Gdi.PATINVERT) {
			// TODO
		} else if (rop == Gdi.SRCINVERT) {
			// TODO
		} else if (rop == Gdi.DSTINVERT) {
			name = "DSTINVERT_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);
				
				Element feColorMatrix = doc.createElement("feColorMatrix");
				feColorMatrix.setAttribute("in", "BackgroundImage");
				feColorMatrix.setAttribute("values", "-1 0 0 0 1 0 -1 0 0 1 0 0 -1 0 1 0 0 0 1 0");
				filter.appendChild(feColorMatrix);
				
				gdi.getDefsElement().appendChild(filter);
			}
		} else if (rop == Gdi.SRCAND) {
			name = "SRCAND_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);
				
				Element feComposite = doc.createElement("feComposite");
				feComposite.setAttribute("in", "SourceGraphic");
				feComposite.setAttribute("in2", "BackgroundImage");
				feComposite.setAttribute("operator", "arithmetic");
				feComposite.setAttribute("k1", "1");
				filter.appendChild(feComposite);
				
				gdi.getDefsElement().appendChild(filter);
			}
		} else if (rop == Gdi.MERGEPAINT) {
			name = "MERGEPAINT_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);

				Element feColorMatrix = doc.createElement("feColorMatrix");
				feColorMatrix.setAttribute("in", "SourceGraphic");
				feColorMatrix.setAttribute("values", "-1 0 0 0 1 0 -1 0 0 1 0 0 -1 0 1 0 0 0 1 0");
				feColorMatrix.setAttribute("result", "result0");
				filter.appendChild(feColorMatrix);
				
				Element feComposite = doc.createElement("feComposite");
				feComposite.setAttribute("in", "result0");
				feComposite.setAttribute("in2", "BackgroundImage");
				feComposite.setAttribute("operator", "arithmetic");
				feComposite.setAttribute("k1", "1");
				filter.appendChild(feComposite);
				
				gdi.getDefsElement().appendChild(filter);
			}
		} else if (rop == Gdi.MERGECOPY) {
			// TODO
		} else if (rop == Gdi.SRCPAINT) {
			name = "SRCPAINT_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);
				
				Element feComposite = doc.createElement("feComposite");
				feComposite.setAttribute("in", "SourceGraphic");
				feComposite.setAttribute("in2", "BackgroundImage");
				feComposite.setAttribute("operator", "arithmetic");
				feComposite.setAttribute("k2", "1");
				feComposite.setAttribute("k3", "1");
				filter.appendChild(feComposite);
				
				gdi.getDefsElement().appendChild(filter);
			}
		} else if (rop == Gdi.PATCOPY) {
			// TODO
		} else if (rop == Gdi.PATPAINT) {
			// TODO
		} else if (rop == Gdi.WHITENESS) {
			name = "WHITENESS_FILTER";
			Element filter = doc.getElementById(name);
			if (filter == null) {
				filter = gdi.getDocument().createElement("filter");
				filter.setAttribute("id", name);
				filter.setIdAttribute("id", true);
				
				Element feColorMatrix = doc.createElement("feColorMatrix");
				feColorMatrix.setAttribute("in", "SourceGraphic");
				feColorMatrix.setAttribute("values", "1 0 0 0 1 0 1 0 0 1 0 0 1 0 1 0 0 0 1 0");
				filter.appendChild(feColorMatrix);
				
				gdi.getDefsElement().appendChild(filter);
			}
		}
		
		if (name != null) {
			if (!doc.getDocumentElement().hasAttribute("enable-background")) {
				doc.getDocumentElement().setAttribute("enable-background", "new");
			}
			return "url(#" + name + ")";
		}
		return null;
	}

	public Object clone() {
		try {
			return (super.clone());
		} catch (CloneNotSupportedException e) {
			throw (new InternalError(e.getMessage()));
		}
	}
}