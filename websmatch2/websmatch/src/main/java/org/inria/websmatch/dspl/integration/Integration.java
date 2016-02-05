package org.inria.websmatch.dspl.integration;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xml.WSMatchXMLLoader;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Integration {

    private String sid1;
    private String sid2;
    private String dbName;

    public Integration(String sid1, String sid2, String dbName) {
	this.setSid1(sid1);
	this.setSid2(sid2);
	this.dbName = dbName;
    }

    @SuppressWarnings("unchecked")
    public String getIntegratedFile() {
	String res = new String();

	Document integratedDocument = new Document();

	// MySQLDBConnector conn = new MySQLDBConnector();

	// first we load the 2 files
	//String firstXML = conn.getXML(sid1);
	//String secondXML = conn.getXML(sid2);
	
	String firstXML = MongoDBConnector.getInstance().getEditedXML(sid1, dbName);
	String secondXML = MongoDBConnector.getInstance().getEditedXML(sid2, dbName);

	// now parse the XML files to make structures and integrate them
	WSMatchXMLLoader loader = new WSMatchXMLLoader(firstXML);
	Document firstDocument = loader.getDocument();
	loader = new WSMatchXMLLoader(secondXML);
	Document secondDocument = loader.getDocument();

	L.Debug(this.getClass().getSimpleName(), "First doc is " + firstDocument.toString(), true);
	L.Debug(this.getClass().getSimpleName(),"Second doc is "+secondDocument.toString(),true);

	// we have to JDOM documents, search for common attributes
	List<Element> attributesSchema1 = firstDocument.getRootElement().getChild("tables").getChild("table").getChild("attributes").getChildren("attribute");
	List<Element> attributesSchema2 = secondDocument.getRootElement().getChild("tables").getChild("table").getChild("attributes").getChildren("attribute");
	// search same dspltype
	for (Element elem1 : attributesSchema1) {
	    for (Element elem2 : attributesSchema2) {
		if (elem1.getChildText("dspltype").equals(elem2.getChildText("dspltype"))
			&& (!elem1.getChildText("dspltype").equals("undefined") || !elem2.getChildText("dspltype").equals("undefined"))) {

		    //System.out.println(elem1.getChildText("name") + " integration with " + elem2.getChildText("name"));

		    // we take the table and add the attributes of second
		    // document minus matching one
		    List<Element> elementsToAdd = new ArrayList<Element>();
		    
		    for (Element toAdd : attributesSchema2) {
			if (!toAdd.equals(elem2)) {
			    Element tmp = (Element) toAdd.clone();
			    tmp.getChild("datas").removeContent();		  
			    elementsToAdd.add(tmp);
			    // integratedDocument.getRootElement().getChild("tables").getChild("table").getChild("attributes").addContent(tmp);
			}
		    }

		    // now find the common values
		    List<Element> leftData = elem1.getChild("datas").getChildren("data");
		    List<Element> rightData = elem2.getChild("datas").getChildren("data");

		    // we order the list
		    for (Element tmpLeft : leftData) {
			for (Element tmpRight : rightData) {
			    if (tmpLeft.getText().equals(tmpRight.getText())) {
				System.out.println("Same year : " + tmpLeft.getText());
				try {
				    
				    int rsheet = tmpRight.getAttribute("sheet").getIntValue();
				    // int rx = tmpRight.getAttribute("x").getIntValue();
				    int ry = tmpRight.getAttribute("y").getIntValue();
				    
				    // System.out.println(rsheet + " "+rx);
				    
				    // now find the good values
				    for (Element element : attributesSchema2){
					// if not the current one
					if(!element.equals(elem2)){
					    // get the data and add to elements
					    List<Element> datasToAdd = element.getChild("datas").getChildren("data");
					    for(Element data : datasToAdd){
						int dsheet = data.getAttribute("sheet").getIntValue();
						// int dx = data.getAttribute("x").getIntValue();
						int dy = data.getAttribute("y").getIntValue();
	
						if(dsheet == rsheet && ry == dy){
						    for(Element tmpElement : elementsToAdd){
							if(tmpElement.getAttribute("x").getIntValue() == data.getAttribute("x").getIntValue() ){
							//System.out.println(data.getText());
							    tmpElement.getChild("datas").addContent((Element)data.clone());
							}
						    }
						}
					    }
					}
				    }				    				    
				} catch (DataConversionException e) {				   
				    L.Error(e.getMessage(),e);
				}				
			    }
			}
		    }

		    // join tables
		    integratedDocument = (Document) firstDocument.clone();
		    
		    for (Element toAdd : elementsToAdd) {
			if (!toAdd.equals(elem2)) {
			    Element tmp = (Element) toAdd.clone();
			    integratedDocument.getRootElement().getChild("tables").getChild("table").getChild("attributes").addContent(tmp);
			}
		    }	    
		}
	    }
	}

	//
	StringWriter swriter = new StringWriter();
	XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
	try {
	    out.output(integratedDocument, swriter);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
	//
	res = swriter.getBuffer().toString();

	L.Debug(this.getClass().getSimpleName(),"Result is "+res,true);

	return res;
    }

    public void setSid1(String sid1) {
	this.sid1 = sid1;
    }

    public String getSid1() {
	return sid1;
    }

    public void setSid2(String sid2) {
	this.sid2 = sid2;
    }

    public String getSid2() {
	return sid2;
    }

}
