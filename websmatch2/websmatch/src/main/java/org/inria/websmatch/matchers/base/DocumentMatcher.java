package org.inria.websmatch.matchers.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xml.WSMatchXMLLoader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

public class DocumentMatcher {
    
    private MatchersList list;
    private String[] matchers;
    
    public DocumentMatcher(){
	// use all technics for matching attributes
	list = MatchersList.getInstance();
	matchers = list.getMatchers();
    }
    
    /**
     * Match 2 documents using the individual label matchers (including descriptions) and instance based
     * 
     * @param strDoc1 First XML document
     * @param strDoc2 Second XML document
     * @return An hashmap with element1 - element2 and average score (using all techniques)
     */ 
    @SuppressWarnings("unchecked")
    public HashMap<Element[],Float> matchDocuments(String strDoc1, String strDoc2){
	
	// next, match the attributes
	WSMatchXMLLoader docLoader1 = new WSMatchXMLLoader(strDoc1);
	Document doc1 = docLoader1.getDocument();
	
	WSMatchXMLLoader docLoader2 = new WSMatchXMLLoader(strDoc2);
	Document doc2 = docLoader2.getDocument();
	
	// go throught attributes
	Filter attributeFilter = new ElementFilter("attribute", null);
	Iterator<Element> doc1Attr = doc1.getRootElement().getDescendants(attributeFilter);
			
	// storage for scores
	HashMap<Element[],Float> scores = new LinkedHashMap<Element[],Float>();
	
	// iterate through attributes
	// we use an average... to be enhanced
	while(doc1Attr.hasNext()){
	    Element attr1 = doc1Attr.next();
	    Iterator<Element> doc2Attr = doc2.getRootElement().getDescendants(attributeFilter);
	    while(doc2Attr.hasNext()){
		Element attr2 = doc2Attr.next();
		
		//
		float avg = 0;
		//
		for(int i = 0; i < matchers.length; i++){
		    AttributeMatcher matcher = new AttributeMatcher(matchers[i]);
		    avg = avg + matcher.match(new String[]{attr1.getChildText("name").trim(),""},new String[]{attr2.getChildText("name").trim(),""});
		}
		avg = avg/(float)matchers.length;
		scores.put(new Element[]{attr1,attr2}, avg);
	    }
	}
	// we have to choose the best matches
	
	//
	return scores;	
    }
    
    /**
     * Not based on scores, only counting shared concepts
     * 
     */
    
    public float computeDistance(String docId1, String docId2, String dbName){
	float distance = 0;
	
	// get concepts for eachs docs
	MongoDBConnector con = MongoDBConnector.getInstance();
	
	String[] doc1Concepts = con.getConceptIdsForDocument(docId1, dbName);
	String[] doc2Concepts = con.getConceptIdsForDocument(docId2, dbName);
	
	//
	double maxConceptsNb = Math.sqrt(Math.min(doc1Concepts.length, doc2Concepts.length));//Math.max(doc1Concepts.length, doc2Concepts.length);
		
	for(int i = 0; i < doc1Concepts.length; i++){
	    String id1C = doc1Concepts[i];
	    
	    boolean found = false;
	    for(int j = 0; j < doc2Concepts.length;j++){
		if(doc2Concepts[j].equals(id1C)){
		    // System.out.println("equals");
		    // get occurences
		    int nbOccur = con.getOccurencesForConcept(id1C, dbName);
		    distance += (float)1/(float)nbOccur;
		    found = true;
		}
		if(found) break;
	    }
	}
	
	distance = distance/(float)maxConceptsNb;
	// System.out.println(distance);
	//
	
	return distance;
    }
    
    /**
     * Compute a distance between the documents, based on matches
     * 
     * @param scores Hashmap containing element 1 - element 2 and scores (based on matches)
     * @return The distance between the documents
     */
    public float computeDistance(HashMap<Element[],Float> scores){
		
	// average
	float distance = 0;
	for(Element[] eles : scores.keySet()){
	    distance += scores.get(eles);
	}
	distance = distance/(float)scores.size();
	L.Debug(this.getClass().getSimpleName(),"Average distance "+distance,true);	
	
	// now the good way to compute distance
	int nbNodes1 = 0;
	int nbNodes2 = 0;
	
	// count them
	List<Element> le = new ArrayList<Element>();
	List<Element> re = new ArrayList<Element>();
	
	for(Element[] eles : scores.keySet()){
	   if(!le.contains(eles[0])) le.add(eles[0]);
	   if(!re.contains(eles[1])) re.add(eles[1]);
	}
	
	nbNodes1 = le.size();
	nbNodes2 = re.size();
	//
	
	int max = Math.max(nbNodes1, nbNodes2);
	// int factor = 5;
	int factor = 1;
	int maxFactor = max * factor;
	
	// filtered scores
	Map<Element[],Float> filteredScores = new LinkedHashMap<Element[],Float>();
	
	// filter value for now : 0.6 and max nodes <= maxFactor
	// IRL would use proba
	float treshold = (float) 0.6;
	int i = 0;
	
	for(Element[] eles : scores.keySet()){		  
	    if(scores.get(eles) >= treshold && i < maxFactor){
		filteredScores.put(eles, scores.get(eles));
		i++;
		if(i >= maxFactor) break;
	    }    
	}
	//

	float sumProba_Prod = 0;
	// we filter
	// we have all the results, we have to filter them
	// we keep only the five max best for each left node
	// if expert, it's only this one
	HashMap<Element, Integer> elementAndCount = new LinkedHashMap<Element, Integer>();

	Set<Element[]> keys = scores.keySet();
	for(Element[] key : keys){
	if ((elementAndCount.get(key[0]) == null || elementAndCount.get(key[0]) < factor)
		    && (elementAndCount.get(key[1]) == null || elementAndCount.get(key[1]) < factor)) {

		if(elementAndCount.get(key[0]) == null) elementAndCount.put(key[0],1);
		else elementAndCount.put(key[0], (elementAndCount.get(key[0]) + 1));
		if(elementAndCount.get(key[1]) == null) elementAndCount.put(key[1],1);
		else elementAndCount.put(key[1], (elementAndCount.get(key[1]) + 1));
		
		// sumProba_Prod += Math.pow(scores.get(key), (double) 1 / (double) 14);
		sumProba_Prod += scores.get(key);
	    }
	}
	// ok we insert in cluster
	distance = (float) sumProba_Prod / (float) Math.min(nbNodes1, nbNodes2);
	
	L.Debug(this.getClass().getSimpleName(),"Filtered distance "+distance,true);
	
	if(distance > 1) distance = 1;
	if(Float.isNaN(distance)) distance = 0;
	
	return distance;			
    }
}
