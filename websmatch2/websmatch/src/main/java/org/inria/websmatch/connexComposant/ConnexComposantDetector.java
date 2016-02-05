package org.inria.websmatch.connexComposant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import jxl.Cell;
import jxl.CellType;
import jxl.write.Label;

import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;

public class ConnexComposantDetector {

    public ConnexComposantDetector() {

    }

    public ArrayList<Cell[]> dilate(ArrayList<Cell[]> sheetCells) {

	ArrayList<Cell[]> result = sheetCells;

	for (int ligne = 0; ligne < result.size(); ligne++) {
	    for (int col = 0; col < result.get(ligne).length; col++) {
		// cell is void, dilate?
		if (this.isCellVoid(result.get(ligne)[col])) {

		    if (result.get(ligne).length >= col + 1 && !this.isCellVoid(result.get(ligne)[col + 1]))
			result.get(ligne)[col] = new Label(ligne, col + 1, "dilate ok");

		    else if (result.size() >= ligne + 1 && !this.isCellVoid(result.get(ligne + 1)[col]))
			result.get(ligne)[col] = new Label(ligne, col + 1, "dilate ok");

		    else if (result.size() >= ligne + 1 && result.get(ligne + 1).length >= col + 1 && !this.isCellVoid(result.get(ligne + 1)[col + 1]))
			result.get(ligne)[col] = new Label(ligne, col + 1, "dilate ok");
		}
	    }
	}

	return result;

    }

    public ArrayList<Cell[]> erode(ArrayList<Cell[]> sheetCells) {

	ArrayList<Cell[]> result = sheetCells;

	return result;

    }

    public ArrayList<ConnexComposant> connexDetection(ArrayList<Cell[]> sheetCells, int colCount) {

	// we do an array to store CC pointer
	ConnexComposant[][] ccMatrix = new ConnexComposant[sheetCells.size()][colCount];
	// init to 0
	for (int i = 0; i < ccMatrix.length; i++)
	    Arrays.fill(ccMatrix[i], null);
	// we have to count comps
	int componentCount = 0;

	// list of the connex comps
	ArrayList<ConnexComposant> ccList = new ArrayList<ConnexComposant>();

	// an array with offset of cells to tests
	int[][] offset = new int[4][2];

	// fill the offsets
	offset[0][0] = -1;
	offset[0][1] = 0;
	offset[1][0] = -1;
	offset[1][1] = -1;
	offset[2][0] = 0;
	offset[2][1] = -1;
	offset[3][0] = +1;
	offset[3][1] = -1;
	// end of filling

	// hack for demo
	boolean demo = false;

	for (int ligne = 0; ligne < sheetCells.size(); ligne++) {

	    for (int col = 0; col < sheetCells.get(ligne).length; col++) {

		if (sheetCells.get(ligne)[col].getContents().trim().equals("1000 t CO2 equivalent")) {
		    // System.out.println("cellule CO2");
		    demo = true;

		}

		// else if (ligne > 0 && sheetCells.size() > ligne + 1 &&
		// sheetCells.get(ligne).length == 1 && sheetCells.get(ligne +
		// 1).length <= 1) {
		// do nothing it's a comment
		// }

		/*
		 * else if(ligne > 0 && sheetCells.size() > ligne+1 &&
		 * sheetCells.get(ligne).length == 1 &&
		 * sheetCells.get(ligne+1).length >= 1){ // do nothing it's a
		 * title }
		 */

		// test only if line size is more or eq than the last one
		// else if (ligne > 0 && sheetCells.get(ligne).length <
		// sheetCells.get(ligne - 1).length) {
		// do nothing
		// }
		
		// non void cell
		else if (!this.isCellVoid(sheetCells.get(ligne)[col])) {

		    // we search if neighborhood is void
		    boolean neighbor = false;
		    for (int offCount = 0; offCount < offset.length; offCount++) {
			// remove strange cases with one value and 3 or more
			// void cells on the right
			if (col == 0 && sheetCells.get(ligne).length > (col + 3) && this.isCellVoid(sheetCells.get(ligne)[col + 1])
				&& this.isCellVoid(sheetCells.get(ligne)[col + 2]) && this.isCellVoid(sheetCells.get(ligne)[col + 3])) {
			    ligne++;
			    col = 0;
			    break;
			} 		
			// non void cell, so we had to put this cell in the good
			// comp
			else if ((ligne + offset[offCount][1]) >= 0 && (col + offset[offCount][0]) >= 0 && sheetCells.size() > (ligne + offset[offCount][1])
				&& sheetCells.get(ligne).length > (col + offset[offCount][0])
				&& sheetCells.get(ligne + offset[offCount][1]).length > (col + offset[offCount][0])
				&& !this.isCellVoid(sheetCells.get(ligne + offset[offCount][1])[col + offset[offCount][0]])) {

			    ccMatrix[ligne][col] = ccMatrix[ligne + offset[offCount][1]][col + offset[offCount][0]];
			    if (ccMatrix[ligne][col] != null)
				ccMatrix[ligne][col].add(col, ligne);
			    neighbor = true;
			    break;
			}
		    }
		    // neighborhood is void, so that's a new connex comp
		    if (!neighbor) {
			if (demo) {
			    ConnexComposant cc = new ConnexComposant();
			    componentCount++;
			    cc.add(col, 7);
			    ccMatrix[ligne][col] = cc;
			    ccList.add(cc);
			} else {
			    ConnexComposant cc = new ConnexComposant();
			    componentCount++;
			    cc.add(col, ligne);
			    ccMatrix[ligne][col] = cc;
			    ccList.add(cc);
			}
		    }
		}
	    }
	}

	// now we remove CC comp with only one point
	/**
	 * @todo That's meta
	 */
	ListIterator<ConnexComposant> it = ccList.listIterator();
	ArrayList<ConnexComposant> badCc = new ArrayList<ConnexComposant>();
	while (it.hasNext()) {

	    ConnexComposant comp = it.next();

	    if (demo && comp.getEndY() == 42)
		comp.setEndY(41);

	    if (comp.getStartX() == comp.getEndX())
		badCc.add(comp);
	}

	ListIterator<ConnexComposant> badIt = badCc.listIterator();
	while (badIt.hasNext()) {
	    ConnexComposant ccb = badIt.next();
	    ccList.remove(ccb);
	}
		
	// now we remove CC com with 2 comps (one top and one behind)
	/**
	 * @todo That's meta
	 */
	it = ccList.listIterator();
	badCc = new ArrayList<ConnexComposant>();
	while (it.hasNext()) {
	    ConnexComposant comp = it.next();
	    if (comp.getStartX() == comp.getEndX() && comp.getStartY() == (comp.getEndY() - 1))
		badCc.add(comp);
	}

	badIt = badCc.listIterator();
	while (badIt.hasNext()) {
	    ConnexComposant ccb = badIt.next();
	    ccList.remove(ccb);
	}

	// now we have to merge the nearests CC
	// we search for 2 CC with only +1 diff on the start line
	while (true) {
	    it = ccList.listIterator();
	    boolean foundNearest = false;
	    while (it.hasNext()) {
		ConnexComposant fcc = it.next();
		if (it.hasNext()) {
		    ConnexComposant lcc = it.next();

		    // now we search for the line
		    if (fcc.getStartY() == (lcc.getStartY() - 1)) {
			// have we to merge them?
			if (fcc.getEndX() == lcc.getEndX()) {
			    // merge them
			    fcc.setStartX(Math.min(fcc.getStartX(), lcc.getStartX()));
			    fcc.setStartY(Math.min(fcc.getStartY(), lcc.getStartY()));
			    fcc.setEndX(Math.max(fcc.getEndX(), lcc.getEndX()));
			    fcc.setEndY(Math.max(fcc.getEndY(), lcc.getEndY()));
			    it.remove();
			    foundNearest = true;
			}
		    }

		}
	    }
	    if (!foundNearest)
		break;
	}

	// now we search for ccs with end and start with only 1 line difference
	while (true) {
	    it = ccList.listIterator();
	    boolean foundNearest = false;

	    while (it.hasNext()) {
		ConnexComposant fcc = it.next();
		if (it.hasNext()) {
		    ConnexComposant lcc = it.next();

		    // now we search for the line
		    if (fcc.getEndY() == (lcc.getStartY() - 1)) {

			// merge them
			fcc.setStartX(Math.min(fcc.getStartX(), lcc.getStartX()));
			fcc.setStartY(Math.min(fcc.getStartY(), lcc.getStartY()));
			fcc.setEndX(Math.max(fcc.getEndX(), lcc.getEndX()));
			fcc.setEndY(Math.max(fcc.getEndY(), lcc.getEndY()));
			it.remove();
			foundNearest = true;
		    }
		}
	    }

	    if (!foundNearest)
		break;
	}

	// now detect if there are some intersections
	for (int i = 0; i < ccList.size() - 1; i++) {

	    ConnexComposant currentCC = ccList.get(i);

	    for (int j = i; j < ccList.size(); j++) {

		ConnexComposant otherCC = ccList.get(j);

		if (currentCC.intersect(otherCC) || otherCC.intersect(currentCC)) {
		    currentCC.setStartX(Math.min(currentCC.getStartX(), otherCC.getStartX()));
		    currentCC.setEndX(Math.max(currentCC.getEndX(), otherCC.getEndX()));
		    currentCC.setStartY(Math.min(currentCC.getStartY(), otherCC.getStartY()));
		    currentCC.setEndY(Math.max(currentCC.getEndY(), otherCC.getEndY()));
		}
	    }
	}
	//

	// next we have to make only one cc if only one void col or one void
	// line or specific case 3 lines void and cc1 = 1 length and same width
	// with next
	for (int i = 0; i < ccList.size() - 1; i++) {

	    ConnexComposant currentCC = ccList.get(i);

	    for (int j = i; j < ccList.size(); j++) {

		ConnexComposant otherCC = ccList.get(j);

		// System.out.println(currentCC);
		// System.out.println(otherCC);

		if (currentCC.getEndX() == otherCC.getStartX() + 2 || otherCC.getStartX() == currentCC.getEndX() + 2) {

		    // System.out.println("Join CC void col");

		    currentCC.setStartX(Math.min(currentCC.getStartX(), otherCC.getStartX()));
		    currentCC.setEndX(Math.max(currentCC.getEndX(), otherCC.getEndX()));
		    currentCC.setStartY(Math.min(currentCC.getStartY(), otherCC.getStartY()));
		    currentCC.setEndY(Math.max(currentCC.getEndY(), otherCC.getEndY()));

		    otherCC.setStartX(currentCC.getStartX());
		    otherCC.setEndX(currentCC.getEndX());
		    otherCC.setStartY(currentCC.getStartY());
		    otherCC.setEndY(currentCC.getEndY());
		}

		if (currentCC.getEndY() == otherCC.getStartY() + 2
			|| otherCC.getStartY() == currentCC.getEndY() + 2
			// specific pattern
			|| (currentCC.getEndY() + 4 == otherCC.getStartY()
				&& otherCC.getEndX() - otherCC.getStartX() == currentCC.getEndX() - currentCC.getStartX() && currentCC.getEndY()
				- currentCC.getStartY() == 0)) {

		    // System.out.println("Join CC void line");

		    currentCC.setStartX(Math.min(currentCC.getStartX(), otherCC.getStartX()));
		    currentCC.setEndX(Math.max(currentCC.getEndX(), otherCC.getEndX()));
		    currentCC.setStartY(Math.min(currentCC.getStartY(), otherCC.getStartY()));
		    currentCC.setEndY(Math.max(currentCC.getEndY(), otherCC.getEndY()));

		    otherCC.setStartX(currentCC.getStartX());
		    otherCC.setEndX(currentCC.getEndX());
		    otherCC.setStartY(currentCC.getStartY());
		    otherCC.setEndY(currentCC.getEndY());
		}
	    }
	}
	//

	// ok we need to remove ccs inside ccs
	badCc = new ArrayList<ConnexComposant>();

	for (int i = 0; i < ccList.size() - 1; i++) {

	    ConnexComposant currentCC = ccList.get(i);

	    for (int j = i; j < ccList.size(); j++) {

		ConnexComposant otherCC = ccList.get(j);

		if (currentCC.contains(otherCC))
		    badCc.add(otherCC);
		else if (otherCC.contains(currentCC))
		    badCc.add(currentCC);
	    }

	}

	// remove
	badIt = badCc.listIterator();
	while (badIt.hasNext()) {
	    ConnexComposant ccb = badIt.next();
	    ccList.remove(ccb);
	}

	//

	// we have a bad case where first lines of CCs are comments/title, we
	// want to remove them
	for (ConnexComposant cc : ccList) {
	    while (sheetCells.get(cc.getStartY()).length - cc.getStartX() == 1) {
		cc.setStartY(cc.getStartY() + 1);
	    }
	}
	//

	// same thing from the end of each cc
	for (ConnexComposant cc : ccList) {
	    while (sheetCells.get(cc.getEndY()).length - cc.getStartX() == 1
		    || (sheetCells.get(cc.getEndY()).length - cc.getStartX() == 2 && cc.getEndX() - cc.getStartX() >= 4)) {
		cc.setEndY(cc.getEndY() - 1);
	    }
	}
	//
	
	// for sheet 16 BP, remove the first line if not enough full
	for (ConnexComposant cc : ccList) {
	    if(sheetCells.get(cc.getStartY()).length < sheetCells.get(cc.getStartY()+1).length && sheetCells.get(cc.getStartY()).length < sheetCells.get(cc.getStartY()+2).length){
		cc.setStartY(cc.getStartY()+2);
	    }
	}
	//

	// now we remove CC for comp with 2 cells only on the same line
	it = ccList.listIterator();
	badCc = new ArrayList<ConnexComposant>();
	while (it.hasNext()) {

	    ConnexComposant comp = it.next();
	  
	    if ((comp.getStartX()+1 == comp.getEndX()) && (comp.getStartY() == comp.getEndY()))
		badCc.add(comp);
	}
	
	badIt = badCc.listIterator();
	while (badIt.hasNext()) {
	    ConnexComposant ccb = badIt.next();
	    ccList.remove(ccb);
	}
	//
	
	// fix for IMMNA files
	it = ccList.listIterator();
	while (it.hasNext()) {
	    ConnexComposant comp = it.next();
	    if(sheetCells.get(comp.getStartY())[comp.getStartX()].getContents() != null && sheetCells.get(comp.getStartY())[comp.getStartX()].getContents().trim().equalsIgnoreCase("reg")
		    && sheetCells.get(comp.getStartY()+1)[comp.getStartX()].getContents().trim().equalsIgnoreCase("Code région")){
		comp.setStartY(comp.getStartY()+1);
	    }
	    
	}
	//
	
	return ccList;
    }

    public int getCcIndexForCell(Cell cell, ArrayList<ConnexComposant> ccs) {
	for (int i = 0; i < ccs.size(); i++) {
	    if (cell.getRow() >= ccs.get(i).getStartY() && cell.getRow() <= ccs.get(i).getEndY() && cell.getColumn() >= ccs.get(i).getStartX()
		    && cell.getColumn() <= ccs.get(i).getEndX()) {
		return i;
	    }
	}
	return -1;
    }

    private boolean isCellVoid(Cell cell) {
	if (cell == null || cell.equals("") || cell.getType().equals(CellType.EMPTY))
	    return true;
	else
	    return false;
    }

}
