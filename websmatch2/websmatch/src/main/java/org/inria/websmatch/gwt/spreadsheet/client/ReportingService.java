package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("ReportingService")
public interface ReportingService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static ReportingServiceAsync instance;
		public static ReportingServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(ReportingService.class);
			}
			return instance;
		}
	}
	
	public void insertReport(String user, String fileName, String description, boolean ccDetectionPb, boolean attrDetectionPb);
}
