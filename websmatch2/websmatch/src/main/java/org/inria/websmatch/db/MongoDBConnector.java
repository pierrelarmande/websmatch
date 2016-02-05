package org.inria.websmatch.db;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.commons.lang3.StringUtils;
import org.bson.io.BasicOutputBuffer;
import org.bson.types.ObjectId;
import org.inria.websmatch.dspl.Concept;
import org.inria.websmatch.gwt.spreadsheet.client.models.*;
import org.inria.websmatch.matchers.base.ConceptMatcher;
import org.inria.websmatch.nway.expes.FullMatchesExpe;
import org.inria.websmatch.nway.expes.NWayMatchesExpe;
import org.inria.websmatch.utils.FileUtils;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xml.WSMatchXMLDiff;
import org.inria.websmatch.xml.WSMatchXMLLoader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * As MongoDB Java Driver is thread safe, we use only one instance (singleton)
 *
 * @author manu
 *
 */

public class MongoDBConnector implements DBConnector {

	// for expes
	public static boolean useNWay = false;
	public static int k = -1;
	public static boolean storeIdMatch = false;
	// ttl in milliseconds
	public static long ttl = 0;
	public static float proba_prod_treeshold = 0.000004851016847943722f;// 0.01f;
	// total matching time even if nothing found
	public static long totalMatchTime = 0;
	public static long totalMatchesFound = 0;
	// min/max
	public static long minMatchTime = 0;
	public static long maxMatchTime = 0;

	// db
	private String dbHost;// = "193.49.106.32";
	private int dbPort;// = 3306;
	// private String dbName;// = "matching_results";

	private HashMap<String, DB> dbs = null;

	private static MongoDBConnector instance;
	private Mongo mongo;

	public static MongoDBConnector getInstance() {
		if (null == instance) {
			instance = new MongoDBConnector();
		}
		return instance;
	}

	private MongoDBConnector() {

		MongoDBConfLoader loader = MongoDBConfLoader.getInstance();

		setDbHost(loader.getDbHost());
		setDbPort(loader.getDbPort());
		// setDbName(loader.getDbName());

		dbs = new HashMap<>();

		this.connect();
	}

	private boolean connect() {

		Mongo m = null;
		try {
			m = new Mongo(dbHost, dbPort);
		} catch (UnknownHostException e) {
			L.Error(e.getMessage(),e);
			return false;
		} catch (MongoException e) {
			L.Error(e.getMessage(),e);
			return false;
		}

		mongo = m;

		return true;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbPort(int dbPort) {
		this.dbPort = dbPort;
	}

	public int getDbPort() {
		return dbPort;
	}

    /*
     * public void setDbName(String dbName) { this.dbName = dbName; }
     * 
     * public String getDbName() { return dbName; }
     */

	@Override
	public String getXML(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean insertXML(String doc) {
		// TODO Auto-generated method stub
		return false;
	}

	public void insertStoredMatchId(Set<String> uris, String dbName) {
		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		// make object
		BasicDBObject doc = new BasicDBObject();
		doc.put("uris", uris.toArray());

		if (currentDB.collectionExists("stored_matches_id")) {
			currentDB.getCollection("stored_matches_id").insert(doc);
		} else {
			currentDB.createCollection("stored_matches_id", doc);
		}
		currentDB.requestDone();
	}

	public List<TreeSet<String>> getStoredMatchIds(String dbName) {

		List<TreeSet<String>> results = new ArrayList<TreeSet<String>>();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return results;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		DBCursor cur = currentDB.getCollection("stored_matches_id").find();

		while (cur.hasNext()) {
			DBObject obj = cur.next();

			TreeSet<String> ids = new TreeSet<String>();
			ids.addAll(Arrays.asList(((BasicDBList) obj.get("uris")).toArray(new String[((BasicDBList) obj.get("uris")).size()])));
			results.add(ids);
		}

		currentDB.requestDone();

		return results;
	}

	public int getOccurencesForConcept(String cId, String dbName){
		int res = 0;

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return res;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		BasicDBObject query = new BasicDBObject();

		query.put("_id", new ObjectId(cId));

		DBCursor cur = currentDB.getCollection("concepts").find(query);

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			res = (Integer) obj.get("nbOccurences");
			break;
		}

		currentDB.requestDone();

		return res;
	}

	public String[] getConceptIdsForDocument(String docId, String dbName){
		String[] res = null;

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return res;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		BasicDBObject query = new BasicDBObject();

		query.put("_id", new ObjectId(docId));

		DBCursor cur = currentDB.getCollection("stored_schemas").find(query);

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			res = obj.get("concepts").toString().split("\\s+");
			break;
		}

		currentDB.requestDone();

		return res;
	}

	/**
	 * Method used to get the couples of matches in each concepts Each couple is
	 * a string uri1|uri2 ordered by lexical where uri is fileName/sheet/x/y
	 *
	 * @param dbName
	 * @return Concepts couples
	 */

	public TreeMap<String, TreeSet<String>> getMatchedCouplesFromConcepts(String dbName) {

		TreeMap<String, TreeSet<String>> results = new TreeMap<String, TreeSet<String>>();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return results;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		DBCursor cur = currentDB.getCollection("concepts").find();

		while (cur.hasNext()) {
			DBObject obj = cur.next();

			String conceptId = obj.get("_id").toString();

			// a couple is uri1|uri2 order by lexico
			TreeSet<String> set = new TreeSet<String>();

			List<String> ids = new ArrayList<String>();
			ids.add(obj.get("uri").toString());
			ids.addAll(Arrays.asList(((BasicDBList) obj.get("alternativeUris")).toArray(new String[((BasicDBList) obj.get("alternativeUris")).size()])));
			if (ids.size() > 1) {
				// make the couples
				for (int i = 0; i < ids.size() - 1; i++) {
					String uri1 = ids.get(i);
					for (int j = i + 1; j < ids.size(); j++) {
						String uri2 = ids.get(j);
						if (uri1.compareTo(uri2) < 0)
							set.add(uri1 + "|" + uri2);
						else
							set.add(uri2 + "|" + uri1);
					}
				}
				results.put(conceptId, set);
			}
		}

		currentDB.requestDone();

		return results;
	}

	@Override
	public String getEditedXML(String object_id, String dbName) {
		String xml = new String();

		DB currentDB = dbs.get(dbName);

		L.Debug(this.getClass().getSimpleName(), "DBName is " + dbName, true);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return xml;
			}
		}

		// work this db
		currentDB.requestStart();

		BasicDBObject query = new BasicDBObject();

		query.put("_id", new ObjectId(object_id));

		L.Debug(this.getClass().getSimpleName(), "OID is " + object_id, true);

		DBCursor cur = currentDB.getCollection("stored_schemas").find(query);

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			// System.out.println(obj);
			xml = obj.get("edit_xml").toString();
			if (xml.equals(""))
				xml = obj.get("auto_xml").toString();

			break;
		}

		currentDB.requestDone();
		//

	/*
	 * if (L._DEBUG) System.out.println("Edited XML : " + xml);
	 */

		return xml;
	}

	public String getAutoXML(String object_id, String dbName) {
		String xml = new String();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return xml;
			}
		}

		// work this db
		currentDB.requestStart();

		BasicDBObject query = new BasicDBObject();

		query.put("_id", new ObjectId(object_id));

		L.Debug(this.getClass().getSimpleName(), "OID is " + object_id, true);

		DBCursor cur = currentDB.getCollection("stored_schemas").find(query);

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			// System.out.println(obj);
			xml = obj.get("auto_xml").toString();
			break;
		}

		currentDB.requestDone();
		//

	/*
	 * if (L._DEBUG) System.out.println("Auto XML : " + xml);
	 */

		return xml;
	}

	/**
	 * For expes
	 *
	 * @param oracleDBName
	 * @param toTestDBName
	 * @param k
	 * @return
	 */

	public double[] analyzeDistances(String oracleDBName, String toTestDBName, int k) {
		// two scores, ordered and not
		double[] scores = new double[2];

		// top key docs for each key in the order
		Map<String, List<String>> oracleDocAndDocs = new TreeMap<String, List<String>>();
		Map<String, List<String>> toTestDocAndDocs = new TreeMap<String, List<String>>();

		DB currentDB = dbs.get(oracleDBName);

		if (currentDB == null) {
			currentDB = mongo.getDB(oracleDBName);
			// doesn't exist, create it
			if (currentDB == null) {
				return scores;
			}
		}

		// work this db
		currentDB.requestStart();

		// construct a map with ids/names
		Map<String, String> fullDocMap = new HashMap<String, String>();
		DBCursor nameCur = currentDB.getCollection("stored_schemas").find();
		while (nameCur.hasNext()) {
			DBObject obj = nameCur.next();
			fullDocMap.put(obj.get("_id").toString(), obj.get("name").toString());
		}
		//

		nameCur = currentDB.getCollection("stored_schemas").find();
		while (nameCur.hasNext()) {
			DBObject obj = nameCur.next();
			// the query to get the top k order by score
			BasicDBObject clause1 = new BasicDBObject();
			clause1.put("doc_id1", obj.get("_id").toString());
			BasicDBObject clause2 = new BasicDBObject();
			clause2.put("doc_id2", obj.get("_id").toString());
			BasicDBList or = new BasicDBList();
			or.add(clause1);
			or.add(clause2);
			DBObject query = new BasicDBObject("$or", or);
			// get the top k distances
			DBCursor resultsOnFull = currentDB.getCollection("distances").find(query).sort(new BasicDBObject("distance", -1))
					.sort(new BasicDBObject("doc_name1",1))
					.sort(new BasicDBObject("doc_name2",1))
					.limit(k);
			List<String> nameListOnFull = new ArrayList<String>();

			// ok make a list of other id
			while (resultsOnFull.hasNext()) {
				DBObject doc = resultsOnFull.next();
				if (doc != null) {
					// create the doc list
					if (doc.get("doc_id1").toString().equals(obj.get("_id").toString()))
						nameListOnFull.add(fullDocMap.get(doc.get("doc_id2").toString()));
					else if (doc.get("doc_id2").toString().equals(obj.get("_id").toString()))
						nameListOnFull.add(fullDocMap.get(doc.get("doc_id1").toString()));
				}
			}
			oracleDocAndDocs.put(fullDocMap.get(obj.get("_id").toString()), nameListOnFull);
		}

		currentDB.requestDone();
		//

		currentDB = dbs.get(toTestDBName);

		if (currentDB == null) {
			currentDB = mongo.getDB(toTestDBName);
			// doesn't exist, create it
			if (currentDB == null) {
				return scores;
			}
		}

		// work this db
		currentDB.requestStart();

		// construct a map with ids/names
		Map<String, String> testDocMap = new HashMap<String, String>();
		nameCur = currentDB.getCollection("stored_schemas").find();
		while (nameCur.hasNext()) {
			DBObject obj = nameCur.next();
			testDocMap.put(obj.get("_id").toString(), obj.get("name").toString());
		}
		//

		nameCur = currentDB.getCollection("stored_schemas").find();
		while (nameCur.hasNext()) {

			DBObject obj = nameCur.next();
			// the query to get the top k order by score
			BasicDBObject clause1 = new BasicDBObject();
			clause1.put("doc_id1", obj.get("_id").toString());
			BasicDBObject clause2 = new BasicDBObject();
			clause2.put("doc_id2", obj.get("_id").toString());
			BasicDBList or = new BasicDBList();
			or.add(clause1);
			or.add(clause2);
			DBObject query = new BasicDBObject("$or", or);
			// get the top k distances
			DBCursor resultsOnFull = currentDB.getCollection("distances").find(query)
					.sort(new BasicDBObject("distance", -1))
					.sort(new BasicDBObject("doc_name1",1))
					.sort(new BasicDBObject("doc_name2",1))
					.limit(k);

			List<String> nameListOnFull = new ArrayList<String>();

			// ok make a list of other id
			while (resultsOnFull.hasNext()) {
				DBObject doc = resultsOnFull.next();
				if (doc != null) {
					// create the doc list
					if (doc.get("doc_id1").equals(obj.get("_id").toString()))
						nameListOnFull.add(testDocMap.get(doc.get("doc_id2").toString()));
					else if (doc.get("doc_id2").equals(obj.get("_id").toString()))
						nameListOnFull.add(testDocMap.get(doc.get("doc_id1").toString()));
				}
			}

			toTestDocAndDocs.put(testDocMap.get(obj.get("_id").toString()), nameListOnFull);
		}

		currentDB.requestDone();

		// ok we have the 2 lists, now calculate the scores!
		Set<String> keys = oracleDocAndDocs.keySet();
		double countNoOrderTotal = 0;
		double countOrderTotal = 0;
		for (String key : keys) {

			// not the same order
			int countNoOrder = 0;
			int countOrder = 0;
			List<String> oracleDocs = oracleDocAndDocs.get(key);
			List<String> toTestDocs = toTestDocAndDocs.get(key);

			// no order
			for (String doc : oracleDocs) {
				if (toTestDocs != null && toTestDocs.contains(doc)) {
					countNoOrder++;
				}
			}

			// order
			for (int i = 0; i < oracleDocs.size(); i++) {
				if (toTestDocs.size() > i && oracleDocs.get(i).equals(toTestDocs.get(i)))
					countOrder++;
			}

			if(oracleDocs.size() == 0){
				countOrderTotal += 1;
				countNoOrderTotal += 1;
			}
			//
			else{
				countOrderTotal += ((double) countOrder / (double) Math.min(oracleDocs.size(), k));
				countNoOrderTotal += ((double) countNoOrder / (double) Math.min(oracleDocs.size(), k));
			}
		}
		//
		// normalize
		countOrderTotal = countOrderTotal / (double) keys.size();
		scores[0] = countOrderTotal;
		countNoOrderTotal = countNoOrderTotal / (double) keys.size();
		scores[1] = countNoOrderTotal;

		return scores;
	}

	/**
	 * Get the content of distances table (id1, id2, distance)
	 *
	 * @param dbName
	 *            The name of the DB
	 * @return A map with {id1,id2},distance
	 */
	public HashMap<String[], Double> getDistances(String dbName) {

		HashMap<String[], Double> distances = new LinkedHashMap<String[], Double>();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return distances;
			}
		}

		// work this db
		currentDB.requestStart();

		// first get the names of documents
		Map<String, String> names = new HashMap<String, String>();
		DBCursor nameCur = currentDB.getCollection("stored_schemas").find();
		while (nameCur.hasNext()) {
			DBObject name = nameCur.next();
			names.put(name.get("_id").toString(), (String) name.get("name"));
		}

		// the query
		BasicDBObject query = new BasicDBObject();
		query.put("distance", new BasicDBObject("$gt", 0));
		//

		DBCursor cur = currentDB.getCollection("distances").find(query).sort(new BasicDBObject("distance",-1));

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			String[] ids = new String[4];
			ids[0] = (String) obj.get("doc_id1");
			ids[1] = names.get(ids[0]);

			ids[2] = (String) obj.get("doc_id2");
			ids[3] = names.get(ids[2]);

			double dist = (Double) obj.get("distance");
			distances.put(ids, dist);
		}

		currentDB.requestDone();
		//

		return distances;
	}

	/**
	 * Insert a new distance between 2 documents, it will be stored by
	 * lexicographical order (for ids)
	 *
	 * @param docId1
	 *            First document
	 * @param docId2
	 *            Second document
	 * @param distance
	 *            Distance between these docs
	 * @param trueMatches
	 *            The XML with true matches (expert)
	 * @param dbName
	 *            Name of the db to use
	 * @return The object_id in database for this distance
	 */
	public String insertOrUpdateDistance(String docId1, String docName1, String docId2, String docName2, float distance, String trueMatches, String dbName) {

		String object_id = new String();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return null;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		// make object
		BasicDBObject doc = new BasicDBObject();

		if (docId1.compareTo(docId2) < 0) {
			doc.put("doc_id1", docId1);
			doc.put("doc_name1", docName1);
			doc.put("doc_id2", docId2);
			doc.put("doc_name2", docName2);
		} else {
			doc.put("doc_id1", docId2);
			doc.put("doc_name1", docName2);
			doc.put("doc_id2", docId1);
			doc.put("doc_name2", docName1);
		}
		doc.put("distance", distance);
		doc.put("true_matches", trueMatches);

		if (currentDB.collectionExists("distances")) {
			// see if we have to update
			BasicDBObject query = new BasicDBObject();
			query.put("doc_id1", doc.getString("doc_id1"));
			query.put("doc_name1", doc.getString("doc_name1"));
			query.put("doc_id2", doc.getString("doc_id2"));
			query.put("doc_name2", doc.getString("doc_name2"));

			DBCursor cur = null;
			cur = currentDB.getCollection("distances").find(query);
			if (cur.hasNext()) {
				DBObject obj = cur.next();
				obj.put("distance", distance);
				obj.put("true_matches", trueMatches);
				currentDB.getCollection("distances").update(query, obj);
				object_id = ((ObjectId) obj.get("_id")).toString();
			}

			// else insert
			else {
				currentDB.getCollection("distances").insert(doc);
				object_id = doc.getString("_id");
			}
		} else {
			currentDB.createCollection("distances", doc);
			object_id = doc.getString("_id");
		}

		currentDB.requestDone();
		//

		return object_id;
	}

	@Override
	public String insertXML(String auto_xml, String edit_xml, String fileCompleteURI, String name, String source, String user, String description,
							boolean ccDetectionPb, boolean attrDetectionPb, String detectPb, boolean trashed, String dbName) {

		DB currentDB = dbs.get(dbName);
		String object_id = null;

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return null;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		// load file
		byte[] bytes = FileUtils.loadFile(fileCompleteURI);

		GridFS fs = new GridFS(currentDB);

		// Save file into database
		GridFSInputFile in = fs.createFile(bytes);
		in.save();

		// make object
		BasicDBObject doc = new BasicDBObject();

		doc.put("auto_xml", auto_xml);
		doc.put("edit_xml", edit_xml);
		doc.put("file_id", in.getId());
		doc.put("name", name);
		doc.put("source", source);
		doc.put("user", user);
		doc.put("description", description);

		// for detection problem
		doc.put("cc_pb", ccDetectionPb);
		doc.put("attr_pb", attrDetectionPb);
		doc.put("detect_pb_desc", detectPb);

		// trashed
		doc.put("trashed", trashed);

		if (currentDB.collectionExists("stored_schemas")) {
			currentDB.getCollection("stored_schemas").insert(doc);
		} else {
			currentDB.createCollection("stored_schemas", doc);
			currentDB.getCollection("stored_schemas").insert(doc);
		}

		ObjectId id = (ObjectId) doc.get("_id");
		object_id = id.toString();

		// we have to get concepts from db and find matches to update occurences

		ArrayList<Concept> currentConcepts = this.getConcepts(dbName, k);
		//

		// second step (we need _id for it), update concepts
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(object_id));

		// parse XML to get concepts
		ArrayList<Concept> concepts = new ArrayList<Concept>();
		// get the first table in each doc
		WSMatchXMLLoader docLoader = new WSMatchXMLLoader(edit_xml);
		Document xmlDoc = docLoader.getDocument();

		// go throught attributes
		Filter attributeFilter = new ElementFilter("attribute", null);
		@SuppressWarnings("unchecked")
		Iterator<Element> docAttr = xmlDoc.getRootElement().getDescendants(attributeFilter);

		// we divide TTL by concepts to match
		@SuppressWarnings("unchecked")
		Iterator<Element> countAttr = xmlDoc.getRootElement().getDescendants(attributeFilter);
		int ttlDivideBy = 0;
		// List<Element> elements = new ArrayList<Element>();
		while( countAttr.hasNext()){
			// elements.add(countAttr.next());
			countAttr.next();
			ttlDivideBy++;
		}
		// Collections.shuffle(elements);
		// dynamic TTL
		if(ttl == 0 && useNWay){
			ttl = Math.min(10000, 1000*ttlDivideBy);
		}
		//

		long timeForEachConcept = 0;
		if(ttlDivideBy > 0) timeForEachConcept = ttl/ttlDivideBy;

		// if nway and k = -1, use time TTL
		long start = 0;
		long end = 0;

		// if not nWay
		// time evolution for matching in a file
		int nbConceptsInDoc = 0;
		int nbConceptsInDB = currentConcepts.size();
		int nbMatches = 0;
		long globalTmpStart = System.currentTimeMillis();
		//

		// ttl by doc
		start = System.currentTimeMillis();

		if (useNWay && k == -1) {
			end = start + ttl;
		}
		boolean ttlArrived = false;

		// for storing concept ids
		String ids = new String();

		// iterate through attributes
		while (docAttr.hasNext()) {

	    /*
	     * if ttl by attr start = System.currentTimeMillis();
	     * 
	     * if (useNWay && k == -1) { end = start + ttl; }
	     */

			nbConceptsInDoc++;

			Element attr = docAttr.next();

			// get instances
			Filter dataFilter = new ElementFilter("data", null);
			@SuppressWarnings("unchecked")
			Iterator<Element> dataAttr = attr.getDescendants(dataFilter);
			Set<String> instances = new TreeSet<String>();
			while (dataAttr.hasNext()) {
				instances.add(dataAttr.next().getText().trim());
			}

			// make a new concept
			// TODO add the attribute type -string, integer, other?- to XML file
			Concept newConcept = new Concept("", attr.getChildText("name").trim(), new ArrayList<String>(), source + "/" + attr.getAttributeValue("sheet")
					+ "/" + attr.getAttributeValue("x") + "/" + attr.getAttributeValue("y"), new ArrayList<String>(), 1, 1, attr.getChildText("type"),
					instances);
			newConcept.setNbTestedMatch(0);

			// insert the new concept to get the id
			String newCId = this.addOrUpdateConcept(newConcept, dbName);
			newConcept.setId(newCId);

			concepts.add(newConcept);

			float maxScore = -1;
			Concept matchedC = null;

			// now for this concept, match with currentConcepts in DB
			// ttl by concept
			// boolean ttlArrived = false;
			// count match number for the attribute in ttl
			int counted = 0;
			long startConcept = System.currentTimeMillis();

			for (int i = 0; i < currentConcepts.size(); i++) {

				// optim for NWay
				if (useNWay && k == -1 && maxScore != -1)
					break;

				if (ttlArrived && useNWay)
					break;

				// if enough time for this attribute
				if((System.currentTimeMillis() - startConcept) > timeForEachConcept && useNWay)
					break;

				// count match for K
				counted++;

				Concept ccTmp = currentConcepts.get(i);
				ccTmp.setNbTestedMatch(ccTmp.getNbTestedMatch() + 1);
				// idem for current concept
				newConcept.setNbTestedMatch(newConcept.getNbTestedMatch() + 1);
				// create the concept matcher
				ConceptMatcher cMatcher = new ConceptMatcher(dbName);
				// float[] results = cMatcher.match(ccTmp, newConcept);
				TreeMap<String, Float> results = cMatcher.match(ccTmp, newConcept);

				nbMatches++;

				L.Debug(this.getClass().getSimpleName(),
						"Concept 1 | Concept 2 | StringWordnet | TypeScore | IbScore | StringScore | WordnetScore | ProbaScore", true);
				L.Debug(
						this.getClass().getSimpleName(),
						ccTmp.getName() + " | " + newConcept.getName() + " | " + results.get("stringWordnetScore") + " | " + results.get("typeScore") + " | "
								+ results.get("ibScore") + " | " + results.get("stringScore") + " | " + results.get("wordnetScore") + " | "
								+ results.get("probaScore"), true);

				MongoDBConnector.getInstance().addConceptsMatchScores(ccTmp, newConcept, results.get("stringWordnetScore"), results.get("typeScore"),
						results.get("ibScore"), results.get("stringScore"), results.get("wordnetScore"), results.get("probaScore"), dbName);

				// if concept matched ok
				if (results.get("probaScore") > proba_prod_treeshold) {
					//
					if (results.get("probaScore") > maxScore) {
						maxScore = results.get("probaScore");
						matchedC = ccTmp;
					}
					//
				}
				// update the nbTestedMatch
				this.addOrUpdateConcept(ccTmp, dbName);

				// ttl ?
				// if using ttl
				if (useNWay && k == -1) {
					if (System.currentTimeMillis() > end) {
						ttlArrived = true;
					}
				}
			}

			// if maxScore != -1, found
			if (maxScore != -1) {
				int i = currentConcepts.indexOf(matchedC);

				matchedC.setNbOccurences(matchedC.getNbOccurences() + 1);
				//
				ArrayList<String> names = matchedC.getAlternativeNames();
				names.add(newConcept.getName());
				matchedC.setAlternativeNames(names);
				//
				ArrayList<String> uris = matchedC.getAlternativeUris();
				uris.add(newConcept.getUri());
				matchedC.setAlternativeUris(uris);
				//
				Set<String> inst = matchedC.getInstances();
				inst.addAll(newConcept.getInstances());
				matchedC.setInstances(inst);
				//
				// so remove newConcept now equals ccTmp
				String idToRemove = newConcept.getId();
				newConcept = matchedC;
				currentConcepts.set(i, newConcept);

				// for testing
				if (storeIdMatch == true) {
					// matchedC and newConcept
					Set<String> trueUris = new TreeSet<String>();
					trueUris.add(matchedC.getUri());
					trueUris.addAll(matchedC.getAlternativeUris());
					insertStoredMatchId(trueUris, dbName);
				}
				//

				// update the nbTestedMatch
				String cid = this.addOrUpdateConcept(matchedC, dbName);
				ids += " "+cid;
				// remove newConcept
				this.deleteConceptFromDB(idToRemove, dbName);
				//
				totalMatchesFound++;
			}else{
				ids += " "+newConcept.getId();
			}

			// ok stopped matching
			long localtmp = System.currentTimeMillis();
			if (useNWay && k == -1 && maxScore != -1 && NWayMatchesExpe.TTLandKWriter != null) {
				try {
					NWayMatchesExpe.TTLandKWriter.write((localtmp - start) + ";" + counted + ";" + matchedC.getId() + ";" + StringUtils.normalizeSpace(matchedC.getName()) + ";"
							+ StringUtils.normalizeSpace(newConcept.getName()) + "\n");
					NWayMatchesExpe.TTLandKWriter.flush();
				} catch (IOException e) {
					L.Error(e.getMessage(),e);
				}
			}
			// update time by concept
	    /*if(ttlDivideBy > 1){
		timeForEachConcept = ttl/(long)(ttlDivideBy-1);
	    }*/
		}

		// stats for time by doc
		if (!useNWay && dbName.equals("fullMatchesExpe")) {
			try {
				FullMatchesExpe.writer.write(nbConceptsInDoc + ";" + nbConceptsInDB + ";" + nbMatches
						+";\""+StringUtils.normalizeSpace(source)+"\";"+(System.currentTimeMillis()-start)/1000+"\n");
				FullMatchesExpe.writer.flush();
			} catch (IOException e) {
				L.Error(e.getMessage(),e);
			}
		}

		if(useNWay){
			try {
				NWayMatchesExpe.writer.write(nbConceptsInDoc + ";" + nbConceptsInDB + ";" + nbMatches
						+";\""+StringUtils.normalizeSpace(source)+"\";"+(System.currentTimeMillis()-start)/1000+"\n");
				NWayMatchesExpe.writer.flush();
			} catch (IOException e) {
				L.Error(e.getMessage(),e);
			}
		}
		//

		// nbMatches for this doc for the TTL
		System.out.println(nbConceptsInDoc + ";" + nbConceptsInDB + ";" + nbMatches+";"+totalMatchesFound);

		// get the times
		long endedMatches = System.currentTimeMillis();
		if (minMatchTime == 0)
			minMatchTime = endedMatches - globalTmpStart;
		if (minMatchTime > (endedMatches - globalTmpStart))
			minMatchTime = endedMatches - globalTmpStart;
		if (maxMatchTime < (endedMatches - globalTmpStart))
			maxMatchTime = endedMatches - globalTmpStart;
		totalMatchTime += (endedMatches - globalTmpStart);

	/*String ids = new String();
	for (int i = 0; i < concepts.size(); i++) {
	    ids += " " + concepts.get(i).getId();
	}*/
		ids = ids.trim();

		doc.put("concepts", ids);
		// end of concepts
		currentDB.getCollection("stored_schemas").update(query, doc);
		currentDB.requestDone();
		//

		L.Debug(this.getClass().getSimpleName(), "Found " + concepts.size() + " concepts in this file.", true);

		return object_id;
	}

	public List<DistanceData> getDistancesFromThisDoc(String docId, String dbName){
		List<DistanceData> results = new ArrayList<DistanceData>();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return results;
			}
		}

		// work this db
		currentDB.requestStart();

		// first get the names of documents
		Map<String, String> names = new HashMap<String, String>();
		DBCursor nameCur = currentDB.getCollection("stored_schemas").find();
		while (nameCur.hasNext()) {
			DBObject name = nameCur.next();
			names.put(name.get("_id").toString(), (String) name.get("name"));
		}

		// the query
		BasicDBObject query = new BasicDBObject();
		query.put("distance", new BasicDBObject("$gt", 0));
		//

		DBCursor cur = currentDB.getCollection("distances").find(query).sort(new BasicDBObject("distance", -1));
		while (cur.hasNext()) {
			DBObject obj = cur.next();
			String[] ids = new String[4];
			ids[0] = (String) obj.get("doc_id1");
			ids[1] = names.get(ids[0]);

			ids[2] = (String) obj.get("doc_id2");
			ids[3] = names.get(ids[2]);

			if(ids[0].equals(docId)){
				DistanceData d = new DistanceData(ids[2],ids[3],(Double) obj.get("distance"));
				results.add(d);
			}
			if(ids[2].equals(docId)){
				DistanceData d = new DistanceData(ids[0],ids[1],(Double) obj.get("distance"));
				results.add(d);
			}
		}
		//System.out.println(results.size());
		currentDB.requestDone();
		//

		return results;
	}

	public void deleteConceptFromDB(String id, String dbName) {
		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return;
			}
		}

		// work this db
		currentDB.requestStart();

		DBCollection coll = currentDB.getCollection("concepts");

		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));

		DBObject obj = coll.findOne(query);
		if (obj != null)
			coll.remove(obj);

		currentDB.requestDone();
		//
	}

	/**
	 * Make the file on the system using the object_id of the stored_schema
	 *
	 * @param object_id
	 * @param baseDir
	 * @return
	 */
	public String getFileNameForObject(String object_id, String baseDir, String dbName) {

		String fileName = new String();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return fileName;
			}
		}

		// work this db
		currentDB.requestStart();

		String file_id = new String();

		BasicDBObject query = new BasicDBObject();

		query.put("_id", new ObjectId(object_id));

		L.Debug(this.getClass().getSimpleName(), "OID is " + object_id, true);

		DBCursor cur = currentDB.getCollection("stored_schemas").find(query);

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			file_id = obj.get("file_id").toString();
			break;
		}

		// we have the file id, now search in GridFS
		GridFS fs = new GridFS(currentDB);

		L.Debug(this.getClass().getSimpleName(), "File OID is " + file_id, true);

		query = new BasicDBObject();
		query.put("_id", new ObjectId(file_id));

		GridFSDBFile gridFSFile = fs.findOne(query);

		try {
			File tmpFile = File.createTempFile("schema", null);
			gridFSFile.writeTo(tmpFile);

			FileUtils.copyFile(tmpFile, new File(baseDir + File.separator + tmpFile.getName()));
			fileName = tmpFile.getName();

		} catch (IOException e) {
			L.Error(e.getMessage(),e);
		}

		currentDB.requestDone();
		//

		return fileName;

	}

	@Override
	public List<SchemaData> getSchemas(boolean onlyEdited, String dbName) {

		List<SchemaData> xmls = new ArrayList<SchemaData>();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return xmls;
			}
		}

		// work this db
		currentDB.requestStart();
		DBCollection coll = currentDB.getCollection("stored_schemas");

		DBCursor cur = null;

		// if only edited one
		if (onlyEdited) {
			BasicDBObject query = new BasicDBObject();
			query.put("edit_xml", new BasicDBObject("$ne", ""));
			cur = coll.find(query).sort(new BasicDBObject("_id", -1));
		}

		// get objects
		else
			cur = coll.find().sort(new BasicDBObject("_id", -1));

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			if (obj != null) {
				SchemaData sd = new SchemaData(obj.get("name").toString(), obj.get("source").toString(), obj.get("user").toString(), obj.get("description")
						.toString(), obj.get("_id").toString());
				xmls.add(sd);
			}
		}

		currentDB.requestDone();
		//

		return xmls;
	}

	@Override
	public List<DetectionQualityData> getDetectionQualityList(boolean onlyEdited, String dbName) {

		L.Debug(this.getClass().getSimpleName(), "Generate liste deb.", true);

		List<DetectionQualityData> list = new ArrayList<DetectionQualityData>();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return list;
			}
		}

		// work this db
		currentDB.requestStart();

		DBCollection coll = currentDB.getCollection("stored_schemas");

		DBCursor cur = null;

		// if only edited one
		if (onlyEdited) {
			BasicDBObject query = new BasicDBObject();
			query.put("edit_xml", new BasicDBObject("$ne", ""));
			cur = coll.find(query);
		}

		// get objects
		else
			cur = coll.find();

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			if (obj != null) {
				String name = obj.get("name").toString();
				String description = obj.get("description").toString();
				String source = obj.get("source").toString();
				String autoXML = obj.get("auto_xml").toString();
				String editXML = obj.get("edit_xml").toString();
				String objectId = obj.get("_id").toString();

				String publication_id = "";
				if (obj.get("publication_id") != null)
					publication_id = obj.get("publication_id").toString();

				//
				boolean cc_pb = false;
				if (obj.get("cc_pb") != null)
					cc_pb = new Boolean(obj.get("cc_pb").toString());

				boolean attr_pb = false;
				if (obj.get("attr_pb") != null)
					attr_pb = new Boolean(obj.get("attr_pb").toString());

				String detec_pb_desc = new String();
				if (obj.get("detect_pb_desc") != null)
					detec_pb_desc = obj.get("detect_pb_desc").toString();
				//

				// trashed
				boolean trashed = false;
				if (obj.get("trashed") != null)
					trashed = new Boolean(obj.get("trashed").toString());
				//

				boolean neverEdited = false;

				double precision = 0.0;
				double recall = 0.0;
				double fmeasure = 0.0;

				//
				List<ConnexComposant> ccList = new ArrayList<ConnexComposant>();

				// never edited, so no prec/recall/fmea
				if (editXML != null && editXML.equals("")) {
					neverEdited = true;
					precision = -1;
					recall = -1;
					fmeasure = -1;
				} else {
					// now find the precision/recall/fmeasure
					WSMatchXMLDiff diff = new WSMatchXMLDiff();
					try {
						HashMap<ConnexComposant, HashMap<String, Integer>> res = diff.getDiff(autoXML, editXML);

						int sumCCSurface = 0;
						double sumCCPrec = 0.0;
						double sumCCRec = 0.0;
						double sumCCFmea = 0.0;

						// for each cc, local pr/rec/fmea
						for (ConnexComposant cc : res.keySet()) {

							double ccPrec = 0.0;
							double ccRecall = 0.0;
							double ccFmea = 0.0;

							// perfect case
							if (res.get(cc).get("intersect") == res.get(cc).get("edit") && res.get(cc).get("intersect") == res.get(cc).get("auto")) {
								ccPrec = 1.0;
								ccRecall = 1.0;
								ccFmea = 1.0;
							}

							// precision = auto / int
							if (res.get(cc).get("auto") == 0) {
								// TODO gérer ce cas
							} else {
								ccPrec = (double) res.get(cc).get("intersect") / (double) res.get(cc).get("auto");
							}

							// rappel = auto / edit
							if (res.get(cc).get("edit") == 0) {
								// TODO gérer ce cas
							} else {
								ccRecall = (double) res.get(cc).get("intersect") / (double) res.get(cc).get("edit");
							}

							// fmea = 2x(PR) / (P+R)
							if ((ccPrec + ccRecall) == 0) {
								// TODO gérer ce cas
							} else {
								ccFmea = ((double) 2 * (ccPrec * ccRecall)) / (ccPrec + ccRecall);
							}

							// set sumCCSurface
							// sumCCSurface +=
							// (cc.getEndX()-cc.getStartX())*(cc.getEndY()-cc.getStartY());
							sumCCSurface++;

							sumCCPrec += /*
					  * (double)(cc.getEndX()-cc.getStartX())
					  * *(cc.getEndY()-cc.getStartY())*
					  */ccPrec;
							sumCCRec += /*
					 * (double)(cc.getEndX()-cc.getStartX())*
					 * (cc.getEndY()-cc.getStartY())*
					 */ccRecall;
							sumCCFmea += /*
					  * (double)(cc.getEndX()-cc.getStartX())
					  * *(cc.getEndY()-cc.getStartY())*
					  */ccFmea;

							//
							cc.setPrecision(ccPrec);
							cc.setRecall(ccRecall);
							cc.setFmeas(ccFmea);

							// add to list of CCs
							ccList.add(cc);
						}

						// set the global values
						precision = sumCCPrec / (double) sumCCSurface;
						recall = sumCCRec / (double) sumCCSurface;
						fmeasure = sumCCFmea / (double) sumCCSurface;

					} catch (Exception saxEx) {
						System.out.println("Can't parse for object : " + objectId);
					}
				}
				// add
				if (Double.isNaN(precision))
					precision = 0.0;
				if (Double.isNaN(recall))
					recall = 0.0;
				if (Double.isNaN(fmeasure))
					fmeasure = 0.0;

				DetectionQualityData data = new DetectionQualityData(name, description, source, precision, recall, fmeasure, objectId, neverEdited, cc_pb,
						attr_pb, detec_pb_desc, trashed, publication_id);

				data.setConnexComposants(ccList);

				list.add(data);
			}
		}
		currentDB.requestDone();
		//

		Collections.sort(list, new FmeasureComparator());
		// Collections.reverse(list);

		L.Debug(this.getClass().getSimpleName(), "Generate liste end.", true);

		return list;
	}

	@Override
	public String updateXML(String objectId, String edit_xml, String name, String description, boolean ccDetectionPb, boolean attrDetectionPb, String detectPb,
							boolean trashed, String dbName) {
		DB currentDB = dbs.get(dbName);

		L.Debug(this.getClass().getSimpleName(), "Update " + objectId, true);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return objectId;
			}
		}

		// work this db
		currentDB.requestStart();

		// System.out.println(edit_xml);

		BasicDBObject newFields = new BasicDBObject();
		newFields.put("edit_xml", edit_xml);
		newFields.put("name", name);
		newFields.put("description", description);
		newFields.put("cc_pb", ccDetectionPb);
		newFields.put("attr_pb", attrDetectionPb);
		newFields.put("detect_pb_desc", detectPb);
		newFields.put("trashed", trashed);

		BasicDBObject newDocument = new BasicDBObject().append("$set", newFields);

		BasicDBObject query = new BasicDBObject();
		query.append("_id", new ObjectId(objectId));
		currentDB.getCollection("stored_schemas").update(query, newDocument);

	/*
	 * BasicDBObject query = new BasicDBObject(); query.put("_id", new
	 * ObjectId(objectId));
	 * 
	 * DBCursor cur = currentDB.getCollection("stored_schemas").find(query);
	 * 
	 * while (cur.hasNext()) { DBObject obj = cur.next(); if (obj != null) {
	 * obj.put("edit_xml", edit_xml); obj.put("name", name);
	 * obj.put("description", description);
	 * 
	 * // for detection problem obj.put("cc_pb", ccDetectionPb);
	 * obj.put("attr_pb", attrDetectionPb); obj.put("detect_pb_desc",
	 * detectPb);
	 * 
	 * // trashed obj.put("trashed", trashed);
	 * 
	 * currentDB.getCollection("stored_schemas").update(query, obj); } }
	 */

		currentDB.requestDone();
		//
		return objectId;
	}

	@Override
	public DetectionQualityData getDetectionQualityData(String objectId, String dbName) {

		DetectionQualityData data = new DetectionQualityData();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return null;
			}
		}

		// work this db
		currentDB.requestStart();

		DBCollection coll = currentDB.getCollection("stored_schemas");

		DBCursor cur = null;

		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(objectId));
		cur = coll.find(query);

		if (cur.hasNext()) {
			DBObject obj = cur.next();
			if (obj != null) {
				String name = obj.get("name").toString();
				String description = obj.get("description").toString();
				String source = obj.get("source").toString();
				String autoXML = obj.get("auto_xml").toString();
				String editXML = obj.get("edit_xml").toString();

				String publication_id = "";
				if (obj.get("publication_id") != null)
					publication_id = obj.get("publication_id").toString();

				//
				boolean cc_pb = false;
				if (obj.get("cc_pb") != null)
					cc_pb = new Boolean(obj.get("cc_pb").toString());

				boolean attr_pb = false;
				if (obj.get("attr_pb") != null)
					attr_pb = new Boolean(obj.get("attr_pb").toString());

				String detec_pb_desc = new String();
				if (obj.get("detect_pb_desc") != null)
					detec_pb_desc = obj.get("detect_pb_desc").toString();
				//

				// trashed
				boolean trashed = false;
				if (obj.get("trashed") != null)
					trashed = new Boolean(obj.get("trashed").toString());
				//

				boolean neverEdited = false;

				double precision = 0.0;
				double recall = 0.0;
				double fmeasure = 0.0;

				//
				List<ConnexComposant> ccList = new ArrayList<ConnexComposant>();

				// never edited, so no prec/recall/fmea
				if (editXML != null && editXML.equals("")) {
					neverEdited = true;
					precision = -1;
					recall = -1;
					fmeasure = -1;
				} else {
					// now find the precision/recall/fmeasure
					WSMatchXMLDiff diff = new WSMatchXMLDiff();
					try {
						HashMap<ConnexComposant, HashMap<String, Integer>> res = diff.getDiff(autoXML, editXML);

						int sumCCSurface = 0;
						double sumCCPrec = 0.0;
						double sumCCRec = 0.0;
						double sumCCFmea = 0.0;

						// for each cc, local pr/rec/fmea
						for (ConnexComposant cc : res.keySet()) {

							double ccPrec = 0.0;
							double ccRecall = 0.0;
							double ccFmea = 0.0;

							// perfect case
							if (res.get(cc).get("intersect") == res.get(cc).get("edit") && res.get(cc).get("intersect") == res.get(cc).get("auto")) {
								ccPrec = 1.0;
								ccRecall = 1.0;
								ccFmea = 1.0;
							}

							// precision = auto / int
							if (res.get(cc).get("auto") == 0) {
								// TODO gérer ce cas
							} else {
								ccPrec = (double) res.get(cc).get("intersect") / (double) res.get(cc).get("auto");
							}

							// rappel = auto / edit
							if (res.get(cc).get("edit") == 0) {
								// TODO gérer ce cas
							} else {
								ccRecall = (double) res.get(cc).get("intersect") / (double) res.get(cc).get("edit");
							}

							// fmea = 2x(PR) / (P+R)
							if ((ccPrec + ccRecall) == 0) {
								// TODO gérer ce cas
							} else {
								ccFmea = ((double) 2 * (ccPrec * ccRecall)) / (ccPrec + ccRecall);
							}

							// set sumCCSurface
							// sumCCSurface +=
							// (cc.getEndX()-cc.getStartX())*(cc.getEndY()-cc.getStartY());
							sumCCSurface++;

							sumCCPrec += /*
					  * (double)(cc.getEndX()-cc.getStartX())
					  * *(cc.getEndY()-cc.getStartY())*
					  */ccPrec;
							sumCCRec += /*
					 * (double)(cc.getEndX()-cc.getStartX())*
					 * (cc.getEndY()-cc.getStartY())*
					 */ccRecall;
							sumCCFmea += /*
					  * (double)(cc.getEndX()-cc.getStartX())
					  * *(cc.getEndY()-cc.getStartY())*
					  */ccFmea;

							//
							cc.setPrecision(ccPrec);
							cc.setRecall(ccRecall);
							cc.setFmeas(ccFmea);

							// add to list of CCs
							ccList.add(cc);
						}

						// set the global values
						precision = sumCCPrec / (double) sumCCSurface;
						recall = sumCCRec / (double) sumCCSurface;
						fmeasure = sumCCFmea / (double) sumCCSurface;

					} catch (Exception saxEx) {
						System.out.println("Can't parse for object : " + objectId);
					}
				}
				// add
				if (Double.isNaN(precision))
					precision = 0.0;
				if (Double.isNaN(recall))
					recall = 0.0;
				if (Double.isNaN(fmeasure))
					fmeasure = 0.0;

				data = new DetectionQualityData(name, description, source, precision, recall, fmeasure, objectId, neverEdited, cc_pb, attr_pb, detec_pb_desc,
						trashed, publication_id);

				data.setConnexComposants(ccList);
			}
		}
		currentDB.requestDone();
		//

		return data;
	}

	public int getFrequenceForConceptType(String type, String dbName) {

		int freq = 1;

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return freq;
			}
		}

		// work this db
		currentDB.requestStart();

		DBCursor cur = currentDB.getCollection("frequences").find();

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			if (obj.get("type").toString().trim().equals(type.trim())) {
				freq = new Integer(obj.get("freq").toString());
			}
		}

		currentDB.requestDone();
		//

		return freq;
	}

	/**
	 * Get the list of concepts
	 *
	 * @param dbName
	 *            The name of the database
	 * @return The list of k concepts ordered by decreasing nubOccurences
	 */

	public ArrayList<Concept> getConcepts(String dbName, int k) {

		ArrayList<Concept> concepts = new ArrayList<Concept>();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return null;
			}
		}

		// work this db
		currentDB.requestStart();

		DBCollection coll = currentDB.getCollection("concepts");

		DBCursor cur = null;

		if (k != -1) {

			BasicDBObject query = new BasicDBObject();
			query.put("nbOccurences", new BasicDBObject("$gt", "1"));

			cur = coll.find(query).sort(new BasicDBObject("nbOccurences", -1)).limit(k);
			if (cur.count() < k)
				cur = coll.find();
		} else {
			cur = coll.find().sort(new BasicDBObject("nbOccurences", -1));
		}

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			if (obj != null) {

				String[] strInst = obj.get("instances").toString().split("\\s+");
				Set<String> set = new TreeSet<String>();
				for (String s : strInst)
					set.add(s);

				concepts.add(new Concept(obj.get("_id").toString(), obj.get("name").toString(), new ArrayList<String>(Arrays.asList(((BasicDBList) obj
						.get("alternativeNames")).toArray(new String[((BasicDBList) obj.get("alternativeNames")).size()]))), obj.get("uri").toString(),
						new ArrayList<String>(Arrays.asList(((BasicDBList) obj.get("alternativeUris")).toArray(new String[((BasicDBList) obj
								.get("alternativeUris")).size()]))), new Integer(obj.get("nbOccurences").toString()).intValue(), new Integer(obj.get(
						"nbTestedMatch").toString()).intValue(), obj.get("type").toString(), set));

			}

			currentDB.requestDone();
			//
		}
		return concepts;
	}

	/**
	 * Add new concept or update existing one
	 *
	 * @param c
	 *            The concept to add or update
	 * @param dbName
	 *            Name of database
	 * @return Object id in database
	 */

	public String addOrUpdateConcept(Concept c, String dbName) {

		String object_id = null;
		if (c.getId() != null && !c.getId().equals(""))
			object_id = c.getId();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return null;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		// make object
		BasicDBObject doc = new BasicDBObject();

		doc.put("name", c.getName());
		doc.put("alternativeNames", c.getAlternativeNames().toArray());

		doc.put("uri", c.getUri());
		doc.put("alternativeUris", c.getAlternativeUris().toArray());

		doc.put("nbOccurences", c.getNbOccurences());
		doc.put("nbTestedMatch", c.getNbTestedMatch());

		doc.put("type", c.getType());

		String strInstances = new String();
		Set<String> set = c.getInstances();
		for (String s : set)
			strInstances += s.trim() + " ";

		doc.put("instances", strInstances);

	/*
	 * BasicDBObject docIdsAndPath = new BasicDBObject();
	 * 
	 * Set<String> keys = c.getDocIdsAndPath().keySet();
	 * 
	 * for(String k : keys){ docIdsAndPath.put("doc_id", k);
	 * docIdsAndPath.put("path", c.getDocIdsAndPath().get(k)); }
	 * 
	 * doc.put("docsIdsAndPath", docIdsAndPath);
	 * 
	 * doc.put("occurence", keys.size());
	 */

		// for BSON max size
		int bsonSize = DefaultDBEncoder.FACTORY.create().writeObject(new BasicOutputBuffer(), doc);
		if (bsonSize > 16777216) {
			doc.remove("instances");
		}
		//

		if (currentDB.collectionExists("concepts")) {
			// update
			if (object_id != null) {
				BasicDBObject query = new BasicDBObject();
				query.put("_id", new ObjectId(object_id));
				currentDB.getCollection("concepts").update(query, doc);
			}
			// insert
			else {
				currentDB.getCollection("concepts").insert(doc);
				ObjectId id = (ObjectId) doc.get("_id");
				object_id = id.toString();
			}
		} else {
			currentDB.createCollection("concepts", doc);
			// update
			if (object_id != null) {
				BasicDBObject query = new BasicDBObject();
				query.put("_id", new ObjectId(object_id));
				currentDB.getCollection("concepts").update(query, doc);
			}
			// insert
			else {
				currentDB.getCollection("concepts").insert(doc);
				ObjectId id = (ObjectId) doc.get("_id");
				object_id = id.toString();
			}
		}

		currentDB.requestDone();
		//
		L.Debug(this.getClass().getSimpleName(), "Concept added, id is " + object_id, true);
		return object_id;
	}

	/**
	 * Add the result of matching concepts, used to compute probas
	 *
	 * @param c1
	 * @param c2
	 * @param score
	 * @param freq
	 * @param ibScore
	 * @param dbName
	 */

	public void addConceptsMatchScores(Concept c1, Concept c2, float stringWordnetScore, float typeScore, float ibScore, float stringScore, float wordnetScore,
									   float proba, String dbName) {

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		// make object
		BasicDBObject doc = new BasicDBObject();

		doc.put("c1ObjectId", c1.getId());
		doc.put("c1Name", c1.getName());
		doc.put("c1AlternativeNames", c1.getAlternativeNames().toArray());

		doc.put("c1Uri", c1.getUri());
		doc.put("c1AlternativeUris", c1.getAlternativeUris().toArray());

		doc.put("c1NbOccurences", c1.getNbOccurences());
		doc.put("c1NbTestedMatch", c1.getNbTestedMatch());

		doc.put("c1Type", c1.getType());
		doc.put("c1Instances", c1.getInstances());

		doc.put("c2ObjectId", c2.getId());
		doc.put("c2Name", c2.getName());
		doc.put("c2AlternativeNames", c2.getAlternativeNames().toArray());

		doc.put("c2Uri", c2.getUri());
		doc.put("c2AlternativeUris", c2.getAlternativeUris().toArray());

		doc.put("c2NbOccurences", c2.getNbOccurences());
		doc.put("c2NbTestedMatch", c2.getNbTestedMatch());

		doc.put("c2Type", c2.getType());
		doc.put("c2Instances", c2.getInstances());

		doc.put("stringWordnetScore", stringWordnetScore);
		doc.put("typeScore", typeScore);
		doc.put("ibScore", ibScore);
		doc.put("stringScore", stringScore);
		doc.put("wordnetScore", wordnetScore);

		doc.put("probaScore", proba);

		doc.put("expert", 0);

		// for BSON max size
		int bsonSize = DefaultDBEncoder.FACTORY.create().writeObject(new BasicOutputBuffer(), doc);
		if (bsonSize > 16777216) {
			doc.remove("c1Instances");
			doc.remove("c2Instances");
		}
		//

		if (currentDB.collectionExists("conceptsMatchScores")) {
			currentDB.getCollection("conceptsMatchScores").insert(doc);
		} else {
			currentDB.createCollection("conceptsMatchScores", doc);
			currentDB.getCollection("conceptsMatchScores").insert(doc);
		}

		currentDB.requestDone();
		//
	}

	public ArrayList<MongoConceptMatchScore> getConceptsMatchScores(String dbName) {

		ArrayList<MongoConceptMatchScore> scores = new ArrayList<MongoConceptMatchScore>();

		DB currentDB = dbs.get(dbName);

		if (currentDB == null) {
			currentDB = mongo.getDB(dbName);
			// doesn't exist, create it
			if (currentDB == null) {
				return null;
			}
		}

		dbs.put(dbName, currentDB);

		// work this db
		currentDB.requestStart();

		DBCollection coll = currentDB.getCollection("conceptsMatchScores");

		DBCursor cur = null;

		cur = coll.find();

		while (cur.hasNext()) {
			DBObject obj = cur.next();
			if (obj != null) {

				MongoConceptMatchScore conceptScore = new MongoConceptMatchScore();

				conceptScore.setC1Name(obj.get("c1Name").toString());
				conceptScore.setC1AlternativeNames(Arrays.asList(((BasicDBList) obj.get("c1AlternativeNames")).toArray(new String[((BasicDBList) obj
						.get("c1AlternativeNames")).size()])));

				conceptScore.setC1Uri(obj.get("c1Uri").toString());
				conceptScore.setC1AlternativeUris(Arrays.asList(((BasicDBList) obj.get("c1AlternativeUris")).toArray(new String[((BasicDBList) obj
						.get("c1AlternativeUris")).size()])));

				try {
					conceptScore.setC1NbOccurences((Integer) obj.get("c1NbOccurences"));
					conceptScore.setC1NbTestedMatch((Integer) obj.get("c1NbTestedMatch"));
				} catch (ClassCastException cce) {
					conceptScore.setC1NbOccurences(((Long) obj.get("c1NbOccurences")).intValue());
					conceptScore.setC1NbTestedMatch(((Long) obj.get("c1NbTestedMatch")).intValue());
				}

				conceptScore.setC1Type(obj.get("c1Type").toString());
				conceptScore.setC1Instances(obj.get("c1Instances").toString());

				conceptScore.setC2Name(obj.get("c2Name").toString());
				conceptScore.setC2AlternativeNames(Arrays.asList(((BasicDBList) obj.get("c2AlternativeNames")).toArray(new String[((BasicDBList) obj
						.get("c2AlternativeNames")).size()])));

				conceptScore.setC2Uri(obj.get("c2Uri").toString());
				conceptScore.setC2AlternativeUris(Arrays.asList(((BasicDBList) obj.get("c2AlternativeUris")).toArray(new String[((BasicDBList) obj
						.get("c2AlternativeUris")).size()])));

				try {
					conceptScore.setC1NbOccurences((Integer) obj.get("c2NbOccurences"));
					conceptScore.setC1NbTestedMatch((Integer) obj.get("c2NbTestedMatch"));
				} catch (ClassCastException cce) {
					conceptScore.setC1NbOccurences(((Long) obj.get("c2NbOccurences")).intValue());
					conceptScore.setC1NbTestedMatch(((Long) obj.get("c2NbTestedMatch")).intValue());
				}

				conceptScore.setC2Type(obj.get("c2Type").toString());
				conceptScore.setC2Instances(obj.get("c2Instances").toString());

				conceptScore.setStringWordnetScore(((Double) obj.get("stringWordnetScore")).floatValue());
				conceptScore.setTypeScore(((Double) obj.get("typeScore")).floatValue());
				conceptScore.setIbScore(((Double) obj.get("ibScore")).floatValue());
				if (obj.get("stringScore") != null)
					conceptScore.setStringScore(((Double) obj.get("stringScore")).floatValue());
				else
					conceptScore.setStringScore(0);
				if (obj.get("wordnetScore") != null)
					conceptScore.setWordnetScore(((Double) obj.get("wordnetScore")).floatValue());
				else
					conceptScore.setWordnetScore(0);
				if (obj.get("probaScore") != null)
					conceptScore.setProbaScore(((Double) obj.get("probaScore")).floatValue());
				else
					conceptScore.setProbaScore(0);

				conceptScore.setExpert(new Integer(obj.get("expert").toString()));

				// set ids
				if (obj.get("c1ObjectId") != null)
					conceptScore.setC1ObjectId(obj.get("c1ObjectId").toString());
				if (obj.get("c2ObjectId") != null)
					conceptScore.setC2ObjectId(obj.get("c2ObjectId").toString());

				scores.add(conceptScore);
			}
		}

		currentDB.requestDone();
		//

		return scores;
	}

	public Mongo getMongo() {
		return mongo;
	}

}
