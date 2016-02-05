package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;

public class SimpleSchema implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = -2610135209742208158L;
    private String id;
    private String name;
    
    public SimpleSchema(){
	
    }
    
    public SimpleSchema(String id, String name){
	this.setId(id);
	this.setName(name);
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

}
