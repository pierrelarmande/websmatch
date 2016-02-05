package org.inria.websmatch.nway.expes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.utils.L;

public class CompareDistances {
    
    public static String nWayUser = "NWayMatchesExpeTTL10000";

    /**
     * @param args
     */
    public static void main(String[] args) {

	// generateDistances();
	MongoDBConnector mongo = MongoDBConnector.getInstance();
	System.out.println("k\tOrdered score\tNot ordered score");
	for (int k = 1 ; k <= 30; k++) {
	    double[] scores = mongo.analyzeDistances("fullMatchesExpe", nWayUser, k);
	    System.out.println(k + "\t" + scores[0] + "\t" + scores[1]);
	}

    }

    public static void generateDistances() {
	int cptNoCount = 0;
	int cptEqScores = 0;

	MongoDBConnector connector = MongoDBConnector.getInstance();
	HashMap<String[], Double> distancesFull = connector.getDistances("fullMatchesExpe");
	HashMap<String[], Double> distancesNWay = connector.getDistances(nWayUser);

	File output = new File("compare_dist.csv");
	if (output.exists())
	    output.delete();

	try {
	    output.createNewFile();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}

	try {
	    FileWriter writer = new FileWriter(output);

	    writer.write("\"Filename1\";\"Filename2\";distFull;distNWay\n");
	    writer.flush();

	    Set<String[]> keys = distancesFull.keySet();
	    for (String[] k : keys) {

		// now found on nway
		double nwayDist = -1;
		Set<String[]> nwayKeys = distancesNWay.keySet();
		for (String[] nwayK : nwayKeys) {
		    if ((nwayK[1].trim().equals(k[1].trim()) && nwayK[3].trim().equals(k[3].trim()))
			    || (nwayK[1].trim().equals(k[3].trim()) && nwayK[3].trim().equals(k[1].trim()))) {
			nwayDist = distancesNWay.get(nwayK);
			if (nwayDist == distancesFull.get(k))
			    cptEqScores++;
			break;
		    }
		}

		if (nwayDist == -1.0) {
		    cptNoCount++;
		}

		writer.write("\"" + k[1] + "\";\"" + k[3] + "\";" + distancesFull.get(k) + ";" + nwayDist + "\n");
		writer.flush();
	    }

	    writer.close();

	    System.out.println(cptNoCount + " distances not found on nway for " + distancesFull.size() + " total distances on full.");
	    System.out.println(cptEqScores + " distances equals on nway and full.");

	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
    }

}
