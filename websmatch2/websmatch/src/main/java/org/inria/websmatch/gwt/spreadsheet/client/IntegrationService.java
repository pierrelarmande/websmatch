package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("IntegrationService")
public interface IntegrationService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static IntegrationServiceAsync instance;
		public static IntegrationServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(IntegrationService.class);
			}
			return instance;
		}
	}

	public String getIntegratedDSPLFile(String id1, String id2, String name, String dbName);
	
	public void integrateAndPublishDSPLFile(String id1, String id2, String name, String dbName);
}
