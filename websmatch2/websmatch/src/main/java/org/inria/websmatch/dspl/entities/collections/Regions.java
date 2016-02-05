package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dsplEngine.geo.datapublica.Region;
import org.inria.websmatch.utils.StringUtils;

public class Regions {
    
    private HashMap<String,Entity> regionsByCode;
    private HashMap<String,Entity> regionsByName;

    public Regions() {
	setRegionsByCode(new HashMap<String,Entity>());
	setRegionsByName(new HashMap<String,Entity>());
	populate();
    }

    public void setRegionsByCode(HashMap<String,Entity> regions) {
	this.regionsByCode = regions;
    }

    public HashMap<String,Entity> getRegionsByCode() {
	return regionsByCode;
    }

    private void populate() {

    }
    
    public void addRegion(Region r){
    	getRegionsByCode().put(r.getRegionCode(),r);
    	getRegionsByName().put(StringUtils.cleanString(r.getName()), r);
    }

    public void setRegionsByName(HashMap<String,Entity> regionsByName) {
	this.regionsByName = regionsByName;
    }

    public HashMap<String,Entity> getRegionsByName() {
	return regionsByName;
    }

}
