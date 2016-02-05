package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dspl.entities.time.Date;

public class Dates {
    
private HashMap<String,Entity> dates;
    
    public Dates(){
	setDates(new HashMap<String,Entity>());
	populate();
    }

    public void setDates(HashMap<String,Entity> dates) {
	this.dates = dates;
    }

    public HashMap<String,Entity> getDates() {
	return dates;
    }
    
    private void populate(){
	// date formats accepted : YYYY-MM, MM-YYYY, YYYY/MM, MM/YYYY, YYYY-M, YYYY/M, M-YYYY, M/YYYY
	// generate them
	for(int i = 1700; i < 2100; i++)
	{
	    for(int m = 1; m < 13; m++)
	    {	
		String[] tabDates;
		//
		if(m < 10){
		    tabDates = new String[8];
		    tabDates[0] = i+"-0"+m;
		    tabDates[1] = "0"+m+"-"+i;
		    tabDates[2] = i+"/0"+m;
		    tabDates[3] = "0"+m+"/"+i;
		    tabDates[4] = i+"-"+m;
		    tabDates[5] = m+"-"+i;
		    tabDates[6] = i+"/"+m;
		    tabDates[7] = m+"/"+i;
		    
		}else{
		    tabDates = new String[4];
		    tabDates[0] = i+"-"+m;
		    tabDates[1] = m+"-"+i;
		    tabDates[2] = i+"/"+m;
		    tabDates[3] = m+"/"+i;
		}
		//
		for(String d : tabDates) getDates().put(new Date(d,d).getName(),new Date(d,d));
	    }
	}
    }

}
