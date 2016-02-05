package org.inria.websmatch.dsplEngine.geo.datapublica;

import org.inria.websmatch.dspl.entities.geo.Location;

public class Pays extends Location{
    
    private String code;
    private String uk_name;
    
    public Pays(String name, String desc, float lat, float lon, String code, String uk_name){
	super(name,desc,lat,lon);
	this.setCode(code);
	this.setUk_name(uk_name);
    }

    public void setCode(String code) {
	this.code = code;
    }

    public String getCode() {
	return code;
    }

    public void setUk_name(String uk_name) {
	this.uk_name = uk_name;
    }

    public String getUk_name() {
	return uk_name;
    }

}
