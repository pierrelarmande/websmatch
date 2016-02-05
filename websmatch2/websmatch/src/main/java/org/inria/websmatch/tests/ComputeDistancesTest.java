package org.inria.websmatch.tests;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.matchers.base.DocumentMatcher;
import org.inria.websmatch.utils.L;

import system.Configs;
import tools.wordnet.WordNetHelper;

public class ComputeDistancesTest {
    
    private static String login = "conceptTest";//"datapublica";
    //private static String login = "gamma";//"datapublica";

    /**
     * @param args
     */
    public static void main(String[] args) {

	System.out.println("Begin : " + new Date());

	// init
	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	} catch (Exception e) {
	    L.Error(e.getMessage(), e);
	}
	try {
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	}
	//

	// load files and match them
	MongoDBConnector connector = MongoDBConnector.getInstance();

	List<SchemaData> list = connector.getSchemas(false, login);

	SchemaData[] schemas = list.toArray(new SchemaData[list.size()]);
	Map<String, Boolean> used = new HashMap<String, Boolean>();
	int cpt = 0;

	while (true) {

	    SchemaData s1 = schemas[(int) (Math.random() * (schemas.length - 0)) + 0];
	    SchemaData s2 = schemas[(int) (Math.random() * (schemas.length - 0)) + 0];

	    if (!s1.getId().equals(s2.getId()) && used.get(s1.getId() + s2.getId()) == null && used.get(s2.getId() + s1.getId()) == null) {
		String strDoc1 = connector.getEditedXML(s1.getId(), login);
		String strDoc2 = connector.getEditedXML(s2.getId(), login);

		DocumentMatcher dMatcher = new DocumentMatcher();
		if (strDoc1 != null && strDoc2 != null && !strDoc1.equals("") && !strDoc2.equals("")) {
		    try {
			float dist = dMatcher.computeDistance(dMatcher.matchDocuments(strDoc1, strDoc2));

			// insert distance
			if (!Float.isNaN(dist) && dist > 0)
			    connector.insertOrUpdateDistance(s1.getId(), s1.getSource(), s2.getId(), s2.getSource(), dist, "", login);
			/*else
			    connector.insertOrUpdateDistance(s1.getId(), s2.getId(), 0, "", "datapublica");*/
		    } catch (Exception e) {
			L.Error(e.getMessage(),e);
		    }
		}

		used.put(s1.getId() + s2.getId(), true);

		cpt++;
		if (cpt % 500 == 0)
		    System.out.println(new Date() + " Cpt insert dist : " + cpt);

	    }

	}

	// ok we need to use some random instead of consecutive loop
	/*
	 * Map<SchemaData,Map<SchemaData,Boolean>> random = new
	 * HashMap<SchemaData,Map<SchemaData,Boolean>>();
	 * 
	 * for (int i = 0; i < schemas.length - 1; i++) {
	 * Map<SchemaData,Boolean> secondMap = new
	 * HashMap<SchemaData,Boolean>(); for (int j = i + 1; j <
	 * schemas.length; j++) { secondMap.put(schemas[j], false); }
	 * random.put(schemas[i], secondMap); } //
	 * 
	 * int cpt = 0;
	 * 
	 * Set<SchemaData> keys = random.keySet(); SchemaData[] keysArray =
	 * keys.toArray(new SchemaData[keys.size()]);
	 * 
	 * while(true){
	 * 
	 * SchemaData key = keysArray[(int)(Math.random() * (keys.size()-0)) +
	 * 0]; Map<SchemaData,Boolean> secondMap = random.get(key);
	 * 
	 * Set<SchemaData> secondKeys = secondMap.keySet(); SchemaData[]
	 * secondKeysArray = secondKeys.toArray(new
	 * SchemaData[secondKeys.size()]); SchemaData secondKey =
	 * secondKeysArray[(int)(Math.random() * (secondKeys.size()-0)) + 0];
	 * Boolean value = secondMap.get(secondKey);
	 * 
	 * if(!value){
	 * 
	 * String strDoc1 = connector.getEditedXML(key.getId(), "datapublica");
	 * String strDoc2 = connector.getEditedXML(secondKey.getId(),
	 * "datapublica");
	 * 
	 * DocumentMatcher dMatcher = new DocumentMatcher(); if (strDoc1 != null
	 * && strDoc2 != null && !strDoc1.equals("") && !strDoc2.equals("")) {
	 * try{ float dist =
	 * dMatcher.computeDistance(dMatcher.matchDocuments(strDoc1, strDoc2));
	 * 
	 * // insert distance if (!Float.isNaN(dist))
	 * connector.insertOrUpdateDistance(key.getId(), secondKey.getId(),
	 * dist, "", "datapublica"); else
	 * connector.insertOrUpdateDistance(key.getId(), secondKey.getId(), 0,
	 * "", "datapublica"); }catch(Exception e){
	 * 
	 * } }
	 * 
	 * random.get(key).put(secondKey, true);
	 * 
	 * // cpt++; if (cpt % 500 == 0) System.out.println(new Date() +
	 * " Cpt insert dist : " + cpt);
	 * 
	 * } }
	 */

	/*
	 * for (int i = 0; i < schemas.length - 1; i++) {
	 * 
	 * // doc 201-0.xls String strDoc1 =
	 * connector.getEditedXML(schemas[i].getId(), "datapublica");
	 * 
	 * for (int j = i + 1; j < schemas.length; j++) { // doc 202-0.xls
	 * String strDoc2 = connector.getEditedXML(schemas[j].getId(),
	 * "datapublica"); //
	 * 
	 * DocumentMatcher dMatcher = new DocumentMatcher(); if (strDoc1 != null
	 * && strDoc2 != null && !strDoc1.equals("") && !strDoc2.equals("")) {
	 * try{ float dist =
	 * dMatcher.computeDistance(dMatcher.matchDocuments(strDoc1, strDoc2));
	 * 
	 * // insert distance if (!Float.isNaN(dist))
	 * connector.insertOrUpdateDistance(schemas[i].getId(),
	 * schemas[j].getId(), dist, "", "datapublica"); else
	 * connector.insertOrUpdateDistance(schemas[i].getId(),
	 * schemas[j].getId(), 0, "", "datapublica"); }catch(Exception e){
	 * 
	 * } }
	 * 
	 * // cpt++; if (cpt % 500 == 0) System.out.println("Cpt insert dist : "
	 * + cpt); } }
	 */

	// System.out.println("End : " + new Date());
    }
}
