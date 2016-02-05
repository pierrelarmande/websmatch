package org.inria.websmatch.gwt.spreadsheet.client.composites.matcher.popup;

import org.inria.websmatch.gwt.spreadsheet.client.MatchingService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.PopupPanel;

public abstract class MatcherPopup extends PopupPanel{

    private String firstFile;
    private String secondFile;

    private MatchingServiceAsync matchService = (MatchingServiceAsync) GWT.create(MatchingService.class);

    public MatcherPopup(){
        super(true);

        setFirstFile("");
        setSecondFile("");

        setGlassEnabled(true);
        //center();
        setPopupPosition(400, 200);
        setAnimationEnabled(true);
    }

    public void setFirstFile(String firstFile) {
        this.firstFile = firstFile;
    }

    public String getFirstFile() {
        return firstFile;
    }

    public void setSecondFile(String secondFile) {
        this.secondFile = secondFile;
    }

    public String getSecondFile() {
        return secondFile;
    }

    public void setMatchService(MatchingServiceAsync matchService) {
        this.matchService = matchService;
    }

    public MatchingServiceAsync getMatchService() {
        return matchService;
    }
}
