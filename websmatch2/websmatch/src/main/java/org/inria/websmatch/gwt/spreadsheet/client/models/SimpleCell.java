package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.io.Serializable;
import java.util.ArrayList;

public class SimpleCell implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5296408896637405114L;
    private String content;
    private boolean isAttribute;

    // to map easily with the JXL cells
    private int jxlRow = -1;
    private int jxlCol = -1;

    // we also need sheet index
    private int sheet = -1;
    private String sheetName = new String();

    // we need to know the ML values if exists
    private double first_cc_col = -1;
    private double first_cc_row = -1;
    private double type = -1;
    private double behind_cell = -1;
    private double right_cell = -1;
    private double is_attributeML = -1;

    // new attr to affine
    private double above_cell = -1;
    private double left_cell = -1;

    // for serices
    private String filename;
    private String username;

    // for tree representation
    private int entityId = -1;
    private String entityName = new String();
    private int ccStartX = -1;
    private int ccStartY = -1;

    private String[] metaInfos = { "title", "comment" };
    private String currentMeta = "";

    // new content for extended edition
    private String editedContent;
    private String editedDescription;

    // for dspl export
    // private String[] dsplMetas = { "undefined", "geo", "time"};
    // private String[][] dsplSubMetas =
    // {{"location","country"},{"year","quarter","month","week","day"}};
    private String currentDsplMeta = "undefined";
    // private String currentDsplSubMeta = "undefined";
    private String[] dsplMetas = { "undefined", "dp:pays", "dp:region", "dp:departement", "dp:commune", "geo:location",
    /* "geo:country", */"time:year", "time:time_point",/*
						        * "time:quarter",
						        * "time:month"
						        * ,"time:week"
						        * ,"time:day",
						        */"quantity:ratio", "entity:entity" };

    private boolean isMerged = false;

    // if attribute : string, numeric
    private String contentType;

    // added for matching on entities
    // if an error in the line set to true and add the position and good value :
    // int[] size 2, x, y
    private ArrayList<int[]> errorList = new ArrayList<int[]>();
    private boolean dsplMapped = false;
    // added to store the edited script for DSPLEngine
    private String engineScript = new String();
    // added to store time:time_point format
    private String format = new String();

    public SimpleCell() {
	super();
	content = new String();
	isAttribute = false;
	editedContent = new String();
	editedDescription = new String();
	contentType = new String();
    }

    public SimpleCell(String content, boolean isAttribute, int r, int c, int sheet) {
	super();
	this.setContent(content);
	this.setAttribute(isAttribute);
	this.setJxlRow(r);
	this.setJxlCol(c);
	this.setSheet(sheet);
	editedContent = new String();
	editedDescription = new String();
	contentType = new String();
    }

    public SimpleCell(String content, boolean isAttribute, int r, int c, int sheet, double fccc, double fccr, double type, double bc, double rc, double ac,
	    double lc, double ia) {
	this(content, isAttribute, r, c, sheet);

	// set the ML values
	this.setFirst_cc_col(fccc);
	this.setFirst_cc_row(fccr);
	this.setType(type);
	this.setBehind_cell(bc);
	this.setRight_cell(rc);
	this.setAbove_cell(ac);
	this.setLeft_cell(lc);
	this.setIs_attributeML(ia);
	contentType = new String();
    }

    public SimpleCell(String content, boolean isAttribute, int r, int c, int sheet, double fccc, double fccr, double type, double bc, double rc, double ac,
	    double lc, double ia, int eid, String ename) {
	this(content, isAttribute, r, c, sheet, fccc, fccr, type, bc, rc, ac, lc, ia);

	this.setEntityId(eid);
	this.setEntityName(ename);
	contentType = new String();
    }

    public void setContent(String content) {
	this.content = content;
    }

    public String getContent() {
	return content;
    }

    public void setAttribute(boolean isAttribute) {
	this.isAttribute = isAttribute;
    }

    public boolean isAttribute() {
	return isAttribute;
    }

    public void setJxlRow(int row) {
	this.jxlRow = row;
    }

    public int getJxlRow() {
	return jxlRow;
    }

    public void setJxlCol(int col) {
	this.jxlCol = col;
    }

    public int getJxlCol() {
	return jxlCol;
    }

    @Override
    public String toString() {

	return ("Col : " + this.getJxlCol() + " Row : " + this.jxlRow + " Sheet : " + this.sheet + " Content : " + this.getContent());

    }

    @Override
    public boolean equals(Object o) {

	if (this == o) {
	    return true;
	}
	if (o == null || getClass() != o.getClass()) {
	    return false;
	}

	if (this.getJxlCol() == ((SimpleCell) o).getJxlCol() && this.getJxlRow() == ((SimpleCell) o).getJxlRow()
		&& this.getContent().equals(((SimpleCell) o).getContent()) && this.sheet == ((SimpleCell) o).sheet)
	    return true;
	else
	    return false;

    }

    public void setSheet(int sheet) {
	this.sheet = sheet;
    }

    public int getSheet() {
	return sheet;
    }

    public double getFirst_cc_col() {
	return first_cc_col;
    }

    public void setFirst_cc_col(double first_cc_col) {
	this.first_cc_col = first_cc_col;
    }

    public double getFirst_cc_row() {
	return first_cc_row;
    }

    public void setFirst_cc_row(double first_cc_row) {
	this.first_cc_row = first_cc_row;
    }

    public double getType() {
	return type;
    }

    public void setType(double type) {
	this.type = type;
    }

    public double getBehind_cell() {
	return behind_cell;
    }

    public void setBehind_cell(double behind_cell) {
	this.behind_cell = behind_cell;
    }

    public double getRight_cell() {
	return right_cell;
    }

    public void setRight_cell(double right_cell) {
	this.right_cell = right_cell;
    }

    public double getIs_attributeML() {
	return is_attributeML;
    }

    public void setIs_attributeML(double is_attribute) {
	this.is_attributeML = is_attribute;
    }

    public String getFilename() {
	return filename;
    }

    public void setFilename(String filename) {
	this.filename = filename;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public void setEntityId(int entityId) {
	this.entityId = entityId;
    }

    public int getEntityId() {
	return entityId;
    }

    public void setEntityName(String entityName) {
	this.entityName = entityName;
    }

    public String getEntityName() {
	return entityName;
    }

    public double getAbove_cell() {
	return above_cell;
    }

    public void setAbove_cell(double above_cell) {
	this.above_cell = above_cell;
    }

    public double getLeft_cell() {
	return left_cell;
    }

    public void setLeft_cell(double left_cell) {
	this.left_cell = left_cell;
    }

    public String getCurrentMeta() {
	return currentMeta;
    }

    public void setCurrentMeta(String currentMeta) {
	this.currentMeta = currentMeta;
    }

    public String[] getMetaInfos() {
	return metaInfos;
    }

    public void setEditedContent(String editedContent) {
	this.editedContent = editedContent;
    }

    public String getEditedContent() {
	return editedContent;
    }

    public void setEditedDescription(String editedDescription) {
	this.editedDescription = editedDescription;
    }

    public String getEditedDescription() {
	return editedDescription;
    }

    public void setDsplMetas(String[] dsplMetas) {
	this.dsplMetas = dsplMetas;
    }

    public String[] getDsplMetas() {
	return dsplMetas;
    }

    public void setCurrentDsplMeta(String currentDsplMeta) {
	this.currentDsplMeta = currentDsplMeta;
    }

    public String getCurrentDsplMeta() {
	return currentDsplMeta;
    }

    public String getSheetName() {
	return sheetName;
    }

    public void setSheetName(String sheetName) {
	this.sheetName = sheetName;
    }

    public void setCcStartX(int ccStartX) {
	this.ccStartX = ccStartX;
    }

    public int getCcStartX() {
	return ccStartX;
    }

    public void setCcStartY(int ccStartY) {
	this.ccStartY = ccStartY;
    }

    public int getCcStartY() {
	return ccStartY;
    }

    public boolean isMerged() {
	return isMerged;
    }

    public void setMerged(boolean isMerged) {
	this.isMerged = isMerged;
    }

    public String getContentType() {
	return contentType;
    }

    public void setContentType(String contentType) {
	this.contentType = contentType;
    }

    public void setErrorList(ArrayList<int[]> errorList) {
	this.errorList = errorList;
    }

    public ArrayList<int[]> getErrorList() {
	return errorList;
    }

    public void setDsplMapped(boolean dsplMapped) {
	this.dsplMapped = dsplMapped;
    }

    public boolean isDsplMapped() {
	return dsplMapped;
    }

    public void setEngineScript(String engineScript) {
	this.engineScript = engineScript;
    }

    public String getEngineScript() {
	return engineScript;
    }

    public void setFormat(String format) {
	this.format = format;
    }

    public String getFormat() {
	return format;
    }

    // for DSPLengine quarters
    public void generateDateFormatScript(String format) {	
	if (format.equals("MQyy") && this.getContent().trim().length() == 4 && this.getContent().indexOf('Q') == 1) {

	    String[] date = this.getContent().split("Q");

	    try {
		new Integer(date[0]);
		new Integer(date[1]);

		String month = new String();
		String year = new String();
		if (date[0].equals("1"))
		    month = "01";
		if (date[0].equals("2"))
		    month = "04";
		if (date[0].equals("3"))
		    month = "07";
		if (date[0].equals("4"))
		    month = "10";

		// now for the year
		if (new Integer(date[1]) <= 50)
		    year = "20" + date[1];
		else
		    year = "19" + date[1];

		//
		this.setEngineScript("formatter.str().replace('" + this.getContent() + "','" + month + "/" + year + "')");
	    } catch (NumberFormatException nfe) {
		//
	    }
	}
    }
    //

    /*
     * public void setDsplSubMetas(String[][] dsplSubMetas) { this.dsplSubMetas
     * = dsplSubMetas; }
     * 
     * public String[][] getDsplSubMetas() { return dsplSubMetas; }
     * 
     * public void setCurrentDsplSubMeta(String currentDsplSubMeta) {
     * this.currentDsplSubMeta = currentDsplSubMeta; }
     * 
     * public String getCurrentDsplSubMeta() { return currentDsplSubMeta; }
     */
}
