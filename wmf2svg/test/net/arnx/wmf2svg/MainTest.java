package net.arnx.wmf2svg;

import junit.framework.TestCase;
import java.io.*;

public class MainTest extends TestCase {
	/*
	 * TestCase for 'net.arnx.wmf2svg.Main.main(String[])'
	 */
	public void testMain() {
		System.setProperty("java.util.logging.config.file", "./logging.properties");
		
		File dir = new File(System.getProperty("user.home") + "/My Documents/wmf2svg");
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith("Circles.wmf");
			}
			
		});
		
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getAbsolutePath();
			name = name.substring(0, name.length() - 4);
			System.out.println(name + " transforming...");
			Main.main(new String[] {"-debug", name + ".wmf", name + ".svg"});
		}
	}
}
