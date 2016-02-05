package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dsplEngine.geo.datapublica.Pays;
import org.inria.websmatch.utils.StringUtils;

public class Payss {
    
    private HashMap<String,Entity> payssByCode;
    private HashMap<String,Entity> payssByName;
    private HashMap<String,Entity> payssByUKName;

    public Payss() {
	setPayssByCode(new HashMap<String,Entity>());
	setPayssByName(new HashMap<String,Entity>());
	setPayssByUKName(new HashMap<String,Entity>());
	populate();
    }

    public void setPayssByCode(HashMap<String,Entity> payss) {
	this.payssByCode = payss;
    }

    public HashMap<String,Entity> getPayssByCode() {
	return payssByCode;
    }

    private void populate() {

    }
    
    public void addPays(Pays p){
    	getPayssByCode().put(p.getCode(),p);
    	getPayssByName().put(StringUtils.cleanString(p.getName()),p);
    	getPayssByUKName().put(StringUtils.cleanString(p.getUk_name()),p);
    }

    public void setPayssByName(HashMap<String,Entity> payssByName) {
	this.payssByName = payssByName;
    }

    public HashMap<String,Entity> getPayssByName() {
	return payssByName;
    }

    public void setPayssByUKName(HashMap<String,Entity> payssByUKName) {
	this.payssByUKName = payssByUKName;
    }

    public HashMap<String,Entity> getPayssByUKName() {
	return payssByUKName;
    }

}
