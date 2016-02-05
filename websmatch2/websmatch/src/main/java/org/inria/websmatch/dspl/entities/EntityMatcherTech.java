package org.inria.websmatch.dspl.entities;

import java.util.ArrayList;

import jxl.Cell;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

public interface EntityMatcherTech {
    
    public SimpleCell match(SimpleCell attributeCell, ArrayList<Cell> toMatchWith);

}
