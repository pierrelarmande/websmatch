package org.mitre.harmony.matchers.matchers;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.inria.websmatch.db.MySQLDBConfLoader;
import org.inria.websmatch.utils.L;
import org.mitre.harmony.Harmony;
import org.mitre.harmony.matchers.MatcherOption;
import org.mitre.harmony.matchers.MatcherOption.OptionType;
import org.mitre.harmony.matchers.MatcherScore;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;

import yam.datatypes.interfaces.IElement;
import yam.simlib.general.IMetric;
import yam.simlib.general.label.ILabelMetric;
import yam.simlib.general.name.INameMetric;

/**
 * @author ngoduyhoa wrap yam similarity metric to openii matcher
 */
public class YAMMatcherWrapper extends Matcher {
    // max value that this matcher can return. A scaling factor.
    public static final double SCORE_CEILING = 1.0;

    // yam metric
    private IMetric ymetric;

    private java.sql.Connection conn;

    private String choosenTech;

    private int userGroup = 0;

    /*
     * public YAMMatcherWrapper(IMetric ymetric) { super(); this.ymetric =
     * ymetric; }
     */

    public void setMetric(IMetric ym) {
	this.ymetric = ym;
    }

    public void setUserGroup(int g) {
	this.userGroup = g;
    }

    public ArrayList<MatcherOption> getMatcherOptions() {
	ArrayList<MatcherOption> options = new ArrayList<MatcherOption>();
	options.add(new MatcherOption(OptionType.CHECKBOX, LEVEN, "true"));
	options.add(new MatcherOption(OptionType.CHECKBOX, SMITH, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, SMITHGTW, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, JARO, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, JAROW, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, STOILOS, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, QGRAMS, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, MONGE, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, WUPALMER, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, LIN, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, MULTI, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, SOFTTFIDF, "false"));
	options.add(new MatcherOption(OptionType.CHECKBOX, SOFTTFIDFWN, "false"));

	return options;
    }

    @Override
    public String getName() {
	// TODO Auto-generated method stub
	// return ymetric.getMetricName();
	return "YAMMatcherWrapper";
    }

    public void setChoosenTech(String name) {
	choosenTech = name;
    }

    @Override
    public MatcherScores match() {

	// long begin = System.currentTimeMillis();
	// System.out.println("Begin match TS : " + begin);
	/*
	 * yam.system.Configs.WNTMP = "WNTemplate.xml";
	 * yam.system.Configs.WNPROP = "file_properties.xml";
	 */

	/*
	 * try { System.out.println("Current dir : "+new File
	 * (".").getCanonicalPath().toString()); } catch (IOException e1) {
	 * e1.printStackTrace(); }
	 */

	// TODO Auto-generated method stub
	// this.ymetric = new SMTokenWrapper(new QGramsDistance());
	// instantiate YAMMatcherWrapperFactory
	YAMMatcherWrapperFactory factory = YAMMatcherWrapperFactory.getInstance();

	/*
	 * try { WordNetHelper.getInstance().initializeWN(Configs.WNDIR,
	 * Configs.WNVER);
	 * WordNetHelper.getInstance().initializeIC(Configs.WNIC); } catch
	 * (Exception e) { // TODO Auto-generated catch block
	 * L.Error(e.getMessage(),e); }
	 */

	// get OpenII Matcher by name
	// Matcher matcher = factory.getMatcherByName("Stoilos_JW");

	String selectedMatcher = new String();

	if (choosenTech == null) {
	    // get selected matcher
	    Iterator<String> i = options.keySet().iterator();
	    while (i.hasNext()) {
		String key = (String) i.next();
		MatcherOption values = (MatcherOption) options.get(key);
		if (values.isSelected()) {
		    selectedMatcher = values.getName();
		    break;
		}
	    }
	}

	else
	    selectedMatcher = choosenTech;

	Matcher matcher = factory.getMatcherByName(selectedMatcher);

	// Get the source and target elements
	ArrayList<SchemaElement> sourceElements = schema1.getFilteredElements();
	ArrayList<SchemaElement> targetElements = schema2.getFilteredElements();

	// inserts shemas
	// int[] matchExpeId = { -1, -1 };
	if (Harmony.yamDB) {
	    /* matchExpeId = */this.insertSchemasAndTech(schema1.getSchema(), schema2.getSchema(), selectedMatcher);
	}
	//

	// Sets the completed and total comparisons
	completedComparisons = 0;
	totalComparisons = sourceElements.size() * targetElements.size();

	// Special RIGA
	boolean riga = false;
	if (schema1.getSchema().getType().indexOf("Osmoze") != -1 && schema2.getSchema().getType().indexOf("Osmoze") != -1)
	    riga = true;
	// if (riga) System.out.println("Riga Mode matching");

	ArrayList<Object[]> toInsert = new ArrayList<Object[]>();
	HashMap<Integer, Object[]> elementsToInsert = new HashMap<Integer, Object[]>();

	// Generate the scores
	MatcherScores scores = new MatcherScores(SCORE_CEILING);
	for (SchemaElement sourceElement : sourceElements)
	    for (SchemaElement targetElement : targetElements) {
		if (isAllowableMatch(sourceElement, targetElement) && (!sourceElement.getName().equals("") && !targetElement.getName().equals("")))
		    if (scores.getScore(sourceElement.getId(), targetElement.getId()) == null) {
			MatcherScore score = null;
			if (!riga) {
			    if (sourceElement.getClass().toString().equals(targetElement.getClass().toString()))
				score = ((YAMMatcherWrapper) matcher).matchElements(sourceElement, targetElement);
			    else
				score = null;
			} else if (riga) {
			    // keep only Course titles (and same level)
			    if (!sourceElement.getDescription().trim().equals(targetElement.getDescription().trim()))
				score = null;
			    else {
				if (sourceElement.getDescription().indexOf("Course titles") != -1
					&& targetElement.getDescription().indexOf("Course titles") != -1) {
				    score = ((YAMMatcherWrapper) matcher).matchElements(sourceElement, targetElement);
				} else
				    score = null;
			    }
			}
			if (score != null) {
			    scores.setScore(sourceElement.getId(), targetElement.getId(), score);

			    //
			    // dont keep riga compulsory courses
			    if (riga) {
				if (sourceElement.getName().indexOf("ompulsory courses") == -1 && targetElement.getName().indexOf("ompulsory courses") == -1) {
				    if (Harmony.yamDB) {
					toInsert.add(new Object[] { schema1.getSchema(), schema2.getSchema(), sourceElement, targetElement, selectedMatcher,
						score.getPositiveEvidence() });
					elementsToInsert.put(sourceElement.getId(), new Object[] { sourceElement.getId(), sourceElement.getName(),
						schema1.getSchema().getId(), sourceElement });
					elementsToInsert.put(targetElement.getId(), new Object[] { targetElement.getId(), targetElement.getName(),
						schema2.getSchema().getId(), targetElement });

				    }
				}
				// to remove after testing
				// this.insertInDB(schema1.getSchema(),
				// schema2.getSchema(), sourceElement,
				// targetElement,
				// selectedMatcher,score.getPositiveEvidence());
			    } else if (Harmony.yamDB) {
				/*if(score.getPositiveEvidence() > 0.6) this.insertInDB(schema1.getSchema(), schema2.getSchema(), sourceElement, targetElement, selectedMatcher,
					score.getPositiveEvidence());*/
				toInsert.add(new Object[] { schema1.getSchema(), schema2.getSchema(), sourceElement, targetElement, selectedMatcher,
					score.getPositiveEvidence() });
				elementsToInsert.put(sourceElement.getId(), new Object[] { sourceElement.getId(), sourceElement.getName(),
					schema1.getSchema().getId(), sourceElement });
				elementsToInsert.put(targetElement.getId(), new Object[] { targetElement.getId(), targetElement.getName(),
					schema2.getSchema().getId(), targetElement });

			    }
			    //
			}
		    }
		completedComparisons++;
	    }

	if (Harmony.yamDB) {
	    this.insertElements(elementsToInsert);
	    
	    // TODO fix this bad thing	    
	    // for riga testing
	    // this.insertInDB(toInsert);
	}

	try {
	    if (conn != null)
		conn.close();
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}

	choosenTech = null;

	// System.out.println("End, total time in ms : " +
	// (System.currentTimeMillis() - begin));
	// System.out.println("Scores : " + scores.getElementPairs().size());

	return scores;
    }

    public MatcherScore matchElements(SchemaElement sourceElement, SchemaElement targetElement) {
	return matchElements(sourceElement, targetElement, ymetric);
    }

    private static MatcherScore matchElements(SchemaElement sourceElement, SchemaElement targetElement, IMetric ymetric) {

	// System.out.println("Source : "+sourceElement.getName()+"\tTarget : "+targetElement.getName());
	// make a wrapper to source and target elements
	IElement selement = new SchemaElementWrapper(sourceElement);
	IElement telement = new SchemaElementWrapper(targetElement);

	if (ymetric instanceof INameMetric) {
	    double score = ((INameMetric) ymetric).getNameSimScore(selement, telement);

	    return new MatcherScore(score, 1.0);
	} else if (ymetric instanceof ILabelMetric) {
	    double score = ((ILabelMetric) ymetric).getLabelSimScore(selement, telement);

	    return new MatcherScore(score, 1.0);
	} else
	    return null;
    }

    public void insertSchemasAndTech(Schema ls, Schema rs, String tech) {
	// connect to the db
	String user;// = "matcher";
	String pass;// = "matcher";
	// Inria String dbHost = "localhost";
	String dbHost;// = "193.49.106.32";
	int dbPort;// = 3306;
	String dbName;// = "matching_results";

	// int[] match_expe_id = { -1, -1 };

	// now we go on and insert each scores with the good values
	try {
	    try {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
	    } catch (InstantiationException e) {
		L.Error(e.getMessage(),e);
	    } catch (IllegalAccessException e) {
		L.Error(e.getMessage(),e);
	    } catch (ClassNotFoundException e) {
		L.Error(e.getMessage(),e);
	    }

	    // load conf
	    MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

	    user = loader.getDbuser();
	    pass = loader.getPass();
	    dbHost = loader.getDbHost();
	    dbPort = loader.getDbPort();
	    dbName = loader.getDbName();
	    
	    conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName, user, pass);
	    java.sql.Statement stat = conn.createStatement();

	    // update the result

	    // first update schema
	    // is this schema existing
	    String query = "SELECT name, xml, original_xml FROM stored_schemas WHERE id = '" + ls.getId() + "';";
	    ResultSet res = stat.executeQuery(query);

	    String xml = new String();
	    String original_xml = new String();

	    if (res.next()) {

		xml = res.getString("xml");
		original_xml = res.getString("original_xml");

	    }

	    query = "REPLACE INTO stored_schemas (id,name,id_group,xml,original_xml ) VALUES ('" + ls.getId() + "' , '" + ls.getName().replaceAll("'", "\\\\'")
		    + "' , '" + this.userGroup + "' , '" + xml + "','" + original_xml + "');";
	    stat.executeUpdate(query);

	    query = "SELECT name, xml, original_xml FROM stored_schemas WHERE id = '" + rs.getId() + "';";
	    res = stat.executeQuery(query);

	    xml = new String();
	    original_xml = new String();

	    if (res.next()) {

		xml = res.getString("xml");
		original_xml = res.getString("original_xml");

	    }
	    query = "REPLACE INTO stored_schemas (id,name,id_group,xml,original_xml) VALUES ('" + rs.getId() + "' , '" + rs.getName().replaceAll("'", "\\\\'")
		    + "' , '" + this.userGroup + "' , '" + xml + "','" + original_xml + "');";
	    stat.executeUpdate(query);

	    // ok is this tech already in db
	    /*
	     * query = "SELECT id FROM matching_techs WHERE name = '" + tech +
	     * "';"; res = stat.executeQuery(query);
	     * 
	     * if (res.next()) match_expe_id[0] = res.getInt("id");
	     * 
	     * else { query = "INSERT INTO matching_techs (name) VALUES ('" +
	     * tech + "');"; stat.executeUpdate(query);
	     * 
	     * // get the id query =
	     * "SELECT id FROM matching_techs WHERE name = '" + tech + "';"; res
	     * = stat.executeQuery(query);
	     * 
	     * res.first(); match_expe_id[0] = res.getInt("id"); res.close(); }
	     */

	    // same thing with expe id
	    /*
	     * query = "REPLACE INTO expes (date) VALUES (CURRENT_TIMESTAMP);";
	     * stat.executeUpdate(query);
	     * 
	     * query = "SELECT MAX(id) FROM expes;"; res =
	     * stat.executeQuery(query);
	     * 
	     * res.first(); match_expe_id[1] = res.getInt("MAX(id)");
	     */

	    res.close();
	    stat.close();

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// return match_expe_id;
    }

    public void insertElements(HashMap<Integer, Object[]> elements) {

	if (elements.size() == 0)
	    return;

	try {

	    conn.setAutoCommit(false);

	    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO elements (id, name, schema_id, type) VALUES ( ? , ? , ? , ? )"
		    + " ON DUPLICATE KEY UPDATE name = ? , schema_id = ? , type = ? ;");

	    for (Object key : elements.keySet().toArray()) {

		Object[] element = elements.get(key);

		pstmt.setString(1, element[0].toString());
		pstmt.setString(2, element[1].toString().replaceAll("'", "\\\\'"));
		pstmt.setString(3, element[2].toString());

		String type = new String();
		if (element[3] instanceof Entity)
		    type = "entity";
		else if (element[3] instanceof Attribute)
		    type = "attribute";
		else if (element[3] instanceof Relationship)
		    type = "relation";

		pstmt.setString(4, type);

		pstmt.setString(5, element[1].toString().replaceAll("'", "\\\\'"));
		pstmt.setString(6, element[2].toString());
		pstmt.setString(7, type);

		pstmt.executeUpdate();

	    }

	    conn.setAutoCommit(true);
	    pstmt.close();

	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}

    }

    public void insertInDB(ArrayList<Object[]> toInsert) {

	if (toInsert.size() == 0)
	    return;

	try {

	    conn.setAutoCommit(false);

	    PreparedStatement pstmt = conn
		    .prepareStatement("INSERT INTO match_results (id_schema1, id_element1, id_schema2, id_element2, " + (String) (toInsert.get(0)[4])
			    + ") VALUES ( ? , ? , ? , ? , ? )" + " ON DUPLICATE KEY UPDATE " + (String) (toInsert.get(0)[4]) + " = ? ;");

	    for (Object[] obj : toInsert) {

		String schema_id1;
		String element_id1;
		String schema_id2;
		String element_id2;

		if (((Schema) obj[0]).getId().intValue() < ((Schema) obj[1]).getId().intValue()) {
		    schema_id1 = ((Schema) obj[0]).getId().toString();
		    element_id1 = ((SchemaElement) obj[2]).getId().toString();
		    schema_id2 = ((Schema) obj[1]).getId().toString();
		    element_id2 = ((SchemaElement) obj[3]).getId().toString();
		} else {
		    schema_id2 = ((Schema) obj[0]).getId().toString();
		    element_id2 = ((SchemaElement) obj[2]).getId().toString();
		    schema_id1 = ((Schema) obj[1]).getId().toString();
		    element_id1 = ((SchemaElement) obj[3]).getId().toString();
		}

		double score;

		if (((Double) obj[5]).isNaN() || ((Double) obj[5]).isInfinite())
		    score = 0.0;
		else
		    score = ((Double) obj[5]).doubleValue();

		pstmt.setString(1, schema_id1);
		pstmt.setString(2, element_id1);
		pstmt.setString(3, schema_id2);
		pstmt.setString(4, element_id2);
		pstmt.setDouble(5, score);
		pstmt.setDouble(6, score);

		pstmt.executeUpdate();

	    }

	    conn.setAutoCommit(true);
	    pstmt.close();

	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}
    }

    /**
     * We insert with left schema_id < right schema_id
     * 
     * @param ls
     * @param rs
     * @param lse
     * @param rse
     * @param selectedMatcher
     * @param score
     */

    public void insertInDB(Schema ls, Schema rs, SchemaElement lse, SchemaElement rse, String selectedMatcher,/*
													       * int
													       * selectedMatcherId
													       * ,
													       * int
													       * expeId
													       * ,
													       */double score) {

	// now we go on and insert each scores with the good values
	try {
	    java.sql.Statement stat = conn.createStatement();

	    int schema_id1;
	    int schema_id2;
	    int element_id1;
	    int element_id2;

	    if (ls.getId().intValue() < rs.getId().intValue()) {
		schema_id1 = ls.getId().intValue();
		schema_id2 = rs.getId().intValue();
		element_id1 = lse.getId().intValue();
		element_id2 = rse.getId().intValue();
	    } else {
		schema_id2 = ls.getId().intValue();
		schema_id1 = rs.getId().intValue();
		element_id2 = lse.getId().intValue();
		element_id1 = rse.getId().intValue();
	    }

	    String query = new String();

	    // first the nodes
	    query = "REPLACE INTO elements SET schema_id = '" + ls.getId() + "', name = '" + lse.getName().replaceAll("'", "\\\\'") + "' , id = '"
		    + lse.getId() + "';";
	    stat.executeUpdate(query);

	    query = "REPLACE INTO elements SET schema_id = '" + rs.getId() + "', name = '" + rse.getName().replaceAll("'", "\\\\'") + "' , id = '"
		    + rse.getId() + "';";
	    stat.executeUpdate(query);
	    // ok for elements

	    // now combined elements
	    /*
	     * query = "INSERT INTO combined_elements SET left_node = '" +
	     * lse.getId() + "', right_node = '" + rse.getId() + "';"; try {
	     * stat.executeUpdate(query); } catch (Exception ex) {
	     * 
	     * }
	     * 
	     * // get the id of combined elements query =
	     * "SELECT id FROM combined_elements WHERE left_node = '" +
	     * lse.getId() + "' AND right_node = '" + rse.getId() + "';";
	     * ResultSet res = stat.executeQuery(query);
	     * 
	     * res.next(); int combId = res.getInt("id");
	     * 
	     * // then set the score query =
	     * "REPLACE INTO results SET id_combined_elements = '" + combId +
	     * "' , id_matching_tech = '" + selectedMatcherId +
	     * "' , id_expe = '" + expeId + "' , score = '" + score + "';";
	     * stat.executeUpdate(query);
	     */

	    // is the node existing

	    query = "SELECT * FROM match_results WHERE id_element1 = '" + element_id1 + "' AND id_element2 = '" + element_id2 + "';";

	    ResultSet res = stat.executeQuery(query);

	    if (res.next()) {
		query = "UPDATE match_results SET " + selectedMatcher + " = '" + score + "' WHERE id_schema1 = '" + schema_id1 + "' AND id_element1 = '"
			+ element_id1 + "' AND" + " id_schema2 = '" + schema_id2 + "' AND id_element2 = '" + element_id2 + "';";
	    } else {
		query = "INSERT INTO match_results (id_schema1, id_element1, id_schema2, id_element2, " + selectedMatcher + ") VALUES ('" + schema_id1 + "','"
			+ element_id1 + "','" + schema_id2 + "','" + element_id2 + "','" + score + "');";
	    }
	    stat.executeUpdate(query);

	    stat.close();

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
    }
}
