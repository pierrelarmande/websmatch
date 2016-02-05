package org.inria.websmatch.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.xml.rpc.ServiceException;

import org.inria.websmatch.utils.L;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.SchemaElementList;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.schemaImporters.OsmozeImporter;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

public class OsmozeImporterTest {

    public static void main(String args[]) {
	
	OsmozeImporter osImp = new OsmozeImporter();
	String fileName = "UV1-B - University of Vilnius Bachelor - 1.xml";

	String suri = new String(("file:/home/manu/Bureau/OSMOZE xmls/"+fileName).replaceAll("\\s", "%20"));
	ArrayList<SchemaElement> res = new ArrayList<SchemaElement>();

	try {
	    try {
		res = osImp.getSchemaElements(new URI(suri));

		/*ListIterator<SchemaElement> it = res.listIterator();
		while (it.hasNext()) {
		    System.out.println("SchemaElement : " + it.next());
		}*/

	    } catch (URISyntaxException e) {
		L.Error(e.getMessage(), e);
	    }
	} catch (ImporterException e) {
	    L.Error(e.getMessage(),e);
	}

	// store it in WS
	Schema sch = new Schema();
	sch.setName(fileName);
	sch.setAuthor("riga");
	sch.setSource(fileName);
	sch.setDescription("Test on Osmoze XMLs.");
	sch.setType("Osmoze XML schema");
	/**
	 * @todo Get the last inserted id?
	 */
	sch.setId(OsmozeImporter.nextId());

	SchemaElementList list = new SchemaElementList();
	SchemaElement[] sea = new SchemaElement[res.size()];

	for (int i = 0; i < res.size(); i++) {
	    sea[i] = res.get(i);
	}

	list = new SchemaElementList(sea);

	// well first we remove relations for storing first the entities
	/*Relationship[] relations = list.getRelationships();
	list.setRelationships(new Relationship[0]);*/
	
	SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();
	
	try {
	    SchemaStoreObject sc = serviceLoc.getSchemaStore();
	    try {
		sc.importSchema(sch, list);
			
		// now we try with rel...
		/*list.setRelationships(relations);
		sc.importSchema(sch, list);*/
		
	    } catch (RemoteException e) {

		L.Error(e.getMessage(),e);
	    }

	} catch (ServiceException e) {

	    L.Error(e.getMessage(),e);
	}

    }

}
