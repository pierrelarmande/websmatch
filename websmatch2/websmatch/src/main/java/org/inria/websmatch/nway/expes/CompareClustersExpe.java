package org.inria.websmatch.nway.expes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.models.Node;
import org.inria.websmatch.linlog.Edge;
import org.inria.websmatch.linlog.LinLogLayout;
import org.inria.websmatch.linlog.MinimizerBarnesHut;
import org.inria.websmatch.linlog.OptimizerModularity;

import com.google.gwt.dev.util.collect.Lists;

public class CompareClustersExpe {
    
    public static void main(String[] args){
    
	Map<Map<Node, double[]>, Map<Node, Integer[]>> fullClusters = getClustersForDB("fullMatchesExpe");
	Map<Map<Node, double[]>, Map<Node, Integer[]>> ttlClusters = getClustersForDB("NWayMatchesExpeTTL10000");
	
	//
	ArrayList<ArrayList<String>> fullNames = getClustersAndNames(fullClusters);
	ArrayList<ArrayList<String>> ttlNames = getClustersAndNames(ttlClusters);
	
	// ok compare them
	System.out.println("Nombre de clusters full :" + fullNames.size());
	System.out.println("Nombre de clusters ttl :" + ttlNames.size());	
	System.out.println("Nombre de docs dans full :"+countElements(fullNames));
	System.out.println("Nombre de docs dans ttl :"+countElements(ttlNames));
	//
	int[] resCpt = countIntersection(fullNames, ttlNames);
	System.out.println("Nombre de couple dans full : "+resCpt[0]);
	System.out.println("Nombre de couple dans tll qui sont ensembles dans full : "+resCpt[1]);
    }
    
    public static int[] countIntersection(ArrayList<ArrayList<String>> oracle, ArrayList<ArrayList<String>> toTest){
	int cpt = 0;
	int countCouple = 0;
	
	for(ArrayList<String> cluster : oracle){
	    Lists.sort(cluster);
	    for(int i = 0; i < cluster.size() -1; i++){
		String[] couple = new String[2];
		couple[0] = cluster.get(i);
		couple[1] = cluster.get(i+1);
		countCouple++;
		// is this couple in the same cluster in ttl?
		for(ArrayList<String> testCluster : toTest){
		    if(testCluster.contains(couple[0]) && testCluster.contains(couple[1])) cpt++; 
		}
	    }
	}
	
	int[] res = new int[2];
	res[0] = countCouple;
	res[1] = cpt;
	
	return res;
    }
    
    public static int countElements(ArrayList<ArrayList<String>> names){
	int cpt = 0;
	
	for(ArrayList<String> tmp : names) cpt += tmp.size();
	return cpt;
    }

    public static ArrayList<ArrayList<String>> getClustersAndNames(Map<Map<Node, double[]>, Map<Node, Integer[]>> fullClusters) {
	
	ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
	
	for (Map<Node, double[]> nodeToPosition : fullClusters.keySet()) {	  
	    
	 // Clusters strings
	ArrayList<ArrayList<String>> clusterNames = new ArrayList<ArrayList<String>>();
	    
	    // determine maximum cluster of the nodes
	    Map<Node, Integer[]> nodeToCluster = fullClusters.get(nodeToPosition);
	    int maxCluster = 0;
	    for (Integer[] cluster : nodeToCluster.values()) {
		maxCluster = Math.max(cluster[0], maxCluster);
		if (maxCluster + 1 > clusterNames.size())
		    clusterNames.add(new ArrayList<String>());
	    }

	    for (Node node : nodeToPosition.keySet()) {
		
		if (node.name.lastIndexOf(" (SchemaId : ") != -1) {		    
		    // add clusternames
		    clusterNames.get(nodeToCluster.get(node)[0]).add(node.name.substring(0, node.name.lastIndexOf(" (SchemaId : ")));
		} else{	   
		    // add clusternames
		    clusterNames.get(nodeToCluster.get(node)[0]).add(node.name);
		}
	    }
	    
	    for (ArrayList<String> names : clusterNames) results.add(names);
	    
	}
	return results;
    }
    
    public static Map<Map<Node, double[]>, Map<Node, Integer[]>> getClustersForDB(String dbName){
	Map<Map<Node, double[]>, Map<Node, Integer[]>> res = new HashMap<Map<Node, double[]>, Map<Node, Integer[]>>();

	Map<String, Map<String, Double>> graph = new HashMap<String, Map<String, Double>>();
	Map<String[],Double> distances = new HashMap<String[], Double>();
	
	MongoDBConnector connector = MongoDBConnector.getInstance();
	distances = connector.getDistances(dbName);
	
	Set<String[]> keys = distances.keySet();
	for(String[] key : keys){
	    
	    graph.put(key[1]+" (SchemaId : "+key[0]+")", new HashMap<String, Double>());    
	    graph.get(key[1]+" (SchemaId : "+key[0]+")").put(key[3]+" (SchemaId : "+key[2]+")", distances.get(key));
	    
	}

	// ok now work with linlog
	graph = LinLogLayout.makeSymmetricGraph(graph);
	Map<String, Node> nameToNode = LinLogLayout.makeNodes(graph);
	List<Node> nodes = new ArrayList<Node>(nameToNode.values());
	List<Edge> edges = LinLogLayout.makeEdges(graph, nameToNode);
	
	Map<Node, double[]> nodeToPosition = LinLogLayout.makeInitialPositions(nodes, false);
	new MinimizerBarnesHut(nodes, edges, -1.0, 2.0, 0.25).minimizeEnergy(nodeToPosition, 100);
	Map<Node, Integer> nodeToCluster = new OptimizerModularity().execute(nodes, edges, false);
	
	// for debug
	//LinLogLayout.writePositions(nodeToPosition, nodeToCluster, "/tmp/out.clusters");
			
	Map<Node, Integer[]> coloredNodeToCluster = new HashMap<Node, Integer[]>();

	// ok now replace integer by RGB color
	int maxCluster = 0;
	for (int cluster : nodeToCluster.values()) {
	    maxCluster = Math.max(cluster, maxCluster);
	}

	for (Node node : nodeToPosition.keySet()) {
	    float hue = nodeToCluster.get(node) / (float) (maxCluster + 1);
	    int icol = Color.HSBtoRGB(hue, 1.0f, 1.0f);
	    Color color = new Color(icol);

	    coloredNodeToCluster.put(node, new Integer[] { nodeToCluster.get(node), color.getRed(), color.getGreen(), color.getBlue() });
	}
	//

	res.put(nodeToPosition, coloredNodeToCluster);
	return res;
    }
}
