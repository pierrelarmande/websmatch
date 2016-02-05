package org.inria.websmatch.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.L;

public class CSVParser {
    
    private String filePath;
    private String[] header;
    private List<String[]> instances;
    private String delimiter = ";";
    
    public CSVParser(String filePath){
	this.setFilePath(filePath);
	instances = new ArrayList<String[]>();
    }
    
    public CSVParser(String filePath, String delimiter){
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

    public void setInstances(List<String[]> instances) {
	this.instances = instances;
    }

    public List<String[]> getInstances() {
	return instances;
    }
    
    public void parse(){
	
	// open file
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new FileReader(filePath));

	    String currentLine;
	    boolean firstLine = true;

	    while ((currentLine = br.readLine()) != null) {

		if(firstLine){
		    header = currentLine.split(delimiter);
		    firstLine = false;
		}else{
		    this.getInstances().add(currentLine.split(delimiter));
		}
		
	    }

	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(), e);
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
