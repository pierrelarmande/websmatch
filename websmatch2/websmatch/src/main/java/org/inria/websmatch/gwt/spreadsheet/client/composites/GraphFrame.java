package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.EvaluationChart;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class GraphFrame extends Composite {

    private FlowPanel flowPanel = null;

    private PopupPanel waitingPopup;
    GfxRessources gfxRes = GfxRessources.INSTANCE;
    private Image ajaxImage;

    private ArrayList<ArrayList<String>> toDraw;
    private int lastChart = 0;
    private Button next;

    public GraphFrame() {

	toDraw = new ArrayList<ArrayList<String>>();
	lastChart = 0;
	ajaxImage = new Image(gfxRes.loader());

	next = new Button("Draw next charts");
	next.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent arg0) {
		drawCharts();
	    }
	});
	next.addStyleName("chart");

	ScrollPanel scroll = new ScrollPanel();
	initWidget(scroll);
	scroll.setSize("99%", "100%");

	flowPanel = new FlowPanel();
	flowPanel.setSize("100%", "100%");
	// initWidget(flowPanel);

	scroll.add(flowPanel);
    }

    public void addChart(EvaluationChart c) {
	flowPanel.add(c);
    }

    public void addRes(ArrayList<String> res) {
	toDraw.add(res);
    }

    public void drawCharts() {

	if (lastChart > 0) {
	    flowPanel.remove(next);
	}

	int maxChart = toDraw.size();//lastChart+8;
	
	// ok we go 8 by 8
	boolean first = false;
	if (lastChart == 0)
	    first = true;
	while (lastChart < toDraw.size() && lastChart < maxChart) {
	    EvaluationChart chart = new EvaluationChart(toDraw.get(lastChart), first);
	    chart.setVisible(true);
	    addChart(chart);
	    first = false;
	    lastChart++;
	}

	if (lastChart < toDraw.size() - 1) {
	    flowPanel.add(next);
	}

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
