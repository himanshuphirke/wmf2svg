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

import org.w3c.dom.Text;

import net.arnx.wmf2svg.gdi.*;

/**
 * @author Hidekatsu Izuno
 */
class SvgFont extends SvgObject implements GdiFont {
	private int height;
	private int width;
	private int escapement;
	private int orientation;
	private int weight;
	private boolean italic;
	private boolean underline;
	private boolean strikeout;
	private int charset;
	private int outPrecision;
	private int clipPrecision;
	private int quality;
	private int pitchAndFamily;
	
	private String faceName;
	private String lang;

	public SvgFont(
		SvgGdi gdi,
		int height,
		int width,
		int escapement,
		int orientation,
		int weight,
		boolean italic,
		boolean underline,
		boolean strikeout,
		int charset,
		int outPrecision,
		int clipPrecision,
		int quality,
		int pitchAndFamily,
		byte[] faceName) {
		
		super(gdi);
		this.height = height;
		this.width = width;
		this.escapement = escapement;
		this.orientation = orientation;
		this.weight = weight;
		this.italic = italic;
		this.underline = underline;
		this.strikeout = strikeout;
		this.charset = charset;
		this.outPrecision = outPrecision;
		this.clipPrecision = clipPrecision;
		this.quality = quality;
		this.pitchAndFamily = pitchAndFamily;
		this.faceName = convertEncoding(faceName);
		
		// xml:lang
		this.lang = (String)getGDI().getProperty("lang." + getCharsetString());
		if (this.lang != null && this.lang.trim().length() == 0) this.lang = null;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getEscapement() {
		return escapement;
	}
	
	public int getOrientation() {
		return orientation;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public boolean isItalic() {
		return italic;
	}
	
	public boolean isUnderlined() {
		return underline;
	}
	
	public boolean isStrikedOut() {
		return strikeout;
	}
	
	public int getCharset() {
		return charset;
	}
	
	public int getOutPrecision() {
		return outPrecision;
	}
	
	public int getClipPrecision() {
		return clipPrecision;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public int getPitchAndFamily() {
		return pitchAndFamily;
	}
	
	public String getFaceName() {
		return faceName;
	}
	
	public String convertEncoding(byte[] chars) {
		String str = null;
		String encoding = null;

		int length = 0;
		while (length < chars.length && chars[length] != 0) {
			length++;
		}

		encoding = (String)getGDI().getProperty("charset." + getCharsetString());
		if (encoding == null) {
			encoding = "Cp1252";
		}
		
		try {
			str = new String(chars, 0, length, encoding);
		} catch (java.io.UnsupportedEncodingException e) {
			try {
				str = new String(chars, 0, length, "Cp1252");
			} catch (java.io.UnsupportedEncodingException e2) {
			}
		}
		return str;
	}

	public String getLang() {
		return lang;
	}

	private String getCharsetString() {
		switch (charset) {
			case ANSI_CHARSET :
				return "ANSI_CHARSET";
			case SYMBOL_CHARSET :
				return "SYMBOL_CHARSET";
			case MAC_CHARSET :
				return "MAC_CHARSET";
			case SHIFTJIS_CHARSET :
				return "SHIFTJIS_CHARSET";
			case HANGUL_CHARSET :
				return "HANGUL_CHARSET";
			case JOHAB_CHARSET :
				return "JOHAB_CHARSET";
			case GB2312_CHARSET :
				return "GB2312_CHARSET";
			case CHINESEBIG5_CHARSET :
				return "CHINESEBIG5_CHARSET";
			case GREEK_CHARSET :
				return "GREEK_CHARSET";
			case TURKISH_CHARSET :
				return "TURKISH_CHARSET";
			case VIETNAMESE_CHARSET :
				return "VIETNAMESE_CHARSET";
			case HEBREW_CHARSET :
				return "HEBREW_CHARSET";
			case ARABIC_CHARSET :
				return "ARABIC_CHARSET";
			case BALTIC_CHARSET :
				return "BALTIC_CHARSET";
			case RUSSIAN_CHARSET :
				return "RUSSIAN_CHARSET";
			case THAI_CHARSET :
				return "THAI_CHARSET";
			case EASTEUROPE_CHARSET :
				return "EASTEUROPE_CHARSET";
			case OEM_CHARSET :
				return "OEM_CHARSET";
			default :
				return "DEFAULT_CHARSET";
		}
	}

	private String getPitchString() {
		int pitch = pitchAndFamily & 0x00000003;
		switch (pitch) {
			case FIXED_PITCH :
				return "FIXED_PITCH";
			case VARIABLE_PITCH :
				return "VARIABLE_PITCH";
			default :
				return "DEFAULT_PITCH";
		}
	}

	private String getFontFamilyString() {
		int family = pitchAndFamily & 0x000000F0;
		switch (family) {
			case FF_DECORATIVE :
				return "FF_DECORATIVE";
			case FF_MODERN :
				return "FF_MODERN";
			case FF_ROMAN :
				return "FF_ROMAN";
			case FF_SCRIPT :
				return "FF_SCRIPT";
			case FF_SWISS :
				return "FF_SWISS";
			default :
				return "FF_DONTCARE";
		}
	}

	public int[] validateDx(byte[] chars, int[] dx) {
		if (dx == null || dx.length == 0) {
			return null;
		}

		List list = (List)getGDI().getProperty("first-byte-area." + getCharsetString());
		if (list == null || list.isEmpty()) {
			return dx;
		}

		int n = 0;
		boolean skip = false;

		for (int i = 0; i < chars.length && i < dx.length; i++) {
			int c = (0xFF & chars[i]);

			if (skip) {
				dx[n - 1] += dx[i];
				skip = false;
				continue;
			}

			for (int j = 0; j < list.size(); j++) {
				int[] area = (int[]) list.get(j);
				if (area[0] <= c && c <= area[1]) {
					skip = true;
					break;
				}
			}

			dx[n++] = dx[i];
		}

		int[] ndx = new int[n];
		System.arraycopy(dx, 0, ndx, 0, n);

		return ndx;
	}
	
	public int getFontSize() {
		return Math.abs(getGDI().getDC().toRelativeY(height));
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + charset;
		result = PRIME * result + clipPrecision;
		result = PRIME * result + escapement;
		result = PRIME * result + ((faceName == null) ? 0 : faceName.hashCode());
		result = PRIME * result + height;
		result = PRIME * result + (italic ? 1231 : 1237);
		result = PRIME * result + orientation;
		result = PRIME * result + outPrecision;
		result = PRIME * result + pitchAndFamily;
		result = PRIME * result + quality;
		result = PRIME * result + (strikeout ? 1231 : 1237);
		result = PRIME * result + (underline ? 1231 : 1237);
		result = PRIME * result + weight;
		result = PRIME * result + width;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SvgFont other = (SvgFont) obj;
		if (charset != other.charset)
			return false;
		if (clipPrecision != other.clipPrecision)
			return false;
		if (escapement != other.escapement)
			return false;
		if (faceName == null) {
			if (other.faceName != null)
				return false;
		} else if (!faceName.equals(other.faceName))
			return false;
		if (height != other.height)
			return false;
		if (italic != other.italic)
			return false;
		if (orientation != other.orientation)
			return false;
		if (outPrecision != other.outPrecision)
			return false;
		if (pitchAndFamily != other.pitchAndFamily)
			return false;
		if (quality != other.quality)
			return false;
		if (strikeout != other.strikeout)
			return false;
		if (underline != other.underline)
			return false;
		if (weight != other.weight)
			return false;
		if (width != other.width)
			return false;
		return true;
	}
	
	public Text createTextNode(String id) {
		return getGDI().getDocument().createTextNode("." + id + " { " + toString() + " }\n");
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		// font-style
		if (italic) {
			buffer.append("font-style: italic; ");
		}

		// font-weight
		if (weight != FW_DONTCARE && weight != FW_NORMAL) {
			if (weight < 100) {
				weight = 100;
			} else if (weight > 900) {
				weight = 900;
			} else {
				weight = (weight / 100) * 100;
			}

			if (weight == FW_BOLD) {
				buffer.append("font-weight: bold; ");
			} else {
				buffer.append("font-weight: " + weight + "; ");
			}
		}
		
		int fontSize = getFontSize();
		if (fontSize != 0) buffer.append("font-size: ").append(fontSize).append("px; ");

		// font-family
		List fontList = new ArrayList();
		if (faceName.length() != 0) {
			String fontFamily = faceName;
			if (faceName.charAt(0) == '@') fontFamily = faceName.substring(1);
			fontList.add(fontFamily);

			String altfont =
				(String)getGDI().getProperty("alternative-font." + fontFamily);
			if (altfont != null && altfont.length() != 0) {
				fontList.add(altfont);
			}
		}

		String pitch = (String)getGDI().getProperty("generic-font." + getPitchString());
		String family =
			(String)getGDI().getProperty("generic-font." + getFontFamilyString());
		if (pitch != null && pitch.length() != 0) {
			fontList.add(pitch);
		} else if (family != null && family.length() != 0) {
			fontList.add(family);
		}

		if (!fontList.isEmpty()) {
			buffer.append("font-family:");
		}

		boolean isVertical = false;
		for (Iterator i = fontList.iterator(); i.hasNext();) {
			String font = (String)i.next();
			if (font.indexOf(" ") != -1) {
				buffer.append(" \"" + font + "\"");
			} else {
				buffer.append(" " + font);
			}

			if (i.hasNext()) {
				buffer.append(",");
			}
		}
		buffer.append("; ");

		// text-decoration
		if (underline || strikeout) {
			buffer.append("text-decoration:");
			if (underline) {
				buffer.append(" underline");
			}
			if (strikeout) {
				buffer.append(" overline");
			}
			buffer.append("; ");
		}
		
		if (buffer.length() > 0) buffer.setLength(buffer.length()-1);
		return buffer.toString();
	}	
}
