package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;

@Deprecated
public class MatchingResult implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -1430402579471997701L;
    private String leftElementName;
    private String rightElementName;
    private double score;
    private boolean expert;
    
    // id needed to update
    private int id_element1;
    private int id_schema1;
    private int id_element2;
    private int id_schema2;
    
    public MatchingResult(){
	
    }
    
    public MatchingResult(String left, String right, double score, boolean exp, int id1, int ids1, int id2, int ids2){
	
	this.leftElementName = left;
	this.rightElementName = right;
	this.score = score;
	this.setExpert(exp);
	setId_element1(id1);
	setId_element2(id2);
	setId_schema1(ids1);
	setId_schema2(ids2);
	
    }

    public String getLeftElementName() {
        return leftElementName;
    }

    public void setLeftElementName(String leftElementName) {
        this.leftElementName = leftElementName;
    }

    public String getRightElementName() {
        return rightElementName;
    }

    public void setRightElementName(String rightElementName) {
        this.rightElementName = rightElementName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setExpert(boolean expert) {
	this.expert = expert;
    }

    public boolean isExpert() {
	return expert;
    }

    public void setId_element1(int id_element1) {
	this.id_element1 = id_element1;
    }

    public int getId_element1() {
	return id_element1;
    }

    public void setId_element2(int id_element2) {
	this.id_element2 = id_element2;
    }

    public int getId_element2() {
	return id_element2;
    }

    public void setId_schema1(int id_schema1) {
	this.id_schema1 = id_schema1;
    }

    public int getId_schema1() {
	return id_schema1;
    }

    public void setId_schema2(int id_schema2) {
	this.id_schema2 = id_schema2;
    }

    public int getId_schema2() {
	return id_schema2;
    }

}
