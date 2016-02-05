package org.inria.websmatch.evaluate.oaei;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.inria.websmatch.utils.L;

import selector.ISimTableFilter;
import selector.MaxWeightAssignment;
import system.Configs;
import tools.Evaluation;
import yam.tools.WordNetHelper;
import datatypes.mapping.GMappingScore;
import datatypes.mapping.GMappingTable;

public class TestEvaluationHoaRes {

    /**
     * @param args
     */
    public static void main(String[] args) {

	// init WordNet
	yam.system.Configs.WNTMP = "WNTemplate.xml";
	yam.system.Configs.WNPROP = "file_properties.xml";
	/*
	 * yam.system.Configs.OAEI_ROOT =
	 * "/home/manu/workspace/OMSystem/data/ontology/newoaei/";
	 * yam.system.Configs.ORIGINAL =
	 * "/home/manu/workspace/OMSystem/data/ontology/newoaei/original.rdf";
	 */

	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}

	// we have to open an align file
	/*
	 * String index = "209"; String tech = "Proba_Max";
	 */

	String[] techs = new String[] { "StringMatcher[Levenshtein]","StringMatcher[SmithWaterman]","StringMatcher[JaroWinkler]"
		    ,"StringMatcher[Stoilos_JW]","StringMatcher[QGramsDistance]","StringMatcher[QGramsDistance]","StringMatcher[MongeElkan]",
		    "StringMatcher[MongeEklanStoilos]","StringMatcher[BagLinStoilois]","StringMatcher[BagWuPalmerStoilois]",
		    "LabelMatcher[MultiLevelMatcherL]","LabelMatcher[MultiLevelMatcherWP]","LabelMatcher[LinStoiloisBagForLabel]",
		    "LabelMatcher[WuPalmerBagForLabel]","SimpleProfileMatcher","IndividualProfileMatcher", "Proba_Max",
		"Proba_Prod" };
	String[] indexes = new String[] { "103", "104", "201", "201-2", "201-4", "201-6", "201-8", "202", "202-2", "202-4", "202-6", "202-8", "203", "204",
		"205", "206", "207", "208", "209", "210", "221", "222", "223", "224", "225", "228", "230", "231", "232", "233", "236", "237", "238", "239",
		"240", "241", "246", "247", "248", "248-2", "248-4", "248-6", "248-8", "249", "249-2", "249-4", "249-6", "249-8", "250", "250-2", "250-4",
		"250-6", "250-8", "251", "251-2", "251-4", "251-6", "251-8", "252", "252-2", "252-4", "252-6", "252-8", "253", "253-2", "253-4", "253-6",
		"253-8", "254", "254-2", "254-4", "254-6", "254-8", "257", "257-2", "257-4", "257-6", "257-8", "258", "258-2", "258-4", "258-6", "258-8",
		"259", "259-2", "259-4", "259-6", "259-8", "260", "260-2", "260-4", "260-6", "260-8", "261", "261-2", "261-4", "261-6", "261-8", "262",
		"262-2", "262-4", "262-6", "262-8", "265", "266", "301", "302", "303", "304" };
	
	/*String[] indexes = new String[] {"201", "201-2", "201-4", "201-6", "201-8", "202", "202-2", "202-4", "202-6", "202-8", "203", "204",
		"205", "206", "207", "208", "209", "210", "221", "222", "223", "224", "225", "228", "230", "231", "232", "233", "236", "237", "238", "239",
		"240", "241", "246", "247", "248", "248-2", "248-4", "248-6", "248-8", "249", "249-2", "249-4", "249-6", "249-8", "250", "250-2", "250-4",
		"250-6", "250-8", "251", "251-2", "251-4", "251-6", "251-8", "252", "252-2", "252-4", "252-6", "252-8", "253", "253-2", "253-4", "253-6",
		"253-8", "254", "254-2", "254-4", "254-6", "254-8", "257", "257-2", "257-4", "257-6", "257-8", "258", "258-2", "258-4", "258-6", "258-8",
		"259", "259-2", "259-4", "259-6", "259-8", "260", "260-2", "260-4", "260-6", "260-8", "261", "261-2", "261-4", "261-6", "261-8", "262",
		"262-2", "262-4", "262-6", "262-8", "265", "266"};*/
	
	// String[] indexes = new String[] {"301", "302", "303", "304" };
	
	//String[] indexes = new String[] { "103" , "104" , "201" };

	// db
	String dbuser = "matcher";
	String pass = "matcher";
	String dbHost = "localhost";
	int dbPort = 3306;
	String dbName = "matching_results";

	//
	java.sql.Connection conn = null;
	java.sql.Statement stat = null;

	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	} catch (InstantiationException e) {
	    L.Error(e.getMessage(),e);
	} catch (IllegalAccessException e) {
	    L.Error(e.getMessage(),e);
	} catch (ClassNotFoundException e) {
	    L.Error(e.getMessage(),e);
	}

	try {
	    conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName, dbuser, pass);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
	try {
	    stat = conn.createStatement();
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	TreeMap<String, SortedMap<String, double[]>> resToPrint = new TreeMap<String, SortedMap<String, double[]>>();
	
	// we need the doc ids
	// System.out.println(scenario.getOntoFN1());
	// System.out.println(scenario.getOntoFN2());

	// int limit = 400;
	int limit = 200;

	System.out.println("Limit : " + limit + "|");
	System.out.println("Source : 101|");
	//System.out.println("Fmeasure results|");
	System.out.println("Technique|");

	for (String index : indexes) {

	    SortedMap<String, double[]> resByTech = new TreeMap<String, double[]>();

	    for (String tech : techs) {

		// for each couple, get expert value
		String query = "SELECT File_Name_1, Entity1_URI, File_Name_2, Entity2_URI FROM test_results WHERE expert = '1' AND File_Name_1 ='"
			+ "101" + "' AND File_Name_2 = '" + index + "'";

		GMappingTable<String> expertMatrix = new GMappingTable<String>();

		try {

		    ResultSet res = stat.executeQuery(query);
		    while (res.next())
			expertMatrix.addMapping(new GMappingScore<String>(res.getString(2), res.getString(4), 1));

		} catch (SQLException e) {
		    L.Error(e.getMessage(),e);
		}

		// expertMatrix.printOut(true);

		// ok get the matching value for the couple on the last xp
		query = "SELECT File_Name_1, Entity1_URI, File_Name_2, Entity2_URI, `" + tech + "` FROM test_results WHERE File_Name_1 = '" + "101" + "' AND File_Name_2 = '" + index + "'"
			+ " ORDER BY `" + tech + "` DESC LIMIT " + limit;// +
								       // " AND "+tech+" > '0.661'";

		GMappingTable<String> scoresMatrix = new GMappingTable<String>();

		try {

		    ResultSet res = stat.executeQuery(query);
		    while (res.next())
			scoresMatrix.addMapping(new GMappingScore<String>(res.getString("Entity1_URI"), res.getString("Entity2_URI"), new Double(res.getDouble(tech))
				.floatValue()));

		} catch (SQLException e) {
		    L.Error(e.getMessage(),e);
		}

		// scoresMatrix.printOut(true);

		// now filter the matrix
		ISimTableFilter filter = new MaxWeightAssignment();

		GMappingTable<String> filteredMatrix = filter.select(scoresMatrix);

		// and now evaluate
		Evaluation<String> eval = new Evaluation<String>(expertMatrix, filteredMatrix);
		// Evaluation<String> eval = new Evaluation<String>(expertMatrix, scoresMatrix);
		
		double fmeas = eval.getFmeasure();
		if(Double.isNaN(fmeas)) fmeas = 0;
		
		resByTech.put(tech, new double[] { eval.getPrecision(), eval.getRecall(), fmeas });

		// System.out.println(tech +
		// " : |"+eval.getPrecision()+"|"+eval.getRecall()+"|"+eval.getFmeasure());

		/*
		 * System.out.println("Precision : " + eval.getPrecision());
		 * System.out.println("Recall : " + eval.getRecall());
		 * System.out.println("Fmeasure : " + eval.getFmeasure());
		 */

	    }

	    resToPrint.put(index, resByTech);

	}
	// close
	try {
	    stat.close();
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
	try {
	    if (conn != null)
		conn.close();
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// print out
	// precision
	Iterator<String> targets = resToPrint.keySet().iterator();
	while (targets.hasNext()) {

	    String target = targets.next();
	    //System.out.print(target + "||||");
	    System.out.print(target + "||");

	}

	System.out.println("");

	targets = resToPrint.keySet().iterator();

	String target = targets.next();

	SortedMap<String, double[]> scores = resToPrint.get(target);

	Iterator<String> itS = scores.keySet().iterator();

	while (itS.hasNext()) {

	    String key = itS.next();

	    //System.out.print(key + "|" + scores.get(key)[0] + "|"+scores.get(key)[1]+"|"+scores.get(key)[2]+"|");
	    System.out.print(key + "|"+(double)scores.get(key)[0]+"|");

	    while (targets.hasNext()) {
		target = targets.next();
		SortedMap<String, double[]> tmpscores = resToPrint.get(target);
		//System.out.print(key + "|" + tmpscores.get(key)[0] + "|"+tmpscores.get(key)[1]+"|"+tmpscores.get(key)[2]+"|");
		System.out.print(key + "|" +(double)tmpscores.get(key)[0]+"|");

	    }

	    System.out.println();
	    targets = resToPrint.keySet().iterator();
	    target = targets.next();
	    	    
	}
	
	// recall
	// print out
	targets = resToPrint.keySet().iterator();
	while (targets.hasNext()) {

	    target = targets.next();
	    //System.out.print(target + "||||");
	    System.out.print(target + "||");

	}

	System.out.println("");

	targets = resToPrint.keySet().iterator();

	target = targets.next();

	scores = resToPrint.get(target);

	itS = scores.keySet().iterator();

	while (itS.hasNext()) {

	    String key = itS.next();

	    //System.out.print(key + "|" + scores.get(key)[0] + "|"+scores.get(key)[1]+"|"+scores.get(key)[2]+"|");
	    System.out.print(key + "|"+(double)scores.get(key)[1]+"|");

	    while (targets.hasNext()) {
		target = targets.next();
		SortedMap<String, double[]> tmpscores = resToPrint.get(target);
		//System.out.print(key + "|" + tmpscores.get(key)[0] + "|"+tmpscores.get(key)[1]+"|"+tmpscores.get(key)[2]+"|");
		System.out.print(key + "|" +(double)tmpscores.get(key)[1]+"|");

	    }

	    System.out.println();
	    targets = resToPrint.keySet().iterator();
	    target = targets.next();
	    	    
	}
	
	// fmeasure
	// print out
	targets = resToPrint.keySet().iterator();
	while (targets.hasNext()) {

	    target = targets.next();
	    //System.out.print(target + "||||");
	    System.out.print(target + "||");

	}

	System.out.println("");

	targets = resToPrint.keySet().iterator();

	target = targets.next();

	scores = resToPrint.get(target);

	itS = scores.keySet().iterator();

	while (itS.hasNext()) {

	    String key = itS.next();

	    //System.out.print(key + "|" + scores.get(key)[0] + "|"+scores.get(key)[1]+"|"+scores.get(key)[2]+"|");
	    System.out.print(key + "|"+(double)scores.get(key)[2]+"|");

	    while (targets.hasNext()) {
		target = targets.next();
		SortedMap<String, double[]> tmpscores = resToPrint.get(target);
		//System.out.print(key + "|" + tmpscores.get(key)[0] + "|"+tmpscores.get(key)[1]+"|"+tmpscores.get(key)[2]+"|");
		System.out.print(key + "|" +(double)tmpscores.get(key)[2]+"|");

	    }

	    System.out.println();
	    targets = resToPrint.keySet().iterator();
	    target = targets.next();
	    	    
	}
	
    }
}
