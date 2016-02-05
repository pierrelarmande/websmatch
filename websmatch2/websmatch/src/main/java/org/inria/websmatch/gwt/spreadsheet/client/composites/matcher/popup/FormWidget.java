package org.inria.websmatch.gwt.spreadsheet.client.composites.matcher.popup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class FormWidget extends FormPanel {

    public FormWidget(String text) {	
	super();
	this.setAction(GWT.getModuleBaseURL() + "UploadFileServlet");

	this.setEncoding(FormPanel.ENCODING_MULTIPART);
	this.setMethod(FormPanel.METHOD_POST);

	HorizontalPanel panel = new HorizontalPanel();
	this.setWidget(panel);

	FileUpload upload = new FileUpload();
	upload.setName("uploadFormElement");
	panel.add(upload);
	
	Button button = new Button(text);
	button.setWidth("200px");
	button.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		submit();
	    }
	});

	panel.add(button);
    }
}
