package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;

public class SchemaData implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -2718862608712256726L;
    
    private String source;
    private String name;
    private String author;
    private String description;
    private String id;
    
    public SchemaData(){
	super();
	source = new String();
	name = new String();
	id = new String("-1");
	setAuthor(new String());
	setDescription(new String());	
    }
    
    public SchemaData(String name, String source, String author, String description, String id){
	super();
	
	this.name = name;
	this.source = source;
	this.author = author;
	this.description = description;
	this.id = id;
	
    }
    
    public void setSource(String source) {
	this.source = source;
    }
    public String getSource() {
	return source;
    }
    public void setName(String name) {
	this.name = name;
    }
    public String getName() {
	return name;
    }
    public void setId(String id) {
	this.id = id;
    }
    public String getId() {
	return id;
    }

    public void setAuthor(String author) {
	this.author = author;
    }

    public String getAuthor() {
	return author;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getDescription() {
	return description;
    }
    
}
