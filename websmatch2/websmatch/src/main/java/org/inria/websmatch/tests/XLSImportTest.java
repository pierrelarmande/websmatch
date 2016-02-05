package org.inria.websmatch.tests;

import java.io.File;
import java.util.Date;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.server.SpreadsheetParsingServiceImpl;
import org.inria.websmatch.gwt.spreadsheet.server.XMLStorageServiceImpl;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.server.SpreadsheetParsingServiceImpl;
import org.inria.websmatch.gwt.spreadsheet.server.XMLStorageServiceImpl;
import org.inria.websmatch.utils.L;

public class XLSImportTest {

    /**
     * @param args
     */
    public static void main(String[] args) {

	SpreadsheetParsingServiceImpl service = new SpreadsheetParsingServiceImpl();
	//service.setBaseXLSDir("/home/manu/Documents/dp-excel");
	// for testing on concept
	service.setBaseXLSDir("/home/manu/Documents/TestsDataSets");
	service.setStoreService("http://localhost:8080/SchemaStore/services/SchemaStore");

	XMLStorageServiceImpl storage = new XMLStorageServiceImpl();
	// storage.setBaseXLSDir("/home/manu/Documents/dp-excel-demo");
	// for testing on concept
	storage.setBaseXLSDir("/home/manu/Documents/TestsDataSets");
	storage.setMongo(MongoDBConnector.getInstance());

	/*
	 * try { SimpleSheet[] sheets = service.parseSpreadsheet("remi",
	 * "15-0.xls", true, "15");
	 * 
	 * // criters String criters = new String(); for (int s = 0; s <
	 * sheets.length; s++) { List<String> crits = sheets[s].getCriters();
	 * criters += "{"; for (String c : crits) criters += c; criters += "}";
	 * }
	 * 
	 * storage.importDocument(sheets, null, "15", "15-0.xls", "remi", "",
	 * "test", "", "15", criters, false, ""); } catch (ParserException e) {
	 * L.Error(e.getMessage(),e); }
	 */

	System.out.println("Begin import : "+new Date());
	
	// File dir = new File("/home/manu/Documents/dp-excel-demo");
	// for testing concept
	File dir = new File("/home/manu/Documents/ConceptTestsDataSets");
	File[] files = dir.listFiles();

	for (int cpt = 0; cpt < files.length; cpt++) {
	    if(cpt % 100 == 0){
		System.out.println("Import : "+cpt);
	    }
	    if (files[cpt].getName().endsWith("xls")) {
		System.out.println("Importing : "+files[cpt].getName());
		try{
		//SimpleSheet[] sheets = service.parseSpreadsheet("datapublica", files[cpt].getName(), true, files[cpt].getName().split("-")[0]);
		// for testing on concepts
		SimpleSheet[] sheets = service.parseSpreadsheet("conceptTest", files[cpt].getName(), true, files[cpt].getName().split("-")[0]);
				
		// criters
		/*String criters = new String();
		for (int s = 0; s < sheets.length; s++) {
		List<String> crits = sheets[s].getCriters();
		criters += "{";
		for (String c : crits)
		    criters += c;
		criters += "}";
		}*/

		/*System.out.println("Id : "+storage.importDocument(sheets, null, files[cpt].getName().split("-")[0], files[cpt].getName(), "datapublica", "", "datapublica", "", files[cpt].getName()
		    .split("-")[0], false, "",false,false,"",false));*/
		// for testing on concepts
		System.out.println("Id : "+storage.importDocument(sheets, sheets, files[cpt].getName().split("-")[0], files[cpt].getName(), "conceptTest", "", "conceptTest", "", files[cpt].getName()
			    .split("-")[0], false, "",false,false,"",false));
		}catch(Exception e){
			L.Error(e.getMessage(), e);
		}
		//files[cpt].delete();
	    }
	}
	
	System.out.println("End import : "+new Date());

    }

}
