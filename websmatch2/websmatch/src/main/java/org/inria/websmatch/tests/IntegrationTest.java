package org.inria.websmatch.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.inria.websmatch.dspl.DSPLExport;
import org.inria.websmatch.dspl.integration.Integration;
import org.inria.websmatch.utils.L;

public class IntegrationTest {

    public static void main(String[] args) {

	Integration integration = new Integration("89137", "98799","demo");
	String document = integration.getIntegratedFile();

	File file = new File("/var/www/xls/generatedXML/testInteg.xml");
	try {
	    if (file.exists())
		file.delete();
	    file.createNewFile();
	    FileWriter output = new FileWriter(file);
	    BufferedWriter writer = new BufferedWriter(output);
	    writer.write(document);
	    writer.flush();
	    writer.close();
	  
	    DSPLExport exporter = new DSPLExport(file.getName(), "test", "test", "fr");
	    exporter.dsplGenerate(document);

	    // zip
	    System.out.println(exporter.zipFiles());

	} catch (IOException e) {
	    L.Error(e.getMessage(), e);
	}
    }
}
