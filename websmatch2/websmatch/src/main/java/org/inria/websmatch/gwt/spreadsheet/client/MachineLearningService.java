package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("MachineLearningService")
public interface MachineLearningService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static MachineLearningServiceAsync instance;
		public static MachineLearningServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(MachineLearningService.class);
			}
			return instance;
		}
	}
	
	public void addDocumentToML(String user, String filename, String baseDir);
	
	public ArrayList<SimpleCell> getAttributeCellsFromML(String user, String filename);
	
	public SimpleCell getCellFromML(String user, String filename, int sheet, int x, int y);
}
