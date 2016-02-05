package org.inria.websmatch.gwt.spreadsheet.server;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.inria.websmatch.db.MySQLDBConfLoader;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingResult;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingScores;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSchema;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingScores;
import org.mitre.harmony.matchers.ElementPair;
import org.mitre.harmony.matchers.MatcherScores;

import yam.system.Configs;
import yam.tools.WordNetHelper;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("deprecation")
public class MatchingResultsServiceImpl extends RemoteServiceServlet implements MatchingResultsService {

    /**
     * 
     */
    private static final long serialVersionUID = 8847557277727160053L;

    private String dbuser;// = "matcher";
    private String pass;// = "matcher";
    // Inria private String dbHost = "localhost";
    private String dbHost;// = "193.49.106.32";
    private int dbPort;// = 3306;
    private String dbName;// = "matching_results";

    //
    private java.sql.Connection conn = null;
    private java.sql.Statement stat = null;
    private HashMap<String, Double[]> minMax = new HashMap<String, Double[]>();

    //
    // private double minProba = Math.pow(0.0001,14);
    // private double maxProba = Math.pow(0.1,14);
    // private double minProba = Double.parseDouble("8.56264367193469e-51");
    // private double maxProba = Double.parseDouble("1.13858930263158e-13");
    // private double avgProba =
    // Double.parseDouble("8.56264367193469e-35");//(maxProba - minProba) / 2;
    // Original value
    public static double avgProba = Math.pow((((double) 7690 / (double) 3259726) * 1.75), 14.0);
    // Test pour Riga
    // public static double avgProba = Math.pow((((double) 7690 / (double) 3259726) * 1.25), 14.0);
    public static double expertProba = avgProba * 4;

    private String storeService;
    
    private List<String> removedEntities = Arrays.asList(new String[]{"Feuil1", "Feuil2", "Feuil3", "Données"});

    public void init() {
	storeService = getServletContext().getInitParameter("schemaStoreService");

	// init WordNet
	/*yam.system.Configs.WNTMP = "WNTemplate.xml";
	yam.system.Configs.WNPROP = "file_properties.xml";

	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}*/
	System.out.println("Current dir : "+System.getProperty("user.dir"));
	// init WordNet
	yam.system.Configs.WNTMP = System.getProperty("user.dir") + "/webapps/WebSmatch/WNTemplate.xml";
	yam.system.Configs.WNPROP = System.getProperty("user.dir") + "/webapps/WebSmatch/file_properties.xml";

	yam.system.Configs.WNDIR = System.getProperty("user.dir") + "/webapps/WebSmatch/WordNet/2.1/dict";

	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	    WordNetHelper.getInstance().initializeIC(System.getProperty("user.dir") + "/webapps/WebSmatch/" + Configs.WNIC);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	}
    }

    @Override
    public List<MatchingResult> getResults(String leftId, String rightId, String treshold, String tech) {

	List<MatchingResult> results = new ArrayList<MatchingResult>();
	int groupId = LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId()).intValue();

	connect();

	String query = "SELECT name1, name2, "
		+ tech
		+ ", expert, id_element1, id_schema1, id_element2, id_schema2 from match_results_view WHERE id_schema1 = (SELECT MAX(id) FROM stored_schemas WHERE name = '"
		+ leftId.replaceAll("'", "\\\\'") + "' AND id_group = '" + groupId + "') AND id_schema2"
		+ " = (SELECT MAX(id) FROM stored_schemas WHERE name ='" + rightId.replaceAll("'", "\\\\'") + "' AND id_group = '" + groupId + "') AND ("
		+ tech + " >= '" + treshold + "' OR expert = '1') GROUP BY name1, name2, " + tech
		+ ", expert, id_element1, id_schema1, id_element2, id_schema2 ORDER BY expert DESC, " + tech + " DESC;";

	// get results now
	ResultSet result;
	try {
	    result = stat.executeQuery(query);

	    while (result.next()) {
		results.add(new MatchingResult(result.getString("name1"), result.getString("name2"), result.getDouble(tech), result.getBoolean("expert"),
			result.getInt("id_element1"), result.getInt("id_schema1"), result.getInt("id_element2"), result.getInt("id_schema2")));
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	if (results.size() == 0) {

	    query = "SELECT name1, name2, "
		    + tech
		    + ", expert, id_element1, id_schema1, id_element2, id_schema2 from match_results_view WHERE id_schema1 = (SELECT MAX(id) FROM stored_schemas WHERE name = '"
		    + rightId.replaceAll("'", "\\\\'") + "' AND id_group = '" + groupId + "') AND id_schema2"
		    + " = (SELECT MAX(id) FROM stored_schemas WHERE name ='" + leftId.replaceAll("'", "\\\\'") + "' AND id_group = '" + groupId + "') AND ("
		    + tech + " >= '" + treshold + "' OR expert = '1') GROUP BY name1, name2, " + tech
		    + ", expert, id_element1, id_schema1, id_element2, id_schema2 ORDER BY expert DESC, " + tech + " DESC;";

	    try {
		result = stat.executeQuery(query);

		while (result.next()) {
		    results.add(new MatchingResult(result.getString("name2"), result.getString("name1"), result.getDouble(tech), result.getBoolean("expert"),
			    result.getInt("id_element2"), result.getInt("id_schema2"), result.getInt("id_element1"), result.getInt("id_schema1")));
		}

	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }

	}

	// System.out.println("Size res : "+results.size());

	close();

	return results;
    }

    @Override
    public List<SimpleSchema> getMatchedSchemas() {

	List<SimpleSchema> schemas = new ArrayList<SimpleSchema>();

	connect();

	String query = "SELECT * FROM stored_schemas where id_group = '" + LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId())
		+ "' ORDER BY name;";

	ResultSet result;
	try {
	    result = stat.executeQuery(query);

	    while (result.next()) {
		schemas.add(new SimpleSchema(result.getString("id"), result.getString("name")));
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	close();

	return schemas;
    }

    private void connect() {
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
	    
	    conn = DriverManager.getConnection("jdbc:mysql://" + this.dbHost + ":" + this.dbPort + "/" + this.dbName, dbuser, pass);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
	try {
	    stat = conn.createStatement();
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
    }

    private void close() {
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
    }

    @Override
    public List<SimpleMatchTech> getMatchingTechs() {

	List<SimpleMatchTech> techs = new ArrayList<SimpleMatchTech>();

	connect();

	String query = "SELECT * FROM matching_techs ORDER BY id;";

	ResultSet result;
	try {
	    result = stat.executeQuery(query);

	    while (result != null && result.next()) {
		techs.add(new SimpleMatchTech(result.getInt("id"), result.getString("name")));
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// get min/max by tech to normalize
	query = "SELECT tech, MIN(proba), MAX(proba) FROM intervals WHERE proba > 0 AND proba < 1 GROUP BY tech;";

	minMax = new HashMap<String, Double[]>();

	try {
	    ResultSet set = stat.executeQuery(query);

	    while (set.next()) {

		minMax.put(set.getString("tech"), new Double[] { set.getDouble("MIN(proba)"), set.getDouble("MAX(proba)") });

	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	close();

	return techs;

    }

    @Override
    public void updateExpert(int id_element1, int id_element2, int val, int sid1, int sid2) {

	connect();

	String query = "UPDATE match_results SET expert = '" + val + "' WHERE (id_element1 = '" + id_element1 + "' " + "AND id_element2 = '" + id_element2
		+ "') OR " + "(id_element1 = '" + id_element2 + "' " + "AND id_element2 = '" + id_element1 + "');";

	// System.out.println(query);
	
	try {
	    stat.executeUpdate(query);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	close();
	
	// then update cluster
	if( sid1 < sid2) this.insertProbaDistance(sid1, sid2);
	else if(sid2 < sid1) this.insertProbaDistance(sid2, sid1);

    }

    @Override
    public MatchingScores getScores(int id_element1, int id_schema1, int id_element2, int id_schema2) {

	MatchingScores scores = new MatchingScores(id_element1, id_schema1, id_element2, id_schema2);

	// take the techs
	List<SimpleMatchTech> techs = this.getMatchingTechs();

	// take the scores
	connect();

	// get the order
	int leftElementId = 0;
	int rightElementId = 0;

	if (id_schema1 < id_schema2) {
	    leftElementId = id_element1;
	    rightElementId = id_element2;
	} else {
	    leftElementId = id_element2;
	    rightElementId = id_element1;
	}

	// only 1 query needed to get scores, then get probas for each tech
	String query = "SELECT * FROM match_results WHERE id_element1 = '" + leftElementId + "' AND id_element2 = '" + rightElementId + "'";
	boolean notInserted = false;

	try {
	    ResultSet set = stat.executeQuery(query);

	    // we have the score in db
	    if (set.next()) {

		double probaScore = set.getDouble("Proba_Prod");

		// normalize
		// double delta = maxProba - minProba;
		// double val = (probaScore - minProba) / delta;

		// System.out.println(minProba);
		// System.out.println(maxProba);

		scores.addScore("Overall", probaScore);

		// System.out.println("Tech : Proba_Prod - Proba (norm) : " +
		// val + " Proba (base) : " + probaScore);

		for (SimpleMatchTech tech : techs) {
		    scores.addScore(tech.getName(), set.getDouble(tech.getName()));
		}

		// scores.addScore("Proba_Max", set.getDouble("Proba_Max"));
		// scores.addScore("Proba_Prod", set.getDouble("Proba_Prod"));
		scores.setExpert(set.getBoolean("expert"));
	    }
	    // it's not in db, compute it
	    else {
		notInserted = true;
		SpreadsheetParsingServiceImpl service = new SpreadsheetParsingServiceImpl();
		service.setStoreService(this.storeService);

		for (SimpleMatchTech tech : techs) {
		    MatcherScores score = null;
		    // we match each tech
		    if (id_schema1 < id_schema2) {
			score = service.localMatchElements(id_schema1, id_schema2, id_element1, id_element2, null, tech.getName());
			if (score != null) {
			    for (ElementPair pair : score.getElementPairs()) {
				scores.addScore(tech.getName(), score.getScore(pair).getPositiveEvidence());
				//System.out.println(tech.getName()+" "+score.getScore(pair).getPositiveEvidence());
				break;
			    }
			}
		    } else {
			score = service.localMatchElements(id_schema2, id_schema1, id_element2, id_element1, null, tech.getName());
			if (score != null) {
			    for (ElementPair pair : score.getElementPairs()) {
				scores.addScore(tech.getName(), score.getScore(pair).getPositiveEvidence());
				//System.out.println(tech.getName()+" "+score.getScore(pair).getPositiveEvidence());
				break;
			    }
			}
		    }
		}

		scores.setExpert(false);

	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// if it was not in db, we have to compute probas max and prod
	double proba_prod = 1.0;
	double proba_max = 0.0;

	// tmp probas for not inserted tuple
	HashMap<String, Double> tmpProba = new HashMap<String, Double>();

	// probas
	for (SimpleMatchTech tech : techs) {

	    try {
		double score = scores.getScores().get(tech.getName());

		query = "SELECT proba FROM intervals WHERE tech = '" + tech.getName() + "' AND begin >= '" + score + "' AND end <= '" + score + "' LIMIT 1;";

		ResultSet probaset = stat.executeQuery(query);

		double proba = 0.0;

		if (probaset != null && probaset.next())
		    proba = probaset.getDouble("proba");

		// normalize
		double delta = minMax.get(tech.getName())[1] - minMax.get(tech.getName())[0];
		double val = (proba - minMax.get(tech.getName())[0]) / delta;

		// System.out.println("Tech : " + tech.getName() +
		// " Proba (norm) : " + val + " Proba (base) : " + proba +
		// " Val (base) : "+ score);
		if (!notInserted) {
		    scores.addScore(tech.getName(), val);
		} else {
		    tmpProba.put(tech.getName(), val);
		}

		//if(proba == 0.0) System.out.println(query);
		
		proba_max = Math.max(proba_max, proba);
		proba_prod = proba_prod * proba;

	    } catch (SQLException e) {
		// L.Error(e.getMessage(),e);
	    }
	}

	// if not inserted, insert now
	if (notInserted) {

	    // construct the query
	    query = "INSERT INTO match_results (id_schema1, id_element1, id_schema2, id_element2, ";
	    String endQuery = " VALUES (";

	    if (id_schema1 < id_schema2) {

		endQuery += "'" + id_schema1 + "', ";
		endQuery += "'" + id_element1 + "', ";
		endQuery += "'" + id_schema2 + "', ";
		endQuery += "'" + id_element2 + "', ";

	    } else {

		endQuery += "'" + id_schema2 + "', ";
		endQuery += "'" + id_element2 + "', ";
		endQuery += "'" + id_schema1 + "', ";
		endQuery += "'" + id_element1 + "', ";

	    }

	    // for each tech
	    for (SimpleMatchTech localTech : techs) {

		query += localTech.getName() + ", ";
		endQuery += "'" + scores.getScores().get(localTech.getName()) + "', ";

	    }

	    query += "Proba_Max, Proba_Prod)";
	    endQuery += "'" + proba_max + "', '" + proba_prod + "');";

	    try {
		// System.out.println(query+endQuery);
		stat.executeUpdate(query + endQuery);
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		L.Error(e.getMessage(),e);
	    }
	    //

	}

	close();

	// then set probas instead of scores
	if (notInserted) {

	    for (SimpleMatchTech tech : techs) {

		scores.getScores().put(tech.getName(), tmpProba.get(tech.getName()));

	    }

	}

	// add them if not setted
	if (!scores.getScores().containsKey("Overall")) {

	    // normalize
	    // double delta = maxProba - minProba;
	    // double val = (proba_prod - minProba) / delta;

	    // System.out.println(minProba);
	    // System.out.println(maxProba);

	    double val = 0.0;

	    if (proba_prod > (4.0 * avgProba))
		val = 1.0;
	    else if (proba_prod > (2.0 * avgProba))
		val = 0.75;
	    else if (proba_prod >= avgProba)
		val = 0.5;
	    else if (proba_prod < (avgProba / 4))
		val = 0.0;
	    else if (proba_prod < (avgProba / 2))
		val = 0.25;
	    else if (proba_prod < avgProba)
		val = 0.4;

	    scores.addScore("Overall", val);
	} else {
	    double val = 0.0;
	    proba_prod = scores.getScores().get("Overall");

	    if (proba_prod > (4.0 * avgProba))
		val = 1.0;
	    else if (proba_prod > (2.0 * avgProba))
		val = 0.75;
	    else if (proba_prod >= avgProba)
		val = 0.5;
	    else if (proba_prod < (avgProba / 4))
		val = 0.0;
	    else if (proba_prod < (avgProba / 2))
		val = 0.25;
	    else if (proba_prod < avgProba)
		val = 0.4;

	    scores.addScore("Overall", val);
	}

	return scores;
    }

    public void insertProbaDistance(int lid, int rid) {

	double sumProba_Prod = 0.0;

	// System.out.println("Avg proba : " + avgProba);

	List<MatchingResult> results = new ArrayList<MatchingResult>();

	List<MatchingResult> filteredResults = new ArrayList<MatchingResult>();

	// int groupId =
	// LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId()).intValue();

	connect();

	int tmpsid1 = lid;
	int tmpsid2 = rid;

	int schemaid1 = -1;
	int schemaid2 = -1;

	//
	if (tmpsid1 < tmpsid2) {
	    schemaid1 = tmpsid1;
	    schemaid2 = tmpsid2;
	} else {
	    schemaid1 = tmpsid2;
	    schemaid2 = tmpsid1;
	}

	int nbNodes1 = 0;
	int nbNodes2 = 0;

	// we need to know the max of node on each side
	String query = "SELECT COUNT(*) FROM elements WHERE schema_id = '" + schemaid1 + "';";
	ResultSet result;

	try {
	    result = stat.executeQuery(query);

	    while (result.next()) {
		nbNodes1 = result.getInt("COUNT(*)");
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	//
	query = "SELECT COUNT(*) FROM elements WHERE schema_id = '" + schemaid2 + "';";

	try {
	    result = stat.executeQuery(query);

	    while (result.next()) {
		nbNodes2 = result.getInt("COUNT(*)");
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	int max = Math.max(nbNodes1, nbNodes2);
	// int factor = 5;
	int factor = 1;
	int maxFactor = max * factor;

	//
	query = "SELECT name1, name2, Proba_Prod" + ", expert, id_element1, id_schema1, id_element2, id_schema2 from match_results_view WHERE id_schema1 = '"
		+ schemaid1 + "' AND id_schema2 = '" + schemaid2 + "' AND (Proba_Prod > 0 OR (Proba_Prod = 0.0 AND expert = '1')) GROUP BY name1, name2, Proba_Prod"
		+ ", expert, id_element1, id_schema1, id_element2, id_schema2 ORDER BY expert DESC, Proba_Prod DESC;";

	// get results now
	try {
	    result = stat.executeQuery(query);

	    int i = 0;
	    while (result.next() && i < maxFactor) {
		if(!removedEntities.contains(result.getString("name1")) && !removedEntities.contains(result.getString("name2"))){
		results.add(new MatchingResult(result.getString("name1"), result.getString("name2"), result.getDouble("Proba_Prod"), result
			.getBoolean("expert"), result.getInt("id_element1"), result.getInt("id_schema1"), result.getInt("id_element2"), result
			.getInt("id_schema2")));
		i++;
		}
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// System.out.println("Size res : "+results.size());

	close();

	// we have all the results, we have to filter them
	// we keep only the five max best for each left node
	// if expert, it's only this one
	HashMap<Integer, Integer> idsAndCount = new HashMap<Integer, Integer>();

	for (MatchingResult match : results) {

	    // if expert, get the first expert
	    if (match.isExpert()) {

		idsAndCount.put(match.getId_element1(), factor);
		idsAndCount.put(match.getId_element2(), factor);
		filteredResults.add(match);
		if(match.getScore() >= expertProba) sumProba_Prod += Math.pow(match.getScore(), (double) 1 / (double) 14);
		else sumProba_Prod += Math.pow(expertProba, (double) 1 / (double) 14);
		// System.out.println("Expert added : "+sumProba_Prod);
	    }

	    // else max factor over the avgProba
	    else if (!match.isExpert() && match.getScore() >= avgProba) {

		    if ((idsAndCount.get(match.getId_element1()) == null || idsAndCount.get(match.getId_element1()) < factor)
			    && (idsAndCount.get(match.getId_element2()) == null || idsAndCount.get(match.getId_element2()) < factor)) {

			if(idsAndCount.get(match.getId_element1()) == null) idsAndCount.put(match.getId_element1(),1);
			else idsAndCount.put(match.getId_element1(), (idsAndCount.get(match.getId_element1()) + 1));
			if(idsAndCount.get(match.getId_element2()) == null) idsAndCount.put(match.getId_element2(),1);
			else idsAndCount.put(match.getId_element2(), (idsAndCount.get(match.getId_element2()) + 1));

			// normalize
			/*
			 * double delta = maxProba - minProba; double val =
			 * (match.getScore() - minProba) / delta; if(val > 1.0)
			 * val = 1.0;
			 * 
			 * match.setScore(val);
			 */
			//

			filteredResults.add(match);
			sumProba_Prod += Math.pow(match.getScore(), (double) 1 / (double) 14);
			// System.out.println("No Expert added : "+sumProba_Prod);

		    }
	    }
	}

	// System.out.println("Original results : " + results.size());
	// System.out.println("Filtered results : " + filteredResults.size());

	// ok we insert in cluster
	double distance = (double) sumProba_Prod / (double) Math.min(nbNodes1, nbNodes2);

	connect();
		
	query = "INSERT INTO schemas_distance SET id_schema1 = '" + schemaid1 + "', name1 = (SELECT name FROM stored_schemas WHERE id ='" + schemaid1
		+ "'), id_group1 = (SELECT id_group FROM stored_schemas WHERE id ='" + schemaid1 + "'), id_schema2 = '" + schemaid2
		+ "', name2 = (SELECT name FROM stored_schemas WHERE id ='" + schemaid2 + "'), id_group2 = (SELECT id_group FROM stored_schemas WHERE id ='"
		+ schemaid2 + "'), distance ='" + distance + "' ON DUPLICATE KEY UPDATE distance ='" + distance + "';";

	// System.out.println(query);

	try {
	    stat.executeUpdate(query);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	close();
	//
    }

    @Override
    public List<MatchingResult> getProbaResults(String leftId, String rightId) {

	// System.out.println("Avg proba : " + avgProba);

	List<MatchingResult> results = new ArrayList<MatchingResult>();

	List<MatchingResult> filteredResults = new ArrayList<MatchingResult>();

	int groupId = LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId()).intValue();

	connect();

	int tmpsid1 = -1;
	int tmpsid2 = -1;

	int schemaid1 = -1;
	int schemaid2 = -1;

	// we need to get the schema1 and schema2 ids
	String query = "SELECT MAX(id) FROM stored_schemas WHERE name = '" + leftId.replaceAll("'", "\\\\'") + "' AND id_group = '" + groupId + "';";

	ResultSet result;
	try {
	    result = stat.executeQuery(query);
	    if (result.next())
		tmpsid1 = result.getInt("MAX(id)");
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	query = "SELECT MAX(id) FROM stored_schemas WHERE name = '" + rightId.replaceAll("'", "\\\\'") + "' AND id_group = '" + groupId + "';";

	try {
	    result = stat.executeQuery(query);
	    if (result.next())
		tmpsid2 = result.getInt("MAX(id)");
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	//
	if (tmpsid1 < tmpsid2) {
	    schemaid1 = tmpsid1;
	    schemaid2 = tmpsid2;
	} else {
	    schemaid1 = tmpsid2;
	    schemaid2 = tmpsid1;
	}

	int nbNodes1 = 0;
	int nbNodes2 = 0;

	// we need to know the max of node on each side
	query = "SELECT COUNT(*) FROM elements WHERE schema_id = '" + schemaid1 + "';";

	try {
	    result = stat.executeQuery(query);

	    while (result.next()) {
		nbNodes1 = result.getInt("COUNT(*)");
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	//
	query = "SELECT COUNT(*) FROM elements WHERE schema_id = '" + schemaid2 + "';";

	try {
	    result = stat.executeQuery(query);

	    while (result.next()) {
		nbNodes2 = result.getInt("COUNT(*)");
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	int max = Math.max(nbNodes1, nbNodes2);
	// int factor = 5;
	int factor = 3;
	int maxFactor = max * factor;

	//
	query = "SELECT name1, name2, Proba_Prod" + ", expert, id_element1, id_schema1, id_element2, id_schema2 from match_results_view WHERE id_schema1 = '"
		+ schemaid1 + "' AND id_schema2 = '" + schemaid2 + "' AND (Proba_Prod > 0 OR expert = 1) GROUP BY name1, name2, Proba_Prod"
		+ ", expert, id_element1, id_schema1, id_element2, id_schema2 ORDER BY expert DESC, Proba_Prod DESC;";

	// get results now
	try {
	    result = stat.executeQuery(query);

	    int i = 0;
	    while (result.next() && i < maxFactor) {
		if(!removedEntities.contains(result.getString("name1")) && !removedEntities.contains(result.getString("name2"))){
		results.add(new MatchingResult(result.getString("name1"), result.getString("name2"), result.getDouble("Proba_Prod"), result
			.getBoolean("expert"), result.getInt("id_element1"), result.getInt("id_schema1"), result.getInt("id_element2"), result
			.getInt("id_schema2")));
		i++;
		}
	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// System.out.println("Size res : "+results.size());

	close();

	// we have all the results, we have to filter them
	// we keep only the five max best for each left node
	// if expert, it's only this one
	HashMap<Integer, Integer> idsAndCount = new HashMap<Integer, Integer>();

	for (MatchingResult match : results) {

	    // if expert, get the first expert
	    if (match.isExpert()) {

		idsAndCount.put(match.getId_element1(), factor);
		idsAndCount.put(match.getId_element2(), factor);
		filteredResults.add(match);

	    }

	    // else max 5 over the avgProba
	    else if (!match.isExpert() && match.getScore() >= avgProba) {

		    if ((idsAndCount.get(match.getId_element1()) == null || idsAndCount.get(match.getId_element1()) < factor)
			    && (idsAndCount.get(match.getId_element2()) == null || idsAndCount.get(match.getId_element2()) < factor)) {

			if(idsAndCount.get(match.getId_element1()) == null) idsAndCount.put(match.getId_element1(),1);
			else idsAndCount.put(match.getId_element1(), (idsAndCount.get(match.getId_element1()) + 1));
			if(idsAndCount.get(match.getId_element2()) == null) idsAndCount.put(match.getId_element2(),1);
			else idsAndCount.put(match.getId_element2(), (idsAndCount.get(match.getId_element2()) + 1));

			// normalize
			/*
			 * double delta = maxProba - minProba; double val =
			 * (match.getScore() - minProba) / delta; if(val > 1.0)
			 * val = 1.0;
			 */

			double val = 0.0;

			if (match.getScore() > (4.0 * avgProba))
			    val = 1.0;
			else if (match.getScore() > (2.0 * avgProba))
			    val = 0.75;
			else if (match.getScore() >= avgProba)
			    val = 0.5;
			else if (match.getScore() < (avgProba / 4))
			    val = 0.0;
			else if (match.getScore() < (avgProba / 2))
			    val = 0.25;
			else if (match.getScore() < avgProba)
			    val = 0.4;

			match.setScore(val);
			//

			filteredResults.add(match);

		    }
		}	  
	}

	// System.out.println("Original results : " + results.size());
	// System.out.println("Filtered results : " + filteredResults.size());

	return filteredResults;
    }

}
