package org.inria.websmatch.parsers;

import java.util.ArrayList;
import java.util.List;

public abstract class OntoParser {
    
    String filePath;
    List<Term> terms;
    
    public OntoParser(String filePath){
	this.setFilePath(filePath);
    	terms = new ArrayList<Term>();
    }
    
    public void setFilePath(String filePath) {
	this.filePath = filePath;
    }

    public String getFilePath() {
	return filePath;
    }

    public List<Term> getTerms() {

	return terms;
    }
    
    public abstract void parse();
}
