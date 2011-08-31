package net.arnx.wmf2svg;

import junit.framework.TestCase;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
				return file.getName().endsWith("abcdef.wmf");
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
		BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();
		
		List list = new ArrayList();
		list.add(new Font("Arial", Font.PLAIN, 72000));
		list.add(new Font("Courier New", Font.PLAIN, 72000));
		list.add(new Font("Lucida Console", Font.PLAIN, 72000));
		
		for (int i = 0; i < list.size(); i++) {
			FontMetrics font = g.getFontMetrics((Font)list.get(i));
			System.out.println("name: " +  font.getFont().getName() 
					+ ", leading: " + font.getLeading() 
					+ ", height: " + font.getHeight()
					+ ", ratio: " + ((double)font.getLeading() / font.getHeight()));
		}
	}
}
