package net.arnx.wmf2svg;

import junit.framework.TestCase;
import java.io.*;

public class MainTest extends TestCase {
	/*
	 * TestCase for 'net.arnx.wmf2svg.Main.main(String[])'
	 */
	public void testMain() {
		File dir = new File("./data");
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".wmf");
			}
			
		});
		
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getAbsolutePath();
			name = name.substring(0, name.length() - 4);
			Main.main(new String[] {name + ".wmf", name + ".svg"});
		}
	}
}
