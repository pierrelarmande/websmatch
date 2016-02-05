package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ReportingServiceAsync {

    void insertReport(String user, String fileName, String description, boolean ccDetectionPb, boolean attrDetectionPb, AsyncCallback<Void> callback);

}
