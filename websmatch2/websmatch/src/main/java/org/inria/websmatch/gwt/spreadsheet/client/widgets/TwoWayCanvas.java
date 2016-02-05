package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.composites.TwoWayViewer;
import org.inria.websmatch.gwt.spreadsheet.client.handlers.TwoWayMouseClickHandler;
import org.inria.websmatch.gwt.spreadsheet.client.handlers.TwoWayMouseOverHandler;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingResult;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleVertex;

import com.blogspot.qbeukes.gwt.html5canvas.client.HTML5Canvas;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.widgetideas.graphics.client.Color;
import org.inria.websmatch.gwt.spreadsheet.client.handlers.TwoWayMouseOverHandler;

@SuppressWarnings("deprecation")
public class TwoWayCanvas extends HTML5Canvas {

    private ArrayList<int[]> xydiam;
    private ArrayList<MatchingResult> results;
    
    // needed to refresh
    private TwoWayViewer viewer = null;

    // store the vertices clickable
    // private TreeMap<Integer,TreeMap<Integer,SimpleVertex>> clickableElements;

    // as navigablemap is not implemented in GWT Java emulation, I use the old
    // way
    private SimpleVertex[][] clickableElementsMap;

    public TwoWayCanvas(int w, int h) {
	super(w, h);
	xydiam = new ArrayList<int[]>();
	results = new ArrayList<MatchingResult>();

	// clickableElements = new
	// TreeMap<Integer,TreeMap<Integer,SimpleVertex>>();
	clickableElementsMap = new SimpleVertex[h][w];

	this.addMouseMoveHandler(new TwoWayMouseOverHandler());
	this.addMouseDownHandler(new TwoWayMouseClickHandler());
    }

    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
	return addDomHandler(handler, MouseMoveEvent.getType());
    }

    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return addDomHandler(handler, MouseDownEvent.getType());
    }

    public void addClickableElement(int x, int y, SimpleVertex vertex) {
	/*
	 * if(clickableElements.containsKey(x)){ clickableElements.get(x).put(y,
	 * vertex); }else{ TreeMap<Integer,SimpleVertex> tmpMap = new
	 * TreeMap<Integer,SimpleVertex>(); tmpMap.put(y, vertex);
	 * clickableElements.put(x, tmpMap); }
	 */

	// System.out.println("Add vertex : "+vertex.getName()+" x : "+x+" y : "+y);
	
	// add on the map
	if (y < clickableElementsMap.length) {
	    if (x < clickableElementsMap[y].length) {
		clickableElementsMap[y][x] = vertex;
	    }
	}

    }

    public SimpleVertex getClickableElementAt(int x, int y) {

	/*
	 * if(clickableElements.ceilingKey(new Integer(x)) != null){
	 * TreeMap<Integer,SimpleVertex> tmpMap =
	 * clickableElements.get(clickableElements.ceilingKey(new Integer(x)));
	 * if(tmpMap.ceilingKey(new Integer(y)) != null){ return
	 * tmpMap.get(tmpMap.ceilingKey(new Integer(y))); } }
	 * 
	 * return null;
	 */

	return clickableElementsMap[y][x];

    }

    public void addNode(int[] xydiam, MatchingResult res) {
	this.xydiam.add(xydiam);
	this.results.add(res);
    }

    public MatchingResult getNodeAt(int x, int y) {

	MatchingResult res = null;

	for (int i = 0; i < xydiam.size(); i++) {
	    int[] xyd = xydiam.get(i);

	    if (((xyd[0] - 5) <= x && x <= (xyd[0] + 5)) && ((xyd[1] - 5) <= y && y <= (xyd[1] + 5))) {
		return results.get(i);
	    }
	}

	return res;
    }
    
    public void fillGraphCompText(String text, double x, double y, boolean rtl, Color rectColor) {

	if (text == null)
	    text = new String();

	text = text.trim();
	double size = this.getTextWidth(text);

	this.saveContext();
	this.removeShadow();
	this.setGlobalAlpha(1.0);
	this.setFillStyle(rectColor);
	if (!rtl) {
	    this.fillRect(x + 10 - 1, y - 5, size + 2, 12);
	    this.setFillStyle(Color.BLACK);
	    this.fillText(text, x + 10, y + 5);
	} else {
	    this.fillRect(x - size - 10 - 1, y - 5, size + 2, 12);
	    this.setFillStyle(Color.BLACK);
	    this.fillText(text, x - size - 10, y + 5);
	}
	this.restoreContext();
    }

    public void setViewer(TwoWayViewer viewer) {
	this.viewer = viewer;
    }

    public TwoWayViewer getViewer() {
	return viewer;
    }


}
