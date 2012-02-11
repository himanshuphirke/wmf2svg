package net.arnx.wmf2svg;

import junit.framework.TestCase;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainTest extends TestCase {
	/*
	 * TestCase for 'net.arnx.wmf2svg.Main.main(String[])'
	 */
	public void testMain() {
		System.setProperty("java.util.logging.config.file", "./logging.properties");
		
		File dir = new File(System.getProperty("user.home") + "/My Documents/wmf2svg");
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".wmf");
			}
		});
		
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getAbsolutePath();
			name = name.substring(0, name.length() - 4);
			System.out.println(name + " transforming...");
			Main.main(new String[] {"-debug", name + ".wmf", name + ".svg"});
		}
	}
	
	public void testFontInternalLeadings() {
		Frame f = new Frame();
		f.setVisible(true);
		
		Graphics2D g = (Graphics2D)f.getGraphics();
		
		List list = new ArrayList();
		list.add(new Font("Arial", Font.PLAIN, 72000));
		list.add(new Font("Courier New", Font.PLAIN, 72000));
		list.add(new Font("Lucida Console", Font.PLAIN, 72000));
		
		for (int i = 0; i < list.size(); i++) {
			Font font = (Font)list.get(i);
			LineMetrics m = font.getLineMetrics("ABCdefg", g.getFontRenderContext());
			
			System.out.println("name: " + font.getName() 
					+ ", leading: " + m.getLeading() 
					+ ", height: " + m.getHeight()
					+ ", ratio: " + ((double)m.getLeading() / m.getHeight()));
		}
	}
}
