package org.inria.websmatch.evaluate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.inria.websmatch.db.Interval;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.db.Interval;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.utils.L;
import org.mitre.harmony.matchers.ElementPair;
import org.mitre.harmony.matchers.MatcherScores;

public class ProbaMatching {

    private Map<String, SortedMap<Interval, Double>> probas = new HashMap<String, SortedMap<Interval, Double>>();
    private MySQLDBConnector connector;

    public ProbaMatching() {

	// we load the intervals
	connector = new MySQLDBConnector();
	connector.connect();

	// ok, get them
	try {
	    ResultSet res = connector.getStat().executeQuery("SELECT * FROM intervals ORDER BY tech;");

	    SortedMap<Interval, Double> intervals = new TreeMap<Interval, Double>();
	    String currentTech = new String();

	    while (res.next()) {

		String tmpTech = res.getString("tech");

		// new tech
		if (!tmpTech.equals(currentTech)) {

		    if (!currentTech.equals(""))
			probas.put(currentTech, intervals);
		    intervals = new TreeMap<Interval, Double>();

		    intervals.put(new Interval(res.getDouble("begin"), res.getDouble("end")), res.getDouble("proba"));
		    currentTech = tmpTech;
		}

		// same tech
		intervals.put(new Interval(res.getDouble("begin"), res.getDouble("end")), res.getDouble("proba"));

	    }

	    probas.put(currentTech, intervals);

	} catch (SQLException e) {
	    L.Error(e.getMessage(), e);
	}

    }

    public void printIntervals() {
	Set<String> techs = probas.keySet();

	for (String tech : techs) {

	    SortedMap<Interval, Double> inter = probas.get(tech);
	    Set<Interval> interKey = inter.keySet();

	    for (Interval in : interKey) {

		System.out.println(tech + " min : " + in.min + " max : " + in.max + " proba : " + inter.get(in));

	    }

	}
    }

    public static void main(String[] args) {

	new ProbaMatching().printIntervals();

    }

    /**
     * Method to match 2 schemas using databases results
     * 
     * @param id1
     * @param id2
     */
    public void matchSchemas(int id1, int id2) {

	int leftSchema = -1;
	int rightSchema = -1;

	if (id1 < id2) {
	    leftSchema = id1;
	    rightSchema = id2;
	}

	else if (id2 < id1) {
	    leftSchema = id2;
	    rightSchema = id1;
	}

	// to have best speed results, we need the id elements first
	ArrayList<Object[]> leftElements = new ArrayList<Object[]>();
	ArrayList<Object[]> rightElements = new ArrayList<Object[]>();

	// System.out.println("Proba match : " + leftSchema + " " + rightSchema);

	try {

	    connector.getStat().close();
	    Statement stat = connector.getConn().createStatement();
	    connector.setStat(stat);

	    ResultSet set = connector.getStat().executeQuery("SELECT id, type FROM elements WHERE schema_id = '" + leftSchema + "' ;");

	    while (set.next()) {

		leftElements.add(new Object[] { new Integer(set.getInt("id")), new String(set.getString("type")) });

	    }

	    // same with right
	    set = connector.getStat().executeQuery("SELECT id, type FROM elements WHERE schema_id = '" + rightSchema + "' ;");

	    while (set.next()) {

		rightElements.add(new Object[] { new Integer(set.getInt("id")), new String(set.getString("type")) });

	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// now we compute
	for (Object[] left : leftElements) {

	    for (Object[] right : rightElements) {

		if (((String) left[1]).equals(((String) right[1]))) {

		    // same type, it must be in the db
		    double proba_max = 0.0;
		    double proba_prod = 1.0;

		    // ok get the results and update max and prod
		    try {
			ResultSet set = connector.getStat().executeQuery(
				"SELECT * FROM match_results WHERE id_schema1 = '" + leftSchema + "' AND" + " id_schema2 = '" + rightSchema
					+ "' AND id_element1 = '" + (Integer) left[0] + "' AND id_element2 = '" + (Integer) right[0] + "';");

			if (set.next()) {

			    Set<String> techs = probas.keySet();

			    // for each tech
			    for (String tech : techs) {

				SortedMap<Interval, Double> probRepart = probas.get(tech);

				Interval inter;

				double scoreTech = set.getDouble(tech);

				if (scoreTech == 0.0)
				    inter = probRepart.firstKey();
				else if (scoreTech >= 1.0)
				    inter = probRepart.lastKey();

				else {
				    Interval tmpInter = new Interval(scoreTech, scoreTech);
				    inter = ((TreeMap<Interval, Double>) probRepart).lowerKey(tmpInter);
				}

				if (inter == null)
				    inter = probRepart.firstKey();

				proba_max = Math.max(proba_max, probRepart.get(inter).doubleValue());
				proba_prod = proba_prod * probRepart.get(inter).doubleValue();
			    }

			    // update probas
			    connector.getStat().executeUpdate(
				    "UPDATE match_results SET Proba_Max = '" + proba_max + "' , Proba_Prod = '" + proba_prod + "'" + " WHERE id_element1 = '"
					    + (Integer) left[0] + "' AND id_element2 = '" + (Integer) right[0] + "';");
			}
		    } catch (SQLException e) {
			L.Error(e.getMessage(),e);
		    }
		}
	    }
	}
    }

    /**
     * Method for matching scores using argument map
     * 
     * @param id1
     * @param id2
     * @param scoresByTech
     * @param avgProba
     *            The double value to keep and insert
     */

    public void matchSchemas(int id1, int id2, HashMap<String, MatcherScores> scoresByTech, double avgProba) {

	int leftSchema = -1;
	int rightSchema = -1;
	boolean invertElements = false;

	if (id1 < id2) {
	    leftSchema = id1;
	    rightSchema = id2;
	}

	else if (id2 < id1) {
	    leftSchema = id2;
	    rightSchema = id1;
	    invertElements = true;
	}

	// to have best speed results, we need the id elements first
	// Object[] contains : elementPair, HashMap<String,Double> for scores by
	// tech for the couple, Proba_Max, Proba_Prod

	// System.out.println("Proba match : " + leftSchema + " " + rightSchema);

	// ok create all objects
	// we need to get technics (so keys of HashMap)
	Set<String> techSet = scoresByTech.keySet();
	Iterator<String> itTech = techSet.iterator();
	ArrayList<Object[]> elements = new ArrayList<Object[]>();

	// first we get the pairs
	String technic = itTech.next();
	Set<String> localTechSet = scoresByTech.keySet();

	MatcherScores matches = scoresByTech.get(technic);

	// for each couple
	for (ElementPair pair : matches.getElementPairs()) {

	    Object[] values = new Object[2];
	    values[0] = pair;

	    HashMap<String, Double> scoresForPair = new HashMap<String, Double>();
	    scoresForPair.put(technic, matches.getScore(pair).getPositiveEvidence());

	    // then we create the scores
	    for (String localTechnic : localTechSet) {

		if (!localTechnic.equals(technic)) {
		    MatcherScores localMatches = scoresByTech.get(localTechnic);
		    scoresForPair.put(localTechnic, localMatches.getScore(pair).getPositiveEvidence());
		}
	    }

	    values[1] = scoresForPair;
	    elements.add(values);

	}

	// now we compute
	for (Object[] ele : elements) {

	    ElementPair pair = (ElementPair) ele[0];
	    @SuppressWarnings("unchecked")
	    HashMap<String, Double> scores = (HashMap<String, Double>) ele[1];

	    // same type, it must be in the db
	    double proba_max = 0.0;
	    double proba_prod = 1.0;

	    Set<String> techs = scores.keySet();

	    // for each tech
	    for (String tech : techs) {

		SortedMap<Interval, Double> probRepart = probas.get(tech);

		Interval inter;

		double scoreTech = scores.get(tech);

		if (scoreTech == 0.0)
		    inter = probRepart.firstKey();
		else if (scoreTech >= 1.0)
		    inter = probRepart.lastKey();

		else {
		    Interval tmpInter = new Interval(scoreTech, scoreTech);
		    inter = ((TreeMap<Interval, Double>) probRepart).lowerKey(tmpInter);
		}

		if (inter == null)
		    inter = probRepart.firstKey();

		proba_max = Math.max(proba_max, probRepart.get(inter).doubleValue());
		proba_prod = proba_prod * probRepart.get(inter).doubleValue();
	    }

	    // update probas
	    if (proba_prod >= avgProba) {

		String query = "INSERT INTO match_results (id_schema1, id_element1, id_schema2, id_element2, ";
		String endQuery = " VALUES (";

		if (invertElements) {

		    endQuery += "'" + leftSchema + "', ";
		    endQuery += "'" + pair.getTargetElement() + "', ";
		    endQuery += "'" + rightSchema + "', ";
		    endQuery += "'" + pair.getSourceElement() + "', ";

		} else {

		    endQuery += "'" + leftSchema + "', ";
		    endQuery += "'" + pair.getSourceElement() + "', ";
		    endQuery += "'" + rightSchema + "', ";
		    endQuery += "'" + pair.getTargetElement() + "', ";

		}

		// for each tech
		for (String localTech : techs) {

		    query += localTech + ", ";
		    if(!Double.isNaN(scores.get(localTech))) endQuery += "'" + scores.get(localTech) + "', ";
		    else endQuery += "'0.0', ";

		}

		query += "Proba_Max, Proba_Prod)";
		endQuery += "'" + proba_max + "', '" + proba_prod + "') ON DUPLICATE KEY UPDATE ";
		
		// for duplicate key
		// for each tech
		for (String localTech : techs) {

		    if(!Double.isNaN(scores.get(localTech))) endQuery += localTech + " ='" + scores.get(localTech) + "', ";
		    else endQuery += localTech + " ='0.0', ";

		}
		
		endQuery+="Proba_Max ='"+proba_max+"', Proba_Prod ='"+proba_prod+"';";		

		// System.out.println(query+endQuery);
		
		try {		 
		    connector.getStat().executeUpdate(query + endQuery);
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    L.Error(e.getMessage(),e);
		}
	    }
	}
    }

    public void close() {

	connector.close();

    }

}
