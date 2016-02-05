package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

public interface XMLStorageServiceAsync {
    
    public void importDocument(SimpleSheet[] original, SimpleSheet[] edited, String name, String source, String user, String description, String dbName, String crawl_id, String publication_id, boolean reloaded, String objectId, boolean ccDetectionPb, boolean attrDetectionPb, String detectPb, boolean trashed, AsyncCallback<String> callback);

    public void importDocument(String auto_xml, String edit_xml, String name, String source, String user, String description, String dbName, String crawl_id, String publication_id, boolean reloaded, String objectId, boolean ccDetectionPb, boolean attrDetectionPb, String detectPb, boolean trashed, AsyncCallback<String> callback);

    public void getDocument(String object_id, String dbName, AsyncCallback<String> callback);

    public void getDocuments(boolean onlyEdited, String dbName, AsyncCallback<List<SchemaData>> callback);
    
    public void getFileName(String object_id, String dbName, AsyncCallback<String> callback);
}
