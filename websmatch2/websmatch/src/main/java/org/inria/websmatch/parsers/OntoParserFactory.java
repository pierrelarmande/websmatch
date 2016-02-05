package org.inria.websmatch.parsers;

public class OntoParserFactory {
    public static OntoParser createOntoParser(String filePath){
	if(filePath.toLowerCase().indexOf(".obo") != -1) return new OboParser(filePath);
	else if(filePath.toLowerCase().indexOf(".rdf") != -1)  return new RDFParser(filePath);
	else return null;
    }
}
