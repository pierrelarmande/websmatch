package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

public class WaitingPopup extends PopupPanel {

    private Image ajaxImage;
    private GfxRessources gfxRes = GfxRessources.INSTANCE;
    
    private static WaitingPopup instance;

    public static WaitingPopup getInstance() {
	if (null == instance) {
	    instance = new WaitingPopup();
	}
	return instance;
    }

    private WaitingPopup() {
	super(false);

	ajaxImage = new Image(gfxRes.loader());

	setGlassEnabled(true);
	center();

	final Grid grid = new Grid(1, 1);
	grid.setWidget(0, 0, ajaxImage);
	add(grid);
	
	addStyleName("onTop");
    }

    @Override
    public void hide() {
	if (this != null && this.isShowing())
	    super.hide();
    }

}
