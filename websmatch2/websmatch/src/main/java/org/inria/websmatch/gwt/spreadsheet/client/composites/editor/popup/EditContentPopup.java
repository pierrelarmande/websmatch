package org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup;

import org.inria.websmatch.gwt.spreadsheet.client.composites.SchemaCellTreeComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SchemaCellTreeComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

public class EditContentPopup extends PopupPanel {

    public EditContentPopup(final SimpleCell cell, final Element elem, final SchemaCellTreeComposite tree) {
	super(false);
	
	LayoutPanel layoutPanel = new LayoutPanel();
	setWidget(layoutPanel);
	layoutPanel.setSize("304px", "181px");
	
	final TextArea textArea = new TextArea();
	if(!cell.getEditedContent().equals("")) textArea.setText(cell.getEditedContent());
	else textArea.setText(cell.getContent());
	textArea.setAlignment(TextAlignment.JUSTIFY);
	textArea.setTitle("Edit the cell content");
	layoutPanel.add(textArea);
	layoutPanel.setWidgetTopHeight(textArea, 0.0, Unit.PX, 132.0, Unit.PX);
	layoutPanel.setWidgetLeftRight(textArea, 0.0, Unit.PX, 8.0, Unit.PX);
	textArea.setSize("98%", "90%");
	
	Button cancelButton = new Button("Cancel");
	cancelButton.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
		    hide();
		}
	});
	cancelButton.setText("Cancel");
	layoutPanel.add(cancelButton);
	layoutPanel.setWidgetLeftWidth(cancelButton, 0.0, Unit.PX, 180.0, Unit.PX);
	layoutPanel.setWidgetTopHeight(cancelButton, 141.0, Unit.PX, 32.0, Unit.PX);
	cancelButton.setSize("30%", "32px");
	
	Button finishButton = new Button("Finish");
	finishButton.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
		    cell.setEditedContent(textArea.getText());
		    if(!textArea.getText().equals("")) elem.setInnerText(textArea.getText());
		    hide();		    
		    if(cell.isAttribute()) tree.updateElement(cell);
		    
		}
	});
	layoutPanel.add(finishButton);
	layoutPanel.setWidgetLeftWidth(finishButton, 242.0, Unit.PX, 180.0, Unit.PX);
	layoutPanel.setWidgetTopHeight(finishButton, 141.0, Unit.PX, 32.0, Unit.PX);
	finishButton.setSize("30%", "32px");
	
	Button btnRestoreInitialValue = new Button("Restore initial value");
	btnRestoreInitialValue.addClickHandler(new ClickHandler() {
		public void onClick(ClickEvent event) {
		    textArea.setText(cell.getContent());
		    cell.setEditedContent("");
		}
	});
	layoutPanel.add(btnRestoreInitialValue);
	layoutPanel.setWidgetLeftWidth(btnRestoreInitialValue, 60.0, Unit.PX, 176.0, Unit.PX);
	layoutPanel.setWidgetTopHeight(btnRestoreInitialValue, 141.0, Unit.PX, 32.0, Unit.PX);
    }
}
