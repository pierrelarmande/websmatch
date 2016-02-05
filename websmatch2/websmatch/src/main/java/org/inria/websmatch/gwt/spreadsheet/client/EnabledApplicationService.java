package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("EnabledApplicationService")
public interface EnabledApplicationService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static EnabledApplicationServiceAsync instance;
		public static EnabledApplicationServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(EnabledApplicationService.class);
			}
			return instance;
		}
	}
	
	public String[] getAppList();
}
