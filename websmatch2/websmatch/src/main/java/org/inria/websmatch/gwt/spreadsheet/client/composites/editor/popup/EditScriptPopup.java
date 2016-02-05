package org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

public class EditScriptPopup  extends PopupPanel {

    public EditScriptPopup(final SimpleCell cell) {
	super(false);
	
	LayoutPanel layoutPanel = new LayoutPanel();
	setWidget(layoutPanel);
	layoutPanel.setSize("304px", "181px");
	
	final TextArea textArea = new TextArea();
	textArea.setText(cell.getEngineScript());
	textArea.setAlignment(TextAlignment.JUSTIFY);
	textArea.setTitle("Edit the dspl engine script");
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
		    cell.setEngineScript(textArea.getText());
		    hide();
		}
	});
	layoutPanel.add(finishButton);
	layoutPanel.setWidgetLeftWidth(finishButton, 242.0, Unit.PX, 180.0, Unit.PX);
	layoutPanel.setWidgetTopHeight(finishButton, 138.0, Unit.PX, 32.0, Unit.PX);
	finishButton.setSize("30%", "32px");

    }

}
