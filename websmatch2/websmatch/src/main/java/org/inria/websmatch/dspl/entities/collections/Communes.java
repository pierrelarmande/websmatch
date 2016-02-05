package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dsplEngine.geo.datapublica.Commune;
import org.inria.websmatch.utils.StringUtils;

public class Communes {

    private HashMap<String, Entity> communesByCode;
    private HashMap<String, Entity> communesByName;//HashMap<String, Entity>> communesByName;

    public Communes() {
        setCommunesByCode(new HashMap<String, Entity>());
        setCommunesByName(new HashMap<String, Entity>());//HashMap<String, Entity>>());
        populate();
    }

    public void setCommunesByCode(HashMap<String, Entity> communes) {
        this.communesByCode = communes;
    }

    public HashMap<String, Entity> getCommunesByCode() {
        return communesByCode;
    }

    private void populate() {

    }

    public void addCommune(Commune c) {
        getCommunesByCode().put(c.getCommuneCode(), c);
        getCommunesByName().put(StringUtils.cleanString(c.getName()), c);
	/*if (getCommunesByName().get(c.getName()) != null)
	    getCommunesByName().get(c.getName()).put(c.getCommuneCode(), c);
	else {
	    HashMap<String, Entity> tmpMap = new HashMap<String, Entity>();
	    tmpMap.put(c.getCommuneCode(), c);
	    getCommunesByName().put(c.getName(), tmpMap);
	}*/
    }

    public void setCommunesByName(HashMap<String, Entity> hashMap) {
        this.communesByName = hashMap;
    }

    public HashMap<String, Entity> getCommunesByName() {
        return communesByName;
    }

}
