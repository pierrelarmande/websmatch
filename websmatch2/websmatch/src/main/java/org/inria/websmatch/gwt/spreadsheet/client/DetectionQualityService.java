package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;

@RemoteServiceRelativePath("DetectionQualityService")
public interface DetectionQualityService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static DetectionQualityServiceAsync instance;
		public static DetectionQualityServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(DetectionQualityService.class);
			}
			return instance;
		}
	}

	public List<DetectionQualityData> getDetectectionQualityList(boolean onlyEdited, String dbName);
	
	public DetectionQualityData getDetectectionQualityData(String objectId, String dbName);
}
