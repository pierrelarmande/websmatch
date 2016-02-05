package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.composites.MainFrame;
import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class DetectionQualityPopup extends PopupPanel {

    public DetectionQualityPopup(List<DetectionQualityData> list, MainFrame frame) {
	super(true);
	
	setGlassEnabled(true);
	setAnimationEnabled(true);
	setPopupPosition(100, 100);

	DetectionQualityCellTable table = new DetectionQualityCellTable(list, frame);

	ScrollPanel scroll = new ScrollPanel(table);
	scroll.setWidth("100%");
	table.setWidth("100%");

	// scroll.setAlwaysShowScrollBars(true);

	add(scroll);
	if (table.getRowCount() > 20)
	    setSize("1100px", "700px");
	else {
	    setSize("1100px", "200px");
	}

	show();
    }

}
