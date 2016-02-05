package org.inria.websmatch.benchmark;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.inria.websmatch.db.Interval;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.db.MySQLDBConnector;

public class BuildIntervalsBench {

    //public java.sql.Connection conn = null;
    //public java.sql.Statement stat = null;

    // Map<SortedSet<Interval>, Integer> nbPossiblesMatches = new
    // HashMap<SortedSet<Interval>, Integer>();
    // Map<SortedSet<Interval>, Integer> nbTrueMatches = new
    // HashMap<SortedSet<Interval>, Integer>();

    Map<String, SortedMap<Interval, Integer>> nbPossiblesMatches = new HashMap<String, SortedMap<Interval, Integer>>();
    Map<String, SortedMap<Interval, Integer>> nbTrueMatches = new HashMap<String, SortedMap<Interval, Integer>>();
    Map<String, SortedMap<Interval, Double>> probas = new HashMap<String, SortedMap<Interval, Double>>();

    ArrayList<HashMap<String, Object>> elements;
    ArrayList<HashMap<String, Object>> expertElements;

    public SortedSet<Interval> computeIntervals(SortedSet<Interval> splited, SortedSet<Interval> toSplit, double epsilon, String tech, int leftCount,
	    int leftExpertCount, int rightCount, int rightExpertCount) {

	if (toSplit.size() == 0) {
	    return splited;
	}

	else {

	    Interval inter = toSplit.first();
	    toSplit.remove(inter);

	    Interval inter1 = new Interval(inter.min, inter.getMid());
	    Interval inter2 = new Interval(inter.getMid(), inter.max);

	    int nb1 = 0;
	    int nb2 = 0;
	    int nbTrue1 = 0;
	    int nbTrue2 = 0;

	    int nbTotal = 0;
	    int nbTrueTotal = 0;

	    for (HashMap<String, Object> element : elements.subList(leftCount, elements.size() - rightCount)) {
		if (((Double) element.get(tech)).doubleValue() >= inter1.min && ((Double) element.get(tech)).doubleValue() <= inter1.max) {
		    nb1++;
		}
	    }

	    for (HashMap<String, Object> element : expertElements.subList(leftExpertCount, expertElements.size() - rightExpertCount)) {
		if (((Double) element.get(tech)).doubleValue() >= inter1.min && ((Double) element.get(tech)).doubleValue() <= inter1.max) {
		    nbTrue1++;
		}
	    }

	    for (HashMap<String, Object> element : elements.subList(leftCount, elements.size() - rightCount)) {
		if (((Double) element.get(tech)).doubleValue() >= inter.min && ((Double) element.get(tech)).doubleValue() <= inter.max) {
		    nbTotal++;
		}
	    }

	    for (HashMap<String, Object> element : expertElements.subList(leftExpertCount, expertElements.size() - rightExpertCount)) {
		if (((Double) element.get(tech)).doubleValue() >= inter.min && ((Double) element.get(tech)).doubleValue() <= inter.max) {
		    nbTrueTotal++;
		}
	    }

	    nb2 = nbTotal - nb1;
	    nbTrue2 = nbTrueTotal - nbTrue1;

	    if (nb1 == 0 || nb2 == 0 || (nbTrue1 == 0 && nbTrue2 == 0)
		    || (Math.abs(((double) nbTrue1 / (double) nb1) - ((double) nbTrue2 / (double) nb2)) < epsilon)) {
		splited.add(inter);

		if (nbPossiblesMatches.containsKey(tech)) {
		    nbPossiblesMatches.get(tech).put(inter, nb1 + nb2);
		} else {
		    SortedMap<Interval, Integer> tmp = new TreeMap<Interval, Integer>();
		    tmp.put(inter, nb1 + nb2);
		    nbPossiblesMatches.put(tech, tmp);
		}

		if (nbTrueMatches.containsKey(tech)) {
		    nbTrueMatches.get(tech).put(inter, nbTrue1 + nbTrue2);
		} else {
		    SortedMap<Interval, Integer> tmp = new TreeMap<Interval, Integer>();
		    tmp.put(inter, nbTrue1 + nbTrue2);
		    nbTrueMatches.put(tech, tmp);
		}

		if (toSplit.size() > 0) {
		    return this.computeIntervals(splited, toSplit, epsilon, tech, leftCount, leftExpertCount, rightCount, rightExpertCount);
		}

		return splited;
	    }

	    else {
		toSplit.add(inter1);
		toSplit.add(inter2);
		return this.computeIntervals(splited, toSplit, epsilon, tech, leftCount, leftExpertCount, rightCount, rightExpertCount);
	    }

	}
    }

    public BuildIntervalsBench() {
	
	MySQLDBConnector connector = new MySQLDBConnector();

	long time = System.currentTimeMillis();

	connector.connect();
	Statement stat = connector.getStat();
	// now go on with calculation

	int totalMatchCount = 0;

	String query = "DROP TABLE IF EXISTS tmp_results;";

	try {

	    stat.executeUpdate(query);
	    //
	    stat.executeUpdate("CREATE TABLE tmp_results AS SELECT * FROM view_validated_results;");
	    //
	    
	    ResultSet set = stat.executeQuery("SELECT COUNT(*) FROM tmp_results;");

	    if (set.next())
		totalMatchCount = set.getInt("COUNT(*)");

	    System.out.println("Total match count : " + totalMatchCount);

	    // now we get the techs
	    ArrayList<String> techs = new ArrayList<String>();

	    set.close();

	    set = stat.executeQuery("SELECT * FROM matching_techs");

	    while (set.next()) {
		techs.add(set.getString("name"));
	    }

	    // epsilon
	    double epsilon = 0;

	    // expert tuples
	    query = "SELECT COUNT(*) FROM tmp_results WHERE expert = '1'";

	    set.close();

	    set = stat.executeQuery(query);
	    set.next();
	    int experts = set.getInt("COUNT(*)");

	    System.out.println("Total experts count : " + experts);

	    epsilon = ((double) experts / ((double) totalMatchCount)) * 0.3;

	    // ok get all scores and work on it exclusively to avoid double
	    // round problems in db
	    set.close();
	    stat.close();

	    // ok get all the experts and work on it exclusively to avoid double
	    // round problems in db
	    // stat = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
	    stat = connector.getConn().createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
	    stat.setFetchSize(Integer.MIN_VALUE);	    

	    set = stat.executeQuery("SELECT * FROM tmp_results WHERE expert = 1");

	    expertElements = new ArrayList<HashMap<String, Object>>();

	    while (set.next()) {

		HashMap<String, Object> result = new HashMap<String, Object>();

		result.put("id_element1", set.getInt("id_element1"));
		result.put("id_element2", set.getInt("id_element2"));

		for (String tech : techs)
		    result.put(tech, set.getDouble(tech));

		expertElements.add(result);
	    }

	    HashMap<String, HashMap<String, Object>> probaRes = new HashMap<String, HashMap<String, Object>>();

	    // now for each tech
	    for (final String tech : techs) {

		//
		elements = new ArrayList<HashMap<String, Object>>();

		// stat = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
		stat.close();
		connector.close();
		
		// intermediate time
		System.out.println("Intermediate time : " + (System.currentTimeMillis() - time) / 1000 + " s.");
		
		connector.connect();
		// stat = connector.getConn().createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
		stat = connector.getStat();
		// stat.setFetchSize(Integer.MIN_VALUE);
		
		set = stat.executeQuery("SELECT id_element1, id_element2, " + tech + " FROM tmp_results");

		while (set.next()) {

		    // add the node with key id1-id2
		    HashMap<String, Object> result = new HashMap<String, Object>();

		    result.put("id_element1", set.getInt("id_element1"));
		    result.put("id_element2", set.getInt("id_element2"));
		    result.put(tech, set.getDouble(tech));

		    if (probaRes.get(set.getInt("id_element1") + "-" + set.getInt("id_element2")) == null) {
			result.put("Proba_Max", new Double(0.0));
			result.put("Proba_Prod", new Double(1.0));
		    } else {
			result.put("Proba_Max", probaRes.get(set.getInt("id_element1") + "-" + set.getInt("id_element2")).get("Proba_Max"));
			result.put("Proba_Prod", probaRes.get(set.getInt("id_element1") + "-" + set.getInt("id_element2")).get("Proba_Prod"));
		    }

		    probaRes.put(set.getInt("id_element1") + "-" + set.getInt("id_element2"), result);

		    HashMap<String, Object> score = new HashMap<String, Object>();
		    score.put(tech, set.getDouble(tech));

		    elements.add(score);

		}
		set.close();
		stat.close();
		//

		int leftCount = 0;
		int leftExpertCount = 0;
		int rightCount = 0;
		int rightExpertCount = 0;

		// first get the end
		// descending sort
		Collections.sort(elements, new Comparator<HashMap<String, Object>>() {

		    @Override
		    public int compare(HashMap<String, Object> arg0, HashMap<String, Object> arg1) {

			Double darg0 = (Double) arg0.get(tech);
			Double darg1 = (Double) arg1.get(tech);

			return darg1.compareTo(darg0);

		    }

		});

		// descending sort
		Collections.sort(expertElements, new Comparator<HashMap<String, Object>>() {

		    @Override
		    public int compare(HashMap<String, Object> arg0, HashMap<String, Object> arg1) {

			Double darg0 = (Double) arg0.get(tech);
			Double darg1 = (Double) arg1.get(tech);

			return darg1.compareTo(darg0);

		    }

		});

		int nb1 = 0;
		int nbTrue1 = 0;

		double end = 1.0;

		// get last score < 1
		for (HashMap<String, Object> element : elements) {
		    if (((Double) element.get(tech)).doubleValue() < 1) {
			end = ((Double) element.get(tech)).doubleValue();
			break;
		    } else
			nb1++;
		}

		for (HashMap<String, Object> element : expertElements) {
		    if (((Double) element.get(tech)).doubleValue() < 1) {
			break;
		    } else
			nbTrue1++;
		}

		// ascending sort
		Collections.sort(elements, new Comparator<HashMap<String, Object>>() {

		    @Override
		    public int compare(HashMap<String, Object> arg0, HashMap<String, Object> arg1) {

			Double darg0 = (Double) arg0.get(tech);
			Double darg1 = (Double) arg1.get(tech);

			return darg0.compareTo(darg1);

		    }

		});

		double begin = 0.0;
		int nb = 0;

		// get first score > 0
		for (HashMap<String, Object> element : elements) {
		    if (((Double) element.get(tech)).doubleValue() > 0) {
			begin = ((Double) element.get(tech)).doubleValue();
			break;
		    } else
			nb++;
		}

		// same with experts
		int nbTrue = 0;

		// ascending sort
		Collections.sort(expertElements, new Comparator<HashMap<String, Object>>() {

		    @Override
		    public int compare(HashMap<String, Object> arg0, HashMap<String, Object> arg1) {

			Double darg0 = (Double) arg0.get(tech);
			Double darg1 = (Double) arg1.get(tech);

			return darg0.compareTo(darg1);

		    }

		});

		//
		for (HashMap<String, Object> element : expertElements) {
		    if (((Double) element.get(tech)).doubleValue() > 0) {
			break;
		    } else
			nbTrue++;
		}

		leftCount = nb;
		leftExpertCount = nbTrue;
		rightCount = nb1;
		rightExpertCount = nbTrue1;

		Interval inter0 = new Interval(0.0, 0.0);
		Interval inter1 = new Interval(1.0, 1.0);

		Interval interMid = new Interval(new Double(begin).doubleValue(), new Double(end).doubleValue());

		SortedSet<Interval> toSplit = new TreeSet<Interval>();
		toSplit.add(interMid);

		SortedSet<Interval> splited = new TreeSet<Interval>();

		SortedMap<Interval, Integer> tmp = new TreeMap<Interval, Integer>();
		tmp.put(inter0, nb);
		nbPossiblesMatches.put(tech, tmp);

		tmp = new TreeMap<Interval, Integer>();
		tmp.put(inter0, nbTrue);
		nbTrueMatches.put(tech, tmp);
		//

		splited = this.computeIntervals(splited, toSplit, epsilon, tech, leftCount, leftExpertCount, rightCount, rightExpertCount);

		nbPossiblesMatches.get(tech).put(inter1, nb1);
		nbTrueMatches.get(tech).put(inter1, nbTrue1);
		//

	    }

	    // we have the possibles matches and true matches for intervals
	    // print them

	    System.out.println("Intervals ok in : " + (System.currentTimeMillis() - time) / 1000 + " s.");
	    // System.exit(0);
	    	 
	} catch (SQLException e1) {
	    L.Error(e1.getMessage(),e1);
	}

	//close();
	connector.close();

    }

    public static void main(String[] args) {

	new BuildIntervalsBench();

    }

}
