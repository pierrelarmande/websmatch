package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DetectionQualityData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5711814392533762658L;

    // needed for reload and report pop
    private String name;
    private String description;
    //
    
    private String source;
    private double precision;
    private double recall;
    private double fmeasure;

    private String objectId;
    private boolean neverEdited;
    private List<ConnexComposant> connexComposants;
    
    // for detect pb
    private boolean ccDetectPb = false;
    private boolean attrDetectPb = false;
    private String detecPb = "";
    
    // trashed doc
    private boolean trashed = false;
    
    // pub id
    private String publication_id = "";
    
    public List<ConnexComposant> getConnexComposants() {
        return connexComposants;
    }

    public void setConnexComposants(List<ConnexComposant> connexComposants) {
        this.connexComposants = connexComposants;
    }

    public DetectionQualityData() {
	super();

	setSource(new String());
	setPrecision(-1);
	setRecall(-1);
	setFmeasure(-1);
	setObjectId(new String());
	setNeverEdited(false);
	
	setName(new String());
	setDescription(new String());
	
	connexComposants = new ArrayList<ConnexComposant>();
    }

    public DetectionQualityData(String name, String description, String source, double precision, double recall, double fmeasure, String objectId, boolean neverEdited, boolean ccDetectPb, boolean attrDetectPb, String detectPb, boolean trashed, String publication_id) {
	super();
	
	this.setName(name);
	this.setDescription(description);
	
	setSource(source);
	setPrecision(precision);
	setRecall(recall);
	setFmeasure(fmeasure);
	setObjectId(objectId);
	setNeverEdited(neverEdited);
	
	//
	setCcDetectPb(ccDetectPb);
	setAttrDetectPb(attrDetectPb);
	setDetecPb(detectPb);
	//
	
	setTrashed(trashed);
	
	//
	setPublication_id(publication_id);
	
	connexComposants = new ArrayList<ConnexComposant>();
    }

    public void setSource(String source) {
	this.source = source;
    }

    public String getSource() {
	return source;
    }

    public void setPrecision(double precision) {
	if(Double.isNaN(precision)) this.precision = 0.0;
	this.precision = precision;
    }

    public double getPrecision() {
	return precision;
    }

    public void setRecall(double recall) {
	if(Double.isNaN(recall)) this.recall = 0.0;
	this.recall = recall;
    }

    public double getRecall() {
	return recall;
    }

    public void setFmeasure(double fmeasure) {
	if(Double.isNaN(fmeasure)) this.fmeasure = 0.0;
	else this.fmeasure = fmeasure;
    }

    public double getFmeasure() {
	return fmeasure;
    }

    public void setObjectId(String objectId) {
	this.objectId = objectId;
    }

    public String getObjectId() {
	return objectId;
    }

    public void setNeverEdited(boolean neverEdited) {
	this.neverEdited = neverEdited;
    }

    public boolean isNeverEdited() {
	return neverEdited;
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

    public void setCcDetectPb(boolean ccDetectPb) {
	this.ccDetectPb = ccDetectPb;
    }

    public boolean isCcDetectPb() {
	return ccDetectPb;
    }

    public void setAttrDetectPb(boolean attrDetectPb) {
	this.attrDetectPb = attrDetectPb;
    }

    public boolean isAttrDetectPb() {
	return attrDetectPb;
    }

    public void setDetecPb(String detecPb) {
	this.detecPb = detecPb;
    }

    public String getDetecPb() {
	return detecPb;
    }

    public void setTrashed(boolean trashed) {
	this.trashed = trashed;
    }

    public boolean isTrashed() {
	return trashed;
    }

    public void setPublication_id(String publication_id) {
	this.publication_id = publication_id;
    }

    public String getPublication_id() {
	return publication_id;
    }

}
