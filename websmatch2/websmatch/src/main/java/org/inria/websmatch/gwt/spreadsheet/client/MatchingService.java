package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.inria.websmatch.matchers.mappings.AnnotatorService;

@RemoteServiceRelativePath("MatchingService")
public interface MatchingService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static MatchingServiceAsync instance;
		public static MatchingServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(MatchingService.class);
			}
			return instance;
		}
	}

	public void matchDocuments(String docId1, String docId2, String dbName);

	public void completeMatching(String dbName);
	
	public String matchOntoFiles(String file1, String file2, boolean withDef, boolean generateFile);
	
	public String matchCSVOboFiles(String file1, String file2, String annotator, final String score, String ontoId, boolean generateFile, int labelPlace, int descPlace, int maxLevel);
	
	public String annotateN3WithObo(String n3File, String oboFile, boolean withDef, String annotator, String ontoId, boolean generateFile);

	public List<String[]> getOntologiesWithIds(String annotator);
}
