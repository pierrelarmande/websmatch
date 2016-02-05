package org.inria.websmatch.matchers.base;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.db.MongoDBConnector;

public class ConceptTypeMatcher {
    
    private String dbName;
    
    public ConceptTypeMatcher(String dbName){	
	this.dbName = dbName;	
    }
    
    public float match(String c1Type, String c2Type){	
	if(c1Type.trim().equals(c2Type.trim())){
	    // get the freq in db
	    MongoDBConnector db = MongoDBConnector.getInstance();
	    int freq = db.getFrequenceForConceptType(c1Type, dbName);
	    L.Debug(this.getClass().getSimpleName(),"Frequence : "+freq,true);
	    return (float)1/(float)freq;
	}
	
	else return (float)0;	
    }
}
