package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingResult;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSchema;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

@SuppressWarnings("deprecation")
public class SchemaResultsListers extends Composite {

    private static final List<String> tresholdValues = Arrays.asList("0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0");

    private MatchingResultsServiceAsync service = (MatchingResultsServiceAsync) GWT.create(MatchingResultsService.class);

    private CellTable<MatchingResult> cellTable;
    private List<MatchingResult> results;
    final ListBox leftList = new ListBox();
    final ListBox rightList = new ListBox();

    private PopupPanel waitingPopup;
    GfxRessources gfxRes = GfxRessources.INSTANCE;
    private Image ajaxImage;

    private Button btnSubmit;

    public SchemaResultsListers(List<SimpleSchema> leftSchemas, List<SimpleSchema> rightSchemas, final MainFrame fr) {

	ajaxImage = new Image(gfxRes.loader());

	FlowPanel flowPanel = new FlowPanel();
	initWidget(flowPanel);
	flowPanel.setSize("100%", "99%");

	/*
	 * CaptionPanel cptnpnlMatchingResults = new CaptionPanel("");
	 * cptnpnlMatchingResults.setStyleName("body");
	 * flowPanel.add(cptnpnlMatchingResults);
	 * cptnpnlMatchingResults.setSize("100%", "100%");
	 */

	FlowPanel flowPanel_1 = new FlowPanel();
	flowPanel_1.setStyleName("body");
	// cptnpnlMatchingResults.setContentWidget(flowPanel_1);

	flowPanel.add(flowPanel_1);

	flowPanel_1.setSize("100%", "");

	FlexTable table = new FlexTable();
	table.setStyleName("body");
	flowPanel_1.add(table);
	table.setSize("100%", "200px");

	Label lblLeftSchema = new Label("Left schema :");
	lblLeftSchema.setHeight("32px");
	table.setWidget(0, 0, lblLeftSchema);

	Label lblRightSchema = new Label("Right schema :");
	lblRightSchema.setHeight("32px");
	table.setWidget(0, 2, lblRightSchema);

	leftList.setHeight("");
	leftList.setVisibleItemCount(1);
	leftList.setWidth("70%");

	table.setWidget(0, 1, leftList);

	rightList.setHeight("");
	rightList.setVisibleItemCount(1);
	rightList.setWidth("70%");

	table.setWidget(0, 3, rightList);

	// add threshold control and submit
	Label treLabel = new Label("Threshold :");
	treLabel.setHeight("32px");
	table.setWidget(1, 0, treLabel);

	final ListBox valuePicker = new ListBox();
	valuePicker.setHeight("");
	valuePicker.setVisibleItemCount(1);

	table.setWidget(1, 1, valuePicker);

	btnSubmit = new Button("Show results");
	btnSubmit.setSize("150px", "");

	Label lblMatchingTechnique = new Label("Matching technique :");
	table.setWidget(1, 2, lblMatchingTechnique);

	final ListBox listBox = new ListBox();
	table.setWidget(1, 3, listBox);
	listBox.setVisibleItemCount(1);

	table.setWidget(1, 4, btnSubmit);

	btnSubmit.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent arg0) {

		showWaitingPanel();

		// Use the service to get results
		service.getResults(leftList.getValue(leftList.getSelectedIndex()), rightList.getValue(rightList.getSelectedIndex()),
			valuePicker.getValue(valuePicker.getSelectedIndex()), listBox.getValue(listBox.getSelectedIndex()),
			new AsyncCallback<List<MatchingResult>>() {

			    @Override
			    public void onFailure(Throwable arg0) {
				arg0.printStackTrace();
				hideWaitingPanel();
			    }

			    @Override
			    public void onSuccess(List<MatchingResult> arg0) {
				results = arg0;
				loadResults(arg0);
				hideWaitingPanel();
			    }
			});

	    }
	});

	SimplePager simplePager = new SimplePager();
	flowPanel_1.add(simplePager);
	simplePager.setWidth("100%");

	// now add the pagin cell table
	cellTable = new CellTable<MatchingResult>();
	flowPanel_1.add(cellTable);
	if (fr.getOffsetHeight() > 700)
	    cellTable.setPageSize(16);
	else
	    cellTable.setPageSize(8);
	cellTable.setWidth("100%");

	results = new ArrayList<MatchingResult>();

	AsyncDataProvider<MatchingResult> provider = new AsyncDataProvider<MatchingResult>() {
	    @Override
	    protected void onRangeChanged(HasData<MatchingResult> display) {
		int start = display.getVisibleRange().getStart();
		int end = start + display.getVisibleRange().getLength();
		end = end >= results.size() ? results.size() : end;
		List<MatchingResult> sub = results.subList(start, end);
		updateRowData(start, sub);
	    }
	};

	provider.updateRowCount(results.size(), true);

	// And validation expert col
	CheckboxCell expert = new CheckboxCell();

	provider.addDataDisplay(cellTable);

	simplePager.setDisplay(cellTable);

	// Create left column.
	Column<MatchingResult, SafeHtml> leftColumn = new Column<MatchingResult, SafeHtml>(new SafeHtmlCell()) {
	    /*
	     * public String getValue(MatchingResult res) {
	     * 
	     * if(res.getLeftElementName().length() > 40) return
	     * res.getLeftElementName().substring(0, 38)+"..."; return
	     * res.getLeftElementName(); }
	     */

	    @Override
	    public SafeHtml getValue(MatchingResult object) {
		String val = object.getLeftElementName();
		String shortVal = new String();
		if (val.length() > 40)
		    shortVal = val.substring(0, 38) + "...";
		else
		    shortVal = val;
		return new SafeHtmlBuilder().appendHtmlConstant(
			"<span title='" + new SafeHtmlBuilder().appendEscaped(val).toSafeHtml().asString() + "'>" + shortVal + "</span>").toSafeHtml();
	    }

	};

	// Create right column
	/*
	 * TextColumn<MatchingResult> rightColumn = new
	 * TextColumn<MatchingResult>() { public String getValue(MatchingResult
	 * res) {
	 * 
	 * if(res.getRightElementName().length() > 40) return
	 * res.getRightElementName().substring(0, 38)+"..."; return
	 * res.getRightElementName(); } };
	 */
	Column<MatchingResult, SafeHtml> rightColumn = new Column<MatchingResult, SafeHtml>(new SafeHtmlCell()) {

	    @Override
	    public SafeHtml getValue(MatchingResult object) {
		String val = object.getRightElementName();
		String shortVal = new String();
		if (val.length() > 40)
		    shortVal = val.substring(0, 38) + "...";
		else
		    shortVal = val;
		return new SafeHtmlBuilder().appendHtmlConstant(
			"<span title='" + new SafeHtmlBuilder().appendEscaped(val).toSafeHtml().asString() + "'>" + shortVal + "</span>").toSafeHtml();
	    }

	};

	// And score column
	TextColumn<MatchingResult> scoreColumn = new TextColumn<MatchingResult>() {
	    public String getValue(MatchingResult res) {
		return new Double(res.getScore()).toString();
	    }
	};

	final Column<MatchingResult, Boolean> expertColumn = new Column<MatchingResult, Boolean>(expert) {
	    @Override
	    public Boolean getValue(MatchingResult object) {
		return object.isExpert();
	    }
	};

	// Click on
	expertColumn.setFieldUpdater(new FieldUpdater<MatchingResult, Boolean>() {
	    @Override
	    public void update(final int index, final MatchingResult object, final Boolean value) {

		int bool = (value) ? 1 : 0;

		service.updateExpert(object.getId_element1(), object.getId_element2(), bool, object.getId_schema1(), object.getId_schema2(),
			new AsyncCallback<Void>() {

			    @Override
			    public void onFailure(Throwable arg0) {

				expertColumn.getFieldUpdater().update(index, object, !value);

			    }

			    @Override
			    public void onSuccess(Void arg0) {

			    }

			});

	    }
	});

	cellTable.addColumn(leftColumn, "Left element");
	cellTable.setColumnWidth(leftColumn, "20%");

	cellTable.addColumn(rightColumn, "Right element");
	cellTable.setColumnWidth(rightColumn, "20%");

	cellTable.addColumn(scoreColumn, "Score");
	cellTable.setColumnWidth(scoreColumn, "10%");
	cellTable.addColumn(expertColumn, "Expert");
	cellTable.setColumnWidth(expertColumn, "10%");

	cellTable.setRowCount(0, true);

	/*
	 * TextCell leftCell = new TextCell(); CellList<String> leftList = new
	 * CellList<String>(leftCell); leftList.setPageSize(1);
	 * leftList.setRowCount(leftSchemas.size(), true);
	 * 
	 * leftList.setRowData(0, loadSchemas(leftSchemas));
	 */
	for (SimpleSchema s : leftSchemas)
	    leftList.addItem(s.getName());

	/*
	 * TextCell rightCell = new TextCell(); CellList<String> rightList = new
	 * CellList<String>(rightCell); rightList.setPageSize(1);
	 * rightList.setRowCount(leftSchemas.size(), true);
	 * 
	 * rightList.setRowData(0, loadSchemas(rightSchemas));
	 */
	for (SimpleSchema s : rightSchemas)
	    rightList.addItem(s.getName());

	/*
	 * CellList<String> valP = new CellList<String>(new TextCell());
	 * valP.setRowData(0, tresholdValues); ValuePicker<String> valuePicker =
	 * new ValuePicker<String>(valP); valuePicker.setPageSize(1);
	 */
	for (String s : tresholdValues)
	    valuePicker.addItem(s);

	valuePicker.setSelectedIndex(5);

	// get the elements
	if (fr.getUserName() == null || !fr.getUserName().contains("rigaowl"))
	    service.getMatchingTechs(new AsyncCallback<List<SimpleMatchTech>>() {

		@Override
		public void onFailure(Throwable arg0) {

		}

		@Override
		public void onSuccess(List<SimpleMatchTech> arg0) {

		    for (SimpleMatchTech tech : arg0) {
			listBox.addItem(tech.getName());
			if (tech.getName().equals(fr.getMatchingTech()))
			    listBox.setSelectedIndex(listBox.getItemCount() - 1);
		    }

		}

	    });

	else
	    listBox.addItem("SoftTFIDF");
    }

    private void loadResults(List<MatchingResult> res) {
	cellTable.setPageStart(0);
	cellTable.setRowCount(res.size(), true);
	cellTable.setRowData(0, res);
    }

    public ListBox getLeftList() {
	return leftList;
    }

    public ListBox getRightList() {
	return rightList;
    }

    public Button getBtnSubmit() {
	return btnSubmit;
    }

    /*
     * private static List<String> loadSchemas(List<SimpleSchema> ls) {
     * 
     * List<String> res = new ArrayList<String>();
     * 
     * for (SimpleSchema s : ls) res.add(s.getName());
     * 
     * return res;
     * 
     * }
     */

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
