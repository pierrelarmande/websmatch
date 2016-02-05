package org.inria.websmatch.tests;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.xml.rpc.ServiceException;

import org.inria.websmatch.utils.L;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.SchemaElementList;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.schemaImporters.IOWLImporter;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

public class IOWLImporterTest {

    /**
     * @param args
     */
    public static void main(String[] args) {

	File ontoDrawer = new File("/home/manu/workspace/WebSmatch/data/ontology/newoaei");
	System.out.println(ontoDrawer.exists());
	String[] paths = ontoDrawer.list();

	for (String path : paths) {

	    path = "/home/manu/workspace/WebSmatch/data/ontology/newoaei/" + path;

	    System.out.println(path);

	    File dir = new File(path);
	    String fileName = dir.getName() + ".rdf";

	    System.out.println(fileName);

	    if (dir.isDirectory() && !fileName.equals("253-6.rdf") && !fileName.equals("262.rdf") && !fileName.equals("201-8.rdf") && !fileName.equals("101.rdf") && !fileName.equals("103.rdf") && !fileName.equals("208.rdf")
		    && !fileName.equals("209.rdf") && !fileName.equals("202.rdf") && !fileName.equals(".svn.rdf")) {

		IOWLImporter owlImp = new IOWLImporter();

		String suri = new String(("file:" + dir.getAbsolutePath() + "/" + fileName).replaceAll("\\s", "%20"));
		ArrayList<SchemaElement> res = new ArrayList<SchemaElement>();

		try {
		    try {
			res = owlImp.getSchemaElements(new URI(suri));
		    } catch (URISyntaxException e) {
			L.Error(e.getMessage(), e);
		    }
		} catch (ImporterException e) {
		    L.Error(e.getMessage(),e);
		}

		// store it in WS
		Schema sch = new Schema();
		sch.setName(fileName.substring(0, fileName.lastIndexOf('.')));
		sch.setAuthor("oeai");
		sch.setSource(fileName);
		sch.setDescription("");
		sch.setType(owlImp.getName());
		/**
		 * @todo Get the last inserted id?
		 */
		sch.setId(IOWLImporter.nextId());

		SchemaElementList list = new SchemaElementList();
		SchemaElement[] sea = new SchemaElement[res.size()];

		for (int i = 0; i < res.size(); i++) {
		    sea[i] = res.get(i);
		}

		list = new SchemaElementList(sea);

		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

		try {
		    SchemaStoreObject sc;
		    try {
			sc = serviceLoc.getSchemaStore(new URL("http://constraint.lirmm.fr/SchemaStore/services/SchemaStore"));

			try {
			    System.out.println(sc.importSchema(sch, list));

			} catch (RemoteException e) {

			    L.Error(e.getMessage(),e);
			}

		    } catch (MalformedURLException e1) {
			e1.printStackTrace();
		    }

		} catch (ServiceException e) {

		    L.Error(e.getMessage(),e);
		}
	    }
	}
    }
}
