package org.inria.websmatch.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

public class WSMatchXMLDiff {

    /**
     * 
     * 
     * @param auto_xml
     * @param edit_xml
     * @return
     */

    @SuppressWarnings("unchecked")
    public HashMap<ConnexComposant, HashMap<String, Integer>> getDiff(String auto_xml, String edit_xml) {

	if (edit_xml.equals("")) {
	    edit_xml = "<xml></xml>";
	}

	HashMap<ConnexComposant, HashMap<String, Integer>> results = new HashMap<ConnexComposant, HashMap<String, Integer>>();

	WSMatchXMLLoader autoLoader = new WSMatchXMLLoader(auto_xml);
	Document autoDoc = autoLoader.getDocument();

	WSMatchXMLLoader editLoader = new WSMatchXMLLoader(edit_xml);
	Document editDoc = editLoader.getDocument();

	// get the tables
	Filter tableFilter = new ElementFilter("table", null);
	Iterator<Element> autoTables = autoDoc.getRootElement().getDescendants(tableFilter);

	while (autoTables.hasNext()) {	    	  

	    Element autoTab = autoTables.next();
	    
	    Iterator<Element> editTables = editDoc.getRootElement().getDescendants(tableFilter);
	    
	    while (editTables.hasNext()) {
		
		Element editTab = editTables.next();

		// if same CC
		if (new Integer(autoTab.getAttributeValue("sheet")).intValue() == new Integer(editTab.getAttributeValue("sheet")).intValue()
			&& new Integer(autoTab.getAttributeValue("startX")).intValue() == new Integer(editTab.getAttributeValue("startX")).intValue()
			&& new Integer(autoTab.getAttributeValue("endX")).intValue() == new Integer(editTab.getAttributeValue("endX")).intValue()
			&& new Integer(autoTab.getAttributeValue("startY")).intValue() == new Integer(editTab.getAttributeValue("startY")).intValue()
			&& new Integer(autoTab.getAttributeValue("endY")).intValue() == new Integer(editTab.getAttributeValue("endY")).intValue()) {
		    
		    ConnexComposant cc = new ConnexComposant();

		    // set values for cc
		    cc.setAttrInLines(new Boolean(autoTab.getAttributeValue("attrInLine")));
		    cc.setBiDimensionnalArray(new Boolean(autoTab.getAttributeValue("biDim")));
		    cc.setCriteria(autoTab.getAttributeValue("criteria"));
		    cc.setSheet(new Integer(autoTab.getAttributeValue("sheet")));
		    cc.setStartX(new Integer(autoTab.getAttributeValue("startX")));
		    cc.setEndX(new Integer(autoTab.getAttributeValue("endX")));
		    cc.setStartY(new Integer(autoTab.getAttributeValue("startY")));
		    cc.setEndY(new Integer(autoTab.getAttributeValue("endY")));
		    //

		    // now for each sheet compare attributes
		    Filter elementFilter = new ElementFilter("attribute", null);

		    Iterator<Element> it = autoTab.getDescendants(elementFilter);
		    List<Element> autoAttributes = new ArrayList<Element>();
		    while (it.hasNext())
			autoAttributes.add(it.next());

		    it = editTab.getDescendants(elementFilter);
		    List<Element> editAttributes = new ArrayList<Element>();
		    while (it.hasNext())
			editAttributes.add(it.next());

		    HashMap<String, Integer> values = new HashMap<String, Integer>();
		    
		    values.put("auto", autoAttributes.size());
		    values.put("edit", editAttributes.size());

		    int intersect = 0;

		    // first
		    for (Element auto : autoAttributes) {

			for (Element edit : editAttributes) {

			    if (auto.getAttributeValue("sheet").equals(edit.getAttributeValue("sheet"))
				    && auto.getAttributeValue("x").equals(edit.getAttributeValue("x"))
				    && auto.getAttributeValue("y").equals(edit.getAttributeValue("y"))) {
				intersect++;
				break;
			    }
			}
		    }

		    values.put("intersect", intersect);

		    // add to CCs
		    results.put(cc, values);
		}
	    }
	}

	return results;
    }
}
