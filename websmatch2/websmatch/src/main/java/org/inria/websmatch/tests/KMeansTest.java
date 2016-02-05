package org.inria.websmatch.tests;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Test;

import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class KMeansTest {

    Logger logger = Logger.getLogger(KMeansTest.class.getName());
    static {
        BasicConfigurator.configure();
    }

    private double getLatitude(BasicDBObject object) {
        BasicDBObject geometry = (BasicDBObject) object.get("geometry");
        BasicDBList list = (BasicDBList) geometry.get("coordinates");

        double d = (Double) list.get(1);
        return 1000 * (d-(int)d);
    }

    private double getLongitude(BasicDBObject object) {
        BasicDBObject geometry = (BasicDBObject) object.get("geometry");
        BasicDBList list = (BasicDBList) geometry.get("coordinates");

        double d = -(Double) list.get(0);
        return 1000 * (d-(int)d);
    }

    @SuppressWarnings("unused")
    private String id(BasicDBObject object) {
        return (String) object.get("id");
    }

    @SuppressWarnings("unused")
    private String address(BasicDBObject object) {
        BasicDBObject properties = (BasicDBObject) object.get("properties");
        return properties.get("address") + ", " +
                properties.get("city") + ", " +
                properties.get("province");
    }

    @Test
    public void doKMeans() throws Exception {

        int lat = 0;
        int lon = 1;
        int id = 2;
        int ixVal = 0;

        Attribute attr1 = new Attribute("x");
        Attribute attr2 = new Attribute("y");
        Attribute attr3 = new Attribute("ix");

        FastVector fvAttributes = new FastVector(3);
        fvAttributes.addElement(attr1);
        fvAttributes.addElement(attr2);
        fvAttributes.addElement(attr3);

        Instances dataset = new Instances("data", fvAttributes, 10);

        Mongo m = new Mongo("128.195.52.175");
        DB db = m.getDB("test");
        DBCollection collection = db.getCollection("places");

        BasicDBObject query = new BasicDBObject("properties.city", "Irvine");
        query.put("properties.province", "CA");
        BasicDBObject filter = new BasicDBObject("$elemMatch", new BasicDBObject("type", "Food & Drink"));
        query.put("properties.classifiers", filter);

        BasicDBObject project = new BasicDBObject();
        project.put("geometry", 1);
        project.put("properties.name", 1);
        project.put("properties.address", 1);
        project.put("_id", 0);

        DBCursor cursor = collection.find(query, project);
        ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();

        BufferedWriter fos = new BufferedWriter(new FileWriter("data"));

        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();

            fos.write(getLatitude(obj) + "\t\t");
            fos.write(getLongitude(obj) + "\t\t");
            fos.write('\n');

            Instance instance = new Instance(3);
            instance.setValue(lat, getLatitude(obj));
            instance.setValue(lon, getLongitude(obj));
            instance.setValue(id, ixVal++);

            dataset.add(instance);
            list.add(obj);
        }

        fos.flush();
        fos.close();

        db.cleanCursors(true);
        m.close();

        logger.info("Dataset count: " + dataset.numInstances());

        SimpleKMeans clusterer = new SimpleKMeans();
//        clusterer.setAcuity(0.01);

        clusterer.setPreserveInstancesOrder(true);
        clusterer.setNumClusters(42);
        clusterer.buildClusterer(dataset);

        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(clusterer);
        eval.evaluateClusterer(dataset);

        int clusterID = 2;
        int[] assignments = clusterer.getAssignments();

        logger.info("Clusterer centroid count: " + clusterer.getClusterCentroids().numInstances());

        for (int i=0;i <list.size(); i++) {
            if (assignments[i] == clusterID) {
                logger.info(i + " " + getLatitude(list.get(i)) +", " + getLongitude(list.get(i)) + " " + " data.Cluster " + clusterID + ": " +  list.get(i).get("properties"));
            }
//            logger.info("Pigs: " + clusterer.clusterInstance(dataset.instance(i)));
        }

        logger.info(eval.clusterResultsToString());
    }
}
