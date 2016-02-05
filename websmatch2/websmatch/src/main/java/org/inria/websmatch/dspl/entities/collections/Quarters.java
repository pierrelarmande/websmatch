package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dspl.entities.time.Quarter;

public class Quarters {

    private HashMap<String, Entity> quarters;

    public Quarters() {
	setQuarters(new HashMap<String, Entity>());
	populate();
    }

    public void setQuarters(HashMap<String, Entity> quarters) {
	this.quarters = quarters;
    }

    public HashMap<String, Entity> getQuarters() {
	return quarters;
    }

    private void populate() {
	for (int i = 1; i <= 4; i++) {
	    for (int y = 0; y <= 99; y++) {
		String q = new String();
		if (y >= 10)
		    q = i + "Q" + y;
		else
		    q = i + "Q0" + y;
		getQuarters().put(q, new Quarter(q, q));
	    }
	}
    }
}
