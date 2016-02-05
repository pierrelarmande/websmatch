package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class ClusterTree extends Composite {
    
    private Tree tree;

    public ClusterTree() {
    	
    	ScrollPanel scrollPanel = new ScrollPanel();
    	initWidget(scrollPanel);
    	
    	tree = new Tree();
    	TreeItem item = new TreeItem();
    	item.setText("Clusters");
    	tree.addItem(item);
    	scrollPanel.setWidget(tree);
    	tree.setSize("100%", "100%");
    }

    public void addCluster(ArrayList<String> nodes){
	
	// sort nodes
	Collections.sort(nodes);
	
	// how many clusters are here already?
	int clusters = tree.getItem(0).getChildCount();
	
	TreeItem cluster = new TreeItem();
	cluster.setText("Cluster "+(clusters+1)+ " : "+nodes.size()+" files");
	
	for(String node : nodes){
	    TreeItem subItem  = new TreeItem();
	    subItem.setText(node);
	    cluster.addItem(subItem);
	}
	
	cluster.setState(false);
	tree.getItem(0).addItem(cluster);
	tree.getItem(0).setState(true);
	
    }
    
}
