package org.inria.websmatch.dsplEngine.geo.datapublica;

import org.inria.websmatch.dspl.entities.geo.Location;

public class Commune extends Location{
    
    private String communeCode;
    private String dptCode;
    
    public Commune(String name, String desc, float lat, float lon, String communeCode, String dptCode){
	super(name,desc,lat,lon);
	this.setDptCode(dptCode);
	this.setCommuneCode(communeCode);
    }

    public void setCommuneCode(String communeCode) {
	this.communeCode = communeCode;
    }

    public String getCommuneCode() {
	return communeCode;
    }

    public void setDptCode(String dptCode) {
	this.dptCode = dptCode;
    }

    public String getDptCode() {
	return dptCode;
    }


}
