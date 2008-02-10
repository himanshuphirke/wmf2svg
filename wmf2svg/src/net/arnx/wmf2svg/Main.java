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
package net.arnx.wmf2svg;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import net.arnx.wmf2svg.gdi.svg.*;
import net.arnx.wmf2svg.gdi.wmf.*;

/**
 * @author Hidekatsu Izuno
 */
public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			usage();
			System.exit(-1);
		}

		try {
			InputStream in = new FileInputStream(args[0]);
			WmfParser parser = new WmfParser();
			SvgGdi gdi = new SvgGdi();
			parser.parse(in, gdi);
		
			Document doc = gdi.getDocument();
			output(doc, new FileOutputStream(args[1]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void output(Document doc, OutputStream out)
		throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, 
				"-//W3C//DTD SVG 1.0//EN");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, 
				"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd");
		transformer.transform(new DOMSource(doc), new StreamResult(out));
		out.flush();
		out.close();
	}
	
	private static void usage() {
		System.out.println("java -jar wmf2svg.jar [wmf filename] [svg filename]");
	}
}
