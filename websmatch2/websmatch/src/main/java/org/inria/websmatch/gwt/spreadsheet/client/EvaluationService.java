package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("EvaluationService")
public interface EvaluationService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static EvaluationServiceAsync instance;
		public static EvaluationServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(EvaluationService.class);
			}
			return instance;
		}
	}
	
	public ArrayList<ArrayList<String>> getEvaluationResults(boolean byTech, String tech, boolean byDoc, String targetDoc);
}
