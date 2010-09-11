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
package net.arnx.wmf2svg.gdi.wmf;

import java.io.*;
import java.nio.ByteOrder;
import java.util.logging.Logger;

import net.arnx.wmf2svg.gdi.*;
import net.arnx.wmf2svg.io.DataInput;

/**
 * @author Hidekatsu Izuno
 * @author Shunsuke Mori
 */
public class WmfParser {
	private static final int RECORD_ANIMATE_PALETTE = 0x0436;
	private static final int RECORD_ARC = 0x0817;
	private static final int RECORD_BIT_BLT = 0x0922;
	private static final int RECORD_CHORD = 0x0830;
	private static final int RECORD_CREATE_BRUSH_INDIRECT = 0x02FC;
	private static final int RECORD_CREATE_FONT_INDIRECT = 0x02FB;
	private static final int RECORD_CREATE_PALETTE = 0x00F7;
	private static final int RECORD_CREATE_PATTERN_BRUSH = 0x01F9;
	private static final int RECORD_CREATE_PEN_INDIRECT = 0x02FA;
	private static final int RECORD_CREATE_RECT_RGN = 0x0149;
	private static final int RECORD_DELETE_OBJECT = 0x01F0;
	private static final int RECORD_DIB_BIT_BLT = 0x0940;
	private static final int RECORD_DIB_CREATE_PATTERN_BRUSH = 0x0142;
	private static final int RECORD_DIB_STRETCH_BLT = 0x0B41;
	private static final int RECORD_ELLIPSE = 0x0418;
	private static final int RECORD_ESCAPE = 0x0626;
	private static final int RECORD_EXCLUDE_CLIP_RECT = 0x0415;
	private static final int RECORD_EXT_FLOOD_FILL = 0x0548;
	private static final int RECORD_EXT_TEXT_OUT = 0x0A32;
	private static final int RECORD_FILL_RGN = 0x0228;
	private static final int RECORD_FLOOD_FILL = 0x0419;
	private static final int RECORD_FRAME_RGN = 0x0429;
	private static final int RECORD_INTERSECT_CLIP_RECT = 0x0416;
	private static final int RECORD_INVERT_RGN = 0x012A;
	private static final int RECORD_LINE_TO = 0x0213;
	private static final int RECORD_MOVE_TO_EX = 0x0214;
	private static final int RECORD_OFFSET_CLIP_RGN = 0x0220;
	private static final int RECORD_OFFSET_VIEWPORT_ORG_EX = 0x0211;
	private static final int RECORD_OFFSET_WINDOW_ORG_EX = 0x020F;
	private static final int RECORD_PAINT_RGN = 0x012B;
	private static final int RECORD_PAT_BLT = 0x061D;
	private static final int RECORD_PIE = 0x081A;
	private static final int RECORD_POLYGON = 0x0324;
	private static final int RECORD_POLYLINE = 0x0325;
	private static final int RECORD_POLY_POLYGON = 0x0538;
	private static final int RECORD_REALIZE_PALETTE = 0x0035;
	private static final int RECORD_RECTANGLE = 0x041B;
	private static final int RECORD_RESIZE_PALETTE = 0x0139;
	private static final int RECORD_RESTORE_DC = 0x0127;
	private static final int RECORD_ROUND_RECT = 0x061C;
	private static final int RECORD_SAVE_DC = 0x001E;
	private static final int RECORD_SCALE_VIEWPORT_EXT_EX = 0x0412;
	private static final int RECORD_SCALE_WINDOW_EXT_EX = 0x0410;
	private static final int RECORD_SELECT_CLIP_RGN = 0x012C;
	private static final int RECORD_SELECT_OBJECT = 0x012D;
	private static final int RECORD_SELECT_PALETTE = 0x0234;
	private static final int RECORD_SET_BK_COLOR = 0x0201;
	private static final int RECORD_SET_BK_MODE = 0x0102;
	private static final int RECORD_SET_DIBITS_TO_DEVICE = 0x0D33;
	private static final int RECORD_SET_MAP_MODE = 0x0103;
	private static final int RECORD_SET_MAPPER_FLAGS = 0x0231;
	private static final int RECORD_SET_PALETTE_ENTRIES = 0x0037;
	private static final int RECORD_SET_PIXEL = 0x041F;
	private static final int RECORD_SET_POLY_FILL_MODE = 0x0106;
	private static final int RECORD_SET_REL_ABS = 0x0105;
	private static final int RECORD_SET_ROP2 = 0x0104;
	private static final int RECORD_SET_STRETCH_BLT_MODE = 0x0107;
	private static final int RECORD_SET_TEXT_ALIGN = 0x012E;
	private static final int RECORD_SET_TEXT_CHARACTER_EXTRA = 0x0108;
	private static final int RECORD_SET_TEXT_COLOR = 0x0209;
	private static final int RECORD_SET_TEXT_JUSTIFICATION = 0x020A;
	private static final int RECORD_SET_VIEWPORT_EXT_EX = 0x020E;
	private static final int RECORD_SET_VIEWPORT_ORG_EX = 0x020D;
	private static final int RECORD_SET_WINDOW_EXT_EX = 0x020C;
	private static final int RECORD_SET_WINDOW_ORG_EX = 0x020B;
	private static final int RECORD_STRETCH_BLT = 0x0B23;
	private static final int RECORD_STRETCH_DIBITS = 0x0F43;
	private static final int RECORD_TEXT_OUT = 0x0521;
	
	private static Logger log = Logger.getLogger(WmfParser.class.getName());
	
	public WmfParser() {
	}	
	
	public void parse(InputStream is, Gdi gdi)
		throws IOException, WmfParseException {
		DataInput in = null;
		boolean isEmpty = true;

		try {
			in = new DataInput(new BufferedInputStream(is), ByteOrder.LITTLE_ENDIAN);

			int mtType = 0;
			int mtHeaderSize = 0;
			
			long key = in.readUint32();
			isEmpty = false;
			if (key == 0x9AC6CDD7) {
				int hmf = in.readInt16();
				int vsx = in.readInt16();
				int vsy = in.readInt16();
				int vex = in.readInt16();
				int vey = in.readInt16();
				int dpi = in.readUint16();
				long reserved = in.readUint32();
				int checksum = in.readUint16();

				gdi.placeableHeader(vsx, vsy, vex, vey, dpi);
				
				mtType = in.readUint16();
				mtHeaderSize = in.readUint16();
			} else {
				mtType = (int)(key & 0x0000FFFF);
				mtHeaderSize = (int)((key & 0xFFFF0000) >> 16);
			}
			
			int mtVersion = in.readUint16();
			long mtSize = in.readUint32();
			int mtNoObjects = in.readUint16();
			long mtMaxRecord = in.readUint32();
			int mtNoParameters = in.readUint16();

			if (mtType != 1 || mtHeaderSize != 9) {
				throw new WmfParseException("invalid file format.");
			}
			
			gdi.header();

			GdiObject[] objs = new GdiObject[mtNoObjects];

			while (true) {
				int size = (int)in.readUint32() - 3;
				int id = in.readUint16();

				if (id == 0) {
					break; // Last record
				}

				in.setCount(0);

				switch (id) {
					case RECORD_ANIMATE_PALETTE :
						{
							//TODO
							log.fine("not implemented: AnimatePalette");
							gdi.animatePalette();
						}
						break;
					case RECORD_ARC :
						{
							int eya = in.readInt16();
							int exa = in.readInt16();
							int sya = in.readInt16();
							int sxa = in.readInt16();
							int eyr = in.readInt16();
							int exr = in.readInt16();
							int syr = in.readInt16();
							int sxr = in.readInt16();
							gdi.arc(sxr, syr, exr, eyr, sxa, sya, exa, eya);
						}
						break;
					case RECORD_BIT_BLT :
						{
							long rop = in.readUint32();
							int sy = in.readInt16();
							int sx = in.readInt16();
							int height = in.readInt16();
							int width = in.readInt16();
							int dy = in.readInt16();
							int dx = in.readInt16();
							
							byte[] image = in.readBytes(size * 2 - in.getCount());
							
							gdi.bitBlt(image, dx, dy, width, height, sx, sy, rop);
						}
						break;
					case RECORD_CHORD :
						{
							int eya = in.readInt16();
							int exa = in.readInt16();
							int sya = in.readInt16();
							int sxa = in.readInt16();
							int eyr = in.readInt16();
							int exr = in.readInt16();
							int syr = in.readInt16();
							int sxr = in.readInt16();
							gdi.chord(sxr, syr, exr, eyr, sxa, sya, exa, eya);
						}
						break;
					case RECORD_CREATE_BRUSH_INDIRECT :
						{
							int style = in.readUint16();
							int color = in.readInt32();
							int hatch = in.readUint16();
							for (int i = 0; i < objs.length; i++) {
								if (objs[i] == null) {
									objs[i] =
										gdi.createBrushIndirect(style, color, hatch);
									break;
								}
							}
						}
						break;
					case RECORD_CREATE_FONT_INDIRECT :
						{
							int height = in.readInt16();
							int width = in.readInt16();
							int escapement = in.readInt16();
							int orientation = in.readInt16();
							int weight = in.readInt16();
							boolean italic = (in.readByte() == 1);
							boolean underline = (in.readByte() == 1);
							boolean strikeout = (in.readByte() == 1);
							int charset = in.readByte();
							int outPrecision = in.readByte();
							int clipPrecision = in.readByte();
							int quality = in.readByte();
							int pitchAndFamily = in.readByte();
							int pitch = 0x00000003 & pitchAndFamily;
							int family = 0x000000F0 & pitchAndFamily;
							byte[] faceName = in.readBytes(2 * (size - 9));
							
							GdiObject obj = gdi.createFontIndirect(
									height,
									width,
									escapement,
									orientation,
									weight,
									italic,
									underline,
									strikeout,
									charset,
									outPrecision,
									clipPrecision,
									quality,
									pitchAndFamily,
									faceName);
							
							for (int i = 0; i < objs.length; i++) {
								if (objs[i] == null) {
									objs[i] = obj;
									break;
								}
							}
						}
						break;
					case RECORD_CREATE_PALETTE :
						{
							for (int i = 0; i < objs.length; i++) {
								if (objs[i] == null) {
									objs[i] = gdi.createPalette();
									break;
								}
							}
						}
						break;
					case RECORD_CREATE_PATTERN_BRUSH :
						{
							for (int i = 0; i < objs.length; i++) {
								if (objs[i] == null) {
									objs[i] = gdi.createPatternBrush();
									break;
								}
							}
						}
						break;
					case RECORD_CREATE_PEN_INDIRECT :
						{
							int style = in.readUint16();
							int width = in.readInt16();
							in.readInt16();
							int color = in.readInt32();
							for (int i = 0; i < objs.length; i++) {
								if (objs[i] == null) {
									objs[i] =
										gdi.createPenIndirect(style, width, color);
									break;
								}
							}
						}
						break;
					case RECORD_CREATE_RECT_RGN:
						{
							int ey = in.readInt16();
							int ex = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							for (int i = 0; i < objs.length; i++) {
								if (objs[i] == null) {
									objs[i] = gdi.createRectRgn(sx, sy, ex, ey);
									break;
								}
							}
							
						}
						break;
					case RECORD_DELETE_OBJECT :
						{
							int objID = in.readUint16();
							gdi.deleteObject(objs[objID]);
							objs[objID] = null;
						}
						break;
					case RECORD_DIB_BIT_BLT:
						{
							boolean isRop = false;
							
							long rop = in.readUint32();
							int sy = in.readInt16();
							int sx = in.readInt16();
							int height = in.readInt16();
							if (height == 0) {
								height = in.readInt16();
								isRop = true;
							}
							int width = in.readInt16();
							int dy = in.readInt16();
							int dx = in.readInt16();
							
							if (isRop) {
								gdi.dibBitBlt(null, dx, dy, width, height, sx, sy, rop);
							} else {
								byte[] image = in.readBytes(size * 2 - in.getCount());
								
								gdi.dibBitBlt(image, dx, dy, width, height, sx, sy, rop);
							}
						}
						break;
					case RECORD_DIB_CREATE_PATTERN_BRUSH :
						{
							int usage = in.readInt32();
							byte[] image =
								in.readBytes(size * 2 - in.getCount());
	
							for (int i = 0; i < objs.length; i++) {
								if (objs[i] == null) {
									objs[i] = gdi.dibCreatePatternBrush(image, usage);
									break;
								}
							}
						}
						break;
					case RECORD_DIB_STRETCH_BLT:
						{
							long rop = in.readUint32();
							int sh = in.readInt16();
							int sw = in.readInt16();
							int sx = in.readInt16();
							int sy = in.readInt16();
							int dh = in.readInt16();
							int dw = in.readInt16();
							int dy = in.readInt16();
							int dx = in.readInt16();
							
							byte[] image =
								in.readBytes(size * 2 - in.getCount());
							
							gdi.dibStretchBlt(image, dx, dy, dw, dh, sx, sy, sw, sh, rop);
						}
						break;
					case RECORD_ELLIPSE :
						{
							int ey = in.readInt16();
							int ex = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							gdi.ellipse(sx, sy, ex, ey);
						}
						break;
					case RECORD_ESCAPE :
						{
							byte[] data = in.readBytes(2 * size);
							gdi.escape(data);
						}
						break;
					case RECORD_EXCLUDE_CLIP_RECT :
						{
							int ey = in.readInt16();
							int ex = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							gdi.excludeClipRect(sx, sy, ex, ey);
						}
						break;
					case RECORD_EXT_FLOOD_FILL :
						{
							int type = in.readUint16();
							int color = in.readInt32();
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.extFloodFill(x, y, color, type);
						}
						break;
					case RECORD_EXT_TEXT_OUT :
						{
							int rsize = size;

							int y = in.readInt16();
							int x = in.readInt16();
							int count = in.readInt16();
							int options = in.readUint16();
							rsize -= 4;

							int[] rect = null;
							if ((options & 0x0006) > 0) {
								rect =
									new int[] {
										in.readInt16(),
										in.readInt16(),
										in.readInt16(),
										in.readInt16()};
								rsize -= 4;
							}
							byte[] text = in.readBytes(count);
							if (count % 2 == 1) {
								in.readByte();
							}
							rsize -= (count + 1) / 2;

							int[] dx = null;
							if (rsize > 0) {
								dx = new int[rsize];
								for (int i = 0; i < dx.length; i++) {
									dx[i] = in.readInt16();
								}
							}
							gdi.extTextOut(x, y, options, rect, text, dx);
						}
						break;
					case RECORD_FILL_RGN :
						{
							int brushID = in.readUint16();
							int rgnID = in.readUint16();
							gdi.fillRgn(objs[rgnID], objs[brushID]);
						}
						break;
					case RECORD_FLOOD_FILL :
						{
							int color = in.readInt32();
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.floodFill(x, y, color);
						}
						break;
					case RECORD_FRAME_RGN :
						{
							int height = in.readInt16();
							int width = in.readInt16();
							int brushID = in.readUint16();
							int rgnID = in.readUint16();
							gdi.frameRgn(objs[rgnID], objs[brushID], width, height);
						}
						break;
					case RECORD_INTERSECT_CLIP_RECT :
						{
							int ey = in.readInt16();
							int ex = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							gdi.intersectClipRect(sx, sy, ex, ey);
						}
						break;
					case RECORD_INVERT_RGN :
						{
							int rgnID = in.readUint16();
							gdi.invertRgn(objs[rgnID]);
						}
						break;
					case RECORD_LINE_TO :
						{
							int ey = in.readInt16();
							int ex = in.readInt16();
							gdi.lineTo(ex, ey);
						}
						break;
					case RECORD_MOVE_TO_EX :
						{
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.moveToEx(x, y, null);
						}
						break;
					case RECORD_OFFSET_CLIP_RGN :
						{
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.offsetClipRgn(x, y);
						}
						break;
					case RECORD_OFFSET_VIEWPORT_ORG_EX :
						{
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.offsetViewportOrgEx(x, y, null);
						}
						break;
					case RECORD_OFFSET_WINDOW_ORG_EX :
						{
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.offsetWindowOrgEx(x, y, null);
						}
						break;
					case RECORD_PAINT_RGN :
						{
							int objID = in.readUint16();
							gdi.paintRgn(objs[objID]);
						}
						break;
					case RECORD_PAT_BLT :
						{
							long rop = in.readUint32();
							int height = in.readInt16();
							int width = in.readInt16();
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.patBlt(x, y, width, height, rop);
						}
						break;
					case RECORD_PIE :
						{
							int eyr = in.readInt16();
							int exr = in.readInt16();
							int syr = in.readInt16();
							int sxr = in.readInt16();
							int ey = in.readInt16();
							int ex = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							gdi.pie(sx, sy, ex, ey, sxr, syr, exr, eyr);
						}
						break;
					case RECORD_POLYGON :
						{
							int count = in.readInt16();
							Point[] points = new Point[count];
							for (int i = 0; i < count; i++) {
								points[i] =
									new Point(in.readInt16(), in.readInt16());
							}
							gdi.polygon(points);
						}
						break;
					case RECORD_POLYLINE :
						{
							int count = in.readInt16();
							Point[] points = new Point[count];
							for (int i = 0; i < count; i++) {
								points[i] =
									new Point(in.readInt16(), in.readInt16());
							}
							gdi.polyline(points);
						}
						break;
					case RECORD_POLY_POLYGON :
						{
							Point[][] points = new Point[in.readInt16()][];
							for (int i = 0; i < points.length; i++) {
								points[i] = new Point[in.readInt16()];
							}
							for (int i = 0; i < points.length; i++) {
								for (int j = 0; j < points[i].length; j++) {
									points[i][j] =
										new Point(in.readInt16(), in.readInt16());
								}
							}
							gdi.polyPolygon(points);
						}
						break;
					case RECORD_REALIZE_PALETTE :
						{
							gdi.realizePalette();
						}
						break;
					case RECORD_RECTANGLE :
						{
							int ey = in.readInt16();
							int ex = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							gdi.rectangle(sx, sy, ex, ey);
						}
						break;
					case RECORD_RESIZE_PALETTE :
						{
							int objID = in.readUint16();
							gdi.resizePalette(objs[objID]);
						}
						break;
					case RECORD_RESTORE_DC :
						{
							gdi.restoreDC();
						}
						break;
					case RECORD_ROUND_RECT :
						{
							int rh = in.readInt16();
							int rw = in.readInt16();
							int ey = in.readInt16();
							int ex = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							gdi.roundRect(sx, sy, ex, ey, rw, rh);
						}
						break;
					case RECORD_SAVE_DC :
						{
							gdi.seveDC();
						}
						break;
					case RECORD_SCALE_VIEWPORT_EXT_EX :
						{
							int yd = in.readInt16();
							int y = in.readInt16();
							int xd = in.readInt16();
							int x = in.readInt16();
							gdi.scaleViewportExtEx(x, xd, y, yd, null);
						}
						break;
					case RECORD_SCALE_WINDOW_EXT_EX :
						{
							int yd = in.readInt16();
							int y = in.readInt16();
							int xd = in.readInt16();
							int x = in.readInt16();
							gdi.scaleWindowExtEx(x, xd, y, yd, null);
						}
						break;
					case RECORD_SELECT_CLIP_RGN :
						{
							int objID = in.readUint16();
							gdi.selectClipRgn(objs[objID]);
						}
						break;
					case RECORD_SELECT_OBJECT :
						{
							int objID = in.readUint16();
							gdi.selectObject(objs[objID]);
						}
						break;
					case RECORD_SELECT_PALETTE :
						{
							boolean mode = (in.readInt16() != -1);
							if ((size * 2 - in.getCount()) > 0) {
								int objID = in.readUint16();
								gdi.selectPalette(objs[objID], mode);
							}
						}
						break;
					case RECORD_SET_BK_COLOR :
						{
							int color = in.readInt32();
							gdi.setBkColor(color);
						}
						break;
					case RECORD_SET_BK_MODE :
						{
							int mode = in.readInt16();
							gdi.setBkMode(mode);
						}
						break;
					case RECORD_SET_DIBITS_TO_DEVICE :
						{
							int colorUse = in.readUint16();
							int scanlines = in.readUint16();
							int startscan = in.readUint16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							int dh = in.readInt16();
							int dw = in.readInt16();
							int dy = in.readInt16();
							int dx = in.readInt16();

							byte[] image =
								in.readBytes(size * 2 - in.getCount());
							
							gdi.setDIBitsToDevice(
								dx,
								dy,
								dw,
								dh,
								sx,
								sy,
								startscan,
								scanlines,
								image,
								colorUse);
						}
						break;
					case RECORD_SET_MAP_MODE :
						{
							int mode = in.readInt16();
							gdi.setMapMode(mode);
						}
						break;
					case RECORD_SET_MAPPER_FLAGS :
						{
							long flag = in.readUint32();
							gdi.setMapperFlags(flag);
						}
						break;
					case RECORD_SET_PALETTE_ENTRIES :
						{
							//TODO
							log.fine("not implemented: PaletteEntries");
							gdi.setPaletteEntries();
						}
						break;
					case RECORD_SET_PIXEL :
						{
							int color = in.readInt32();
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.setPixel(x, y, color);
						}
						break;
					case RECORD_SET_POLY_FILL_MODE :
						{
							int mode = in.readInt16();
							gdi.setPolyFillMode(mode);
						}
						break;
					case RECORD_SET_REL_ABS:
						{
							int mode = in.readInt16();
							gdi.setRelAbs(mode);
						}
						break;
					case RECORD_SET_ROP2 :
						{
							int mode = in.readInt16();
							gdi.setROP2(mode);
						}
						break;
					case RECORD_SET_STRETCH_BLT_MODE :
						{
							int mode = in.readInt16();
							gdi.setStretchBltMode(mode);
						}
						break;
					case RECORD_SET_TEXT_ALIGN :
						{
							int align = in.readInt16();
							gdi.setTextAlign(align);
						}
						break;
					case RECORD_SET_TEXT_CHARACTER_EXTRA :
						{
							int extra = in.readInt16();
							gdi.setTextCharacterExtra(extra);
						}
						break;
					case RECORD_SET_TEXT_COLOR :
						{
							int color = in.readInt32();
							gdi.setTextColor(color);
						}
						break;
					case RECORD_SET_TEXT_JUSTIFICATION :
						{
							int breakCount = in.readInt16();
							int breakExtra = in.readInt16();
							gdi.setTextJustification(breakExtra, breakCount);
						}
						break;
					case RECORD_SET_VIEWPORT_EXT_EX :
						{
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.setViewportExtEx(x, y, null);
						}
						break;
					case RECORD_SET_VIEWPORT_ORG_EX :
						{
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.setViewportOrgEx(x, y, null);
						}
						break;
					case RECORD_SET_WINDOW_EXT_EX :
						{
							int height = in.readInt16();
							int width = in.readInt16();
							gdi.setWindowExtEx(width, height, null);
						}
						break;
					case RECORD_SET_WINDOW_ORG_EX :
						{
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.setWindowOrgEx(x, y, null);
						}
						break;
					case RECORD_STRETCH_BLT :
						{
							long rop = in.readUint32();
							int sh = in.readInt16();
							int sw = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							int dh = in.readInt16();
							int dw = in.readInt16();
							int dy = in.readInt16();
							int dx = in.readInt16();
							
							byte[] image =
								in.readBytes(size * 2 - in.getCount());

							gdi.stretchBlt(image, dx, dy, dw, dh, sx, sy, sw, sh, rop);
						}
						break;
					case RECORD_STRETCH_DIBITS :
						{
							long rop = in.readUint32();
							int usage = in.readUint16();
							int sh = in.readInt16();
							int sw = in.readInt16();
							int sy = in.readInt16();
							int sx = in.readInt16();
							int dh = in.readInt16();
							int dw = in.readInt16();
							int dy = in.readInt16();
							int dx = in.readInt16();

							byte[] image =
								in.readBytes(size * 2 - in.getCount());

							gdi.stretchDIBits(dx, dy, dw, dh, sx, sy, sw, sh, image, usage, rop);
						}
						break;
					case RECORD_TEXT_OUT :
						{
							int count = in.readInt16();
							byte[] text = in.readBytes(count);
							if (count % 2 == 1) {
								in.readByte();
							}
							int y = in.readInt16();
							int x = in.readInt16();
							gdi.textOut(x, y, text);
						}
						break;
					default:
						{
							log.fine("unsuppored id find: " + id + " (size=" + size + ")");
						}
				}
				
				int rest = size * 2 - in.getCount();
				for (int i = 0; i < rest; i++) {
					in.readByte();
				}
			}
			in.close();

			gdi.footer();
		} catch (EOFException e) {
			if (isEmpty) throw new WmfParseException("input file size is zero.");
		}
	}
}
