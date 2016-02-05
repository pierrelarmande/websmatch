package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingService;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.SchemaCellTable;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.generic.SchemaTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class SchemaTreeLoader extends Composite {
    
    private SpreadsheetParsingServiceAsync parsingService = (SpreadsheetParsingServiceAsync) GWT.create(SpreadsheetParsingService.class);

    private PopupPanel popup = null;
    
    private SchemaTree tree;
    
    private int schemaId = -1;
    private String schemaName;
    
    public SchemaTreeLoader(final String userName) {
    	
    	FlowPanel verticalPanel = new FlowPanel();
    	initWidget(verticalPanel);
    	verticalPanel.setSize("100%", "740px");
    	
    	Button button = new Button("Load  a schema");
    	button.setHeight("32px");
    	
    	verticalPanel.add(button);
    	
    	ScrollPanel panel = new ScrollPanel();
    	panel.setStyleName("body");
    	
    	tree = new SchemaTree();
    	tree.setAnimationEnabled(true);
    	panel.add(tree);
    	tree.setSize("100%", "100%");
    	
    	verticalPanel.add(panel);
    	panel.setSize("100%", "700px");
    	
    	button.addClickHandler(new ClickHandler(){

	    @SuppressWarnings("deprecation")
	    @Override
	    public void onClick(ClickEvent event) {
		
		parsingService.getSchemas(false, userName, new AsyncCallback<List<SchemaData>>() {
		    @Override
		    public void onFailure(Throwable caught) {
			Window.alert("Can't load schemas.");
		    }

		    @Override
		    public void onSuccess(List<SchemaData> result) {
			showSchemas(result);
		    }
		});
		
	    }
    	    
    	});
    	
    }
    
    public void showSchemas(List<SchemaData> list) {

	/*popup = new PopupPanel(true);
	popup.setGlassEnabled(true);
	popup.setPopupPosition(100, 100);

	popup.add(new SchemaCellTable(list, this));
	popup.show();*/
	
	popup = new PopupPanel(true);
	popup.setGlassEnabled(true);
	popup.setAnimationEnabled(true);
	popup.setPopupPosition(100, 100);
	
	SchemaCellTable table = new SchemaCellTable(list, this);

	ScrollPanel scroll = new ScrollPanel(table);
	scroll.setWidth("100%");
	table.setWidth("100%");

	// scroll.setAlwaysShowScrollBars(true);

	popup.add(scroll);
	if(table.getRowCount() > 20) popup.setSize("1000px", "600px");
	else{
	    popup.setSize("1000px", "200px");
	}

	popup.show();

    }

    public void closePopup(){
	if(popup != null && popup.isShowing()) popup.hide();
    }
    
    public SchemaTree getTree() {
        return tree;
    }

    public void setTree(SchemaTree tree) {
        this.tree = tree;                   
    }

    public void setSchemaId(int schemaId) {
	this.schemaId = schemaId;
    }

    public int getSchemaId() {
	return schemaId;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

}
