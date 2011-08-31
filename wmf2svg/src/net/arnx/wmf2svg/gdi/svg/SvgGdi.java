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

import java.util.*;
import java.util.logging.Logger;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import net.arnx.wmf2svg.gdi.*;
import net.arnx.wmf2svg.util.Base64;
import net.arnx.wmf2svg.util.ImageUtil;

/**
 * @author Hidekatsu Izuno
 * @author Shunsuke Mori
 */
public class SvgGdi implements Gdi {
	private static Logger log = Logger.getLogger(SvgGdi.class.getName());
	
	private boolean compatible;
	
	private Map props = new HashMap();

	private SvgDc dc;

	private SvgDc saveDC;

	private Document doc = null;
	
	private Element parentNode = null;
	
	private Element styleNode = null;
	
	private Element defsNode = null;

	private int brushNo = 0;

	private int fontNo = 0;

	private int penNo = 0;

	private int patternNo = 0;
	
	private int rgnNo = 0;
	
	private int clipPathNo = 0;
	
	private int maskNo = 0;
	
	private Map nameMap = new HashMap();

	private StringBuffer buffer = new StringBuffer();

	private SvgBrush defaultBrush;

	private SvgPen defaultPen;

	private SvgFont defaultFont;
	
	public SvgGdi() throws SvgGdiException {
		this(false);
	}
	
	public SvgGdi(boolean compatible) throws SvgGdiException {
		this.compatible = compatible;
		
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
	
	public Element getDefsElement() {
		return defsNode;
	}
	
	public Element getStyleElement() {
		return styleNode;
	}

	public void placeableHeader(int wsx, int wsy, int wex, int wey, int dpi) {
		if (parentNode == null) {
			init();
		}
		
		dc.setWindowExtEx(Math.abs(wex - wsx), Math.abs(wey - wsy), null);
		dc.setDpi(dpi);

		Element root = doc.getDocumentElement();
		root.setAttribute("width", ""
				+ (Math.abs(wex - wsx) / (double) dc.getDpi()) + "in");
		root.setAttribute("height", ""
				+ (Math.abs(wey - wsy) / (double) dc.getDpi()) + "in");
	}

	public void header() {
		if (parentNode == null) {
			init();
		}
	}

	private void init() {
		dc = new SvgDc(this);

		Element root = doc.getDocumentElement();
		root.setAttribute("xmlns", "http://www.w3.org/2000/svg");
		root.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		
		defsNode = doc.createElement("defs");
		root.appendChild(defsNode);

		styleNode = doc.createElement("style");
		styleNode.setAttribute("type", "text/css");
		root.appendChild(styleNode);
		
		parentNode = doc.createElement("g");
		doc.getDocumentElement().appendChild(parentNode);

		defaultBrush = (SvgBrush) createBrushIndirect(GdiBrush.BS_SOLID,
				0x00FFFFFF, 0);
		defaultPen = (SvgPen) createPenIndirect(GdiPen.PS_SOLID, 1,
				0x00000000);
		defaultFont = null;

		dc.setBrush(defaultBrush);
		dc.setPen(defaultPen);
		dc.setFont(defaultFont);
	}

	public void animatePalette(GdiPalette palette, int startIndex, int entryCount, byte[] entries) {
		// TODO
		log.fine("not implemented: animatePalette");
	}

	public void arc(int sxr, int syr, int exr, int eyr, int sxa, int sya,
			int exa, int eya) {
		
		double rx = Math.abs(exr - sxr)/2.0;
		double ry = Math.abs(eyr - syr)/2.0;
		if (rx <= 0 || ry <= 0) return;
		
		double cx = Math.min(sxr, exr) + rx;
		double cy = Math.min(syr, eyr) + ry;
		
		Element elem = null;
		if (sxa == exa && sya == eya) {
			if (rx == ry) {
				elem = doc.createElement("circle");
				elem.setAttribute("cx", "" + dc.toAbsoluteX((int)cx));
				elem.setAttribute("cy", "" + dc.toAbsoluteY((int)cy));
				elem.setAttribute("r", "" + dc.toRelativeX((int)rx));
			} else {
				elem = doc.createElement("ellipse");
				elem.setAttribute("cx", "" + dc.toAbsoluteX((int)cx));
				elem.setAttribute("cy", "" + dc.toAbsoluteY((int)cy));
				elem.setAttribute("rx", "" + dc.toRelativeX((int)rx));
				elem.setAttribute("ry", "" + dc.toRelativeY((int)ry));
			}
		} else {
			double sa = Math.atan2((sya - cy) * rx, (sxa - cx) * ry);
			double sx = rx * Math.cos(sa);
			double sy = ry * Math.sin(sa);
			
			double ea = Math.atan2((eya - cy) * rx, (exa - cx) * ry);
			double ex = rx * Math.cos(ea);
			double ey = ry * Math.sin(ea);
			
			double a = Math.atan2((ex-sx) * (-sy) - (ey-sy) * (-sx), (ex-sx) * (-sx) + (ey-sy) * (-sy));
			
			elem = doc.createElement("path");
			elem.setAttribute("d", "M " + dc.toAbsoluteX(Math.round(sx + cx)) + "," + dc.toAbsoluteY(Math.round(sy + cy))
					+ " A " + dc.toRelativeX((int)rx) + "," + dc.toRelativeY((int)ry)
					+ " 0 " + (a > 0 ? "1" : "0") + " 0"
					+ " " + dc.toAbsoluteX(Math.round(ex + cx)) + "," + dc.toAbsoluteY(Math.round(ey + cy)));
		}
		
		if (dc.getPen() != null) {
			elem.setAttribute("class", getClassString(dc.getPen()));
		}
		elem.setAttribute("fill", "none");
		parentNode.appendChild(elem);
	}

	public void bitBlt(byte[] image, int dx, int dy, int dw, int dh, 
			int sx, int sy, long rop) {
		bmpToSvg(image, dx, dy, dw, dh, sx, sy, dw, dh, Gdi.DIB_RGB_COLORS, rop);
	}

	public void chord(int sxr, int syr, int exr, int eyr, int sxa, int sya,
			int exa, int eya) {
		double rx = (Math.abs(exr - sxr)-1)/2.0;
		double ry = (Math.abs(eyr - syr)-1)/2.0;
		if (rx <= 0 || ry <= 0) return;
		
		double cx = Math.min(sxr, exr) + rx;
		double cy = Math.min(syr, eyr) + ry;
		
		Element elem = null;
		if (sxa == exa && sya == eya) {
			if (rx == ry) {
				elem = doc.createElement("circle");
				elem.setAttribute("cx", "" + dc.toAbsoluteX((int)cx));
				elem.setAttribute("cy", "" + dc.toAbsoluteY((int)cy));
				elem.setAttribute("r", "" + dc.toRelativeX((int)rx));
			} else {
				elem = doc.createElement("ellipse");
				elem.setAttribute("cx", "" + dc.toAbsoluteX((int)cx));
				elem.setAttribute("cy", "" + dc.toAbsoluteY((int)cy));
				elem.setAttribute("rx", "" + dc.toRelativeX((int)rx));
				elem.setAttribute("ry", "" + dc.toRelativeY((int)ry));
			}
		} else {
			double sa = Math.atan2((sya - cy) * rx, (sxa - cx) * ry);
			double sx = rx * Math.cos(sa);
			double sy = ry * Math.sin(sa);
			
			double ea = Math.atan2((eya - cy) * rx, (exa - cx) * ry);
			double ex = rx * Math.cos(ea);
			double ey = ry * Math.sin(ea);
			
			double a = Math.atan2((ex-sx) * (-sy) - (ey-sy) * (-sx), (ex-sx) * (-sx) + (ey-sy) * (-sy));
			
			elem = doc.createElement("path");
			elem.setAttribute("d", "M " + dc.toAbsoluteX(Math.round(sx + cx)) + "," + dc.toAbsoluteY(Math.round(sy + cy))
					+ " A " + dc.toRelativeX((int)rx) + "," + dc.toRelativeY((int)ry)
					+ " 0 " + (a > 0 ? "1" : "0") + " 0"
					+ " " + dc.toAbsoluteX(Math.round(ex + cx)) + "," + dc.toAbsoluteY(Math.round(ey + cy)) + " z");
		}

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defsNode.appendChild(dc.getBrush().createFillPattern(id));
			}
		}

		parentNode.appendChild(elem);
	}

	public GdiBrush createBrushIndirect(int style, int color, int hatch) {
		SvgBrush brush = new SvgBrush(this, style, color, hatch);
		if (!nameMap.containsKey(brush)) {
			String name = "brush" + (brushNo++);
			nameMap.put(brush, name);
			styleNode.appendChild(brush.createTextNode(name));
		}
		return brush;
	}

	public GdiFont createFontIndirect(int height, int width, int escapement,
			int orientation, int weight, boolean italic, boolean underline,
			boolean strikeout, int charset, int outPrecision,
			int clipPrecision, int quality, int pitchAndFamily, byte[] faceName) {
		SvgFont font = new SvgFont(this, height, width, escapement,
				orientation, weight, italic, underline, strikeout, charset,
				outPrecision, clipPrecision, quality, pitchAndFamily, faceName);
		if (!nameMap.containsKey(font)) {
			String name = "font" + (fontNo++);
			nameMap.put(font, name);
			styleNode.appendChild(font.createTextNode(name));
		}
		return font;
	}
	
	public GdiPalette createPalette() {
		// TODO
		log.fine("not implemented: createPalette");
		return new GdiPalette() {
		};
	}
	
	public GdiBrush createPatternBrush(byte[] image) {
		return new SvgPatternBrush(this, image);
	}

	public GdiPen createPenIndirect(int style, int width, int color) {
		SvgPen pen = new SvgPen(this, style, width, color);
		if (!nameMap.containsKey(pen)) {
			String name = "pen" + (penNo++);
			nameMap.put(pen, name);
			styleNode.appendChild(pen.createTextNode(name));
		}
		return pen;
	}
	
	public GdiRegion createRectRgn(int left, int top, int right, int bottom) {
		SvgRectRegion rgn = new SvgRectRegion(this, left, top, right, bottom);
		if (!nameMap.containsKey(rgn)) {
			nameMap.put(rgn, "rgn" + (rgnNo++));
			defsNode.appendChild(rgn.createElement());
		}
		return rgn;
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
	
	public void dibBitBlt(byte[] image, int dx, int dy, int dw, int dh,
			int sx, int sy, long rop) {
		bitBlt(image, dx, dy, dw, dh, sx, sy, rop);
	}

	public GdiBrush dibCreatePatternBrush(byte[] image, int usage) {
		// TODO
		log.fine("not implemented: dibCreatePatternBrush");
		return new GdiBrush() {
		};
	}
	
    public void dibStretchBlt(byte[] image, int dx, int dy, int dw, int dh,
			int sx, int sy, int sw, int sh, long rop) {
		
    	this.stretchDIBits(dx, dy, dw, dh, sx, sy, sw, sh, image, Gdi.DIB_RGB_COLORS, rop);
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
				defsNode.appendChild(dc.getBrush().createFillPattern(id));
			}
		}

		elem.setAttribute("cx", "" + dc.toAbsoluteX((sx + ex) / 2));
		elem.setAttribute("cy", "" + dc.toAbsoluteY((sy + ey) / 2));
		elem.setAttribute("rx", "" + dc.toRelativeX((ex - sx) / 2));
		elem.setAttribute("ry", "" + dc.toRelativeY((ey - sy) / 2));
		parentNode.appendChild(elem);
	}

	public void escape(byte[] data) {
	}

	public int excludeClipRect(int left, int top, int right, int bottom) {
		Element mask = dc.getMask();
		if (mask != null) {
			mask = (Element)mask.cloneNode(true);
			String name = "mask" + (maskNo++);
			mask.setAttribute("id", name);
			defsNode.appendChild(mask);
			
			Element unclip = doc.createElement("rect");
			unclip.setAttribute("x", "" + dc.toAbsoluteX(left));
			unclip.setAttribute("y", "" + dc.toAbsoluteY(top));
			unclip.setAttribute("width", "" + dc.toRelativeX(right - left));
			unclip.setAttribute("height", "" + dc.toRelativeY(bottom - top));
			unclip.setAttribute("fill", "black");
			mask.appendChild(unclip);
			dc.setMask(mask);
			
			return GdiRegion.COMPLEXREGION;
		} else {
			return GdiRegion.NULLREGION;
		}
	}

	public void extFloodFill(int x, int y, int color, int type) {
		// TODO
		log.fine("not implemented: extFloodFill");
	}

	public void extTextOut(int x, int y, int options, int[] rect, byte[] text, int[] dx) {
		Element elem = doc.createElement("text");

		int escapement = 0;
		boolean vertical = false;
		if (dc.getFont() != null) {
			elem.setAttribute("class", getClassString(dc.getFont()));
			if (dc.getFont().getFaceName().startsWith("@")) {
				vertical = true;
				escapement = dc.getFont().getEscapement()-2700;
			} else {
				escapement = dc.getFont().getEscapement();
			}
		}
		elem.setAttribute("fill", SvgObject.toColor(dc.getTextColor()));

		// style
		buffer.setLength(0);
		int align = dc.getTextAlign();

		if ((align & (TA_LEFT|TA_CENTER|TA_RIGHT)) == TA_RIGHT) {
			buffer.append("text-anchor: end; ");
		} else if ((align & (TA_LEFT|TA_CENTER|TA_RIGHT)) == TA_CENTER) {
			buffer.append("text-anchor: middle; ");
		}
		
		if (compatible) {
			buffer.append("dominant-baseline: baseline; ");
		} else {
			if (vertical) {
				elem.setAttribute("writing-mode", "tb");
			} else {
				if ((align & (TA_BOTTOM|TA_TOP|TA_BASELINE)) == TA_BASELINE) {
					buffer.append("dominant-baseline: baseline; ");
				} else {
					buffer.append("dominant-baseline: text-before-edge; ");				
				}
			}
		}

		if ((align & TA_RTLREADING) == TA_RTLREADING  || (options & ETO_RTLREADING) > 0) {
			buffer.append("unicode-bidi: bidi-override; direction: rtl; ");
		}

		if (dc.getTextSpace() > 0) {
			buffer.append("word-spacing: ").append(dc.getTextSpace()).append("; ");
		}
		
		if (buffer.length() > 0) {
			buffer.setLength(buffer.length()-1);
			elem.setAttribute("style", buffer.toString());
		}

		elem.setAttribute("stroke", "none");

		if ((align & (TA_NOUPDATECP|TA_UPDATECP)) == TA_UPDATECP) {
			x = dc.getCurrentX();
			y = dc.getCurrentY();
		}

		// x
		int ax = dc.toAbsoluteX(x);
		int width = 0;
		if (vertical) {
			elem.setAttribute("x", Integer.toString(ax));
			if (dc.getFont() != null) width = dc.getFont().getFontSize();
		} else {
			if (dc.getFont() != null) {
				dx = dc.getFont().validateDx(text, dx);
			}
			
			if (dx != null && dx.length > 0) {
				for (int i = 0; i < dx.length; i++) {
					width += dx[i];
				}

				int tx = x;

				if ((align & (TA_LEFT|TA_CENTER|TA_RIGHT)) == TA_RIGHT) {
					tx -= (width-dx[dx.length-1]);
				} else if ((align & (TA_LEFT|TA_CENTER|TA_RIGHT)) == TA_CENTER) {
					tx -= (width-dx[dx.length-1]) / 2;
				}
				
				buffer.setLength(0);
				for (int i = 0; i < dx.length; i++) {
					if (i > 0) buffer.append(" ");
					buffer.append(dc.toAbsoluteX(tx));
					tx += dx[i];
				}
				if ((align & (TA_NOUPDATECP|TA_UPDATECP)) == TA_UPDATECP) {
					dc.moveToEx(tx, y, null);
				}
				elem.setAttribute("x", buffer.toString());
			} else {
				if (dc.getFont() != null) width = (dc.getFont().getFontSize() * text.length)/2;
				elem.setAttribute("x", Integer.toString(ax));				
			}
		}
		
		// y
		int ay = dc.toAbsoluteY(y);
		int height = 0;
		if (vertical) {
			if (dc.getFont() != null) {
				dx = dc.getFont().validateDx(text, dx);
			}
			
			buffer.setLength(0);
			if(align == 0) {
				buffer.append(ay + Math.abs(dc.toRelativeY(dc.getFont().getHeight())));
			} else {
				buffer.append(ay);
			}
			
			if (dx != null && dx.length > 0) {
				for (int i = 0; i < dx.length - 1; i++) {
					height += dx[i];
				}
				
				int ty = y;

				if ((align & (TA_LEFT|TA_CENTER|TA_RIGHT)) == TA_RIGHT) {
					ty -= (height-dx[dx.length-1]);
				} else if ((align & (TA_LEFT|TA_CENTER|TA_RIGHT)) == TA_CENTER) {
					ty -= (height-dx[dx.length-1]) / 2;
				}
				
				for (int i = 0; i < dx.length; i++) {
					buffer.append(" ");
					buffer.append(dc.toAbsoluteY(ty));
					ty += dx[i];
				}
	
				if ((align & (TA_NOUPDATECP|TA_UPDATECP)) == TA_UPDATECP) {
					dc.moveToEx(x, ty, null);
				}
			} else {
				if (dc.getFont() != null) height = (dc.getFont().getFontSize() * text.length)/2;
			}
			elem.setAttribute("y", buffer.toString());
		} else {
			if (dc.getFont() != null) height = dc.getFont().getFontSize();
			if (compatible) {
				if ((align & (TA_BOTTOM|TA_TOP|TA_BASELINE)) == TA_TOP) {
					elem.setAttribute("y", Integer.toString(ay + Math.abs(dc.toRelativeY(height*0.88))));	
				} else if ((align & (TA_BOTTOM|TA_TOP|TA_BASELINE)) == TA_BOTTOM) {
					elem.setAttribute("y", Integer.toString(ay + rect[3] - rect[1] + Math.abs(dc.toRelativeY(height*0.88))));					
				} else {
					elem.setAttribute("y", Integer.toString(ay));
				}
			} else {
				if((align & (TA_BOTTOM|TA_TOP|TA_BASELINE)) == TA_BOTTOM && rect != null) {
					elem.setAttribute("y", Integer.toString(ay + rect[3] - rect[1] - Math.abs(dc.toRelativeY(height))));
				} else {
					elem.setAttribute("y", Integer.toString(ay));
				}				
			}
		}
		
		Element bk = null;
		if (dc.getBkMode() == OPAQUE || (options & ETO_OPAQUE) > 0) {
			if (rect == null && dc.getFont() != null) {
				rect = new int[4];
				if (vertical) {
					rect[0] = x-(int)(width * 0.85);
					if ((align & (TA_LEFT|TA_RIGHT|TA_CENTER)) == TA_RIGHT) {
						rect[0] = y-height;
					} else if ((align & (TA_LEFT|TA_RIGHT|TA_CENTER)) == TA_CENTER) {
						rect[0] = y-height/2;
					} else {
						rect[0] = y;
					}
				} else {
					if ((align & (TA_LEFT|TA_RIGHT|TA_CENTER)) == TA_RIGHT) {
						rect[0] = x-width;						
					} else if ((align & (TA_LEFT|TA_RIGHT|TA_CENTER)) == TA_CENTER) {
						rect[0] = x-width/2;
					} else {
						rect[0] = x;
					}
					rect[1] = y-(int)(height * 0.85);
				}
				rect[2] = width;
				rect[3] = height;
			}
			bk = doc.createElement("rect");
			bk.setAttribute("x", Integer.toString(dc.toAbsoluteX(rect[0])));
			bk.setAttribute("y", Integer.toString(dc.toAbsoluteY(rect[1])));
			bk.setAttribute("width", Integer.toString(dc.toRelativeX(rect[2] - rect[0])));
			bk.setAttribute("height", Integer.toString(dc.toRelativeY(rect[3] - rect[1])));
			bk.setAttribute("fill", SvgObject.toColor(dc.getBkColor()));
		}
		
		Element clip = null;
		if ((options & ETO_CLIPPED) > 0) {
			String name = "clipPath" + (clipPathNo++);
			clip = doc.createElement("clipPath");
			clip.setAttribute("id", name);
			clip.setIdAttribute("id", true);
			
			Element clipRect = doc.createElement("rect");
			clipRect.setAttribute("x", Integer.toString(dc.toAbsoluteX(rect[0])));
			clipRect.setAttribute("y", Integer.toString(dc.toAbsoluteY(rect[1])));
			clipRect.setAttribute("width", Integer.toString(dc.toRelativeX(rect[2] - rect[0])));
			clipRect.setAttribute("height", Integer.toString(dc.toRelativeY(rect[3] - rect[1])));
			
			clip.appendChild(clipRect);
			elem.setAttribute("clip-path", "url(#" + name + ")");
		}
		
		String str = null;
		if (dc.getFont() != null) {
			str = GdiUtils.convertString(text, dc.getFont().getCharset());
		} else {
			str = GdiUtils.convertString(text, GdiFont.DEFAULT_CHARSET);
		}

		if (dc.getFont() != null && dc.getFont().getLang() != null) {
			elem.setAttribute("xml:lang", dc.getFont().getLang());
		}
		
		elem.setAttribute("xml:space", "preserve");
		if (compatible) {
			str = str.replaceAll("\\r\\n|[\\t\\r\\n ]", "\u00A0");
		}
		elem.appendChild(doc.createTextNode(str));
		
		if (bk != null || clip != null) {
			Element g = doc.createElement("g");
			if (bk != null) g.appendChild(bk);
			if (clip != null) g.appendChild(clip);
			g.appendChild(elem);
			elem = g;
		}
		
		if (escapement != 0)  {
			elem.setAttribute("transform", "rotate(" + (-escapement/10.0) + ", " + ax + ", " + ay + ")");
		}
		parentNode.appendChild(elem);
	}

	public void fillRgn(GdiRegion rgn, GdiBrush brush) {
		if (rgn == null) return;
		
		Element elem = doc.createElement("use");
		elem.setAttribute("xlink:href", "url(#" + nameMap.get(rgn) + ")");
		elem.setAttribute("class", getClassString(brush));
		SvgBrush sbrush = (SvgBrush)brush;
		if(sbrush.getStyle() == GdiBrush.BS_HATCHED) {
			String id = "pattern" + (patternNo++);
			elem.setAttribute("fill", "url(#" + id + ")");
			defsNode.appendChild(sbrush.createFillPattern(id));
		}
		parentNode.appendChild(elem);
	}

	public void floodFill(int x, int y, int color) {
		// TODO
		log.fine("not implemented: floodFill");
	}

	public void frameRgn(GdiRegion rgn, GdiBrush brush, int width, int height) {
		// TODO
		log.fine("not implemented: frameRgn");
	}

	public void intersectClipRect(int left, int top, int right, int bottom) {
		// TODO
		log.fine("not implemented: intersectClipRect");
	}

	public void invertRgn(GdiRegion rgn) {
		if (rgn == null) return;
		
		Element elem = doc.createElement("use");
		elem.setAttribute("xlink:href", "url(#" + nameMap.get(rgn) + ")");
		String ropFilter = dc.getRopFilter(DSTINVERT);
		if (ropFilter != null) {
			elem.setAttribute("filter", ropFilter);
		}
		parentNode.appendChild(elem);
	}

	public void lineTo(int ex, int ey) {
		Element elem = doc.createElement("line");
		if (dc.getPen() != null) {
			elem.setAttribute("class", getClassString(dc.getPen()));
		}

		elem.setAttribute("fill", "none");

		elem.setAttribute("x1", "" + dc.toAbsoluteX(dc.getCurrentX()));
		elem.setAttribute("y1", "" + dc.toAbsoluteY(dc.getCurrentY()));
		elem.setAttribute("x2", "" + dc.toAbsoluteX(ex));
		elem.setAttribute("y2", "" + dc.toAbsoluteY(ey));
		parentNode.appendChild(elem);

		dc.moveToEx(ex, ey, null);
	}

	public void moveToEx(int x, int y, Point old) {
		dc.moveToEx(x, y, old);
	}

	public void offsetClipRgn(int x, int y) {
		dc.offsetClipRgn(x, y);
		Element mask = dc.getMask();
		if (mask != null) {
			mask = (Element)mask.cloneNode(true);
			String name = "mask" + (maskNo++);
			mask.setAttribute("id", name);
			if (dc.getOffsetClipX() != 0 || dc.getOffsetClipY() != 0) {
				mask.setAttribute("transform", "translate(" + dc.getOffsetClipX() + "," + dc.getOffsetClipY() + ")");
			}
			defsNode.appendChild(mask);
			
			if (!parentNode.hasChildNodes()) {
				doc.getDocumentElement().removeChild(parentNode);
			}
			parentNode = doc.createElement("g");
			parentNode.setAttribute("mask", name);
			doc.getDocumentElement().appendChild(parentNode);
			
			dc.setMask(mask);
		}
	}

	public void offsetViewportOrgEx(int x, int y, Point point) {
		dc.offsetViewportOrgEx(x, y, point);
	}

	public void offsetWindowOrgEx(int x, int y, Point point) {
		dc.offsetWindowOrgEx(x, y, point);
	}

	public void paintRgn(GdiRegion rgn) {
		fillRgn(rgn, dc.getBrush());
	}

	public void patBlt(int x, int y, int width, int height, long rop) {
		// TODO
		log.fine("not implemented: patBlt");
	}

	public void pie(int sxr, int syr, int exr, int eyr, int sxa, int sya,
			int exa, int eya) {
		double rx = (Math.abs(exr - sxr)-1)/2.0;
		double ry = (Math.abs(eyr - syr)-1)/2.0;
		if (rx <= 0 || ry <= 0) return;
		
		double cx = Math.min(sxr, exr) + rx;
		double cy = Math.min(syr, eyr) + ry;
		
		Element elem = null;
		if (sxa == exa && sya == eya) {
			if (rx == ry) {
				elem = doc.createElement("circle");
				elem.setAttribute("cx", "" + dc.toAbsoluteX((int)cx));
				elem.setAttribute("cy", "" + dc.toAbsoluteY((int)cy));
				elem.setAttribute("r", "" + dc.toRelativeX((int)rx));
			} else {
				elem = doc.createElement("ellipse");
				elem.setAttribute("cx", "" + dc.toAbsoluteX((int)cx));
				elem.setAttribute("cy", "" + dc.toAbsoluteY((int)cy));
				elem.setAttribute("rx", "" + dc.toRelativeX((int)rx));
				elem.setAttribute("ry", "" + dc.toRelativeY((int)ry));
			}
		} else {
			double sa = Math.atan2((sya - cy) * rx, (sxa - cx) * ry);
			double sx = rx * Math.cos(sa);
			double sy = ry * Math.sin(sa);
			
			double ea = Math.atan2((eya - cy) * rx, (exa - cx) * ry);
			double ex = rx * Math.cos(ea);
			double ey = ry * Math.sin(ea);
			
			double a = Math.atan2((ex-sx) * (-sy) - (ey-sy) * (-sx), (ex-sx) * (-sx) + (ey-sy) * (-sy));
			
			elem = doc.createElement("path");
			elem.setAttribute("d", "M " + dc.toAbsoluteX(Math.round(sx + cx)) + "," + dc.toAbsoluteY(Math.round(sy + cy))
					+ " L " + dc.toAbsoluteX(Math.round(sx + cx)) + "," + dc.toAbsoluteY(Math.round(sy + cy))
					+ " A " + dc.toRelativeX((int)rx) + "," + dc.toRelativeY((int)ry)
					+ " 0 " + (a > 0 ? "1" : "0") + " 0"
					+ " " + dc.toAbsoluteX(Math.round(ex + cx)) + "," + dc.toAbsoluteY(Math.round(ey + cy)) + " z");
		}

		if (dc.getPen() != null || dc.getBrush() != null) {
			elem.setAttribute("class", getClassString(dc.getPen(), dc
					.getBrush()));
			if (dc.getBrush() != null
					&& dc.getBrush().getStyle() == GdiBrush.BS_HATCHED) {
				String id = "pattern" + (patternNo++);
				elem.setAttribute("fill", "url(#" + id + ")");
				defsNode.appendChild(dc.getBrush().createFillPattern(id));
			}
		}
		parentNode.appendChild(elem);
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
				defsNode.appendChild(dc.getBrush().createFillPattern(id));
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
		parentNode.appendChild(elem);
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
		parentNode.appendChild(elem);
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
				defsNode.appendChild(dc.getBrush().createFillPattern(id));
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
		parentNode.appendChild(elem);
	}

	public void realizePalette() {
		// TODO
		log.fine("not implemented: realizePalette");
	}

	public void restoreDC() {
		if(saveDC != null) {
			dc = saveDC;
			if (!parentNode.hasChildNodes()) {
				doc.getDocumentElement().removeChild(parentNode);
			}
			parentNode = doc.createElement("g");
			Element mask = dc.getMask();
			if (mask != null) {
				parentNode.setAttribute("mask", "url(#" + mask.getAttribute("id") + ")");
			}
			doc.getDocumentElement().appendChild(parentNode);
		}
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
				defsNode.appendChild(dc.getBrush().createFillPattern(id));
			}
		}

		elem.setAttribute("x", "" + dc.toAbsoluteX(sx));
		elem.setAttribute("y", "" + dc.toAbsoluteY(sy));
		elem.setAttribute("width", "" + dc.toRelativeX(ex - sx));
		elem.setAttribute("height", "" + dc.toRelativeY(ey - sy));
		parentNode.appendChild(elem);
	}

	public void resizePalette(GdiPalette palette) {
		// TODO
		log.fine("not implemented: ResizePalette");
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
				defsNode.appendChild(dc.getBrush().createFillPattern(id));
			}
		}

		elem.setAttribute("x", "" + dc.toAbsoluteX(sx));
		elem.setAttribute("y", "" + dc.toAbsoluteY(sy));
		elem.setAttribute("width", "" + dc.toRelativeX(ex - sx));
		elem.setAttribute("height", "" + dc.toRelativeY(ey - sy));
		elem.setAttribute("rx", "" + dc.toRelativeX(rw));
		elem.setAttribute("ry", "" + dc.toRelativeY(rh));
		parentNode.appendChild(elem);
	}

	public void seveDC() {
		saveDC = (SvgDc) dc.clone();
	}

	public void scaleViewportExtEx(int x, int xd, int y, int yd, Size old) {
		dc.scaleViewportExtEx(x, xd, y, yd, old);
	}

	public void scaleWindowExtEx(int x, int xd, int y, int yd, Size old) {
		dc.scaleWindowExtEx(x, xd, y, yd, old);
	}

	public void selectClipRgn(GdiRegion rgn) {
		if (!parentNode.hasChildNodes()) {
			doc.getDocumentElement().removeChild(parentNode);
		}
		parentNode = doc.createElement("g");
		
		if (rgn != null) {
			Element mask = doc.createElement("mask");
			mask.setAttribute("id", "mask" + (maskNo++));
			mask.setIdAttribute("id", true);
			
			if (dc.getOffsetClipX() != 0 || dc.getOffsetClipY() != 0) {
				mask.setAttribute("transform", "translate(" + dc.getOffsetClipX() + "," + dc.getOffsetClipY() + ")");
			}
			defsNode.appendChild(mask);
			
			Element clip = doc.createElement("use");
			clip.setAttribute("xlink:href", "url(#" + nameMap.get(rgn) + ")");
			clip.setAttribute("fill", "white");
			
			mask.appendChild(clip);
			
			parentNode.setAttribute("mask", "url(#" + mask.getAttribute("id") + ")");
		}
		
		doc.getDocumentElement().appendChild(parentNode);
	}

	public void selectObject(GdiObject obj) {
		if (obj instanceof SvgBrush) {
			dc.setBrush((SvgBrush) obj);
		} else if (obj instanceof SvgFont) {
			dc.setFont((SvgFont) obj);
		} else if (obj instanceof SvgPen) {
			dc.setPen((SvgPen) obj);
		}
	}

	public void selectPalette(GdiPalette palette, boolean mode) {
		// TODO
		log.fine("not implemented: selectPalette");
	}

	public void setBkColor(int color) {
		dc.setBkColor(color);
	}

	public void setBkMode(int mode) {
		dc.setBkMode(mode);
	}

	public void setDIBitsToDevice(int dx, int dy, int dw, int dh, int sx,
			int sy, int startscan, int scanlines, byte[] image, int colorUse) {
		stretchDIBits(dx, dy, dw, dh, sx, sy, dw, dh, image, colorUse, SRCCOPY);
	}
	
	public void setLayout(long layout) {
		dc.setLayout(layout);
	}
	
	public void setMapMode(int mode) {
		dc.setMapMode(mode);
	}

	public void setMapperFlags(long flags) {
		dc.setMapperFlags(flags);
	}

	public void setPaletteEntries(GdiPalette palette, int startIndex, int entryCount, byte[] entries) {
		// TODO
		log.fine("not implemented: setPaletteEntries");
	}

	public void setPixel(int x, int y, int color) {
		Element elem = doc.createElement("rect");
		elem.setAttribute("stroke", "none");
		elem.setAttribute("fill", SvgPen.toColor(color));
		elem.setAttribute("x", "" + dc.toAbsoluteX(x));
		elem.setAttribute("y", "" + dc.toAbsoluteY(y));
		elem.setAttribute("width", "" + dc.toRelativeX(1));
		elem.setAttribute("height", "" + dc.toRelativeY(1));
		parentNode.appendChild(elem);
	}

	public void setPolyFillMode(int mode) {
		dc.setPolyFillMode(mode);
	}
	
	public void setRelAbs(int mode) {
		dc.setRelAbs(mode);
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
	
	public void setViewportExtEx(int x, int y, Size old) {
		dc.setViewportExtEx(x, y, old);
	}

	public void setViewportOrgEx(int x, int y, Point old) {
		dc.setViewportOrgEx(x, y, old);
	}

	public void setWindowExtEx(int width, int height, Size old) {
		dc.setWindowExtEx(width, height, old);
	}

	public void setWindowOrgEx(int x, int y, Point old) {
		dc.setWindowOrgEx(x, y, old);
	}

	public void stretchBlt(byte[] image, int dx, int dy, int dw, int dh, int sx, int sy,
			int sw, int sh, long rop) {
		dibStretchBlt(image, dx, dy, dw, dh, sx, sy, sw, sh, rop);
	}

	public void stretchDIBits(int dx, int dy, int dw, int dh, int sx, int sy,
			int sw, int sh, byte[] image, int usage, long rop) {
		bmpToSvg(image, dx, dy, dw, dh, sx, sy, sw, sh, usage, rop);
	}
	
	public void textOut(int x, int y, byte[] text) {
		Element elem = doc.createElement("text");
		
		int escapement = 0;
		boolean vertical = false;
		if (dc.getFont() != null) {
			elem.setAttribute("class", getClassString(dc.getFont()));
			if (dc.getFont().getFaceName().startsWith("@")) {
				vertical = true;
				escapement = dc.getFont().getEscapement()-2700;
			} else {
				escapement = dc.getFont().getEscapement();
			}
		}
		elem.setAttribute("fill", SvgObject.toColor(dc.getTextColor()));

		// style
		buffer.setLength(0);
		int align = dc.getTextAlign();

		if ((align & (TA_LEFT|TA_RIGHT|TA_CENTER)) == TA_RIGHT) {
			buffer.append("text-anchor: end; ");
		} else if ((align & (TA_LEFT|TA_RIGHT|TA_CENTER)) == TA_CENTER) {
			buffer.append("text-anchor: middle; ");
		}

		if (vertical) {
			elem.setAttribute("writing-mode", "tb");
			buffer.append("dominant-baseline: ideographic; ");
		} else {
			if ((align & (TA_BOTTOM|TA_TOP|TA_BASELINE)) == TA_BASELINE) {
				buffer.append("dominant-baseline: baseline; ");
			} else {
				buffer.append("dominant-baseline: text-before-edge; ");				
			}
		}

		if ((align & TA_RTLREADING) == TA_RTLREADING) {
			buffer.append("unicode-bidi: bidi-override; direction: rtl; ");
		}

		if (dc.getTextSpace() > 0) {
			buffer.append("word-spacing: " + dc.getTextSpace() + "; ");
		}
		
		if (buffer.length() > 0) {
			buffer.setLength(buffer.length()-1);
			elem.setAttribute("style", buffer.toString());
		}

		elem.setAttribute("stroke", "none");
		
		int ax = dc.toAbsoluteX(x);
		int ay = dc.toAbsoluteY(y);
		elem.setAttribute("x", Integer.toString(ax));
		elem.setAttribute("y", Integer.toString(ay));
				
		if (escapement != 0)  {
			elem.setAttribute("transform", "rotate(" + (-escapement/10.0) + ", " + ax + ", " + ay + ")");
		}

		String str = null;
		if (dc.getFont() != null) {
			str = GdiUtils.convertString(text, dc.getFont().getCharset());
		} else {
			str = GdiUtils.convertString(text, GdiFont.DEFAULT_CHARSET);
		}

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

		if (dc.getFont() != null && dc.getFont().getLang() != null) {
			elem.setAttribute("xml:lang", dc.getFont().getLang());
		}
		elem.setAttribute("xml:space", "preserve");
		if (compatible) {
			str = str.replaceAll("\\r\\n|[\\t\\r\\n ]", "\u00A0");
		}
		elem.appendChild(doc.createTextNode(str));
		parentNode.appendChild(elem);
	}

	public void footer() {
		Element root = doc.getDocumentElement();
		if (!root.hasAttribute("width") && dc.getWindowWidth() != 0) {
			root.setAttribute("width", "" + Math.abs(dc.getWindowWidth()));
		}
		if (!root.hasAttribute("height") && dc.getWindowHeight() != 0) {
			root.setAttribute("height", "" + Math.abs(dc.getWindowHeight()));
		}
		if (dc.getWindowWidth() != 0 && dc.getWindowHeight() != 0) {
			root.setAttribute("viewBox", "0 0 " + Math.abs(dc.getWindowWidth()) + " " + Math.abs(dc.getWindowHeight()));
			root.setAttribute("preserveAspectRatio", "none");
		}
		root.setAttribute("stroke-linecap", "round");
		root.setAttribute("fill-rule", "evenodd");
		
		if (!styleNode.hasChildNodes()) {
			root.removeChild(styleNode);
		} else {
			styleNode.insertBefore(doc.createTextNode("\n"), styleNode.getFirstChild());
		}

		if (!defsNode.hasChildNodes()) {
			root.removeChild(defsNode);
		}
	}

	private String getClassString(GdiObject obj1, GdiObject obj2) {
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
		return "";
	}

	private String getClassString(GdiObject style) {
		if (style == null) {
			return "";
		}

		return (String) nameMap.get(style);
	}
	
	private void bmpToSvg(byte[] image, int dx, int dy, int dw, int dh, int sx, int sy,
			int sw, int sh, int usage, long rop) {
		if (image == null) {
			// TODO
			return;
		}
		
		image = ImageUtil.convert(dibToBmp(image), "png", dh < 0);
	
		StringBuffer buffer = new StringBuffer("data:image/png;base64,");
		buffer.append(Base64.encode(image));
		String data = buffer.toString();
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
		
		String ropFilter = dc.getRopFilter(rop);
		if (ropFilter != null) {
			elem.setAttribute("filter", ropFilter);
		}
		
		elem.setAttribute("xlink:href", data);
		parentNode.appendChild(elem);
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
