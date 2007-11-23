package net.arnx.wmf2svg.gdi;

public interface GdiBrush extends GdiObject {
	public static final int BS_DIBPATTERN = 5;
	public static final int BS_DIBPATTERN8X8 = 8;
	public static final int BS_DIBPATTERNPT = 6;
	public static final int BS_HATCHED = 2;
	public static final int BS_HOLLOW = 1;
	public static final int BS_NULL = 1;
	public static final int BS_PATTERN = 3;
	public static final int BS_PATTERN8X8 = 7;
	public static final int BS_SOLID = 0;

	public static final int DIB_PAL_COLORS = 1;
	public static final int DIB_RGB_COLORS = 0;

	public static final int HS_HORIZONTAL = 0;
	public static final int HS_VERTICAL = 1;
	public static final int HS_FDIAGONAL = 2;
	public static final int HS_BDIAGONAL = 3;
	public static final int HS_CROSS = 4;
	public static final int HS_DIAGCROSS = 5;
}
