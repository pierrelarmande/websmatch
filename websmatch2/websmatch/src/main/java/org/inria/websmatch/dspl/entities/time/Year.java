package org.inria.websmatch.dspl.entities.time;

import org.inria.websmatch.dspl.entities.Entity;

public class Year extends Entity {
    
    public Year(String name, String description){
	super(name,description);
    }
    
    public boolean equals(Object o){
	if(o instanceof Year){
	    if(this.getName().equals(((Year) o).getName())) return true;
	    else return false;
	}else return false;
    }

}
