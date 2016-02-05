package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.DistancesService;
import org.inria.websmatch.gwt.spreadsheet.client.DistancesServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetViewer;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageService;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.DistanceData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;

public class DistancesListing extends Composite {
    
    private XMLStorageServiceAsync storageService =  (XMLStorageServiceAsync) GWT.create(XMLStorageService.class);
    private DistancesServiceAsync distancesService =  (DistancesServiceAsync) GWT.create(DistancesService.class);
    final ScrollPanel scroll = new ScrollPanel();
    
    public DistancesListing() {
	
	final FlowPanel panel = new FlowPanel();
	initWidget(panel);
	
	final ListBox box = new ListBox();
	panel.add(box);

	storageService.getDocuments(false,SpreadsheetViewer.username, new AsyncCallback<List<SchemaData>>(){

	    @Override
	    public void onFailure(Throwable caught) {
			
	    }

	    @Override
	    public void onSuccess(List<SchemaData> result) {
		for(SchemaData s : result){
		    box.addItem(s.getName() + "(id:"+s.getId()+")");
		}
	    }	    
	});
	
	Button getDistances = new Button("Get nearest docs");
	panel.add(getDistances);
	
	scroll.setWidth("100%");
	panel.add(scroll);
	getDistances.addClickHandler(new ClickHandler(){

	    @Override
	    public void onClick(ClickEvent event) {
		
		GWT.log(box.getItemText(box.getSelectedIndex()));
		GWT.log(""+(box.getItemText(box.getSelectedIndex()).lastIndexOf("(id:")+4));
		GWT.log(""+(box.getItemText(box.getSelectedIndex()).length()-1));
		String docId = box.getItemText(box.getSelectedIndex()).substring(box.getItemText(box.getSelectedIndex()).lastIndexOf("(id:")+4,box.getItemText(box.getSelectedIndex()).length()-1);
		GWT.log(docId);
		distancesService.getDistancesFromThisDoc(docId, SpreadsheetViewer.username, new AsyncCallback<List<DistanceData>>(){

		    @Override
		    public void onFailure(Throwable caught) {
			
		    }

		    @Override
		    public void onSuccess(List<DistanceData> result) {
				
			DistancesCellTable table = new DistancesCellTable(result);
			scroll.setWidget(table);
			scroll.setWidth("100%");
			table.setWidth("100%");			
		    }		    
		});	
	    }   
	});	
    }

    public ScrollPanel getScroll() {
        return scroll;
    }
}
