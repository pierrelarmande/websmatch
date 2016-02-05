package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;

public class SimpleEdge extends SimpleGraphComponent implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -7671452512386951339L;
    
    private String comment;
    private int parentId;
    private int childId;
    private double parentX;
    private double parentY;
    private double childX;
    private double childY;
    
    public SimpleEdge(){
	
    }

    public void setComment(String comment) {
	this.comment = comment;
    }

    public String getComment() {
	return comment;
    }

    public void setParentId(int parentId) {
	this.parentId = parentId;
    }

    public int getParentId() {
	return parentId;
    }

    public void setChildId(int childId) {
	this.childId = childId;
    }

    public int getChildId() {
	return childId;
    }

    public void setParentX(double parentX) {
	this.parentX = parentX;
    }

    public double getParentX() {
	return parentX;
    }

    public void setParentY(double parentY) {
	this.parentY = parentY;
    }

    public double getParentY() {
	return parentY;
    }

    public void setChildX(double childX) {
	this.childX = childX;
    }

    public double getChildX() {
	return childX;
    }

    public void setChildY(double childY) {
	this.childY = childY;
    }

    public double getChildY() {
	return childY;
    }
    
    

}
