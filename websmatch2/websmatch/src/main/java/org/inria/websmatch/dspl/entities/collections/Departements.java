package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dsplEngine.geo.datapublica.Departement;
import org.inria.websmatch.utils.StringUtils;

public class Departements {
    
    private HashMap<String,Entity> departementsByCode;
    private HashMap<String,Entity> departementsByName;

    public Departements() {
	setDepartementsByCode(new HashMap<String,Entity>());
	setDepartementsByName(new HashMap<String,Entity>());
	populate();
    }

    private void populate() {

    }
    
    public void addDepartement(Departement d){
	getDepartementsByCode().put(d.getDptCode(),d);
	getDepartementsByName().put(StringUtils.cleanString(d.getName()), d);
    }

    public void setDepartementsByCode(HashMap<String,Entity> departementsByCode) {
	this.departementsByCode = departementsByCode;
    }

    public HashMap<String,Entity> getDepartementsByCode() {
	return departementsByCode;
    }

    public void setDepartementsByName(HashMap<String,Entity> departementsByName) {
	this.departementsByName = departementsByName;
    }

    public HashMap<String,Entity> getDepartementsByName() {
	return departementsByName;
    }

}
