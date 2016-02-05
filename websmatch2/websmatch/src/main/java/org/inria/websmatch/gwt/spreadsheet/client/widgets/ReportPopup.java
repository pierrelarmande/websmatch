package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.DetectionQualityService;
import org.inria.websmatch.gwt.spreadsheet.client.DetectionQualityServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.ReportingService;
import org.inria.websmatch.gwt.spreadsheet.client.ReportingServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingService;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetViewer;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageService;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SpreadsheetComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

// TODO corriger le probleme du double editedSheets

public class ReportPopup extends PopupPanel {

    private boolean _DATAPUBLICA_TEST = false;
    private PopupPanel waitingPopup = null;
    private Image ajaxImage;
    GfxRessources gfxRes = GfxRessources.INSTANCE;

    // the services RPC
    private ReportingServiceAsync reportingService = (ReportingServiceAsync) GWT.create(ReportingService.class);

    public ReportPopup(final String user, final String fileName, final SimpleSheet[] autoSheets, final SimpleSheet[] editedSheets,
	    final boolean forDataPublica, final HashMap<String, String> dataPublicaInfos, String storedSchemaName, String storedSchemaDescription,
	    final boolean reloaded, final String objectId, final SpreadsheetComposite compo, final String publication_id) {
	super(true);

	ajaxImage = new Image(gfxRes.loader());

	setAnimationEnabled(true);
	this.setGlassEnabled(true);
	this.setPopupPosition(100, 100);

	VerticalPanel verticalPanel = new VerticalPanel();
	setWidget(verticalPanel);
	verticalPanel.setSize("547px", "277px");

	CaptionPanel cptnpnlNewPanel = new CaptionPanel("Store the document and report problems");
	cptnpnlNewPanel.setCaptionHTML("<b>Store the document and report problems</b>");
	verticalPanel.add(cptnpnlNewPanel);

	Label lblSchemaName = new Label("Schema name");

	final TextBox schemaName = new TextBox();

	Label lblSchemaDescription = new Label("Schema description");

	final TextArea schemaDescription = new TextArea();

	SimpleCheckBox storeCheckBox = new SimpleCheckBox();
	storeCheckBox.setValue(true);

	Label lblNewLabel_1 = new Label("Report table detection problem");

	final SimpleCheckBox tableDetectionCheckBox = new SimpleCheckBox();
	tableDetectionCheckBox.setValue(false);

	Label lblNewLabel_2 = new Label("Report metadata dectection problem");

	final SimpleCheckBox metadataDetectionCheckBox = new SimpleCheckBox();
	metadataDetectionCheckBox.setValue(false);

	Label lblNewLabel_3 = new Label("Describe another problem");

	final TextArea description = new TextArea();

	FlexTable flexTable = new FlexTable();
	cptnpnlNewPanel.setContentWidget(flexTable);
	flexTable.setSize("520px", "245px");

	final Button btnOk = new Button("Ok");

	if (!forDataPublica) {
	    btnOk.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {

		    btnOk.setEnabled(false);

		    // send report
		    sendReport(user, fileName, description.getText(), tableDetectionCheckBox.getValue(), metadataDetectionCheckBox.getValue());
		    // store
		    storeSchema(autoSheets, editedSheets, schemaName.getText(), fileName, user, schemaDescription.getText(), reloaded, objectId, compo,
			    tableDetectionCheckBox.getValue(), metadataDetectionCheckBox.getValue(), description.getText(), false, publication_id);

		    setVisible(false);
		}
	    });
	}

	// TODO("Finir dans le cas DataPublica pour passer sur MongoDB")
	else {
	    btnOk.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {

		    btnOk.setEnabled(false);

		    // send report
		    sendReport(user, fileName, description.getText(), tableDetectionCheckBox.getValue(), metadataDetectionCheckBox.getValue());

		    if (_DATAPUBLICA_TEST) {

			// store schema
			final SpreadsheetParsingServiceAsync parsingService = SpreadsheetParsingService.Util.getInstance();

			parsingService.sendToDataPublica(dataPublicaInfos, editedSheets, false, new AsyncCallback<Void>() {

			    @Override
			    public void onFailure(Throwable caught) {
				setVisible(false);
				Window.alert("Can't send to post_url");
			    }

			    @Override
			    public void onSuccess(Void result) {
				setVisible(false);
				Window.open(dataPublicaInfos.get("callback_url"), "_self", "");
			    }
			});
		    } else {

			// store schema
			final XMLStorageServiceAsync storeService = XMLStorageService.Util.getInstance();
			final SpreadsheetParsingServiceAsync parsingService = SpreadsheetParsingService.Util.getInstance();

			Integer crawl_id = null;
			Integer publication_id = null;

			try {
			    crawl_id = Integer.valueOf(dataPublicaInfos.get("crawl_id"));
			} catch (NumberFormatException e) {
			}

			try {
			    publication_id = Integer.valueOf(dataPublicaInfos.get("publication_id"));
			} catch (NumberFormatException e) {
			}

			showWaitingPanel();

			AsyncCallback<String> storeCallback = new AsyncCallback<String>() {

			    @Override
			    public void onFailure(Throwable caught) {
				hideWaitingPanel();
				setVisible(false);

				Window.alert("Can't access to SchemaStore");
			    }

			    @Override
			    public void onSuccess(String result) {
				parsingService.sendToDataPublica(dataPublicaInfos, editedSheets, false, new AsyncCallback<Void>() {

				    @Override
				    public void onFailure(Throwable caught) {

					hideWaitingPanel();
					setVisible(false);

					Window.alert("Can't send to post_url");
				    }

				    @Override
				    public void onSuccess(Void result) {

					hideWaitingPanel();
					setVisible(false);

					Window.open(dataPublicaInfos.get("callback_url"), "_self", "");
				    }
				});
			    }
			};

			storeService.importDocument(autoSheets, editedSheets, schemaName.getText(), dataPublicaInfos.get("fileName"),
				dataPublicaInfos.get("user_id"), schemaDescription.getText(), SpreadsheetViewer.username, crawl_id.toString(),
				publication_id.toString(), reloaded, objectId, tableDetectionCheckBox.getValue(), metadataDetectionCheckBox.getValue(),
				description.getText(), false, storeCallback);
		    }
		}
	    });
	}

	Button btnCancel = new Button("Cancel");
	btnCancel.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		hide();
	    }
	});

	if (editedSheets[0] != null && !editedSheets[0].getStoredName().equals(""))
	    schemaName.setText(editedSheets[0].getStoredName());
	else
	    schemaName.setText(fileName);

	if (editedSheets[0] != null && !editedSheets[0].getStoredDescription().equals(""))
	    schemaDescription.setText(editedSheets[0].getStoredDescription());

	flexTable.setWidget(0, 0, lblSchemaName);
	flexTable.setWidget(0, 1, schemaName);
	schemaName.setSize("248px", "");
	flexTable.setWidget(1, 0, lblSchemaDescription);
	flexTable.setWidget(1, 1, schemaDescription);
	schemaDescription.setSize("248px", "48px");
	flexTable.setWidget(3, 0, lblNewLabel_1);
	flexTable.setWidget(3, 1, tableDetectionCheckBox);
	flexTable.setWidget(4, 0, lblNewLabel_2);
	flexTable.setWidget(4, 1, metadataDetectionCheckBox);
	flexTable.setWidget(5, 0, lblNewLabel_3);
	flexTable.setWidget(5, 1, description);
	flexTable.setWidget(6, 0, btnOk);
	btnOk.setWidth("100px");
	flexTable.setWidget(6, 1, btnCancel);
	btnCancel.setWidth("100px");
	description.setSize("248px", "75px");
	flexTable.getCellFormatter().setHorizontalAlignment(6, 1, HasHorizontalAlignment.ALIGN_RIGHT);
	flexTable.getCellFormatter().setHorizontalAlignment(6, 0, HasHorizontalAlignment.ALIGN_LEFT);
    }

    public void sendReport(String user, String fileName, String description, boolean ccDetect, boolean metaDetect) {

	GWT.log(user + " " + fileName + " " + description + " " + ccDetect + " " + metaDetect);

	reportingService.insertReport(user, fileName, description, ccDetect, metaDetect, new AsyncCallback<Void>() {

	    @Override
	    public void onFailure(Throwable caught) {

	    }

	    @Override
	    public void onSuccess(Void result) {

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

    public void storeSchema(SimpleSheet[] autoSheets, SimpleSheet[] editedSheets, String schemaName, String fileName, String user, String desc,
	    boolean reloaded, String objectId, final SpreadsheetComposite compo, boolean ccDetectPb, boolean attrDetectPb, String ccDetectDesc, boolean trashed, String publication_id) {
	
	final XMLStorageServiceAsync storeService = XMLStorageService.Util.getInstance();

	AsyncCallback<String> storeCallback = new AsyncCallback<String>() {

	    @Override
	    public void onFailure(Throwable caught) {
		Window.alert("Can't save the doc.");
	    }

	    @Override
	    public void onSuccess(String result) {

		// update table
		// get the fmeas etc... and update
		DetectionQualityServiceAsync qualityService = (DetectionQualityServiceAsync) GWT.create(DetectionQualityService.class);
		qualityService.getDetectectionQualityData(compo.getObjectId(), SpreadsheetViewer.username, new AsyncCallback<DetectionQualityData>() {

		    @Override
		    public void onFailure(Throwable caught) {
			GWT.log(caught.getMessage());
			if (compo.getParent().getClass().equals(PopupPanel.class))
			    ((PopupPanel) compo.getParent()).hide();
		    }

		    @Override
		    public void onSuccess(DetectionQualityData result) {

			if (compo.getQualityTable() != null) {
			    GWT.log("Get data : " + result.getName() + " Fmea : " + result.getFmeasure());

			    // update cell table
			    List<DetectionQualityData> list = compo.getQualityTable().getVisibleItems();
			    List<DetectionQualityData> newList = new ArrayList<DetectionQualityData>();
			    int index = -1;

			    for (int i = 0; i < list.size(); i++) {
				DetectionQualityData dd = list.get(i);
				if (dd.getObjectId().equals(compo.getObjectId())) {
				    dd.setNeverEdited(false);
				    index = i;
				    // newList.add(dd);
				    newList.add(result);
				}
			    }

			    int pageStart = compo.getQualityTable().getPageStart();

			    if (index != -1){
				GWT.log("Update at index "+(index+pageStart));
				compo.getQualityTable().setRowData(index + pageStart, newList);
				compo.getQualityTable().redraw();
			    }
			}
			//
			if (compo.getParent().getClass().equals(PopupPanel.class))
			    ((PopupPanel) compo.getParent()).hide();
		    }
		});
	    }
	};

	storeService.importDocument(autoSheets, editedSheets, schemaName, fileName, user, desc, SpreadsheetViewer.username, "", publication_id, reloaded, objectId,
		ccDetectPb, attrDetectPb, ccDetectDesc, trashed, storeCallback);
    }
}
