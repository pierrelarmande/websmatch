package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import com.google.gwt.user.client.ui.TreeItem;

public class SimpleEntityItem extends TreeItem {
    
    private int id = -1;
    private int sheetIndex = -1;
    private int startX = -1;
    private int startY = -1;
    
    public SimpleEntityItem(String name, int id, int sheetIndex, int startX, int startY){
	
	super();
	this.setText(name);
	this.id = id;
	this.setSheetIndex(sheetIndex);
	this.setStartX(startX);
	this.setStartY(startY);
	
    }
    
    public String toString(){
	return "Entity : "+this.getText()+ " SheetIndex : "+this.getSheetIndex()+ " CC startX : "+this.getStartX()+ " CC startY : "+ this.getStartY();
    }
    
    public boolean equals(Object o){
	
	if (this == o) {
	    return true;
	}
	if (o == null || getClass() != o.getClass()) {
	    return false;
	}
	
	if (this.id == ((SimpleEntityItem) o).id)
	    return true;
	else
	    return false;
	
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSheetIndex(int sheetIndex) {
	this.sheetIndex = sheetIndex;
    }

    public int getSheetIndex() {
	return sheetIndex;
    }

    public void setStartX(int startX) {
	this.startX = startX;
    }

    public int getStartX() {
	return startX;
    }

    public void setStartY(int startY) {
	this.startY = startY;
    }

    public int getStartY() {
	return startY;
    }

}
