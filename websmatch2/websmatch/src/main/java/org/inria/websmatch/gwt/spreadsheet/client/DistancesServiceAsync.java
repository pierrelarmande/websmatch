package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.DistanceData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DistancesServiceAsync {
    
    public void getDistancesFromThisDoc(String docId, String dbName,  AsyncCallback<List<DistanceData>> callBack);

}
