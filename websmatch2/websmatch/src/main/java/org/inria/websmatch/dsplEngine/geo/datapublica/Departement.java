package org.inria.websmatch.dsplEngine.geo.datapublica;

import org.inria.websmatch.dspl.entities.geo.Location;

public class Departement extends Location{
    
    private String dptCode;
    private String regionCode;
    
    public Departement(String name, String desc, float lat, float lon, String dptCode, String regionCode){
	super(name,desc,lat,lon);
	this.setDptCode(dptCode);
	this.setRegionCode(regionCode);
    }

    public void setDptCode(String dptCode) {
	this.dptCode = dptCode;
    }

    public String getDptCode() {
	return dptCode;
    }

    public void setRegionCode(String regionCode) {
	this.regionCode = regionCode;
    }

    public String getRegionCode() {
	return regionCode;
    }

}
