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

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import net.arnx.wmf2svg.gdi.*;
import net.arnx.wmf2svg.util.Base64;

/**
 * @author Hidekatsu Izuno
 * @author Shunsuke Mori
 */
public class SvgGdi implements Gdi {
	private Map props = new HashMap();

	private SvgDc dc;

	private SvgDc saveDC;

	private Document doc = null;

	private Element parent = null;

	private Element defs = null;

	private Element style = null;

	private int brushNo = 0;

	private int fontNo = 0;

	private int penNo = 0;

	private int patternNo = 0;

	private Map nameMap = new HashMap();

	private Map objectMap = new LinkedHashMap();

	private StringBuffer buffer = new StringBuffer();

	private SvgStyleBrush defaultBrush;

	private SvgStylePen defaultPen;

	private SvgStyleFont defaultFont;

	public SvgGdi() throws SvgGdiException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new SvgGdiException(e);
		}

		DOMImplementation dom = builder.getDOMImplementation();
		doc = dom.createDocument("http://www.w3.org/2000/svg", "svg", null);

		try {
			Properties ps = new Properties();
			ps.load(getClass().getResourceAsStream("SvgGdi.properties"));

			for (Iterator i = ps.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				String key = (String) entry.getKey();
				Object value = entry.getValue();
				if (key.startsWith("first-byte-area.")) {
					List list = new ArrayList();
					StringTokenizer st = new StringTokenizer((String) value);
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						int[] area = new int[2];
						int index = token.indexOf("-");
						if (index != -1) {
							area[0] = Integer.parseInt(token
									.substring(0, index), 16);
							area[1] = Integer.parseInt(token.substring(
									index + 1, token.length()), 16);
						} else {
							area[0] = Integer.parseInt(token, 16);
							area[1] = area[0];
						}
						list.add(area);
					}
					value = list;
				}
				props.put(key, value);
			}
		} catch (Exception e) {
			throw new SvgGdiException(
					"properties format error: SvgGDI.properties");
		}

	}

	public SvgDc getDC() {
		return dc;
	}

	public Object getProperty(String key) {
		return props.get(key);
	}

	public Document getDocument() {
		return doc;
	}

	public void placeableHeader(int wsx, int wsy, int wex, int wey, int dpi) {
		if (parent == null) {
			init();
		}

		dc.setDpi(dpi);

		Element root = doc.getDocumentElement();
		root.setAttribute("width", ""
				+ (Math.abs(wex - wsx) / (double) dc.getDpi()) + "in");
		root.setAttribute("height", ""
				+ (Math.abs(wey - wsy) / (double) dc.getDpi()) + "in");
	}

	public void header() {
		if (parent == null) {
			init();
		}
	}

	private void init() {
		dc = new SvgDc(this);

		defaultBrush = (SvgStyleBrush) createBrushIndirect(GdiBrush.BS_SOLID,
				0x00FFFFFF, 0);
		defaultPen = (SvgStylePen) createPenIndirect(GdiPen.PS_SOLID, 1,
				0x00000000);
		defaultFont = null;

		dc.setBrush(defaultBrush);
		dc.setPen(defaultPen);
		dc.setFont(defaultFont);

		Element root = doc.getDocumentElement();
		root.setAttribute("xmlns", "http://www.w3.org/2000/svg");
		root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		parent = root;

		defs = doc.createElement("defs");

		style = doc.createElement("style");
		style.setAttribute("type", "text/css");
	}

	public void animatePalette() {
	}

	public void arc(int sxr, int syr, int exr, int eyr, int sxa, int sya,
			int exa, int eya) {
		int cx = sxr + (exr - sxr) / 2;
		int cy = syr + (eyr - syr) / 2;
		int rx = exr - sxr;
		int ry = eyr - syr;
		double sa = Math.atan2(sxa - cx, sya - cy);
		double sr = rx * Math.cos(sa) + ry * Math.sin(sa);
		double ea = Math.atan2(exa - cx, eya - cy);
		double er = rx * Math.cos(ea) + ry * Math.sin(ea);
		sxa = (int) (sr * Math.cos(sa)) + cx;
		sya = (int) (sr * Math.sin(sa)) + cy;
		exa = (int) (er * Math.cos(ea)) + cx;
		eya = (int) (er * Math.sin(ea)) + cy;

		Element elem = null;
		if (sa == ea) {
			if (rx == ry) {
				elem = doc.createElement("circle");
			} else {
				elem = doc.createElement("ellipse");
			}
			elem.setAttribute("cx", "" + dc.toAbsoluteX(cx));
			elem.setAttribute("cy", "" + dc.toAbsoluteY(cy));
			if (rx == ry) {
				elem.setAttribute("r", "" + dc.toRelativeX(rx));
			} else {
				elem.setAttribute("rx", "" + dc.toRelativeX(rx));
				elem.setAttribute("ry", "" + dc.toRelativeY(ry));
			}
		} else {
			elem = doc.createElement("path");
			double diff = (ea > sa) ? ea - sa : ea + 2 * Math.PI - sa;
			int large = (diff > Math.PI) ? 1 : 0;
			elem.setAttribute("d", "M " + dc.toAbsoluteX(sxa) + ","
					+ dc.toAbsoluteY(sya) + " A " + dc.toRelativeX(rx) + ","
					+ dc.toRelativeY(ry) + " 0 " + large + " 1 "
					+ dc.toAbsoluteX(exa) + "," + dc.toAbsoluteY(eya));
		}

		if (dc.getPen() != null) {
			elem.setAttribute("class", getClassString(dc.getPen()));
		}
		elem.setAttribute("fill", "none");
		parent.appendChild(elem);
	}

	public void bitBlt(int dx, int dy, int width, int height, int sx, int sy,
			long rop) {
	}

	public void chord(int sxr, int syr, int exr, int eyr, int sxa, int sya,
			int exa, int eya) {
		int cx = sxr + (exr - sxr) / 2;
		int cy = syr + (eyr - syr) / 2;
		int rx = exr - sxr;
		int ry = eyr - syr;
		double sa = Math.atan2(sxa - cx, sya - cy);
		double sr = rx * Math.cos(sa) + ry * Math.sin(sa);
		double ea = Math.atan2(exa - cx, eya - cy);
		double er = rx * Math.cos(ea) + ry * Math.sin(ea);
		sxa = (int) (sr * Math.cos(sa)) + cx;
		sya = (int) (sr * Math.sin(sa)) + cy;
		exa = (int) (er * Math.cos(ea)) + cx;
		eya = (int) (er * Math.sin(ea)) + cy;

		Element elem = null;
		if (sa == ea) {
			if (rx == ry) {
				elem = doc.createElement("circle");
			} else {
				elem = doc.createElement("ellipse");
			}
			elem.setAttribute("cx", "" + dc.toAbsoluteX(cx));
			elem.setAttribute("cy", "" + dc.toAbsoluteY(cy));
			if (rx == ry) {
				elem.setAttribute("r", "" + dc.toRelativeX(rx));
			} else {
				elem.setAttribute("rx", "" + dc.toRelativeX(rx));
				elem.setAttribute("ry", "" + dc.toRelativeX(ry));
			}
		} else {
			elem = doc.createElement("path");
			double diff = (ea > sa) ? ea - sa : ea + 2 * Math.PI - sa;
			int large = (diff > Math.PI) ? 1 : 0;
			elem.setAttribute("d", "M " + dc.toAbsoluteX(sxa) + ","
					+ dc.toAbsoluteY(sya) + " A " + dc.toRelativeX(rx) + ","
					+ dc.toRelativeX(ry) + " 0 " + large + " 1 "
					+ dc.toAbsoluteX(exa) + "," + dc.toAbsoluteY(eya) + " z");
		}

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc
					.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defs.appendChild(dc.getBrush().createFillPattern(id));
			}
		}

		parent.appendChild(elem);
	}

	public GdiBrush createBrushIndirect(int style, int color, int hatch) {
		SvgStyleBrush brush = new SvgStyleBrush(this, style, color, hatch);
		if (objectMap.containsKey(brush)) {
			return (SvgStyleBrush) objectMap.get(brush);
		} else {
			nameMap.put(brush, "brush" + (brushNo++));
			objectMap.put(brush, brush);

			return brush;
		}
	}

	public GdiObject createDIBPatternBrush(byte[] image, int usage) {
		return new GdiObject() {
		};
	}

	public GdiFont createFontIndirect(int height, int width, int escapement,
			int orientation, int weight, boolean italic, boolean underline,
			boolean strikeout, int charset, int outPrecision,
			int clipPrecision, int quality, int pitchAndFamily, byte[] faceName) {
		SvgStyleFont font = new SvgStyleFont(this, height, width, escapement,
				orientation, weight, italic, underline, strikeout, charset,
				outPrecision, clipPrecision, quality, pitchAndFamily, faceName);
		if (objectMap.containsKey(font)) {
			return (SvgStyleFont) objectMap.get(font);
		} else {
			nameMap.put(font, "font" + (fontNo++));
			objectMap.put(font, font);
			return font;
		}
	}

	public GdiObject createPalette() {
		return new GdiObject() {
		};
	}

	public GdiObject createPatternBrush() {
		return new GdiObject() {
		};
	}

	public GdiPen createPenIndirect(int style, int width, int color) {
		SvgStylePen pen = new SvgStylePen(this, style, width, color);
		if (objectMap.containsKey(pen)) {
			return (SvgStylePen) objectMap.get(pen);
		} else {
			nameMap.put(pen, "pen" + (penNo++));
			objectMap.put(pen, pen);
			return pen;
		}
	}
	
	public GdiObject createRectRgn(int sx, int sy, int ex, int ey) {
		return new GdiObject() {
		};		
	}

	public void deleteObject(GdiObject obj) {
		if (dc.getBrush() == obj) {
			dc.setBrush(defaultBrush);
		} else if (dc.getFont() == obj) {
			dc.setFont(defaultFont);
		} else if (dc.getPen() == obj) {
			dc.setPen(defaultPen);
		}
	}

	public void ellipse(int sx, int sy, int ex, int ey) {
		Element elem = doc.createElement("ellipse");

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc
					.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defs.appendChild(dc.getBrush().createFillPattern(id));
			}
		}

		elem.setAttribute("cx", "" + dc.toAbsoluteX((sx + ex) / 2));
		elem.setAttribute("cy", "" + dc.toAbsoluteY((sy + ey) / 2));
		elem.setAttribute("rx", "" + dc.toRelativeX((ex - sx) / 2));
		elem.setAttribute("ry", "" + dc.toRelativeY((ey - sy) / 2));
		parent.appendChild(elem);
	}

	public void escape(byte[] data) {
	}

	public void excludeClipRect(int sx, int sy, int ex, int ey) {
	}

	public void extFloodFill(int x, int y, int color, int type) {
	}

	public void extTextOut(int x, int y, int options, int[] rect, byte[] text,
			int[] dx) {
		Element elem = doc.createElement("text");

		int escapement = 0;
		int orientation = 0;
		if (dc.getFont() != null) {
			elem.setAttribute("class", getClassString(dc.getFont()));
			escapement = dc.getFont().getEscapement();
			orientation = dc.getFont().getOrientation();
		}
		elem.setAttribute("fill", SvgStyleObject.toColor(dc.getTextColor()));

		// style
		buffer.setLength(0);
		int align = dc.getTextAlign();

		if( align == 0 ){
			y-= dc.getFont().getHeight();	
		}
		if ((align & 0x0006) == TA_RIGHT) {
			buffer.append(" text-anchor: end;");
		} else if ((align & 0x0006) == TA_CENTER) {
			buffer.append(" text-anchor: middle;");
		}

		if ((align & 0x0018) == TA_TOP) {
			buffer.append(" dominant-baseline: text-top;");
		} else if ((align & 0x0018) == TA_BOTTOM) {
			buffer.append(" dominant-baseline: text-bottom;");
		} else if ((align & 0x0018) == TA_BASELINE) {
			buffer.append(" dominant-baseline: auto;");
		}

		if ((align & 0x0100) == TA_RTLREADING) {
			buffer.append(" unicode-bidi: bidi-override; direction: rtl;");
		}

		if (dc.getTextSpace() > 0) {
			buffer.append(" word-spacing: " + dc.getTextSpace() + ";");
		}

		elem.setAttribute("style", buffer.toString());

		elem.setAttribute("stroke", "none");

		if ((align & 0x0001) == TA_UPDATECP) {
			x = dc.getCurrentX();
			y = dc.getCurrentY();
		}

		if (dc.getBkMode() == OPAQUE) {
			if (rect != null) {
				parent.appendChild(dc.createFillBk(rect));
			} else {
				//TODO
			}
		}

		// x
		int ax = dc.toAbsoluteX(x);
		buffer.setLength(0);
		buffer.append(ax);
		dx = dc.getFont().validateDx(text, dx);
		if (dx != null) {
			for (int i = 0; i < dx.length - 1; i++) {
				x += dx[i];
				buffer.append(" ").append(dc.toAbsoluteX(x));
			}

			if ((align & 0x0001) == TA_UPDATECP) {
				dc.moveToEx(x + dx[dx.length - 1], y, null);
			}
		}
		elem.setAttribute("x", buffer.toString());
		
		// y
		int ay = dc.toAbsoluteY(y);
		elem.setAttribute("y", Integer.toString(ay));
		
		if (escapement != 0) {
			elem.setAttribute("transform", "rotate(" + (-escapement/10.0) + ", " + ax + ", " + ay + ")");
		}
		
		String str = dc.getFont().convertEncoding(text);
		if (orientation != 0) {
			buffer.setLength(0);
			for (int i = 0; i < str.length(); i++) {
				if (i != 0) buffer.append(' ');
				buffer.append(-orientation/10.0);
			}
			elem.setAttribute("rotate", buffer.toString());
		}

		String lang = dc.getFont().getLang();
		if (lang != null) {
			elem.setAttribute("xml:lang", lang);
		}
		
		elem.appendChild(doc.createTextNode(str));
		parent.appendChild(elem);
	}

	public void fillRgn() {
	}

	public void floodFill(int x, int y, int color) {
	}

	public void frameRgn() {
	}

	public void intersectClipRect(int sx, int sy, int ex, int ey) {
	}

	public void invertRgn() {
	}

	public void lineTo(int ex, int ey) {
		Element elem = doc.createElement("line");
		if (dc.getPen() != null) {
			elem.setAttribute("class", getClassString(dc.getPen()));
		}

		elem.setAttribute("fill", "none");

		elem.setAttribute("x1", "" + dc.toAbsoluteX(dc.getCurrentX()));
		elem.setAttribute("y1", "" + dc.toAbsoluteX(dc.getCurrentY()));
		elem.setAttribute("x2", "" + dc.toAbsoluteX(ex));
		elem.setAttribute("y2", "" + dc.toAbsoluteY(ey));
		parent.appendChild(elem);

		dc.moveToEx(ex, ey, null);
	}

	public void moveToEx(int x, int y, Point old) {
		dc.moveToEx(x, y, old);
	}

	public void offsetClipRgn(int x, int y) {
	}

	public void offsetViewportOrgEx(int x, int y, Point point) {
		dc.offsetViewportOrgEx(x, y, point);
	}

	public void offsetWindowOrgEx(int x, int y, Point point) {
		dc.offsetWindowOrgEx(x, y, point);
	}

	public void paintRgn() {
	}

	public void patBlt() {
	}

	public void pie(int sxr, int syr, int exr, int eyr, int sxa, int sya,
			int exa, int eya) {
		int cx = sxr + (exr - sxr) / 2;
		int cy = syr + (eyr - syr) / 2;
		int rx = exr - sxr;
		int ry = eyr - syr;
		double sa = Math.atan2(sxa - cx, sya - cy);
		double sr = rx * Math.cos(sa) + ry * Math.sin(sa);
		double ea = Math.atan2(exa - cx, eya - cy);
		double er = rx * Math.cos(ea) + ry * Math.sin(ea);
		sxa = (int) (sr * Math.cos(sa)) + cx;
		sya = (int) (sr * Math.sin(sa)) + cy;
		exa = (int) (er * Math.cos(ea)) + cx;
		eya = (int) (er * Math.sin(ea)) + cy;

		Element elem = null;
		if (sa == ea) {
			if (rx == ry) {
				elem = doc.createElement("circle");
			} else {
				elem = doc.createElement("ellipse");
			}
			elem.setAttribute("cx", "" + dc.toAbsoluteX(cx));
			elem.setAttribute("cy", "" + dc.toAbsoluteY(cy));
			if (rx == ry) {
				elem.setAttribute("r", "" + dc.toRelativeX(rx));
			} else {
				elem.setAttribute("rx", "" + dc.toRelativeX(rx));
				elem.setAttribute("ry", "" + dc.toRelativeY(ry));
			}
		} else {
			elem = doc.createElement("path");
			double diff = (ea > sa) ? ea - sa : ea + 2 * Math.PI - sa;
			int large = (diff > Math.PI) ? 1 : 0;
			elem.setAttribute("d", "M " + dc.toAbsoluteX(cx) + ","
					+ dc.toAbsoluteY(cy) + " L " + dc.toAbsoluteX(sxa) + ","
					+ dc.toAbsoluteY(sya) + " A " + dc.toRelativeX(rx) + ","
					+ dc.toRelativeY(ry) + " 0 " + large + " 1 "
					+ dc.toAbsoluteX(exa) + "," + dc.toAbsoluteY(eya) + " z");
		}

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc
					.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defs.appendChild(dc.getBrush().createFillPattern(id));
			}
		}
		parent.appendChild(elem);
	}

	public void polygon(Point[] points) {
		Element elem = doc.createElement("polygon");

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc
					.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defs.appendChild(dc.getBrush().createFillPattern(id));
			}
			if (dc.getPolyFillMode() == WINDING) {
				elem.setAttribute("fill-rule", "nonzero");
			}
		}

		buffer.setLength(0);
		for (int i = 0; i < points.length; i++) {
			if (i != 0) {
				buffer.append(" ");
			}
			buffer.append(dc.toAbsoluteX(points[i].x)).append(",");
			buffer.append(dc.toAbsoluteY(points[i].y));
		}
		elem.setAttribute("points", buffer.toString());
		parent.appendChild(elem);
	}

	public void polyline(Point[] points) {
		Element elem = doc.createElement("polyline");
		if (dc.getPen() != null) {
			elem.setAttribute("class", getClassString(dc.getPen()));
		}
		elem.setAttribute("fill", "none");

		buffer.setLength(0);
		for (int i = 0; i < points.length; i++) {
			if (i != 0)
				buffer.append(" ");
			buffer.append(dc.toAbsoluteX(points[i].x)).append(",");
			buffer.append(dc.toAbsoluteY(points[i].y));
		}
		elem.setAttribute("points", buffer.toString());
		parent.appendChild(elem);
	}

	public void polyPolygon(Point[][] points) {
		Element elem = doc.createElement("path");

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc
					.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defs.appendChild(dc.getBrush().createFillPattern(id));
			}
			if (dc.getPolyFillMode() == WINDING) {
				elem.setAttribute("fill-rule", "nonzero");
			}
		}

		buffer.setLength(0);
		for (int i = 0; i < points.length; i++) {
			if (i != 0) {
				buffer.append(" ");
			}
			for (int j = 0; j < points[i].length; j++) {
				if (j == 0) {
					buffer.append("M ");
				} else if (j == 1) {
					buffer.append(" L ");
				}
				buffer.append(dc.toAbsoluteX(points[i][j].x)).append(",");
				buffer.append(dc.toAbsoluteY(points[i][j].y)).append(" ");
				if (j == points[i].length - 1) {
					buffer.append("z");
				}
			}
		}
		elem.setAttribute("d", buffer.toString());
		parent.appendChild(elem);
	}

	public void realizePalette() {
	}

	public void restoreDC() {
		if(saveDC != null) dc = saveDC;
	}

	public void rectangle(int sx, int sy, int ex, int ey) {
		Element elem = doc.createElement("rect");

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc
					.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defs.appendChild(dc.getBrush().createFillPattern(id));
			}
		}

		elem.setAttribute("x", "" + dc.toAbsoluteX(sx));
		elem.setAttribute("y", "" + dc.toAbsoluteY(sy));
		elem.setAttribute("width", "" + dc.toRelativeX(ex - sx));
		elem.setAttribute("height", "" + dc.toRelativeY(ey - sy));
		parent.appendChild(elem);
	}

	public void resizePalette(GdiObject obj) {
	}

	public void roundRect(int sx, int sy, int ex, int ey, int rw, int rh) {
		Element elem = doc.createElement("rect");

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc
					.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defs.appendChild(dc.getBrush().createFillPattern(id));
			}
		}

		elem.setAttribute("x", "" + dc.toAbsoluteX(sx));
		elem.setAttribute("y", "" + dc.toAbsoluteY(sy));
		elem.setAttribute("width", "" + dc.toRelativeX(ex - sx));
		elem.setAttribute("height", "" + dc.toRelativeY(ey - sy));
		elem.setAttribute("rx", "" + dc.toRelativeX(rw));
		elem.setAttribute("ry", "" + dc.toRelativeY(rh));
		parent.appendChild(elem);
	}

	public void seveDC() {
		saveDC = (SvgDc) dc.clone();
	}

	public void scaleViewportExtEx(int x, int xd, int y, int yd, Point old) {
		dc.scaleViewportExtEx(x, xd, y, yd, old);
	}

	public void scaleWindowExtEx(int x, int xd, int y, int yd, Point old) {
		dc.scaleWindowExtEx(x, xd, y, yd, old);
	}

	public void selectClipRgn(GdiObject obj) {
	}

	public void selectObject(GdiObject obj) {
		if (obj instanceof SvgStyleBrush) {
			dc.setBrush((SvgStyleBrush) obj);
		} else if (obj instanceof SvgStyleFont) {
			dc.setFont((SvgStyleFont) obj);
		} else if (obj instanceof SvgStylePen) {
			dc.setPen((SvgStylePen) obj);
		}
	}

	public void selectPalette(GdiObject obj, boolean mode) {
	}

	public void setBkColor(int color) {
		dc.setBkColor(color);
	}

	public void setBkMode(int mode) {
		dc.setBkMode(mode);
	}

	public void setDIBitsToDevice(int dx, int dy, long dw, long dh, int sx,
			int sy, int startscan, int scanlines, byte[] image, int colorUse) {
	}

	public void setMapMode(int mode) {
		dc.setMapMode(mode);
	}

	public void setMapperFlags(long flag) {
	}

	public void setPaletteEntries() {
	}

	public void setPixel(int x, int y, int color) {
		Element elem = doc.createElement("rect");
		elem.setAttribute("stroke", "none");
		elem.setAttribute("fill", SvgStylePen.toColor(color));
		elem.setAttribute("x", "" + dc.toAbsoluteX(x));
		elem.setAttribute("y", "" + dc.toAbsoluteY(y));
		elem.setAttribute("width", "" + dc.toRelativeX(1));
		elem.setAttribute("height", "" + dc.toRelativeY(1));
		parent.appendChild(elem);
	}

	public void setPolyFillMode(int mode) {
		dc.setPolyFillMode(mode);
	}

	public void setROP2(int mode) {
		dc.setROP2(mode);
	}

	public void setStretchBltMode(int mode) {
		dc.setStretchBltMode(mode);
	}

	public void setTextAlign(int align) {
		dc.setTextAlign(align);
	}

	public void setTextCharacterExtra(int extra) {
		dc.setTextCharacterExtra(extra);
	}

	public void setTextColor(int color) {
		dc.setTextColor(color);
	}

	public void setTextJustification(int breakExtra, int breakCount) {
		if (breakCount > 0) {
			dc.setTextSpace(Math.abs(dc.toRelativeX(breakExtra)) / breakCount);
		}
	}
	
	public void setViewportExtEx(int x, int y, Dimension old) {
		dc.setViewportExtEx(x, y, old);
	}

	public void setViewportOrgEx(int x, int y, Point old) {
		dc.setViewportOrgEx(x, y, old);
	}

	public void setWindowExtEx(int width, int height, Dimension old) {
		dc.setWindowExtEx(width, height, old);
	}

	public void setWindowOrgEx(int x, int y, Point old) {
		dc.setWindowOrgEx(x, y, old);
	}

	public void stretchBlt() {
	}

	public void stretchDIBits(int dx, int dy, int dw, int dh, int sx, int sy,
			int sw, int sh, byte[] image, int usage, long rop) {
		
		try{
			// convert to 24bit color
			BufferedImage bufferedImage = bmpToImage(dibToBmp(image));
			BufferedImage dst = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			ColorConvertOp colorConvert = new ColorConvertOp(dst.getColorModel().getColorSpace(), null);
			colorConvert.filter(bufferedImage, dst);
			bufferedImage = dst;
			
			if (dh < 0) {
				DataBuffer srcData = bufferedImage.getRaster().getDataBuffer();
				BufferedImage dstImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
				DataBuffer dstData = dstImage.getRaster().getDataBuffer();
				int lineSize = bufferedImage.getWidth() * bufferedImage.getColorModel().getPixelSize() / 8;
				for (int h = 0, k = bufferedImage.getHeight() - 1; h < bufferedImage.getHeight(); h++, k--) {
					for (int j = 0; j < lineSize; j++) {
						dstData.setElem(h * lineSize + j, srcData.getElem(k * lineSize + j));
					}
				}
				bufferedImage = dstImage;
			}
			
			String data = imageToURI(bufferedImage);
			
			if (data == null || data.equals("")) {
				return;
			}

			Element elem = doc.createElement("image");
			if( dh < 0 ){
				elem.setAttribute("x", "" + dc.toAbsoluteX(dx));
				elem.setAttribute("y", "" + dc.toAbsoluteY(dy+dh));
				elem.setAttribute("width", "" + dc.toRelativeX(dw));
				elem.setAttribute("height", "" + dc.toRelativeY(-dh));
			}else{
				elem.setAttribute("x", "" + dc.toAbsoluteX(dx));
				elem.setAttribute("y", "" + dc.toAbsoluteY(dy));
				elem.setAttribute("width", "" + dc.toRelativeX(dw));
				elem.setAttribute("height", "" + dc.toRelativeY(dh));
			}
	
			if (sx != 0 || sy != 0 || sw != dw || sh != dh) {
				elem.setAttribute("viewBox", "" + sx + " " + sy + " " + sw + " "+ sh);
				elem.setAttribute("preserveAspectRatio", "none");
			}
	
			elem.setAttribute("xlink:href", data);
			parent.appendChild(elem);
		}catch (Exception e) {
			throw new UnsupportedOperationException();
		}
	}

	public void textOut(int x, int y, byte[] text) {
		Element elem = doc.createElement("text");
		
		int escapement = 0;
		int orientation = 0;
		if (dc.getFont() != null) {
			elem.setAttribute("class", getClassString(dc.getFont()));
			escapement = dc.getFont().getEscapement();
			orientation = dc.getFont().getOrientation();
		}
		elem.setAttribute("fill", SvgStyleObject.toColor(dc.getTextColor()));

		// style
		buffer.setLength(0);
		int align = dc.getTextAlign();

		if ((align & 0x0006) == TA_RIGHT) {
			buffer.append(" text-anchor: end;");
		} else if ((align & 0x0006) == TA_CENTER) {
			buffer.append(" text-anchor: middle;");
		}

		if ((align & 0x0018) == TA_TOP) {
			buffer.append(" dominant-baseline: text-top;");
		} else if ((align & 0x0018) == TA_BOTTOM) {
			buffer.append(" dominant-baseline: text-bottom;");
		} else if ((align & 0x0018) == TA_BASELINE) {
			buffer.append(" dominant-baseline: auto;");
		}

		if ((align & 0x0100) == TA_RTLREADING) {
			buffer.append(" unicode-bidi: bidi-override; direction: rtl;");
		}

		if (dc.getTextSpace() > 0) {
			buffer.append(" word-spacing: " + dc.getTextSpace() + ";");
		}

		elem.setAttribute("style", buffer.toString());

		elem.setAttribute("stroke", "none");
		
		int ax = dc.toAbsoluteX(x);
		int ay = dc.toAbsoluteY(y);
		elem.setAttribute("x", Integer.toString(ax));
		elem.setAttribute("y", Integer.toString(ay));
		
		if (escapement != 0) {
			elem.setAttribute("transform", "rotate(" + (-escapement/10.0) + ", " + ax + ", " + ay + ")");
		}

		String str = dc.getFont().convertEncoding(text);

		if (dc.getTextCharacterExtra() != 0) {
			buffer.setLength(0);

			for (int i = 0; i < str.length() - 1; i++) {
				if (i != 0) {
					buffer.append(" ");
				}
				buffer.append(dc.toRelativeX(dc.getTextCharacterExtra()));
			}

			elem.setAttribute("dx", buffer.toString());
		}
		
		if (orientation != 0) {
			buffer.setLength(0);
			for (int i = 0; i < str.length(); i++) {
				if (i != 0) buffer.append(' ');
				buffer.append(-orientation/10.0);
			}
			elem.setAttribute("rotate", buffer.toString());
		}

		String lang = dc.getFont().getLang();
		if (lang != null) {
			elem.setAttribute("xml:lang", lang);
		}
		elem.appendChild(doc.createTextNode(str));
		parent.appendChild(elem);
	}

	public void footer() {
		Element root = doc.getDocumentElement();
		if (!root.hasAttribute("width")) {
			root.setAttribute("width", "" + Math.abs(dc.getWindowWidth()));
		}
		if (!root.hasAttribute("height")) {
			root.setAttribute("height", "" + Math.abs(dc.getWindowHeight()));
		}
		root.setAttribute("viewBox", "0 0 " + Math.abs(dc.getWindowWidth())
				+ " " + Math.abs(dc.getWindowHeight()));
		root.setAttribute("stroke-linecap", "round");
		root.setAttribute("fill-rule", "evenodd");

		if (!objectMap.isEmpty()) {
			buffer.setLength(0);
			buffer.append("\n");
			for (Iterator i = objectMap.keySet().iterator(); i.hasNext();) {
				SvgStyleObject so = (SvgStyleObject) i.next();
				buffer.append(".").append(nameMap.get(so)).append(" {").append(
						so).append("}\n");
			}
			style.appendChild(doc.createTextNode(buffer.toString()));
			root.insertBefore(style, root.getFirstChild());
		}

		if (defs.hasChildNodes()) {
			root.insertBefore(defs, root.getFirstChild());
		}
	}

	private String getClassString(SvgStyleObject obj1, SvgStyleObject obj2) {
		String name1 = getClassString(obj1);
		String name2 = getClassString(obj2);
		if (name1 != null && name2 != null) {
			return name1 + " " + name2;
		}
		if (name1 != null) {
			return name1;
		}
		if (name2 != null) {
			return name2;
		}
		return null;
	}

	private String getClassString(SvgStyleObject style) {
		if (style == null) {
			return "";
		}

		return (String) nameMap.get(style);
	}
	
	private String imageToURI(BufferedImage image) throws IOException {
		StringBuffer buffer = new StringBuffer("data:image/png;base64,");
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			ImageIO.write(image, "png", out);
			out.close();
			byte[] data = out.toByteArray();
			buffer.append(Base64.encode(data));
			return buffer.toString();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private BufferedImage bmpToImage(byte[] bmp ) throws IOException{
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(bmp));
		return image;
		
	}

	private byte[] dibToBmp(byte[] dib) {
		byte[] data = new byte[14 + dib.length];

		/* BitmapFileHeader */
		data[0] = 0x42; // 'B'
		data[1] = 0x4d; // 'M'

		long bfSize = data.length;
		data[2] = (byte) (bfSize & 0xff);
		data[3] = (byte) ((bfSize >> 8) & 0xff);
		data[4] = (byte) ((bfSize >> 16) & 0xff);
		data[5] = (byte) ((bfSize >> 24) & 0xff);

		// reserved 1
		data[6] = 0x00;
		data[7] = 0x00;

		// reserved 2
		data[8] = 0x00;
		data[9] = 0x00;

		// offset
		long bfOffBits = 14;

		/* BitmapInfoHeader */
		long biSize = (dib[0] & 0xff) + ((dib[1] & 0xff) << 8)
				+ ((dib[2] & 0xff) << 16) + ((dib[3] & 0xff) << 24);
		bfOffBits += biSize;

		int biBitCount = (dib[14] & 0xff) + ((dib[15] & 0xff) << 8);

		long clrUsed = (dib[32] & 0xff) + ((dib[33] & 0xff) << 8)
				+ ((dib[34] & 0xff) << 16) + ((dib[35] & 0xff) << 24);

		switch (biBitCount) {
		case 1:
			bfOffBits += (0x1L + 1) * 4;
			break;
		case 4:
			bfOffBits += (0xFL + 1) * 4;
			break;
		case 8:
			bfOffBits += (0xFFL + 1) * 4;
			break;
		case 16:
			bfOffBits += (clrUsed == 0L) ? 0 : (0xFFFFL + 1) * 4;
			break;
		case 24:
			bfOffBits += (clrUsed == 0L) ? 0 : (0xFFFFFFL + 1) * 4;
			break;
		case 32:
			bfOffBits += (clrUsed == 0L) ? 0 : (0xFFFFFFFFL + 1) * 4;
			break;
		}
		
		data[10] = (byte) (bfOffBits & 0xff);
		data[11] = (byte) ((bfOffBits >> 8) & 0xff);
		data[12] = (byte) ((bfOffBits >> 16) & 0xff);
		data[13] = (byte) ((bfOffBits >> 24) & 0xff);
		System.arraycopy(dib, 0, data, 14, dib.length);

		return data;
	}
}
