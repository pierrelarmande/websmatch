package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.ArrayList;
import java.util.ListIterator;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.ConnexCompEditor;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.SimpleCellItem;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.SimpleEntityItem;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.SpreadsheetEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class SchemaCellTreeComposite extends Composite {

    private Tree tree;
    private final ScrollPanel panel;
    private final TabLayoutPanel table;

    public SchemaCellTreeComposite(final TabLayoutPanel t) {

	panel = new ScrollPanel();
	initWidget(panel);
	panel.setSize("100%", "100%");
	this.table = t;

	tree = new Tree();

	tree.addSelectionHandler(new SelectionHandler<TreeItem>() {

	    @Override
	    public void onSelection(SelectionEvent<TreeItem> event) {

		if (event.getSelectedItem().getClass().equals(SimpleCellItem.class)) {

		    // first select the tab
		    table.selectTab(((SimpleCellItem) event.getSelectedItem()).getSheetIndex());
		    // then select cell
		    ((SpreadsheetEditor) ((ScrollPanel) table.getWidget(((SimpleCellItem) event.getSelectedItem()).getSheetIndex())).getWidget())
			    .showSelectedCell(((SimpleCellItem) event.getSelectedItem()).getRowIndex(),
				    ((SimpleCellItem) event.getSelectedItem()).getColIndex());
		}

		else {
		    ((SpreadsheetEditor) ((ScrollPanel) table.getWidget(table.getSelectedIndex())).getWidget()).unselectAll();
		    if(event.getSelectedItem().getClass().equals(SimpleEntityItem.class)){
			SimpleEntityItem ent = (SimpleEntityItem) event.getSelectedItem();
			for(int i = 0; i <= ent.getSheetIndex(); i ++) table.selectTab(i);
			ConnexCompEditor ccEdit = new ConnexCompEditor((SpreadsheetComposite) table.getParent().getParent().getParent(), ent, event.getSelectedItem().getOffsetHeight());
			ccEdit.show();
		    }
		}
	    }
	});

	tree.setAnimationEnabled(true);
	panel.add(tree);
	tree.setSize("100%", "100%");

	TreeItem item = new TreeItem();
	item.setText("No schema loaded");
	tree.addItem(item);
    }

    public void setElements(String schemaName, ArrayList<SimpleCell> list) {

	ListIterator<SimpleCell> it = list.listIterator();

	panel.remove(tree);

	tree.removeItems();

	TreeItem root = new TreeItem();
	root.setText(schemaName);

	while (it.hasNext()) {

	    SimpleCell cell = it.next();

	    GWT.log("Add attr to tree : "+cell.getContent() + " " + cell.getEntityName() + " " + cell.getEntityId()+" "+cell.getCcStartX()+" "+cell.getCcStartY());

	    SimpleCellItem item = new SimpleCellItem(cell);

	    if (root.getChildCount() == 0 && cell.getEntityId() != -1) {

		SimpleEntityItem entity = new SimpleEntityItem(cell.getEntityName(), cell.getEntityId(), cell.getSheet(), cell.getCcStartX(), cell.getCcStartY());
		entity.addItem(item);

		entity.setState(true);
		root.addItem(entity);

	    } else {

		// search the good entity
		boolean entityFound = false;
		for (int i = 0; i < root.getChildCount(); i++) {

		    SimpleEntityItem entity = (SimpleEntityItem) root.getChild(i);

		    if (entity.getId() == cell.getEntityId()) {
			if (entity.getText().equals(""))
			    entity.setText(cell.getEntityName());
			entity.addItem(item);
			entityFound = true;
		    }
		}

		if (!entityFound && cell.getEntityId() != -1) {
		    SimpleEntityItem newEntity = new SimpleEntityItem(cell.getEntityName(), cell.getEntityId(), cell.getSheet(), cell.getCcStartX(), cell.getCcStartY());
		    newEntity.addItem(item);
		    newEntity.setState(true);
		    root.addItem(newEntity);
		}

	    }
	}

	root.setState(true);
	tree.addItem(root);
	panel.add(tree);
    }

    public void removeElement(SimpleCell cell) {

	boolean foundAttr = false;

	// for (int i = 0; i < tree.getItem(0).getChildCount(); i++) {
	if (cell.getSheet() != -1 && tree.getItem(0).getChild(cell.getSheet()) != null) {
	    for (int j = 0; j < tree.getItem(0).getChild(cell.getSheet()).getChildCount(); j++) {
		if (((SimpleCellItem) tree.getItem(0).getChild(cell.getSheet()).getChild(j)).getCell().equals(cell)) {
		    tree.getItem(0).getChild(cell.getSheet()).removeItem(tree.getItem(0).getChild(cell.getSheet()).getChild(j));
		    foundAttr = true;
		}
	    }
	}
	// }

	if (!foundAttr) {
	    for (int i = 0; i < tree.getItem(0).getChildCount(); i++) {
		for (int j = 0; j < tree.getItem(0).getChild(i).getChildCount(); j++) {
		    if (((SimpleCellItem) tree.getItem(0).getChild(i).getChild(j)).getCell().equals(cell)) {
			tree.getItem(0).getChild(i).removeItem(tree.getItem(0).getChild(i).getChild(j));
			foundAttr = true;
		    }
		}
	    }
	}
    }

    /**
     * @todo Add the attribute in the good entity!
     * 
     * @param cell
     */

    public SimpleCell addElement(SimpleCell cell) {

	boolean foundEnt = false;

	for (int i = 0; i < tree.getItem(0).getChildCount(); i++) {
	    if (cell.getEntityId() == ((SimpleEntityItem) tree.getItem(0).getChild(i)).getId()) {
		tree.getItem(0).getChild(i).addItem(new SimpleCellItem(cell));
		foundEnt = true;
	    }
	}

	if (!foundEnt) {

	    GWT.log(this.getClass().getName() + " No entity found.");

	    if (cell.getEntityId() != -1) {
		SimpleEntityItem newEntity = new SimpleEntityItem(cell.getEntityName(), cell.getEntityId(), cell.getSheet(), cell.getCcStartX(), cell.getCcStartY());
		newEntity.addItem(new SimpleCellItem(cell));
		newEntity.setState(true);
		tree.getItem(0).addItem(newEntity);
	    }

	    else if (tree.getItem(0).getChild(cell.getSheet()) != null) {
		cell.setEntityId(((SimpleEntityItem) (tree.getItem(0).getChild(cell.getSheet()))).getId());
		cell.setEntityName(((SimpleEntityItem) (tree.getItem(0).getChild(cell.getSheet()))).getText());
		tree.getItem(0).getChild(cell.getSheet()).addItem(new SimpleCellItem(cell));
	    } else {

		// add the sheet entity
		GWT.log(this.getClass().getName() + " ent id : " + cell.getEntityId() + " ent name : " + cell.getEntityName());

		// add entity
		SimpleEntityItem ent = new SimpleEntityItem(cell.getEntityName(), cell.getEntityId(), cell.getSheet(), cell.getCcStartX(), cell.getCcStartY());
		tree.getItem(0).addItem(ent);
		ent.setState(true);
		tree.getItem(0).setState(true);
		//

		tree.getItem(0).getChild(tree.getItemCount() - 1).addItem(new SimpleCellItem(cell));
		tree.getItem(0).getChild(tree.getItemCount() - 1).setState(true);
		tree.getItem(0).setState(true);
	    }

	}

	return cell;
    }

    /**
     * Update the tree element
     * 
     * @param cell
     */

    public void updateElement(SimpleCell cell) {
	
	boolean found = false;

	for (int i = 0; i < tree.getItem(0).getChildCount(); i++) {
	    if (tree.getItem(0).getChild(i).getChildCount() > 0) {

		for (int j = 0; j < tree.getItem(0).getChild(i).getChildCount(); j++) {

		    SimpleCellItem item = (SimpleCellItem) tree.getItem(0).getChild(i).getChild(j);
		    if (item.getCell().getSheet() == cell.getSheet() && item.getCell().getJxlCol() == cell.getJxlCol()
			    && item.getCell().getJxlRow() == cell.getJxlRow()) {

			if (!cell.getEditedContent().equals("") && !cell.getEditedContent().equals(cell.getContent()))
			    item.setText(cell.getEditedContent());
			found = true;

		    }

		}
	    }
	}
	
	if(!found){
	    addElement(cell);
	}
    }
}
