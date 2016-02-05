package org.inria.websmatch.gwt.spreadsheet.server;

import java.util.List;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.DetectionQualityService;
import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DetectionQualityServiceImpl extends RemoteServiceServlet implements DetectionQualityService {

    /**
     * 
     */
    private static final long serialVersionUID = 3532091346608656189L;

    @Override
    public List<DetectionQualityData> getDetectectionQualityList(boolean onlyEdited, String dbName) {
	
	return(MongoDBConnector.getInstance().getDetectionQualityList(onlyEdited, dbName));
	
    }

    @Override
    public DetectionQualityData getDetectectionQualityData(String objectId, String dbName) {
	
	return(MongoDBConnector.getInstance().getDetectionQualityData(objectId, dbName));
	
    }
}
