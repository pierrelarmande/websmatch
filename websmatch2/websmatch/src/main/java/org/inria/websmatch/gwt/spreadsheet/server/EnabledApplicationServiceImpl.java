package org.inria.websmatch.gwt.spreadsheet.server;

import org.inria.websmatch.gwt.spreadsheet.client.EnabledApplicationService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.inria.websmatch.gwt.spreadsheet.client.EnabledApplicationService;

public class EnabledApplicationServiceImpl extends RemoteServiceServlet implements EnabledApplicationService {

    /**
     * 
     */
    private static final long serialVersionUID = -2904480410431835099L;
    
    private String[] appList;
    
    public void init(){
	appList = getServletContext().getInitParameter("appList").split(";");
    }

    @Override
    public String[] getAppList() {
	
	return this.appList;
	
    }
}
