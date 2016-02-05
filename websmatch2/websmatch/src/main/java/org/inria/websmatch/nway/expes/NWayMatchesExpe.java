package org.inria.websmatch.nway.expes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.server.SpreadsheetParsingServiceImpl;
import org.inria.websmatch.gwt.spreadsheet.server.XMLStorageServiceImpl;
import org.inria.websmatch.matchers.base.DocumentMatcher;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.FileUtils;

import system.Configs;
import tools.wordnet.WordNetHelper;

public class NWayMatchesExpe {

    // used account for testing
    private String accountUsed;
    public static FileWriter TTLandKWriter = null;
    public static boolean shuffled = false;
    public static FileWriter writer;
    
    static // min/max time for importing a doc
    long minImportTime = 0;
    static long maxImportTime = 0;

    public NWayMatchesExpe(String account) {
	this.setAccountUsed(account);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

	long[] ttls = { /*1000, 2000, 3000, 4000,*/ 5000 };
	
	File dir = new File("/home/manu/Documents/TestsDataSets");
	File[] files = dir.listFiles();
	Arrays.sort(files);
	
	// randomize
	if(!NWayMatchesExpe.shuffled){
	    NWayMatchesExpe.shuffled = true;
	    files = FileUtils.randomizeFiles(files);
	}

	generateExpes(true, -1, ttls, files);
    }

    public static File[] generateExpes(boolean useNWay, int k, long[] ttls, File[] files) {
	
	for (int i = 0; i < ttls.length; i++) {
	    
	    // clean values
	    MongoDBConnector.minMatchTime = 0;
	    MongoDBConnector.maxMatchTime = 0;
	    minImportTime = 0;
	    maxImportTime = 0;
	    MongoDBConnector.totalMatchesFound = 0;
	    MongoDBConnector.totalMatchTime = 0;
	    MongoDBConnector.storeIdMatch = true;
	    //
	    
	    System.out.println("----------");
	    System.out.println("Running with ttl : "+ttls[i]+" ms");
	    
	    long startTime = System.currentTimeMillis();

	    NWayMatchesExpe nwme = new NWayMatchesExpe("NWayMatchesExpeTTL"+ttls[i]);

	    // import documents
	    // compute nway matches and distances
	    files = nwme.importFilesAndCompute(useNWay, k, ttls[i], files);

	    // nwme.computeDistances();

	    // print results
	    // nwme.printResult();
	 	    
	    System.out.println("Min match time : "+MongoDBConnector.minMatchTime+" ms");
	    System.out.println("Max match time : "+MongoDBConnector.maxMatchTime+" ms");
	    System.out.println("Total match time : "+ (MongoDBConnector.totalMatchTime/1000) + " s");
	    System.out.println("Min import time : "+minImportTime+" ms");
	    System.out.println("Max import time : "+maxImportTime+" ms");
	    System.out.println("Total running time : " + (System.currentTimeMillis() - startTime) / 1000 + " s");
	    System.out.println("Total matches found : "+MongoDBConnector.totalMatchesFound);
	    System.out.println("----------");	    
	}
	
	    return files;

    }

    public File[] importFilesAndCompute(boolean useNWay, int k, long ttl, File[] files) {

	MongoDBConnector connector = MongoDBConnector.getInstance();
	connector.getMongo().dropDatabase(getAccountUsed());

	MongoDBConnector.useNWay = useNWay; // true;
	// MongoDBConnector.k = 5;
	// if true and k = -1, use time
	MongoDBConnector.k = k; // -1;
	MongoDBConnector.ttl = ttl; // 2000;
	MongoDBConnector.totalMatchTime = 0;

	// output for expe
	String ttlAndKFile = "ttl" + MongoDBConnector.ttl + "AndK.csv";
	File outputTTLandKFile = new File(ttlAndKFile);
	try {
	    TTLandKWriter = new FileWriter(outputTTLandKFile);
	    TTLandKWriter.write("\"TTL\";\"K\";\"id\";\"C1\";\"C2\"\n");
	    TTLandKWriter.flush();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	//

	// first import the docs
	SpreadsheetParsingServiceImpl service = new SpreadsheetParsingServiceImpl();
	// for testing on concept
	service.setBaseXLSDir("/home/manu/Documents/TestsDataSets");
	service.setStoreService("http://localhost:8080/SchemaStore/services/SchemaStore");

	XMLStorageServiceImpl storage = new XMLStorageServiceImpl();
	// for testing on concept
	storage.setBaseXLSDir("/home/manu/Documents/TestsDataSets");
	storage.setMongo(MongoDBConnector.getInstance());

	// time evolution for matching in a file
	File output = new File(this.getAccountUsed() + "_matching_time.csv");
	if (output.exists())
	    output.delete();

	try {
	    output.createNewFile();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

	writer = null;

	try {
	    writer = new FileWriter(output);

	    writer.write("\"NbConceptsInDoc\";\"NbConceptsInDB\";\"NbMatches\";\"Filename\";\"Time (in seconds)\"\n");
	    writer.flush();
	} catch (IOException e) {
	    L.Error(e.getMessage(), e);
	}
	
	System.out.println("Begin import : " + new Date());
	
	for (int cpt = 0; cpt < files.length; cpt++) {
	    MongoDBConnector.ttl = ttl;
	    if (cpt % 100 == 0) {
		L.Debug(this, "Import : " + cpt, true);
	    }
	    if (files[cpt].getName().endsWith("xls")) {
		
		long startTime = System.currentTimeMillis();
		
		// System.out.println(files[cpt].getName());
		L.Debug(this, "Importing : " + files[cpt].getName(), true);
		try {

		    // for testing on concepts
		    SimpleSheet[] sheets = service.parseSpreadsheet(this.getAccountUsed(), files[cpt].getName(), true, new Integer(cpt + 1).toString());

		    // for testing on concepts
		    // if useNWay is true on MongoDBConnector, will match k
		    // concept (5 by default)
		    /* String docId = */storage.importDocument(sheets, sheets, files[cpt].getName(), files[cpt].getName(), this.getAccountUsed(), "",
			    this.getAccountUsed(), "", new Integer(cpt + 1).toString(), false, "", false, false, "", false);

		} catch (Exception e) {
		    L.Error(e.getMessage(),e);
		}
		// if neeeded to delete
		// files[cpt].delete();
		
		long matchingTime = System.currentTimeMillis() - startTime;
		if(minImportTime == 0) minImportTime = matchingTime;
		if(matchingTime < minImportTime) minImportTime = matchingTime;
		if(matchingTime > maxImportTime) maxImportTime = matchingTime;
	    }
	}
	
	if (writer != null) {
	    try {
		writer.close();
	    } catch (IOException e) {
		L.Error(e.getMessage(),e);
	    }
	}

	System.out.println("End import : " + new Date());
	return files;
    }

    /**
     * We want to print results in CSV, order by proba prod desc filename 1 -
     * node name 1 - filename 2 - node name 2 - proba prod
     * 
     * Second time, distances desc filename 1 - filename 2 - distance
     */

    public void printResult() {
	MongoDBConnector connector = MongoDBConnector.getInstance();
	HashMap<String[], Double> distances = connector.getDistances(this.getAccountUsed());

	File output = new File(this.getAccountUsed() + "_dist.csv");
	if (output.exists())
	    output.delete();

	try {
	    output.createNewFile();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

	try {
	    FileWriter writer = new FileWriter(output);

	    writer.write("\"Filename1\";\"Filename2\";dist\n");
	    writer.flush();

	    Set<String[]> keys = distances.keySet();
	    for (String[] k : keys) {
		writer.write("\"" + k[1] + "\";\"" + k[3] + "\";" + distances.get(k) + "\n");
		writer.flush();
	    }

	    writer.close();
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}

    }

    public void computeDistances() {

	System.out.println("Begin : " + new Date());

	// init
	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	}
	try {
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	}
	//

	// load files and match them
	MongoDBConnector connector = MongoDBConnector.getInstance();

	List<SchemaData> list = connector.getSchemas(false, this.getAccountUsed());

	SchemaData[] schemas = list.toArray(new SchemaData[list.size()]);
	Map<String, Boolean> used = new HashMap<String, Boolean>();
	int cpt = 0;

	// while (true) {
	for(int i = 0; i < schemas.length - 1; i++){
	    for(int j = i + 1; j < schemas.length; j++){

	    //SchemaData s1 = schemas[(int) (Math.random() * (schemas.length - 0)) + 0];
	    //SchemaData s2 = schemas[(int) (Math.random() * (schemas.length - 0)) + 0];
		SchemaData s1 = schemas[i];
		SchemaData s2 = schemas[j];

	    if (!s1.getId().equals(s2.getId()) && used.get(s1.getId() + s2.getId()) == null && used.get(s2.getId() + s1.getId()) == null) {
		String strDoc1 = connector.getEditedXML(s1.getId(), this.getAccountUsed());
		String strDoc2 = connector.getEditedXML(s2.getId(), this.getAccountUsed());

		DocumentMatcher dMatcher = new DocumentMatcher();
		if (strDoc1 != null && strDoc2 != null && !strDoc1.equals("") && !strDoc2.equals("")) {
		    try {
			float dist = dMatcher.computeDistance(s1.getId(), s2.getId(), this.getAccountUsed());//dMatcher.computeDistance(dMatcher.matchDocuments(strDoc1, strDoc2));

			// insert distance
			if (!Float.isNaN(dist) && dist > 0)
			    connector.insertOrUpdateDistance(s1.getId(), s1.getSource(), s2.getId(), s2.getSource(), dist, "", this.getAccountUsed());
			/*
			 * else connector.insertOrUpdateDistance(s1.getId(),
			 * s2.getId(), 0, "", "datapublica");
			 */
		    } catch (Exception e) {

		    }
		}

		used.put(s1.getId() + s2.getId(), true);

		cpt++;
		if (cpt % 500 == 0)
		    System.out.println(new Date() + " Cpt insert dist : " + cpt);

	    }
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

    public void setAccountUsed(String accountUsed) {
	this.accountUsed = accountUsed;
    }

    public String getAccountUsed() {
	return accountUsed;
    }

}
