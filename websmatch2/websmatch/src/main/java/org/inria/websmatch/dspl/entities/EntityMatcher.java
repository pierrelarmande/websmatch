package org.inria.websmatch.dspl.entities;

import java.util.ArrayList;
import java.util.HashMap;

import jxl.Cell;

import org.inria.websmatch.dspl.entities.time.Date;
import org.inria.websmatch.dspl.entities.quantity.Ratio;
import org.inria.websmatch.dspl.entities.time.Quarter;
import org.inria.websmatch.dspl.entities.time.Year;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.StringUtils;

public class EntityMatcher implements EntityMatcherTech {

    private double treeshold = 30.0;

    private HashMap<String, Entity> entities;

    public EntityMatcher(HashMap<String, Entity> entities) {
	this.entities = entities;
    }

    @Override
    public SimpleCell match(SimpleCell attributeCell, ArrayList<Cell> toMatchWith) {

	// if toMatchWith size over, take only 100 random values
	/*if (toMatchWith.size() > 100) {
	    ArrayList<Cell> tmp = new ArrayList<Cell>();
	    Random r = new Random();
	    for (int i = 0; i < 100; i++) {
		int rand = r.nextInt(toMatchWith.size());
		tmp.add(toMatchWith.remove(rand));
	    }
	    toMatchWith = tmp;
	}*/
	//
	attributeCell.setErrorList(new ArrayList<int[]>());
	int sizeToMatch = toMatchWith.size();

	if (sizeToMatch == 0) {
	    attributeCell.setDsplMapped(false);
	    return attributeCell;
	}

	int matchCount = 0;

	// optimize, stop matching if threshold not possible
	int count = 0;

	// just for the class
	Entity ent = entities.entrySet().iterator().next().getValue();

	for (Cell c : toMatchWith) {

	    String s = c.getContents().trim();
	    boolean found = false;

	    // if threshold not possible
	    if (((double) matchCount + ((double) sizeToMatch - (double) count)) / (double) sizeToMatch * (double) 100 < treeshold) {
		// L.Debug(this, "Stopping matching on this entitie.",
		// true);
		attributeCell.setDsplMapped(false);
		return attributeCell;
	    }

	    if (ent.getClass().equals(Ratio.class)) {
		if (s.indexOf("%") == s.length() - 1) {
		    matchCount++;
		    found = true;
		}
	    }	    
	    else if (ent.getClass().equals(Date.class)
		    || ent.getClass().equals(Year.class)
		    || ent.getClass().equals(Quarter.class)) {
		if (entities.containsKey(s)) {
		    matchCount++;
		    found = true;
		}else 
		    // date case where dd/MM/yyyy
		    if(s.split("/").length == 3 && entities.containsKey(s.split("/")[1]+"/"+s.split("/")[2])){
		    matchCount++;
		    found = true;
		}
	    } else {
		// if a number int/float, break
		/*
		 * try{ new Double(s); // L.Debug(this,
		 * "Stopping matching on entitie as it contains number.", true);
		 * found = true; break; } catch (NumberFormatException nfe){ //
		 * }
		 */
		// we remove - and spaces
		if (entities.containsKey(StringUtils.cleanString(s))) {
		    matchCount++;
		    found = true;
		}
	    }
	    // add the not recognized value
	    if (!found) {
		ArrayList<int[]> list = attributeCell.getErrorList();
		list.add(new int[] { c.getColumn(), c.getRow() });
		//System.out.println(c.getContents()+" "+c.getColumn()+" "+c.getRow());
		attributeCell.setErrorList(list);
	    }
	    count++;
	}

	if (matchCount == 0 || sizeToMatch == 0) {
	    attributeCell.setDsplMapped(false);
	    attributeCell.setErrorList(new ArrayList<int[]>());
	    return attributeCell;
	}
	if ((double) matchCount / (double) sizeToMatch * (double) 100 > treeshold) {
	    attributeCell.setDsplMapped(true);
	    L.Debug(this, "Mapped but " + attributeCell.getErrorList().size() + " error(s).", true);

	    // if this time:time_point, we have to set the format
	    if (ent instanceof Date) {
		for (Cell c : toMatchWith) {
		    String s = c.getContents().trim();
		    if(s.indexOf('/') != -1 && s.split("/").length == 3 && s.split("/")[0].length() == 2 && s.split("/")[1].length() == 2 && s.split("/")[2].length() == 4){
			attributeCell.setFormat("dd/MM/yyyy");
			return attributeCell;
		    }
		    else if (s.indexOf('-') != -1 && s.split("-")[0].length() == 4 && s.split("-")[1].length() >= 1) {
			attributeCell.setFormat("yyyy-MM");
			return attributeCell;
		    } else if (s.indexOf('-') != -1 && s.split("-")[0].length() >= 1 && s.split("-")[1].length() == 4) {
			attributeCell.setFormat("MM-yyyy");
			return attributeCell;
		    } else if (s.indexOf('/') != -1 && s.split("/")[0].length() == 4 && s.split("/")[1].length() >= 1) {
			attributeCell.setFormat("yyyy/MM");
			return attributeCell;
		    } else if (s.indexOf('/') != -1 && s.split("/")[0].length() >= 1 && s.split("/")[1].length() == 4) {
			attributeCell.setFormat("MM/yyyy");
			return attributeCell;
		    } else if(s.indexOf('Q') == 1 && s.split("Q")[0].length() == 1 && s.split("Q")[1].length() == 2){
			// quarter
			attributeCell.setFormat("MQyy");
			return attributeCell;
		    }
		}
	    }
	    //
	    return attributeCell;
	}

	attributeCell.setDsplMapped(false);
	attributeCell.setErrorList(new ArrayList<int[]>());
	return attributeCell;
    }

    public HashMap<String, Entity> getEntities() {
	return entities;
    }

    public void setEntities(HashMap<String, Entity> entities) {
	this.entities = entities;
    }

}
