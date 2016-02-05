package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DetectionQualityServiceAsync {
    
    public void getDetectectionQualityList(boolean onlyEdited, String dbName, AsyncCallback<List<DetectionQualityData>> callBack);
    
    public void getDetectectionQualityData(String objectId, String dbName, AsyncCallback<DetectionQualityData> callBack);

}
