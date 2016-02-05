package org.inria.websmatch.matchers.base;

import java.util.HashMap;
import java.util.Iterator;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

public class TableMatcher {
    
    private MatchersList list;
    private String[] matchers;
    
    public TableMatcher(){
	// use all technics for matching attributes
	list = MatchersList.getInstance();
	matchers = list.getMatchers();
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Element[],Float> match(Element table1, Element table2){
	
	HashMap<Element[],Float> scores = new HashMap<Element[],Float>();
	
	// go throught attributes
	Filter attributeFilter = new ElementFilter("attribute", null);
	Iterator<Element> doc1Attr = table1.getDescendants(attributeFilter);
			
	// iterate through attributes
	// we use an average... to be enhanced
	while(doc1Attr.hasNext()){
	    Element attr1 = doc1Attr.next();
	    Iterator<Element> doc2Attr = table2.getDescendants(attributeFilter);
	    while(doc2Attr.hasNext()){
		Element attr2 = doc2Attr.next();
		//
		float avg = 0;
		//
		for(int i = 0; i < matchers.length; i++){
		    AttributeMatcher matcher = new AttributeMatcher(matchers[i]);		
		    avg = avg + matcher.match(new String[]{attr1.getValue().trim(),""},new String[]{attr2.getValue().trim(),""});
		}
		avg = avg/(float)matchers.length;
		scores.put(new Element[]{attr1,attr2}, avg);
	    }
	}
	// we have to choose the best matches
	
	//
	return scores;		
    }

}
