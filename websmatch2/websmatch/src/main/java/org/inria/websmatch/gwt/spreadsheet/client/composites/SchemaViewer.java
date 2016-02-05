package org.inria.websmatch.gwt.spreadsheet.client.composites;

import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaService;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

public class SchemaViewer extends Composite {

    private FlowPanel verticalPanel;
    private MainFrame results = null;

    private SchemaServiceAsync schemaService = (SchemaServiceAsync) GWT.create(SchemaService.class);
    private PopupPanel waitingPopup;
    private Image ajaxImage;
    private GfxRessources gfxRes = GfxRessources.INSTANCE;

    public SchemaViewer(final MainFrame frame) {

	results = frame;

	verticalPanel = new FlowPanel();
	initWidget(verticalPanel);
	verticalPanel.setSize("100%", "100%");

	SplitLayoutPanel horizontalPanel = new SplitLayoutPanel();
	verticalPanel.add(horizontalPanel);
	horizontalPanel.setSize("99.5%", "95%");

	final SchemaTreeLoader leftTreeLoader = new SchemaTreeLoader(results.getUserName());
	leftTreeLoader.getTree().setSize("100%", "100%");
	horizontalPanel.addWest(leftTreeLoader, 570.0);
	leftTreeLoader.setSize("98%", "94%");
	final SchemaTreeLoader rightTreeLoader = new SchemaTreeLoader(results.getUserName());
	rightTreeLoader.getTree().setSize("100%", "100%");

	horizontalPanel.add(rightTreeLoader);
	rightTreeLoader.setSize("98%", "94%");

	ajaxImage = new Image(gfxRes.loader());

	Button button = new Button("New button");
	button.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent arg0) {

		if (leftTreeLoader.getSchemaId() == rightTreeLoader.getSchemaId()) {
		    Window.alert("This matching is not authorized.");
		}

		else {
		    showWaitingPanel();

		    schemaService.matchSchemas(leftTreeLoader.getSchemaId(), rightTreeLoader.getSchemaId(), -1, frame.getMatchingTech(),
			    new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable arg0) {
				    hideWaitingPanel();
				}

				@Override
				public void onSuccess(Void arg0) {
				    results.setListResult(leftTreeLoader.getSchemaName(), rightTreeLoader.getSchemaName());
				    hideWaitingPanel();
				}
			    });
		}
	    }
	});
	button.setText("Launch YAM matching");
	verticalPanel.add(button);
	button.setWidth("182px");
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

}
