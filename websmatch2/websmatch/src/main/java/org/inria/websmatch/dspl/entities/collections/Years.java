package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dspl.entities.time.Year;

public class Years {
    
    private HashMap<String,Entity> years;
    
    public Years(){
	setYears(new HashMap<String,Entity>());
	populate();
    }

    public void setYears(HashMap<String,Entity> years) {
	this.years = years;
    }

    public HashMap<String,Entity> getYears() {
	return years;
    }
    
    private void populate(){
	for(int i = 1700; i < 2100; i++){
	    getYears().put(new Year(new Integer(i).toString(),new Integer(i).toString()).getName(),new Year(new Integer(i).toString(),new Integer(i).toString()));
	}
    }

}
