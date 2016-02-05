package org.inria.websmatch.nway.expes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.inria.websmatch.utils.L;

import com.google.gwt.dev.util.collect.Lists;

public class CompareMCLClusters {
    
    public static void main(String[] args){
	List<List<String>> fullClusters = new ArrayList<List<String>>();
	List<List<String>> ttlClusters = new ArrayList<List<String>>();

	// load clusters
	fullClusters = getClusters("/home/manu/Bureau/NWayExpeResults/Run18_300docs/full_MCL.txt");
	ttlClusters = getClusters("/home/manu/Bureau/NWayExpeResults/Run18_300docs/TTL_MCL.txt");
	
	// intersect
	// ok compare them
	System.out.println("Nombre de clusters full :" + fullClusters.size());
	System.out.println("Nombre de clusters ttl :" + ttlClusters.size());	
	//
	int[] resCpt = countIntersection(fullClusters, ttlClusters);
	System.out.println("Nombre de couple dans full : "+resCpt[0]);
	System.out.println("Nombre de couple dans tll qui sont ensembles dans full : "+resCpt[1]);
    }
    
    public static List<List<String>> getClusters(String filePath){
	List<List<String>> clusters = new ArrayList<List<String>>();
	
	// open file
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new FileReader(filePath));

	    String currentLine;

	    while ((currentLine = br.readLine()) != null) {
		clusters.add(Arrays.asList(currentLine.split("\\s")));
	    }

	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(),e);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
	
	return clusters;
    }
    
    public static int[] countIntersection(List<List<String>> oracle, List<List<String>> toTest){
	int cpt = 0;
	int countCouple = 0;
	
	for(List<String> cluster : oracle){
	    Lists.sort(cluster);
	    for(int i = 0; i < cluster.size() -1; i++){
		String[] couple = new String[2];
		couple[0] = cluster.get(i);
		couple[1] = cluster.get(i+1);
		countCouple++;
		// is this couple in the same cluster in ttl?
		for(List<String> testCluster : toTest){
		    if(testCluster.contains(couple[0]) && testCluster.contains(couple[1])) cpt++; 
		}
	    }
	}
	
	int[] res = new int[2];
	res[0] = countCouple;
	res[1] = cpt;
	
	return res;
    }
}
