package org.inria.websmatch.evaluate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.inria.websmatch.db.Interval;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.db.Interval;
import org.inria.websmatch.db.MySQLDBConnector;

public class BuildNewMatchersV2 {

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

    public BuildNewMatchersV2() {
	
	MySQLDBConnector connector = new MySQLDBConnector();

	long time = System.currentTimeMillis();

	connector.connect();
	Statement stat = connector.getStat();
	// now go on with calculation

	int totalMatchCount = 0;

	String query = "CREATE TEMPORARY TABLE tmp_results AS SELECT * FROM view_validated_results;";

	try {

	    stat.executeUpdate(query);
	    
	    // we also have to delete stored intervals
	    stat.executeUpdate("DELETE FROM intervals;");
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
		stat = connector.getConn().createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
		stat.setFetchSize(Integer.MIN_VALUE);

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

		// ok now compute the probas
		int countTrue = 0;
		int total = 0;

		Map<Interval, Integer> nbpm = nbPossiblesMatches.get(tech);
		Map<Interval, Integer> nbtm = nbTrueMatches.get(tech);

		// add probas of for this tech
		probas.put(tech, new TreeMap<Interval, Double>());

		System.out.println(tech);
		
		double moy = 0.0;
		int possibTotal = 0;

		// stat = conn.createStatement();
		stat = connector.getConn().createStatement();
		for (Object key : nbpm.keySet()) {

		    Integer possib = nbpm.get(key);
		    total += possib;
		    System.out.println("Interval min : " + ((Interval) key).min + " max : " + ((Interval) key).max + " possibles matches : " + possib);

		    Integer tru = nbtm.get(key);
		    countTrue += tru;
		    System.out.println("Interval min : " + ((Interval) key).min + " max : " + ((Interval) key).max + " true matches : " + tru);

		    double prob = (double) tru / (double) possib;

		    probas.get(tech).put(((Interval) key), new Double(prob));
		    
		    // ok now we insert this interval		 
		    stat.executeUpdate("INSERT INTO intervals VALUES ('"+tech+"','"+((Interval) key).min+"','"+((Interval) key).max+"','"
			    +prob+"');");
			    //,'"+ tru +"','"+ possib +"','"+ totalMatchCount +"');");
		    
		    // ok calculate probas
		    possibTotal += possib;
		    moy = moy + (prob*possib);
		    
		}
		stat.close();

		System.out.println("Average : "+moy/possibTotal);
		System.out.println("Total matches : " + total + " Combinations were : " + totalMatchCount);
		System.out.println("True matches total : " + countTrue + " Experts was : " + experts);

		// ok go on probaRes and calculate
		Iterator<String> keys = probaRes.keySet().iterator();
		while (keys.hasNext()) {
		    String key = keys.next();
		    HashMap<String, Object> obj = probaRes.get(key);

		    SortedMap<Interval, Double> probRepart = probas.get(tech);

		    Interval inter;

		    if (((Double) obj.get(tech)).doubleValue() == 0.0)
			inter = probRepart.firstKey();
		    else if (((Double) obj.get(tech)).doubleValue() >= 1.0)
			inter = probRepart.lastKey();

		    else {
			Interval tmpInter = new Interval(((Double) obj.get(tech)).doubleValue(), ((Double) obj.get(tech)).doubleValue());
			inter = ((TreeMap<Interval, Double>) probRepart).lowerKey(tmpInter);
		    }

		    if (inter == null)
			inter = probRepart.firstKey();

		    obj.put("Proba_Max", Math.max(((Double) obj.get("Proba_Max")).doubleValue(), probRepart.get(inter).doubleValue()));
		    obj.put("Proba_Prod", ((Double) obj.get("Proba_Prod")).doubleValue() * probRepart.get(inter).doubleValue());

		    probaRes.put(key, obj);
		}

	    }

	    // we have the possibles matches and true matches for intervals
	    // print them

	    System.out.println("Intervals and probas ok in : " + (System.currentTimeMillis() - time) / 1000 + " s.");
	    // System.exit(0);
	    	  
	    // ok now we have all the probas, get elements and update them
	    // according to the max and prod

	    // we make statement by 500
	    Iterator<String> it = probaRes.keySet().iterator();
	    
	    // conn.setAutoCommit(false);
	    connector.getConn().setAutoCommit(false);
	    if(stat != null) stat.close();
	    
	    /*PreparedStatement pstmt = conn
		.prepareStatement("UPDATE match_results SET Proba_Max = ? , Proba_Prod = ? WHERE id_element1 = ? AND id_element2 = ? ;");*/
	    PreparedStatement pstmt = connector.getConn()
		.prepareStatement("UPDATE match_results SET Proba_Max = ? , Proba_Prod = ? WHERE id_element1 = ? AND id_element2 = ? ;");

	    String text = "\rProgress : ";// + Math.round(((double) count /
	      // (double) elementsSize) * 100) +
	      // "%";
	    System.out.print(text);
	    
	    int count = 0;
	    
	    while (it.hasNext()) {

		HashMap<String, Object> obj = probaRes.get(it.next());
				
		int elementsSize = probaRes.size();
		
		long percent = Math.round(((double) count / (double) elementsSize) * 100);
		
		// ok now we go on elements and calculate

		pstmt.setDouble(1, ((Double) obj.get("Proba_Max")).doubleValue());
		pstmt.setDouble(2, ((Double) obj.get("Proba_Prod")).doubleValue());
		pstmt.setInt(3, ((Integer) obj.get("id_element1")).intValue());
		pstmt.setInt(4, ((Integer) obj.get("id_element2")).intValue());

		// pstmt.executeUpdate();
		pstmt.addBatch();

		count++;
		// if (!text.equals("\rProgress : " + Math.round(((double) count
		// / (double) elementsSize) * 100) + "%")) {
		if (percent != Math.round(((double) count / (double) elementsSize) * 100)) {
		    // text = "\rProgress : " + Math.round(((double) count /
		    // (double) elementsSize) * 100) + "%";
		    // System.out.print(text);
		    System.out.print(".");
		    percent = Math.round(((double) count / (double) elementsSize) * 100);
		}

		if (count % 500 == 0) {
		    System.out.println("\nExecuting update db.");
		    pstmt.executeBatch();
		    System.out.println("Db updated.");
		    //conn.setAutoCommit(true);
		    connector.getConn().setAutoCommit(true);
		    pstmt.close();
		    /*pstmt = conn
			.prepareStatement("UPDATE match_results SET Proba_Max = ? , Proba_Prod = ? WHERE id_element1 = ? AND id_element2 = ? ;");
		    conn.setAutoCommit(false);*/
		    pstmt = connector.getConn()
			.prepareStatement("UPDATE match_results SET Proba_Max = ? , Proba_Prod = ? WHERE id_element1 = ? AND id_element2 = ? ;");
		    connector.getConn().setAutoCommit(false);
		}
	    }
	    
	    // last update
	    System.out.println("\nExecuting update db.");
	    pstmt.executeBatch();
	    System.out.println("Db updated.");
	    //conn.setAutoCommit(true);
	    connector.getConn().setAutoCommit(true);
	    pstmt.close();
	    
	    System.out.println("End, processing time : " + (System.currentTimeMillis() - time) / 1000 + " s.");

	} catch (SQLException e1) {
	    e1.printStackTrace();
	}

	//close();
	connector.close();

    }

    public static void main(String[] args) {

	new BuildNewMatchersV2();

    }

}
