package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.DistanceData;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;

public class DistancesCellTable extends CellTable<DistanceData> {

    public DistancesCellTable() {
	super();
	
	// Create name column.
	TextColumn<DistanceData> nameColumn = new TextColumn<DistanceData>() {
	    public String getValue(DistanceData sch) {		
		return sch.getName();
	    }	   
	};

	TextColumn<DistanceData> distanceColumn = new TextColumn<DistanceData>() {
	    public String getValue(DistanceData sch) {		
		    return new Double(sch.getDist()).toString();
	    }	
	};

	this.addColumn(nameColumn, "Name");
	this.addColumn(distanceColumn, "Distance");
    }
    
    public DistancesCellTable(final List<DistanceData> list) {
	this();
	
	AsyncDataProvider<DistanceData> provider = new AsyncDataProvider<DistanceData>() {
	    @Override
	    protected void onRangeChanged(HasData<DistanceData> display) {
		int start = display.getVisibleRange().getStart();
		int end = start + display.getVisibleRange().getLength();
		end = end >= list.size() ? list.size() : end;
		List<DistanceData> sub = list.subList(start, end);
		updateRowData(start, sub);
		setWidth("100%");
	    }
	};
	provider.addDataDisplay(this);

	this.setRowCount(list.size(), true);
	this.setPageSize(100);
    }
}
