package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.IntegrationService;
import org.inria.websmatch.gwt.spreadsheet.client.IntegrationServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetViewer;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageService;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class IntegrationExportPopup extends PopupPanel {
    
    // the services RPC
    // private SpreadsheetParsingServiceAsync parsingService = (SpreadsheetParsingServiceAsync) GWT.create(SpreadsheetParsingService.class);
    private IntegrationServiceAsync integrationService = (IntegrationServiceAsync) GWT.create(IntegrationService.class);
    private XMLStorageServiceAsync storageService =  (XMLStorageServiceAsync) GWT.create(XMLStorageService.class);
    
    private ListBox listBox;
    private ListBox listBox_1;
    
    private Button btnNewButton;
    private Button btnNewButton_1;
    
    public IntegrationExportPopup() {
	
	super(true);
	
	this.setAnimationEnabled(true);
	this.setGlassEnabled(true);
	this.setPopupPosition(100, 100);
    	
    	FlowPanel flowPanel = new FlowPanel();
    	flowPanel.setSize("674px", "228px");
    	
    	FlexTable flexTable = new FlexTable();
    	
    	Label lblNewLabel = new Label("Schema one : ");
    	Label lblNewLabel_1 = new Label("Schema two : ");
    	
    	flexTable.setWidget(0, 0, lblNewLabel);
    	flexTable.setWidget(1, 0, lblNewLabel_1);
    	
    	listBox = new ListBox();
    	listBox.setVisibleItemCount(1);
    	
    	listBox_1 = new ListBox();
    	listBox_1.setVisibleItemCount(1);
    	
    	flexTable.setWidget(0, 1, listBox);
    	listBox.setWidth("335px");
    	flexTable.setWidget(1, 1, listBox_1);
    	listBox_1.setWidth("335px");
    	
    	btnNewButton = new Button("Export as a zip (DSPL)");
    	btnNewButton.setEnabled(false);
    	
    	final TextBox textBox = new TextBox();
    	textBox.setText("default" + System.currentTimeMillis());
    	
    	final IntegrationExportPopup currentPop = this;
    	
    	btnNewButton.addClickHandler(new ClickHandler(){

	    @Override
	    public void onClick(ClickEvent event) {
		
		String schema1 = listBox.getItemText(listBox.getSelectedIndex());
		String schema2 = listBox_1.getItemText(listBox_1.getSelectedIndex());
		
		// get ids
		String id1 = schema1.substring(schema1.lastIndexOf(" (Id : ",schema1.length()-1)+7,schema1.length()-1);
		String id2 = schema2.substring(schema2.lastIndexOf(" (Id : ",schema2.length()-1)+7,schema2.length()-1);		
		
		String name = new String();
		
		/*if(!textBox.getText().equals("")) name = "default" + System.currentTimeMillis();
		else*/ name = textBox.getText();
		
		// make the document and export
		integrationService.getIntegratedDSPLFile(id1, id2, name, SpreadsheetViewer.username, new AsyncCallback<String>(){

		    @Override
		    public void onFailure(Throwable caught) {
			
			if(currentPop != null && currentPop.isShowing()) currentPop.hide();
			Window.alert("Can't create DSPL integrated file");
			
		    }

		    @Override
		    public void onSuccess(String zipPath) {
			
			if(currentPop != null && currentPop.isShowing()) currentPop.hide();
			
			// get it!
			String baseURL = GWT.getModuleBaseURL();
			String url = baseURL + "DownloadFileServlet?filename=" + zipPath;
			Window.open(
				url,
				"",
				"menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=no,toolbar=true, width="
					+ Window.getClientWidth() + ",height=" + Window.getClientHeight());
			
		    }		    
		});		
	    }    
    	});
    	
    	btnNewButton_1 = new Button("Export to visualizator");
    	btnNewButton_1.addClickHandler(new ClickHandler() {
    		public void onClick(ClickEvent event) {
    		    
    		String schema1 = listBox.getItemText(listBox.getSelectedIndex());
		String schema2 = listBox_1.getItemText(listBox_1.getSelectedIndex());
		
		// get ids
		String id1 = schema1.substring(schema1.lastIndexOf(" (Id : ",schema1.length()-1)+7,schema1.length()-1);
		String id2 = schema2.substring(schema2.lastIndexOf(" (Id : ",schema2.length()-1)+7,schema2.length()-1);		
		
		String name = new String();
		
		/*if(!textBox.getText().equals("")) name = "default" + System.currentTimeMillis();
		else*/ name = textBox.getText();
		
		final String fname = name;
		
		// first generate the file and then upload using POST method
		integrationService.integrateAndPublishDSPLFile(id1, id2, name, SpreadsheetViewer.username, new AsyncCallback<Void>(){

		    @Override
		    public void onFailure(Throwable caught) {
			
			if(currentPop != null && currentPop.isShowing()) currentPop.hide();
			Window.alert("Can't create DSPL integrated file");
			
		    }

		    @Override
		    public void onSuccess(Void result) {

			if(currentPop != null && currentPop.isShowing()) currentPop.hide();
			
			// show it!
			String baseUrl = SpreadsheetViewer.providerUri;
			// String baseUrl = "http://constraint.lirmm.fr:8320";
			// String baseUrl = "http://otmedia:8320";
			// String baseUrl = "http://admin-int.data-publica.com/api";
			Window.open(baseUrl+"/visualizator.html?publicationReference="
				+ fname/*fileToConvert.substring(0, fileToConvert.lastIndexOf("."))*//* + "&apiBaseURL=api/"*/, "_blank",
				null);
		    }		    
		});	
		
		// Window.open("http://api-int.data-publica.com/static/upload.html","","");
    		    
    		}
    	});
    	btnNewButton_1.setEnabled(false);
    	
    	flexTable.setWidget(3, 0, btnNewButton);
    	flexTable.setWidget(3, 1, btnNewButton_1);
    	
    	flowPanel.add(flexTable);
    	flexTable.setSize("671px", "227px");
    	flexTable.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_MIDDLE);
    	flexTable.getCellFormatter().setVerticalAlignment(2, 1, HasVerticalAlignment.ALIGN_MIDDLE);
    	flexTable.getCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_LEFT);
    	
    	Label lblNewLabel_2 = new Label("Choose a name : ");
    	
    	flexTable.setWidget(2, 0, lblNewLabel_2);
    	flexTable.setWidget(2, 1, textBox);
    	textBox.setWidth("335px");
    	flexTable.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
    	flexTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
    	flexTable.getCellFormatter().setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_CENTER);
    	flexTable.getCellFormatter().setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_CENTER);
 	
    	this.add(flowPanel);
    }
    
    public void show(){
	
	this.getSchemas();
	
	super.show();
	
    }
    
    private void getSchemas(){
	
	storageService.getDocuments(false, SpreadsheetViewer.username, new AsyncCallback<List<SchemaData>>(){

	    @Override
	    public void onFailure(Throwable caught) {
		hide();
		Window.alert("Can't load schemas");
	    }

	    @Override
	    public void onSuccess(List<SchemaData> result) {
		for(SchemaData schema : result){
		    listBox.addItem(schema.getName()+ " (Id : "+schema.getId()+")");
		    listBox_1.addItem(schema.getName()+ " (Id : "+schema.getId()+")");
		}
		
		btnNewButton.setEnabled(true);
		btnNewButton_1.setEnabled(true);	
	    }
	    
	});
	
	/*parsingService.getSchemas(true, user, new AsyncCallback<List<SchemaData>>(){

	    @Override
	    public void onFailure(Throwable caught) {
	
		hide();
		Window.alert("Can't load schemas");
		
	    }

	    @Override
	    public void onSuccess(List<SchemaData> result) {
		
		for(SchemaData schema : result){
		    listBox.addItem(schema.getName()+ " (Id : "+schema.getId()+")");
		    listBox_1.addItem(schema.getName()+ " (Id : "+schema.getId()+")");
		}
		
		btnNewButton.setEnabled(true);
		btnNewButton_1.setEnabled(true);
		
	    }
	    
	});*/
    }
}
