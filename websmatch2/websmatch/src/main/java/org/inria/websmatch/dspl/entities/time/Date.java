package org.inria.websmatch.dspl.entities.time;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dspl.entities.Entity;

public class Date extends Entity {
    
    public Date(String name, String description){
	super(name,description);
    }
    
    public boolean equals(Object o){
	if(o instanceof Date){
	    if(this.getName().equals(((Date) o).getName())) return true;
	    else return false;
	}else return false;
    }

}
