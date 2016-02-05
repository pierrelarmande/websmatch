package org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup;

import org.inria.websmatch.gwt.spreadsheet.client.GetLoadersService;
import org.inria.websmatch.gwt.spreadsheet.client.GetLoadersServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SchemaCellTreeComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.WaitingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

public class ReplaceScriptPopup extends PopupPanel {

    public ReplaceScriptPopup(final SimpleCell cell, final Element elem, final SchemaCellTreeComposite tree, final int x, final int y) {

	super(false);

	final LayoutPanel layoutPanel = new LayoutPanel();
	setWidget(layoutPanel);
	layoutPanel.setSize("304px", "181px");

	final TextArea textArea = new TextArea();
	textArea.setText(cell.getEngineScript());
	textArea.setAlignment(TextAlignment.JUSTIFY);
	textArea.setTitle("Choose the new value");

	final ListBox combo = new ListBox();

	if (cell.getCurrentDsplMeta().equals("dp:pays") || cell.getCurrentDsplMeta().equals("dp:region") || cell.getCurrentDsplMeta().equals("dp:departement")
		|| cell.getCurrentDsplMeta().equals("dp:commune")) {

	    layoutPanel.setSize("304px", "60px");
	    
	    final GetLoadersServiceAsync service = (GetLoadersServiceAsync) GWT.create(GetLoadersService.class);

	    WaitingPopup.getInstance().show();

	    final ReplaceScriptPopup pop = this;

	    service.getLoaders(cell.getCurrentDsplMeta(), new AsyncCallback<String[]>() {

		@Override
		public void onFailure(Throwable caught) {
		    WaitingPopup.getInstance().hide();
		    showUIEnd(cell, elem, tree, layoutPanel, textArea, x, y);
		    Window.alert(caught.getMessage());
		}

		@Override
		public void onSuccess(String[] result) {
		    
		    for (String v : result) {
			if(cell.getCurrentDsplMeta().trim().equals("dp:pays")) combo.addItem(v);
			else combo.addItem(v);
		    }
		    layoutPanel.add(combo);

		    WaitingPopup.getInstance().hide();

		    layoutPanel.setWidgetTopHeight(combo, 0.0, Unit.PX, 132.0, Unit.PX);
		    layoutPanel.setWidgetLeftRight(combo, 0.0, Unit.PX, 8.0, Unit.PX);
		    combo.setSize("99%", "24px");

		    Button cancelButton = new Button("Cancel");
		    cancelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
			    hide();
			}
		    });
		    cancelButton.setText("Cancel");
		    layoutPanel.add(cancelButton);
		    layoutPanel.setWidgetLeftWidth(cancelButton, 0.0, Unit.PX, 180.0, Unit.PX);
		    layoutPanel.setWidgetTopHeight(cancelButton, 28.0, Unit.PX, 32.0, Unit.PX);
		    cancelButton.setSize("30%", "32px");
		    Button finishButton = new Button("Finish");
		    finishButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

			    if (cell.getContent() != null)
				cell.setEngineScript("formatter.str().replace('" + cell.getContent().trim() + "','"
					+ combo.getItemText(combo.getSelectedIndex()) + "')");
			    else
				cell.setEngineScript("formatter.str().replace('','" + combo.getItemText(combo.getSelectedIndex()) + "')");
			    //
			    cell.setEditedContent(combo.getItemText(combo.getSelectedIndex()));
			    elem.setInnerText(combo.getItemText(combo.getSelectedIndex()));
			    elem.getParentElement().removeClassName("errorCell");
			    elem.removeClassName("errorCell");

			    if (cell.isAttribute())
				tree.updateElement(cell);
			    //
			    hide();
			}
		    });
		    layoutPanel.add(finishButton);
		    layoutPanel.setWidgetLeftWidth(finishButton, 242.0, Unit.PX, 180.0, Unit.PX);
		    layoutPanel.setWidgetTopHeight(finishButton, 28.0, Unit.PX, 32.0, Unit.PX);
		    finishButton.setSize("30%", "32px");

		    pop.setGlassEnabled(true);
		    if (x > 100)
			pop.setPopupPosition(x - 200, y + 20);
		    else
			pop.setPopupPosition(x, y + 20);

		    pop.show();
		}
	    });
	}

	else {
	    showUIEnd(cell, elem, tree, layoutPanel, textArea, x, y);
	}
    }

    private void showUIEnd(final SimpleCell cell, final Element elem, final SchemaCellTreeComposite tree, final LayoutPanel layoutPanel,
	    final TextArea textArea, final int x, final int y) {
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

		if (cell.getContent() != null)
		    cell.setEngineScript("formatter.str().replace('" + cell.getContent().trim() + "','" + textArea.getText() + "')");
		else
		    cell.setEngineScript("formatter.str().replace('','" + textArea.getText() + "')");
		//
		cell.setEditedContent(textArea.getText());
		if (!textArea.getText().equals("")) {
		    elem.setInnerText(textArea.getText());
		    elem.getParentElement().removeClassName("errorCell");
		}

		if (cell.isAttribute())
		    tree.updateElement(cell);
		//
		hide();
	    }
	});
	layoutPanel.add(finishButton);
	layoutPanel.setWidgetLeftWidth(finishButton, 242.0, Unit.PX, 180.0, Unit.PX);
	layoutPanel.setWidgetTopHeight(finishButton, 138.0, Unit.PX, 32.0, Unit.PX);
	finishButton.setSize("30%", "32px");

	this.setGlassEnabled(true);
	if (x > 100)
	    this.setPopupPosition(x - 200, y + 20);
	else
	    this.setPopupPosition(x, y + 20);

	this.show();
    }
}
