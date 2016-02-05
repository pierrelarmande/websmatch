/**
 *  Copyright 2008 The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mitre.schemastore.porters.schemaImporters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import jxl.Cell;
import jxl.CellType;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.inria.websmatch.connexComposant.ConnexComposantDetector;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.machineLearning.CellClassifier;
import org.inria.websmatch.machineLearning.MLInstanceCompute;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.Structures;
import org.inria.websmatch.xls.MetaDataExtractor;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Domain;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.ImporterException.ImporterExceptionType;
import org.mitre.schemastore.porters.URIType;

/**
 *
 * SpreadsheetImporter was a poor man's importer. It imports the schema of a
 * spreadsheet. This is very simplistic - the following are the assumptions:
 * <ul>
 * <li>Multiple worksheets are imported as separate tables - no effort is made
 * to link them through analysis of the formulas</li>
 * <li>No blank lines above or to the left of the data</li>
 * <li>The schema attribute names are in the first row</li>
 * <li>No breaks in the data listing (i.e., no blank rows until after all the
 * data is listed)</li>
 * </ul>
 *
 * @author Jeffrey Hoyt
 *
 *         Improvement made by ATLAS team at INRIA
 *
 *         Now it use JXL API instead of Apache POI API (better support for old
 *         Excel files -95 Excel files-) It can search for attributes in each
 *         sheet with some efforts :
 *         <ul>
 *         <li>It removes blank lines until finding the attributes</li>
 *         <li>It removes comments or such things before the table</li>
 *         <li>It removes blank columns too</li>
 *         </ul>
 *
 *         TODO Searching for multiple tables in only one sheet, getting
 *         attributes in matrix format table, linking between sheets with usage
 *         of formulas...
 *
 * @author Emmanuel Castanier
 *
 */
public class SpreadsheetImporter extends SchemaImporter {

	protected Workbook workbook;
	private URI sourceURI;
	private HashMap<String, Entity> entities;
	private HashMap<String, Attribute> attributes;
	protected ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>();
	private HashMap<String, Domain> domainList = new HashMap<String, Domain>();
	protected String documentation = "";

	// for DEBUG
	// private static boolean _DEBUG = true;

	// list of the row containing
	private ArrayList<List<Cell[]>> datas;

	// list of the connexCompo
	private ArrayList<ArrayList<ConnexComposant>> connexComps = new ArrayList<ArrayList<ConnexComposant>>();

	// list of cells wich are attributes
	private ArrayList<ArrayList<Cell>> attribCells;

	// names of sheets
	private ArrayList<String> sheetNames;

	// use ML or basic attribute detection mode
	private boolean useMLMode = true;

	// use MetaDataExtractor instead of ML
	private boolean useMetaDataExtractor = true;

	// ranges of merged cells
	private HashMap<Integer,ArrayList<int[]>> mergedCells = new HashMap<Integer,ArrayList<int[]>>();

	// ccCriters by sheet
	// private HashMap<Integer,HashMap<ConnexComposant,String>> criters = new HashMap<Integer,HashMap<ConnexComposant,String>>();

	public SpreadsheetImporter() {
		loadDomains();
	}

	// get rid of characters
	protected String cleanup(String s) {
		return s.trim().replaceAll("'", "''").replaceAll("\"", "\\\"");
	}

	protected String getCellValStr(Cell cell) {
		if (cell.getType().equals(CellType.BOOLEAN))
			return new Boolean(Boolean.parseBoolean(cell.getContents())).toString();
		else if (cell.getType().equals(CellType.NUMBER))
			return new Double(cell.getContents()).toString();
		else if (cell.getType().equals(CellType.LABEL))
			return cleanup(cell.getContents());
		else if (cell.getType().equals(CellType.EMPTY))
			return "";
		else if (cell.getType().equals(CellType.BOOLEAN_FORMULA) || cell.getType().equals(CellType.DATE_FORMULA)
				|| cell.getType().equals(CellType.NUMBER_FORMULA) || cell.getType().equals(CellType.STRING_FORMULA))
			return cell.getContents();
		else if (cell.getType().equals(CellType.ERROR) || cell.getType().equals(CellType.FORMULA_ERROR))
			return cell.getContents();
		else if (cell.getType().equals(CellType.DATE))
			return String.valueOf(cell.getContents());
		else
			return "";
	}

	/**
	 * Returns the JXL cell type.
	 */
	protected CellType getCellDataType(Cell cell) {
		if (cell == null) {
			return CellType.EMPTY;
		}
		return cell.getType();
	}

	/**
	 * Derive the schema from the contents of an Excel workbook This is where
	 * the magic happens!
	 */
	protected void generate() {
		int numSheets = workbook.getNumberOfSheets();

		attribCells = new ArrayList<ArrayList<Cell>>();
		datas = new ArrayList<List<Cell[]>>();
		sheetNames = new ArrayList<String>();
		connexComps = new ArrayList<ArrayList<ConnexComposant>>();

		MLInstanceCompute mlCompute = null;
		ConnexComposantDetector ccDetect = new ConnexComposantDetector();
		if (useMLMode)
			mlCompute = new MLInstanceCompute(this);

		CellClassifier classifier = null;
		try {
			classifier = new CellClassifier();
		} catch (Exception e) {
			System.out.println("Can't use ML, go to basic mode.");
			useMLMode = false;
			L.Error(e.getMessage(),e);
		}

		// iterate and load individual work sheets
		for (int s = 0; s < numSheets; s++) {
			Sheet sheet = workbook.getSheet(s);
			String sheetName = sheet.getName();
			sheetNames.add(sheetName);

			// for each sheet, we will search for the tables in it
			ArrayList<Cell[]> sheetCells = new ArrayList<Cell[]>();
			sheetCells = this.loadSheet(sheet);

			// now we search for the zones

			L.Debug(this.getClass().getSimpleName(),"Prepare to detect cc for sheet " + s,true);

			connexComps.add(ccDetect.connexDetection(sheetCells, sheet.getColumns()));

			// then
			// add merged cells (bug if before cc detect)
			Range[] ranges = sheet.getMergedCells();

			ArrayList<int[]> mCells = new ArrayList<int[]>();

			for(Range r : ranges){
				int[] t = new int[4];
				t[0] = r.getTopLeft().getColumn();
				t[1] = r.getTopLeft().getRow();
				t[2] = r.getBottomRight().getColumn();
				t[3] = r.getBottomRight().getRow();

				mCells.add(t);
			}

			mergedCells.put(new Integer(s), mCells);

			// debug
	    /*if(L._DEBUG){
		for(Range r : sheet.getMergedCells()){
		    System.out.println(r.getTopLeft());
		}
	    }*/

			L.Debug(this.getClass().getSimpleName(),"End of cc detection for sheet " + s,true);

			for (int i = 0; i < connexComps.get(s).size(); i++) {
				L.Debug(this.getClass().getSimpleName(),"Sheet ConnexComposant " + connexComps.get(s).get(i).toString(),true);
			}


			// there is only one row... so this sheet is useless, next
			// we keep it cause of XLS editor
	    /*
	     * if (sheet.getRows() <= 1) { continue; }
	     */
			// end of case
			ArrayList<Cell> sheetAttrCells = new ArrayList<Cell>();

			// now load all datas with filtering in CCs to avoid long
			// waiting...
			L.Debug(this.getClass().getSimpleName(),"Prepare load datas for sheet " + s,true);
			loadDatas(sheet, connexComps.get(s).listIterator());
			L.Debug(this.getClass().getSimpleName(),"End load datas for sheet " + s,true);

			//
			if (useMetaDataExtractor && this.getDatas().size() > s) {

				List<Cell[]> localDatas = this.getDatas().get(s);
				ArrayList<ConnexComposant> ccs = this.getConnexComps().get(s);

				MetaDataExtractor extractor = new MetaDataExtractor(ccs, localDatas);
				HashMap<ConnexComposant, ArrayList<Cell>> meta = extractor.computeExtraction(mergedCells.get(s));

				// put criters
				// criters.put(new Integer(s), extractor.getCriters());
				//

				// then with results make entities and attributes!
				ArrayList<Cell> tmpAttribCells = new ArrayList<Cell>();

				Set<ConnexComposant> keys = meta.keySet();

				for (ConnexComposant cc : keys) {

					// get attributes for CC
					ArrayList<Cell> ccAttrCells = meta.get(cc);

					// we attributes, make entity
					if (ccAttrCells.size() > 0) {

						// add the entities (map using sheetname + cc index)
						if (!entities.containsKey(sheetName + "-"
								+ ccDetect.getCcIndexForCell(localDatas.get(ccAttrCells.get(0).getRow())[ccAttrCells.get(0).getColumn()], ccs))) {
							String entityKey = sheetName + "-"
									+ ccDetect.getCcIndexForCell(localDatas.get(ccAttrCells.get(0).getRow())[ccAttrCells.get(0).getColumn()], ccs);
							Entity tblEntity = new Entity(nextId(), sheetName, "", 0);
							entities.put(entityKey, tblEntity);
						}

						for (Cell attrCell : ccAttrCells) {
							// create attributes, we need to find the domain
							tmpAttribCells.add(attrCell);

							// get the domain
							boolean voidDomain = false;

							// fisrt case, bidim array
							if(cc.isBiDimensionnalArray()){

								voidDomain = true;

							}

							else if (!cc.isAttrInLines()) {
								if (((attrCell.getRow() + 1) > localDatas.size())
										|| ((attrCell.getRow() + 1) < localDatas.size() && localDatas.get(attrCell.getRow() + 1).length < attrCell.getColumn() + 1)) {
									// on next row, this cell has nothing in...
									// in
									// this
									// case, we can use the string domain
									voidDomain = true;
								}
							} else {
								if (localDatas.get(attrCell.getRow()).length <= 1) {
									// on next col, this cell has nothing in...
									// in
									// this
									// case, we can use the string domain
									voidDomain = true;
								}
							}

							Domain domain = null;

							if (!cc.isAttrInLines()) {
								if (!voidDomain && localDatas.size() > attrCell.getRow() + 1) {
									Cell behind = localDatas.get(attrCell.getRow() + 1)[attrCell.getColumn()];
									L.Debug(this.getClass().getSimpleName(),"Behind "+behind.getContents() + " "+behind.getType(),true);
									// Get the domain attribute
									domain = domainList.get(translateType(behind.getType()));
								} else {
									domain = domainList.get(STRING);
								}
							} else {
								if (!voidDomain && localDatas.size() > attrCell.getRow() && localDatas.get(attrCell.getRow()) != null && localDatas.get(attrCell.getRow()).length > attrCell.getColumn() + 1) {
									Cell behind = localDatas.get(attrCell.getRow())[attrCell.getColumn() + 1];
									L.Debug(this.getClass().getSimpleName(),"Behind "+behind.getContents()+ " "+behind.getType(),true);
									// Get the domain attribute
									domain = domainList.get(translateType(behind.getType()));
								} else {
									domain = domainList.get(STRING);
								}
							}

							// we had this attribute to this entity
							// attribute
							// we try to add description for keeping x and y
							// of the cell (important for edition)
							// we have to split to get them
							Attribute attribute = new Attribute(nextId(), localDatas.get(attrCell.getRow())[attrCell.getColumn()].getContents(), "x:"
									+ attrCell.getColumn() + "|y:" + attrCell.getRow() + "|sheet:" + s, entities.get(
									sheetName + "-" + ccDetect.getCcIndexForCell(localDatas.get(attrCell.getRow())[attrCell.getColumn()], ccs)).getId(),
									domain.getId(), null, null, false, 0);

							// we use the name + desc for unicity in the
							// hashmap
							attributes.put(attribute.getName() + " " + attribute.getDescription(), attribute);

						}

						if (tmpAttribCells.size() > 0)
							sheetAttrCells.addAll(tmpAttribCells);

					}
				}
			}
			//

			// we have datas and ccs, we can use ML to know wich is attribute
			else if (useMLMode && this.getDatas().size() > s) {

				ArrayList<Cell> tmpAttribCells = new ArrayList<Cell>();
				// TODO work only on SimpleCell

				// get the datas and ccs
				List<Cell[]> localDatas = this.getDatas().get(s);
				ArrayList<ConnexComposant> ccs = this.getConnexComps().get(s);

				L.Debug(this.getClass().getSimpleName(),"Computing ml for sheet " + s,true);
				// now we have to go trough the cells
				for (int row = 0; row < localDatas.size(); row++) {

					for (int col = 0; col < localDatas.get(row).length; col++) {

						// calculate the simple cell
						SimpleCell sCell = mlCompute.computeValue(localDatas.get(row)[col], localDatas, ccs, s, row, col);

						sCell = classifier.classifyCell(sCell);

						// now if sCell is an attribute add to attr cells and
						// Schema
						if (sCell.isAttribute() && sCell.getIs_attributeML() == 1.0) {
							tmpAttribCells.add(localDatas.get(row)[col]);

							// get the domain
							boolean voidDomain = false;

							if (((row + 1) > localDatas.size()) || ((row + 1) < localDatas.size() && localDatas.get(row + 1).length < col + 1)) {
								// on next row, this cell has nothing in...
								// in
								// this
								// case, we can use the string domain
								voidDomain = true;
							}

							Domain domain = null;
							if (!voidDomain) {
								Cell behind = localDatas.get(row + 1)[col];
								// Get the domain attribute
								domain = domainList.get(translateType(behind.getType()));
							} else {
								domain = domainList.get(STRING);
							}

							// add the entities (map using sheetname + cc index)
							if (!entities.containsKey(sheetName + "-" + ccDetect.getCcIndexForCell(localDatas.get(row)[col], ccs))) {
								String entityKey = sheetName + "-" + ccDetect.getCcIndexForCell(localDatas.get(row)[col], ccs);
								Entity tblEntity = new Entity(nextId(), sheetName, "", 0);
								entities.put(entityKey, tblEntity);
							}
							// we had this attribute to this entity
							// attribute
							// we try to add description for keeping x and y
							// of the cell (important for edition)
							// we have to split to get them
							Attribute attribute = new Attribute(nextId(), localDatas.get(row)[col].getContents(), "x:" + col + "|y:" + row + "|sheet:" + s,
									entities.get(sheetName + "-" + ccDetect.getCcIndexForCell(localDatas.get(row)[col], ccs)).getId(), domain.getId(), null,
									null, false, 0);

							// we use the name + desc for unicity in the
							// hashmap
							attributes.put(attribute.getName() + " " + attribute.getDescription(), attribute);
						}
					}
					if (tmpAttribCells.size() > 0)
						sheetAttrCells.addAll(tmpAttribCells);
				}

				L.Debug(this.getClass().getSimpleName(),"End of computing ml for sheet " + s,true);

				// ok, no entity, we had the default sheet entity
				if (entities.size() == s) {
					String entityKey = sheetName + "-" + 0;
					Entity tblEntity = new Entity(nextId(), sheetName, "", 0);
					entities.put(entityKey, tblEntity);
				}

			}

			else {
				// we have to work on connexComps not on sheet
				// go on with cc
				ListIterator<ConnexComposant> itCc = connexComps.get(s).listIterator();

				while (itCc.hasNext()) {

					ArrayList<Cell> tmpAttribCells = new ArrayList<Cell>();
					ConnexComposant cc = itCc.next();

					// now more than one row
					// we have to work on sub arrays from minx to maxx of the cc
					int firstRow = cc.getStartY();
					Cell[] fullTopRow = sheet.getRow(firstRow); // first logical
					// row
					Object[] topRow = Structures.subArray(fullTopRow, cc.getStartX(), (cc.getEndX() - cc.getStartX() + 1));

					// finding the first non void row
					// if the row contains only one cell, that's a title
					while ((topRow == null || topRow.length <= 1
							|| Structures.subArray(sheet.getRow(firstRow + 1), cc.getStartX(), (cc.getEndX() - cc.getStartX() + 1)) == null || topRow.length < Structures
							.subArray(sheet.getRow(firstRow + 1), cc.getStartX(), (cc.getEndX() - cc.getStartX() + 1)).length)
							&& (firstRow + 1) <= cc.getEndY()) {
						firstRow++;
						fullTopRow = sheet.getRow(firstRow);
						topRow = Structures.subArray(fullTopRow, cc.getStartX(), (cc.getEndX() - cc.getStartX() + 1));
					}

					if (topRow == null || topRow.length <= 1) {
						continue;
					}
					// end of case

					// go on with attributes
					int itCcPrevIndex = itCc.previousIndex();
					Entity tblEntity = new Entity(nextId(), sheetName, "", 0);
					String entityKey = new String();
					if (entities.containsKey(sheetName))
						entityKey = sheetName + "-" + itCcPrevIndex;
					else
						entityKey = sheetName;
					entities.put(entityKey, tblEntity);

					// same with attCells
					for (int j = 0; j < topRow.length; j++) {
						if (((Cell) topRow[j]).getContents().length() > 0 && ((Cell) topRow[j]).getType() != CellType.NUMBER
								&& ((Cell) topRow[j]).getType() != CellType.NUMBER_FORMULA) {
							// we remove whitespace, dot and coma to test
							String tmp = ((Cell) topRow[j]).getContents().trim().replaceAll("\\s+", "");
							tmp = tmp.replaceAll(".", "");
							tmp = tmp.replaceAll(",", "");
							try {
								new Integer(tmp);
							} catch (NumberFormatException nfe) {
								// that's an attr
								tmpAttribCells.add((Cell) topRow[j]);
							}
						}
					}

					for (int i = 0; i < tmpAttribCells.size(); i++) {
						if (!isCellVoid(tmpAttribCells.get(i))) {
							Attribute attribute = null;
							// get the cell behind
							// if no cells behind, we can consider that's not an
							// attribute

							if (sheet.getRows() <= ((Cell) tmpAttribCells.get(i)).getRow() + 1) {

								// we are on last row...
								// remove this attribute!
								tmpAttribCells.remove(i);

							} else {

								boolean voidDomain = false;

								if (sheet.getRow(((Cell) tmpAttribCells.get(i))

										.getRow() + 1).length <= ((Cell) tmpAttribCells.get(i)).getColumn()) {

									// on next row, this cell has nothing in...
									// in
									// this
									// case, we can use the string domain
									voidDomain = true;

								}

								// domain
								Cell behind = null;
								Domain domain = null;
								if (!voidDomain) {
									behind = sheet.getRow(((Cell) tmpAttribCells.get(i)).getRow() + 1)[((Cell) tmpAttribCells.get(i)).getColumn()];
									// Get the domain attribute
									domain = domainList.get(translateType(behind.getType()));
								} else {
									domain = domainList.get(STRING);
								}

								// attribute
								// we try to add description for keeping x and y
								// of
								// the cell (important for edition)
								// we have to split to get them
								attribute = new Attribute(nextId(), tmpAttribCells.get(i).getContents(), "x:" + tmpAttribCells.get(i).getColumn() + "|y:"
										+ tmpAttribCells.get(i).getRow() + "|sheet:" + s, tblEntity.getId(), domain.getId(), null, null, false, 0);

								// we use the name + desc for unicity in the
								// hashmap
								attributes.put(attribute.getName() + " " + attribute.getDescription(), attribute);

							}
						}
					}
					// this entitie is useless
					if (tmpAttribCells.size() == 0) {
						entities.remove(entityKey);
						itCc.remove();
					} else
						sheetAttrCells.addAll(tmpAttribCells);
				}
			}

			// add the attrib for the sheet
			this.attribCells.add(sheetAttrCells);

			// end of working on comps
		}
		// end of working on sheets
		workbook.close();

		this.printFoundStructures();
	}

	private void printFoundStructures() {

		Iterator<Entity> it = entities.values().iterator();
		while (it.hasNext()) {
			L.Debug(this.getClass().getSimpleName(),"Entity " + it.next().toString(),true);
		}

		Iterator<Attribute> ita = attributes.values().iterator();
		while (ita.hasNext()) {
			Attribute att = ita.next();
			// get the domain
			Collection<Domain> list = (Collection<Domain>) domainList.values();
			String sdom = "Inconnu";
			Iterator<Domain> dit = list.iterator();
			while (dit.hasNext()) {
				Domain dom = dit.next();
				if (att.getDomainID() == dom.getId()) {
					sdom = dom.getName();
					break;
				}
			}

			L.Debug(this.getClass().getSimpleName(),"Attribute " + att.getName() + "\tDomain : " + sdom,true);
		}

	}

	public boolean isCellVoid(Cell cell) {
		if (cell == null || cell.getContents().trim().equals("") || cell.getType().equals(CellType.EMPTY))
			return true;
		else
			return false;
	}

	/**
	 * Load the cells from a sheet
	 *
	 * @param sheet
	 * @return The cells (JXL cells)
	 */

	private ArrayList<Cell[]> loadSheet(Sheet sheet) {

		ArrayList<Cell[]> cells = new ArrayList<Cell[]>();

		for (int i = 0; i < sheet.getRows(); i++)
			cells.add(sheet.getRow(i));

		return cells;
	}

	/**
	 * sets the datas. Use the CCs to keep only 20 lines by CCs
	 *
	 * @todo Paramter for the number of line filter
	 *
	 * @param itCc
	 */

	protected void loadDatas(Sheet sheet, ListIterator<ConnexComposant> itCc) {

		List<Cell[]> tmpDatas = new ArrayList<Cell[]>();

		// we filter only if sheet has more than 500 rows
	/*if (sheet.getRows() > 1000) {

	    // get the spaces to load rows...
	    int lastMaxLoadedRow = -1;

	    // go on with CCs
	    while (itCc.hasNext()) {

		ConnexComposant cc = itCc.next();

		// first case, CC is less or equals to 20 rows... load all
		if (cc.getStartY() > lastMaxLoadedRow) {
		    if ((cc.getEndY() - cc.getStartY()) < 1000) {

			for (int i = cc.getStartY(); i <= cc.getEndY(); i++)
			    tmpDatas.add(sheet.getRow(i));
			lastMaxLoadedRow = cc.getEndY();
		    } else {

			// too long cc, we cut after the 9 first lines.
			for (int i = cc.getStartY(); i < cc.getStartY() + 10; i++) {
			    tmpDatas.add(sheet.getRow(i));
			}
			// then add a cell with ...
			tmpDatas.add(new Cell[] { new Label(0, cc.getStartY() + 10, "...") });
			// then the last rows of the cc
			for (int i = cc.getEndY() - 9; i <= cc.getEndY(); i++) {
			    tmpDatas.add(sheet.getRow(i));
			}
			lastMaxLoadedRow = cc.getEndY();
		    }

		}
	    }
	} else {*/

		for (int i = 0; i < sheet.getRows(); i++) {

			tmpDatas.add(sheet.getRow(i));

			//    }
		}

		int howManyToRemove = 0;
		// for each tmpDatas, remove last blank lines
		for(int i = tmpDatas.size() - 1; i >= 0; i--){
			if(tmpDatas.get(i).length > 0) break;
			if(tmpDatas.get(i).length == 0) howManyToRemove++;
		}
		tmpDatas = tmpDatas.subList(0, tmpDatas.size()-howManyToRemove);

		datas.add(tmpDatas);

	}

	/**
	 * Translate the JXL CellType into the SchemaElement type Limitation :
	 * transform formulas in string without linking anything
	 *
	 * @param type
	 *            The JXL CellType
	 * @return The String representation (internal type)
	 */

	public String translateType(CellType type) {
		L.Debug(this,"Cell type : "+type.toString(),true);
		if (type.equals(CellType.BOOLEAN))
			return BOOLEAN;
		else if (type.equals(CellType.NUMBER))
			return REAL;
		else if (type.equals(CellType.ERROR) || type.equals(CellType.FORMULA_ERROR))
			throw new RuntimeException("There appears to be an error in the formulas in the spreadsheet");
		else if (type.equals(CellType.BOOLEAN_FORMULA) || type.equals(CellType.DATE_FORMULA) || type.equals(CellType.NUMBER_FORMULA)
				|| type.equals(CellType.STRING_FORMULA))
			return STRING;
		else if (type.equals(CellType.DATE))
			return DATETIME;
		else
			return STRING;
	}

	/** Returns the importer name */
	public String getName() {
		return "Spreadsheet Importer";
	}

	/** Returns the importer description */
	public String getDescription() {
		return "Imports Excel formatted schema into the schema store.";
	}

	/** Returns the importer URI type */
	public URIType getURIType() {
		return URIType.FILE;
	}

	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes() {
		ArrayList<String> filetypes = new ArrayList<String>(3);
		filetypes.add("xls");
		return filetypes;
	}

	protected void initialize() throws ImporterException {
		try {
			InputStream fileStream;
			entities = new HashMap<String, Entity>();
			attributes = new HashMap<String, Attribute>();

			// Do nothing if the excel sheet has been cached.
			if ((sourceURI != null) && sourceURI.equals(uri)) {
				return;
			}

			sourceURI = uri;
			fileStream = sourceURI.toURL().openStream();
			try {
				WorkbookSettings ws = new WorkbookSettings();
				ws.setSuppressWarnings(true);
				ws.setEncoding("ISO-8859-1");
				ws.setIgnoreBlanks(true);

				L.Debug(this.getClass().getSimpleName(),"GetWorkbook",true);

				workbook = Workbook.getWorkbook(new File(sourceURI), ws);

				L.Debug(this.getClass().getSimpleName(),"GetWorkbook ended",true);

			} catch (BiffException e) {
				L.Error(e.getMessage(),e);
			}
			fileStream.close();
		} catch (IOException e) {
			throw new ImporterException(ImporterExceptionType.PARSE_FAILURE, e.getMessage());
		}
	}

	/** Generate the schema elements */
	public ArrayList<SchemaElement> generateSchemaElements() throws ImporterException {
		generate();
		for (Entity e : entities.values()) {
			schemaElements.add(e);
		}
		for (Attribute a : attributes.values()) {
			schemaElements.add(a);
		}
		return schemaElements;
	}

	/**
	 * Function for loading the preset domains into the Schema and into a list
	 * for use during Attribute creation
	 */
	private void loadDomains() {
		Domain domain = new Domain(SchemaImporter.nextId(), ANY, "The Any domain", 0);
		schemaElements.add(domain);
		domainList.put(ANY, domain);
		domain = new Domain(SchemaImporter.nextId(), INTEGER, "The Integer domain", 0);
		schemaElements.add(domain);
		domainList.put(INTEGER, domain);
		domain = new Domain(SchemaImporter.nextId(), REAL, "The Real domain", 0);
		schemaElements.add(domain);
		domainList.put(REAL, domain);
		domain = new Domain(SchemaImporter.nextId(), STRING, "The String domain", 0);
		schemaElements.add(domain);
		domainList.put(STRING, domain);
		domain = new Domain(SchemaImporter.nextId(), DATETIME, "The DateTime domain", 0);
		schemaElements.add(domain);
		domainList.put(DATETIME, domain);
		domain = new Domain(SchemaImporter.nextId(), BOOLEAN, "The Boolean domain", 0);
		schemaElements.add(domain);
		domainList.put(BOOLEAN, domain);
	}

	public Integer getAnyDomainId() {
		return this.domainList.get(ANY).getId();
	}

	/**
	 * Some getters/setters
	 */

	public void setSheetNames(ArrayList<String> sheetNames) {
		this.sheetNames = sheetNames;
	}

	public ArrayList<String> getSheetNames() {
		return sheetNames;
	}

	public ArrayList<ArrayList<Cell>> getAttribCells() {
		return attribCells;
	}

	public void setAttribCells(ArrayList<ArrayList<Cell>> attribCells) {
		this.attribCells = attribCells;
	}

	public ArrayList<List<Cell[]>> getDatas() {
		return datas;
	}

	public void setDatas(ArrayList<List<Cell[]>> datas) {
		this.datas = datas;
	}

	public ArrayList<ArrayList<ConnexComposant>> getConnexComps() {
		return connexComps;
	}

	public void setConnexComps(ArrayList<ArrayList<ConnexComposant>> connexComps) {
		this.connexComps = connexComps;
	}

	public HashMap<String, Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(HashMap<String, Attribute> attributes) {
		this.attributes = attributes;
	}

	public HashMap<String, Entity> getEntities() {
		return entities;
	}

	public void setEntities(HashMap<String, Entity> entities) {
		this.entities = entities;
	}

	public boolean isUseMLMode() {
		return useMLMode;
	}

	public void setUseMLMode(boolean useMLMode) {
		this.useMLMode = useMLMode;
	}

	public HashMap<Integer,ArrayList<int[]>> getMergedCells() {
		return mergedCells;
	}

	public void setMergedCells(HashMap<Integer,ArrayList<int[]>> mergedCells) {
		this.mergedCells = mergedCells;
	}

    /*public HashMap<Integer, HashMap<ConnexComposant, String>> getCriters() {
        return criters;
    }

    public void setCriters(HashMap<Integer, HashMap<ConnexComposant, String>> criters) {
        this.criters = criters;
    }*/
}
