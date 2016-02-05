package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.SchemaService;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.MainFrame;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SchemaTreeLoader;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SpreadsheetComposite;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SpreadsheetComposite;

public class SchemaCellTable extends CellTable<SchemaData> {

    private SpreadsheetComposite viewer;
    private SchemaTreeLoader treeLoader;

    public SchemaCellTable() {
	super();

	// Create name column.
	TextColumn<SchemaData> nameColumn = new TextColumn<SchemaData>() {
	    public String getValue(SchemaData sch) {
		if (sch.getName().length() > 50)
		    return sch.getName().substring(0, 47) + "...";
		return sch.getName();
	    }
	};

	nameColumn.setSortable(true);

	TextColumn<SchemaData> sourceColumn = new TextColumn<SchemaData>() {
	    public String getValue(SchemaData sch) {
		if (sch.getSource().length() > 30)
		    return sch.getSource().substring(0, 27) + "...";
		return sch.getSource();
	    }
	};

	TextColumn<SchemaData> authorColumn = new TextColumn<SchemaData>() {
	    public String getValue(SchemaData sch) {
		return sch.getAuthor();
	    }
	};

	TextColumn<SchemaData> descColumn = new TextColumn<SchemaData>() {
	    public String getValue(SchemaData sch) {
		if (sch.getDescription().length() > 100)
		    return sch.getDescription().substring(0, 97) + "...";
		return sch.getDescription();
	    }
	};

	TextColumn<SchemaData> idColumn = new TextColumn<SchemaData>() {
	    public String getValue(SchemaData sch) {
		return sch.getId().toString();
	    }
	};

	idColumn.setSortable(true);

	this.addColumn(nameColumn, "Name");
	this.addColumn(sourceColumn, "Source");
	this.addColumn(authorColumn, "Author");
	this.addColumn(descColumn, "Description");
	this.addColumn(idColumn, "Id");
	this.addSortHandler();
    }

    public SchemaCellTable(List<SchemaData> list, SpreadsheetComposite viewer) {

	this();

	this.viewer = viewer;

	// handle the selection
	// Add a selection model to handle user selection.
	final SingleSelectionModel<SchemaData> selectionModel = new SingleSelectionModel<SchemaData>();
	this.setSelectionModel(selectionModel);
	selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
	    public void onSelectionChange(SelectionChangeEvent event) {
		SchemaData selected = selectionModel.getSelectedObject();
		if (selected != null) {
		    // Window.alert("You selected: " + selected.getSource());
		    // we need to load datas and elements
		    loadSchema(selected.getSource(), selected.getId());
		}
	    }
	});
	this.setRowCount(list.size(), true);
	this.setPageSize(list.size());
	this.setRowData(0, list);

    }

    public SchemaCellTable(List<SchemaData> list, final SchemaTreeLoader treeLoader) {

	this();

	this.treeLoader = treeLoader;

	// handle the selection
	// Add a selection model to handle user selection.
	final SingleSelectionModel<SchemaData> selectionModel = new SingleSelectionModel<SchemaData>();
	this.setSelectionModel(selectionModel);
	selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
	    public void onSelectionChange(SelectionChangeEvent event) {
		SchemaData selected = selectionModel.getSelectedObject();
		if (selected != null) {

		    Integer selectedId = new Integer(selected.getId());
		    final String name = selected.getName();
		    treeLoader.setSchemaId(selectedId);

		    // now use the service
		    SchemaServiceAsync schemaService = SchemaService.Util.getInstance();

		    schemaService.getSchemaElements(selectedId.toString(), new AsyncCallback<ArrayList<SimpleSchemaElement>>() {

			@Override
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(ArrayList<SimpleSchemaElement> result) {
			    loadTree(name, result);
			}

		    });

		}
	    }
	});

	this.setRowCount(list.size(), true);
	this.setPageSize(list.size());
	this.setRowData(0, list);

    }

    public SchemaCellTable(List<SchemaData> list, final MainFrame frame) {

	this();

	// handle the selection
	// Add a selection model to handle user selection.
	final SingleSelectionModel<SchemaData> selectionModel = new SingleSelectionModel<SchemaData>();
	this.setSelectionModel(selectionModel);
	selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
	    public void onSelectionChange(SelectionChangeEvent event) {
		final SchemaData selected = selectionModel.getSelectedObject();
		if (selected != null) {

		    // if (SpreadsheetViewer._MONGO) {
			String selectedId = selected.getId();

			SpreadsheetComposite compo = new SpreadsheetComposite(false, selected.getSource(),true,false,null);
			compo.loadXMLSchema(selectedId);
			compo.setReloaded(true);
			compo.setObjectId(selectedId);
			frame.setMainWidget(compo);
			
			frame.closeFileImportPopup();		    
		    /*} else {
			Integer selectedId = new Integer(selected.getId());
			final String name = selected.getName();

			// now use the service
			SchemaServiceAsync schemaService = SchemaService.Util.getInstance();

			schemaService.getSchemaElements(selectedId, new AsyncCallback<ArrayList<SimpleSchemaElement>>() {

			    @Override
			    public void onFailure(Throwable caught) {

			    }

			    @Override
			    public void onSuccess(ArrayList<SimpleSchemaElement> result) {
				if (selected.getSource().toLowerCase().endsWith(".xls")) {
				    SpreadsheetComposite compo = new SpreadsheetComposite(false, selected.getSource(),true,false);
				    compo.setUsername(frame.getUserName());
				    compo.reloadSchema(selected.getSource(), selected.getId());
				    frame.setMainWidget(compo);
				} else {
				    SchemaTree tree = new SchemaTree();
				    tree.setRootName(name);
				    tree.setElements(result);
				    tree.setSize("100%", "100%");
				    ScrollPanel panel = new ScrollPanel(tree);
				    panel.setSize("99%", "99%");
				    frame.setMainWidget(panel);
				}
				frame.closeFileImportPopup();
			    }
			});
		    }*/
		}
	    }
	});

	this.setRowCount(list.size(), true);
	this.setPageSize(list.size());
	this.setRowData(0, list);

    }

    public void loadSchema(String file, String id) {
	if (viewer != null) {
	    viewer.closePopup();
	    viewer.reloadSchema(file, id);
	}
    }

    public void loadTree(String name, ArrayList<SimpleSchemaElement> elements) {
	if (treeLoader != null) {
	    treeLoader.closePopup();
	    treeLoader.setSchemaName(name);
	    treeLoader.getTree().setRootName(name);
	    treeLoader.getTree().setElements(elements);
	}
    }

    private void addSortHandler() {
	final SchemaCellTable tab = this;

	this.addColumnSortHandler(new ColumnSortEvent.Handler() {
	    public void onColumnSort(ColumnSortEvent event) {
		List<SchemaData> newData = new ArrayList<SchemaData>(tab.getVisibleItems());

		// specific case, id column
		if (event.getColumn().equals(tab.getColumn(4))) {
		    if (event.isSortAscending()) {
			Collections.sort(newData, new Comparator<SchemaData>() {
			    public int compare(SchemaData o1, SchemaData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the name columns.
				if (o2 != null) {
				    return (o1 != null) ? o2.getId().compareTo(o1.getId()) : 1;
				}
				return -1;
			    }
			});
			// Sort ascending.
		    } else {
			Collections.sort(newData, new Comparator<SchemaData>() {
			    public int compare(SchemaData o1, SchemaData o2) {
				if (o1 == o2) {
				    return 0;
				}

				// Compare the name columns.
				if (o1 != null) {
				    return (o2 != null) ? o1.getId().compareTo(o2.getId()) : 1;
				}
				return -1;
			    }
			});
			// Sort descending.
		    }
		    tab.setRowData(tab.getVisibleRange().getStart(), newData);
		} else if (event.isSortAscending()) {
		    Collections.sort(newData, new Comparator<SchemaData>() {
			public int compare(SchemaData o1, SchemaData o2) {
			    if (o1 == o2) {
				return 0;
			    }

			    // Compare the name columns.
			    if (o2 != null) {
				return (o1 != null) ? o2.getName().compareToIgnoreCase(o1.getName()) : 1;
			    }
			    return -1;
			}
		    });
		    // Sort ascending.
		} else {
		    Collections.sort(newData, new Comparator<SchemaData>() {
			public int compare(SchemaData o1, SchemaData o2) {
			    if (o1 == o2) {
				return 0;
			    }

			    // Compare the name columns.
			    if (o1 != null) {
				return (o2 != null) ? o1.getName().compareToIgnoreCase(o2.getName()) : 1;
			    }
			    return -1;
			}
		    });
		    // Sort descending.
		}
		tab.setRowData(tab.getVisibleRange().getStart(), newData);
	    }

	});
    }

}
