package org.inria.websmatch.dsplEngine;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.inria.websmatch.dspl.DSPLColumn;
import org.inria.websmatch.dspl.DSPLSlice;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.dspl.DSPLColumn;
import org.inria.websmatch.dspl.DSPLSlice;
import org.inria.websmatch.utils.L;

public class DSPLEngineSerializer {

    private String doc_url;
    private LinkedList<DSPLSlice> slicesList;

    public DSPLEngineSerializer(String doc_url, LinkedList<DSPLSlice> slices) {
	this.doc_url = doc_url;
	this.slicesList = slices;
    }

    /**
     * This method is used to clean strings for DSPL export, what it does :
     * Replace spaces by underscore for characters Replace comma by dot to be US
     * standard Replace backquote by _ Replace (); by underscore
     * 
     * @param s
     *            The string to clean
     * @return The cleaned string
     */

    private String dsplCleanString(String s) {

	// System.out.println("Clean header : "+s);

	String cleaned = s.trim();

	// unaccent
	String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
	Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	cleaned = pattern.matcher(temp).replaceAll("");

	cleaned = cleaned.replaceAll("/", "_");
	cleaned = cleaned.replaceAll("\\s+", "_");
	cleaned = cleaned.replaceAll(",", ".");
	cleaned = cleaned.replaceAll("\\'", "_");
	cleaned = cleaned.replaceAll("\\(", "_");
	cleaned = cleaned.replaceAll("\\)", "_");
	cleaned = cleaned.replaceAll(";", "_");
	cleaned = cleaned.replaceAll("%", "");
	// remove the thousand separator
	cleaned = cleaned.replaceAll("\\xA0", "");
	cleaned = cleaned.replace('-', '_');
	// remove cause of scripting in DSPLE
	cleaned = cleaned.replace('$', '_');
	cleaned = cleaned.replace(':', '_');

	return cleaned;
    }

    public String[] convertDSPLType(String dsplOrigin) {

	String[] converted = new String[2];

	String[] tmp = dsplOrigin.split(":");
	if (tmp[0].equals("time")) {
	    converted[0] = "date";
	    if (tmp[1].equals("year"))
		converted[1] = "yyyy";
	}

	return converted;

    }

    public String toXML() {

	ArrayList<String> entityTables = new ArrayList<String>();

	// concepts
	String concepts = new String();

	concepts += "<concepts>";

	// slices
	String slices = new String();

	slices += "<slices>";

	// tables
	String tables = new String();

	for (DSPLSlice slice : slicesList) {

	    slices += "<slice id=\"" + slice.getFileName().substring(0, slice.getFileName().lastIndexOf(".")) + "\">";
	    tables += "<table id=\"" + slice.getFileName().substring(0, slice.getFileName().lastIndexOf(".")) + "_table" + "\" filename=\""
		    + slice.getFileName() + "\">";
	    LinkedList<DSPLColumn> cols = slice.getColumns();

	    for (DSPLColumn tmpCol : cols) {
		// DSPL type
		if (tmpCol.getType().contains(":")) {
		    // it's a time:year or time:time_point
		    if (tmpCol.getType().startsWith("time:")) {
			// ok generate all the needed things (namespace and
			// extends)
			concepts += "<concept id=\"" + dsplCleanString(tmpCol.getId())
				+ "\" xmlns:time=\"http://www.google.com/publicdata/dataset/google/time\" extends=\"" + tmpCol.getType() + "\" type=\"date\">";
			concepts += generateNameAndDescription(tmpCol);
			concepts += "</concept>";
		    }
		    // it's a ratio
		    else if (tmpCol.getType().equals("quantity:ratio")) {
			concepts += "<concept id=\"" + dsplCleanString(tmpCol.getId()) + "\" extends=\"" + tmpCol.getType() + "\" type=\"float\">";
			concepts += generateNameAndDescription(tmpCol);
			// attribute
			concepts += "<attribute id=\"is_percentage\">";
			concepts += "<value>true</value>";
			concepts += "</attribute>";
			concepts += "</concept>";
		    }
		    // an entity defined by user
		    else if (tmpCol.getType().split(":")[0].equals("entity") && tmpCol.getType().split(":")[1].equals("entity")) {
			slices += "<dimension concept=\"" + dsplCleanString(tmpCol.getId()) + "\"/>";
			concepts += "<concept id=\"" + dsplCleanString(tmpCol.getId()) + "\" extends=\"" + tmpCol.getType() + "\" type=\"string\">";
			concepts += generateNameAndDescription(tmpCol);
			//
			concepts += "<property id=\"name\">";
			concepts += "<name>";
			concepts += "Nom";
			concepts += "</name>";
			concepts += "<type ref=\"string\"/>";
			concepts += "</property>";
			concepts += "<table ref=\"" + dsplCleanString(tmpCol.getId()) + "_table\"/>";
			concepts += "</concept>";
		    }

		    // zone geo
		    else if (tmpCol.getType().split(":")[0].equals("geo") && tmpCol.getType().split(":")[1].equals("location")) {
			concepts += "<concept id=\"" + dsplCleanString(tmpCol.getId())
				+ "\" xmlns:geo=\"http://www.google.com/publicdata/dataset/google/geo\" extends=\"geo:location\" type=\"string\">";
			concepts += generateNameAndDescription(tmpCol);
			concepts += "</concept>";
		    }
		    // else dp
		    else if (tmpCol.getType().startsWith("dp:")) {
			concepts += "<concept id=\"" + dsplCleanString(tmpCol.getId()) + "\" xmlns:dp=\"http://www.data-publica.com/geo\" extends=\""
				+ tmpCol.getType() + "\" type=\"string\">";
			concepts += generateNameAndDescription(tmpCol);
			concepts += "</concept>";
		    } else
			slices += "<dimension concept=\"" + tmpCol.getType() + "\"/>";
		}
		// not a DSPL type
		else {
		    concepts += "<concept id=\"" + dsplCleanString(tmpCol.getId()) + "\" type=\"" + tmpCol.getType() + "\">";
		    concepts += generateNameAndDescription(tmpCol);
		    concepts += "</concept>";
		}

		if (tmpCol.getType().contains(":") && tmpCol.getType().split(":")[0].equals("entity") && tmpCol.getType().split(":")[1].equals("entity")) {
		    tables += "<column type=\"string\">" + dsplCleanString(tmpCol.getId()) + "</column>";
		    // create the table
		    String tmpTable = new String();
		    tmpTable += "<table id=\"" + dsplCleanString(tmpCol.getId()) + "_table\">";
		    tmpTable += "<column type=\"string\">" + dsplCleanString(tmpCol.getId()) + "</column>";
		    tmpTable += "<column type=\"string\">" + "name" + "</column>";
		    tmpTable += "<data>";
		    tmpTable += "<file format=\"csv\" encoding=\"utf-8\">" + dsplCleanString(tmpCol.getId()) + ".csv</file>";
		    tmpTable += "</data>";
		    tmpTable += "</table>";
		    entityTables.add(tmpTable);
		} else if (tmpCol.getType().split(":")[0].equals("geo") && tmpCol.getType().split(":")[1].equals("location")) {
		    tables += "<column type=\"string\">" + tmpCol.getId() + "</column>";
		} else if (tmpCol.getType().split(":")[0].equals("dp")) {
		    tables += "<column type=\"string\">" + dsplCleanString(tmpCol.getId()) + "</column>";
		} else if (tmpCol.getType().contains(":") && !tmpCol.getType().split(":")[0].equals("quantity") && !tmpCol.getType().split(":")[0].equals("dp")) {
		    String[] res = this.convertDSPLType(tmpCol.getType());
		    // FIXME added for buggy Data Publica import
		    if (tmpCol.getType().split(":")[1].equals("year") || tmpCol.getType().split(":")[1].equals("yyyy"))
			tables += "<column type=\"" + res[0] + "\" format=\"" + res[1] + "\">" + dsplCleanString(tmpCol.getId()) + "</column>";
		    else
		    // if it's a time_point, find the fomat
		    if (tmpCol.getType().equals("time:time_point") && !tmpCol.getFormat().equals("MQyy")) {
			tables += "<column type=\"date\" format=\"" + tmpCol.getFormat() + "\">" + dsplCleanString(tmpCol.getId()) + "</column>";
		    } else if(tmpCol.getType().equals("time:time_point") && tmpCol.getFormat().equals("MQyy")){
			tables += "<column type=\"date\" format=\"MM/yyyy\">" + dsplCleanString(tmpCol.getId()) + "</column>";
		    }
		    else tables += "<column type=\"" + res[0] + "\" format=\"" + res[1] + "\">" + tmpCol.getType().split(":")[1] + "</column>";
		} else
		    tables += "<column type=\"" + tmpCol.getType() + "\" metric=\"true\">" + dsplCleanString(tmpCol.getId()) + "</column>";
	    }
	    tables += "</table>";
	    slices += tables;
	    slices += "</slice>";
	}
	concepts += "</concepts>";
	slices += "</slices>";
	// specific code for engine part
	// FIXME put this in antoher method?
	String engineXML = new String();
	engineXML += generateReadersXML();
	engineXML += "<writers/></engine>";
	// end of engine XML
	return concepts + slices + "</descriptor>" + engineXML + "</config>";
    }

    private String generateNameAndDescription(DSPLColumn tmpCol) {
	String concepts = new String();
	concepts += "<name>";
	concepts += StringEscapeUtils.escapeXml(tmpCol.getId());
	concepts += "</name>";
	concepts += "<description>";
	concepts += StringEscapeUtils.escapeXml(tmpCol.getDescription());
	concepts += "</description>";
	return concepts;
    }

    public String generateReadersXML() {

	// for the individual scripting by cell
	LinkedHashMap<DSPLColumn, ArrayList<String[]>> indivScripts = new LinkedHashMap<DSPLColumn, ArrayList<String[]>>();
	//

	String engine = new String();

	boolean conditionNeeded = false;
	boolean needNumericClose = false;

	engine += "<engine refresh=\"0 0 1 1 * ?\">";
	engine += "<readers>";
	engine += "<reader processor=\"UrlReader\" url=\"file:" + doc_url + "\">";
	engine += "<parser processor=\"XlsParser\">";
	engine += "<filters>";
	engine += "<filter processor=\"NotNullFilter\">";
	engine += "<filters>";

	for (DSPLSlice slice : slicesList) {

	    boolean attrInLine = slice.isInLineAttr();
	    boolean biDim = slice.isBiDim();
	    // for bidim, x and y to modify
	    LinkedHashMap<Integer, String> yCondMap = new LinkedHashMap<Integer, String>();
	    LinkedHashMap<Integer, String> xCondMap = new LinkedHashMap<Integer, String>();
	    //

	    // cache keys and [type,format]
	    LinkedHashMap<String, DSPLColumn> caches = new LinkedHashMap<String, DSPLColumn>();

	    // keep the inverted condition
	    String invertedCondition = new String();

	    // first set a boolean if conditional filter needed
	    for (int i = 0; i < slice.getColumns().size(); i++) {
		DSPLColumn col = slice.getColumns().get(i);
		// for this column we generate specific scripts
		HashMap<int[], String> sMap = col.getSpecScripts();
		Set<int[]> keys = sMap.keySet();
		ArrayList<String[]> tmp = new ArrayList<String[]>();
		for (int[] key : keys) {
		    if (!sMap.get(key).equals("")) {
			if (attrInLine) {
			    tmp.add(this.generateIndividualScripts(key[1], -1, sMap.get(key), dsplCleanString(col.getId()) + "_' + cell.column"));
			    invertedCondition = "cell.column != " + (key[1] - 1);
			    conditionNeeded = true;
			} else if (biDim) {
			    tmp.add(this.generateIndividualScripts(key[0], key[1], sMap.get(key), dsplCleanString(col.getId()) + "_' + cell.column"));
			    String tmpCond = "(cell.column != " + (key[0] - 1) + " " + StringEscapeUtils.escapeXml("&&") + " cell.row != " + (key[1] - 1) + ")";
			    if (invertedCondition.equals(""))
				invertedCondition = tmpCond;
			    else if (invertedCondition.indexOf(tmpCond) == -1)
				invertedCondition += " " + StringEscapeUtils.escapeXml("&&") + " " + tmpCond;
			    // first column y
			    if (i == 0) {
				yCondMap.put(key[1], sMap.get(key));
			    } else
			    // second column x
			    if (i == 1) {
				xCondMap.put(key[0], sMap.get(key));
			    }
			    conditionNeeded = true;
			} else {
			    tmp.add(this.generateIndividualScripts(-1, key[1], sMap.get(key), dsplCleanString(col.getId())));
			    if (invertedCondition.equals(""))
				invertedCondition += this.generateIndividualScripts(-1, key[1], sMap.get(key), dsplCleanString(col.getId()))[0].replace("==",
					"!=");
			    else
				invertedCondition += " " + StringEscapeUtils.escapeXml("&&") + " "
					+ this.generateIndividualScripts(-1, key[1], sMap.get(key), dsplCleanString(col.getId()))[0].replace("==", "!=");
			    conditionNeeded = true;
			}
		    }
		}
		indivScripts.put(col, tmp);
		//
	    }
	    //

	    String numericFilter = new String();

	    for (int i = 0; i < slice.getColumns().size(); i++) {

		int conditionCount = 0;
		DSPLColumn col = slice.getColumns().get(i);

		// add a notnullfilter

		// a filter for each col
		engine += "<filter processor=\"RangeFilter\">";

		engine += "<params>";
		if (attrInLine) {
		    engine += "<param name=\"range\">R" + (col.getLine() - 1) + "C" + col.getCol() + ":R" + (col.getLine() - 1) + "C" + (slice.getCcEndX() - 1)
			    + "</param>";
		} else if (biDim) {
		    // in this case, we have to cache the values position to
		    // construct the CSV file
		    // and give the total square
		    if (i == 0)
			engine += "<param name=\"range\">R" + (col.getLine() - 1) + "C" + (col.getCol() - 1) + ":R" + (slice.getCcEndY() - 1) + "C"
				+ (col.getCol() - 1) + "</param>";
		    else if (i == 1)
			engine += "<param name=\"range\">R" + (col.getLine() - 1) + "C" + (col.getCol() - 1) + ":R" + (col.getLine() - 1) + "C"
				+ (slice.getCcEndX() - 1) + "</param>";
		    else
			engine += "<param name=\"range\">R" + (slice.getColumns().get(0).getLine() - 1) + "C" + (col.getCol() - 1) + ":R"
				+ (slice.getCcEndY() - 1) + "C" + (slice.getCcEndX() - 1) + "</param>";
		    //
		} else {
		    // TODO fix this, specific case for BP sheet 9
		    if (!slice.isInLineAttr() && !slice.isBiDim() && slice.getColumns().getFirst().getType().indexOf("time:year") != -1
			    && slice.getSheetName().indexOf("Oil –  Spot crude prices") != -1) {
			col.setLine(col.getLine() + 4);
		    }
		    engine += "<param name=\"range\">R" + col.getLine() + "C" + (col.getCol() - 1) + ":R" + (slice.getCcEndY() - 1) + "C" + (col.getCol() - 1)
			    + "</param>";
		}
		engine += "<param name=\"inclusive\">true</param>";
		engine += "</params>";

		// cache only if not the last
		if (i < slice.getColumns().size() - 1) {

		    engine += "<handlers>";
		    engine += "<handler processor=\"CacheHandler\">";
		    engine += "<params>";
		    if (attrInLine) {
			engine += "<param name=\"key\">'" + dsplCleanString(col.getId()) + "_' + cell.column</param>";
			caches.put(dsplCleanString(col.getId()) + "_' + cell.column", col);
			// add numeric filter
			if (col.getType().equals("float") || col.getType().equals("integer")) {
			    if (numericFilter.equals(""))
				numericFilter = "cache('" + dsplCleanString(col.getId()) + "_' + cell.column).formatter.isNumeric() == true";
			    else
				numericFilter += " &amp;&amp; " + "cache('" + dsplCleanString(col.getId()) + "_' + cell.column).formatter.isNumeric() == true";
			}
		    } else if (biDim) {
			if (i == 0) {
			    engine += "<param name=\"key\">'" + dsplCleanString(col.getId()) + "_' + cell.row</param>";
			    caches.put(dsplCleanString(col.getId()) + "_' + cell.row", col);
			} else {
			    engine += "<param name=\"key\">'" + dsplCleanString(col.getId()) + "_' + cell.column</param>";
			    caches.put(dsplCleanString(col.getId()) + "_' + cell.column", col);
			}
		    } else {
			engine += "<param name=\"key\">'" + dsplCleanString(col.getId()) + "'</param>";
			caches.put(dsplCleanString(col.getId()), col);
			if (col.getType().equals("float") || col.getType().equals("integer")) {
			    if (numericFilter.equals(""))
				numericFilter = "cache('" + dsplCleanString(col.getId()) + "').formatter.isNumeric() == true";
			    else
				numericFilter += " &amp;&amp; " + "cache('" + dsplCleanString(col.getId()) + "').formatter.isNumeric() == true";
			}
		    }
		    engine += "</params>";
		    engine += "</handler>";

		    engine += "</handlers>";

		    engine += "</filter>";

		    L.Debug(this, "DSPLCol type " + col.getType(), true);
		}

		// the last one
		else {
		    // if needed construct numeric filter
		    if (col.getType().equals("float") || col.getType().equals("integer")) {
			// add a filter to avoid parse exception
			if (numericFilter.equals(""))
			    numericFilter = "cell.formatter.isNumeric() == true";
			else
			    numericFilter += " &amp;&amp; cell.formatter.isNumeric() == true";
		    }

		    if (!numericFilter.equals("")) {
			engine += "<filters><filter processor=\"ConditionalFilter\"><params><param name=\"condition\">" + numericFilter + "</param></params>";
			needNumericClose = true;
		    }

		    if (conditionNeeded) {

			engine += "<filters>";

			// biDim is a too much specific case, make it here
			// first case, if x and y, replace the two
			if (biDim) {

			    // add the csv for other cases
			    engine += "<filter processor=\"ConditionalFilter\"><params>";
			    // generate inverted conditions
			    engine += "<param name=\"condition\">" + invertedCondition + "</param>";
			    engine += "</params>";
			    //
			    engine += generateCsvHandler(engine, slice, caches, col, null);
			    engine += "</filter>";

			    Set<Integer> xKeys = xCondMap.keySet();
			    Set<Integer> yKeys = yCondMap.keySet();
			    // generate all conditions
			    String fullConditions = new String();

			    // if only modif on y
			    if (xKeys.size() == 0 && yKeys.size() > 0) {
				for (Integer y : yKeys) {
				    // 1 condition matched
				    fullConditions = "cell.row == " + (y - 1);
				    // now the filter
				    engine += "<filter processor=\"ConditionalFilter\"><params>";
				    // generate inverted conditions
				    engine += "<param name=\"condition\">" + fullConditions + "</param>";
				    engine += "</params>";
				    //
				    //
				    String yTmp = yCondMap.get(y);
				    // add cache
				    Set<String> cSet = caches.keySet();
				    String[] keys = cSet.toArray(new String[3]);
				    yTmp = "cache('" + keys[0] + ")." + yTmp;
				    // add function if needed
				    if (slice.getColumns().get(0).getType().startsWith("dp:"))
					yTmp = slice.getColumns().get(0).getType().split(":")[1] + "(" + yTmp + ")";
				    //
				    //
				    String tmpScript = generateCsvScripting(caches, keys[1]);
				    engine += generateCsvHandler(engine, slice, caches, col, yTmp + " | " + tmpScript.substring(0, tmpScript.lastIndexOf('|')));
				    engine += "</filter>";
				}
			    }

			    // if only modif on x
			    if (xKeys.size() > 0 && yKeys.size() == 0) {
				for (Integer x : xKeys) {
				    // 1 condition matched
				    fullConditions = "cell.column == " + (x - 1);
				    // now the filter
				    engine += "<filter processor=\"ConditionalFilter\"><params>";
				    // generate inverted conditions
				    engine += "<param name=\"condition\">" + fullConditions + "</param>";
				    engine += "</params>";
				    // now the filter
				    String xTmp = xCondMap.get(x);
				    // add cache
				    Set<String> cSet = caches.keySet();
				    String[] keys = cSet.toArray(new String[3]);
				    xTmp = "cache('" + keys[1] + ")." + xTmp;

				    // add function if needed
				    if (slice.getColumns().get(1).getType().startsWith("dp:"))
					xTmp = slice.getColumns().get(1).getType().split(":")[1] + "(" + xTmp + ")";
				    //
				    String tmpScript = generateCsvScripting(caches, keys[0]);
				    engine += generateCsvHandler(engine, slice, caches, col, tmpScript + xTmp);
				    engine += "</filter>";
				    // end
				}
			    }

			    if (xKeys.size() > 0 && yKeys.size() > 0) {
				for (Integer y : yKeys) {
				    for (Integer x : xKeys) {

					// 2 conditions matched
					fullConditions = " cell.row == " + (y - 1) + " &amp;&amp; cell.column == " + (x - 1);
					// now the filter
					engine += "<filter processor=\"ConditionalFilter\"><params>";
					// generate inverted conditions
					engine += "<param name=\"condition\">" + fullConditions + "</param>";
					engine += "</params>";
					//
					String yTmp = yCondMap.get(y);
					String xTmp = xCondMap.get(x);

					// add cache
					Set<String> cSet = caches.keySet();
					String[] keys = cSet.toArray(new String[3]);
					yTmp = "cache('" + keys[0] + ")." + yTmp;
					xTmp = "cache('" + keys[1] + ")." + xTmp;

					// add function if needed
					if (slice.getColumns().get(0).getType().startsWith("dp:"))
					    yTmp = slice.getColumns().get(0).getType().split(":")[1] + "(" + yTmp + ")";
					if (slice.getColumns().get(1).getType().startsWith("dp:"))
					    xTmp = slice.getColumns().get(1).getType().split(":")[1] + "(" + xTmp + ")";
					//

					engine += generateCsvHandler(engine, slice, caches, col, yTmp + " | " + xTmp);
					engine += "</filter>";
					// end

					// the line but not the col
					fullConditions = " cell.row == " + (y - 1) + " &amp;&amp; cell.column != " + (x - 1);
					// now the filter
					engine += "<filter processor=\"ConditionalFilter\"><params>";
					// generate inverted conditions
					engine += "<param name=\"condition\">" + fullConditions + "</param>";
					engine += "</params>";
					//
					String tmpScript = generateCsvScripting(caches, keys[1]);
					engine += generateCsvHandler(engine, slice, caches, col,
						yTmp + " | " + tmpScript.substring(0, tmpScript.lastIndexOf('|')));
					engine += "</filter>";
					// end

					// the col but not the line
					fullConditions = " cell.row != " + (y - 1) + " &amp;&amp; cell.column == " + (x - 1);
					// now the filter
					engine += "<filter processor=\"ConditionalFilter\"><params>";
					// generate inverted conditions
					engine += "<param name=\"condition\">" + fullConditions + "</param>";
					engine += "</params>";
					//
					tmpScript = generateCsvScripting(caches, keys[0]);
					engine += generateCsvHandler(engine, slice, caches, col, tmpScript + xTmp);
					engine += "</filter>";
					// end
				    }
				}
			    }
			}

			else {
			    conditionCount++;
			    // then add the csv for other cases
			    engine += "<filter processor=\"ConditionalFilter\"><params>";
			    // generate inverted conditions
			    engine += "<param name=\"condition\">" + invertedCondition + "</param>";
			    engine += "</params>";
			    //
			    engine += generateCsvHandler(engine, slice, caches, col, null);
			    engine += "</filter>";
			    //

			    // if needed, specific filters
			    Set<DSPLColumn> colKeys = indivScripts.keySet();
			    // we need to combine all scripts of the same line
			    // if we are a normal table
			    // put by row if attributes in column, put by column
			    // if attributes in line
			    LinkedHashMap<Integer, LinkedHashMap<String, String[]>> scrMap = new LinkedHashMap<Integer, LinkedHashMap<String, String[]>>();
			    for (DSPLColumn indivCol : colKeys) {
				ArrayList<String[]> tmpScr = indivScripts.get(indivCol);
				if (tmpScr.size() > 0) {
				    for (String[] condScr : tmpScr) {
					if (attrInLine) {
					    if (scrMap.get(new Integer(condScr[2]) - 1) != null) {
						scrMap.get(new Integer(condScr[2]) - 1).put(condScr[4], condScr);
					    } else {
						LinkedHashMap<String, String[]> tmpL = new LinkedHashMap<String, String[]>();
						tmpL.put(condScr[4], condScr);
						scrMap.put(new Integer(condScr[2]) - 1, tmpL);
					    }
					} else {
					    if (scrMap.get(new Integer(condScr[3]) - 1) != null) {
						scrMap.get(new Integer(condScr[3]) - 1).put(condScr[4], condScr);
					    } else {
						LinkedHashMap<String, String[]> tmpL = new LinkedHashMap<String, String[]>();
						tmpL.put(condScr[4], condScr);
						scrMap.put(new Integer(condScr[3]) - 1, tmpL);
					    }
					}
				    }
				}
			    }
			    //

			    // now iterate through the map
			    /*
			     * Set<Integer> rowOrCol = scrMap.keySet(); for(int
			     * roc : rowOrCol){ // get the scripts to apply here
			     * LinkedHashMap<String,String[]> scriptsToApply =
			     * scrMap.get(roc);
			     * 
			     * // first generate header engine +=
			     * "<filter processor=\"ConditionalFilter\"><params><param name=\"condition\">"
			     * ; if (attrInLine) engine += "cell.column == " +
			     * roc +
			     * "</param></params><handlers><handler processor=\"CsvHandler\">"
			     * ; else engine += "cell.row == " + roc +
			     * "</param></params><handlers><handler processor=\"CsvHandler\">"
			     * ; engine += "<params><param name=\"filename\">" +
			     * slice.getFileName() + "</param>"; engine +=
			     * "<param name=\"append\">true</param>"; engine +=
			     * "<param name=\"scripts\">";
			     * 
			     * // then apply the modifications or the formatting
			     * for (DSPLColumn indivCol : colKeys) { // find
			     * cached name if exists Set<String> tmpKeys =
			     * caches.keySet(); boolean addedScript = false; for
			     * (String key : tmpKeys) { // fist case in column
			     * if(attrInLine){
			     * 
			     * }else{ if(caches.get(key).getCol() ==
			     * indivCol.getCol()){
			     * 
			     * } } } // usual formatting if(!addedScript){
			     * engine += "cell.formatter" +
			     * this.generateEndOfScript(col); } } }
			     */

			    int place = 0;
			    for (DSPLColumn indivCol : colKeys) {
				ArrayList<String[]> scr = indivScripts.get(indivCol);
				if (scr.size() > 0) {
				    for (String[] condScr : scr) {
					// only if not already created
					String tmp = "<filter processor=\"ConditionalFilter\"><params><param name=\"condition\">";
					if (attrInLine)
					    tmp += "cell.column == " + (new Integer(condScr[2]) - 1)
						    + "</param></params><handlers><handler processor=\"CsvHandler\">";
					else
					    tmp += "cell.row == " + (new Integer(condScr[3]) - 1)
						    + "</param></params><handlers><handler processor=\"CsvHandler\">";

					if (engine.indexOf(tmp) == -1) {
					    // ok need to generate
					    conditionCount++;
					    int lineOrCol;
					    engine += "<filter processor=\"ConditionalFilter\"><params><param name=\"condition\">";
					    if (attrInLine) {
						engine += "cell.column == " + (new Integer(condScr[2]) - 1)
							+ "</param></params><handlers><handler processor=\"CsvHandler\">";
						lineOrCol = new Integer(condScr[2]) - 1;
					    } else {
						engine += "cell.row == " + (new Integer(condScr[3]) - 1)
							+ "</param></params><handlers><handler processor=\"CsvHandler\">";
						lineOrCol = new Integer(condScr[3]) - 1;
					    }
					    engine += "<params><param name=\"filename\">" + slice.getFileName() + "</param>";
					    engine += "<param name=\"append\">true</param>";
					    engine += "<param name=\"scripts\">";
					    //
					    Set<String> tmpKeys = caches.keySet();
					    int count = 0;

					    for (String key : tmpKeys) {
						if (place != count) {
						    // case a script to use
						    if (scrMap.get(lineOrCol) != null && scrMap.get(lineOrCol).get(key) != null) {
							if (caches.get(key).getType().startsWith("dp:")) engine += caches.get(key).getType().split(":")[1] + "(";
							engine += scrMap.get(lineOrCol).get(key)[1];
							if (caches.get(key).getType().startsWith("dp:")) engine += ")";
							engine += " | ";
						    }
						    //
						    else if (caches.get(key).getEngineScript().equals("")) {
							if (caches.get(key).getType().startsWith("dp:"))
							    engine += caches.get(key).getType().split(":")[1] + "(";
							engine += "cache('" + key + "').formatter";

							// now format using the
							// type
							engine += this.generateEndOfScript(caches.get(key));
							if (caches.get(key).getType().startsWith("dp:"))
							    engine += ")";
							engine += " | ";
						    } else {
							if (caches.get(key).getType().startsWith("dp:")) {
							    engine += caches.get(key).getType().split(":")[1] + "(";
							    engine += caches.get(key).getEngineScript() + ") | ";
							} else
							    engine += caches.get(key).getEngineScript() + " | ";
						    }
						} else {
						    // case a script to use
						    if (scrMap.get(lineOrCol) != null && scrMap.get(lineOrCol).get(key) != null) {
							if (caches.get(key).getType().startsWith("dp:"))  engine += caches.get(key).getType().split(":")[1] + "(";
							engine += scrMap.get(lineOrCol).get(key)[1];
							if (caches.get(key).getType().startsWith("dp:"))
							    engine += ")";
						    }
						    //
						    else if (caches.get(key).getType().startsWith("dp:")) {
							engine += caches.get(key).getType().split(":")[1] + "(";
							engine += condScr[1];
							engine += caches.get(key).getEngineScript() + ")";
						    } else
							engine += condScr[1];
						    if (count < indivScripts.size() - 1)
							engine += " | ";
						}
						count++;
					    }

					    // then the last script
					    if (col.getEngineScript().equals(""))
						engine += "cell.formatter" + this.generateEndOfScript(col);
					    else {
						engine += col.getEngineScript();
					    }

					    //
					    engine += "</param></params></handler></handlers></filter>";
					}
				    }
				}
				place++;
			    }
			}
		    }
		    // no conditions
		    else {
			// if needed construct numeric filter
			if (col.getType().equals("float") || col.getType().equals("integer")) {
			    // add a filter to avoid parse exception
			    if (numericFilter.equals(""))
				numericFilter = "cell.formatter.isNumeric() == true";
			    else
				numericFilter += " &amp;&amp; cell.formatter.isNumeric() == true";
			}

			if (!numericFilter.equals("")) {
			    engine += "<filters><filter processor=\"ConditionalFilter\"><params><param name=\"condition\">" + numericFilter
				    + "</param></params>";
			    needNumericClose = true;
			}
			engine += generateCsvHandler(engine, slice, caches, col, null);
			if (needNumericClose) {
			    // add a filter to avoid parse exception
			    engine += "</filter></filters>";
			}
			engine += "</filter>";
			engine += "</filters>";
		    }
		}
	    }
	}

	if (conditionNeeded) {
	    engine += "</filters>";
	    if (needNumericClose) {
		// add a filter to avoid parse exception
		engine += "</filter></filters>";
	    }
	}

	// end of nullfilter
	engine += "</filter>";
	engine += "</filters>";

	// if(!conditionNeeded) engine += "</handlers>";
	engine += "</filter>";
	engine += "</filters>";
	engine += "</parser>";
	engine += "</reader>";
	engine += "</readers>";

	return engine;
    }

    /**
     * Generate the CSV handler part
     * 
     * @param engine
     * @param slice
     * @param caches
     * @param col
     * @return
     */

    private String generateCsvHandler(String engine, DSPLSlice slice, LinkedHashMap<String, DSPLColumn> caches, DSPLColumn col, String biDimCondition) {
	String res = new String();
	res += "<handlers>";
	res += "<handler processor=\"CsvHandler\">";
	res += "<params>";
	res += "<param name=\"filename\">" + slice.getFileName() + "</param>";
	res += "<param name=\"append\">true</param>";
	res += "<param name=\"headers\">";
	for (String key : caches.keySet()) {
	    if (key.indexOf("_' + cell.column") != -1)
		res += key.substring(0, key.indexOf("_' + cell.column")) + " | ";
	    else if (key.indexOf("_' + cell.row") != -1)
		res += key.substring(0, key.indexOf("_' + cell.row")) + " | ";
	    else
		res += key + " | ";
	}
	res += dsplCleanString(col.getId()) + "</param>";
	res += "<param name=\"scripts\">";

	if (biDimCondition == null) {
	    for (String key : caches.keySet()) {
		res += generateCsvScripting(caches, key);
	    }
	}

	else {
	    res += biDimCondition + " | ";
	}

	L.Debug(this, "DSPLCol type " + col.getType(), true);
	// then the last script
	if (col.getEngineScript().equals(""))
	    res += "cell.formatter" + this.generateEndOfScript(col);
	else
	    res += col.getEngineScript();

	res += "</param>";

	res += "</params>";
	res += "</handler>";
	res += "</handlers>";
	return res;
    }

    private String generateCsvScripting(LinkedHashMap<String, DSPLColumn> caches, String key) {
	String res = new String();
	if (caches.get(key).getEngineScript().equals("")) {
	    if (caches.get(key).getType().startsWith("dp:"))
		res += caches.get(key).getType().split(":")[1] + "(";

	    if (key.indexOf("_' + cell.column") != -1 || key.indexOf("_' + cell.row") != -1)
		res += "cache('" + key + ").formatter";
	    else
		res += "cache('" + key + "').formatter";

	    // now format using the type
	    res += this.generateEndOfScript(caches.get(key));
	    if (caches.get(key).getType().startsWith("dp:"))
		res += ")";
	    res += " | ";
	} else {
	    if (caches.get(key).getType().startsWith("dp:")) {
		res += caches.get(key).getType().split(":")[1] + "(";
		res += caches.get(key).getEngineScript() + ") | ";
	    } else
		res += caches.get(key).getEngineScript() + " | ";
	}
	return res;
    }

    /*
     * Generate the condition and scripting value part Example : 0 0
     * formatter.str.replace('plop','Alsace'), region will return
     * cache('region').column == 0 && cache('region').row == 0 and
     * cache('region').formatter.str().replace('plop','Alsace') if cachedName ==
     * null, use cell
     */

    private String[] generateIndividualScripts(int col, int row, String content, String cachedName) {

	String[] script = new String[5];

	// bidim case
	if (col != -1 && row != -1) {
	    script[0] = "cell.row == " + row + " &amp;&amp; cell.column == " + col;
	    script[1] = "cell." + content;
	}

	else {
	    if (cachedName != null) {
		if (cachedName.indexOf("_' + cell.column") == -1)
		    script[0] = "cache('" + cachedName + "')";
		else
		    script[0] = "cache('" + cachedName + ")";
	    } else
		script[0] = "cell";

	    if (row != -1)
		script[0] += ".row == " + (row - 1);
	    if (col != -1)
		script[0] += ".column == " + (col - 1);

	    //
	    if (cachedName != null) {
		if (cachedName.indexOf("_' + cell.column") == -1)
		    script[1] = "cache('" + cachedName + "')";
		else
		    script[1] = "cache('" + cachedName + ")";
	    } else
		script[1] = "cell";

	    script[1] += "." + content;
	}

	script[2] = new Integer(col).toString();
	script[3] = new Integer(row).toString();
	script[4] = cachedName;

	return script;
    }

    private String generateEndOfScript(DSPLColumn col) {

	String script = new String();

	if (col.getType().indexOf("year") != -1)
	    script += ".date('yyyy','yyyy')";
	else if (col.getType().equals("time:time_point")){
	    // convert here
	    if(col.getFormat().equals("MQyy")){
		script += ".date('MM-yyyy','yyyy-MM')";
	    }
	    else if(col.getFormat().equals("dd/MM/yyyy")){
		script += ".date('" + col.getFormat() + "','dd/MM/yyyy')";
	    }
	    else script += ".date('" + col.getFormat() + "','yyyy-MM')";
	}
	else if (col.getType().indexOf("integer") != -1)
	    script += ".integer()";
	else if (col.getType().indexOf("float") != -1)
	    script += ".dbl()";
	else
	    script += ".str()";

	return script;
    }

}
