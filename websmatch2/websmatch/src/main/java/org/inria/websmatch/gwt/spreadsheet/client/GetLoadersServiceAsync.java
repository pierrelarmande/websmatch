package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GetLoadersServiceAsync {
    
    public void getLoaders(String key, AsyncCallback<String[]> callback);

}
