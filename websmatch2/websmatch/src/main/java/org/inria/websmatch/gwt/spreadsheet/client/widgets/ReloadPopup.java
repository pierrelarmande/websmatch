package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import org.inria.websmatch.gwt.spreadsheet.client.composites.SpreadsheetComposite;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class ReloadPopup extends PopupPanel {
    
    private SpreadsheetComposite comp;
    private String source;
    private String id;

    public ReloadPopup(final SpreadsheetComposite comp, final String source, final String id) {
	super(true);
	
	this.setComp(comp);
	this.setSource(source);
	this.setId(id);
	
	setAnimationEnabled(true);
	this.setGlassEnabled(true);
	this.setPopupPosition(100, 100);
	
	CaptionPanel cptnpnlNewPanel = new CaptionPanel("New panel");
	cptnpnlNewPanel.setCaptionHTML("Document already in the system");
	setWidget(cptnpnlNewPanel);
	cptnpnlNewPanel.setSize("388px", "77px");
	
	FlowPanel verticalPanel = new FlowPanel();
	cptnpnlNewPanel.setContentWidget(verticalPanel);
	verticalPanel.setSize("386px", "108px");
	
	Label lblNewLabel = new Label("This document is already integrated in the system, do you want to reload it as it was stored?");
	verticalPanel.add(lblNewLabel);
	
	Button btnNewButton_1 = new Button("Yes");
	btnNewButton_1.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
		    hide();		 
		    comp.loadXMLSchema(id);
		    comp.setReloaded(true);
		    comp.setObjectId(id);
		}
	});
	
	Button btnNewButton = new Button("No (reparse the file)");
	btnNewButton.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
		    hide();
		    comp.parseSpreadsheet(source, true,null);
		    comp.setReloaded(true);
		    comp.setObjectId(id);
		    //comp.getSchemaAttributes(source, id);
		}
	});
	
	FlexTable flexTable = new FlexTable();
	
	if(comp.getUsername().equals("demo")) btnNewButton_1.setEnabled(false);
	
	flexTable.setWidget(0, 0, btnNewButton_1);
	btnNewButton.setWidth("150px");
	flexTable.setWidget(0, 1, btnNewButton);
	btnNewButton_1.setWidth("150px");
	
	verticalPanel.add(flexTable);
	flexTable.setWidth("384px");
	flexTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
	flexTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    }

    public void setComp(SpreadsheetComposite comp) {
	this.comp = comp;
    }

    public SpreadsheetComposite getComp() {
	return comp;
    }

    public void setSource(String source) {
	this.source = source;
    }

    public String getSource() {
	return source;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getId() {
	return id;
    }

}
