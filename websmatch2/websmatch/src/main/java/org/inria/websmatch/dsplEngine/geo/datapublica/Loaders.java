package org.inria.websmatch.dsplEngine.geo.datapublica;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.inria.websmatch.utils.L;

public class Loaders {
    
    private static Loaders loaders = null;
    
    private TreeMap<String,List<String[]>> typeVal = new TreeMap<String,List<String[]>>();
    
    private List<String[]> pays;
    private List<String[]> regions;
    private List<String[]> departements;
    private List<String[]> communes;
    
    // pays, regions, departements, communes
    private Loaders(){
	Loader loader = new Loader("/org/inria/websmatch/dsplEngine/geo/datapublica/pays.csv");
	pays = new ArrayList<String[]>();
	try {
	    pays = loader.load();
	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(),e);
	}
	loader = new Loader("/org/inria/websmatch/dsplEngine/geo/datapublica/regions.csv");
	try {
	    setRegions(loader.load());
	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(),e);
	}
	loader = new Loader("/org/inria/websmatch/dsplEngine/geo/datapublica/departements.csv");
	try {
	    departements = loader.load();
	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(),e);
	}
	loader = new Loader("/org/inria/websmatch/dsplEngine/geo/datapublica/communes.csv");
	try {
	    setCommunes(loader.load());
	} catch (FileNotFoundException e) {
	    L.Error(e.getMessage(),e);
	}
	
	typeVal.put("dp:pays", pays);
	typeVal.put("dp:region", regions);
	typeVal.put("dp:departement", departements);
	typeVal.put("dp:commune", communes);
    }
    
    public static Loaders getInstance(){
	if(loaders == null){
	    loaders = new Loaders();
	}
	return loaders;
    }

    public void setPays(List<String[]> pays) {
	this.pays = pays;
    }

    public List<String[]> getPays() {
	return pays;
    }

    public void setRegions(List<String[]> regions) {
	this.regions = regions;
    }

    public List<String[]> getRegions() {
	return regions;
    }

    public void setDepartements(List<String[]> departements) {
	this.departements = departements;
    }

    public List<String[]> getDepartements() {
	return departements;
    }

    public void setCommunes(List<String[]> communes) {
	this.communes = communes;
    }

    public List<String[]> getCommunes() {
	return communes;
    }

    public TreeMap<String, List<String[]>> getTypeVal() {
        return typeVal;
    }

    public void setTypeVal(TreeMap<String, List<String[]>> typeVal) {
        this.typeVal = typeVal;
    }

}
