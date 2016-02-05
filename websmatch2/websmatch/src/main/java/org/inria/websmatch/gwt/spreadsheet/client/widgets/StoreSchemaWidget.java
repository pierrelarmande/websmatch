package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.HashMap;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingService;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class StoreSchemaWidget extends PopupPanel {

    public StoreSchemaWidget(String user, HashMap<String, String> dataPublica, final SimpleSheet[] editedSheets) {
	super(true);
	setAnimationEnabled(true);
	this.setGlassEnabled(true);
	this.setPopupPosition(100, 100);
	setSize("700px", "250px");

	// now initialize the form to set author/desc

	CaptionPanel cptnpnlSubmitSchema = new CaptionPanel("Submit schema");
	cptnpnlSubmitSchema.setCaptionHTML("Submit schema");
	cptnpnlSubmitSchema.setSize("95%", "95%");

	FlowPanel flowPanel = new FlowPanel();
	cptnpnlSubmitSchema.setContentWidget(flowPanel);
	flowPanel.setSize("100%", "100%");

	Grid grid = new Grid(2, 2);
	flowPanel.add(grid);
	grid.setSize("98%", "80%");

	Label label = new Label("Schema name");
	grid.setWidget(0, 0, label);
	label.setSize("100%", "100%");

	TextBox textBox = new TextBox();
	grid.setWidget(0, 1, textBox);
	textBox.setWidth("90%");
	Label label_1 = new Label("Description");
	grid.setWidget(1, 0, label_1);
	label_1.setSize("100%", "100%");

	TextArea textArea = new TextArea();
	grid.setWidget(1, 1, textArea);
	textArea.setSize("90%", "90%");

	Button button = new Button("New button");

	final TextBox name = textBox;
	final TextArea desc = textArea;
	final String fuser = user;

	final Button fbutton = button;

	// final HashMap<String,String> data = dataPublica;

	button.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		fbutton.setEnabled(false);

		final SpreadsheetParsingServiceAsync service = SpreadsheetParsingService.Util.getInstance();

		AsyncCallback<List<String>> callback = new AsyncCallback<List<String>>() {

		    @Override
		    public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub

		    }

		    @Override
		    public void onSuccess(List<String> attributeList) {
			/*
			 * if(data != null) service.sendToDataPublica(data, new
			 * AsyncCallback<Void>(){
			 * 
			 * @Override public void onFailure(Throwable caught) {
			 * // TODO Auto-generated method stub
			 * Window.alert("Can't send to post_url"); }
			 * 
			 * @Override public void onSuccess(Void result) { //
			 * TODO Auto-generated method stub
			 * Window.open(data.get("callback_url"), "_self", ""); }
			 * 
			 * });
			 */
		    }

		};
		service.storeSchema(name.getText(), "", fuser, desc.getText(), true, null, null, editedSheets, callback);
		
		setVisible(false);
	    }
	});
	button.setText("Submit schema");
	flowPanel.add(button);
	button.setSize("143px", "38px");
	this.add(cptnpnlSubmitSchema);

    }

}
