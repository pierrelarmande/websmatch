package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.HashMap;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

public interface DSPLEngineServiceAsync {

    public void createXMLFile(HashMap<String, String> data, SimpleSheet[] editedSheets, String ref, String dataSetName, String dataSetDescription, String editorSetName, String editerSetDescription, String bidimValueName, boolean getZip, AsyncCallback<String> callback);

}
