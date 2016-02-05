package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaService;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.handlers.FileUploadCompositeSubmitCompleteHandler;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.generic.SchemaTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.generic.SchemaTree;

public class FileUploadComposite extends Composite {

    private PopupPanel waitingPopup;
    private Widget ajaxImage;
    GfxRessources gfxRes = GfxRessources.INSTANCE;
    private TabLayoutPanel tabLayoutPanel;
    private SpreadsheetComposite spreadsheetComp;
    private final FormPanel uploadForm;
    private boolean showCc;
    private boolean useML;
    private String userName;
    private SchemaServiceAsync schemaService = (SchemaServiceAsync) GWT.create(SchemaService.class);

    /**
     * Generic one
     * 
     * @wbp.parser.constructor
     */

    public FileUploadComposite(String username, final MainFrame frame) {

	uploadForm = new FormPanel();
	initWidget(uploadForm);
	ajaxImage = new Image(gfxRes.loader());
	uploadForm.setSize("99,5%", "");
	uploadForm.setAction(GWT.getModuleBaseURL() + "UploadFileServlet");
	this.userName = username;

	// Because we're going to add a FileUpload widget, we'll need to set
	// the
	// form to use the POST method, and multipart MIME encoding.
	uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
	uploadForm.setMethod(FormPanel.METHOD_POST);

	// Create a panel to hold all of the form widgets.
	VerticalPanel panel = new VerticalPanel();
	panel.setStyleName("loginPanel");
	uploadForm.setWidget(panel);
	panel.setSize("100%", "100%");

	// Create a FileUpload widget.
	final FileUpload upload = new FileUpload();
	upload.setStyleName("loginPanel");
	upload.setName("uploadFormElement");
	panel.add(upload);
	panel.setCellHorizontalAlignment(upload, HasHorizontalAlignment.ALIGN_LEFT);
	panel.setCellWidth(upload, "400px");
	upload.setSize("471px", "32px");

	FlexTable flexTable = new FlexTable();
	panel.add(flexTable);

	Label lblImporterToUse = new Label("Importer to use : ");
	flexTable.setWidget(0, 0, lblImporterToUse);

	final ListBox list = new ListBox();
	list.addItem("Spreadsheet Importer (XLS)");
	list.addItem("Ontology Importer (OWL, RDF, RDFS)");
	list.addItem("Osmoze Importer (XML)");
	flexTable.setWidget(0, 1, list);

	// Add a 'submit' button.
	Button uploadSubmitButton = new Button("Submit file");
	panel.add(uploadSubmitButton);
	uploadSubmitButton.setSize("", "32px%");

	final FileUploadComposite uploadCompo = this;

	uploadSubmitButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {

		//System.out.println("Original Filename : " + upload.getFilename());

		// verifying the file type
		if (upload.getFilename() != null && !upload.getFilename().equals("")) {
		    if ((list.getSelectedIndex() == 0 && (!upload.getFilename().toLowerCase().endsWith(".xls")))
			    || (list.getSelectedIndex() == 1 && (!upload.getFilename().toLowerCase().endsWith(".owl")
				    && !upload.getFilename().toLowerCase().endsWith(".rdf") && !upload.getFilename().toLowerCase().endsWith(".rdfs")))
			    || (list.getSelectedIndex() == 2 && (!upload.getFilename().toLowerCase().endsWith(".xml")))) {
			Window.alert("Not a valid file for this importer");
		    } else {
			if (list.getSelectedIndex() == 0) {
			    uploadCompo.setUseML(true);
			    SpreadsheetComposite compo = new SpreadsheetComposite(false,upload.getFilename().replace("C:\\fakepath\\",""),true,false,null);
			    compo.setUsername(uploadCompo.getUserName());
			    uploadCompo.setTabLayoutPanel(compo.getTabLayoutPanel());
			    uploadCompo.setSpreadsheetComp(compo);
			    frame.setMainWidget(compo);
			    GWT.log(this.getClass().getName()+" Uploading file : "+upload.getFilename().replace("C:\\fakepath\\",""));
			    uploadForm.addSubmitCompleteHandler(new FileUploadCompositeSubmitCompleteHandler(uploadCompo, uploadCompo.getUserName(), compo));
			} else {
			    final String importer;
			    if (list.getSelectedIndex() == 1)
				importer = "ontology";
			    else
				importer = "osmoze";
			    final String uname = userName;

			    uploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {

				@Override
				public void onSubmitComplete(SubmitCompleteEvent event) {

				    // ok file arrived, now parsing it
				    final String name = event.getResults().substring(event.getResults().indexOf(">") + 1,
					    event.getResults().indexOf("<", event.getResults().indexOf(">")));

				    schemaService.importSchema(name, importer, uname, new AsyncCallback<ArrayList<SimpleSchemaElement>>() {

					@Override
					public void onFailure(Throwable arg0) {

					}

					@Override
					public void onSuccess(ArrayList<SimpleSchemaElement> elements) {
					   
					    SchemaTree tree = new SchemaTree();
					    tree.setRootName(name);
					    tree.setElements(elements);
					    tree.setSize("100%", "100%");
					    ScrollPanel panel = new ScrollPanel(tree);
					    panel.setSize("99%", "99%");
					    tree.setRootName(name.substring(0, name.lastIndexOf('.')));
					    frame.setMainWidget(panel);					 

					}

				    });

				}

			    });
			}
			uploadForm.submit();
			frame.closeFileImportPopup();
		    }
		}
	    }
	});
    }

    /**
     * Specific for SpreadsheetViewer... bad
     * 
     * @param tlp
     * @param showCc
     * @param useML
     */
    public FileUploadComposite(TabLayoutPanel tlp, boolean showCc, boolean useML) {

	this.showCc = showCc;
	this.useML = useML;

	uploadForm = new FormPanel();
	initWidget(uploadForm);
	tabLayoutPanel = tlp;
	ajaxImage = new Image(gfxRes.loader());
	uploadForm.setSize("99,5%", "");
	uploadForm.setAction(GWT.getModuleBaseURL() + "UploadFileServlet");

	// Because we're going to add a FileUpload widget, we'll need to set
	// the
	// form to use the POST method, and multipart MIME encoding.
	uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
	uploadForm.setMethod(FormPanel.METHOD_POST);

	// Create a panel to hold all of the form widgets.
	VerticalPanel panel = new VerticalPanel();
	panel.setStyleName("loginPanel");
	uploadForm.setWidget(panel);
	panel.setSize("100%", "100%");

	// Create a FileUpload widget.
	final FileUpload upload = new FileUpload();
	upload.setStyleName("loginPanel");
	upload.setName("uploadFormElement");
	panel.add(upload);
	panel.setCellHorizontalAlignment(upload, HasHorizontalAlignment.ALIGN_LEFT);
	panel.setCellWidth(upload, "400px");
	upload.setSize("", "32px");

	// Add a 'submit' button.
	Button uploadSubmitButton = new Button("Submit spreadsheet file");
	panel.add(uploadSubmitButton);
	uploadSubmitButton.setSize("", "32px%");

	uploadSubmitButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {

		if (!upload.getFilename().endsWith("XLS") && !upload.getFilename().endsWith("xls")) {
		    Window.alert("Can't upload this file :\nUse only xls file.");
		    return;
		}

		if (upload.getFilename() != null && !upload.getFilename().equals("")) {
		    uploadForm.submit();
		    showWaitingPanel();
		}
	    }
	});
    }

    public void showWaitingPanel() {
	waitingPopup = new PopupPanel(false);
	waitingPopup.setGlassEnabled(true);
	waitingPopup.center();

	final Grid grid = new Grid(1, 2);
	grid.setWidget(0, 0, ajaxImage);
	grid.setText(0, 1, "Loading...");
	waitingPopup.add(grid);

	waitingPopup.show();
    }

    public void hideWaitingPanel() {
	if (waitingPopup != null && waitingPopup.isShowing())
	    waitingPopup.hide();
    }

    public void setTabTitle(int page, String title) {
	((Label) tabLayoutPanel.getTabWidget(page)).setText(title);
    }
 
    public TabLayoutPanel getTabLayoutPanel() {
	return tabLayoutPanel;
    }

    public void setTabLayoutPanel(TabLayoutPanel tabLayoutPanel) {
	this.tabLayoutPanel = tabLayoutPanel;
    }

    public boolean isShowCc() {
	return showCc;
    }

    public void setShowCc(boolean showCc) {
	this.showCc = showCc;
    }

    public boolean isUseML() {
	return useML;
    }

    public void setUseML(boolean useML) {
	this.useML = useML;
    }

    public FormPanel getUploadForm() {
	return uploadForm;
    }

    public void setSpreadsheetComp(SpreadsheetComposite spreadsheetComp) {
	this.spreadsheetComp = spreadsheetComp;
    }

    public SpreadsheetComposite getSpreadsheetComp() {
	return spreadsheetComp;
    }

    public String getUserName() {
	return userName;
    }

    public void setUserName(String userName) {
	this.userName = userName;
    }

}
