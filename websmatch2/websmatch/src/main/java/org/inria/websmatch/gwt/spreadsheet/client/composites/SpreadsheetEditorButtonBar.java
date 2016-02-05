package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.HashMap;

import org.inria.websmatch.gwt.spreadsheet.client.DSPLEngineService;
import org.inria.websmatch.gwt.spreadsheet.client.DSPLEngineServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingService;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetViewer;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.ReportPopup;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.SpreadsheetEditor;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.WaitingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import org.inria.websmatch.gwt.spreadsheet.client.DSPLEngineServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.ReportPopup;

public class SpreadsheetEditorButtonBar extends Composite {

    private FlowPanel flowPanel;
    private SpreadsheetComposite compo;

    public SpreadsheetEditorButtonBar(final SpreadsheetComposite compo) {

	flowPanel = new FlowPanel();
	initWidget(flowPanel);
	flowPanel.setSize("1013px", "56px");

	FlexTable mainTable = new FlexTable();
	flowPanel.add(mainTable);
	mainTable.setSize("980px", "55px");

	CaptionPanel filePanel = new CaptionPanel("File");
	mainTable.setWidget(0, 0, filePanel);
	filePanel.setCaptionHTML("File");
	filePanel.setSize("452px", "36px");

	FlexTable fileTable = new FlexTable();
	filePanel.setContentWidget(fileTable);
	fileTable.setSize("5cm", "26px");

	Button btnSaveChanges = new Button("Save changes");
	btnSaveChanges.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		//
		WaitingPopup.getInstance().hide();
		// get sheets
		SimpleSheet[] editedSheets = new SimpleSheet[compo.getTabLayoutPanel().getWidgetCount()];

		// ok set them
		for (int i = 0; i < compo.getTabLayoutPanel().getWidgetCount(); i++) {
		    SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) compo.getTabLayoutPanel().getWidget(i)).getWidget();
		    editedSheets[i] = localTable.getSheet();
		    editedSheets[i].setStoredName(compo.getName());
		    editedSheets[i].setStoredDescription(compo.getDescription());
		}
		// create report but dont show
		ReportPopup report = new ReportPopup(SpreadsheetViewer.username, compo.getCurrentFile(), compo.getAutoSheets(), editedSheets, false, null,
			editedSheets[0].getStoredName(), editedSheets[0].getStoredDescription(), compo.isReloaded(), compo.getObjectId(), compo, compo
				.getPublication_id());

		String schemaName = new String();

		if (editedSheets[0] != null && !editedSheets[0].getStoredName().equals(""))
		    schemaName = editedSheets[0].getStoredName();
		else
		    schemaName = compo.getCurrentFile();

		report.storeSchema(compo.getAutoSheets(), editedSheets, schemaName, compo.getCurrentFile(), SpreadsheetViewer.username,
			editedSheets[0].getStoredDescription(), compo.isReloaded(), compo.getObjectId(), compo, false, false, "", false,
			compo.getPublication_id());

	    }
	});
	btnSaveChanges.setStyleName("gwt-Label-info");
	fileTable.setWidget(0, 0, btnSaveChanges);
	btnSaveChanges.setSize("110px", "23px");

	Button btnReportAndSave = new Button("Report and save");
	btnReportAndSave.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		//
		WaitingPopup.getInstance().hide();
		// get sheets
		SimpleSheet[] editedSheets = new SimpleSheet[compo.getTabLayoutPanel().getWidgetCount()];

		// ok set them
		for (int i = 0; i < compo.getTabLayoutPanel().getWidgetCount(); i++) {
		    SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) compo.getTabLayoutPanel().getWidget(i)).getWidget();
		    editedSheets[i] = localTable.getSheet();
		    editedSheets[i].setStoredName(compo.getName());
		    editedSheets[i].setStoredDescription(compo.getDescription());
		}

		// show the report and store popup
		ReportPopup report = new ReportPopup(SpreadsheetViewer.username, compo.getCurrentFile(), compo.getAutoSheets(), editedSheets, false, null,
			editedSheets[0].getStoredName(), editedSheets[0].getStoredDescription(), compo.isReloaded(), compo.getObjectId(), compo, compo
				.getPublication_id());
		report.show();
	    }
	});
	btnReportAndSave.setStyleName("gwt-Label-info");
	fileTable.setWidget(0, 1, btnReportAndSave);
	btnReportAndSave.setSize("110px", "23px");

	// discard button
	Button btnDiscardChanges = new Button("Discard changes");
	btnDiscardChanges.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		//
		WaitingPopup.getInstance().hide();
		if (compo != null && compo.getParent().getClass().equals(PopupPanel.class))
		    ((PopupPanel) compo.getParent()).hide();
	    }
	});
	btnDiscardChanges.setStyleName("gwt-Label-info");
	fileTable.setWidget(0, 2, btnDiscardChanges);
	btnDiscardChanges.setSize("110px", "23px");
	//

	// trash button
	Button btnTrashButton = new Button("Trash document");
	btnTrashButton.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		//
		WaitingPopup.getInstance().hide();
		// get sheets
		SimpleSheet[] editedSheets = new SimpleSheet[compo.getTabLayoutPanel().getWidgetCount()];

		// ok set them
		for (int i = 0; i < compo.getTabLayoutPanel().getWidgetCount(); i++) {
		    SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) compo.getTabLayoutPanel().getWidget(i)).getWidget();
		    editedSheets[i] = localTable.getSheet();
		    editedSheets[i].setStoredName(compo.getName());
		    editedSheets[i].setStoredDescription(compo.getDescription());
		}
		// create report but dont show
		ReportPopup report = new ReportPopup(SpreadsheetViewer.username, compo.getCurrentFile(), compo.getAutoSheets(), editedSheets, false, null,
			editedSheets[0].getStoredName(), editedSheets[0].getStoredDescription(), compo.isReloaded(), compo.getObjectId(), compo, compo
				.getPublication_id());

		String schemaName = new String();

		if (editedSheets[0] != null && !editedSheets[0].getStoredName().equals(""))
		    schemaName = editedSheets[0].getStoredName();
		else
		    schemaName = compo.getCurrentFile();

		report.storeSchema(compo.getAutoSheets(), editedSheets, schemaName, compo.getCurrentFile(), compo.getUsername(),
			editedSheets[0].getStoredDescription(), compo.isReloaded(), compo.getObjectId(), compo, false, false, "", true,
			compo.getPublication_id());

	    }
	});
	btnTrashButton.setStyleName("gwt-Label-info");
	fileTable.setWidget(0, 3, btnTrashButton);
	btnTrashButton.setSize("110px", "23px");
	fileTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
	fileTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
	fileTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
	fileTable.getCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);

	CaptionPanel exportPanel = new CaptionPanel("Export");
	exportPanel.setCaptionHTML("Export");
	mainTable.setWidget(0, 1, exportPanel);
	exportPanel.setSize("500px", "36px");

	FlexTable exportTable = new FlexTable();
	exportPanel.setContentWidget(exportTable);
	exportTable.setSize("498px", "26px");

	// get XML with data
	Button btnGetXml = new Button("Get XML");
	btnGetXml.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		HashMap<String, String> metas = compo.getMetas();

		/*
		 * if (dataPublica == null || dataPublica.size() == 0) {
		 * dataPublica = new HashMap<String, String>();
		 * dataPublica.put("user_id", compo.getUsername());
		 * dataPublica.put("doc_url", compo.getCurrentFile()); }
		 */

		metas.put("user_id", compo.getUsername());
		metas.put("doc_url", compo.getCurrentFile());
		if (compo.getPublication_id() != null && !compo.getPublication_id().equals(""))
		    metas.put("publication_id", compo.getPublication_id());

		SpreadsheetParsingServiceAsync parsingService = (SpreadsheetParsingServiceAsync) GWT.create(SpreadsheetParsingService.class);
		parsingService.createXMLFile(metas, compo.getEditedSheets(), true, new AsyncCallback<String>() {

		    @Override
		    public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			Window.alert("Can't create XML file");
		    }

		    @Override
		    public void onSuccess(String path) {
			GWT.log("Download file : " + path);
			// get it!
			String baseURL = GWT.getModuleBaseURL();
			String url = baseURL + "DownloadFileServlet?filename=" + path;
			Window.open(url, "", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=no,toolbar=true, width=" + Window.getClientWidth()
				+ ",height=" + Window.getClientHeight());
		    }
		});
	    }
	});
	btnGetXml.setStyleName("gwt-Label-info");
	exportTable.setWidget(0, 0, btnGetXml);
	btnGetXml.setHeight("23px");
	//

	// get XML without data
	Button btnGetXmlWo = new Button("Get XML w/o data");
	btnGetXmlWo.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		HashMap<String, String> metas = compo.getMetas();

		/*
		 * if (dataPublica == null || dataPublica.size() == 0) {
		 * dataPublica = new HashMap<String, String>();
		 * dataPublica.put("user_id", compo.getUsername());
		 * dataPublica.put("doc_url", compo.getCurrentFile()); }
		 */

		metas.put("user_id", compo.getUsername());
		metas.put("doc_url", compo.getCurrentFile());
		if (compo.getPublication_id() != null && !compo.getPublication_id().equals(""))
		    metas.put("publication_id", compo.getPublication_id());

		SpreadsheetParsingServiceAsync parsingService = (SpreadsheetParsingServiceAsync) GWT.create(SpreadsheetParsingService.class);
		parsingService.createXMLFile(metas, compo.getEditedSheets(), false, new AsyncCallback<String>() {

		    @Override
		    public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			Window.alert("Can't create XML file");
		    }

		    @Override
		    public void onSuccess(String path) {
			// get it!
			String baseURL = GWT.getModuleBaseURL();
			String url = baseURL + "DownloadFileServlet?filename=" + path;
			Window.open(url, "", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=no,toolbar=true, width=" + Window.getClientWidth()
				+ ",height=" + Window.getClientHeight());
		    }
		});
	    }
	});
	btnGetXmlWo.setStyleName("gwt-Label-info");
	exportTable.setWidget(0, 1, btnGetXmlWo);
	btnGetXmlWo.setHeight("23px");
	exportTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
	exportTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
	//

	// Get DSPL zip
	Button btnGetDspl = new Button("Get DSPL");
	btnGetDspl.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		// open a popup for name and description of the dataset
		final PopupPanel popup = new PopupPanel(false);
		popup.setGlassEnabled(true);
		popup.setAutoHideEnabled(true);
		popup.center();

		final TextBox nameBox = new TextBox();
		nameBox.setWidth("200px");

		nameBox.setText(compo.getName());

		final TextArea descArea = new TextArea();
		descArea.setWidth("200px");

		descArea.setText(compo.getDescription());

		Label nameLabel = new Label("DataSet Name : ");
		Label descLabel = new Label("DataSet Desc : ");

		Button okButton = new Button("Create data set");
		okButton.setWidth("200px");

		okButton.addClickHandler(new ClickHandler() {

		    @Override
		    public void onClick(ClickEvent event) {

			final String dataSetName = nameBox.getText();
			final String dataSetDescription = descArea.getText();

			popup.hide();

			// get sheets
			final SimpleSheet[] editedSheets = compo.getEditedSheets();

			HashMap<String, String> metas = compo.getMetas();

			/*
			 * if (dataPublica == null || dataPublica.size() == 0) {
			 * dataPublica = new HashMap<String, String>();
			 * dataPublica.put("user_id", compo.getUsername());
			 * dataPublica.put("doc_url", compo.getCurrentFile()); }
			 */

			metas.put("user_id", compo.getUsername());
			metas.put("doc_url", compo.getCurrentFile());
			if (compo.getPublication_id() != null && !compo.getPublication_id().equals(""))
			    metas.put("publication_id", compo.getPublication_id());

			// get the zip
			final SpreadsheetParsingServiceAsync parsingService = (SpreadsheetParsingServiceAsync) GWT.create(SpreadsheetParsingService.class);
			parsingService.createXMLFile(metas, editedSheets, true, new AsyncCallback<String>() {

			    @Override
			    public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				Window.alert("Can't create XML file");
			    }

			    @Override
			    public void onSuccess(String path) {

				String fileToConvert = path.substring(path.lastIndexOf('/') + 1);

				parsingService.createDSPLFile(fileToConvert, editedSheets, dataSetName, dataSetDescription, new AsyncCallback<String>() {

				    @Override
				    public void onFailure(Throwable caught) {
					// TODO Auto-generated method stub
					Window.alert("Can't create DSPL zip file");
				    }

				    @Override
				    public void onSuccess(String zipPath) {
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
		    }
		});

		Grid grid = new Grid(3, 2);
		grid.setWidget(0, 0, nameLabel);
		grid.setWidget(0, 1, nameBox);
		grid.setWidget(1, 0, descLabel);
		grid.setWidget(1, 1, descArea);
		grid.setWidget(2, 1, okButton);

		popup.add(grid);

		popup.show();
		//
	    }
	});
	btnGetDspl.setStyleName("gwt-Label-info");
	exportTable.setWidget(0, 2, btnGetDspl);
	btnGetDspl.setHeight("23px");
	exportTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
	//

	// Visualize directly the data
	Button btnVisualize = new Button("Visualize");

	// FIXME if/else useless
	if (true/*
		 * SpreadsheetViewer.username.equals("datapublica") ||
		 * SpreadsheetViewer.username.equals("test")
		 */) {
	    btnVisualize.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
		    // add a warning if not simple xls file
		    boolean warn = false;
		    int ccCount = 0;
		    for (int i = 0; i < compo.getEditedSheets().length; i++) {
			if (compo.getEditedSheets()[i] != null && compo.getEditedSheets()[i].getConnexComps() != null)
			    ccCount += compo.getEditedSheets()[i].getConnexComps().size();
		    }
		    if (ccCount > 1)
			warn = true;
		    if (warn)
			Window.alert("Warning : this document may not be easyly converted into DSPLEngine script.");
		    //

		    // open a popup for name and description of the dataset
		    final PopupPanel popup = new PopupPanel(false);
		    popup.setGlassEnabled(true);
		    popup.setAutoHideEnabled(true);
		    popup.setPopupPosition(event.getClientX() - 50, event.getClientY() + 50);
		    // popup.center();

		    final TextBox nameBox = new TextBox();
		    nameBox.setWidth("200px");

		    if (compo.getName() != null && compo.getName().toLowerCase().endsWith(".xls"))
			nameBox.setText(com.google.gwt.http.client.URL.encodeQueryString(compo.getName().substring(0, compo.getName().length() - 4)));
		    else
			nameBox.setText(com.google.gwt.http.client.URL.encodeQueryString(compo.getName()));

		    final TextArea descArea = new TextArea();
		    descArea.setWidth("200px");

		    descArea.setText(compo.getDescription());

		    Label refLabel = new Label("DataSet Ref : ");

		    Label nameLabel = new Label("DataSet Name : ");
		    Label descLabel = new Label("DataSet Desc : ");

		    Label editorNameLabel = new Label("Editor Name : ");
		    Label editorDescLabel = new Label("Editor Desc : ");

		    final TextBox refBox = new TextBox();
		    refBox.setWidth("200px");
		    GWT.log("Compo getName : " + compo.getName());
		    // refBox.setText(com.google.gwt.http.client.URL.encodeQueryString(compo.getName()));
		    if (compo.getName() != null && compo.getName().toLowerCase().endsWith(".xls"))
			refBox.setText(com.google.gwt.http.client.URL.encodeQueryString(compo.getName().substring(0, compo.getName().length() - 4)));
		    else
			refBox.setText(com.google.gwt.http.client.URL.encodeQueryString(compo.getName()));

		    final TextBox editorNameBox = new TextBox();
		    editorNameBox.setWidth("200px");
		    final TextArea editorDescArea = new TextArea();
		    editorDescArea.setWidth("200px");

		    // TODO fix this
		    boolean needValue = false;
		    final TextBox valueBox = new TextBox();
		    Label valueLabel = new Label("Value name : ");
		    valueLabel.addStyleName("bold");
		    valueBox.setWidth("200px");
		    if (compo.getEditedSheets() != null && compo.getEditedSheets()[0].getConnexComps().get(0).isBiDimensionnalArray()) {
			needValue = true;
		    }
		    //

		    Button okButton = new Button("Create data set");
		    okButton.setWidth("200px");

		    final boolean fneedValue = needValue;

		    okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

			    // required field
			    if (fneedValue) {
				if (valueBox.getText() == null || valueBox.getText().equals("")) {
				    Window.alert("Please choose a name for the value.");
				    return;
				}
			    }

			    WaitingPopup.getInstance().show();

			    final String ref = refBox.getText();

			    final String dataSetName = nameBox.getText();
			    final String dataSetDescription = descArea.getText();

			    final String dataSetEditorName = nameBox.getText();
			    final String dataSetEditorDescription = descArea.getText();

			    popup.hide();

			    HashMap<String, String> metas = compo.getMetas();

			    /*
			     * if (dataPublica == null || dataPublica.size() ==
			     * 0) { dataPublica = new HashMap<String, String>();
			     * dataPublica.put("user_id", compo.getUsername());
			     * dataPublica.put("doc_url",
			     * compo.getCurrentFile()); }
			     */

			    metas.put("user_id", compo.getUsername());
			    metas.put("doc_url", compo.getCurrentFile());
			    if (compo.getPublication_id() != null && !compo.getPublication_id().equals(""))
				metas.put("publication_id", compo.getPublication_id());

			    // get the zip
			    final SpreadsheetParsingServiceAsync parsingService = (SpreadsheetParsingServiceAsync) GWT.create(SpreadsheetParsingService.class);
			    DSPLEngineServiceAsync dsplEngineService = (DSPLEngineServiceAsync) GWT.create(DSPLEngineService.class);
			    dsplEngineService.createXMLFile(metas, compo.getEditedSheets(), ref, dataSetName, dataSetDescription, dataSetEditorName,
				    dataSetEditorDescription, valueBox.getText(), true, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
					    WaitingPopup.getInstance().hide();
					    // Window.alert(caught.getMessage());
					    // TODO Auto-generated method stub
					    Window.alert("Can't create XML file");
					}

					@Override
					public void onSuccess(String path) {
					    // get it!
					    /*
					     * String baseURL =
					     * GWT.getModuleBaseURL(); String
					     * url = baseURL +
					     * "DownloadFileServlet?filename=" +
					     * path; Window.open(url, "",
					     * "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=no,toolbar=true, width="
					     * + Window.getClientWidth() +
					     * ",height=" +
					     * Window.getClientHeight());
					     */

					    parsingService.visualizeSpreadsheet(path, ref, new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
						    WaitingPopup.getInstance().hide();
						    Window.alert("Can't visualize datas");
						}

						@Override
						public void onSuccess(Void result) {
						    WaitingPopup.getInstance().hide();
						    // show it!
						    String baseUrl = SpreadsheetViewer.providerUri;
						    // String baseUrl =
						    // "http://constraint.lirmm.fr:8320";
						    // String baseUrl =
						    // "http://otmedia.lirmm.fr:8320";
						    // String baseUrl =
						    // "http://admin-int.data-publica.com/api";

						    Window.open(baseUrl + "/visualizator.html?publicationReference=" + ref/*
															   * fileToConvert
															   * .
															   * substring
															   * (
															   * 0
															   * ,
															   * fileToConvert
															   * .
															   * lastIndexOf
															   * (
															   * "."
															   * )
															   * )
															   *//*
															      * +
															      * "&apiBaseURL=api/"
															      */, "_blank", null);
						}
					    });
					}
				    });
			}
		    });

		    Grid grid;
		    if (needValue)
			grid = new Grid(7, 2);
		    else
			grid = new Grid(6, 2);

		    grid.setWidget(0, 0, refLabel);
		    grid.setWidget(0, 1, refBox);
		    grid.setWidget(1, 0, nameLabel);
		    grid.setWidget(1, 1, nameBox);
		    grid.setWidget(2, 0, descLabel);
		    grid.setWidget(2, 1, descArea);
		    grid.setWidget(3, 0, editorNameLabel);
		    grid.setWidget(3, 1, editorNameBox);
		    grid.setWidget(4, 0, editorDescLabel);
		    grid.setWidget(4, 1, editorDescArea);

		    if (needValue) {
			grid.setWidget(5, 0, valueLabel);
			grid.setWidget(5, 1, valueBox);
			grid.setWidget(6, 1, okButton);
		    }

		    else
			grid.setWidget(5, 1, okButton);

		    popup.add(grid);

		    popup.show();
		}
	    });
	}

	/*else {
	    btnVisualize.addClickHandler(new ClickHandler() {

		public void onClick(ClickEvent event) {
		    // open a popup for name and description of the dataset
		    final PopupPanel popup = new PopupPanel(false);
		    popup.setGlassEnabled(true);
		    popup.setAutoHideEnabled(true);
		    popup.center();

		    final TextBox nameBox = new TextBox();
		    nameBox.setWidth("200px");

		    nameBox.setText(compo.getName());

		    final TextArea descArea = new TextArea();
		    descArea.setWidth("200px");

		    descArea.setText(compo.getDescription());

		    Label nameLabel = new Label("DataSet Name : ");
		    Label descLabel = new Label("DataSet Desc : ");

		    Button okButton = new Button("Create data set");
		    okButton.setWidth("200px");

		    okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

			    final String dataSetName = nameBox.getText();
			    final String dataSetDescription = descArea.getText();

			    popup.hide();

			    // get sheets
			    final SimpleSheet[] editedSheets = compo.getEditedSheets();

			    HashMap<String, String> metas = compo.getMetas();

			    
			     * if (dataPublica == null || dataPublica.size() ==
			     * 0) { dataPublica = new HashMap<String, String>();
			     * dataPublica.put("user_id", compo.getUsername());
			     * dataPublica.put("doc_url",
			     * compo.getCurrentFile()); }
			     

			    metas.put("user_id", compo.getUsername());
			    metas.put("doc_url", compo.getCurrentFile());
			    if (compo.getPublication_id() != null && !compo.getPublication_id().equals(""))
				metas.put("publication_id", compo.getPublication_id());

			    // get the zip
			    final SpreadsheetParsingServiceAsync parsingService = (SpreadsheetParsingServiceAsync) GWT.create(SpreadsheetParsingService.class);
			    parsingService.createXMLFile(metas, editedSheets, true, new AsyncCallback<String>() {

				@Override
				public void onFailure(Throwable caught) {
				    // TODO Auto-generated method stub
				    Window.alert("Can't create XML file");
				}

				@Override
				public void onSuccess(String path) {

				    final String fileToConvert = path.substring(path.lastIndexOf('/') + 1);

				    parsingService.createDSPLFile(fileToConvert, editedSheets, dataSetName, dataSetDescription, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
					    // TODO Auto-generated method stub
					    Window.alert("Can't create DSPL zip file");
					}

					@Override
					public void onSuccess(String zipPath) {

					    parsingService.visualizeSpreadsheet(zipPath, fileToConvert.substring(0, fileToConvert.lastIndexOf(".")),
						    new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
							    Window.alert("Can't visualize datas");
							}

							@Override
							public void onSuccess(Void result) {
							    // show it!
							    String baseUrl = SpreadsheetViewer.providerUri;
							    // String baseUrl =
							    // "http://constraint.lirmm.fr:8320";
							    // String baseUrl =
							    // "http://otmedia:8320";
							    // String baseUrl =
							    // "http://admin-int.data-publica.com/api";

							    Window.open(baseUrl + "/visualizator.html?publicationReference=" + dataSetName
																	   * fileToConvert
																	   * .
																	   * substring
																	   * (
																	   * 0
																	   * ,
																	   * fileToConvert
																	   * .
																	   * lastIndexOf
																	   * (
																	   * "."
																	   * )
																	   * )
																	   
																	      * +
																	      * "&apiBaseURL=api/"
																	      , "_blank",
								    null);
							}
						    });
					}
				    });
				}
			    });
			}
		    });

		    Grid grid = new Grid(3, 2);
		    grid.setWidget(0, 0, nameLabel);
		    grid.setWidget(0, 1, nameBox);
		    grid.setWidget(1, 0, descLabel);
		    grid.setWidget(1, 1, descArea);
		    grid.setWidget(2, 1, okButton);

		    popup.add(grid);

		    popup.show();
		    //
		}
	    });
	}*/
	btnVisualize.setStyleName("gwt-Label-info");
	exportTable.setWidget(0, 3, btnVisualize);
	btnVisualize.setHeight("23px");
	exportTable.getCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
	mainTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
	//

	// Export in XML format for DSPL engine
	// FIXME problem on account test
	if (SpreadsheetViewer.username.equals("datapublica") /*
							      * ||
							      * SpreadsheetViewer
							      * .
							      * username.equals(
							      * "test")
							      */) {
	    Button btnDSPLEngine = new Button("Get DSPL Engine XML");
	    btnDSPLEngine.addClickHandler(new ClickHandler() {

		public void onClick(ClickEvent event) {

		    // add a warning if not simple xls file
		    boolean warn = false;
		    int ccCount = 0;
		    for (int i = 0; i < compo.getEditedSheets().length; i++) {
			if (compo.getEditedSheets()[i] != null && compo.getEditedSheets()[i].getConnexComps() != null)
			    ccCount += compo.getEditedSheets()[i].getConnexComps().size();
		    }
		    if (ccCount > 1)
			warn = true;
		    if (warn)
			Window.alert("Warning : this document may not be easyly converted into DSPLEngine script.");
		    //

		    // open a popup for name and description of the dataset
		    final PopupPanel popup = new PopupPanel(false);
		    popup.setGlassEnabled(true);
		    popup.setAutoHideEnabled(true);
		    popup.setPopupPosition(event.getClientX() - 50, event.getClientY() + 50);
		    // popup.center();

		    final TextBox nameBox = new TextBox();
		    nameBox.setWidth("200px");

		    nameBox.setText(compo.getName());

		    final TextArea descArea = new TextArea();
		    descArea.setWidth("200px");

		    descArea.setText(compo.getDescription());

		    Label refLabel = new Label("DataSet Ref : ");

		    Label nameLabel = new Label("DataSet Name : ");
		    Label descLabel = new Label("DataSet Desc : ");

		    Label editorNameLabel = new Label("Editor Name : ");
		    Label editorDescLabel = new Label("Editor Desc : ");

		    final TextBox refBox = new TextBox();
		    refBox.setWidth("200px");
		    GWT.log("Compo getName : " + compo.getName());
		    refBox.setText(com.google.gwt.http.client.URL.encodeQueryString(compo.getName()));

		    final TextBox editorNameBox = new TextBox();
		    editorNameBox.setWidth("200px");
		    final TextArea editorDescArea = new TextArea();
		    editorDescArea.setWidth("200px");

		    // TODO fix this
		    boolean needValue = false;
		    final TextBox valueBox = new TextBox();
		    Label valueLabel = new Label("Value name : ");
		    valueLabel.addStyleName("bold");
		    valueBox.setWidth("200px");
		    if (compo.getEditedSheets() != null && compo.getEditedSheets()[0].getConnexComps().get(0).isBiDimensionnalArray()) {
			needValue = true;
		    }
		    //

		    Button okButton = new Button("Create data set");
		    okButton.setWidth("200px");

		    final boolean fneedValue = needValue;

		    okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

			    // required field
			    if (fneedValue) {
				if (valueBox.getText() == null || valueBox.getText().equals("")) {
				    Window.alert("Please choose a name for the value.");
				    return;
				}
			    }

			    final String ref = refBox.getText();

			    final String dataSetName = nameBox.getText();
			    final String dataSetDescription = descArea.getText();

			    final String dataSetEditorName = nameBox.getText();
			    final String dataSetEditorDescription = descArea.getText();

			    popup.hide();

			    HashMap<String, String> metas = compo.getMetas();

			    /*
			     * if (dataPublica == null || dataPublica.size() ==
			     * 0) { dataPublica = new HashMap<String, String>();
			     * dataPublica.put("user_id", compo.getUsername());
			     * dataPublica.put("doc_url",
			     * compo.getCurrentFile()); }
			     */

			    metas.put("user_id", compo.getUsername());
			    metas.put("doc_url", compo.getCurrentFile());
			    if (compo.getPublication_id() != null && !compo.getPublication_id().equals(""))
				metas.put("publication_id", compo.getPublication_id());

			    DSPLEngineServiceAsync dsplEngineService = (DSPLEngineServiceAsync) GWT.create(DSPLEngineService.class);
			    dsplEngineService.createXMLFile(metas, compo.getEditedSheets(), ref, dataSetName, dataSetDescription, dataSetEditorName,
				    dataSetEditorDescription, valueBox.getText(), false, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
					    // Window.alert(caught.getMessage());
					    // TODO Auto-generated method stub
					    Window.alert("Can't create XML file");
					}

					@Override
					public void onSuccess(String path) {
					    // get it!
					    String baseURL = GWT.getModuleBaseURL();
					    String url = baseURL + "DownloadFileServlet?filename=" + path;
					    Window.open(url, "", "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=no,toolbar=true, width="
						    + Window.getClientWidth() + ",height=" + Window.getClientHeight());
					}
				    });
			}
		    });

		    Grid grid;
		    if (needValue)
			grid = new Grid(7, 2);
		    else
			grid = new Grid(6, 2);

		    grid.setWidget(0, 0, refLabel);
		    grid.setWidget(0, 1, refBox);
		    grid.setWidget(1, 0, nameLabel);
		    grid.setWidget(1, 1, nameBox);
		    grid.setWidget(2, 0, descLabel);
		    grid.setWidget(2, 1, descArea);
		    grid.setWidget(3, 0, editorNameLabel);
		    grid.setWidget(3, 1, editorNameBox);
		    grid.setWidget(4, 0, editorDescLabel);
		    grid.setWidget(4, 1, editorDescArea);

		    if (needValue) {
			grid.setWidget(5, 0, valueLabel);
			grid.setWidget(5, 1, valueBox);
			grid.setWidget(6, 1, okButton);
		    }

		    else
			grid.setWidget(5, 1, okButton);

		    popup.add(grid);

		    popup.show();
		}
	    });
	    btnDSPLEngine.setStyleName("gwt-Label-info");
	    exportTable.setWidget(0, 4, btnDSPLEngine);
	    btnDSPLEngine.setHeight("23px");
	    exportTable.getCellFormatter().setHorizontalAlignment(0, 4, HasHorizontalAlignment.ALIGN_CENTER);
	    mainTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
	}

	this.setCompo(compo);
    }

    public void setCompo(SpreadsheetComposite compo) {
	this.compo = compo;
    }

    public SpreadsheetComposite getCompo() {
	return compo;
    }
}
