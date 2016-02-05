package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.MainFrame;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;

public class YAMConfigurationPopup extends PopupPanel {

    public YAMConfigurationPopup(final MainFrame fr) {
	super(true);
	setPopupPosition(100, 100);
	setGlassEnabled(true);
	setAnimationEnabled(true);
	FlowPanel flowPanel = new FlowPanel();
	setWidget(flowPanel);
	flowPanel.setSize("260px", "60px");

	final ListBox listBox = new ListBox();

	/*
	 * listBox.addItem("Levenshtein"); listBox.addItem("SmithWaterman");
	 * listBox.addItem("SmithWatermanGotoh");
	 * listBox.addItem("SmithWatermanGotohWindowedAffine");
	 * listBox.addItem("Jaro"); listBox.addItem("JaroWinkler");
	 * listBox.addItem("Stoilos_JW"); listBox.addItem("QGramsDistance");
	 * listBox.addItem("MongeElkan"); listBox.addItem("WuPalmer");
	 * listBox.addItem("Lin"); listBox.addItem("MultiLevelMatcher");
	 * listBox.addItem("SoftTFIDF"); listBox.addItem("SoftTFIDFWordNet");
	 */

	if (fr.getUserName() == null || !fr.getUserName().contains("rigaowl"))
	    listBox.addItem("Stoilos_JW");
	else
	    listBox.addItem("SoftTFIDF");

	// add the techs to listbox
	MatchingResultsServiceAsync service = (MatchingResultsServiceAsync) GWT.create(MatchingResultsService.class);

	listBox.setSelectedIndex(0);

	if (fr.getUserName() == null || !fr.getUserName().contains("rigaowl")) {
	    service.getMatchingTechs(new AsyncCallback<List<SimpleMatchTech>>() {

		@Override
		public void onFailure(Throwable arg0) {
		    // TODO Auto-generated method stub

		}

		@Override
		public void onSuccess(List<SimpleMatchTech> arg0) {

		    for (SimpleMatchTech tech : arg0) {
			if (!tech.getName().equals("Stoilos_JW"))
			    listBox.addItem(tech.getName());
		    }

		}

	    });
	}

	flowPanel.add(listBox);
	listBox.setWidth("250px");
	listBox.setVisibleItemCount(1);

	final YAMConfigurationPopup pop = this;

	Button button = new Button("New button");
	button.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent arg0) {
		fr.setMatchingTech(listBox.getItemText(listBox.getSelectedIndex()));
		pop.hide();
	    }
	});
	button.setText("Apply configuration");
	flowPanel.add(button);
    }

}
