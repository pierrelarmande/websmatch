package org.inria.websmatch.models;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.table.DefaultTableModel;

import jxl.Cell;

public class ShowTableModel extends DefaultTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6103903897876796550L;
	
	private String title;

	public ShowTableModel(String name){
		super();
		this.setTitle(name);
	}
	
	public ShowTableModel(String name, List<Cell[]> list, ArrayList<Cell> attribCells){
		this(name);
		
		ListIterator<Cell[]> it = list.listIterator();
								
		while(it.hasNext()){
			
			Cell[] row = it.next();
			
			while(row.length > this.getColumnCount()) this.addColumn(new String());
			
			Object[] rowContent = new Object[row.length];	
									
			for(int i = 0;i<rowContent.length;i++){
				
				//if that's an attribute, we had the row instead of the string		
				if(attribCells.contains(row[i])) rowContent[i] = row[i];
				else rowContent[i] = row[i].getContents();
				
			}
			
			this.addRow(rowContent);
			
		}
		
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		
		return false;
		
	}

	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

}
