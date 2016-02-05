package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("GetLoadersService")
public interface GetLoadersService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static GetLoadersServiceAsync instance;
		public static GetLoadersServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(GetLoadersService.class);
			}
			return instance;
		}
	}

	String[] getLoaders(String key);
}
