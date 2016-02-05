package org.mitre.schemastore.porters.schemaImporters;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.inria.websmatch.utils.L;
import org.inria.websmatch.xml.handlers.OsmozeHandler;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.URIType;
import org.xml.sax.SAXException;

public class OsmozeImporter extends SchemaImporter {
    
    private URI sourceURI;
    private ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
    private HashMap<String, Entity> entityList = new HashMap<String, Entity>();
    private HashMap<String, Domain> domainList = new HashMap<String, Domain>();
      
    /** Generate the schema elements */
    public ArrayList<SchemaElement> generateSchemaElements()
	    throws ImporterException {
	generate();
	return schemaElements;
    }
    
    protected void initialize() throws ImporterException {

	    schemaElements = new ArrayList<SchemaElement>();
	    entityList = new HashMap<String, Entity>();
	    domainList = new HashMap<String, Domain>();

	    loadDomains();
	    // Do nothing
	    if ((sourceURI != null) && sourceURI.equals(uri)) {
		return;
	    }    
    }
    
    /** Handles the loading of the specified domain */
    private void loadDomain(String name, String description) {
	Domain domain = new Domain(nextId(), name, description, 0);
	schemaElements.add(domain);
	domainList.put(name, domain);
    }

    /** Handles the loading of all default domains */
    private void loadDomains() {
	loadDomain(ANY.toLowerCase(), "The Any wildcard domain");
	loadDomain(INTEGER.toLowerCase(), "The Integer domain");
	loadDomain(STRING.toLowerCase(), "The String domain");
	loadDomain(BOOLEAN.toLowerCase(), "The Boolean domain");
	loadDomain(DATETIME.toLowerCase(), "The DateTime domain");
    }
    
    public void generate(){
	SAXParserFactory factory = SAXParserFactory.newInstance();
	    try {
		javax.xml.parsers.SAXParser saxParser = factory.newSAXParser();
		
		try {
		    saxParser.parse(new File(uri), new OsmozeHandler(this));
		} catch (IOException e) {	
		    L.Error(e.getMessage(),e);
		    return;
		}
		
		
	    } catch (ParserConfigurationException e) {
		L.Error(e.getMessage(),e);
		return;
	    } catch (SAXException e) {
		L.Error(e.getMessage(),e);
		return;
	    }
    }
    
    @Override
    public URIType getURIType() {
	return URIType.FILE;
    }
    
    /** Returns the importer URI file types */
    public ArrayList<String> getFileTypes() {
	ArrayList<String> fileTypes = new ArrayList<String>();
	fileTypes.add(".xml");
	return fileTypes;
    }

    @Override
    public String getDescription() {
	return "This importer can be used to import schemas from an Osmoze XML format";
    }

    @Override
    public String getName() {
	return "Osmoze Importer";
    }

    public void addEntity(String osmozeId, String value, String type) {
	
	/**
	 * @todo Correct nextId shitty thing
	 * Next id seems buggy... as it works only for local things... great... +1 make the deal... strange not?
	 */
	
	if(!entityList.containsKey(osmozeId)){
	    Entity tblEntity = new Entity(nextId(), value, type, 0);
	    entityList.put(osmozeId, tblEntity);
	    schemaElements.add(tblEntity);
	}
	
    }

    public void addSubRelation(String parentId, String childId) {
	// relation?
	/*Relationship rel = new Relationship(nextId()+1,lastSource + " - " + value,relType,entities.get(lastSource).getId(),1,1,entities.get(value).getId(),1,1,entities.get(lastSource).getBase());
	
	System.out.println("Rel Id : "+rel.getId()+"\t"+"LeftId : "+entities.get(lastSource).getId()+"\t"+"RightId : "+entities.get(value).getId());
	
	relations.add(rel);*/
	
	// subtype?
	Subtype subtype = new Subtype(nextId(), entityList.get(parentId).getId(), entityList.get(childId).getId(), entityList.get(parentId).getBase());
	schemaElements.add(subtype);
    }
    
    public void setTitle(String nodeId, String titleNodeId){
	
	 entityList.get(nodeId).setName(entityList.get(nodeId).getName() + " : "+entityList.get(titleNodeId).getName());
	 schemaElements.remove(entityList.remove(titleNodeId));
	
    }

}
