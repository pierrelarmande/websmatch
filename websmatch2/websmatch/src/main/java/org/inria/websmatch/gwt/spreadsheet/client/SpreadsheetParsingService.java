package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.exceptions.ParserException;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.inria.websmatch.gwt.spreadsheet.client.exceptions.ParserException;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;

@RemoteServiceRelativePath("SpreadsheetParsingService")
public interface SpreadsheetParsingService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static SpreadsheetParsingServiceAsync instance;
		public static SpreadsheetParsingServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(SpreadsheetParsingService.class);
			}
			return instance;
		}
	}
	
	/**
	 * Method for parsing an XLS/CSV file
	 * 
	 * @param URI The location of the XLS/CSV file
	 * @return Table of title and cells (String values)
	 * @throws Throwable 
	 */
	
	public SimpleSheet[] parseSpreadsheet(String userName, String URI, boolean withAttrDetect, String schemaId) throws ParserException;
	
	public SimpleSheet getSheet(String userName, int place);
	
	public ArrayList<SimpleCell> getTree(String userName, int place);
	
	@Deprecated
	public List<SchemaData> getSchemas(boolean onlyXls, String user);
	
	@Deprecated
	public List<SchemaData> getSchemas(boolean onlyXls);
	
	public List<String> storeSchema(String name, String source, String author, String description, boolean withML, Integer crawl_id, Integer publication_id, SimpleSheet[] editedSheets);
	
	/*@Deprecated
	public SimpleCell updateCell(SimpleCell cell);*/
	
	/*@Deprecated
	public ArrayList<SimpleCell> getSchemaAttributes(String id);*/
	
	public void matchSchemas(Integer sourceId, Integer targetId, Integer group, String tech);
	
	public ArrayList<SimpleEntity> getEntityItem();
	
	public void sendToDataPublica(HashMap<String,String> data, SimpleSheet[] editedSheets, boolean withData);//,List<String> attributeList);
	
	public String createXMLFile(HashMap<String,String> data, SimpleSheet[] editedSheets, boolean withData);
	
	public String createDSPLFile(String webSMatchFileName, SimpleSheet[] sheets, String dataSetName, String dataSetDescription);
	
	/**
	 * Method to get the XML store in database (containing all informations -title, attributes, comments-)
	 * 
	 * @param schemaId
	 * @return The XML document
	 */
	
	@Deprecated
	public String getXMLFile(String schemaId);
		
	public void visualizeSpreadsheet(String filePath, String name);
	    
}
