package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.Map;

import org.inria.websmatch.gwt.spreadsheet.client.models.Node;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("ClusteringService")
public interface ClusteringService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static ClusteringServiceAsync instance;
		public static ClusteringServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(ClusteringService.class);
			}
			return instance;
		}
	}
	
	public Map<Map<Node, double[]>, Map<Node, Integer[]>> getClusterNodes(int w, int h, boolean datapublica, String dbName);
	
	//public Map<Map<Node, double[]>, Map<Node, Integer[]>> getStringClusterNodes(int w, int h);
}
