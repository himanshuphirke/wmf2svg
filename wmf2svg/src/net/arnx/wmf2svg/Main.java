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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import net.arnx.wmf2svg.gdi.Gdi;
import net.arnx.wmf2svg.gdi.svg.*;
import net.arnx.wmf2svg.gdi.wmf.*;

/**
 * @author Hidekatsu Izuno
 */
public class Main {
	private static Logger log = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
		String src = null;
		String dest = null;
		
		boolean debug = false;
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				if (args[i].equals("-debug")) {
					debug = true;
				} else {
					usage();
					return;
				}
			} else if (i == args.length-2) {
				src = args[i];
			} else if (i == args.length-1) {
				dest = args[i];
			}
		}
		
		if (src == null || dest == null) {
			usage();
			return;
		}

		try {
			InputStream in = new FileInputStream(src);
			WmfParser parser = new WmfParser();
			final SvgGdi gdi = new SvgGdi();
			if (debug) {
				ClassLoader cl = gdi.getClass().getClassLoader();
				Class[] interfaces = new Class[] { Gdi.class };
				parser.parse(in, (Gdi)Proxy.newProxyInstance(cl, interfaces, new InvocationHandler() {
					StringBuffer sb = new StringBuffer(1000);
					
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						sb.setLength(0);
						sb.append(method.getName()).append("(");
						if (args != null) {
							for (int i = 0; i < args.length; i++) {
								if (i > 0) sb.append(", ");
								if (args[i] instanceof int[]) {
									int[] array = (int[])args[i];
									sb.append("[");
									for (int j = 0; j < array.length; j++) {
										if (j > 0) sb.append(", ");
										sb.append(array[j]);
									}
									sb.append("]");
								} else if (args[i] instanceof byte[]) {
									byte[] array = (byte[])args[i];
									sb.append("[");
									for (int j = 0; j < array.length; j++) {
										if (j > 0) sb.append(", ");
										sb.append(Integer.toHexString(array[j]));
									}
									sb.append("]");
								} else if (args[i] instanceof double[]) {
									double[] array = (double[])args[i];
									sb.append("[");
									for (int j = 0; j < array.length; j++) {
										if (j > 0) sb.append(", ");
										sb.append(array[j]);
									}
									sb.append("]");
								} else if (args[i] instanceof Object[]) {
									Object[] array = (Object[])args[i];
									sb.append("[");
									for (int j = 0; j < array.length; j++) {
										if (j > 0) sb.append(", ");
										sb.append(array[j]);
									}
									sb.append("]");
								} else {
									sb.append(args[i]);
								}
							}
						}
						sb.append(")");
						log.fine(sb.toString());
						return method.invoke(gdi, args);
					}
				}));
			} else {
				parser.parse(in, gdi);
			}
		
			Document doc = gdi.getDocument();
			OutputStream out = new FileOutputStream(dest);
			if (args[1].endsWith(".svgz")) {
				out = new GZIPOutputStream(out);
			}
			
			output(doc, out);
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
		System.out.println("java -jar wmf2svg.jar [wmf filename] [svg filename(svg, xml, or .svgz)]");
		System.exit(-1);
	}
}