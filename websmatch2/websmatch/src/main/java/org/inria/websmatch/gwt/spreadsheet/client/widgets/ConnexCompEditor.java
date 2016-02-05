package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.composites.ImprovedIntegerLabel;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SpreadsheetComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ConnexCompEditor extends PopupPanel {
    
    private SpreadsheetComposite comp;
    private ImprovedIntegerLabel startX;
    private ImprovedIntegerLabel endX;
    private ImprovedIntegerLabel startY;
    private ImprovedIntegerLabel endY;
    private ConnexComposant ccToEdit;
    
    public ConnexCompEditor(final SpreadsheetComposite comp, SimpleEntityItem ent, int y) {
	super(true);
	
	GWT.log(ent.toString());
	
	this.setComp(comp);
	
	setAnimationEnabled(true);
	// this.setGlassEnabled(true);
	this.setPopupPosition(10, 100);
	
	CaptionPanel cptnpnlNewPanel = new CaptionPanel("New panel");
	cptnpnlNewPanel.setCaptionHTML("Edit the table");
	setWidget(cptnpnlNewPanel);
	cptnpnlNewPanel.setSize("273px", "165px");
	
	FlowPanel flowPanel = new FlowPanel();
	cptnpnlNewPanel.setContentWidget(flowPanel);
	flowPanel.setSize("268px", "153px");
	
	Label startLabel = new Label("Start line");
	Label endLabel = new Label("End line");
	
	FlexTable flexTable = new FlexTable();
	
	Label label = new Label("Start col");	
	Label label_1 = new Label("End col");

	flexTable.setWidget(0, 0, startLabel);
	flexTable.setWidget(1, 0, endLabel);
	flexTable.setWidget(2, 0, label);
	flexTable.setWidget(3, 0, label_1);
	
	for(int i = 0 ; i < comp.getEditedSheets().length; i++){
	    if(comp.getEditedSheets()[i] != null) GWT.log(comp.getEditedSheets()[i].getTitle());
	}
	
	ArrayList<ConnexComposant> ccList = comp.getEditedSheets()[ent.getSheetIndex()].getConnexComps();
	ConnexComposant cc = null;
	if(ccList.size() == 1) cc = ccList.get(0);
	else{
	    for(int i = 0; i < ccList.size(); i++){
		if(ccList.get(i).containsPoint(ent.getStartX(), ent.getStartY())){
		    cc = ccList.get(i); break;
		}
	    }
	}
	final ConnexComposant toEdit = cc;
	ccToEdit = cc;
	// SpreadsheetEditor edit = ((SpreadsheetEditor) ((ScrollPanel) comp.getTabLayoutPanel().getWidget(0)).getWidget());
	
	/*GWT.log(toEdit.toString());
	GWT.log(new Integer(toEdit.getStartX()).toString());
	GWT.log(this.toString());*/
	
	startX = new ImprovedIntegerLabel(toEdit.getStartX(),this);
	flexTable.setWidget(2, 1, startX);
	startX.setWidth("80px");
	
	endX = new ImprovedIntegerLabel(toEdit.getEndX(),this);
	flexTable.setWidget(3, 1, endX);
	endX.setWidth("80px");
	
	startY = new ImprovedIntegerLabel(toEdit.getStartY(),this);
	flexTable.setWidget(0, 1, startY);
	startY.setWidth("80px");
	
	endY = new ImprovedIntegerLabel(toEdit.getEndY(),this);
	flexTable.setWidget(1, 1, endY);
	endY.setWidth("80px");
	
	Button ok = new Button("Ok");
	ok.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
		    
		    refresh();
		    
		    hide();
		}
	});
	Button cancel = new Button("Cancel");
	cancel.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
		    
		    // remove CSS on tables
		    ((SpreadsheetEditor) ((ScrollPanel) comp.getTabLayoutPanel().getWidget(0)).getWidget()).removeTableCSS();
		    
		    toEdit.setStartX(startX.getOriginalValue());
		    toEdit.setStartY(startY.getOriginalValue());
		    toEdit.setEndX(endX.getOriginalValue());
		    toEdit.setEndY(endY.getOriginalValue());
		    
		    // refresh table		  
		    ((SpreadsheetEditor) ((ScrollPanel) comp.getTabLayoutPanel().getWidget(0)).getWidget()).setTablesCSS();
		    ((SpreadsheetEditor) ((ScrollPanel) comp.getTabLayoutPanel().getWidget(0)).getWidget()).setAttributesCSS();
		    
		    hide();
		}
	});
	
	flexTable.setWidget(4, 0, ok);
	ok.setWidth("100px");
	flexTable.setWidget(4, 1, cancel);
	cancel.setWidth("100px");
	
	flowPanel.add(flexTable);
	flexTable.setSize("250px", "147px");
	flexTable.getCellFormatter().setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_LEFT);
	flexTable.getCellFormatter().setHorizontalAlignment(4, 1, HasHorizontalAlignment.ALIGN_RIGHT);
	
    }

    public void refresh(){
		// remove CSS on tables
	    ((SpreadsheetEditor) ((ScrollPanel) comp.getTabLayoutPanel().getWidget(0)).getWidget()).removeTableCSS();
	    
	    ccToEdit.setStartX(startX.getValue());
	    ccToEdit.setStartY(startY.getValue());
	    ccToEdit.setEndX(endX.getValue());
	    ccToEdit.setEndY(endY.getValue());
	    
	    // refresh table	   
	    ((SpreadsheetEditor) ((ScrollPanel) comp.getTabLayoutPanel().getWidget(0)).getWidget()).setTablesCSS();
	    ((SpreadsheetEditor) ((ScrollPanel) comp.getTabLayoutPanel().getWidget(0)).getWidget()).setAttributesCSS();
    }
    
    public void setComp(SpreadsheetComposite comp) {
	this.comp = comp;
    }

    public SpreadsheetComposite getComp() {
	return comp;
    }

}
