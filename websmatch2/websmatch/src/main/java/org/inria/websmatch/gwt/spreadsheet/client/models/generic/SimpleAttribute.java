package org.inria.websmatch.gwt.spreadsheet.client.models.generic;

public class SimpleAttribute extends SimpleSchemaElement {

    /**
     * 
     */
    private static final long serialVersionUID = -6425922179238293280L;
    
    private Integer entityId;
    private String entityName;
    
    public SimpleAttribute(){
	super();
    }
    
    public SimpleAttribute(Integer id, String name, Integer entId, String entName){
	super(id,name);
	this.entityId = entId;
	this.setEntityName(entName);
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public void setEntityName(String entityName) {
	this.entityName = entityName;
    }

    public String getEntityName() {
	return entityName;
    }

}
