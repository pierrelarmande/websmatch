package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface EnabledApplicationServiceAsync {
	public void getAppList(AsyncCallback<String[]> asyncCallBack);
}
