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

public class N3AnnotatorPopup extends MatcherPopup {

	public N3AnnotatorPopup() {
		// TODO: restructure like CSVOboMapperPopup
		super();

		final VerticalPanel mainPanel = new VerticalPanel();
		final FormWidget form = new FormWidget("Send N3 file");
		mainPanel.add(form);

		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				// ok file arrived, now parsing it
				setFirstFile(event.getResults()
						.substring(event.getResults().indexOf(">") + 1, event.getResults().indexOf("<", event.getResults().indexOf(">"))));

				// add second form
				final FormWidget form = new FormWidget("Send OBO file (optional)");
				mainPanel.add(form);

				form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
					public void onSubmitComplete(SubmitCompleteEvent event) {
						setSecondFile(event.getResults().substring(event.getResults().indexOf(">") + 1,
								event.getResults().indexOf("<", event.getResults().indexOf(">"))));

						// add match
						Button matchButton = new Button("Match files");
						matchButton.setWidth("200px");

						HorizontalPanel checkPan = new HorizontalPanel();
						final CheckBox useBioPortal = new CheckBox();
						final ListBox ontoList = new ListBox();

						// fill it
						getMatchService().getOntologiesWithIds("IBC", new AsyncCallback<List<String[]>>(){
							@Override
							public void onFailure(Throwable caught) {
								ontoList.addItem("All ontologies");
							}

							@Override
							public void onSuccess(List<String[]> result) {
								ontoList.addItem("All ontologies");
								for(String[] res : result){
									ontoList.addItem(res[0]+" ["+res[1]+"]");
								}
							}
						});
						//

						final CheckBox labelBox = new CheckBox();
						final CheckBox n3Box = new CheckBox();
						useBioPortal.setValue(false);
						labelBox.setValue(true);
						n3Box.setValue(true);
						Label bioLabel = new Label("Use BioPortal");
						Label ontoLabel = new Label("Use ontologie");
						Label boxLabel = new Label("Use labels only");
						Label n3Label = new Label("Direct DL Annotated N3");

						FlexTable table = new FlexTable();

						table.setWidget(0, 0, bioLabel);
						table.setWidget(0, 1, useBioPortal);
						table.setWidget(1, 0, ontoLabel);
						table.setWidget(1, 1, ontoList);
						table.setWidget(2, 0, boxLabel);
						table.setWidget(3, 0, n3Label);
						table.setWidget(2, 1, labelBox);
						table.setWidget(3, 1, n3Box);
						table.setWidget(4, 0, matchButton);
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

								String ontoId;
								if(ontoList.getItemText(ontoList.getSelectedIndex()).equals("All ontologies")) {
									ontoId = null;
								}else{
									ontoId = ontoList.getItemText(ontoList.getSelectedIndex()).substring(ontoList.getItemText(ontoList.getSelectedIndex()).lastIndexOf('[')+1, ontoList.getItemText(ontoList.getSelectedIndex()).lastIndexOf(']'));
								}

								getMatchService().annotateN3WithObo(getFirstFile(), getSecondFile(), !labelBox.getValue(), "IBC", ontoId, n3Box.getValue(),
										new AsyncCallback<String>() {

											@Override
											public void onFailure(Throwable caught) {
												WaitingPopup.getInstance().hide();
												Window.alert("Can't match those files.");
											}

											@Override
											public void onSuccess(String result) {
												WaitingPopup.getInstance().hide();
												if (n3Box.getValue()) {
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
