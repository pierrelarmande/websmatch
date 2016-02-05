package org.inria.websmatch.dspl;

import java.util.LinkedList;

public class DSPLSlice {
    
    private String fileName;
    private LinkedList<DSPLColumn> columns;
    // will stat from 1
    private int sheetNumber = -1;
    private int tableSheetNumber = -1;
    private String sheetName = "";
    // needed for DSPLEngine
    private boolean inLineAttr = false;
    private boolean biDim = false;
    private int ccEndX = -1;
    private int ccEndY = -1;
    
    public DSPLSlice(){
	setFileName("");
	setColumns(new LinkedList<DSPLColumn>());
    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    public String getFileName() {
	return fileName;
    }

    public void setColumns(LinkedList<DSPLColumn> columns) {
	this.columns = columns;
    }

    public LinkedList<DSPLColumn> getColumns() {
	return columns;
    }
    
    public void addColumn(DSPLColumn column){
	this.columns.add(column);
    }

    public void setSheetNumber(int sheetNumber) {
	this.sheetNumber = sheetNumber;
    }

    public int getSheetNumber() {
	return sheetNumber;
    }

    public void setTableSheetNumber(int tableSheetNumber) {
	this.tableSheetNumber = tableSheetNumber;
    }

    public int getTableSheetNumber() {
	return tableSheetNumber;
    }

    public boolean isInLineAttr() {
        return inLineAttr;
    }

    public void setInLineAttr(boolean inLineAttr) {
        this.inLineAttr = inLineAttr;
    }

    public boolean isBiDim() {
        return biDim;
    }

    public void setBiDim(boolean biDim) {
        this.biDim = biDim;
    }

    public void setCcEndX(int ccEndX) {
	this.ccEndX = ccEndX;
    }

    public int getCcEndX() {
	return ccEndX;
    }

    public void setCcEndY(int ccEndY) {
	this.ccEndY = ccEndY;
    }

    public int getCcEndY() {
	return ccEndY;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

}
