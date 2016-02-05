package org.inria.websmatch.gwt.spreadsheet.client.widgets.generic;

import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;

import com.google.gwt.user.client.ui.TreeItem;

public class SimpleSchemaElementItem extends TreeItem {
    
    private SimpleSchemaElement schemaElement;
    
    public SimpleSchemaElementItem(SimpleSchemaElement element){
	super();
	this.setText(element.getName());
	this.schemaElement = element;
    }

    public SimpleSchemaElement getSchemaElement() {
        return schemaElement;
    }

    public void setSchemaElement(SimpleSchemaElement element) {
        this.schemaElement = element;
    }

}
