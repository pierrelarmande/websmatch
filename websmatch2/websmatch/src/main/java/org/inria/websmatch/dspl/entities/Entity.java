package org.inria.websmatch.dspl.entities;

public class Entity {
    
    private String name;
    private String description;
    
    public Entity(){
	setName(new String());
	setDescription(new String());
    }
    
    public Entity(String name, String descString){
	this();
	this.name = name;
	this.description = descString;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getDescription() {
	return description;
    }

}
