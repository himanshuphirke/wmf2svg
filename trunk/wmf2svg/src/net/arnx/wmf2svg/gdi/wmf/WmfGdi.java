package net.arnx.wmf2svg.gdi.wmf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.arnx.wmf2svg.gdi.Gdi;
import net.arnx.wmf2svg.gdi.GdiBrush;
import net.arnx.wmf2svg.gdi.GdiFont;
import net.arnx.wmf2svg.gdi.GdiObject;
import net.arnx.wmf2svg.gdi.GdiPalette;
import net.arnx.wmf2svg.gdi.GdiPen;
import net.arnx.wmf2svg.gdi.GdiRegion;
import net.arnx.wmf2svg.gdi.Point;
import net.arnx.wmf2svg.gdi.Size;

public class WmfGdi implements Gdi, WmfConstants {
	private byte[] placeableHeader;
	private byte[] header;

	private List objects = new ArrayList();
	private List records = new ArrayList();
	
	public WmfGdi() {
	}
	
	public void write(OutputStream out) throws IOException {
		footer();
		if (placeableHeader != null) out.write(placeableHeader);
		if (header != null) out.write(header);
		
		Iterator i = records.iterator();
		while (i.hasNext()) {
			out.write((byte[])i.next());
		}
	}
	
	public void placeableHeader(int vsx, int vsy, int vex, int vey, int dpi) {
		byte[] record = new byte[22];
		int pos = 0;
		pos = setUint32(record, pos, 0x9AC6CDD7);
		pos = setInt16(record, pos, 0x0000);
		pos = setInt16(record, pos, vsx);
		pos = setInt16(record, pos, vsy);
		pos = setInt16(record, pos, vex);
		pos = setInt16(record, pos, vey);
		pos = setUint16(record, pos, dpi);
		pos = setUint32(record, pos, 0x00000000);
		
		int checksum = 0;
		for (int i = 0; i < record.length-2; i+=2) {
			checksum ^= (0xFF & record[i]) | ((0xFF & record[i+1]) << 8);
		}
		
		pos = setUint16(record, pos, checksum);
		placeableHeader = record;
	}

	public void header() {
		byte[] record = new byte[18];
		int pos = 0;
		pos = setUint16(record, pos, 0x0001);
		pos = setUint16(record, pos, 0x0009);
		pos = setUint16(record, pos, 0x0300);
		pos = setUint32(record, pos, 0x0000); // dummy size
		pos = setUint16(record, pos, 0x0000); // dummy noObjects
		pos = setUint32(record, pos, 0x0000); // dummy maxRecords
		pos = setUint16(record, pos, 0x0000);
		header = record;
	}

	public void animatePalette(GdiPalette palette, int startIndex,
			int entryCount, byte[] entries) {
		// TODO Auto-generated method stub

	}

	public void arc(int sxr, int syr, int exr, int eyr, int sxa, int sya,
			int exa, int eya) {
		// TODO Auto-generated method stub

	}

	public void bitBlt(byte[] bits, int dx, int dy, int width, int height,
			int sx, int sy, long rop) {
		// TODO Auto-generated method stub

	}

	public void chord(int sxr, int syr, int exr, int eyr, int sxa, int sya,
			int exa, int eya) {
		// TODO Auto-generated method stub

	}

	public GdiBrush createBrushIndirect(int style, int color, int hatch) {
		byte[] record = new byte[14];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_CREATE_BRUSH_INDIRECT);
		pos = setUint16(record, pos, style);
		pos = setInt32(record, pos, color);
		pos = setUint16(record, pos, hatch);
		records.add(record);
		
		WmfGdiBrush brush = new WmfGdiBrush(objects.size());
		objects.add(brush);
		return brush;
	}

	public GdiFont createFontIndirect(int height, int width, int escapement,
			int orientation, int weight, boolean italic, boolean underline,
			boolean strikeout, int charset, int outPrecision,
			int clipPrecision, int quality, int pitchAndFamily, byte[] faceName) {

		byte[] record = new byte[24 + faceName.length];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_CREATE_FONT_INDIRECT);
		pos = setInt16(record, pos, height);
		pos = setInt16(record, pos, width);
		pos = setInt16(record, pos, escapement);
		pos = setInt16(record, pos, orientation);
		pos = setInt16(record, pos, weight);
		pos = setByte(record, pos, (italic) ? 0x01 : 0x00);
		pos = setByte(record, pos, (underline) ? 0x01 : 0x00);
		pos = setByte(record, pos, (strikeout) ? 0x01 : 0x00);
		pos = setByte(record, pos, charset);
		pos = setByte(record, pos, outPrecision);
		pos = setByte(record, pos, clipPrecision);
		pos = setByte(record, pos, quality);
		pos = setByte(record, pos, pitchAndFamily);
		pos = setBytes(record, pos, faceName);
		records.add(record);
		
		WmfGdiFont font = new WmfGdiFont(objects.size());
		objects.add(font);
		return font;
	}

	public GdiPalette createPalette() {
		// TODO Auto-generated method stub
		return null;
	}

	public GdiBrush createPatternBrush(byte[] image) {
		// TODO Auto-generated method stub
		return null;
	}

	public GdiPen createPenIndirect(int style, int width, int color) {
		// TODO Auto-generated method stub
		return null;
	}

	public GdiRegion createRectRgn(int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteObject(GdiObject obj) {
		// TODO Auto-generated method stub

	}

	public void dibBitBlt(byte[] image, int dx, int dy, int dw, int dh, int sx,
			int sy, long rop) {
		// TODO Auto-generated method stub

	}

	public GdiBrush dibCreatePatternBrush(byte[] image, int usage) {
		// TODO Auto-generated method stub
		return null;
	}

	public void dibStretchBlt(byte[] image, int dx, int dy, int dw, int dh,
			int sx, int sy, int sw, int sh, long rop) {
		// TODO Auto-generated method stub

	}

	public void ellipse(int sx, int sy, int ex, int ey) {
		byte[] record = new byte[14];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_ELLIPSE);
		pos = setInt16(record, pos, ey);
		pos = setInt16(record, pos, ex);
		pos = setInt16(record, pos, sy);
		pos = setInt16(record, pos, sx);
		records.add(record);
	}

	public void escape(byte[] data) {
		// TODO Auto-generated method stub

	}

	public int excludeClipRect(int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void extFloodFill(int x, int y, int color, int type) {
		// TODO Auto-generated method stub

	}

	public void extTextOut(int x, int y, int options, int[] rect, byte[] text, int[] lpdx) {
		// TODO Auto-generated method stub

	}

	public void fillRgn(GdiRegion rgn, GdiBrush brush) {
		// TODO Auto-generated method stub

	}

	public void floodFill(int x, int y, int color) {
		// TODO Auto-generated method stub

	}

	public void frameRgn(GdiRegion rgn, GdiBrush brush, int w, int h) {
		// TODO Auto-generated method stub

	}

	public void intersectClipRect(int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub

	}

	public void invertRgn(GdiRegion rgn) {
		// TODO Auto-generated method stub

	}

	public void lineTo(int ex, int ey) {
		byte[] record = new byte[10];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_LINE_TO);
		pos = setInt16(record, pos, ey);
		pos = setInt16(record, pos, ex);
		records.add(record);
	}

	public void moveToEx(int x, int y, Point old) {
		byte[] record = new byte[10];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_MOVE_TO_EX);
		pos = setInt16(record, pos, y);
		pos = setInt16(record, pos, x);
		records.add(record);
	}

	public void offsetClipRgn(int x, int y) {
		// TODO Auto-generated method stub

	}

	public void offsetViewportOrgEx(int x, int y, Point point) {
		// TODO Auto-generated method stub

	}

	public void offsetWindowOrgEx(int x, int y, Point point) {
		// TODO Auto-generated method stub

	}

	public void paintRgn(GdiRegion rgn) {
		// TODO Auto-generated method stub

	}

	public void patBlt(int x, int y, int width, int height, long rop) {
		// TODO Auto-generated method stub

	}

	public void pie(int sx, int sy, int ex, int ey, int sxr, int syr, int exr,
			int eyr) {
		// TODO Auto-generated method stub

	}

	public void polygon(Point[] points) {
		// TODO Auto-generated method stub

	}

	public void polyline(Point[] points) {
		// TODO Auto-generated method stub

	}

	public void polyPolygon(Point[][] points) {
		// TODO Auto-generated method stub

	}

	public void realizePalette() {
		// TODO Auto-generated method stub

	}

	public void restoreDC() {
		// TODO Auto-generated method stub

	}

	public void rectangle(int sx, int sy, int ex, int ey) {
		byte[] record = new byte[14];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_RECTANGLE);
		pos = setInt16(record, pos, ey);
		pos = setInt16(record, pos, ex);
		pos = setInt16(record, pos, sy);
		pos = setInt16(record, pos, sx);
		records.add(record);
	}

	public void resizePalette(GdiPalette palette) {
		// TODO Auto-generated method stub

	}

	public void roundRect(int sx, int sy, int ex, int ey, int rw, int rh) {
		// TODO Auto-generated method stub

	}

	public void seveDC() {
		// TODO Auto-generated method stub

	}

	public void scaleViewportExtEx(int x, int xd, int y, int yd, Size old) {
		// TODO Auto-generated method stub

	}

	public void scaleWindowExtEx(int x, int xd, int y, int yd, Size old) {
		// TODO Auto-generated method stub

	}

	public void selectClipRgn(GdiRegion rgn) {
		// TODO Auto-generated method stub

	}

	public void selectObject(GdiObject obj) {
		byte[] record = new byte[8];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_SELECT_OBJECT);
		pos = setUint16(record, pos, ((WmfGdiObject)obj).getID());
		records.add(record);
	}

	public void selectPalette(GdiPalette palette, boolean mode) {
		// TODO Auto-generated method stub

	}

	public void setBkColor(int color) {
		byte[] record = new byte[10];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_SET_BK_COLOR);
		pos = setInt32(record, pos, color);
		records.add(record);
	}

	public void setBkMode(int mode) {
		byte[] record = new byte[10];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_SET_BK_MODE);
		pos = setInt16(record, pos, mode);
		records.add(record);
	}

	public void setDIBitsToDevice(int dx, int dy, int dw, int dh, int sx,
			int sy, int startscan, int scanlines, byte[] image, int colorUse) {
		// TODO Auto-generated method stub

	}

	public void setLayout(long layout) {
		// TODO Auto-generated method stub

	}

	public void setMapMode(int mode) {
		// TODO Auto-generated method stub

	}

	public void setMapperFlags(long flags) {
		// TODO Auto-generated method stub

	}

	public void setPaletteEntries(GdiPalette palette, int startIndex,
			int entryCount, byte[] entries) {
		// TODO Auto-generated method stub

	}

	public void setPixel(int x, int y, int color) {
		// TODO Auto-generated method stub

	}

	public void setPolyFillMode(int mode) {
		// TODO Auto-generated method stub

	}

	public void setRelAbs(int mode) {
		// TODO Auto-generated method stub

	}

	public void setROP2(int mode) {
		// TODO Auto-generated method stub

	}

	public void setStretchBltMode(int mode) {
		// TODO Auto-generated method stub

	}

	public void setTextAlign(int align) {
		// TODO Auto-generated method stub

	}

	public void setTextCharacterExtra(int extra) {
		// TODO Auto-generated method stub

	}

	public void setTextColor(int color) {
		// TODO Auto-generated method stub

	}

	public void setTextJustification(int breakExtra, int breakCount) {
		// TODO Auto-generated method stub

	}

	public void setViewportExtEx(int x, int y, Size old) {
		// TODO Auto-generated method stub

	}

	public void setViewportOrgEx(int x, int y, Point old) {
		// TODO Auto-generated method stub

	}

	public void setWindowExtEx(int width, int height, Size old) {
		byte[] record = new byte[10];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_SET_WINDOW_EXT_EX);
		pos = setInt16(record, pos, height);
		pos = setInt16(record, pos, width);
		records.add(record);
	}

	public void setWindowOrgEx(int x, int y, Point old) {
		byte[] record = new byte[10];
		int pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, RECORD_SET_WINDOW_ORG_EX);
		pos = setInt16(record, pos, y);
		pos = setInt16(record, pos, x);
		records.add(record);
	}

	public void stretchBlt(byte[] image, int dx, int dy, int dw, int dh,
			int sx, int sy, int sw, int sh, long rop) {
		// TODO Auto-generated method stub

	}

	public void stretchDIBits(int dx, int dy, int dw, int dh, int sx, int sy,
			int sw, int sh, byte[] image, int usage, long rop) {
		// TODO Auto-generated method stub

	}

	public void textOut(int x, int y, byte[] text) {
		// TODO Auto-generated method stub

	}

	public void footer() {
		int pos = 0;
		if (header != null) {
			long size = header.length;
			long maxRecordSize = 0;
			Iterator i = records.iterator();
			while (i.hasNext()) {
				byte[] record = (byte[])i.next();
				size += record.length;
				if (record.length > maxRecordSize) maxRecordSize = record.length;
			}
			
			pos = setUint32(header, 6, size/2);
			pos = setUint16(header, pos, objects.size());
			pos = setUint32(header, pos, maxRecordSize / 2);
		}
		
		byte[] record = new byte[6];
		pos = 0;
		pos = setUint32(record, pos, record.length/2);
		pos = setUint16(record, pos, 0x0000);
		records.add(record);
	}
	
	private int setByte(byte[] out, int pos, int value) {
		out[pos] = (byte)(0xFF & value);
		return pos + 1;
	}
	
	private int setBytes(byte[] out, int pos, byte[] data) {
		System.arraycopy(data, 0, out, pos, data.length);
		return pos + data.length;
	}
	
	private int setInt16(byte[] out, int pos, int value) {
		out[pos] = (byte)(0xFF & value);
		out[pos+1] = (byte)(0xFF & (value >> 8));
		return pos + 2;
	}
	
	private int setInt32(byte[] out, int pos, int value) {
		out[pos] = (byte)(0xFF & value);
		out[pos+1] = (byte)(0xFF & (value >> 8));
		out[pos+2] = (byte)(0xFF & (value >> 16));
		out[pos+3] = (byte)(0xFF & (value >> 24));
		return pos + 4;
	}
	
	private int setUint16(byte[] out, int pos, int value) {
		out[pos] = (byte)(0xFF & value);
		out[pos+1] = (byte)(0xFF & (value >> 8));
		return pos + 2;
	}
	
	private int setUint32(byte[] out, int pos, long value) {
		out[pos] = (byte)(0xFF & value);
		out[pos+1] = (byte)(0xFF & (value >> 8));
		out[pos+2] = (byte)(0xFF & (value >> 16));
		out[pos+3] = (byte)(0xFF & (value >> 24));
		return pos + 4;
	}
}
