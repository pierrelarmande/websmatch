package org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup;

import org.inria.websmatch.gwt.spreadsheet.client.composites.SchemaCellTreeComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SchemaCellTreeComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;

public class BidimEditorPopup extends PopupPanel {
    
    private String[] concepts = {"geo:location","time:year"};
    // private String[] concepts = {"geo:location","geo:countries","geo:regions","geo:cities","geo:departments","time","time:year"};

    public BidimEditorPopup(int x, int y, final ConnexComposant cc, final SimpleCell cell, final Element ele, final SchemaCellTreeComposite tree) {
	super(false);

	this.setPopupPosition(x, y);
	
	LayoutPanel layoutPanel = new LayoutPanel();
	setWidget(layoutPanel);
	layoutPanel.setSize("293px", "119px");

	Button cancelButton = new Button("Cancel");
	cancelButton.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		hide();
	    }
	});
	
	FlexTable flexTable = new FlexTable();
	layoutPanel.add(flexTable);
	layoutPanel.setWidgetLeftWidth(flexTable, 0.0, Unit.PX, 304.0, Unit.PX);
	layoutPanel.setWidgetTopHeight(flexTable, 0.0, Unit.PX, 130.0, Unit.PX);
	
	Label lblColumnConcept = new Label("Column concept");
	flexTable.setWidget(0, 0, lblColumnConcept);
	
	final ListBox listBox = new ListBox();
	flexTable.setWidget(0, 1, listBox);
	listBox.setSize("100%", "100%");
	listBox.setVisibleItemCount(1);
			
	Label lblLineConcept = new Label("Line concept");
	flexTable.setWidget(1, 0, lblLineConcept);
	
	final ListBox listBox_1 = new ListBox();
	flexTable.setWidget(1, 1, listBox_1);
	listBox_1.setSize("100%", "100%");
	listBox_1.setVisibleItemCount(1);
	
	Label lblValueName = new Label("Value name");
	flexTable.setWidget(2, 0, lblValueName);
	
	final TextBox textBox = new TextBox();
	if(!cc.getValueName().equals("")) textBox.setText(cc.getValueName());
	flexTable.setWidget(2, 1, textBox);
	textBox.setHeight("100%");
	
	// fill the lists
	for(String concept : concepts){
	    listBox.addItem(concept);
	    listBox_1.addItem(concept);
	}
	
	cancelButton.setText("Cancel");
	layoutPanel.add(cancelButton);
	layoutPanel.setWidgetLeftWidth(cancelButton, 0.0, Unit.PX, 180.0, Unit.PX);
	layoutPanel.setWidgetBottomHeight(cancelButton, 4.0, Unit.PX, 32.0, Unit.PX);
	cancelButton.setSize("30%", "32px");

	Button finishButton = new Button("Finish");
	finishButton.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		// set bidim
		cell.setAttribute(true);
		ele.setClassName("attributeCell");
		cc.setBiDimensionnalArray(true);
		cc.setValueName(textBox.getText());
		
		// change content
		cell.setEditedContent(listBox.getItemText(listBox.getSelectedIndex())+"\\"+listBox_1.getItemText(listBox_1.getSelectedIndex()));
		ele.setInnerText(listBox.getItemText(listBox.getSelectedIndex())+"\\"+listBox_1.getItemText(listBox_1.getSelectedIndex()));
		
		tree.updateElement(cell);
		
		hide();
	    }
	});
	layoutPanel.add(finishButton);
	layoutPanel.setWidgetLeftWidth(finishButton, 238.0, Unit.PX, 180.0, Unit.PX);
	layoutPanel.setWidgetBottomHeight(finishButton, 4.0, Unit.PX, 32.0, Unit.PX);
	finishButton.setSize("30%", "32px");
    }
}
