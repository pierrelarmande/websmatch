package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IntegrationServiceAsync {
    
    public void getIntegratedDSPLFile(String id1, String id2, String name, String dbName, AsyncCallback<String> callback);

    void integrateAndPublishDSPLFile(String id1, String id2, String name, String dbName, AsyncCallback<Void> callback);

}
