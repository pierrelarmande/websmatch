package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingService;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetViewer;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageService;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.exceptions.ParserException;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.DetectionQualityCellTable;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.ReloadPopup;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.ReportPopup;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.SchemaCellTable;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.SpreadsheetEditor;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.StoreSchemaWidget;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.WaitingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;

public class SpreadsheetComposite extends Composite {

    // the services RPC
    private SpreadsheetParsingServiceAsync parsingService = (SpreadsheetParsingServiceAsync) GWT.create(SpreadsheetParsingService.class);

    // private SpreadsheetTable table;
    private SpreadsheetEditor table;

    // private TextArea textArea;

    // private ScrolledTabLayoutPanel tabLayoutPanel;
    private TabLayoutPanel tabLayoutPanel;
    // GfxRessources gfxRes = GfxRessources.INSTANCE;

    // private Image ajaxImage;

    private FlowPanel flowPanel;
    private boolean showCC = false;

    private PopupPanel popup = null;
    // private PopupPanel waitingPopup = null;

    private String username = null;

    private SchemaCellTreeComposite tree;

    // for datapublica
    private HashMap<String, String> metas = null;

    private boolean _DEBUG = false;
    // private boolean _DATAPUBLICA_TEST = false;

    private int borderToRemove = 132;

    private String currentFile = null;

    // store auto simple sheets
    private SimpleSheet[] autoSheets = null;

    // boolean for reload
    private boolean reloaded = false;
    private String objectId = "";
    private String publication_id = "";

    private String name = "";
    private String description = "";

    // quality table to update on save
    private DetectionQualityCellTable qualityTable;

    public SpreadsheetComposite(boolean datapublica, final String curFile, boolean showLog, boolean fromQualityList, DetectionQualityCellTable qualityTable) {

	// FIXME remove qualityList parameter
	fromQualityList = true;
	/* if (datapublica) */
	metas = new HashMap<String, String>();
	if (SpreadsheetViewer.username != null && !SpreadsheetViewer.username.equals(""))
	    username = SpreadsheetViewer.username;

	final SpreadsheetComposite fcomp = this;

	this.setQualityTable(qualityTable);

	currentFile = curFile;
	flowPanel = new FlowPanel();
	initWidget(flowPanel);
	flowPanel.setStyleName("centerFp");
	flowPanel.setSize("100%", "100%");
	Window.addResizeHandler(new ResizeHandler() {

	    public void onResize(ResizeEvent event) {
		int height = event.getHeight() - borderToRemove;
		flowPanel.setHeight(height + "px");
		flowPanel.setWidth("100%");
	    }
	});

	// Test fileUpload
	// Create a FormPanel and point it at a service.
	final FormPanel uploadForm = new FormPanel();
	uploadForm.setSize("99,5%", "");
	uploadForm.setAction(GWT.getModuleBaseURL() + "UploadFileServlet");

	// Because we're going to add a FileUpload widget, we'll need to set the
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
	uploadSubmitButton.setSize("", "32px");

	uploadSubmitButton.addClickHandler(new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {

		if (_DEBUG)
		    System.out.println("Original Filename : " + upload.getFilename());

		if (!upload.getFilename().endsWith("XLS") && !upload.getFilename().endsWith("xls")) {
		    Window.alert("Can't upload this file :\nUse only xls file.");
		    return;
		}

		if (upload.getFilename() != null && !upload.getFilename().equals("")) {
		    uploadForm.submit();
		    WaitingPopup.getInstance().show();
		}
	    }
	});

	uploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {
	    public void onSubmitComplete(SubmitCompleteEvent event) {

		// ok file arrived, now parsing it
		String name = event.getResults().substring(event.getResults().indexOf(">") + 1,
			event.getResults().indexOf("<", event.getResults().indexOf(">")));

		final String fname = name;

		currentFile = name;

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
			    reloadSchema(fname, sd.getId());
			} else {
			    // ok parsing service now
			    WaitingPopup.getInstance().show();
			    parseSpreadsheet(fname, true, null);
			}
		    }
		});
	    }
	});

	/*
	 * textArea = new TextArea(); textArea.setVisibleLines(6);
	 * textArea.setTitle("Output console");
	 * textArea.setAlignment(TextAlignment.LEFT); if (showLog)
	 * flowPanel.add(textArea); textArea.setSize("99.2%", "15%");
	 * textArea.setReadOnly(true);
	 */

	Button btnShowStoredSchemas = new Button("Show stored XLS schemas");
	btnShowStoredSchemas.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		showSchemas();

	    }
	});

	CheckBox showCCBox = new CheckBox("Show CC");
	showCCBox.setHeight("32px");
	showCCBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
	    public void onValueChange(ValueChangeEvent<Boolean> event) {
		// add code here to change SpreadSheetTable
		// compo
		showCC = event.getValue();
	    }
	});
	showCCBox.setHTML("Show CC");
	showCCBox.setTitle("By enabling this, you will see the CC (NB: not applied on the current loaded spreadsheet)");

	Button btnStoreSchema = new Button("Store XLS schema");
	btnStoreSchema.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		// get sheets
		SimpleSheet[] editedSheets = new SimpleSheet[tabLayoutPanel.getWidgetCount()];

		// ok set them
		for (int i = 0; i < tabLayoutPanel.getWidgetCount(); i++) {
		    SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(i)).getWidget();
		    editedSheets[i] = localTable.getSheet();
		}

		// show the report and store popup
		ReportPopup report = new ReportPopup(username, currentFile, autoSheets, editedSheets, false, null, editedSheets[0].getStoredName(),
			editedSheets[0].getStoredDescription(), isReloaded(), getObjectId(), fcomp, getPublication_id());
		report.show();
	    }
	});

	if (fromQualityList)
	    flowPanel.add(new SpreadsheetEditorButtonBar(this));

	SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel();
	flowPanel.add(splitLayoutPanel);
	splitLayoutPanel.setSize("100%", "94%");

	tabLayoutPanel = new TabLayoutPanel(2.5, Unit.EM);

	tree = new SchemaCellTreeComposite(tabLayoutPanel);
	splitLayoutPanel.addWest(tree, 180.0);

	splitLayoutPanel.add(tabLayoutPanel);
	tabLayoutPanel.setSize("100%", "100%");

	// Datas
	table = new SpreadsheetEditor();
	table.setStyleName("loginPanel");
	table.setTitle("Spreadsheet visualization");
	table.setSize("97%", "97%");

	table.setVisible(false);

	ScrollPanel listPanel = new ScrollPanel(table);

	tabLayoutPanel.add(listPanel, "Spreadsheet", true);
	listPanel.setHorizontalScrollPosition(1);

	// add the dynamic handler on tab selection
	tabLayoutPanel.addSelectionHandler(new SelectionHandler<Integer>() {

	    @Override
	    public void onSelection(final SelectionEvent<Integer> event) {
		// ok see if it alreaydy loaded
		SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(event.getSelectedItem().intValue())).getWidget();
		if (event.getSelectedItem().intValue() > 0
			&& (localTable.getColumnCount() == 0 || localTable.getCells() == null || localTable.getCells().length == 0)) {

		    WaitingPopup.getInstance().show();

		    parsingService.getSheet(username, event.getSelectedItem().intValue(), new AsyncCallback<SimpleSheet>() {

			@Override
			public void onFailure(Throwable caught) {
			    GWT.log(this.getClass().getName() + " " + caught.getMessage());
			    WaitingPopup.getInstance().hide();
			}

			@Override
			public void onSuccess(SimpleSheet result) {
			    GWT.log(this.getClass().getName() + " getSheet results");

			    SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(event.getSelectedItem().intValue()))
				    .getWidget();
			    localTable.updateSheet(result, showCC, false);
			    WaitingPopup.getInstance().hide();
			}
		    });
		}
	    }
	});

	LayoutPanel layoutPanel = new LayoutPanel();
	layoutPanel.setStyleName("loginPanel");

	if (!fromQualityList)
	    flowPanel.add(layoutPanel);

	layoutPanel.setSize("99.5%", "36px");

	FlexTable flexTable = new FlexTable();
	flexTable.setSize("95%", "36px");

	layoutPanel.add(flexTable);

	// button to get DSPL files
	Button btnGetDSPL = new Button("Download DSPL files");

	btnGetDSPL.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		// open a popup for name and description of the dataset
		final PopupPanel popup = new PopupPanel(false);
		popup.setGlassEnabled(true);
		popup.setAutoHideEnabled(true);
		popup.center();

		final TextBox nameBox = new TextBox();
		nameBox.setWidth("200px");
		final TextArea descArea = new TextArea();
		descArea.setWidth("200px");

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
			final SimpleSheet[] editedSheets = getEditedSheets();

			if (metas == null || metas.size() == 0) {
			    metas = new HashMap<String, String>();
			    metas.put("user_id", username);
			    metas.put("doc_url", curFile);
			}

			// get the zip
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

	flexTable.setWidget(0, 2, btnGetDSPL);

	// button to get XML
	Button btnGetXML = new Button("Download XML description");

	btnGetXML.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		if (metas == null || metas.size() == 0) {
		    metas = new HashMap<String, String>();
		    metas.put("user_id", username);
		    metas.put("doc_url", curFile);
		}

		parsingService.createXMLFile(metas, getEditedSheets(), false, new AsyncCallback<String>() {

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

	flexTable.setWidget(0, 3, btnGetXML);

	// with data
	// button to get XML
	Button btnGetXMLandDatas = new Button("Download XML desc. and datas");

	btnGetXMLandDatas.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		if (metas == null || metas.size() == 0) {
		    metas = new HashMap<String, String>();
		    metas.put("user_id", username);
		    metas.put("doc_url", curFile);
		}

		parsingService.createXMLFile(metas, getEditedSheets(), true, new AsyncCallback<String>() {

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

	flexTable.setWidget(0, 4, btnGetXMLandDatas);
	//

	// with data
	// button to get XML
	Button btnVizualise = new Button("Visualize");

	btnVizualise.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {

		// open a popup for name and description of the dataset
		final PopupPanel popup = new PopupPanel(false);
		popup.setGlassEnabled(true);
		popup.setAutoHideEnabled(true);
		popup.center();

		final TextBox nameBox = new TextBox();
		nameBox.setWidth("200px");
		final TextArea descArea = new TextArea();
		descArea.setWidth("200px");

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
			final SimpleSheet[] editedSheets = getEditedSheets();

			if (metas == null || metas.size() == 0) {
			    metas = new HashMap<String, String>();
			    metas.put("user_id", username);
			    metas.put("doc_url", curFile);
			}

			// get the zip
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

					parsingService.visualizeSpreadsheet(zipPath, dataSetName, new AsyncCallback<Void>() {

					    @Override
					    public void onFailure(Throwable caught) {

						Window.alert("Can't visualize datas");

					    }

					    @Override
					    public void onSuccess(Void result) {
						// show it!
						Window.open("http://admin-int.data-publica.com/api/visualizator.html?publicationReference=" + dataSetName /*
																			   * +
																			   * "&apiBaseURL=api/"
																			   */,
							"", "");
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

	flexTable.setWidget(0, 5, btnVizualise);
	//

	if (datapublica) {

	    // publish document
	    Button btnPubliOnDP = new Button("Publish on Data Publica");

	    btnPubliOnDP.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {

		    // get sheets
		    final SimpleSheet[] editedSheets = getEditedSheets();

		    // show the report and store popup
		    ReportPopup report = new ReportPopup(username, currentFile, autoSheets, editedSheets, true, metas, editedSheets[0].getStoredName(),
			    editedSheets[0].getStoredDescription(), isReloaded(), getObjectId(), fcomp, getPublication_id());
		    report.show();
		}
	    });

	    flexTable.setWidget(0, 0, btnPubliOnDP);

	}

	else {
	    flexTable.setWidget(0, 0, btnStoreSchema);
	    flexTable.setWidget(0, 1, btnShowStoredSchemas);
	}
	// end of event handling
    }

    public void showSchemas(List<SchemaData> list) {
	popup = new PopupPanel(true);
	popup.setGlassEnabled(true);
	popup.setAnimationEnabled(true);
	popup.setPopupPosition(100, 100);

	SchemaCellTable table = new SchemaCellTable(list, this);

	ScrollPanel scroll = new ScrollPanel(table);
	scroll.setWidth("100%");
	table.setWidth("100%");

	popup.add(scroll);
	if (table.getRowCount() > 20)
	    popup.setSize("1000px", "600px");
	else {
	    popup.setSize("1000px", "200px");
	}

	popup.show();
    }

    public void showStoreSchemaForm(String user, SimpleSheet[] editedSheets) {
	StoreSchemaWidget ssw = new StoreSchemaWidget(this.username, metas, editedSheets);
	ssw.show();
    }

    public void reloadSchema(String source, String id) {

	// show the fact it is a reloading
	ReloadPopup reload = new ReloadPopup(this, source, id);
	reload.show();

	// this.getSchemaAttributes(source, id);
    }

    public void closePopup() {
	if (popup != null && popup.isShowing())
	    popup.hide();
    }

    private void reloadFile(final String fileSource, final String schemaId) {

	// now we reload the file
	parsingService.parseSpreadsheet(username, fileSource, false, schemaId, new AsyncCallback<SimpleSheet[]>() {

	    public void onSuccess(SimpleSheet[] result) {

		SimpleSheet[] titleAndDatas = result;

		autoSheets = result;

		initTab();

		ArrayList<SimpleCell> totalAttrCells = new ArrayList<SimpleCell>();

		for (int sheet = 0; sheet < titleAndDatas.length; sheet++) {

		    String title = titleAndDatas[sheet].getTitle();

		    if (sheet > 0)
			addAndSelectTab(sheet);

		    // setting title and datas in SpreadsheetTable obj
		    SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(sheet)).getWidget();

		    localTable.setVisible(false);

		    // before setting the data in the flextable, we go
		    // on with replacing detected attributes
		    SimpleCell[][] cells = titleAndDatas[sheet].getCells();

		    ArrayList<SimpleCell> attrCells = new ArrayList<SimpleCell>();

		    for (int row = 0; row < cells.length; row++) {

			for (int col = 0; col < cells[row].length; col++) {

			    // add to the definitive attr list
			    if (cells[row][col].isAttribute()) {

				ListIterator<ConnexComposant> it = titleAndDatas[sheet].getConnexComps().listIterator();

				SimpleCell c = cells[row][col];
				c.setEntityName(titleAndDatas[sheet].getTitle());
				cells[row][col] = c;

				while (it.hasNext()) {
				    ConnexComposant cc = it.next();
				    if (cc.containsPoint(cells[row][col].getJxlCol(), cells[row][col].getJxlRow())) {
					cells[row][col].setCcStartX(cc.getStartX());
					cells[row][col].setCcStartY(cc.getStartY());
					break;
				    }
				}

				attrCells.add(cells[row][col]);
				totalAttrCells.add(cells[row][col]);
			    }
			}
		    }

		    titleAndDatas[sheet].setCells(cells);

		    // pass attribute list to the tree
		    setTree(fileSource, totalAttrCells);

		    // handle updates
		    localTable.setListener(tree);

		    setTabTitle(sheet, title);

		    // set cell
		    localTable.updateSheet(titleAndDatas[sheet], false, false);

		    localTable.setVisible(true);

		}

		final XMLStorageServiceAsync storeService = XMLStorageService.Util.getInstance();

		// get the edited xml
		storeService.getDocument(schemaId, SpreadsheetViewer.username, new AsyncCallback<String>() {

		    @Override
		    public void onFailure(Throwable caught) {
			WaitingPopup.getInstance().hide();
			tabLayoutPanel.selectTab(0);
		    }

		    @Override
		    public void onSuccess(String result) {

			parseMessage(result);
			WaitingPopup.getInstance().hide();
			tabLayoutPanel.selectTab(0);

		    }
		});
	    }

	    public void onFailure(Throwable caught) {
		WaitingPopup.getInstance().hide();
		try {
		    throw caught;
		} catch (ParserException e) {
		    Window.alert(e.getMessage());
		} catch (Throwable e) {
		}
	    }
	});

	// textArea.setText(textArea.getText() + "Reloaded " + fileSource +
	// "\n");
    }

    public void addAndSelectTab(int page) {
	// Datas
	SpreadsheetEditor addtable = new SpreadsheetEditor();
	addtable.setTitle("Spreadsheet visualization");
	addtable.setSize("97%", "97%");

	ScrollPanel listPanel = new ScrollPanel(addtable);
	tabLayoutPanel.add(listPanel, "Spreadsheet", true);
	listPanel.setHorizontalScrollPosition(1);
    }

    public void setTabTitle(int page, String title) {
	((Label) tabLayoutPanel.getTabWidget(page)).setText(title);
    }

    public void setTree(String schemaName, ArrayList<SimpleCell> attrCells) {
	tree.setElements(schemaName, attrCells);
    }

    public void parseSpreadsheet(final String filename, final boolean withAttrDetect, final String schemaId) {
	// parsingService.parseSpreadsheet(username, filename, withAttrDetect,
	// null, new AsyncCallback<SimpleSheet[]>() {
	// @TODO bad
	parsingService.parseSpreadsheet(username, filename, true, null, new AsyncCallback<SimpleSheet[]>() {
	    public void onSuccess(SimpleSheet[] result) {

		GWT.log(this.getClass().getName() + " File parsed");

		autoSheets = result;

		SimpleSheet[] titleAndDatas = (SimpleSheet[]) result;

		initTab();

		final ArrayList<SimpleCell> totalAttrCells = new ArrayList<SimpleCell>();

		for (int sheet = 0; sheet < titleAndDatas.length; sheet++) {

		    String title = titleAndDatas[sheet].getTitle();

		    if (sheet > 0)
			addAndSelectTab(sheet);

		    // setting title and datas in SpreadsheetTable obj
		    SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(sheet)).getWidget();
		    localTable.setVisible(false);
		    localTable.setTitle(title);

		    //
		    if (withAttrDetect) {
			final int finalSheet = sheet;
			parsingService.getTree(username, finalSheet, new AsyncCallback<ArrayList<SimpleCell>>() {

			    @Override
			    public void onFailure(Throwable caught) {
				GWT.log("getTree fail");
			    }

			    @Override
			    public void onSuccess(ArrayList<SimpleCell> result) {

				GWT.log(this.getClass().getName() + " result for tree " + result.size());

				// get the attr cells
				// list
				totalAttrCells.addAll(result);

				setTree(filename, totalAttrCells);
				((SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(finalSheet)).getWidget()).setListener(tree);
				// now it will handle
				// the updates
				//
			    }
			});
		    } else {
			// new way to reload the file, using xml document
			if (schemaId != null) {
			    final XMLStorageServiceAsync storeService = XMLStorageService.Util.getInstance();
			    storeService.getDocument(schemaId, SpreadsheetViewer.username, new AsyncCallback<String>() {

				@Override
				public void onFailure(Throwable caught) {
				    WaitingPopup.getInstance().hide();
				}

				@Override
				public void onSuccess(String result) {

				    GWT.log("Reload xml file : " + result);
				    parseMessage(result);

				    WaitingPopup.getInstance().hide();
				}
			    });
			}
		    }

		    setTabTitle(sheet, title);

		    localTable.setVisible(true);
		    WaitingPopup.getInstance().hide();

		}

		GWT.log(this.getClass().getName() + " Widget filled");

		//
		parsingService.getSheet(username, 0, new AsyncCallback<SimpleSheet>() {
		    @Override
		    public void onFailure(Throwable caught) {
			GWT.log(this.getClass().getName() + " " + caught.getMessage());
		    }

		    @Override
		    public void onSuccess(SimpleSheet result) {
			GWT.log(this.getClass().getName() + " getSheet results");
			SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(0)).getWidget();
			localTable.updateSheet(result, showCC, false);
		    }
		});
		//

		// textArea.setText(textArea.getText() + "Uploaded " + filename
		// + "\n");
	    }

	    public void onFailure(Throwable caught) {
		WaitingPopup.getInstance().hide();
		GWT.log(this.getClass().getName() + " " + caught.getMessage());
	    }
	});
    }

    private void showSchemas() {

	XMLStorageServiceAsync storeService = (XMLStorageServiceAsync) GWT.create(XMLStorageService.class);
	storeService.getDocuments(false, SpreadsheetViewer.username, new AsyncCallback<List<SchemaData>>() {
	    @Override
	    public void onFailure(Throwable caught) {
		caught.printStackTrace();
		Window.alert("Can't load schemas.");
	    }

	    @Override
	    public void onSuccess(List<SchemaData> result) {
		showSchemas(result);
	    }
	});
    }

    public void initTab() {
	tabLayoutPanel.clear();
	this.addAndSelectTab(0);
    }

    public TabLayoutPanel getTabLayoutPanel() {
	return tabLayoutPanel;
    }

    public void setTabLayoutPanel(TabLayoutPanel tabLayoutPanel) {
	this.tabLayoutPanel = tabLayoutPanel;
    }

    public SpreadsheetParsingServiceAsync getParsingService() {
	return parsingService;
    }

    public void setParsingService(SpreadsheetParsingServiceAsync parsingService) {
	this.parsingService = parsingService;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public HashMap<String, String> getMetas() {
	return metas;
    }

    public void setMetas(HashMap<String, String> metas) {
	this.metas = metas;
    }

    public SimpleSheet[] getEditedSheets() {
	// get sheets
	SimpleSheet[] editedSheets = new SimpleSheet[tabLayoutPanel.getWidgetCount()];

	// ok set them
	for (int i = 0; i < tabLayoutPanel.getWidgetCount(); i++) {
	    SpreadsheetEditor localTable = (SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(i)).getWidget();
	    editedSheets[i] = localTable.getSheet();
	}
	return editedSheets;
    }

    private void parseMessage(String messageXml) {
	try {
	    // parse the XML document into a DOM
	    Document messageDom = XMLParser.parse(messageXml);

	    GWT.log("Parse xml");

	    // same thing with sheet
	    SimpleSheet[] sheets = this.getEditedSheets();

	    // set criteria to CCs
	    for (int i = 0; i < messageDom.getElementsByTagName("table").getLength(); i++) {
		NamedNodeMap table = messageDom.getElementsByTagName("table").item(i).getAttributes();

		int sheet = new Integer(table.getNamedItem("sheet").getNodeValue()) - 1;
		if (table.getNamedItem("criteria") != null) {
		    String criteria = table.getNamedItem("criteria").getNodeValue();
		    String attrInLine = "false";
		    if (table.getNamedItem("attrInLine") != null)
			attrInLine = table.getNamedItem("attrInLine").getNodeValue();
		    String biDim = "false";
		    if (table.getNamedItem("biDim") != null)
			biDim = table.getNamedItem("biDim").getNodeValue();
		    int startX = new Integer(table.getNamedItem("startX").getNodeValue()) - 1;
		    int endX = new Integer(table.getNamedItem("endX").getNodeValue()) - 1;
		    int startY = new Integer(table.getNamedItem("startY").getNodeValue()) - 1;
		    int endY = new Integer(table.getNamedItem("endY").getNodeValue()) - 1;
		    String sheetName = new String();
		    if (table.getNamedItem("sheetName") != null)
			sheetName = table.getNamedItem("sheetName").getNodeValue();

		    for (int ccs = 0; ccs < sheets[sheet].getConnexComps().size(); ccs++) {
			if (sheets[sheet].getConnexComps().get(ccs).getStartX() == startX && sheets[sheet].getConnexComps().get(ccs).getEndX() == endX
				&& sheets[sheet].getConnexComps().get(ccs).getStartY() == startY && sheets[sheet].getConnexComps().get(ccs).getEndY() == endY) {
			    sheets[sheet].getConnexComps().get(ccs).setCriteria(criteria);
			    sheets[sheet].getConnexComps().get(ccs).setAttrInLines(new Boolean(attrInLine));
			    sheets[sheet].getConnexComps().get(ccs).setBiDimensionnalArray(new Boolean(biDim));
			    sheets[sheet].setTitle(sheetName);
			}
		    }
		}
	    }
	    //

	    // set comments and titles
	    for (int i = 0; i < messageDom.getElementsByTagName("title").getLength(); i++) {
		NamedNodeMap title = messageDom.getElementsByTagName("title").item(i).getAttributes();

		int sheet = new Integer(title.getNamedItem("sheet").getNodeValue()) - 1;
		int x = new Integer(title.getNamedItem("x").getNodeValue()) - 1;
		int y = new Integer(title.getNamedItem("y").getNodeValue()) - 1;

		SpreadsheetEditor localTable = ((SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(sheet)).getWidget());
		((Element) localTable.getRowElement(y).getCells().getItem(x).getChild(0)).addClassName("titleCell");

		sheets[sheet].getCells()[y][x].setCurrentMeta("title");
	    }

	    for (int i = 0; i < messageDom.getElementsByTagName("comment").getLength(); i++) {
		NamedNodeMap comment = messageDom.getElementsByTagName("comment").item(i).getAttributes();

		int sheet = new Integer(comment.getNamedItem("sheet").getNodeValue()) - 1;
		int x = new Integer(comment.getNamedItem("x").getNodeValue()) - 1;
		int y = new Integer(comment.getNamedItem("y").getNodeValue()) - 1;

		SpreadsheetEditor localTable = ((SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(sheet)).getWidget());
		((Element) localTable.getRowElement(y).getCells().getItem(x).getChild(0)).addClassName("commentCell");

		sheets[sheet].getCells()[y][x].setCurrentMeta("comment");
	    }

	    // set attributes and dspl meta for attributes
	    for (int i = 0; i < messageDom.getElementsByTagName("attribute").getLength(); i++) {
		NamedNodeMap attribute = messageDom.getElementsByTagName("attribute").item(i).getAttributes();
		Node attrName = messageDom.getElementsByTagName("name").item(i);
		Node dspltype = messageDom.getElementsByTagName("dspltype").item(i);
		Node type = null;
		if (messageDom.getElementsByTagName("type") != null)
		    type = messageDom.getElementsByTagName("type").item(i);
		Node format = null;
		if (messageDom.getElementsByTagName("format") != null)
		    format = messageDom.getElementsByTagName("format").item(i);
		Node enginescript = null;
		if (messageDom.getElementsByTagName("enginescript") != null)
		    enginescript = messageDom.getElementsByTagName("enginescript").item(i);

		int sheet = new Integer(attribute.getNamedItem("sheet").getNodeValue()) - 1;
		
		int x = -1;
		int y = -1;

		if (attribute.getNamedItem("origX") != null) {
		    x = new Integer(attribute.getNamedItem("origX").getNodeValue()) - 1;
		    y = new Integer(attribute.getNamedItem("origY").getNodeValue()) - 1;
		} else {
		    x = new Integer(attribute.getNamedItem("x").getNodeValue()) - 1;
		    y = new Integer(attribute.getNamedItem("y").getNodeValue()) - 1;
		}
				
		SpreadsheetEditor localTable = ((SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(sheet)).getWidget());

		if (attrName.getFirstChild() != null && localTable.getRowCount() > y) {
		    ((Element) localTable.getRowElement(y).getCells().getItem(x).getChild(0)).setInnerText(((Text) attrName.getFirstChild()).getData());

		    sheets[sheet].getCells()[y][x].setEditedContent(((Text) attrName.getFirstChild()).getData());
		    sheets[sheet].getCells()[y][x].setCurrentDsplMeta(dspltype.getFirstChild().getNodeValue());
		    // new after dsplengine
		    if (type != null)
			sheets[sheet].getCells()[y][x].setContentType(dspltype.getFirstChild().getNodeValue());
		    if (format != null && format.getFirstChild() != null)
			sheets[sheet].getCells()[y][x].setFormat(format.getFirstChild().getNodeValue());
		    if (enginescript != null && enginescript.getFirstChild() != null)
			sheets[sheet].getCells()[y][x].setEngineScript(enginescript.getFirstChild().getNodeValue());
		    //
		    sheets[sheet].getCells()[y][x].setAttribute(true);
		    ((Element) localTable.getRowElement(y).getCells().getItem(x).getChild(0)).addClassName("attributeCell");

		    //
		    sheets[sheet].getCells()[y][x].setSheetName(sheets[sheet].getTitle());
		    sheets[sheet].getCells()[y][x].setEntityName(sheets[sheet].getTitle());
		    //

		    localTable.getAttrCells().add(sheets[sheet].getCells()[y][x]);
		    localTable.updateElement(sheets[sheet].getCells()[y][x]);
		}
	    }

	    // set datas
	    for (int i = 0; i < messageDom.getElementsByTagName("data").getLength(); i++) {
		NamedNodeMap data = messageDom.getElementsByTagName("data").item(i).getAttributes();

		Node content = messageDom.getElementsByTagName("data").item(i);

		int sheet = new Integer(data.getNamedItem("sheet").getNodeValue()) - 1;

		int x = -1;
		int y = -1;

		if (data.getNamedItem("origX") != null) {
		    x = new Integer(data.getNamedItem("origX").getNodeValue()) - 1;
		    y = new Integer(data.getNamedItem("origY").getNodeValue()) - 1;
		} else {
		    x = new Integer(data.getNamedItem("x").getNodeValue()) - 1;
		    y = new Integer(data.getNamedItem("y").getNodeValue()) - 1;
		}

		// added for dsple
		String script = new String();
		if (data.getNamedItem("script") != null)
		    script = data.getNamedItem("script").getNodeValue();

		// GWT.log(content.getFirstChild().getNodeValue());

		SpreadsheetEditor localTable = ((SpreadsheetEditor) ((ScrollPanel) tabLayoutPanel.getWidget(sheet)).getWidget());

		if (sheets[sheet].getCells() != null && sheets[sheet].getCells().length > y && sheets[sheet].getCells()[y].length > x
			&& content.getFirstChild() != null) {
		    sheets[sheet].getCells()[y][x].setEditedContent(((Text) content.getFirstChild()).getNodeValue());		 
		    sheets[sheet].getCells()[y][x].setEngineScript(script);
		    localTable.updateCellContent(sheets[sheet].getCells()[y][x]);
		}
	    }

	} catch (DOMException e) {
	   GWT.log(e.getMessage());
	    Window.alert("Could not parse document.");
	}
    }

    public void loadXMLSchema(final String schemaId) {

	final XMLStorageServiceAsync storeService = XMLStorageService.Util.getInstance();

	WaitingPopup.getInstance().show();

	// first reparse original file
	storeService.getFileName(schemaId, SpreadsheetViewer.username, new AsyncCallback<String>() {

	    @Override
	    public void onFailure(Throwable caught) {
		// hideWaitingPanel();
		WaitingPopup.getInstance().hide();
		tabLayoutPanel.selectTab(0);
	    }

	    @Override
	    public void onSuccess(String result) {

		GWT.log(result);

		reloadFile(result, schemaId);
	    }
	});
    }

    public void setReloaded(boolean reloaded) {
	this.reloaded = reloaded;
    }

    public boolean isReloaded() {
	return reloaded;
    }

    public void setObjectId(String objectId) {
	this.objectId = objectId;
    }

    public String getObjectId() {
	return objectId;
    }

    public SimpleSheet[] getAutoSheets() {
	return autoSheets;
    }

    public String getCurrentFile() {
	return currentFile;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getDescription() {
	return description;
    }

    public void setQualityTable(DetectionQualityCellTable qualityTable) {
	this.qualityTable = qualityTable;
    }

    public DetectionQualityCellTable getQualityTable() {
	return qualityTable;
    }

    public String getPublication_id() {
	return publication_id;
    }

    public void setPublication_id(String publication_id) {
	this.publication_id = publication_id;
    }
}
