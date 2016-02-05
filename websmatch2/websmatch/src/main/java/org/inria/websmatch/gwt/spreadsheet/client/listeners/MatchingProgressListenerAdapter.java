package org.inria.websmatch.gwt.spreadsheet.client.listeners;

import de.novanic.eventservice.client.event.Event;

public abstract class MatchingProgressListenerAdapter implements MatchingProgressListener{

    @Override
    public void apply(Event anEvent) {
	
	if(anEvent instanceof MatchingProgressEvent) {
            onProgress((MatchingProgressEvent)anEvent);
	} 
	
    }

    @Override
    public void onProgress(MatchingProgressEvent event) {
	
    }

}
