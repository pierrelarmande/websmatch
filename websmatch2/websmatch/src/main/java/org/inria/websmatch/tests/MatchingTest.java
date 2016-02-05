package org.inria.websmatch.tests;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.matchers.base.DocumentMatcher;
import org.inria.websmatch.utils.L;

import system.Configs;
import tools.wordnet.WordNetHelper;

public class MatchingTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
	
	// init
	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	}
	try {
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	}
	//
	
	//MatchersList list = MatchersList.getInstance();
	//String[] matchers = list.getMatchers();
	
	/*for(int i = 0; i < matchers.length; i++){
	    AttributeMatcher matcher = new AttributeMatcher(matchers[i]);
	    System.out.println("Matcher name : " + matchers[i] + " Score : " + matcher.match(new String[]{"bonjour",""},new String[]{"bon jour",""}));
	}*/
	
	// test 2 columns
	/*ColumnMatcher cMatcher = new ColumnMatcher();
	System.out.println("Instance based : "+cMatcher.match(new String[]{"villes","agde béziers marseille"}, new String[]{"cité","narbonne béziers toulouse"}));
	*/
	
	// test 2 documents
	// get the 2 documents
	MongoDBConnector connector = MongoDBConnector.getInstance();
	
	// doc 201-0.xls
	String strDoc1 = connector.getEditedXML("4f4272e1e4b0ac50d2391896", "datapublica");
	// doc 202-0.xls
	String strDoc2 = connector.getEditedXML("4f427df5e4b0ac50d2391e10", "datapublica");
	//
	
	DocumentMatcher dMatcher = new DocumentMatcher();
	
	//System.out.println("Doc1 : "+strDoc1);
	//System.out.println("Doc2 : "+strDoc2);
	
	float dist = dMatcher.computeDistance(dMatcher.matchDocuments(strDoc1, strDoc2));
	
	System.out.println("Distance : "+dist);
	
	// dist = (float) 0.1;
	
	// insert distance
	// System.out.println("Id : "+connector.insertOrUpdateDistance("4f4280f6e4b0ac50d2391fd3", "4f42b26ee4b0ac50d239387c", dist, "", "datapublica"));
	//
	
	// test 2 tables
	// get the 2 documents

	/*
	// doc 201-0.xls
	strDoc1 = connector.getEditedXML("4f4280f6e4b0ac50d2391fd3", "datapublica");
	// doc 202-0.xls
	strDoc2 = connector.getEditedXML("4f42b26ee4b0ac50d239387c", "datapublica");
	//
	
	TableMatcher tMatcher = new TableMatcher();
	
	// get the first table in each doc
	WSMatchXMLLoader docLoader1 = new WSMatchXMLLoader(strDoc1);
	Document doc1 = docLoader1.getDocument();
	
	WSMatchXMLLoader docLoader2 = new WSMatchXMLLoader(strDoc2);
	Document doc2 = docLoader2.getDocument();
	
	// go throught attributes
	Filter tableFilter = new ElementFilter("table", null);
	Iterator<Element> doc1Table = doc1.getRootElement().getDescendants(tableFilter);
	
	while(doc1Table.hasNext()){
	    Element table1 = doc1Table.next();
	    
	    Iterator<Element> doc2Table = doc2.getRootElement().getDescendants(tableFilter);
	    
	    while(doc2Table.hasNext()){
		Element table2 = doc2Table.next();
		
		System.out.println("Table match score : "+tMatcher.match(table1, table2));
	    }    
	}
	*/
	
    }
}
