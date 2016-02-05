package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

import com.google.gwt.user.client.ui.TreeItem;

public class SimpleCellItem extends TreeItem {
    
    private int sheetIndex = -1;
    private int rowIndex = -1;
    private int colIndex = -1;
    
    private SimpleCell cell;
    
    public SimpleCellItem(SimpleCell cell){
	super();
	if(!cell.getEditedContent().equals("") && !cell.getEditedContent().equals(cell.getContent())){
	    this.setText(cell.getEditedContent());
	}
	else this.setText(cell.getContent());
	this.setTitle("Attribute content : "+cell.getContent()+" Sheet index : "+cell.getSheet()+" Row : "+cell.getJxlRow()+" Column : "+cell.getJxlCol());
	this.setSheetIndex(cell.getSheet());
	this.setRowIndex(cell.getJxlRow());
	this.setColIndex(cell.getJxlCol());
	
	this.cell = cell;
    }

    public void setSheetIndex(int sheetIndex) {
	this.sheetIndex = sheetIndex;
    }

    public int getSheetIndex() {
	return sheetIndex;
    }

    public void setRowIndex(int rowIndex) {
	this.rowIndex = rowIndex;
    }

    public int getRowIndex() {
	return rowIndex;
    }

    public void setColIndex(int colIndex) {
	this.colIndex = colIndex;
    }

    public int getColIndex() {
	return colIndex;
    }

    public SimpleCell getCell() {
        return cell;
    }

    public void setCell(SimpleCell cell) {
        this.cell = cell;
    }

}
