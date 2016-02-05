package org.inria.websmatch.gwt.spreadsheet.client.handlers;

import java.util.List;
import java.util.ListIterator;

import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetViewer;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageService;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.FileUploadComposite;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SpreadsheetComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.WaitingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;

public class FileUploadCompositeSubmitCompleteHandler implements
	SubmitCompleteHandler {

    private FileUploadComposite fuc;
    private String user;
    private String filename;
    private SpreadsheetComposite compo;

    public FileUploadCompositeSubmitCompleteHandler(FileUploadComposite fuc,
	    String user) {

	this.setFuc(fuc);
	this.user = user;
	compo = null;

    }
    
    public FileUploadCompositeSubmitCompleteHandler(FileUploadComposite fuc,
	    String user, SpreadsheetComposite c) {

	this(fuc,user);
	compo = c;

    }
    
    public void onSubmitComplete(SubmitCompleteEvent event) {
	
		GWT.log(this.getClass().getName()+" File uploaded");
	
		    WaitingPopup.getInstance().show();
		// ok file arrived, now parsing it
		String name = event.getResults().substring(event.getResults().indexOf(">") + 1,
			event.getResults().indexOf("<", event.getResults().indexOf(">")));

		final String fname = name;
		//final FileUploadComposite ffuc = fuc;
		
		XMLStorageServiceAsync storeService = (XMLStorageServiceAsync) GWT.create(XMLStorageService.class);
		storeService.getDocuments(false, SpreadsheetViewer.username, new AsyncCallback<List<SchemaData>>() {
			@Override
			public void onFailure(Throwable caught) {
			    caught.printStackTrace();
			    Window.alert("Can't load schemas.");
			}

			@Override
			public void onSuccess(List<SchemaData> result) {
			    
			 // search for it and if not found go
				// parsing, if found, reloading
				ListIterator<SchemaData> its = result.listIterator();
				boolean reload = false;
				SchemaData sd = null;
				while (its.hasNext()) {

				    sd = its.next();
				    if (sd.getSource().equals(fname)) {
					reload = true;
					break;
				    }
				}
				if (reload) {
				    compo.reloadSchema(fname, sd.getId());
				} else {
				    // ok parsing service now
				    // compo.showWaitingPanel();
				    compo.parseSpreadsheet(fname,true,null);
				}
			    
			}
		    });
	    }
	
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFuc(FileUploadComposite fuc) {
	this.fuc = fuc;
    }

    public FileUploadComposite getFuc() {
	return fuc;
    }

}
