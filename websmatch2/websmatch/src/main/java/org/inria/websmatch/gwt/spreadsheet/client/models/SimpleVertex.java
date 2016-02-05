package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;

public class SimpleVertex extends SimpleGraphComponent implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 273004319826407558L;
    
    private int id;
    private String name;
    private double x;
    private double y;
    
    // added to be used on click in canvas
    private String schemaId;
    
    // added to use on canvas
    private boolean rtl = false;
    private double canvasX = -1;
    private double canvasY = -1;
    
    public SimpleVertex(){
	
    }

    public void setId(int id) {
	this.id = id;
    }

    public int getId() {
	return id;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setX(double x) {
	this.x = x;
    }

    public double getX() {
	return x;
    }

    public void setY(double y) {
	this.y = y;
    }

    public double getY() {
	return y;
    }

    public void setSchemaId(String schemaId) {
	this.schemaId = schemaId;
    }

    public String getSchemaId() {
	return schemaId;
    }

    public void setRtl(boolean rtl) {
	this.rtl = rtl;
    }

    public boolean isRtl() {
	return rtl;
    }

    public void setCanvasX(double canvasX) {
	this.canvasX = canvasX;
    }

    public double getCanvasX() {
	return canvasX;
    }

    public void setCanvasY(double canvasY) {
	this.canvasY = canvasY;
    }

    public double getCanvasY() {
	return canvasY;
    }
    
    

}
