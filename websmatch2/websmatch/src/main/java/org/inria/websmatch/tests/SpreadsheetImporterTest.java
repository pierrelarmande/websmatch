package org.inria.websmatch.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.inria.websmatch.utils.L;
import org.junit.Test;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.schemaImporters.SpreadsheetImporter;

public class SpreadsheetImporterTest { // todo: move to test package? (if so, set junit maven scope to test)

    @Test
    public void testSpreadsheetImporter() {
	//Test
	SpreadsheetImporter test = new SpreadsheetImporter();
	try {
		//test.importSchema("Test", "Test", "Test", new URI("file:/home/manu/Téléchargements/XLS/ORT_route.xls"));	
		ArrayList<SchemaElement> res = test.getSchemaElements(new URI("file:/home/manu/Téléchargements/XLS/ORT_route.xls"));
		ListIterator<SchemaElement> it = res.listIterator();
		SchemaElement se = null;
		while(it.hasNext()){
			
			se = (SchemaElement) it.next();
			
			System.out.println("Name : "+se.getName()+"\tDescription : "+se.getDescription());
			
		}
	} catch (ImporterException e) {
		// TODO Auto-generated catch block
		L.Error(e.getMessage(),e);
	} catch (URISyntaxException e) {
		// TODO Auto-generated catch block
		L.Error(e.getMessage(),e);
	}
    }

}
