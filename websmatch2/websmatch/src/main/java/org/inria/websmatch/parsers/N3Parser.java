package org.inria.websmatch.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.L;

public class N3Parser {
    
    private String filePath;
    List<String> mapObjects;

    public N3Parser(String filePath) {
	this.setFilePath(filePath);
	mapObjects = new ArrayList<String>();
    }

    public void setFilePath(String filePath) {
	this.filePath = filePath;
    }

    public String getFilePath() {
	return filePath;
    }

    public List<String> getMapObjects() {
	return mapObjects;
    }

    public void parse() {

	// open file
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new FileReader(filePath));

	    String currentLine;

	    while ((currentLine = br.readLine()) != null) {

		if (currentLine.trim().startsWith("d2rq:propertyDefinitionLabel")) {
		    this.getMapObjects().add(currentLine.substring(currentLine.indexOf("\""),currentLine.lastIndexOf("\"")));
		}	
	    }

	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(), e);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
    }

}
