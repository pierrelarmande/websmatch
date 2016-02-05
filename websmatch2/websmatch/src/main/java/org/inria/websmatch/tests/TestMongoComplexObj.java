package org.inria.websmatch.tests;

import org.inria.websmatch.db.MongoDBConnector;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;

public class TestMongoComplexObj {

    /**
     * @param args
     */
    public static void main(String[] args) {

	BasicDBObject dbo = new BasicDBObject("cluster", "xxxxx").append("tag", new BasicDBObject("latitude", 51).append("longitude", 1));

	MongoDBConnector connect = MongoDBConnector.getInstance();
	DB db = connect.getMongo().getDB("testComplex");

	db.getCollection("testComplex").insert(dbo);

	//
	BasicDBObject oQuery = new BasicDBObject("tag", new BasicDBObject("$exists", new BasicDBObject("latitude",
		new BasicDBObject("$gt", 50.856269514840506).append("$lt", 54.698784372492796)).append("longitude",
		new BasicDBObject("$gt", -5.276301090590983).append("$lt", 0.158913960894979))));

	System.out.println(oQuery);
	System.out.println(db.getCollection("testComplex").find(oQuery).next().toString());
    }
}
