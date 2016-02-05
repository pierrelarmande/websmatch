package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;
import java.util.ArrayList;



public class SimpleSheet implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -906520563774071109L;
    private String title;
    private SimpleCell[][] cells;
    private int schemaId;
    private ArrayList<ConnexComposant> connexComps;
    // private ArrayList<String> criters;
    
    private String username;
    private String filename;
    
    private String storedName;
    private String storedDescription;
    
    public SimpleSheet(){
	super();
	title = new String();
	cells = new SimpleCell[0][0];
	setSchemaId(-1);
	setConnexComps(new ArrayList<ConnexComposant>());
	username = new String();
	filename = new String();
	setStoredName(new String());
	setStoredDescription(new String());
	//setCriters(new ArrayList<String>());
    }
    
    public SimpleSheet(String title, SimpleCell[][] cells, int schemaId, String uname, String fname, String storedName, String storedDesciption){
	
	super();
	
	this.setTitle(title);
	this.setCells(cells);
	this.setSchemaId(schemaId);
	
	this.username = uname;
	this.filename = fname;
	
	setStoredName(storedName);
	setStoredDescription(storedDesciption);
	
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getTitle() {
	return title;
    }

    public void setCells(SimpleCell[][] cells) {
	this.cells = cells;
    }

    public SimpleCell[][] getCells() {
	return cells;
    }

    public void setSchemaId(int schemaId) {
	this.schemaId = schemaId;
    }

    public int getSchemaId() {
	return schemaId;
    }

    public void setConnexComps(ArrayList<ConnexComposant> connexComps) {
	this.connexComps = connexComps;
    }

    public ArrayList<ConnexComposant> getConnexComps() {
	return connexComps;
    }
    
    /**
     * Set all the cells to not attribute
     */
    
    public void setNotAttributeCells(String username, String filename){
	for(int row = 0; row<cells.length; row++){
	    for(int col = 0; col<cells[row].length; col++){
		cells[row][col].setAttribute(false);
		cells[row][col].setFilename(filename);
		cells[row][col].setUsername(username);
	    }  
	}
    }
    
   public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setStoredName(String storedName) {
	this.storedName = storedName;
    }

    public String getStoredName() {
	return storedName;
    }

    public void setStoredDescription(String storedDescription) {
	this.storedDescription = storedDescription;
    }

    public String getStoredDescription() {
	return storedDescription;
    }

    /*public void setCriters(ArrayList<String> criters) {
	this.criters = criters;
    }

    public ArrayList<String> getCriters() {
	return criters;
    }*/
      
}
