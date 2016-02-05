package org.inria.websmatch.gwt.spreadsheet.client.models.generic;

public class SimpleSubtype extends SimpleSchemaElement {
    
    /**
     * 
     */
    private static final long serialVersionUID = -839931075339298343L;
    private Integer parentId;
    private Integer childId;
    
    public SimpleSubtype(){
	super();
    }
    
    public SimpleSubtype(Integer id, Integer parentId, Integer childId){
	super();
	this.setId(id);
	this.parentId = parentId;
	this.childId = childId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getChildId() {
        return childId;
    }

    public void setChildId(Integer childId) {
        this.childId = childId;
    }

}
