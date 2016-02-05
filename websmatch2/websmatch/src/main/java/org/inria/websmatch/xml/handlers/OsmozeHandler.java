package org.inria.websmatch.xml.handlers;

import org.mitre.schemastore.porters.schemaImporters.OsmozeImporter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OsmozeHandler extends DefaultHandler {
    
    OsmozeImporter importer;
      
    public OsmozeHandler(OsmozeImporter oi){
	super();
	this.importer = oi;
    }

    public void startElement(String uri, String localName, String qName,
	    Attributes attributes) throws SAXException {

	// we search for the entity, we iterates in the XML attributes
	if(attributes.getLength() > 0 && attributes.getValue("name").equals("node")){
	    importer.addEntity(attributes.getValue("id"), attributes.getValue("value"), attributes.getValue("level"));
	}
	
	// if it's a relation
	if(attributes.getLength() > 0 && attributes.getValue("name").equals("relation")){
	    if(attributes.getValue("value").equals("title")){
		importer.setTitle(attributes.getValue("source"), attributes.getValue("target"));
	    }
	    else{
		importer.addSubRelation(attributes.getValue("target"), attributes.getValue("source"));
	    }
	}
	
	
	/*for(int i = 0; i < attributes.getLength(); i++){
	    
	    if(attributes.getValue(i).equals("node")){
		lastRelType = attributes.getValue(i+1);
		i = i+1;
	    }
	    	    
	    // if we have a source or a target, that's an entity
	    else if(attributes.getValue(i).equals("source")){
		
		// get the supertype
		lastSourceSuperType = attributes.getValue(i+1);
		
		// entity	
		lastSource = attributes.getValue(i+2);
		importer.addEntity(attributes.getValue(i+2),lastSourceSuperType);
		i = i+2;		
	    }
	    
	    else if(attributes.getValue(i).equals("target")){
		
		if(lastRelType.equals("title")){
		    importer.setTitle(lastSource,attributes.getValue(i+2));
		}
		
		else{
		// entity		
		importer.addEntity(attributes.getValue(i+2),attributes.getValue(i+1));
		
		// we had the rel
		importer.addSubRelation(attributes.getValue(i+2),lastSource);
		}

		i = i+2;		
	    }
	}*/
		
    }

    public void endElement(String uri, String localName, String qName)
	    throws SAXException {

    }

}
