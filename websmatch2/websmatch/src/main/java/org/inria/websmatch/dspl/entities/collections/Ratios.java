package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dspl.entities.quantity.Ratio;

public class Ratios {
    
    private HashMap<String,Entity> ratios;
    
    public Ratios(){
	
	ratios = new HashMap<String,Entity>();
	populate();
	
    }

    public void setRatios(HashMap<String,Entity> ratios) {
	this.ratios = ratios;
    }

    public HashMap<String,Entity> getRatios() {
	return ratios;
    }
    
    private void populate(){
	ratios.put(new Ratio().getName(),new Ratio());
    }

}
