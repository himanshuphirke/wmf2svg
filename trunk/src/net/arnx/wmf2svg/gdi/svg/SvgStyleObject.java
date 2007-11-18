package net.arnx.wmf2svg.gdi.svg;

abstract class SvgStyleObject {
	private SvgGdi gdi;

	public SvgStyleObject(SvgGdi gdi) {
		this.gdi = gdi;
	}

	public SvgGdi getGDI() {
		return gdi;
	}

	public int toRealSize(int px) {
		return getGDI().getDC().getDpi() * px / 90;
	}

	public static String toColor(int color) {
		int b = (0x00FF0000 & color) >> 16;
		int g = (0x0000FF00 & color) >> 8;
		int r = (0x000000FF & color);

		return "rgb(" + r + "," + g + "," + b + ")";
	}
}
