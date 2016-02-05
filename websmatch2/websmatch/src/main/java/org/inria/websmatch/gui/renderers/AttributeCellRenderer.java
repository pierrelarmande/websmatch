package org.inria.websmatch.gui.renderers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import jxl.Cell;

public class AttributeCellRenderer extends DefaultTableCellRenderer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6870931168609066257L;

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int col) {

			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			if(value instanceof Cell){
				this.setBackground(Color.YELLOW);
				this.setText(((Cell)value).getContents());
			}
			else this.setBackground(Color.WHITE);
			return this;
		}
}
