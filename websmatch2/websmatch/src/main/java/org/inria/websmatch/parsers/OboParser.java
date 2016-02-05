package org.inria.websmatch.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.inria.websmatch.utils.L;

public class OboParser extends OntoParser{

    public OboParser(String filePath) {
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

		// if line contains [Term], start the term
		if (currentLine.trim().startsWith("[Term]")) {
		    if(term != null) this.getTerms().add(term);
		    term = new Term();
		}
		else {
		    // if start with id:
		    if (currentLine.trim().startsWith("id:")) {
			term.setId(currentLine.trim().substring(3));
		    }
		    // if start with name:
		    if (currentLine.trim().startsWith("name:")) {
			term.setName(currentLine.trim().substring(5));
		    }
		    // if start with def:
		    if (currentLine.trim().startsWith("def:")) {
			term.setDef(currentLine.trim().substring(4));
		    }
		    // if start with synonym:
		    if (currentLine.trim().startsWith("synonym:")) {
			term.getSynonyms().add(currentLine.trim().substring(8+2,currentLine.trim().lastIndexOf("\"")));
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
