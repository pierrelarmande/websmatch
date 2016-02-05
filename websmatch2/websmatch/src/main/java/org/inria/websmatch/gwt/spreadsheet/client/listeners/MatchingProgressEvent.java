package org.inria.websmatch.gwt.spreadsheet.client.listeners;

import de.novanic.eventservice.client.event.Event;

public class MatchingProgressEvent implements Event{

    /**
     * 
     */
    private static final long serialVersionUID = 5309614572192563811L;
    private int matchCount = 0;
    private int maxMatch = 0;
    private String msg = new String();
    
    public MatchingProgressEvent(){
	
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    public void setMaxMatch(int maxMatch) {
	this.maxMatch = maxMatch;
    }

    public int getMaxMatch() {
	return maxMatch;
    }

    public void setMsg(String msg) {
	this.msg = "Status : "+msg;
    }

    public String getMsg() {
	return msg;
    }
    
}
