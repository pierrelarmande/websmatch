package org.inria.websmatch.machineLearning;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.rpc.ServiceException;

import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.L;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.schemaImporters.SpreadsheetImporter;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

public class Main {
    
    private static String baseXLSDir = "/tmp";
    private static String storeSerivce = "http://localhost:8080/SchemaStore/services/SchemaStore";

    /**
     * @param args
     */
    public static void main(String[] args) {
	
	List<SchemaData> res = new ArrayList<SchemaData>();

	SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();
	try {
	    SchemaStoreObject sc = serviceLoc.getSchemaStore(new URL(
		    storeSerivce));

	    Schema[] sch = sc.getSchemas();

	    for (int i = 0; i < sch.length; i++) {
		res.add(new SchemaData(sch[i].getName(),
			    sch[i].getSource(), sch[i].getAuthor(), sch[i]
				    .getDescription(), sch[i].getId().toString()));
		
	    }

	} catch (ServiceException e) {
	    L.Error(e.getMessage(), e);
	} catch (RemoteException e) {
	    L.Error(e.getMessage(),e);
	} catch (MalformedURLException e) {
	    L.Error(e.getMessage(),e);
	}

	ListIterator<SchemaData> it = res.listIterator();
	
	SpreadsheetImporter xlsImporter = new SpreadsheetImporter();
	
	while(it.hasNext()){
	
	try {
	    
	    SchemaData data = it.next();
	    
	    xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:"+baseXLSDir+"/" + data.getSource().replaceAll("\\s", "%20")));
	    MLInstanceCompute fact = new MLInstanceCompute(data.getSource().replaceAll("\\s", "%20"),data.getAuthor(),xlsImporter);
	    fact.insertDatas(true);
	
	    /*xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:/tmp/" + "ClubsDPT01-2.xls".replaceAll("\\s", "%20")));
	    MachineLearningFactory fact = new MachineLearningFactory("ClubsDPT01-2.xls".replaceAll("\\s", "%20"),"manu",xlsImporter);
	    fact.insertDatas();
	    
	    xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:/tmp/" + "immensn_cle2fa25b.xls".replaceAll("\\s", "%20")));
	    fact = new MachineLearningFactory("immensn_cle2fa25b.xls".replaceAll("\\s", "%20"),"manu",xlsImporter);
	    fact.insertDatas();
	    
	    xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:/tmp/" + "0IF3_cle5cd453-4.xls".replaceAll("\\s", "%20")));
	    fact = new MachineLearningFactory("0IF3_cle5cd453-4.xls".replaceAll("\\s", "%20"),"manu",xlsImporter);
	    fact.insertDatas();
	    
	    xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:/tmp/" + "2Bilan_comparatif_2000-2008_cle2996b2.xls".replaceAll("\\s", "%20")));
	    fact = new MachineLearningFactory("2Bilan_comparatif_2000-2008_cle2996b2.xls".replaceAll("\\s", "%20"),"manu",xlsImporter);
	    fact.insertDatas();
	    
	    xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:/tmp/" + "Revalorisation_retraites.xls".replaceAll("\\s", "%20")));
	    fact = new MachineLearningFactory("Revalorisation_retraites.xls".replaceAll("\\s", "%20"),"manu",xlsImporter);
	    fact.insertDatas();
	    
	    xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:/tmp/" + "RFL_C_11_75851.xls".replaceAll("\\s", "%20")));
	    fact = new MachineLearningFactory("RFL_C_11_75851.xls".replaceAll("\\s", "%20"),"manu",xlsImporter);
	    fact.insertDatas();
	    
	    xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:/tmp/" + "Minimum_vieillesse.xls".replaceAll("\\s", "%20")));
	    fact = new MachineLearningFactory("Minimum_vieillesse.xls".replaceAll("\\s", "%20"),"manu",xlsImporter);
	    fact.insertDatas();
	    
	    xlsImporter = new SpreadsheetImporter();
	    xlsImporter.getSchemaElements(new URI("file:/tmp/" + "08_21_Valeur_venale_des_terres_cle02eb21.xls".replaceAll("\\s", "%20")));
	    fact = new MachineLearningFactory("08_21_Valeur_venale_des_terres_cle02eb21.xls".replaceAll("\\s", "%20"),"manu",xlsImporter);
	    fact.insertDatas();*/
	    
	} catch (ImporterException e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	} catch (URISyntaxException e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}

	}

    }

}
