package org.mitre.schemastore.model;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

/**
 * Not used for now.
 * May be usefull for storing the cell (and keep the Cell to Attr conversion)
 * 
 */

public class AttributeCell extends Attribute {

    /**
     * 
     */
    private static final long serialVersionUID = 3465513673731317970L;
    
    private SimpleCell cell;
    
    public AttributeCell(SimpleCell cell){
	
	super();
	this.setCell(cell);
	
    }

    public void setCell(SimpleCell cell) {
	this.cell = cell;
    }

    public SimpleCell getCell() {
	return cell;
    }

}
