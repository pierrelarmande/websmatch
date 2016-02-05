package org.inria.websmatch.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

import org.inria.websmatch.utils.L;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class UpdateXLSNames {

    public static void main(String[] args) {
	
	String dbName = "datapublica";
	
	Mongo m = null;
	try {
	    m = new Mongo("localhost", 27017);
	} catch (UnknownHostException e) {
	    L.Error(e.getMessage(),e);
	} catch (MongoException e) {
	    L.Error(e.getMessage(),e);
	}

	DB currentDB = m.getDB(dbName);
	   	
	// load csv file
	File file = new File("/home/manu/Bureau/datapublica_excel_utf.csv");
	BufferedReader bufRdr = null;
	try {
	    bufRdr = new BufferedReader(new FileReader(file));
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	}
	String line = null;
	try {
	    while ((line = bufRdr.readLine()) != null) {
		System.out.println(line);
	        String[] values = line.split(";");

		// work this db
		currentDB.requestStart();

		BasicDBObject newDocument = new BasicDBObject().append("$set", 
			new BasicDBObject().append("name", values[2].trim()));
		/*newDocument = new BasicDBObject().append("$set", 
			new BasicDBObject().append("publication_id", values[0].trim()));
		newDocument = new BasicDBObject().append("$set", 
			new BasicDBObject().append("source", values[1].trim()));
		newDocument.append("$push", 
			new BasicDBObject().append("categories", values[3].trim()+":"+values[4].trim()));*/
	 
		currentDB.getCollection("stored_schemas").update(new BasicDBObject().append("publication_id", values[0].trim()), newDocument);

		currentDB.requestDone();
		//
	    }
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
	// close the file
	try {
	    bufRdr.close();
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
    }
}
