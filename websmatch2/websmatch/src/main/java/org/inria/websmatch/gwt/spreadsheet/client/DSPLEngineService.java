package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.HashMap;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

@RemoteServiceRelativePath("DSPLEngineService")
public interface DSPLEngineService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static DSPLEngineServiceAsync instance;
		public static DSPLEngineServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(DSPLEngineService.class);
			}
			return instance;
		}
	}
	
	/**
	 * Generate the XML file using DSPL Engine format from DataPublica
	 * 
	 * @param data Meta informations
	 * @param editedSheets The sheets from XLS editor
	 * @return The XML file
	 */
	public String createXMLFile(HashMap<String, String> data, SimpleSheet[] editedSheets, String ref, String dataSetName, String dataSetDescription, String editorSetName, String editerSetDescription, String bidimValueName, boolean getZip);

}
