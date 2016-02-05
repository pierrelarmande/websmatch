package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;

public class DistanceData implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = -2860624338340730686L;
    private String id;
    private String name;
    private double dist;
    
    public DistanceData(String id, String name, double dist){
	this.setId(id);
	this.setName(name);
	this.setDist(dist);
    }
    
    public DistanceData(){
	id = "";
	name = "";
	dist = 0;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getId() {
	return id;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setDist(double dist) {
	this.dist = dist;
    }

    public double getDist() {
	return dist;
    }
}
