package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleGraphComponent;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SchemaServiceAsync {
	public void getSchemaElements(String schemaId, AsyncCallback<ArrayList<SimpleSchemaElement>> callback);
	
	public void matchSchemas(Integer sourceId, Integer targetId, Integer groupId, String tech, AsyncCallback<Void> callback);
	
	public void importSchema(String uri, String importer, String userName, AsyncCallback<ArrayList<SimpleSchemaElement>> callback);
	
	public void getSchemaTree(String schemaId, boolean rtl, AsyncCallback<ArrayList<SimpleGraphComponent>> callback);

}
