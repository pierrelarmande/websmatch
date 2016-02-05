package org.inria.websmatch.tests;

import java.util.HashMap;
import java.util.List;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.xml.WSMatchXMLDiff;

public class WSMatchXMLDiffTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
	
	MongoDBConnector mongo = MongoDBConnector.getInstance();

	List<SchemaData> schemas = mongo.getSchemas(true, "datapublica");

	// now for each schema get the diff edited vs auto
	for (SchemaData schema : schemas) {

	    String auto_xml = mongo.getAutoXML(schema.getId(), "datapublica");
	    String edit_xml = mongo.getEditedXML(schema.getId(), "datapublica");

	    WSMatchXMLDiff diff = new WSMatchXMLDiff();
	    HashMap<ConnexComposant, HashMap<String, Integer>> res = diff.getDiff(auto_xml, edit_xml);

	    for (ConnexComposant cc : res.keySet()) {
		System.out.println("Schema : " + schema.getName() + "\tCC : " + cc.toString() + "\tAuto : " + res.get(cc).get("auto") + "\tEdit : "
			+ res.get(cc).get("edit") + "\tIntersect : " + res.get(cc).get("intersect"));
	    }
	}
	
    }
}
