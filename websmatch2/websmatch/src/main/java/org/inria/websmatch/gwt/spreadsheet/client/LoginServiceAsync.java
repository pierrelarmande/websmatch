package org.inria.websmatch.gwt.spreadsheet.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {
	public void login(String username, String password, AsyncCallback<String> callback);
}
