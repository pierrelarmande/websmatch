package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleGraphComponent;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleGraphComponent;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;

@RemoteServiceRelativePath("SchemaService")
public interface SchemaService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static SchemaServiceAsync instance;
		public static SchemaServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(SchemaService.class);
			}
			return instance;
		}
	}
	
	public ArrayList<SimpleSchemaElement> getSchemaElements(String schemaId);
	
	public void matchSchemas(Integer sourceId, Integer targetId, Integer groupId, String tech);
	
	public ArrayList<SimpleSchemaElement> importSchema(String uri, String importer, String userName);
	
	public ArrayList<SimpleGraphComponent> getSchemaTree(String schemaId, boolean rtl);

}
