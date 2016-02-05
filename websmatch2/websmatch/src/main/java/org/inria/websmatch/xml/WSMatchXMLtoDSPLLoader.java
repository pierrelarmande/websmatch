package org.inria.websmatch.xml;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.inria.websmatch.dspl.DSPLSlice;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xml.handlers.WebSMatchFileToDSPLHandler;
import org.inria.websmatch.dspl.DSPLSlice;
import org.inria.websmatch.xml.handlers.WebSMatchFileToDSPLHandler;
import org.xml.sax.SAXException;

public class WSMatchXMLtoDSPLLoader {

	private boolean _DEBUG = false;
	private String webSMatchFileNameFullPath;


	public WSMatchXMLtoDSPLLoader(String webSMatchFileNameFullPath){

		this.webSMatchFileNameFullPath = webSMatchFileNameFullPath;

	}

	public LinkedList<DSPLSlice> getSlices(){
		return this.parseFile();
	}

	private LinkedList<DSPLSlice> parseFile(){

		if(_DEBUG) System.out.println("Load : "+webSMatchFileNameFullPath);

		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			javax.xml.parsers.SAXParser parser = factory.newSAXParser();
			File file = new File(webSMatchFileNameFullPath);
			WebSMatchFileToDSPLHandler handler = new WebSMatchFileToDSPLHandler(webSMatchFileNameFullPath.substring(webSMatchFileNameFullPath.lastIndexOf("/")+1, webSMatchFileNameFullPath.lastIndexOf(".")));
			parser.parse(file, handler);

			return handler.getSlices();

		}catch(ParserConfigurationException | SAXException | IOException e){
			L.Error(e.getMessage(),e);
		}

		return null;
	}

}
