package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;
import java.util.TreeMap;

@Deprecated
public class MatchingScores implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 586235365500656497L;
    private int id_schema1;
    private int id_element1;
    private int id_schema2;
    private int id_element2;
    private boolean isExpert;
    
    private TreeMap<String, Double> scores;
    
    public MatchingScores(){
	scores = new TreeMap<String, Double>();
    }
    
    public MatchingScores(int eid1, int sid1, int eid2, int sid2){
	this();
	id_element1 = eid1;
	id_schema1 = sid1;
	id_element2 = eid2;
	id_schema2 = sid2;
    }

    public int getId_schema1() {
        return id_schema1;
    }

    public void setId_schema1(int id_schema1) {
        this.id_schema1 = id_schema1;
    }

    public int getId_element1() {
        return id_element1;
    }

    public void setId_element1(int id_element1) {
        this.id_element1 = id_element1;
    }

    public int getId_schema2() {
        return id_schema2;
    }

    public void setId_schema2(int id_schema2) {
        this.id_schema2 = id_schema2;
    }

    public int getId_element2() {
        return id_element2;
    }

    public void setId_element2(int id_element2) {
        this.id_element2 = id_element2;
    }

    public TreeMap<String, Double> getScores() {
        return scores;
    }

    public void setScores(TreeMap<String, Double> scores) {
        this.scores = scores;
    }

    public void addScore(String tech, double value){
	this.scores.put(tech, value);
    }

    public void setExpert(boolean isExpert) {
	this.isExpert = isExpert;
    }

    public boolean isExpert() {
	return isExpert;
    }
    
}
