package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MachineLearningServiceAsync {
	public void addDocumentToML(String user, String filename, String baseDir, AsyncCallback<Void> callback);
	
	public void getAttributeCellsFromML(String user, String filename, AsyncCallback<ArrayList<SimpleCell>> callback);
	
	public void getCellFromML(String user, String filename, int sheet, int x, int y, AsyncCallback<SimpleCell> callback);
}
