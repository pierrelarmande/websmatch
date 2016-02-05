package org.inria.websmatch.gwt.spreadsheet.server;

import java.util.Arrays;
import java.util.List;

import org.inria.websmatch.dsplEngine.geo.datapublica.Loaders;
import org.inria.websmatch.gwt.spreadsheet.client.GetLoadersService;

import de.novanic.eventservice.service.RemoteEventServiceServlet;

public class GetLoadersServiceImpl extends RemoteEventServiceServlet implements GetLoadersService {

    /**
     * 
     */
    private static final long serialVersionUID = -2753778758299803602L;

    @Override
    public String[] getLoaders(String key) {
		
	Loaders loaders = Loaders.getInstance();
	List<String[]> tmpList = loaders.getTypeVal().get(key);
	
	String[] res = new String[tmpList.size()];
	
	//
	if(key.equals("dp:pays")){
	    for(int i = 0; i < tmpList.size(); i++) res[i] = tmpList.get(i)[3].substring(1,tmpList.get(i)[3].length()-1); 
	}else{
	    for(int i = 0; i < tmpList.size(); i++) res[i] = tmpList.get(i)[1].substring(1,tmpList.get(i)[1].length()-1); 
	}
	//
	
	Arrays.sort(res);
	
	return res;
    }
}
