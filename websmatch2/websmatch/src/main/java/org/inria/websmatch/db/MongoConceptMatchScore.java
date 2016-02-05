package org.inria.websmatch.db;

import java.util.ArrayList;
import java.util.List;

public class MongoConceptMatchScore {

    private String c1Name;
    private List<String> c1AlternativeNames;
    private String c1Uri;
    private List<String> c1AlternativeUris;
    private int c1NbOccurences;
    private int c1NbTestedMatch;
    private String c1Type;
    private String c1Instances;

    private String c2Name;
    private List<String> c2AlternativeNames;
    private String c2Uri;
    private List<String> c2AlternativeUris;
    private int c2NbOccurences;
    private int c2NbTestedMatch;
    private String c2Type;
    private String c2Instances;

    private float stringWordnetScore;
    private float typeScore;
    private float ibScore;
    private float stringScore;
    private float wordnetScore;
    private float probaScore;

    private int expert;
    
    // if needed
    private String c1ObjectId;
    private String c2ObjectId;
    
    public MongoConceptMatchScore(){
	c1Name = new String();
	c1AlternativeNames = new ArrayList<String>();
	c1Uri = new String();
	c1AlternativeUris = new ArrayList<String>();
	c1NbOccurences = 0;
	c1NbTestedMatch = 0;
	c1Type = new String();
	c1Instances = new String();
	
	c2Name = new String();
	c2AlternativeNames = new ArrayList<String>();
	c2Uri = new String();
	c2AlternativeUris = new ArrayList<String>();
	c2NbOccurences = 0;
	c2NbTestedMatch = 0;
	c2Type = new String();
	c2Instances = new String();
	
	stringWordnetScore = 0;
	typeScore = 0;
	ibScore = 0;
	setStringScore(0);
	setWordnetScore(0);
	setProbaScore(0);
	
	expert = 0;
	
	c1ObjectId = new String();
	c2ObjectId = new String();
    }

    public MongoConceptMatchScore(String c1Name, List<String> c1AlternativeNames, String c1Uri, List<String> c1AlternativeUris, int c1NbOccurences,
	    int c1NbTestedMatch, String c1Type, String c1Instances, String c2Name, List<String> c2AlternativeNames, String c2Uri,
	    List<String> c2AlternativeUris, int c2NbOccurences, int c2NbTestedMatch, String c2Type, String c2Instances, float stringWordnetScore,
	    float typeScore, float ibScore, float stringScore, float wordnetScore, float probaScore, int expert, String c1ObjectId, String c2ObjectId) {

	this.c1Name = c1Name;
	this.c1AlternativeNames = c1AlternativeNames;
	this.c1Uri = c1Uri;
	this.c1AlternativeUris = c1AlternativeUris;
	this.c1NbOccurences = c1NbOccurences;
	this.c1NbTestedMatch = c1NbTestedMatch;
	this.c1Type = c1Type;
	this.c1Instances = c1Instances;

	this.c2Name = c2Name;
	this.c2AlternativeNames = c2AlternativeNames;
	this.c2Uri = c2Uri;
	this.c2AlternativeUris = c2AlternativeUris;
	this.c2NbOccurences = c2NbOccurences;
	this.c2NbTestedMatch = c2NbTestedMatch;
	this.c2Type = c2Type;
	this.c2Instances = c2Instances;

	this.stringWordnetScore = stringWordnetScore;
	this.typeScore = typeScore;
	this.ibScore = ibScore;
	this.setStringScore(stringScore);
	this.setWordnetScore(wordnetScore);
	this.setProbaScore(probaScore);

	this.expert = expert;
    }

    public String getC1Name() {
	return c1Name;
    }

    public void setC1Name(String c1Name) {
	this.c1Name = c1Name;
    }

    public List<String> getC1AlternativeNames() {
	return c1AlternativeNames;
    }

    public void setC1AlternativeNames(List<String> c1AlternativeNames) {
	this.c1AlternativeNames = c1AlternativeNames;
    }

    public String getC1Uri() {
	return c1Uri;
    }

    public void setC1Uri(String c1Uri) {
	this.c1Uri = c1Uri;
    }

    public List<String> getC1AlternativeUris() {
	return c1AlternativeUris;
    }

    public void setC1AlternativeUris(List<String> c1AlternativeUris) {
	this.c1AlternativeUris = c1AlternativeUris;
    }

    public int getC1NbOccurences() {
	return c1NbOccurences;
    }

    public void setC1NbOccurences(int c1NbOccurences) {
	this.c1NbOccurences = c1NbOccurences;
    }

    public int getC1NbTestedMatch() {
	return c1NbTestedMatch;
    }

    public void setC1NbTestedMatch(int c1NbTestedMatch) {
	this.c1NbTestedMatch = c1NbTestedMatch;
    }

    public String getC1Type() {
	return c1Type;
    }

    public void setC1Type(String c1Type) {
	this.c1Type = c1Type;
    }

    public String getC1Instances() {
	return c1Instances;
    }

    public void setC1Instances(String c1Instances) {
	this.c1Instances = c1Instances;
    }

    public String getC2Name() {
	return c2Name;
    }

    public void setC2Name(String c2Name) {
	this.c2Name = c2Name;
    }

    public List<String> getC2AlternativeNames() {
	return c2AlternativeNames;
    }

    public void setC2AlternativeNames(List<String> c2AlternativeNames) {
	this.c2AlternativeNames = c2AlternativeNames;
    }

    public String getC2Uri() {
	return c2Uri;
    }

    public void setC2Uri(String c2Uri) {
	this.c2Uri = c2Uri;
    }

    public List<String> getC2AlternativeUris() {
	return c2AlternativeUris;
    }

    public void setC2AlternativeUris(List<String> c2AlternativeUris) {
	this.c2AlternativeUris = c2AlternativeUris;
    }

    public int getC2NbOccurences() {
	return c2NbOccurences;
    }

    public void setC2NbOccurences(int c2NbOccurences) {
	this.c2NbOccurences = c2NbOccurences;
    }

    public int getC2NbTestedMatch() {
	return c2NbTestedMatch;
    }

    public void setC2NbTestedMatch(int c2NbTestedMatch) {
	this.c2NbTestedMatch = c2NbTestedMatch;
    }

    public String getC2Type() {
	return c2Type;
    }

    public void setC2Type(String c2Type) {
	this.c2Type = c2Type;
    }

    public String getC2Instances() {
	return c2Instances;
    }

    public void setC2Instances(String c2Instances) {
	this.c2Instances = c2Instances;
    }

    public float getStringWordnetScore() {
	return stringWordnetScore;
    }

    public void setStringWordnetScore(float stringWordnetScore) {
	this.stringWordnetScore = stringWordnetScore;
    }

    public float getTypeScore() {
	return typeScore;
    }

    public void setTypeScore(float typeScore) {
	this.typeScore = typeScore;
    }

    public float getIbScore() {
	return ibScore;
    }

    public void setIbScore(float ibScore) {
	this.ibScore = ibScore;
    }

    public int getExpert() {
	return expert;
    }

    public void setExpert(int expert) {
	this.expert = expert;
    }

    public void setC1ObjectId(String c1ObjectId) {
	this.c1ObjectId = c1ObjectId;
    }

    public String getC1ObjectId() {
	return c1ObjectId;
    }

    public void setC2ObjectId(String c2ObjectId) {
	this.c2ObjectId = c2ObjectId;
    }

    public String getC2ObjectId() {
	return c2ObjectId;
    }

    public void setStringScore(float stringScore) {
	this.stringScore = stringScore;
    }

    public float getStringScore() {
	return stringScore;
    }

    public void setWordnetScore(float wordnetScore) {
	this.wordnetScore = wordnetScore;
    }

    public float getWordnetScore() {
	return wordnetScore;
    }

    public void setProbaScore(float probaScore) {
	this.probaScore = probaScore;
    }

    public float getProbaScore() {
	return probaScore;
    }

}
