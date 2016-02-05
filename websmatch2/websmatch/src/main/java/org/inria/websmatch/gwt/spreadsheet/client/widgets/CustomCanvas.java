package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.composites.MainFrame;
import org.inria.websmatch.gwt.spreadsheet.client.handlers.ClusterCanvasMouseClickHandler;

import com.blogspot.qbeukes.gwt.html5canvas.client.HTML5Canvas;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class CustomCanvas extends HTML5Canvas {

    private ArrayList<int[]> xydiam;
    private ArrayList<String> names;
    private ArrayList<String> ids;
    
    private MainFrame frame = null;
    
    public CustomCanvas(int w, int h) {
	super(w, h);
	xydiam = new ArrayList<int[]>();
	names = new ArrayList<String>();
	ids = new ArrayList<String>();
    }

    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
	return addDomHandler(handler, MouseMoveEvent.getType());
    }
    
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
	return addDomHandler(handler, MouseDownEvent.getType());
    }
    
    public void addNode(int[] xydiam, String name, String id) {
	this.xydiam.add(xydiam);
	this.names.add(name);
	this.ids.add(id);
	// System.out.println("Name : "+name+" Id : "+id);
    }

    public String getNodeAt(int x, int y) {

	String res = null;

	for (int i = 0; i < xydiam.size(); i++) {
	    int[] xyd = xydiam.get(i);
	    
	    int dx = x - xyd[0];
	    int dy = y - xyd[1];
	    
	    if( (dx*dx+dy*dy) <= (xyd[2] / 2)*(xyd[2] / 2) ) return names.get(i);

	    /*if (((xyd[0] - xyd[2] / 2) <= x && x <= (xyd[0] + xyd[2] / 2)) && ((xyd[1] - xyd[2] / 2) <= y && y <= (xyd[1] + xyd[2] / 2))){
		return names.get(i);
	    }*/
	}

	return res;
    }
    
    public String getNodeIdAt(int x, int y) {

	String res = null;

	for (int i = 0; i < xydiam.size(); i++) {
	    int[] xyd = xydiam.get(i);

	    int dx = x - xyd[0];
	    int dy = y - xyd[1];
	    
	    if( (dx*dx+dy*dy) <= (xyd[2] / 2)*(xyd[2] / 2) ) return ids.get(i);
	    
	    /*if (((xyd[0] - xyd[2] / 2) <= x && x <= (xyd[0] + xyd[2] / 2)) && ((xyd[1] - xyd[2] / 2) <= y && y <= (xyd[1] + xyd[2] / 2))){
		return ids.get(i);
	    }*/
	}

	return res;
    }

    public void setFrame(MainFrame frame) {
	this.frame = frame;
	ClusterCanvasMouseClickHandler handler = new ClusterCanvasMouseClickHandler();
	handler.setFrame(frame);
	this.addMouseDownHandler(handler);
    }

    public MainFrame getFrame() {
	return frame;
    }

}
