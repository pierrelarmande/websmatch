package org.inria.websmatch.gwt.spreadsheet.client.models.generic;

import java.io.Serializable;

public class SimpleSchemaElement implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5254648295868814265L;
    
    private Integer id;
    private String name;
    
    public SimpleSchemaElement(){
	
    }
    
    public SimpleSchemaElement(Integer id, String name){
	this.id = id;
	this.name = name==null ? "" : name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
