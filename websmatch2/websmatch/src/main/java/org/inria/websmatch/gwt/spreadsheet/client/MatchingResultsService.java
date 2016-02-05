package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingResult;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingScores;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSchema;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingScores;

@SuppressWarnings("deprecation")
@RemoteServiceRelativePath("MatchingResultsService")
public interface MatchingResultsService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static MatchingResultsServiceAsync instance;
		public static MatchingResultsServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(MatchingResultsService.class);
			}
			return instance;
		}
	}
	
	public List<SimpleSchema> getMatchedSchemas();
	
	public List<SimpleMatchTech> getMatchingTechs();
	
	public List<MatchingResult> getResults(String leftId, String rightId, String treshold, String tech);
	
	public List<MatchingResult> getProbaResults(String leftId, String rightId);
	
	public void updateExpert(int id_element1, int id_element2, int val, int sid1, int sid2);
	
	public MatchingScores getScores(int id_element1, int id_schema1, int id_element2, int id_schema2);
}
