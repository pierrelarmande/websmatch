package org.inria.websmatch.evaluate;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.inria.websmatch.db.Interval;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.db.Interval;
import org.inria.websmatch.utils.L;

@Deprecated
public class BuildNewMatchers {

    public java.sql.Connection conn = null;
    public java.sql.Statement stat = null;

    // Map<SortedSet<Interval>, Integer> nbPossiblesMatches = new
    // HashMap<SortedSet<Interval>, Integer>();
    // Map<SortedSet<Interval>, Integer> nbTrueMatches = new
    // HashMap<SortedSet<Interval>, Integer>();

    Map<String, SortedMap<Interval, Integer>> nbPossiblesMatches = new HashMap<String, SortedMap<Interval, Integer>>();
    Map<String, SortedMap<Interval, Integer>> nbTrueMatches = new HashMap<String, SortedMap<Interval, Integer>>();
    Map<String, SortedMap<Interval, Double>> probas = new HashMap<String, SortedMap<Interval, Double>>();

    // private static double hack = 0.000000000000001;
    private static double hack = 0.00000001;

    private void connect() {

	// db
	String dbuser = "matcher";
	String pass = "matcher";
	String dbHost = "localhost";
	int dbPort = 3306;
	String dbName = "matching_results";

	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	} catch (InstantiationException e) {
	    L.Error(e.getMessage(), e);
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
	    //stat = conn.createStatement();
	    stat = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
	              java.sql.ResultSet.CONCUR_READ_ONLY);
	    stat.setFetchSize(Integer.MIN_VALUE);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

    }

    private void close() {

	// close
	try {
	   if(stat != null) stat.close();
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

    public SortedSet<Interval> computeIntervals(SortedSet<Interval> splited, SortedSet<Interval> toSplit, double epsilon, String tech) {

	if (toSplit.size() == 0) {
	    return splited;
	}

	else {

	    Interval inter = toSplit.first();
	    toSplit.remove(inter);

	    String query;

	    // hacky, to correct
	    /*
	     * Interval inter1 = new Interval(inter.min - 0.0000001,
	     * inter.getMid() + 0.0000001); Interval inter2 = new
	     * Interval(inter.getMid() - 0.0000001, inter.max + 0.0000001);
	     */
	    Interval inter1 = new Interval(inter.min, inter.getMid() + hack);
	    Interval inter2 = new Interval(inter.getMid() + hack, inter.max);

	    // SQL
	    query = "SELECT COUNT(*) FROM tmp_results WHERE " + tech + " > 0 AND " + tech + " BETWEEN " + inter1.min + " AND " + inter1.max;

	    int nb1 = 0;
	    int nb2 = 0;
	    int nbTrue1 = 0;
	    int nbTrue2 = 0;

	    try {
		stat.close();
		
		stat = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
		              java.sql.ResultSet.CONCUR_READ_ONLY);
		stat.setFetchSize(Integer.MIN_VALUE);
			
		ResultSet set = stat.executeQuery(query);
		set.next();
		nb1 = set.getInt("COUNT(*)");

		/*
		 * set = stat
		 * .executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " +
		 * tech + " < 1 AND " + tech + " BETWEEN " + inter2.min +
		 * " AND " + inter2.max); set.next(); nb2 =
		 * set.getInt("COUNT(*)");
		 */
		set.close();
		set = stat.executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " + tech + " > 0 AND " + tech + " BETWEEN " + inter1.min + " AND " + inter1.max
			+ " AND expert = '1'");

		set.next();
		nbTrue1 = set.getInt("COUNT(*)");

		/*
		 * set =
		 * stat.executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " +
		 * tech + " < 1 AND " + tech + " BETWEEN " + inter2.min +
		 * " AND " + inter2.max + " AND expert = '1'");
		 * 
		 * set.next(); nbTrue2 = set.getInt("COUNT(*)");
		 */

		// ok get total tuples on inter
		set.close();
		set = stat.executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " + tech + " > 0 AND " + tech + " < 1 AND " + tech + " BETWEEN " + inter.min
			+ " AND " + inter.max);
		set.next();
		int nbTotal = set.getInt("COUNT(*)");

		set.close();
		set = stat.executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " + tech + " > 0 AND " + tech + " < 1 AND " + tech + " BETWEEN " + inter.min
			+ " AND " + inter.max + " AND expert = 1");
		set.next();
		int nbTrueTotal = set.getInt("COUNT(*)");

		nb2 = nbTotal - nb1;
		nbTrue2 = nbTrueTotal - nbTrue1;

	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }

	    /*
	     * System.out.println("nbTrue1 : " + nbTrue1);
	     * System.out.println("nb1 : " + nb1);
	     * 
	     * System.out.println("nbTrue2 : " + nbTrue2);
	     * System.out.println("nb2 : " + nb2);
	     * 
	     * System.out.println(Math.abs(((double) nbTrue1 / (double) nb1) -
	     * ((double) nbTrue2 / (double) nb2)));
	     */

	    /*
	     * if(nb1 == 0){ if(toSplit.size() == 0) return splited; else{
	     * //if(nb2 > 0) toSplit.add(inter2); return
	     * this.computeIntervals(splited, toSplit, epsilon, tech); } }
	     * 
	     * if(nb2 == 0){ if(toSplit.size() == 0) return splited; else{
	     * //if(nb1 > 0) toSplit.add(inter1); return
	     * this.computeIntervals(splited, toSplit, epsilon, tech); } }
	     */

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

		if (toSplit.size() > 0)
		    return this.computeIntervals(splited, toSplit, epsilon, tech);

		return splited;
	    }

	    else {
		toSplit.add(inter1);
		toSplit.add(inter2);
		return this.computeIntervals(splited, toSplit, epsilon, tech);
	    }

	}
    }

    public BuildNewMatchers() {

	long time = System.currentTimeMillis();

	connect();
	// now go on with calculation

	int totalMatchCount = 0;

	String query = "CREATE TEMPORARY TABLE tmp_results AS SELECT * FROM view_validated_results;";

	try {

	    stat.executeUpdate(query);

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

	    System.out.println("Epsilon : " + epsilon);

	    // test only Stoilos_JW
	    /*
	     * techs = new ArrayList<String>(); techs.add("Stoilos_JW");
	     */

	    // now for each tech
	    for (String tech : techs) {

		Interval inter0 = new Interval(0, 0);
		Interval inter1 = new Interval(1, 1);

		// get first score > 0
		query = "SELECT " + tech + " FROM tmp_results WHERE " + tech + " > 0 ORDER BY " + tech + " LIMIT 1";

		set.close();
		set = stat.executeQuery(query);
		set.next();
		double begin = set.getDouble(tech);

		// hack, problem with MySQL double
		if ((begin - hack) > 0.0)
		    begin = begin - hack;

		// get last score < 1
		query = "SELECT " + tech + " FROM tmp_results WHERE " + tech + " < 1 ORDER BY " + tech + " DESC LIMIT 1";

		set.close();
		set = stat.executeQuery(query);
		set.next();
		double end = set.getDouble(tech);

		// hack, problem with MySQL double
		if ((end + hack) < 1.0)
		    end = end + hack;

		Interval interMid = new Interval(begin, end);

		SortedSet<Interval> toSplit = new TreeSet<Interval>();
		toSplit.add(interMid);

		SortedSet<Interval> splited = new TreeSet<Interval>();

		// we add the 0 count
		set.close();
		set = stat.executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " + tech + " <= 0");
		set.next();
		int nb = set.getInt("COUNT(*)");

		set.close();
		set = stat.executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " + tech + " <= 0 AND expert = '1'");
		set.next();
		int nbTrue = set.getInt("COUNT(*)");

		SortedMap<Interval, Integer> tmp = new TreeMap<Interval, Integer>();
		tmp.put(inter0, nb);
		nbPossiblesMatches.put(tech, tmp);

		tmp = new TreeMap<Interval, Integer>();
		tmp.put(inter0, nbTrue);
		nbTrueMatches.put(tech, tmp);
		//

		splited = this.computeIntervals(splited, toSplit, epsilon, tech);

		// we add the 1 count
		set.close();
		
		stat.close();
		stat = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
		              java.sql.ResultSet.CONCUR_READ_ONLY);
		stat.setFetchSize(Integer.MIN_VALUE);
		
		set = stat.executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " + tech + " >= 1");
		set.next();
		nb = set.getInt("COUNT(*)");

		set.close();
		set = stat.executeQuery("SELECT COUNT(*) FROM tmp_results WHERE " + tech + " >= 1 AND expert = '1'");
		set.next();
		nbTrue = set.getInt("COUNT(*)");

		nbPossiblesMatches.get(tech).put(inter1, nb);
		nbTrueMatches.get(tech).put(inter1, nbTrue);
		//

	    }

	    // we have the possibles matches and true matches for intervals
	    // print them

	    for (String tech : techs) {

		int countTrue = 0;
		int total = 0;

		Map<Interval, Integer> nbpm = nbPossiblesMatches.get(tech);
		Map<Interval, Integer> nbtm = nbTrueMatches.get(tech);

		// add probas of for this tech
		probas.put(tech, new TreeMap<Interval, Double>());

		System.out.println(tech);

		for (Object key : nbpm.keySet()) {

		    Integer possib = nbpm.get(key);
		    total += possib;
		    System.out.println("Interval min : " + ((Interval) key).min + " max : " + ((Interval) key).max + " possibles matches : " + possib);

		    Integer tru = nbtm.get(key);
		    countTrue += tru;
		    System.out.println("Interval min : " + ((Interval) key).min + " max : " + ((Interval) key).max + " true matches : " + tru);

		    double prob = (double) tru / (double) possib;

		    probas.get(tech).put(((Interval) key), new Double(prob));
		}

		/*
		 * for (Object key : nbtm.keySet()) {
		 * 
		 * Integer value = nbtm.get(key); countTrue += value;
		 * System.out.println("Interval min : " + ((Interval) key).min +
		 * " max : " + ((Interval) key).max + " true matches : " +
		 * value); }
		 */

		System.out.println("Total matches : " + total + " Combinations were : " + totalMatchCount);
		System.out.println("True matches total : " + countTrue + " Experts was : " + experts);

	    }

	    System.out.println("Intervals and probas ok in : " + (System.currentTimeMillis() - time) / 1000 + " s.");

	    // ok now we have all the probas, get elements and update them
	    // according to the max and prod

	    //int i = 1;
	    int i = 0;

	    while (i <= 3600000) {
		
		stat = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
		              java.sql.ResultSet.CONCUR_READ_ONLY);
		stat.setFetchSize(Integer.MIN_VALUE);
		
		if(set != null) set.close();
		set = stat.executeQuery("SELECT * FROM tmp_results LIMIT " + i + "," + (i + 99999));

		i += 100000;

		ArrayList<HashMap<String, Object>> elements = new ArrayList<HashMap<String, Object>>();

		while (set.next()) {

		    HashMap<String, Object> result = new HashMap<String, Object>();

		    result.put("id_element1", set.getInt("id_element1"));
		    result.put("id_element2", set.getInt("id_element2"));

		    for (String tech : techs)
			result.put(tech, set.getDouble(tech));

		    elements.add(result);

		}

		conn.setAutoCommit(false);

		PreparedStatement pstmt = conn
			.prepareStatement("UPDATE match_results SET Proba_Max = ? , Proba_Prod = ? WHERE id_element1 = ? AND id_element2 = ? ;");

		int elementsSize = elements.size();
		int count = 0;
		String text = "\rProgress : ";// + Math.round(((double) count /
					      // (double) elementsSize) * 100) +
					      // "%";
		long percent = Math.round(((double) count / (double) elementsSize) * 100);
		System.out.print(text);
		
		// ok now we go on elements and calculate
		for (HashMap<String, Object> obj : elements) {

		    double max = 0;
		    double prod = 1;

		    for (String tech : techs) {

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

			/*
			 * System.out.println("Value : " + ((Double)
			 * obj.get(tech)).doubleValue());
			 * System.out.println("Inter min : " + inter.min +
			 * " Inter max : " + inter.max);
			 */

			max = Math.max(max, probRepart.get(inter));
			prod = prod * probRepart.get(inter);

			/*
			 * for(Interval inter : probRepart.keySet()){
			 * if(((Double)obj.get(tech)).doubleValue() <= inter.max
			 * && ((Double)obj.get(tech)).doubleValue() >=
			 * inter.min){
			 * 
			 * max = Math.max(max, probRepart.get(inter)); if(prod
			 * == 0){ prod = probRepart.get(inter); }else{ prod =
			 * prod * probRepart.get(inter); }
			 * 
			 * break; } }
			 */

			// update the element
			/*
			 * obj.put("Proba_Max", max); obj.put("Proba_Prod",
			 * prod);
			 */
		    }

		    pstmt.setDouble(1, max);
		    pstmt.setDouble(2, prod);
		    pstmt.setInt(3, ((Integer) obj.get("id_element1")).intValue());
		    pstmt.setInt(4, ((Integer) obj.get("id_element2")).intValue());

		    // pstmt.executeUpdate();
		    pstmt.addBatch();

		    count++;
		    // if (!text.equals("\rProgress : " + Math.round(((double)
		    // count / (double) elementsSize) * 100) + "%")) {
		    if (percent != Math.round(((double) count / (double) elementsSize) * 100)) {
			// text = "\rProgress : " + Math.round(((double) count /
			// (double) elementsSize) * 100) + "%";
			// System.out.print(text);
			System.out.print(".");
			percent = Math.round(((double) count / (double) elementsSize) * 100);
		    }

		}

		System.out.println("\nExecuting update db.");
		pstmt.executeBatch();
		System.out.println("Db updated.");
		conn.setAutoCommit(true);
		pstmt.close();
		System.out.println("End, processing time : " + (System.currentTimeMillis() - time) / 1000 + " s.");
		// ok force GC
		pstmt = null;
		elements = null;
		set.close();
		set = null;
		stat.close();
		stat = null;

	    }
	} catch (SQLException e1) {
	    e1.printStackTrace();
	}

	close();

    }

    public static void main(String[] args) {

	new BuildNewMatchers();

    }

}
