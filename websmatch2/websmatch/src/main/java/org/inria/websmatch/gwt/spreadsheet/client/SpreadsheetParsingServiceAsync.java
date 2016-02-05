package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;

public interface SpreadsheetParsingServiceAsync {
	/**
	 * Method for parsing an XLS/CSV file
	 * 
	 * @param URI The location of the XLS/CSV file
	 * @param callback the callback to return Table of title and cells (String values)
	 * @throws Throwable 
	 */
	
	public void parseSpreadsheet(String userName, String URI, boolean withAttrDetect, String schemaId, AsyncCallback<SimpleSheet[]> callback);
	
	@Deprecated
	public void getSchemas(boolean onlyXls, String user, AsyncCallback<List<SchemaData>> callback);
	
	@Deprecated
	public void getSchemas(boolean onlyXls, AsyncCallback<List<SchemaData>> callback);
	
	public void storeSchema(String name, String source, String author, String description, boolean withML, Integer crawl_id, Integer publication_id, SimpleSheet[] editedSheets, AsyncCallback<List<String>> callback);
	
	/*@Deprecated
	public void updateCell(SimpleCell cell, AsyncCallback<SimpleCell> callback);*/
	
	/*@Deprecated
	public void getSchemaAttributes(String id, AsyncCallback<ArrayList<SimpleCell>> callback);*/
	
	public void matchSchemas(Integer sourceId, Integer targetId, Integer group, String tech, AsyncCallback<Void> callback);
	
	public void getEntityItem(AsyncCallback<ArrayList<SimpleEntity>> callback);
	    
	public void sendToDataPublica(HashMap<String,String> data, SimpleSheet[] editedSheets/*,List<String> attributeList*/, boolean withData, AsyncCallback<Void> callback);

	void getSheet(String userName, int place, AsyncCallback<SimpleSheet> callback);

	void getTree(String userName, int place, AsyncCallback<ArrayList<SimpleCell>> callback);

	void createXMLFile(HashMap<String, String> data, SimpleSheet[] editedSheets, boolean withData, AsyncCallback<String> callback);

	void createDSPLFile(String webSMatchFileName, SimpleSheet[] sheets, String dataSetName, String dataSetDescription, AsyncCallback<String> callback);

	@Deprecated
	void getXMLFile(String schemaId, AsyncCallback<String> callback);

	void visualizeSpreadsheet(String filePath, String name, AsyncCallback<Void> callback);
	
	//public void getCurrentSelectedAttributes(AsyncCallback<List<String>> callback);
}
