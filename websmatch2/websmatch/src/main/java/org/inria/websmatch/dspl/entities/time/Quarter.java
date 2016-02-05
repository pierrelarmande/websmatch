package org.inria.websmatch.dspl.entities.time;

public class Quarter extends Date{
    
    public Quarter(String name, String description){
	super(name,description);
    }
    
    public boolean equals(Object o){
	if(o instanceof Quarter){
	    if(this.getName().equals(((Quarter) o).getName())) return true;
	    else return false;
	}else return false;
    }

}
