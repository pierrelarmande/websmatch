package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingResult;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingScores;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSchema;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingScores;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSchema;

@SuppressWarnings("deprecation")
public interface MatchingResultsServiceAsync {
	public void getMatchedSchemas(AsyncCallback<List<SimpleSchema>> callback);
	
	public void getMatchingTechs(AsyncCallback<List<SimpleMatchTech>> callback);
	
	public void getResults(String leftId, String rightId, String treshold, String tech, AsyncCallback<List<MatchingResult>> callback);
	
	public void updateExpert(int id_element1, int id_element2, int val, int sid1, int sid2, AsyncCallback<Void> callback);
	
	public void getScores(int id_element1, int schema_id1, int id_element2, int schema_id2, AsyncCallback<MatchingScores> callback);

	public void getProbaResults(String leftId, String rightId, AsyncCallback<List<MatchingResult>> callback);
}
