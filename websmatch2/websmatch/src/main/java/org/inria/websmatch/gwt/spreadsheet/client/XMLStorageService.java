package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("XMLStorageService")
public interface XMLStorageService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static XMLStorageServiceAsync instance;
		public static XMLStorageServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(XMLStorageService.class);
			}
			return instance;
		}
	}

	public String importDocument(SimpleSheet[] original, SimpleSheet[] edited, String name, String source, String user, String description, String dbName, String crawl_id, String publication_id, boolean reloaded, String objectId, boolean ccDetectionPb, boolean attrDetectionPb, String detectPb, boolean trashed);
	
	public String importDocument(String auto_xml, String edit_xml, String name, String source, String user, String description, String dbName, String crawl_id, String publication_id, boolean reloaded, String objectId, boolean ccDetectionPb, boolean attrDetectionPb, String detectPb, boolean trashed);
	
	public String getDocument(String object_id, String dbName);
	
	public List<SchemaData> getDocuments(boolean onlyEdited, String dbName);
	
	public String getFileName(String object_id, String dbName);
}
