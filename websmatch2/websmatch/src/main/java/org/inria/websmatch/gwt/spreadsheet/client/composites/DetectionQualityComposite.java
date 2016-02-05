package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.DetectionQualityCellTable;

import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class DetectionQualityComposite extends Composite {

    public DetectionQualityComposite(List<DetectionQualityData> result, MainFrame mainF) {
	FlowPanel panel = new FlowPanel();
	initWidget(panel);

	SimplePager pager = new SimplePager(SimplePager.TextLocation.CENTER);
	pager.setWidth("100%");

	DetectionQualityCellTable table = new DetectionQualityCellTable(result, mainF);
	pager.setDisplay(table);

	ScrollPanel scroll = new ScrollPanel(table);
	scroll.setHeight(mainF.getOffsetHeight()-110+"px");

	panel.add(pager);
	panel.add(scroll);
    }
}
