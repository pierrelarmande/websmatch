package org.inria.websmatch.gwt.spreadsheet.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MatchingServiceAsync {
    
    public void matchDocuments(String docId1, String docId2, String dbName, AsyncCallback<Void> callback);

    public void completeMatching(String dbName, AsyncCallback<Void> callback);

    public void matchOntoFiles(String file1, String file2, boolean withDef, boolean generateFile, AsyncCallback<String> callback);
    
    public void matchCSVOboFiles(String file1, String file2, String annotator, final String score, String ontoId, boolean generateFile, int labelPlace, int descPlace, int maxLevel, AsyncCallback<String> callback);

    public void annotateN3WithObo(String n3File, String oboFile, boolean withDef, String annotator, String ontoId, boolean generateFile, AsyncCallback<String> callback);

    public void getOntologiesWithIds(String annotator, AsyncCallback<List<String[]>> callback);
}
