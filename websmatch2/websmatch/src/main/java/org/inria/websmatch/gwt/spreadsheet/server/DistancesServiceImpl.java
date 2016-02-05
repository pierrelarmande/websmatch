package org.inria.websmatch.gwt.spreadsheet.server;

import java.util.List;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.DistancesService;
import org.inria.websmatch.gwt.spreadsheet.client.models.DistanceData;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DistancesServiceImpl extends RemoteServiceServlet implements DistancesService {

    /**
     * 
     */
    private static final long serialVersionUID = 3911517931610983090L;

    @Override
    public List<DistanceData> getDistancesFromThisDoc(String docId, String dbName) {
	
	MongoDBConnector con = MongoDBConnector.getInstance();
	return con.getDistancesFromThisDoc(docId, dbName);
	
    }
}
