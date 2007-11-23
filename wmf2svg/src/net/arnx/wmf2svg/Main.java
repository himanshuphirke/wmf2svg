package net.arnx.wmf2svg;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import net.arnx.wmf2svg.gdi.svg.*;
import net.arnx.wmf2svg.gdi.wmf.*;

public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("error");
			System.exit(0);
		}

		SvgGdi gdi = null;
		try {
			InputStream in = new FileInputStream(args[0]);
			WmfParser parser = new WmfParser();
			gdi = new SvgGdi();
			parser.parse(in, gdi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Document doc = gdi.getDocument();
		try {
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
}
