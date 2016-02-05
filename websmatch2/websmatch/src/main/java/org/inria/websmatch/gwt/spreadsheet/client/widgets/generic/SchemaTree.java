package org.inria.websmatch.gwt.spreadsheet.client.widgets.generic;

import java.util.ArrayList;
import java.util.ListIterator;

import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleAttribute;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSubtype;

import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class SchemaTree extends Tree {

    private TreeItem root = new TreeItem();
    private ArrayList<SimpleSchemaElementItem> items;

    public SchemaTree() {
	super();
	this.setRootName("No schema loaded");
	this.addItem(root);
	this.setSize("100%", "100%");
    }

    public SchemaTree(ArrayList<SimpleSchemaElement> elements) {
	this();
	this.setElements(elements);
    }

    public void setRootName(String name) {
	root.setText(name);
    }

    public void setElements(ArrayList<SimpleSchemaElement> elements) {
		
	ListIterator<SimpleSchemaElement> it = elements.listIterator();
	
	root.removeItems();
	items = new ArrayList<SimpleSchemaElementItem>();
	ArrayList<SimpleSchemaElementItem> attrs = new ArrayList<SimpleSchemaElementItem>();

	while (it.hasNext()) {
	    SimpleSchemaElement element = it.next();
	    SimpleSchemaElementItem entItem = new SimpleSchemaElementItem(element);
	    if (element instanceof SimpleEntity) {
		items.add(entItem);
	    } else if (element instanceof SimpleAttribute) {
		attrs.add(entItem);
	    }
	}

	// after adding entities, we complete them with attributes
	ListIterator<SimpleSchemaElementItem> itAttrs = attrs.listIterator();

	while (itAttrs.hasNext()) {

	    SimpleSchemaElementItem attrItem = itAttrs.next();

	    // iterates to find the entity
	    for (int its = 0; its < items.size(); its++) {
		if (items.get(its).getSchemaElement().getId().equals(((SimpleAttribute) attrItem.getSchemaElement()).getEntityId())) {
		    items.get(its).addItem(attrItem);
		    items.get(its).setState(true);
		    break;
		}
	    }
	}

	// now working on subtypes and organise all nodes
	it = elements.listIterator();
	ArrayList<SimpleSchemaElementItem> itemsToRemove = new ArrayList<SimpleSchemaElementItem>();

	while (it.hasNext()) {
	    SimpleSchemaElement element = it.next();
	    if (element instanceof SimpleSubtype) {
		int parentId = ((SimpleSubtype) element).getParentId();
		int childId = ((SimpleSubtype) element).getChildId();

		// now search in the nodes
		ListIterator<SimpleSchemaElementItem> itNodes = items.listIterator();
		// parent
		while (itNodes.hasNext()) {
		    SimpleSchemaElementItem parentItem = itNodes.next();
		    if (parentItem.getSchemaElement().getId() == parentId) {

			// ok search for the child
			ListIterator<SimpleSchemaElementItem> itNodesC = items.listIterator();
			// child
			while (itNodesC.hasNext()) {
			    SimpleSchemaElementItem childItem = itNodesC.next();
			    if (childItem.getSchemaElement().getId() == childId) {
				parentItem.addItem(childItem);
				parentItem.setState(true);
				itemsToRemove.add(childItem);
				break;
			    }
			}
		    }
		}
	    }
	}

	ListIterator<SimpleSchemaElementItem> itRem = itemsToRemove.listIterator();
	while (itRem.hasNext())
	    items.remove(itRem.next());

	ListIterator<SimpleSchemaElementItem> itNodes = items.listIterator();
	while (itNodes.hasNext())
	    root.addItem(itNodes.next());
	this.addItem(root);

	root.setState(true);
    }

}
