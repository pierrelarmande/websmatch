package org.inria.websmatch.gwt.spreadsheet.client.handlers;

import org.inria.websmatch.gwt.spreadsheet.client.widgets.CustomCanvas;

import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class CanvasMouseOverHandler implements MouseMoveHandler {
    
    private PopupPanel pop = new PopupPanel(true);
    private String lastTitle = null;

    @Override
    public void onMouseMove(final MouseMoveEvent moe) {
	CustomCanvas widget = (CustomCanvas) moe.getSource();
	int x = moe.getX();
	int y = moe.getY();
	String title = widget.getNodeAt(x, y);
	if(pop != null && pop.isShowing() && title == null){
	    pop.hide();
	    lastTitle = null;
	}
	if(title != null && (lastTitle == null || !lastTitle.equals(title))){
	    if(pop.isShowing()) pop.hide();
	    pop = new PopupPanel(true);
	    pop.setGlassEnabled(false);
	    pop.setPopupPosition(moe.getClientX()+20, moe.getClientY());
	    pop.add(new Label(title));
	    pop.setAutoHideEnabled(true);
	    pop.show();
	    lastTitle = title;
	}
    }

}
