package org.inria.websmatch.matchers.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.inria.websmatch.db.Interval;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.dspl.Concept;
import org.inria.websmatch.utils.L;

public class ConceptMatcher {
    
    private String dbName;
    
    // for intervals
    private Map<String, SortedMap<Interval, Double>> probas = new HashMap<String, SortedMap<Interval, Double>>();
    
    public ConceptMatcher(String dbName){
	this.dbName = dbName;
	
	// load intervals
	// we load the intervals
	MySQLDBConnector connector = new MySQLDBConnector();
	connector.connect();

	// ok, get them
	try {
	    ResultSet res = connector.getStat().executeQuery("SELECT * FROM mongo_intervals ORDER BY tech;");

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
	    L.Error(e.getMessage(),e);
	}
	connector.close();
	//
    }
    
    public TreeMap<String,Float> match(Concept ccTmp, Concept newConcept){
	
	TreeMap<String,Float> results = new TreeMap<String,Float>();

	// using attribute matcher (combining string + wordnet)
	AttributeMatcher matcher = new AttributeMatcher("LinStoiloisBagForLabel");
	// AttributeMatcher matcher = new AttributeMatcher("GeneralBagForLabel");
	
	float matchScore = matcher.match(new String[] { ccTmp.getName(), "" }, new String[] { newConcept.getName().trim(), "" });
	// then get the freq type
	ConceptTypeMatcher typeMatcher = new ConceptTypeMatcher(dbName);
	float typeScore = typeMatcher.match(ccTmp.getType().trim(), newConcept.getType().trim());
	
	// then ib if needed (not if int)
	// @TODO problem on loading all instances
	float ibScore = 0;
	if (typeScore > 0 && !ccTmp.getType().trim().equals("numeric") && !newConcept.getType().trim().equals("numeric")) {
	    CollectionMatcher cMatcher = new CollectionMatcher();
	    
	    Set<String> set1 = ccTmp.getInstances();
	    String strSet1 = new String();
	    for(String s : set1) strSet1 += s + " ";
	    
	    Set<String> set2 = newConcept.getInstances();
	    String strSet2 = new String();
	    for(String s : set2) strSet2 += s + " ";
	    
	    ibScore = cMatcher.match(new String[] {ccTmp.getName().trim(),strSet1.trim()}, new String[] {newConcept.getName().trim(),strSet2.trim()});
	}

	// we have a score for matching, type and ib, strore them
	// L.Debug(ccTmp.getName()+" | "+newConcept.getName()+" | "+matchScore+" | "+typeScore+" | "+ibScore);
	// MongoDBConnector.getInstance().addConceptsMatchScores(ccTmp, newConcept, matchScore, typeScore, ibScore, dbName);

	results.put("stringWordnetScore", matchScore);
	results.put("typeScore", typeScore);
	results.put("ibScore", ibScore);
	/*results[0] = matchScore;
	results[1] = typeScore;
	results[2] = ibScore;*/
	
	// add the individual scores for string and wordnet
	// first string matcher
	matcher = new AttributeMatcher("GeneralBagForLabel");
	float stringScore = matcher.match(new String[] { ccTmp.getName(), "" }, new String[] { newConcept.getName().trim(), "" });
	results.put("stringScore", stringScore);
	//results[3] = stringScore;
	// then only wordnet
	matcher = new AttributeMatcher("Lin");
	// TODO fix this
	// float wordnetScore = matcher.match(new String[] { ccTmp.getName(), "" }, new String[] { newConcept.getName().trim(), "" });
	float wordnetScore = 0;
	results.put("wordnetScore", wordnetScore);
	//results[4] = wordnetScore;
	
	// calculate Proba_Prod
	Set<String> techs = probas.keySet();
	
	double proba_prod = 1;

	// for each tech
	for (String tech : techs) {

	    SortedMap<Interval, Double> probRepart = probas.get(tech);

	    Interval inter;

	    double scoreTech = new Double(results.get(tech));

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
	    
	    proba_prod = proba_prod * probRepart.get(inter).doubleValue();
	}
	// end of proba
	
	results.put("probaScore", new Float(proba_prod));
	
	return results;
	
	// match ok (à changer en utilisant les probas)
	// @TODO proba
	/*if (matchScore >= 0.6) {
	    ccTmp.setNbOccurences(ccTmp.getNbOccurences() + 1);
	    // update uris
	    List<String> newUris = ccTmp.getAlternativeUris();
	    newUris.add(object_id + "/" + attr.getAttributeValue("sheet") + "/" + attr.getAttributeValue("x") + "/" + attr.getAttributeValue("y"));
	    ccTmp.setAlternativeUris(newUris);
	    // update names
	    if (!ccTmp.getName().equals(attr.getChildText("name").trim()) && !ccTmp.getAlternativeNames().contains(attr.getChildText("name").trim())) {
		List<String> newNames = ccTmp.getAlternativeNames();
		newNames.add(attr.getChildText("name").trim());
		ccTmp.setAlternativeNames(newNames);
	    }
	    // update concept
	    currentConcepts.set(i, ccTmp);
	}
	// new concept
	else {
	    // to test
	    MongoDBConnector.getInstance().addOrUpdateConcept(newConcept, dbName);
	}*/
    }

}
