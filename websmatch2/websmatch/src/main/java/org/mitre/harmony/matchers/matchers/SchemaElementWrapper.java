package org.mitre.harmony.matchers.matchers;
import java.util.ArrayList;
import java.util.List;

import org.mitre.schemastore.model.SchemaElement;

import yam.datatypes.interfaces.IElement;
import yam.datatypes.tree.Tree;

/**
 * @author ngoduyhoa
 * wrap SchemaElement from OpenII to IElement of YAM
 */
public class SchemaElementWrapper implements IElement 
{
 // schema element
	private	SchemaElement	selement;
		
	public SchemaElementWrapper(SchemaElement selement) 
	{
		this.selement = selement;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return selement.getName();
	}

	public String[] getLabels() {
		// TODO Auto-generated method stub
		List<String>	labels	=	new ArrayList<String>();
		
		labels.add(selement.getName());
		
		// maybe more...
		
		return labels.toArray(new String[labels.size()]);
	}

	
	public String getComment() {
		// TODO Auto-generated method stub
		return selement.getDescription();
	}

	
	public String getProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getSimpleProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getInstanceInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<String> getParents() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<String> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<String> getSiblings() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Tree<String> getSupTree() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Tree<String> getSubTree() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int compareTo(IElement arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
