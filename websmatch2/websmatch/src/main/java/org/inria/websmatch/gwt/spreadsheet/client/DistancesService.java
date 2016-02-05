package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.DistanceData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("DistancesService")
public interface DistancesService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static DistancesServiceAsync instance;
		public static DistancesServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(DistancesService.class);
			}
			return instance;
		}
	}

	List<DistanceData> getDistancesFromThisDoc(String docId, String dbName);
}
