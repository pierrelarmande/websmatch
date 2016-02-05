package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DetailsPopup extends PopupPanel {

    public DetailsPopup() {
	super(true);
	
	VerticalPanel verticalPanel = new VerticalPanel();
	setWidget(verticalPanel);
	verticalPanel.setSize("100%", "100%");
	
	CaptionPanel cptnpnlNewPanel = new CaptionPanel("Detected as");
	cptnpnlNewPanel.setCaptionHTML("<b>Detected as</b>");
	verticalPanel.add(cptnpnlNewPanel);
	
	Label lblNewLabel = new Label("geo:location");
	
	SimpleCheckBox simpleCheckBox = new SimpleCheckBox();
	simpleCheckBox.setValue(true);
	
	Label lblNewLabel_1 = new Label("time:year");
	
	SimpleCheckBox simpleCheckBox_1 = new SimpleCheckBox();
	simpleCheckBox_1.setValue(false);
	
	Label lblNewLabel_2 = new Label("quantity:ratio");
	
	SimpleCheckBox simpleCheckBox_2 = new SimpleCheckBox();
	simpleCheckBox_2.setValue(false);
	
	FlexTable flexTable = new FlexTable();
	cptnpnlNewPanel.setContentWidget(flexTable);
	flexTable.setSize("5cm", "3cm");
	
	flexTable.setWidget(0, 0, lblNewLabel);
	flexTable.setWidget(0, 1, simpleCheckBox);
	flexTable.setWidget(1, 0, lblNewLabel_1);
	flexTable.setWidget(1, 1, simpleCheckBox_1);
	flexTable.setWidget(2, 0, lblNewLabel_2);
	flexTable.setWidget(2, 1, simpleCheckBox_2);
    }

}
