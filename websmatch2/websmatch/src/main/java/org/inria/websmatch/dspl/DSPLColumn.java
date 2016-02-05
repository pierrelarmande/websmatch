package org.inria.websmatch.dspl;

import java.util.HashMap;

public class DSPLColumn {
    
    private String id;
    private String type;
    // needed for time:time_point
    private String format = new String();
    //
    private String description;
    private int line;
    private int col;
    
    // add the editedScript info
    private String engineScript = new String();
    
    // add the editedScript for specific values (conditional filter)
    private HashMap<int[],String> specScripts = new HashMap<int[],String>();
    
    public DSPLColumn(){
	id = new String();
	type = new String();
	description = new String();
	line = -1;
	setCol(-1);
    }
    
    public DSPLColumn(String id, String type, String description, int line, int col){
	this.id = id;
	this.type = type;
	this.description = description;
	this.line = line;
	this.setCol(col);
    }

    public String getId() {
	if(id != null && id.equals("")) return "value";
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getDescription() {
	return description;
    }

    public void setLine(int line) {
	this.line = line;
    }

    public int getLine() {
	return line;
    }

    public void setCol(int col) {
	this.col = col;
    }

    public int getCol() {
	return col;
    }

    public void setFormat(String format) {
	this.format = format;
    }

    public String getFormat() {
	return format;
    }

    public void setEngineScript(String engineScript) {
	this.engineScript = engineScript;
    }

    public String getEngineScript() {
	return engineScript;
    }

    public void setSpecScripts(HashMap<int[],String> specScripts) {
	this.specScripts = specScripts;
    }

    public HashMap<int[],String> getSpecScripts() {
	return specScripts;
    }

}
