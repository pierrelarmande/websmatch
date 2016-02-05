package org.inria.websmatch.gwt.spreadsheet.client.composites.matcher.popup;

import org.inria.websmatch.gwt.spreadsheet.client.widgets.ResultsTextAreaPopup;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.WaitingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OntoMatcherPopup extends MatcherPopup {

    public OntoMatcherPopup() {
	super();
	
	final VerticalPanel mainPanel = new VerticalPanel();
	final FormWidget form = new FormWidget("Send the first RDF or OBO file");
	mainPanel.add(form);

	form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
	    public void onSubmitComplete(SubmitCompleteEvent event) {
		// ok file arrived, now parsing it
		setFirstFile(event.getResults().substring(event.getResults().indexOf(">") + 1, event.getResults().indexOf("<", event.getResults().indexOf(">"))));

		// add second form
		final FormWidget form = new FormWidget("Send the second RDF or OBO file");
		mainPanel.add(form);

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
		    public void onSubmitComplete(SubmitCompleteEvent event) {
			setSecondFile(event.getResults().substring(event.getResults().indexOf(">") + 1,
				event.getResults().indexOf("<", event.getResults().indexOf(">"))));

			// add match
			Button matchButton = new Button("Match files");
			matchButton.setWidth("200px");

			HorizontalPanel checkPan = new HorizontalPanel();
			final CheckBox labelBox = new CheckBox();
			final CheckBox csvBox = new CheckBox();
			labelBox.setValue(true);
			csvBox.setValue(true);
			Label boxLabel = new Label("Use labels only");
			Label csvLabel = new Label("Direct DL CSV");

			FlexTable table = new FlexTable();
			table.setWidget(0, 0, boxLabel);
			table.setWidget(1, 0, csvLabel);
			table.setWidget(0, 1, labelBox);
			table.setWidget(1, 1, csvBox);
			table.setWidget(2, 0, matchButton);
			table.setWidth("50%");
			
			checkPan.add(table);
			mainPanel.add(checkPan);
			
			table.getElement().setAttribute("align", "center");
			table.getElement().getParentElement().setAttribute("align", "center");
			table.getElement().getParentElement().getParentElement().getParentElement().getParentElement().setAttribute("align", "center");

			matchButton.addClickHandler(new ClickHandler() {

			    @Override
			    public void onClick(ClickEvent event) {

				WaitingPopup.getInstance().show();
				hide();	
				
				getMatchService().matchOntoFiles(getFirstFile(), getSecondFile(), !labelBox.getValue(), csvBox.getValue(), new AsyncCallback<String>() {

				    @Override
				    public void onFailure(Throwable caught) {
					WaitingPopup.getInstance().hide();
					Window.alert("Can't match those files.");
				    }

				    @Override
				    public void onSuccess(String result) {
					WaitingPopup.getInstance().hide();
					if (csvBox.getValue()) {
					    // get it!
					    String baseURL = GWT.getModuleBaseURL();
					    String url = baseURL + "DownloadFileServlet?filename=" + result;
					    Window.open(url, "", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=no,toolbar=true, width="
						    + Window.getClientWidth() + ",height=" + Window.getClientHeight());
					} else
					    new ResultsTextAreaPopup(result);
				    }
				});
			    }
			});
		    }
		});
	    }
	});

	this.add(mainPanel);	
    }
}
