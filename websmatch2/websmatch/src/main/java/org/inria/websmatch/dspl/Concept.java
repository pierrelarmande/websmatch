package org.inria.websmatch.dspl;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class Concept {
    
    private String id;
    private String name;
    private ArrayList<String> alternativeNames;
    // uri is : idDoc/numsheet/x/y
    private String uri;
    private ArrayList<String> alternativeUris;
    // private HashMap<String,String> docIdsAndPath;
    private int nbOccurences;
    private int nbTestedMatch;
    private String type;
    private Set<String> instances;
    
    public Concept(String id, String name, ArrayList<String> alternateNames, String uri, ArrayList<String> alternativeUris, int nbOcc, int nbTested, String type, Set<String> instances){
	this.setId(id);
	this.setAlternativeNames(alternateNames);
	this.setName(name);
	this.setUri(uri);
	this.setAlternativeUris(alternativeUris);
	this.nbOccurences = nbOcc;
	this.nbTestedMatch = nbTested;
	this.type = type;
	this.setInstances(instances);
	//this.setDocIdsAndPath(docIdsAndPath);
	// nbOccurences = docIdsAndPath.size();
    }
    
    public Concept(){
	id = new String();
	name = new String();
	alternativeNames = new ArrayList<String>();
	setUri(new String());
	setAlternativeUris(new ArrayList<String>());
	nbOccurences = 0;
	nbTestedMatch = 0;
	instances = new TreeSet<String>();
	type = new String();
	//docIdsAndPath = new HashMap<String,String>();
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

    public void setAlternativeNames(ArrayList<String> alternateNames) {
	this.alternativeNames = alternateNames;
    }

    public ArrayList<String> getAlternativeNames() {
	return alternativeNames;
    }

    /*public void setDocIdsAndPath(HashMap<String,String> docIdsAndPath) {
	this.docIdsAndPath = docIdsAndPath;
	this.occurence = docIdsAndPath.size();
    }

    public HashMap<String,String> getDocIdsAndPath() {
	return docIdsAndPath;
    }*/

    public int getNbOccurences() {
	return nbOccurences;
    }
    
    public void setNbOccurences(int i){
	this.nbOccurences = i;
    }

    public int getNbTestedMatch() {
        return nbTestedMatch;
    }

    public void setNbTestedMatch(int nbTestedMatch) {
        this.nbTestedMatch = nbTestedMatch;
    }

    public void setUri(String uri) {
	this.uri = uri;
    }

    public String getUri() {
	return uri;
    }

    public void setAlternativeUris(ArrayList<String> alternativeUris) {
	this.alternativeUris = alternativeUris;
    }

    public ArrayList<String> getAlternativeUris() {
	return alternativeUris;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getInstances() {
        return instances;
    }

    public void setInstances(Set<String> instances) {
        this.instances = instances;
    }
}
