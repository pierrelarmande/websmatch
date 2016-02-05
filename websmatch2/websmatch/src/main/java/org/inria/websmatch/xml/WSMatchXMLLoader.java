package org.inria.websmatch.xml;

import java.io.IOException;
import java.io.StringReader;

import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.L;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class WSMatchXMLLoader {
    
    /**
     * Take an XML string
     * 
     * @param xmlContent
     */
    
    private String content;
    
    public WSMatchXMLLoader(String xmlContent){
	content = xmlContent.replaceAll("\\xA0", "");
    }
    
    public Document getDocument(){
	return this.parseContent();
    }
    
    private Document parseContent(){
	SAXBuilder builder = new SAXBuilder();
	Document document = null;
	
	try {
	    document = builder.build(new StringReader(content.trim()));
	} catch (JDOMException e) {	   
	    L.Error(e.getMessage(), e);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	} 
	
	return document;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
