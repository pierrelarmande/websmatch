package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;

public class SimpleMatchTech implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8822839255213415872L;
    private int id;
    private String name;
    
    public SimpleMatchTech(){
	
    }
    
    public SimpleMatchTech(int id, String name){
	
	this.id = id;
	this.name = name;
	
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
