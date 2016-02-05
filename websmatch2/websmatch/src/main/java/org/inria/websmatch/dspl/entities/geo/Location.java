package org.inria.websmatch.dspl.entities.geo;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dspl.entities.Entity;

public class Location extends Entity {

    private float latitude;
    private float longitude;

    public Location() {
	super();
	setLatitude(0);
	setLongitude(0);
    }

    public Location(float lat, float lon) {
	this();
	setLatitude(lat);
	setLongitude(lon);
    }

    public Location(String name, String desc, float lat, float lon) {
	super(name, desc);
	setLatitude(lat);
	setLongitude(lon);
    }

    public void setLatitude(float latitude) {
	this.latitude = latitude;
    }

    public float getLatitude() {
	return latitude;
    }

    public void setLongitude(float longitude) {
	this.longitude = longitude;
    }

    public float getLongitude() {
	return longitude;
    }

    public boolean equals(Object o) {
	if (o instanceof Location) {
	    if (this.getName().toUpperCase().contains((((Location) o).getName().toUpperCase()))
		    || (((Location) o).getName().toUpperCase().contains(this.getName().toUpperCase())))
		return true;
	    else
		return false;
	} else
	    return false;
    }

}
