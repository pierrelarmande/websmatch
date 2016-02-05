package org.inria.websmatch.xls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jxl.Cell;
import jxl.CellType;

import org.inria.websmatch.dspl.EntityMatcherImpl;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.dspl.EntityMatcherImpl;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.utils.L;

public class MetaDataExtractor {

    List<Cell[]> localDatas;
    ArrayList<ConnexComposant> ccs;

    public MetaDataExtractor(ArrayList<ConnexComposant> ccs, List<Cell[]> localDatas2) {
	this.localDatas = localDatas2;
	this.ccs = ccs;
    }

    public HashMap<ConnexComposant, ArrayList<Cell>> computeExtraction(ArrayList<int[]> mergedCells) {

	HashMap<ConnexComposant, ArrayList<Cell>> attributeCellsForCCs = new HashMap<ConnexComposant, ArrayList<Cell>>();

	// int ccCount = 0;

	for (ConnexComposant cc : ccs) {

	    // we will keep the 4th first lines
	    String ccCriters = new String();
	    /*
	     * ccCount++;
	     * 
	     * ccCriters = "cc=" + ccCount + ";";
	     */

	    int ccLineCount = 0;
	    int ccColCount = 0;

	    if (cc.getEndX() - cc.getStartX() > 0) {
		ArrayList<Cell> attributes = new ArrayList<Cell>();
		ArrayList<ArrayList<Cell>> columns = new ArrayList<ArrayList<Cell>>();

		// create the cols
		for (int i = 0; i < cc.getEndX() - cc.getStartX() + 1; i++)
		    columns.add(new ArrayList<Cell>());
		int colCount = cc.getEndX() - cc.getStartX() + 1;

		// variables
		float percentStrLineOne = 0;
		float percentStrColOne = 0;

		float avgMaxLines = 0;
		float avgMaxCols = 0;

		boolean setFirstLine = false;
		boolean firstLine = true;
		boolean firstCol = true;

		Cell[] firstLineCells = null;
		Cell[] secondLineCells = null;

		Cell[] firstColCells = null;
		Cell[] secondColCells = null;

		// hack date detection on certain files (Wine files)
		if(localDatas.size() > 6 && localDatas.get(4)[0] != null && localDatas.get(4)[0].getContents().trim().equalsIgnoreCase("Date")){
		    attributes.add(localDatas.get(4)[0]);
		}
		//
		
		// hack demo detection of bi dimensionnal array
		if (localDatas.size() > 6 && localDatas.get(6).length > 2 && localDatas.get(6)[2].getContents().trim().equals("1000 t CO2 equivalent")) {
		    attributes.add(localDatas.get(7)[0]);
		    cc.setBiDimensionnalArray(true);
		    attributeCellsForCCs.put(cc, attributes);
		    return attributeCellsForCCs;
		}
		//

		// first search for collection on first line/first column for
		// bidim array
		Cell[] subArray = new Cell[0];
		if (localDatas.size() > cc.getStartY() && localDatas.get(cc.getStartY()).length < cc.getEndX() + 1 && localDatas.get(cc.getStartY()).length > 0)
		    subArray = Arrays.copyOfRange(localDatas.get(cc.getStartY()), cc.getStartX(), localDatas.get(cc.getStartY()).length);
		else if (localDatas.size() > cc.getStartY() && localDatas.get(cc.getStartY()).length > 0)
		    subArray = Arrays.copyOfRange(localDatas.get(cc.getStartY()), cc.getStartX(), cc.getEndX()+1);
		SimpleCell sCell = new SimpleCell(subArray[0].getContents(), false, subArray[0].getRow(), subArray[0].getColumn(), cc.getSheet());

		EntityMatcherImpl entMatch = new EntityMatcherImpl();

		// match the first line
		sCell = entMatch.matchLine(sCell, subArray);
		if (sCell.isDsplMapped()) {
		    ArrayList<int[]> currentErrors = sCell.getErrorList();
		    sCell = new SimpleCell(subArray[0].getContents(), false, subArray[0].getRow(), subArray[0].getColumn(), cc.getSheet());		   
		    // load the values
		    ArrayList<Cell> tmp = new ArrayList<Cell>();
		    for(int i = cc.getStartY()+1; i <= cc.getEndY(); i++){
			if(localDatas.size() > i && localDatas.get(i).length > cc.getStartX()) tmp.add(localDatas.get(i)[cc.getStartX()]);			
		    }
		    //
		    subArray = Arrays.copyOfRange(tmp.toArray(new Cell[tmp.size()]), 0, cc.getEndY() - cc.getStartY());		    
		    // match the column
		    sCell = entMatch.matchLine(sCell, subArray);
		    
		    if (sCell.isDsplMapped()) {
			// ok bidim
			// add the first errors
			for(int[] err : currentErrors){
			    ArrayList<int[]> tmpErr = sCell.getErrorList();
			    tmpErr.add(err);
			    sCell.setErrorList(tmpErr);
			}
			//
			attributes.add(localDatas.get(cc.getStartY())[cc.getStartX()]);
			cc.setBiDimensionnalArray(true);
			cc.setCriteria("");
			attributeCellsForCCs.put(cc, attributes);
			return attributeCellsForCCs;
		    }
		    // only in line
		    else{
			sCell.setErrorList(currentErrors);
			cc.setBiDimensionnalArray(false);
			cc.setAttrInLines(true);
			cc.setCriteria("");
			for(int line = cc.getStartY(); line <= cc.getEndY(); line++){
			    if(localDatas.get(line) != null && localDatas.get(line).length > cc.getStartX()) attributes.add(localDatas.get(line)[cc.getStartX()]);
			}
			attributeCellsForCCs.put(cc, attributes);
			L.Debug(this, "Attributes are in line.", true);
			return attributeCellsForCCs;
		    }
		}
		// else match first column
		else{	    
		    sCell = new SimpleCell(subArray[0].getContents(), false, subArray[0].getRow(), subArray[0].getColumn(), cc.getSheet());
		    // load the values
		    ArrayList<Cell> tmp = new ArrayList<Cell>();
		    for(int i = cc.getStartY()+1; i <= cc.getEndY(); i++){
			if(localDatas.size() > i && localDatas.get(i).length > cc.getStartX()) tmp.add(localDatas.get(i)[cc.getStartX()]);			
		    }
		    //
		    subArray = Arrays.copyOfRange(tmp.toArray(new Cell[tmp.size()]), 0, cc.getEndY() - cc.getStartY());	
		    sCell = entMatch.matchLine(sCell, subArray);
		    // column matched
		    if (sCell.isDsplMapped()) {
			cc.setBiDimensionnalArray(false);
			cc.setAttrInLines(false);
			cc.setCriteria("");
			for(int col = cc.getStartX(); col <= cc.getEndX(); col++){
			    if(localDatas.get(cc.getStartY()).length > col) attributes.add(localDatas.get(cc.getStartY())[col]);
			}
			attributeCellsForCCs.put(cc, attributes);
			L.Debug(this, "Attributes are in column.", true);
			return attributeCellsForCCs;
		    }		   
		}
		//

		// ok, we need to use the CCs to choose lines/columns
		// for the lines
		for (int i = cc.getStartY(); i <= cc.getEndY(); i++) {

		    subArray = new Cell[0];
		    if (localDatas.size() > i && localDatas.get(i).length < cc.getEndX() + 1 && localDatas.get(i).length > 0)
			subArray = Arrays.copyOfRange(localDatas.get(i), cc.getStartX(), localDatas.get(i).length);
		    else if (localDatas.size() > i && localDatas.get(i).length > 0)
			subArray = Arrays.copyOfRange(localDatas.get(i), cc.getStartX(), cc.getEndX() + 1);
		    if (firstLine) {
			firstLineCells = subArray;

			//
			Cell[] secondSubArray = new Cell[0];
			if (localDatas.size() > i + 1 && localDatas.get(i + 1).length < cc.getEndX() + 1 && localDatas.get(i + 1).length > 0)
			    secondSubArray = Arrays.copyOfRange(localDatas.get(i + 1), cc.getStartX(), localDatas.get(i + 1).length);
			else if (localDatas.size() > i + 1 && localDatas.get(i + 1).length > 0)
			    secondSubArray = Arrays.copyOfRange(localDatas.get(i + 1), cc.getStartX(), cc.getEndX() + 1);

			secondLineCells = secondSubArray;
			//
		    }

		    for (int col = 0; col < colCount; col++) {
			if (subArray.length > col)
			    columns.get(col).add(subArray[col]);
		    }

		    if (subArray != null) {
			HashMap<String, Float> res = this.getTypesForCollection(subArray);

			// fill criters
			ccLineCount++;
			if (ccLineCount < 5) {
			    // ccCriters += "line=" + ccLineCount + ";";
			    Set<String> ccKeys = res.keySet();
			    for (String key : ccKeys) {

				if (res.get(key).isNaN()) {
				    ccCriters += key + "_l" + ccLineCount + "=0.0;";
				} else {
				    String value = res.get(key).toString();
				    if (value.length() > 5)
					value = value.substring(0, 5);
				    ccCriters += key + "_l" + ccLineCount + "=" + value + ";";
				}
			    }
			}
			//

			if (firstLine == true) {
			    if (res.get("void").floatValue() < 0.5) {
				percentStrLineOne = res.get("string");
				setFirstLine = true;
			    }
			}

			// get the max and add it
			float tmpMax = 0;
			Set<String> keys = res.keySet();
			for (String key : keys)
			    if (res.get(key).floatValue() > tmpMax)
				tmpMax = res.get(key).floatValue();

			avgMaxLines += tmpMax;
		    }

		    if (setFirstLine)
			firstLine = false;
		}
		// calculate avg
		avgMaxLines = avgMaxLines / (cc.getEndY() - cc.getStartY() + 1);

		// for the colummns
		L.Debug(this.getClass().getSimpleName(), "MetaData In columns ", true);

		for (int i = 0; i < columns.size(); i++) {

		    ArrayList<Cell> tmp = columns.get(i);

		    // if(_DEBUG)
		    // System.out.println(" Column Size : "+tmp.size());

		    subArray = Arrays.copyOfRange(tmp.toArray(new Cell[tmp.size()]), 0, cc.getEndY() - cc.getStartY());

		    if (firstCol) {
			firstColCells = subArray;

			//
			if (i + 1 < columns.size()) {
			    Cell[] secondSubArray = Arrays.copyOfRange(columns.get(i + 1).toArray(new Cell[columns.get(i + 1).size()]), 0,
				    cc.getEndY() - cc.getStartY());
			    secondColCells = secondSubArray;
			}
			//

		    }

		    if (subArray != null) {
			HashMap<String, Float> res = this.getTypesForCollection(subArray);

			// fill criters
			ccColCount++;
			if (ccColCount < 5) {
			    // ccCriters += "col=" + ccColCount + ";";
			    Set<String> ccKeys = res.keySet();
			    for (String key : ccKeys) {
				if (res.get(key).isNaN()) {
				    ccCriters += key + "_c" + ccColCount + "=0.0;";
				} else {
				    String value = res.get(key).toString();
				    if (value.length() > 5)
					value = value.substring(0, 5);
				    ccCriters += key + "_c" + ccColCount + "=" + value + ";";
				}
			    }
			}
			//

			if (firstCol == true) {
			    percentStrColOne = res.get("string");
			}

			// get the max and add it
			float tmpMax = 0;
			Set<String> keys = res.keySet();
			for (String key : keys)
			    if (res.get(key).floatValue() > tmpMax)
				tmpMax = res.get(key).floatValue();

			avgMaxCols += tmpMax;
		    }

		    firstCol = false;
		}
		// calculate avg
		avgMaxCols = avgMaxCols / (cc.getEndX() - cc.getStartX() + 1);

		if (Float.isNaN(percentStrLineOne))
		    percentStrLineOne = 0;
		if (Float.isNaN(percentStrColOne))
		    percentStrColOne = 0;
		if (Float.isNaN(avgMaxLines))
		    avgMaxLines = 0;
		if (Float.isNaN(avgMaxCols))
		    avgMaxCols = 0;

		L.Debug(this.getClass().getSimpleName(), "percentStrLineOne " + percentStrLineOne, true);
		L.Debug(this.getClass().getSimpleName(), "percentStrColOne " + percentStrColOne, true);
		L.Debug(this.getClass().getSimpleName(), "avgMaxLines " + avgMaxLines, true);
		L.Debug(this.getClass().getSimpleName(), "avgMaxCols " + avgMaxCols, true);

		// now we decide
		// added a new arg when long list
		if (((double) (cc.getEndY() - cc.getStartY()) / (double) (cc.getEndX() - cc.getStartX())) > 3
			|| (percentStrLineOne == 1 && percentStrColOne < 1) || ((percentStrLineOne >= percentStrColOne) && (avgMaxLines < avgMaxCols))) {
		    L.Debug(this.getClass().getSimpleName(), "Attributes in col for this CC.", true);
		    if (firstLineCells != null) {
			for (int i = 0; i < firstLineCells.length; i++) {
			    if (firstLineCells[i] != null && firstLineCells[i].getContents() != null && !firstLineCells[i].getContents().equals("")) {
				attributes.add(firstLineCells[i]);
			    }
			}

			// see if cells are merged if yes, check next line
			boolean hasMergedCell = false;

			for (int[] mCell : mergedCells) {
			    for (Cell attr : attributes) {
				if (attr.getColumn() >= mCell[0] && attr.getRow() >= mCell[1] && attr.getColumn() <= mCell[2] && attr.getRow() <= mCell[3]) {
				    hasMergedCell = true;
				}
			    }
			}

			if (hasMergedCell) {
			    if (secondLineCells != null) {
				for (int i = 0; i < secondLineCells.length; i++) {
				    if (secondLineCells[i] != null && secondLineCells[i].getContents() != null && !secondLineCells[i].getContents().equals("")) {
					attributes.add(secondLineCells[i]);
				    }
				}
			    }
			}
			cc.setCriteria(ccCriters);
			// add attributes
			attributeCellsForCCs.put(cc, attributes);
		    }
		} else {
		    L.Debug(this.getClass().getSimpleName(), "Attributes in line for this CC.", true);
		    if (firstColCells != null) {
			for (int i = 0; i < firstColCells.length; i++) {
			    if (firstColCells[i] != null && firstColCells[i].getContents() != null && !firstColCells[i].getContents().equals(""))
				attributes.add(firstColCells[i]);
			}
			cc.setAttrInLines(true);

			// see if cells are merged if yes, check next line
			boolean hasMergedCell = false;

			for (int[] mCell : mergedCells) {
			    for (Cell attr : attributes) {
				if (attr.getColumn() >= mCell[0] && attr.getRow() >= mCell[1] && attr.getColumn() <= mCell[2] && attr.getRow() <= mCell[3]
					&& mCell[2] - mCell[0] == 1) {
				    hasMergedCell = true;
				}
			    }
			}

			if (hasMergedCell) {
			    if (secondColCells != null) {
				for (int i = 0; i < secondColCells.length; i++) {
				    if (secondColCells[i] != null && secondColCells[i].getContents() != null && !secondColCells[i].getContents().equals("")) {
					attributes.add(secondColCells[i]);
				    }
				}
			    }
			}
			//
			cc.setCriteria(ccCriters);
			attributeCellsForCCs.put(cc, attributes);
		    }
		}
	    }
	}
	return attributeCellsForCCs;
    }

    public HashMap<String, Float> getTypesForCollection(Cell[] cells) {

	HashMap<String, Float> results = new HashMap<String, Float>();

	int totalLength = cells.length;

	int voidCount = 0;
	int integerCount = 0;
	int floatCount = 0;
	int dateCount = 0;
	int booleanCount = 0;
	int stringCount = 0;

	for (int i = 0; i < cells.length; i++) {

	    if (cells[i] != null) {

		if (cells[i].getContents() == null || cells[i].getContents().equals("") || cells[i].getType().equals(CellType.EMPTY))
		    voidCount++;	
		else if ((cells[i].getType().equals(CellType.NUMBER) || cells[i].getType().equals(CellType.NUMBER_FORMULA))
			&& !cells[i].getContents().contains(".") && !cells[i].getContents().contains(","))
		    integerCount++;
		else if (cells[i].getType().equals(CellType.NUMBER) || cells[i].getType().equals(CellType.NUMBER_FORMULA))
		    floatCount++;
		else if (cells[i].getType().equals(CellType.DATE) || cells[i].getType().equals(CellType.DATE_FORMULA))
		    dateCount++;
		else if (cells[i].getType().equals(CellType.BOOLEAN) || cells[i].getType().equals(CellType.BOOLEAN_FORMULA))
		    booleanCount++;
		
		// for case where people use string as numbers...
		else if(cells[i].getType().equals(CellType.LABEL)){
		    try{
			new Integer(cells[i].getContents().trim());
			integerCount++;
		    }catch(NumberFormatException nfe){
			stringCount++;
		    }
		}
		//
		
		//else stringCount++;
	    }

	}

	results.put("void", (float) voidCount / (float) totalLength);
	results.put("integer", (float) integerCount / (float) totalLength);
	results.put("float", (float) floatCount / (float) totalLength);
	results.put("date", (float) dateCount / (float) totalLength);
	results.put("boolean", (float) booleanCount / (float) totalLength);
	results.put("string", (float) stringCount / (float) totalLength);

	this.printResults(results);

	return results;
    }

    private void printResults(HashMap<String, Float> res) {
	Set<String> keys = res.keySet();

	for (String key : keys) {
	    L.Debug(this.getClass().getSimpleName(), key + " " + res.get(key) + "\t", true);
	}

	L.Debug(this.getClass().getSimpleName(), "", true);
    }
}
