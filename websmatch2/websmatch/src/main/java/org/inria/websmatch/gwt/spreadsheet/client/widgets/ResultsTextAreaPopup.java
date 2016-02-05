package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;

public class ResultsTextAreaPopup extends PopupPanel{

    public ResultsTextAreaPopup(String text) {
	super(false);
	
	this.setWidth("800px");
	this.setHeight("400px");
	
	FlowPanel mainPanel = new FlowPanel();
	
	TextArea area = new TextArea();
	area.setText(text);	
	area.setWidth("796px");
	area.setHeight("370px");
	
	mainPanel.add(area);
	
	Button close = new Button("Close");
	close.addClickHandler(new ClickHandler(){

	    @Override
	    public void onClick(ClickEvent event) {
		hide();
	    }
	    
	});
	
	mainPanel.addStyleName("center");
	mainPanel.add(close);
	
	setGlassEnabled(true);
	
	this.add(mainPanel);
	this.center();
	this.setAnimationEnabled(true);
	this.show();
    }
}
