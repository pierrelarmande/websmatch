package org.inria.websmatch.gwt.spreadsheet.client.handlers;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.MainFrame;
import org.inria.websmatch.gwt.spreadsheet.client.composites.TwoWayViewer;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSchema;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.CustomCanvas;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ClusterCanvasMouseClickHandler implements MouseDownHandler{
    
    private String firstClicked = null;
    private MainFrame frame = null;
    private MatchingResultsServiceAsync mresService = (MatchingResultsServiceAsync) GWT.create(MatchingResultsService.class);

    @Override
    public void onMouseDown(MouseDownEvent event) {
	
	CustomCanvas widget = (CustomCanvas) event.getSource();
	
	if(firstClicked == null) firstClicked = widget.getNodeIdAt(event.getX(), event.getY());
	else{
	    
	    if(!firstClicked.equals(widget.getNodeIdAt(event.getX(), event.getY()))){
		
		final String second = widget.getNodeIdAt(event.getX(), event.getY());
		
	    // second click, go on 2 Way View
	    mresService.getMatchedSchemas(new AsyncCallback<List<SimpleSchema>>() {

		    @Override
		    public void onFailure(Throwable arg0) {

		    }

		    @Override
		    public void onSuccess(List<SimpleSchema> arg0) {			
						
			frame.setMainWidget(new TwoWayViewer(arg0, arg0, frame));
			((TwoWayViewer)frame.getMainWidget()).loadResults(firstClicked, second);
			
		    }

		});
	    }
	}
	
    }

    public void setFrame(MainFrame frame) {
	this.frame = frame;
    }

    public MainFrame getFrame() {
	return frame;
    }

}
