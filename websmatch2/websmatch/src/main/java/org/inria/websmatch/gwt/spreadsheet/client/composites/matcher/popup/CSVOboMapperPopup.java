package org.inria.websmatch.gwt.spreadsheet.client.composites.matcher.popup;

import java.util.List;

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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CSVOboMapperPopup extends MatcherPopup {

	public CSVOboMapperPopup() {
		super();

		final VerticalPanel mainPanel = new VerticalPanel();

		// upload csv file
		final FormWidget csvForm = new FormWidget("Send the CSV file to annotate");
		mainPanel.add(csvForm);

		csvForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				// ok file arrived, now parsing it
				setFirstFile(event.getResults()
						.substring(event.getResults().indexOf(">") + 1, event.getResults().indexOf("<", event.getResults().indexOf(">"))));
			}
		});


		// upload obo file
		final FormWidget oboForm = new FormWidget("Send the OBO file used for annotation");
		mainPanel.add(oboForm);

		oboForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				setSecondFile(event.getResults().substring(event.getResults().indexOf(">") + 1,
						event.getResults().indexOf("<", event.getResults().indexOf(">"))));
}
		});


		// List of possible annotator
		Label annotatorLabel = new Label("Use BioPortalAnnotator (take more time)");
		final ListBox annotatorList = new ListBox();
		annotatorList.addItem("IBC");
		// TODO: add other annotator here  - must be exact same name as AnnotatorService enum


		// list possible scoring method
		Label scoreLabel = new Label("Scoring method (require BioPortalAnnotator)");
		final ListBox scoreList = new ListBox();
		scoreList.addItem("no score");
		scoreList.addItem("old");
		scoreList.addItem("cvalue");
		scoreList.addItem("cvalue-h");


		// list of ontologies, used by selected annotator
		final ListBox ontologyList = new ListBox();
		Label ontologyLabel = new Label("Label column");
		// TODO: update ontoList when annotatorList selection is changed
		//fillOntologyList(ontoList, annotatorList.getSelectedValue());
                System.out.println("ontology selected: "+annotatorList.getSelectedValue());
		getMatchService().getOntologiesWithIds(annotatorList.getSelectedValue(),
				new AsyncCallback<List<String[]>>() {
			@Override
			public void onFailure(Throwable caught) {
				ontologyList.addItem("All ontologies.");
			}

			@Override
			public void onSuccess(List<String[]> result) {
				ontologyList.addItem("All ontologies");
				for (String[] res : result) {
					ontologyList.addItem(res[0] + " [" + res[1] + "]");
				}
			}
		});


		// chose to return result as a file download
		final CheckBox directDLBox = new CheckBox();
		Label directDLLabel = new Label("Direct DL CSV");
		directDLBox.setValue(true);


		// other parameters :
		//  - column index of label
		//  - column index of Desc
		//  - max level to use by annotator
		final ListBox labelPlace  = new ListBox();
		final ListBox descPlace   = new ListBox();
		final ListBox maxLevelNum = new ListBox();
		Label labelPlaceLabel = new Label("Label column");
		Label descPlaceLabel = new Label("Desc column");
		Label maxLevelLabel = new Label("Max level");

		for (int i = 0; i < 20; i++) {
			labelPlace.addItem(String.valueOf(i));
			descPlace.addItem(String.valueOf(i));
			if (i < 6)
				maxLevelNum.addItem(String.valueOf(i));
		}

		maxLevelNum.setSelectedIndex(5);


		// the match button
		Button matchButton = new Button("Match files");
		matchButton.setWidth("200px");


		// Layout widget
		HorizontalPanel checkPan = new HorizontalPanel();
		FlexTable table = new FlexTable();

		table.setWidget(0, 0, annotatorLabel);
		table.setWidget(0, 1, annotatorList);
		table.setWidget(1, 0, scoreLabel);
		table.setWidget(1, 1, scoreList);
		table.setWidget(2, 0, ontologyLabel);
		table.setWidget(2, 1, ontologyList);
		table.setWidget(3, 0, directDLLabel);
		table.setWidget(3, 1, directDLBox);

		table.setWidget(3, 0, labelPlaceLabel);
		table.setWidget(3, 1, labelPlace);
		table.setWidget(4, 0, descPlaceLabel);
		table.setWidget(4, 1, descPlace);
		table.setWidget(5, 0, maxLevelLabel);
		table.setWidget(5, 1, maxLevelNum);

		table.setWidget(6, 0, matchButton);
		table.setWidth("50%");

		checkPan.add(table);
		mainPanel.add(checkPan);

		table.getElement().setAttribute("align", "center");
		table.getElement().getParentElement().setAttribute("align", "center");
		table.getElement().getParentElement().getParentElement().getParentElement().getParentElement().setAttribute("align", "center");


		//
		matchButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				WaitingPopup.getInstance().show();
				hide();

				String ontoId;
				if (ontologyList.getItemText(ontologyList.getSelectedIndex()).equals("All ontologies")) {
					ontoId = "{";
					for (int i = 1; i < ontologyList.getItemCount(); i++) {
						ontoId += ontologyList.getItemText(i).substring(ontologyList.getItemText(i).lastIndexOf('[') + 1,
								ontologyList.getItemText(i).lastIndexOf(']'));
						if (i < ontologyList.getItemCount() - 1)
							ontoId += ",";
					}
					ontoId += "}";
				} else {
					ontoId = ontologyList.getItemText(ontologyList.getSelectedIndex()).substring(
							ontologyList.getItemText(ontologyList.getSelectedIndex()).lastIndexOf('[') + 1,
							ontologyList.getItemText(ontologyList.getSelectedIndex()).lastIndexOf(']'));
				}

				String annotatorName = annotatorList.getSelectedItemText();
				String scoreMethod   = scoreList.getSelectedItemText();

				Integer labelPosition = Integer.parseInt(labelPlace.getItemText(labelPlace.getSelectedIndex()));
				Integer descPosition  = Integer.parseInt(descPlace.getItemText(descPlace.getSelectedIndex()));
				Integer maxLevel = Integer.parseInt(maxLevelNum.getItemText(maxLevelNum.getSelectedIndex()));


				getMatchService().matchCSVOboFiles(getFirstFile(), getSecondFile(),
						annotatorName, scoreMethod, ontoId, directDLBox.getValue(),
						labelPosition, descPosition, maxLevel,
						new AsyncCallback<String>() {

							@Override
							public void onFailure(Throwable caught) {
								WaitingPopup.getInstance().hide();
								GWT.log(caught.getMessage());
								Window.alert("Can't match those files.");
							}

							@Override
							public void onSuccess(String result) {
								WaitingPopup.getInstance().hide();
								if (directDLBox.getValue()) {
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

		this.add(mainPanel);
	}

}
