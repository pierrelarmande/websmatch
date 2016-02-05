package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import org.inria.websmatch.gwt.spreadsheet.client.listeners.MatchingProgressEvent;
import org.inria.websmatch.gwt.spreadsheet.client.listeners.MatchingProgressListenerAdapter;

import com.google.gwt.widgetideas.client.ProgressBar;

import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

public class CustomProgressBar extends ProgressBar {
    
    private RemoteEventServiceFactory theEventServiceFactory = RemoteEventServiceFactory.getInstance();
    private RemoteEventService theEventService = theEventServiceFactory.getRemoteEventService();
    private MatchingProgressListenerAdapter progListener = null;
    private Domain dom = null;
    
    public CustomProgressBar(final String userName, String sid) {

	super();
	
	// we are logged, use sid for event listening
	dom = DomainFactory.getDomain(sid);

	progListener = new MatchingProgressListenerAdapter() {
	    public void onProgress(final MatchingProgressEvent event) {
		// do something
		/*System.out.println("Event received");
		System.out.println(event.getMatchCount()+"/"+event.getMaxMatch());*/
		setMax(event.getMaxMatch());
		setTextFormatter(new TextFormatter() {	    
		    @Override
		    protected String getText(ProgressBar arg0, double arg1) {
			return event.getMsg();
		    }
		});
		setProgress(event.getMatchCount());
	    }
	};
	
	theEventService.addListener(dom, progListener);
	//

    }
    
    public void setMax(int max){
	this.setMaxProgress(max);
    }
    
   public void removeListener(){
       theEventService.removeListener(dom, progListener);
   }
    
   @Override
   protected void onUnload(){
       super.onUnload();
       this.removeListener();
   }

}
