package org.inria.websmatch.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.inria.websmatch.utils.L;

public class ManualAnnotationParser {

    private String filePath;
    private String[] header;
    private List<String[]> mappings;
    private String delimiter = ";";

    public ManualAnnotationParser(String filePath) {
	this.setFilePath(filePath);
	mappings = new ArrayList<String[]>();
    }

    public ManualAnnotationParser(String filePath, String delimiter) {
	this(filePath);
	this.delimiter = delimiter;
    }

    public void setFilePath(String filePath) {
	this.filePath = filePath;
    }

    public String getFilePath() {
	return filePath;
    }

    public void setHeader(String[] header) {
	this.header = header;
    }

    public String[] getHeader() {
	return header;
    }

    public void setMappings(List<String[]> instances) {
	this.mappings = instances;
    }

    public List<String[]> getMappings() {
	return mappings;
    }

    public void parse() {

	// open file
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new FileReader(filePath));

	    String currentLine;
	    boolean firstLine = true;

	    while ((currentLine = br.readLine()) != null) {

		if (firstLine) {
		    // header = currentLine.split(delimiter);
		    firstLine = false;
		} else {
		    if(currentLine.indexOf(delimiter) != -1){
			// 0 is instance name, 1 is annotation name
			String[] tmp = currentLine.split(delimiter);
			String[] mapping = new String[2];
			if(tmp.length >= 2 && tmp[0] != null && tmp[1] != null){
			    mapping[0] = tmp[0];
			    mapping[1] = tmp[1];
			    this.getMappings().add(mapping);
			}
		    }
		}

	    }

	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(),e);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}

    }

    public String getDelimiter() {
	return delimiter;
    }

    public void setDelimiter(String delimiter) {
	this.delimiter = delimiter;
    }
}
