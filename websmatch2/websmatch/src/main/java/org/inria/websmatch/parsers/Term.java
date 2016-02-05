package org.inria.websmatch.parsers;

import java.util.ArrayList;
import java.util.List;

public class Term {
    
    String id;
    String name;
    String def;
    List<String> synonyms;
    
    public Term(){
	id = new String();
	name = new String();
	def = new String();
	synonyms = new ArrayList<String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def.trim();
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }
    
    @Override
    public String toString(){
	String res = "";
	
	res += "Id: "+id+"\nName: "+name+"\nDef: "+def+"\n";
	
	return res;
    }

}
