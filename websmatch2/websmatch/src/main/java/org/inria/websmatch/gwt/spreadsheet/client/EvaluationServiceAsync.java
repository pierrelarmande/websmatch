package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EvaluationServiceAsync {
	public void getEvaluationResults(boolean byTech, String tech, boolean byDoc, String targetDoc, AsyncCallback<ArrayList<ArrayList<String>>> callback);
}
