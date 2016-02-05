package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;

public class ConnexComposant implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4621325978674242126L;

    private int startX = -1;
    private int startY = -1;
    private int endX = -1;
    private int endY = -1;
    private String criteria;
    private double fmeas;
    private double recall;
    private double precision;
    
    private boolean attrInLines = false;
    private boolean biDimensionnalArray = false;
    // if bidim, name to export
    private String valueName = "";
    
    // for use on diff
    private int sheet = -1;

    public ConnexComposant() {

	setStartX(-1);
	setStartY(-1);
	setEndX(-1);
	setEndY(-1);

    }

    public void add(int x, int y) {
	if (startX == -1) {
	    startX = x;
	    startY = y;
	    endX = x;
	    endY = y;
	} else {
	    if (x < startX)
		startX = x;
	    if (y < startY)
		startY = y;
	    if (x > endX)
		endX = x;
	    if (y > endY)
		endY = y;
	}
    }

    @Override
    public String toString() {
	return "StartX : " + this.startX + "\tStartY : " + this.startY + "\tEndX : " + this.endX + "\tEndY : " + this.endY;
    }

    public void setStartX(int startX) {
	this.startX = startX;
    }

    public int getStartX() {
	return startX;
    }

    public void setStartY(int startY) {
	this.startY = startY;
    }

    public int getStartY() {
	return startY;
    }

    public void setEndX(int endX) {
	this.endX = endX;
    }

    public int getEndX() {
	return endX;
    }

    public void setEndY(int endY) {
	this.endY = endY;
    }

    public int getEndY() {
	return endY;
    }

    public boolean contains(ConnexComposant cc) {

	if (cc.equals(this))
	    return false;

	if (cc.getStartX() >= this.getStartX() && cc.getEndX() <= this.getEndX() && cc.getStartY() >= this.getStartY() && cc.getEndY() <= this.getEndY())
	    return true;
	else
	    return false;

    }

    public boolean intersect(ConnexComposant cc) {

	return !(cc.getStartX() > this.getEndX() || cc.getEndX() < this.getStartX() || cc.getStartY() > this.getEndY() || cc.getEndY() < this.getStartY());

    }

    public boolean isAttrInLines() {
        return attrInLines;
    }

    public void setAttrInLines(boolean attrInLines) {
        this.attrInLines = attrInLines;
    }
    
    public boolean containsPoint(int x, int y){
	if(this.getStartX() <= x && this.getEndX() >= x && this.getStartY() <= y && this.getEndY() >= y) return true;
	else return false;
    }

    public void setBiDimensionnalArray(boolean biDimensionnalArray) {
	this.biDimensionnalArray = biDimensionnalArray;
    }

    public boolean isBiDimensionnalArray() {
	return biDimensionnalArray;
    }

    public String getCriteria() {
        return criteria;
    }

    public double getFmeas() {
        return fmeas;
    }

    public double getRecall() {
        return recall;
    }

    public double getPrecision() {
        return precision;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public void setFmeas(double fmeas) {
        this.fmeas = fmeas;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public void setSheet(int sheet) {
	this.sheet = sheet;
    }

    public int getSheet() {
	return sheet;
    }

    public void setValueName(String valueName) {
	this.valueName = valueName;
    }

    public String getValueName() {
	return valueName;
    }

}
