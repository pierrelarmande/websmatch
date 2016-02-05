package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;

import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

/**
 * CellTable without mouse over
 */

public class NoMouseOverCellTable<T> extends com.google.gwt.user.cellview.client.CellTable<T> {

    protected Element lastElement;

    public NoMouseOverCellTable() {
	super();
    }

    public NoMouseOverCellTable(int rowCount, CellTable.Resources resources) {
	super(rowCount, resources);
    }

    @Override
    protected void onBrowserEvent2(Event event) {
	
	EventTarget eventTarget = event.getEventTarget();
	if (!Element.is(eventTarget)) {
	    return;
	}

	Element currentElement = eventTarget.cast();	

	// Find the cell where the event occurred.
	TableCellElement tableCell = findNearestParentCell(currentElement);
	if (tableCell == null) {
	    return;
	}

	Element trElem = tableCell.getParentElement();
	if (trElem == null) {
	    return;
	}
	
	TableRowElement tr = TableRowElement.as(trElem);
	Element sectionElem = tr.getParentElement();
	if (sectionElem == null) {
	    return;
	}
	
	int x = tableCell.getCellIndex();
	//TODO removed the -1 glitch
	int y = tr.getSectionRowIndex();
	//
	// SimpleCell test = ((SpreadsheetEditor) this).getCells()[y][x];
	// System.out.println(test.getContent());

	// it's an element so test if we ignore the mouseover
	// if not setTitle
	if (((lastElement != null && !lastElement.equals(currentElement)) || lastElement == null) && ((SpreadsheetEditor) this).getCells().length > y
		&& ((SpreadsheetEditor) this).getCells()[y].length > x && ((SpreadsheetEditor) this).getCells()[y][x] != null) {
	    SimpleCell simpleCell = ((SpreadsheetEditor) this).getCells()[y][x];
	    if (simpleCell != null) {
		
		String tTip = new String();

		if (simpleCell.getEditedDescription() != null) {
		    tTip = "Description : " + simpleCell.getEditedDescription();
		}
		if (!simpleCell.getEditedContent().equals(simpleCell.getContent())) {
		    tTip = "Original content : " + simpleCell.getContent() + "   " + tTip;
		}

		// add data infos on tTip
		if (simpleCell.isAttribute()) {
		    ArrayList<ConnexComposant> ccs = ((SpreadsheetEditor) this).getSheet().getConnexComps();
		    for (ConnexComposant cc : ccs) {
			if (cc.containsPoint(simpleCell.getJxlCol(), simpleCell.getJxlRow())) {
			    tTip += "Values : {";
			    // attr in lines
			    if (cc.isAttrInLines()) {
				for (int col = simpleCell.getJxlCol() + 1; col <= cc.getEndX(); col++) {
				    if (((SpreadsheetEditor) this).getSheet().getCells().length > simpleCell.getJxlRow()
					    && ((SpreadsheetEditor) this).getSheet().getCells()[simpleCell.getJxlRow()].length > col) {
					tTip += ((SpreadsheetEditor) this).getSheet().getCells()[simpleCell.getJxlRow()][col].getContent();
					if (col == cc.getEndX())
					    tTip += "}";
					else
					    tTip += ", ";
				    }
				}
			    }
			    // attr in cols
			    else {
				for (int row = simpleCell.getJxlRow() + 1; row <= cc.getEndY(); row++) {
				    if (((SpreadsheetEditor) this).getSheet().getCells().length > row
					    && ((SpreadsheetEditor) this).getSheet().getCells()[row].length > simpleCell.getJxlCol()) {
					tTip += ((SpreadsheetEditor) this).getSheet().getCells()[row][simpleCell.getJxlCol()].getContent();
					if (row == cc.getEndY())
					    tTip += "}";
					else
					    tTip += ", ";
				    }
				}
			    }
			}
		    }
		}
		//
		
		this.setTitle(tTip);
	    }
	    lastElement = currentElement;
	}
	
	// Ignore mouseover
	switch (DOM.eventGetType(event)) {
	case Event.ONMOUSEOVER:
	case Event.ONMOUSEOUT:
	    return;
	default:
	    super.onBrowserEvent2(event);
	}

    }

    protected TableCellElement findNearestParentCell(Element elem) {
	while ((elem != null)) {
	    String tagName = elem.getTagName();
	    if ("td".equalsIgnoreCase(tagName) || "th".equalsIgnoreCase(tagName)) {
		return elem.cast();
	    }
	    elem = elem.getParentElement();
	}
	return null;
    }

}
