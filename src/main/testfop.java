package main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.SAXException;

public class testfop {
	public static void main(String[] args) throws SAXException, IOException, TransformerException {
		// Step 1: Construct a FopFactory by specifying a reference to the configuration file
		// (reuse if you plan to render multiple documents!)
		FopFactory fopFactory = FopFactory.newInstance(new File("F:/code/libs/fop-2.1/conf/fop.xconf"));
		
		// Step 2: Set up output stream.
		// Note: Using BufferedOutputStream for performance reasons (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File("src/output/test.pdf")));
		
		// Step 3: Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

	    // Step 4: Setup JAXP using identity transformerx
	    TransformerFactory factory = TransformerFactory.newInstance();
	    Transformer transformer = factory.newTransformer(); // identity transformer

	    // Step 5: Setup input and output for XSLT transformation
	    // Setup input stream
	    Source src = new StreamSource(new File("F:/code/libs/fop-2.1/examples/fo/basic/textdeko.fo"));

	    // Resulting SAX events (the generated FO) must be piped through to FOP
	    Result res = new SAXResult(fop.getDefaultHandler());

	    // Step 6: Start XSLT transformation and FOP processing
	    transformer.transform(src, res);

	    //Clean-up
	    out.close();
	}	
}
