package org.inria.websmatch.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.inria.websmatch.utils.L;

public class RDFParser extends OntoParser{

        public RDFParser(String filePath) {
    		super(filePath);
        }

        @Override
        public void parse() {

    	// open file
    	BufferedReader br = null;
    	try {
    	    br = new BufferedReader(new FileReader(filePath));

    	    String currentLine;
    	    Term term = null;

    	    while ((currentLine = br.readLine()) != null) {

    		// start the term
    		if (currentLine.trim().startsWith("<rdf:Description rdf:about=\"")) {
    		    if(term != null && term.getId().indexOf(':') != -1) this.getTerms().add(term);
    		    term = new Term();
    		    term.setId(currentLine.trim().substring(currentLine.trim().lastIndexOf("/")+1,currentLine.trim().length()-2));
    		}
    		else {   		
    		    if (currentLine.trim().startsWith("<rdfs:label>")) {
    			term.setName(currentLine.trim().substring(12,currentLine.trim().lastIndexOf('<')));
    		    }
    		    else if (currentLine.trim().startsWith("<rdfs:comment>")) {
    			term.setDef(currentLine.trim().substring(14,currentLine.trim().lastIndexOf('<')));
    		    }		       
    		}
    	    }

    	} catch (FileNotFoundException e) {
    	    L.Error(e.getMessage(),e);
    	} catch (IOException e) {
    	    L.Error(e.getMessage(),e);
    	}
        }
}
