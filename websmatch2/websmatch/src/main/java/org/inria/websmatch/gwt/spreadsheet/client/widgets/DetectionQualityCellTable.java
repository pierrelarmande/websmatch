package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetViewer;
import org.inria.websmatch.gwt.spreadsheet.client.composites.MainFrame;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SpreadsheetComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SingleSelectionModel;

public class DetectionQualityCellTable extends CellTable<DetectionQualityData> {

    public DetectionQualityCellTable() {
	super();
	
	// Create name column.
	TextColumn<DetectionQualityData> nameColumn = new TextColumn<DetectionQualityData>() {
	    public String getValue(DetectionQualityData sch) {
		if (sch.getName().length() > 40)
		    return sch.getName().substring(0, 37) + "...";
		return sch.getSource();
	    }

	    @Override
	    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context c, DetectionQualityData o) {
		if (!o.isNeverEdited())
		    return "editedXLSSchema";
		else
		    return super.getCellStyleNames(c, o);
	    }
	};
	nameColumn.setSortable(true);


	// Create source column.
	TextColumn<DetectionQualityData> sourceColumn = new TextColumn<DetectionQualityData>() {
	    public String getValue(DetectionQualityData sch) {
		if (sch.getSource().length() > 40)
		    return sch.getSource().substring(0, 37) + "...";
		return sch.getSource();
	    }

	    @Override
	    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context c, DetectionQualityData o) {
		if (!o.isNeverEdited())
		    return "editedXLSSchema";
		else
		    return super.getCellStyleNames(c, o);
	    }
	};
	sourceColumn.setSortable(true);

	TextColumn<DetectionQualityData> precisionColumn = new TextColumn<DetectionQualityData>() {
	    public String getValue(DetectionQualityData sch) {
		if (sch.getPrecision() == -1)
		    return "?";
		if (new Float(sch.getPrecision()).toString().length() > 5)
		    return new Float(sch.getPrecision()).toString().substring(0, 5);
		else
		    return new Float(sch.getPrecision()).toString();
	    }

	    @Override
	    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context c, DetectionQualityData o) {
		if (!o.isNeverEdited())
		    return "editedXLSSchema";
		else
		    return super.getCellStyleNames(c, o);
	    }
	};
	precisionColumn.setSortable(true);

	TextColumn<DetectionQualityData> recallColumn = new TextColumn<DetectionQualityData>() {
	    public String getValue(DetectionQualityData sch) {
		if (sch.getRecall() == -1)
		    return "?";
		if (new Float(sch.getRecall()).toString().length() > 5)
		    return new Float(sch.getRecall()).toString().substring(0, 5);
		else
		    return new Float(sch.getRecall()).toString();
	    }

	    @Override
	    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context c, DetectionQualityData o) {
		if (!o.isNeverEdited())
		    return "editedXLSSchema";
		else
		    return super.getCellStyleNames(c, o);
	    }
	};
	recallColumn.setSortable(true);

	TextColumn<DetectionQualityData> fmeaColumn = new TextColumn<DetectionQualityData>() {
	    public String getValue(DetectionQualityData sch) {
		if (sch.getFmeasure() == -1) {
		    return "?";
		}
		if (new Float(sch.getFmeasure()).toString().length() > 5)
		    return new Float(sch.getFmeasure()).toString().substring(0, 5);
		else
		    return new Float(sch.getFmeasure()).toString();
	    }

	    @Override
	    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context c, DetectionQualityData o) {
		if (!o.isNeverEdited())
		    return "editedXLSSchema";
		else
		    return super.getCellStyleNames(c, o);
	    }
	};
	fmeaColumn.setSortable(true);

	@SuppressWarnings("unused")
	TextColumn<DetectionQualityData> idColumn = new TextColumn<DetectionQualityData>() {
	    public String getValue(DetectionQualityData sch) {
		return sch.getObjectId();
	    }

	    @Override
	    public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context c, DetectionQualityData o) {
		if (!o.isNeverEdited())
		    return "editedXLSSchema";
		else
		    return super.getCellStyleNames(c, o);
	    }
	};

	// new column for warning if reported problem
	final GfxRessources resources = GfxRessources.INSTANCE;

	Column<DetectionQualityData, ImageResource> imageColumn = new Column<DetectionQualityData, ImageResource>(new ImageResourceCell()) {
	    @Override
	    public ImageResource getValue(DetectionQualityData object) {
		if(object.isAttrDetectPb() || object.isCcDetectPb()){
		    return resources.warning();
		}
		else return null;
	    }
	};
	
	Column<DetectionQualityData, ImageResource> trashedColumn = new Column<DetectionQualityData, ImageResource>(new ImageResourceCell()) {
	    @Override
	    public ImageResource getValue(DetectionQualityData object) {
		if(object.isTrashed()){
		    return resources.trashed();
		}
		else return null;
	    }
	};
	
	this.addColumn(nameColumn, "Name");
	this.addColumn(sourceColumn, "Source");
	this.addColumn(precisionColumn, "Precision");
	this.addColumn(recallColumn, "Recall");
	this.addColumn(fmeaColumn, "Fmeasure");

	this.addColumn(imageColumn, "Problem");
	this.addColumn(trashedColumn, "Trashed");

	// this.addColumn(critersColumn, "Criters");
	// for debug
	// this.addColumn(idColumn, "Object id");
	this.addSortHandler();
    }

    public DetectionQualityCellTable(final List<DetectionQualityData> list, final MainFrame frame) {
	this();

	AsyncDataProvider<DetectionQualityData> provider = new AsyncDataProvider<DetectionQualityData>() {
	    @Override
	    protected void onRangeChanged(HasData<DetectionQualityData> display) {
		int start = display.getVisibleRange().getStart();
		int end = start + display.getVisibleRange().getLength();
		end = end >= list.size() ? list.size() : end;
		List<DetectionQualityData> sub = list.subList(start, end);
		updateRowData(start, sub);
		setWidth("100%");
	    }
	};
	provider.addDataDisplay(this);

	this.setRowCount(list.size(), true);
	this.setPageSize(100);

	this.addStyleName("left");
	
	final DetectionQualityCellTable table = this;
	
	// handle click
	final SingleSelectionModel<DetectionQualityData> selectionModel = new SingleSelectionModel<DetectionQualityData>();
	this.setSelectionModel(selectionModel);
	this.addCellPreviewHandler(new CellPreviewEvent.Handler<DetectionQualityData>() {

	    @Override
	    public void onCellPreview(CellPreviewEvent<DetectionQualityData> event) {
		
		boolean isClick = "click".equals(event.getNativeEvent().getType());
		if(isClick){
		    final DetectionQualityData selected = selectionModel.getSelectedObject();
			if (selected != null) {
			    String selectedId = selected.getObjectId();

			    SpreadsheetComposite compo = new SpreadsheetComposite(false, selected.getSource(), false, true,table);

			    compo.setName(selected.getName());
			    compo.setDescription(selected.getDescription());

			    compo.loadXMLSchema(selectedId);
			    compo.setReloaded(true);
			    compo.setObjectId(selectedId);
			    compo.setPublication_id(selected.getPublication_id());
			    compo.setUsername(SpreadsheetViewer.username);

			    //
			    PopupPanel pop = new PopupPanel(false);
			    pop.setGlassEnabled(true);
			    pop.setAnimationEnabled(true);
			    pop.setPopupPosition(10, 10);
			    pop.setSize((frame.getOffsetWidth() - 50) + "px", (frame.getOffsetHeight() - 50) + "px");

			    GWT.log("Pub_id : "+compo.getPublication_id());
			    
			    pop.add(compo);

			    pop.show();
			    WaitingPopup.getInstance().show();
			    //
			}
		}
		
	    }
	    
	});

	// handle the selection
	// Add a selection model to handle user selection.
	/*final SingleSelectionModel<DetectionQualityData> selectionModel = new SingleSelectionModel<DetectionQualityData>();
	this.setSelectionModel(selectionModel);
	selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
	    public void onSelectionChange(SelectionChangeEvent event) {
		final DetectionQualityData selected = selectionModel.getSelectedObject();
		if (selected != null) {
		    String selectedId = selected.getObjectId();

		    SpreadsheetComposite compo = new SpreadsheetComposite(false, selected.getSource(), false, true,table);

		    compo.setName(selected.getName());
		    compo.setDescription(selected.getDescription());

		    compo.loadXMLSchema(selectedId);
		    compo.setReloaded(true);
		    compo.setObjectId(selectedId);
		    compo.setPublication_id(selected.getPublication_id());

		    //
		    PopupPanel pop = new PopupPanel(false);
		    pop.setGlassEnabled(true);
		    pop.setAnimationEnabled(true);
		    pop.setPopupPosition(10, 10);
		    pop.setSize((frame.getOffsetWidth() - 50) + "px", (frame.getOffsetHeight() - 50) + "px");

		    GWT.log("Pub_id : "+compo.getPublication_id());
		    
		    pop.add(compo);

		    pop.show();
		    WaitingPopup.getInstance().show();
		    //
		}
	    }
	});*/
    }

    private void addSortHandler() {
	final DetectionQualityCellTable tab = this;

	this.addColumnSortHandler(new ColumnSortEvent.Handler() {
	    public void onColumnSort(ColumnSortEvent event) {
		List<DetectionQualityData> newData = new ArrayList<DetectionQualityData>(tab.getVisibleItems());

		if (event.getColumn().equals(tab.getColumn(0))) {
		    if (event.isSortAscending()) {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o2 != null) {
				    return (o1 != null) ? o2.getName().toString().compareToIgnoreCase(o1.getName().toString()) : 1;
				}
				return -1;
			    }
			});
			// Sort ascending.
		    } else {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o1 != null) {
				    return (o2 != null) ? o1.getName().toString().compareToIgnoreCase(o2.getName().toString()) : 1;
				}
				return -1;
			    }
			});
			// Sort descending.
		    }
		    tab.setRowData(tab.getVisibleRange().getStart(), newData);
		}
		
		if (event.getColumn().equals(tab.getColumn(1))) {
		    if (event.isSortAscending()) {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o2 != null) {
				    return (o1 != null) ? o2.getSource().toString().compareToIgnoreCase(o1.getSource().toString()) : 1;
				}
				return -1;
			    }
			});
			// Sort ascending.
		    } else {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o1 != null) {
				    return (o2 != null) ? o1.getSource().toString().compareToIgnoreCase(o2.getSource().toString()) : 1;
				}
				return -1;
			    }
			});
			// Sort descending.
		    }
		    tab.setRowData(tab.getVisibleRange().getStart(), newData);
		}


		if (event.getColumn().equals(tab.getColumn(2))) {
		    if (event.isSortAscending()) {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o2 != null) {
				    return (o1 != null) ? new Float(o2.getPrecision()).toString().compareToIgnoreCase(new Float(o1.getPrecision()).toString())
					    : 1;
				}
				return -1;
			    }
			});
			// Sort ascending.
		    } else {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o1 != null) {
				    return (o2 != null) ? new Float(o1.getPrecision()).toString().compareToIgnoreCase(new Float(o2.getPrecision()).toString())
					    : 1;
				}
				return -1;
			    }
			});
			// Sort descending.
		    }
		    tab.setRowData(tab.getVisibleRange().getStart(), newData);
		}

		if (event.getColumn().equals(tab.getColumn(3))) {
		    if (event.isSortAscending()) {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o2 != null) {
				    return (o1 != null) ? new Float(o2.getRecall()).toString().compareToIgnoreCase(new Float(o1.getRecall()).toString()) : 1;
				}
				return -1;
			    }
			});
			// Sort ascending.
		    } else {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o1 != null) {
				    return (o2 != null) ? new Float(o1.getRecall()).toString().compareToIgnoreCase(new Float(o2.getRecall()).toString()) : 1;
				}
				return -1;
			    }
			});
			// Sort descending.
		    }
		    tab.setRowData(tab.getVisibleRange().getStart(), newData);
		}

		if (event.getColumn().equals(tab.getColumn(4))) {
		    if (event.isSortAscending()) {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o2 != null) {
				    return (o1 != null) ? new Float(o2.getFmeasure()).toString().compareToIgnoreCase(new Float(o1.getFmeasure()).toString())
					    : 1;
				}
				return -1;
			    }
			});
			// Sort ascending.
		    } else {
			Collections.sort(newData, new Comparator<DetectionQualityData>() {
			    public int compare(DetectionQualityData o1, DetectionQualityData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the fmea columns.
				if (o1 != null) {
				    return (o2 != null) ? new Float(o1.getFmeasure()).toString().compareToIgnoreCase(new Float(o2.getFmeasure()).toString())
					    : 1;
				}
				return -1;
			    }
			});
			// Sort descending.
		    }
		    tab.setRowData(tab.getVisibleRange().getStart(), newData);
		}	
	    }
	});
    }
}
