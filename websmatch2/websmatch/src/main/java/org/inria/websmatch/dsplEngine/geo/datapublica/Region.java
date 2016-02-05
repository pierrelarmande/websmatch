package org.inria.websmatch.dsplEngine.geo.datapublica;

import org.inria.websmatch.dspl.entities.geo.Location;
import org.inria.websmatch.dspl.entities.geo.Location;

public class Region extends Location {
    
    private String regionCode;
    private String paysCode;
    
    public Region(String name, String desc, float lat, float lon, String regionCode, String paysCode){
	super(name,desc,lat,lon);
	this.setRegionCode(regionCode);
	this.setPaysCode(paysCode);
    }

    public void setRegionCode(String regionCode) {
	this.regionCode = regionCode;
    }

    public String getRegionCode() {
	return regionCode;
    }

    public void setPaysCode(String paysCode) {
	this.paysCode = paysCode;
    }

    public String getPaysCode() {
	return paysCode;
    }
    
}
