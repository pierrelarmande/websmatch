package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.Map;

import org.inria.websmatch.gwt.spreadsheet.client.models.Node;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ClusteringServiceAsync {
	public void getClusterNodes(int w, int h, boolean datapublica, String dbName, AsyncCallback<Map<Map<Node, double[]>, Map<Node, Integer[]>>> callback);
	
	//public void getStringClusterNodes(int w, int h, AsyncCallback<Map<Map<Node, double[]>, Map<Node, Integer[]>>> callback);
}
