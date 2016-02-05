package org.inria.websmatch.gwt.spreadsheet.client.composites;

import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.ConnexCompEditor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.NumberLabel;

public class ImprovedIntegerLabel extends Composite {

    private NumberLabel<Integer> numberLabel;
    private int originalValue;

    public ImprovedIntegerLabel(int originalValue, final ConnexCompEditor editor) {

	this.setOriginalValue(originalValue);

	FlowPanel fp = new FlowPanel();

	initWidget(fp);

	FlexTable flexTable = new FlexTable();
	fp.add(flexTable);

	numberLabel = new NumberLabel<Integer>();
	numberLabel.setWidth("50px");
	numberLabel.setValue(originalValue);

	Image plus = new Image();
	plus.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		getNumberLabel().setValue(getNumberLabel().getValue() + 1);
		editor.refresh();
	    }
	});
	plus.setResource(GfxRessources.INSTANCE.plus());

	Image moins = new Image();
	moins.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		if (getNumberLabel().getValue() > 0) {
		    getNumberLabel().setValue(getNumberLabel().getValue() - 1);	
		    editor.refresh();
		}
	    }
	});
	moins.setResource(GfxRessources.INSTANCE.moins());

	flexTable.setWidget(0, 1, numberLabel);
	flexTable.setWidget(0, 2, plus);
	flexTable.setWidget(0, 0, moins);
	flexTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
	flexTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
    }

    public int getValue() {
	return numberLabel.getValue();
    }

    public NumberLabel<Integer> getNumberLabel() {
	return numberLabel;
    }

    public void setNumberLabel(NumberLabel<Integer> numberLabel) {
	this.numberLabel = numberLabel;
    }

    public void setOriginalValue(int originalValue) {
	this.originalValue = originalValue;
    }

    public int getOriginalValue() {
	return originalValue;
    }

}
