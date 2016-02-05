package org.inria.websmatch.gwt.spreadsheet.server;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.inria.websmatch.db.MySQLDBConfLoader;
import org.inria.websmatch.gwt.spreadsheet.client.EvaluationService;
import org.inria.websmatch.gwt.spreadsheet.client.listeners.MatchingProgressEvent;
import org.inria.websmatch.utils.L;

import org.inria.websmatch.gwt.spreadsheet.client.EvaluationService;
import selector.ISimTableFilter;
import selector.MaxWeightAssignment;
import tools.Evaluation;
import yam.system.Configs;
import yam.tools.WordNetHelper;
import datatypes.mapping.GMappingScore;
import datatypes.mapping.GMappingTable;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.RemoteEventServiceServlet;

public class EvaluationServiceImpl extends RemoteEventServiceServlet implements EvaluationService {

    /**
     * 
     */
    private static final long serialVersionUID = -5436246057935043682L;

    final String[] techs = new String[] { "Stoilos_JW", "Levenshtein", "SmithWaterman", "SmithWatermanGotoh", "SmithWatermanGotohWindowedAffine", "Jaro",
	    "JaroWinkler", "QGramsDistance", "MongeElkan", "WuPalmer", "Lin", "MultiLevelMatcher", "SoftTFIDF", "SoftTFIDFWordNet", "Proba_Max", "Proba_Prod" };

    final String[] indexes = new String[] { "103", "104", "201", "201-2", "201-4", "201-6", "201-8", "202", "202-2", "202-4", "202-6", "202-8", "203", "204",
	    "205", "206", "207", "208", "209", "210", "221", "222", "223", "224", "225", "228", "230", "231", "232", "233", "236", "237", "238", "239", "240",
	    "241", "246", "247", "248", "248-2", "248-4", "248-6", "248-8", "249", "249-2", "249-4", "249-6", "249-8", "250", "250-2", "250-4", "250-6",
	    "250-8", "251", "251-2", "251-4", "251-6", "251-8", "252", "252-2", "252-4", "252-6", "252-8", "253", "253-2", "253-4", "253-6", "253-8", "254",
	    "254-2", "254-4", "254-6", "254-8", "257", "257-2", "257-4", "257-6", "257-8", "258", "258-2", "258-4", "258-6", "258-8", "259", "259-2", "259-4",
	    "259-6", "259-8", "260", "260-2", "260-4", "260-6", "260-8", "261", "261-2", "261-4", "261-6", "261-8", "262", "262-2", "262-4", "262-6", "262-8",
	    "265", "266", "301", "302", "303", "304" };

    public void init() {

	// init WordNet
	yam.system.Configs.WNTMP = "WNTemplate.xml";
	yam.system.Configs.WNPROP = "file_properties.xml";

	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}
    }

    @Override
    public ArrayList<ArrayList<String>> getEvaluationResults(boolean byTech, String choosenTech, boolean byDoc, String targetDoc) {

	ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

	// db
	String dbuser;// = "matcher";
	String pass;// = "matcher";
	// Inria String dbHost = "localhost";
	String dbHost;// = "193.49.106.32";
	int dbPort;// = 3306;
	String dbName;// = "matching_results";

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
	    
	    // load conf
	    MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

	    dbuser = loader.getDbuser();
	    pass = loader.getPass();
	    dbHost = loader.getDbHost();
	    dbPort = loader.getDbPort();
	    dbName = loader.getDbName();
	    
	    conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName, dbuser, pass);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
	try {
	    stat = conn.createStatement();
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	int leftId = -1;
	int rightId = -1;

	int limit = 400;

	//
	Domain dom = DomainFactory.getDomain(this.getThreadLocalRequest().getSession().getId());
	MatchingProgressEvent event = new MatchingProgressEvent();
	//

	if (byTech)
	    event.setMaxMatch(indexes.length);
	if (byDoc)
	    event.setMaxMatch(techs.length);

	int cpt = 0;

	if (byTech) {

	    for (String index : indexes) {

		cpt++;

		event.setMatchCount(cpt);
		event.setMsg("Evaluating " + (int) (((double) cpt / (double) event.getMaxMatch()) * 100.0) + "% complete. Currently evaluating : 101/" + index
			+ " using " + choosenTech + ".");
		addEvent(dom, event);

		String query = "SELECT id FROM stored_schemas WHERE name = '101' and id_group = '4'";

		try {
		    ResultSet res = stat.executeQuery(query);

		    res.next();
		    leftId = res.getInt("id");

		    query = "SELECT id FROM stored_schemas WHERE name = '" + index + "' and id_group = '4'";

		    res = stat.executeQuery(query);

		    res.next();
		    rightId = res.getInt("id");

		} catch (SQLException e1) {
		    e1.printStackTrace();
		}

		if (leftId == -1 || rightId == -1)
		    return results;

		// for each couple, get expert value
		query = "SELECT ele1.name, ele2.name FROM match_results, elements as ele1, elements as ele2 WHERE expert = '1' AND id_element1 = ele1.id AND id_element2 = ele2.id AND id_element1 IN (SELECT id FROM elements WHERE schema_id = '"
			+ leftId + "') AND id_element2 IN (SELECT id FROM elements WHERE schema_id = '" + rightId + "')";

		GMappingTable<String> expertMatrix = new GMappingTable<String>();

		try {

		    ResultSet res = stat.executeQuery(query);
		    while (res.next())
			expertMatrix.addMapping(new GMappingScore<String>(res.getString(1), res.getString(2), 1));

		} catch (SQLException e) {
		    L.Error(e.getMessage(),e);
		}

		// ok get the matching value for the couple on the last xp
		query = "SELECT name1, name2, " + choosenTech + " FROM match_results_view WHERE id_schema1 = '" + leftId + "' AND id_schema2 = '" + rightId
			+ "'" + " AND name1 NOT LIKE '% onproperty %' AND name2 NOT LIKE '% onproperty %'"
			+ " AND name1 NOT LIKE '% domain %' AND name2 NOT LIKE '% domain %' AND name1 NOT LIKE '% range %' AND name2 NOT LIKE '% range %'"
			+ " ORDER BY " + choosenTech + " DESC LIMIT " + limit;// +
									      // " AND "+tech+" > '0.661'";

		GMappingTable<String> scoresMatrix = new GMappingTable<String>();

		try {

		    ResultSet res = stat.executeQuery(query);
		    while (res.next())
			scoresMatrix.addMapping(new GMappingScore<String>(res.getString("name1"), res.getString("name2"),
				new Double(res.getDouble(choosenTech)).floatValue()));

		} catch (SQLException e) {
		    L.Error(e.getMessage(),e);
		}

		// now filter the matrix
		ISimTableFilter filter = new MaxWeightAssignment();

		GMappingTable<String> filteredMatrix = filter.select(scoresMatrix);

		// and now evaluate
		Evaluation<String> eval = new Evaluation<String>(expertMatrix, filteredMatrix);

		ArrayList<String> tmpRes = new ArrayList<String>();
		tmpRes.add("101/"+index);

		tmpRes.add(new Double(eval.getPrecision()).toString());
		tmpRes.add(new Double(eval.getRecall()).toString());
		tmpRes.add(new Double(eval.getFmeasure()).toString());
		tmpRes.add(new String("Results for "+choosenTech));
		
		results.add(tmpRes);

	    }
	}
	
	// second case by doc
	if(byDoc){
	    
	    for (String tech : techs) {

		cpt++;

		event.setMatchCount(cpt);
		event.setMsg("Evaluating " + (int) (((double) cpt / (double) event.getMaxMatch()) * 100.0) + "% complete. Currently evaluating : 101/" + targetDoc
			+ " using " + tech + ".");
		addEvent(dom, event);

		String query = "SELECT id FROM stored_schemas WHERE name = '101' and id_group = '4'";

		try {
		    ResultSet res = stat.executeQuery(query);

		    res.next();
		    leftId = res.getInt("id");

		    query = "SELECT id FROM stored_schemas WHERE name = '" + targetDoc + "' and id_group = '4'";

		    res = stat.executeQuery(query);

		    res.next();
		    rightId = res.getInt("id");

		} catch (SQLException e1) {
		    e1.printStackTrace();
		}

		if (leftId == -1 || rightId == -1)
		    return results;

		// for each couple, get expert value
		query = "SELECT ele1.name, ele2.name FROM match_results, elements as ele1, elements as ele2 WHERE expert = '1' AND id_element1 = ele1.id AND id_element2 = ele2.id AND id_element1 IN (SELECT id FROM elements WHERE schema_id = '"
			+ leftId + "') AND id_element2 IN (SELECT id FROM elements WHERE schema_id = '" + rightId + "')";

		GMappingTable<String> expertMatrix = new GMappingTable<String>();

		try {

		    ResultSet res = stat.executeQuery(query);
		    while (res.next())
			expertMatrix.addMapping(new GMappingScore<String>(res.getString(1), res.getString(2), 1));

		} catch (SQLException e) {
		    L.Error(e.getMessage(),e);
		}

		// ok get the matching value for the couple on the last xp
		query = "SELECT name1, name2, " + tech + " FROM match_results_view WHERE id_schema1 = '" + leftId + "' AND id_schema2 = '" + rightId
			+ "'" + " AND name1 NOT LIKE '% onproperty %' AND name2 NOT LIKE '% onproperty %'"
			+ " AND name1 NOT LIKE '% domain %' AND name2 NOT LIKE '% domain %' AND name1 NOT LIKE '% range %' AND name2 NOT LIKE '% range %'"
			+ " ORDER BY " + tech + " DESC LIMIT " + limit;// +
									      // " AND "+tech+" > '0.661'";

		GMappingTable<String> scoresMatrix = new GMappingTable<String>();

		try {

		    ResultSet res = stat.executeQuery(query);
		    while (res.next())
			scoresMatrix.addMapping(new GMappingScore<String>(res.getString("name1"), res.getString("name2"),
				new Double(res.getDouble(tech)).floatValue()));

		} catch (SQLException e) {
		    L.Error(e.getMessage(),e);
		}

		// now filter the matrix
		ISimTableFilter filter = new MaxWeightAssignment();

		GMappingTable<String> filteredMatrix = filter.select(scoresMatrix);

		// and now evaluate
		Evaluation<String> eval = new Evaluation<String>(expertMatrix, filteredMatrix);

		ArrayList<String> tmpRes = new ArrayList<String>();
		tmpRes.add(tech);

		tmpRes.add(new Double(eval.getPrecision()).toString());
		tmpRes.add(new Double(eval.getRecall()).toString());
		tmpRes.add(new Double(eval.getFmeasure()).toString());
		tmpRes.add(new String("Results for 101/"+targetDoc));

		results.add(tmpRes);

	    }
	    
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

	event.setMatchCount(0);
	event.setMaxMatch(0);
	event.setMsg("Processing terminated, evaluation up to date");
	addEvent(dom, event);

	return results;

    }
}
