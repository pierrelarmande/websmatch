package org.inria.websmatch.xml.handlers;

import java.util.HashMap;
import java.util.LinkedList;

import org.inria.websmatch.dspl.DSPLColumn;
import org.inria.websmatch.dspl.DSPLSlice;
import org.inria.websmatch.dspl.DSPLSlice;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WebSMatchFileToDSPLHandler extends DefaultHandler {

    private LinkedList<DSPLSlice> slices;
    private DSPLSlice currentSlice;

    // current sheet needed
    private int currentSheet;
    private int tablePerSheetCount;

    // where we are
    boolean inTable = false;
    boolean inAttribute = false;
    boolean inDsplType = false;
    boolean inName = false;
    boolean inData = false;

    boolean inFormat = false;
    boolean inEngineScript = false;
    
    // base fileName (without any .*)
    private String fileNameWithoutSuffix;

    // value buffer
    private StringBuffer buffer;

    // current attribute
    private DSPLColumn column;

    // current data line
    private int dataLine;
    
    // current local data script
    private String script = new String();
    
    // for bidim
    private int origX = -1;
    private int origY = -1;

    public WebSMatchFileToDSPLHandler(String fileNameWithoutSuffix) {
	super();
	slices = new LinkedList<DSPLSlice>();
	currentSheet = -1;
	tablePerSheetCount = 0;
	this.fileNameWithoutSuffix = fileNameWithoutSuffix;
	dataLine = -1;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

	if (qName.equals("table")) {

	    // we are in table
	    inTable = true;

	    currentSlice = new DSPLSlice();

	    // set sheet name
	    currentSlice.setSheetName(attributes.getValue("sheetName"));
	    
	    // get sheet number
	    int tableSheet = new Integer(attributes.getValue("sheet")).intValue();
	    
	    // set attr in line and bi dim
	    currentSlice.setInLineAttr(new Boolean(attributes.getValue("attrInLine")));
	    currentSlice.setBiDim(new Boolean(attributes.getValue("biDim")));
	    // set the end of the table
	    currentSlice.setCcEndX(new Integer(attributes.getValue("endX")));
	    currentSlice.setCcEndY(new Integer(attributes.getValue("endY")));

	    // first table found
	    if (currentSheet == -1) {
		currentSheet = tableSheet;
		tablePerSheetCount = 1;
	    }

	    //
	    else {
		if (currentSheet == tableSheet)
		    tablePerSheetCount++;
		else {
		    currentSheet = tableSheet;
		    tablePerSheetCount = 1;
		}
	    }

	    // set filename in Slice
	    currentSlice.setFileName(fileNameWithoutSuffix + "_s" + currentSheet + "_t" + tablePerSheetCount + ".csv");
	    // set the sheet number
	    currentSlice.setSheetNumber(currentSheet);
	    // set table number in sheet
	    currentSlice.setTableSheetNumber(tablePerSheetCount);

	} else if (qName.equals("attribute")) {
	    inAttribute = true;
	    column = new DSPLColumn();

	    // set the line
	    column.setLine(new Integer(attributes.getValue("y")).intValue());
	    column.setCol(new Integer(attributes.getValue("x")).intValue());

	} else if (qName.equals("dspltype")) {
	    buffer = new StringBuffer();
	    inDsplType = true;
	} else if (qName.equals("name") && inAttribute == true) {
	    buffer = new StringBuffer();
	    inName = true;
	} else if (qName.equals("data") && inAttribute == true) {
	    buffer = new StringBuffer();
	    inData = true;
	    if(currentSlice.isInLineAttr()) dataLine = new Integer(attributes.getValue("x")).intValue();  
	    else dataLine = new Integer(attributes.getValue("y")).intValue();
	    if(currentSlice.isBiDim()){
		origX = new Integer(attributes.getValue("origX")).intValue();
		origY = new Integer(attributes.getValue("origY")).intValue();
	    }
	    script = attributes.getValue("script");
	} else if (qName.equals("format")) {
	    buffer = new StringBuffer();
	    inFormat = true;
	} else if (qName.equals("enginescript")) {
	    buffer = new StringBuffer();
	    inEngineScript = true;
	}

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

	if (qName.equals("table")) {
	    slices.add(currentSlice);
	    inTable = false;
	} else if (qName.equals("dspltype")) {
	    inDsplType = false;
	    if (buffer.toString().equals("undefined"))
		column.setType("string");
	    else {
		column.setType(buffer.toString());
	    }
	} else if (qName.equals("format")) {
	    inFormat = false;
	    column.setFormat(buffer.toString());
	} else if (qName.equals("enginescript")) {
	    inEngineScript = false;
	    column.setEngineScript(buffer.toString());
	} else if (qName.equals("name") && inAttribute == true) {
	    inName = false;
	    if(buffer.toString().endsWith(" (d")) column.setId(buffer.toString().substring(0, buffer.toString().lastIndexOf(" (d")));
	    else column.setId(buffer.toString());
	} else if (qName.equals("attribute")) {
	    inAttribute = false;
	    currentSlice.addColumn(column);
	} else if (qName.equals("data")) {
	    inData = false;
	    // add local scripts
	    if(currentSlice.isBiDim()){
		HashMap<int[],String> localScripts = column.getSpecScripts();
		localScripts.put(new int[]{origX,origY}, script);
		script = new String();
	    }else{
		HashMap<int[],String> localScripts = column.getSpecScripts();
		localScripts.put(new int[]{column.getCol(),dataLine}, script);
		script = new String();
	    }
	    // check if we have a type for datas
	    if (!column.getType().contains(":") && !column.getType().equals("float")) {
		if (dataLine > column.getLine() && !buffer.toString().equals("")) {
		    // is integer?
		    try {
			new Integer(buffer.toString().replaceAll("\\xA0", ""));
			column.setType("integer");
		    } catch (NumberFormatException nfebis) {
			// is float?
			try {
			    /*
			     * String s = buffer.toString();
			     * System.out.println(s); if(s.indexOf("-") == 0) s
			     * = s.substring(1);
			     */
			    new Float(buffer.toString().replaceAll("\\xA0", "").replaceAll(",", "."));
			    column.setType("float");
			} catch (NumberFormatException nfe) {
			    // so... string
			    column.setType("string");
			}
		    }
		}
	    }
	}

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
	String read = new String(ch, start, length);
	if (buffer != null)
	    buffer.append(read);
    }

    public LinkedList<DSPLSlice> getSlices() {
	return slices;
    }

}
