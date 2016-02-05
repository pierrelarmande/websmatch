package org.inria.websmatch.nway.expes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.dspl.Concept;
import org.inria.websmatch.utils.FileUtils;
import org.inria.websmatch.utils.L;

public class CompleteMatchesExpe {
    
    public final static String nWayUser = "NWayMatchesExpeTTL10000";
    
    public static void generateOccurencesDistrib(String dbName){
	
	MongoDBConnector dbCon = MongoDBConnector.getInstance();
	ArrayList<Concept> concepts = dbCon.getConcepts(dbName, -1);
	
	File output = new File(dbName+"_concept_distrib.csv");
	if (output.exists())
	    output.delete();

	try {
	    output.createNewFile();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	
	FileWriter writer = null;
	try {
	    writer = new FileWriter(output);
	    writer.write("concept_id;name;nb_occurences\n");
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
	
	for(Concept c : concepts){
	    if(c.getNbOccurences() > 1)
		try {
		    writer.write(c.getId()+";\""+c.getName()+"\";"+c.getNbOccurences()+"\n");
		} catch (IOException e) {
		    L.Error(e.getMessage(),e);
		}
	}
	
	try {
	    writer.flush();
		writer.close();
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
    }

    public static void main(String[] args) {
					
	// compute matches
	@SuppressWarnings("unused")
	long[] ttls = { 0, 5000, 6000, 8000, 10000};
	NWayMatchesExpe.shuffled = false;
	
	// for testing concept
	File dir = new File("/home/manu/Documents/TestsDataSets");
	File[] files = dir.listFiles();
	Arrays.sort(files);
	
	// randomize
	if(!NWayMatchesExpe.shuffled){
	    NWayMatchesExpe.shuffled = true;
	    files = FileUtils.randomizeFiles(files);	  
	}
	//
	
	/*files = NWayMatchesExpe.generateExpes(true, -1, ttls, files);
	FullMatchesExpe.generateExpes(files);*/
	
	// compute distances
	NWayMatchesExpe nwme = new NWayMatchesExpe(nWayUser);
	nwme.computeDistances();
	nwme.printResult();
	
	FullMatchesExpe fme = new FullMatchesExpe("fullMatchesExpe");
	fme.computeDistances();
	fme.printResult();
	
	// distrib concepts
	generateOccurencesDistrib("fullMatchesExpe");
	generateOccurencesDistrib(nWayUser);
	//
	
	// compare matches now prec/recall/fmeas
	// we have to compare the concepts from TTL5000 and Full
	// each contains the list of docs/concepts matched
	// we use the full as an oracle	
	MongoDBConnector dbCon = MongoDBConnector.getInstance();
	
	TreeMap<String,TreeSet<String>> fullIds = dbCon.getMatchedCouplesFromConcepts("fullMatchesExpe");
	TreeMap<String,TreeSet<String>> ttlIds = dbCon.getMatchedCouplesFromConcepts(nWayUser);
	
	System.out.println("Concepts (no single) for full : "+fullIds.size());
	System.out.println("Concepts (no single) for TTL : "+ttlIds.size());
	
	File output = new File("compare_concepts_matrix.csv");
	if (output.exists())
	    output.delete();

	try {
	    output.createNewFile();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	
	FileWriter writer = null;
	try {
	    writer = new FileWriter(output);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
	
	// write the TTL ids
	try {
	    writer.write("fullIds\\TTLIds;");
	        
	    Set<String> keys = ttlIds.navigableKeySet();
	    
	    for(String k : keys){
		writer.write(k+";");
	    }
	    writer.write("\n");
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

	// make a matrix cFull/cTTL ratio shared
	Set<String> keys = fullIds.navigableKeySet();
	for(String k : keys){
	    
	    try {
		writer.write(k+";");
	    } catch (IOException e1) {
		e1.printStackTrace();
	    }
	    
	    Set<String> fullUris = fullIds.get(k);
	    
	    Set<String> ttlKeys = ttlIds.navigableKeySet();
	    
	    for(String ttlK : ttlKeys){
				
		Set<String> ttlUris = ttlIds.get(ttlK);
		
		int sizeFull = fullUris.size();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<String> clone = (Set<String>) ((TreeSet) fullUris).clone();
		// get the intersection set
		clone.retainAll(ttlUris);
		
		double result = (double) clone.size()/(double)sizeFull;
			
		if(Double.isNaN(result)) result = 0;
		try {
		    writer.write(new Double(result).toString()+";");
		} catch (IOException e) {
		    L.Error(e.getMessage(),e);
		}		
	    }
	    try {
		writer.write("\n");
	    } catch (IOException e) {
		L.Error(e.getMessage(),e);
	    }
	}
	try {
	    writer.flush();
	    writer.close();
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
	//
    }
}
