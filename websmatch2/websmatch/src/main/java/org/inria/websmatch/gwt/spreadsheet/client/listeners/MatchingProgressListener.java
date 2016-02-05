package org.inria.websmatch.gwt.spreadsheet.client.listeners;

import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public abstract interface MatchingProgressListener extends RemoteEventListener
{
    void onProgress(MatchingProgressEvent event);
}
