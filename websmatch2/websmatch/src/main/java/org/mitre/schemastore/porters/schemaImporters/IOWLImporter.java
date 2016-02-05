package org.mitre.schemastore.porters.schemaImporters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import loader.ontology.OntoBuffer;

import org.inria.websmatch.utils.L;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import tools.Supports;

public class IOWLImporter extends SchemaImporter {
    // Defines the additional owl domain types
    public static final String FLOAT = "Float";
    public static final String DATE = "Date";
    public static final String TIME = "Time";

    private ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
    private HashMap<String, Entity> entityList = new HashMap<String, Entity>();
    private HashMap<String, Attribute> attributeList = new HashMap<String, Attribute>();
    private HashMap<String, Domain> domainList = new HashMap<String, Domain>();
    private OntoBuffer parser;
    private String language;

    @Override
    public URIType getURIType() {
	return URIType.FILE;
    }

    @Override
    public String getName() {
	return "Improved OWL Importer";
    }

    @Override
    public String getDescription() {
	return "This importer can be used to import schemas from an owl format";
    }

    /** Returns the importer URI file types */
    public ArrayList<String> getFileTypes() {
	ArrayList<String> fileTypes = new ArrayList<String>();
	fileTypes.add(".owl");
	fileTypes.add(".rdf");
	fileTypes.add(".rdfs");
	return fileTypes;
    }

    /** Initializes the importer for the specified URI */
    protected void initialize() throws ImporterException {
	schemaElements = new ArrayList<SchemaElement>();
	entityList = new HashMap<String, Entity>();
	domainList = new HashMap<String, Domain>();

	try {
	    loadDomains();
	    initializeOntModel(uri);

	} catch (Exception e) {
	    throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
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
	loadDomain(FLOAT.toLowerCase(), "The Float domain");
	loadDomain(STRING.toLowerCase(), "The String domain");
	loadDomain(BOOLEAN.toLowerCase(), "The Boolean domain");
	loadDomain(DATETIME.toLowerCase(), "The DateTime domain");
	loadDomain(DATE.toLowerCase(), "The Date domain");
	loadDomain(TIME.toLowerCase(), "The Time domain");
    }

    /** Initializes the ontology model */
    private void initializeOntModel(URI uri) throws MalformedURLException, IOException {
	// Determine what version of owl to use
	setLanguage("");
	if (uri.toString().endsWith(".owl"))
	    setLanguage("http://www.owl-ontologies.com/generations.owl#Daughter");
	else if (uri.toString().endsWith(".rdf"))
	    setLanguage("http://oaei.ontologymatching.org/2009/benchmarks/204/onto.rdf#Technical_report");
	else if (uri.toString().endsWith(".rdfs"))
	    setLanguage("http://oaei.ontologymatching.org/2009/benchmarks/204/onto.rdf#Book");

	// read and parse ontology file
	parser = new OntoBuffer(uri.getPath().toString());

    }

    private void linearGen() {
	// Entities from OWL classes
	Set<OWLClass> classes = parser.getNamedClasses();
	for (OWLClass cls : classes) {
	    // if (!Supports.getLocalName(cls.toStringID()).equals("Thing")) {
	    Entity entity = null;
	    // TODO remove the Cirad hack
	    // cirad hack
	    // entity = new Entity(nextId(), Supports.getLocalName(cls.toStringID()), parser.getEntityComment(cls), 0);
	    if(parser.getEntityLabel(cls) == null || parser.getEntityLabel(cls).trim().equals("")) entity = new Entity(nextId(), Supports.getLocalName(cls.toStringID()), parser.getEntityComment(cls), 0);
	    else entity = new Entity(nextId(), parser.getEntityLabel(cls), parser.getEntityComment(cls), 0);
	    
	    // entity.setLabels(parser.getEntityLabel(cls));
	    entityList.put(cls.toStringID(), entity);
	    schemaElements.add(entity);
	    System.out.println("Class " + cls.toStringID() + " became an entity.");
	    // }
	}
	//

	// Then, data properties (convert to attribute with domain)
	Set<OWLDataProperty> datas = parser.getNamedDataProperties();
	for (OWLDataProperty data : datas) {
	    Domain domain = this.convertRangeToDomain(data);
	    // find the parent entities
	    Set<OWLClass> propClasses = parser.getClsWithDPropertyRestricted(data);
	    for (OWLClass cls : propClasses) {
		if (entityList.get(cls.toStringID()) != null) {
		    Entity entity = entityList.get(cls.toStringID());
		    
		    Attribute attribute = null;
		    if(parser.getEntityLabel(data) == null || parser.getEntityLabel(data).trim().equals("")) attribute = new Attribute(nextId(), Supports.getLocalName(data.toStringID()), parser.getEntityComment(data), entity.getId(),
			    domain.getId(), null, null, false, 0);
		    else attribute = new Attribute(nextId(), parser.getEntityLabel(data), parser.getEntityComment(data), entity.getId(),
			    domain.getId(), null, null, false, 0);
		    
		    
		    attributeList.put(data.toStringID(), attribute);
		    schemaElements.add(attribute);
		    System.out.println("Data property " + data.toStringID() + " became an attribute of class " + cls.toStringID() + " with domain "
			    + domain.getName() + " .");
		}
	    }
	    // find entities using domain of the property
	    try {
		Set<OWLClass> domainClasses = parser.getDPropertyDomains(data, true);
		for (OWLClass cls : domainClasses) {
		    if (entityList.get(cls.toStringID()) != null) {
			Entity entity = entityList.get(cls.toStringID());
			
			Attribute attribute = null;
			
			 if(parser.getEntityLabel(data) == null || parser.getEntityLabel(data).trim().equals("")) attribute = new Attribute(nextId(), Supports.getLocalName(data.toStringID()), parser.getEntityComment(data), entity.getId(),
				domain.getId(), null, null, false, 0);
			 else attribute = new Attribute(nextId(), parser.getEntityLabel(data), parser.getEntityComment(data), entity.getId(),
					domain.getId(), null, null, false, 0);
			
			attributeList.put(data.toStringID(), attribute);
			schemaElements.add(attribute);
			System.out.println("Data property " + data.toStringID() + " became an attribute of class " + cls.toStringID() + " with domain "
				+ domain.getName() + " .");
		    }
		}
	    } catch (NullPointerException e) {
		L.Error(e.getMessage(),e);
	    }
	}
	//

	// Then, object properties (specific relationships), the property become
	// an Entity with 3 relation ships
	Set<OWLObjectProperty> objs = parser.getNamedObjProperties();
	for (OWLObjectProperty obj : objs) {
	    // property
	    Entity entity = new Entity(nextId(), Supports.getLocalName(obj.toStringID()), "Object property : " + parser.getEntityComment(obj), 0);
	    // entity.setLabels(parser.getEntityLabel(obj));
	    entityList.put(obj.toStringID(), entity);
	    schemaElements.add(entity);
	    System.out.println("Object property " + obj.toStringID() + " became an entity.");
	    // find all classes in relation with this prop
	    Set<OWLClass> propClasses = parser.getClsWithOPropertyRestricted(obj);
	    for (OWLClass cls : propClasses) {
		System.out.println("In relation with " + cls.toStringID());
		Relationship rel = new Relationship(nextId(), Supports.getLocalName(obj.toStringID()) + " onproperty "
			+ Supports.getLocalName(cls.toStringID()), "onproperty", entity.getId(), 1, 1, entityList.get(cls.toStringID()).getId(), 1, 1, 0);
		schemaElements.add(rel);
	    }
	    // then add domain and range relations
	    Set<OWLClass> domainClasses = parser.getOPropertyDomains(obj, true);
	    for (OWLClass cls : domainClasses) {
		System.out.println("In domain relation with " + cls.toStringID());
		// create if not exist
		if (entityList.get(cls.toStringID()) == null) {
		    Entity tmpEntity = new Entity(nextId(), Supports.getLocalName(cls.toStringID()), parser.getEntityComment(cls), 0);
		    entityList.put(cls.toStringID(), tmpEntity);
		    schemaElements.add(tmpEntity);
		}
		Relationship rel = new Relationship(nextId(), Supports.getLocalName(obj.toStringID()) + " domain " + Supports.getLocalName(cls.toStringID()),
			"domain", entity.getId(), 1, 1, entityList.get(cls.toStringID()).getId(), 1, 1, 0);
		schemaElements.add(rel);
	    }
	    // range
	    Set<OWLClass> rangeClasses = parser.getOPropertyRanges(obj, true);
	    for (OWLClass cls : rangeClasses) {
		System.out.println("In range relation with " + cls.toStringID());
		// create if not exist
		if (entityList.get(cls.toStringID()) == null) {
		    Entity tmpEntity = new Entity(nextId(), Supports.getLocalName(cls.toStringID()), parser.getEntityComment(cls), 0);
		    entityList.put(cls.toStringID(), tmpEntity);
		    schemaElements.add(tmpEntity);
		}
		Relationship rel = new Relationship(nextId(), Supports.getLocalName(obj.toStringID()) + " range " + Supports.getLocalName(cls.toStringID()),
			"range", entity.getId(), 1, 1, entityList.get(cls.toStringID()).getId(), 1, 1, 0);
		schemaElements.add(rel);
	    }
	}
	//

	// Subclasses
	for (OWLClass cls : classes) {
	    Set<OWLClass> subs = parser.getSubClasses(cls, true);
	    for (OWLClass sub : subs) {
		if (entityList.get(cls.toStringID()) != null && entityList.get(sub.toStringID()) != null) {
		    Subtype subtype = new Subtype(nextId(), entityList.get(cls.toStringID()).getId(), entityList.get(sub.toStringID()).getId(), 0);
		    schemaElements.add(subtype);
		}
	    }
	}
	//

	// Sub obj property
	for (OWLObjectProperty obj : objs) {
	    Set<OWLObjectProperty> subobjs = parser.getSubOProperties(obj, true);
	    for (OWLObjectProperty prop : subobjs) {
		if (entityList.get(obj.toStringID()) != null && entityList.get(prop.toStringID()) != null) {
		    Subtype subtype = new Subtype(nextId(), entityList.get(obj.toStringID()).getId(), entityList.get(prop.toStringID()).getId(), 0);
		    schemaElements.add(subtype);
		}
	    }
	}

	// Sub data property
	for (OWLDataProperty data : datas) {
	    Set<OWLDataProperty> subobjs = parser.getSubDProperties(data, true);
	    for (OWLDataProperty prop : subobjs) {
		Domain domain = this.convertRangeToDomain(prop);
		// find the parent entities
		Set<OWLClass> propClasses = parser.getClsWithDPropertyRestricted(prop);
		for (OWLClass cls : propClasses) {
		    if (entityList.get(cls.toStringID()) != null) {
			Entity entity = entityList.get(cls.toStringID());
			Attribute attribute = new Attribute(nextId(), Supports.getLocalName(prop.toStringID()), parser.getEntityComment(prop), entity.getId(),
				domain.getId(), null, null, false, 0);
			schemaElements.add(attribute);
			attributeList.put(data.toStringID(), attribute);
			System.out.println("Data property " + prop.toStringID() + " became an attribute of class " + cls.toStringID() + " with domain "
				+ domain.getName() + " .");
			// Subtype
			Subtype subtype = new Subtype(nextId(), attributeList.get(data.toStringID()).getId(), attributeList.get(prop.toStringID()).getId(), 0);
			schemaElements.add(subtype);
		    }
		}
	    }
	}

	/*
	 * for (OWLDataProperty obj : datas) { Set<OWLDataProperty> subobjs =
	 * parser.getSubDProperties(obj, true); for (OWLDataProperty prop :
	 * subobjs) { if (entityList.get(obj.toStringID()) != null &&
	 * entityList.get(prop.toStringID()) != null) { Subtype subtype = new
	 * Subtype(nextId(), entityList.get(obj.toStringID()).getId(),
	 * entityList.get(prop.toStringID()).getId(), 0);
	 * schemaElements.add(subtype); } } }
	 */

    }

    // converts a data property range to M3 domain.
    private Domain convertRangeToDomain(OWLDataProperty dataProp) {
	Set<String> propType = parser.getDPropertyRanges(dataProp);
	for (String strdom : propType) {
	    Domain domain = domainList.get(strdom.toLowerCase());
	    if (domain == null)
		domain = domainList.get(ANY.toLowerCase());
	    return domain;
	}
	return domainList.get(ANY.toLowerCase());
    }

    @Override
    /** Returns the schema elements from the specified URI */
    protected ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
	linearGen();
	return schemaElements;
    }

    public void setLanguage(String language) {
	this.language = language;
    }

    public String getLanguage() {
	return language;
    }

}
