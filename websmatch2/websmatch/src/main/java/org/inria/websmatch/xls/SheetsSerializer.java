package org.inria.websmatch.xls;

import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.L;

public class SheetsSerializer {

    private SimpleSheet[] sheets;

    public SheetsSerializer(SimpleSheet[] sheets) {
	this.sheets = sheets;
    }

    public String toXML(boolean withData) {

	String xmlResult = new String();

	ArrayList<SimpleCell> attributeList = new ArrayList<SimpleCell>();

	ArrayList<SimpleCell> titleList = new ArrayList<SimpleCell>();
	ArrayList<SimpleCell> commentList = new ArrayList<SimpleCell>();

	for (int sheet = 0; sheet < sheets.length; sheet++) {

	    if (sheets[sheet] != null) {
		SimpleCell[][] cells = sheets[sheet].getCells();

		for (int line = 0; line < cells.length; line++) {
		    for (int col = 0; col < cells[line].length; col++) {
			// is a title
			if (cells[line][col].getCurrentMeta().equals("title")) {
			    titleList.add(cells[line][col]);
			    L.Debug(this.getClass().getSimpleName(), "Title " + cells[line][col].getContent(), true);
			}

			// is a comment
			if (cells[line][col].getCurrentMeta().equals("comment")) {
			    commentList.add(cells[line][col]);
			    L.Debug(this.getClass().getSimpleName(), "Comment " + cells[line][col].getContent(), true);
			}

			// is attribute
			if (cells[line][col].getIs_attributeML() == 1.0 || cells[line][col].isAttribute()) {
			    if (!attributeList.contains(cells[line][col]))
				attributeList.add(cells[line][col]);
			    L.Debug(this.getClass().getSimpleName(), "Sheet " + sheet + "\tAttribute pos : " + col + "\t" + line, true);
			}
		    }
		}
	    }
	}

	// ok we had the tables and datas for detected attributes
	for (int ns = 0; ns < sheets.length; ns++) {
	    SimpleSheet tmpSheet = sheets[ns];
	    if (tmpSheet != null) {
		xmlResult += "<titles>";
		for (int titles = 0; titles < titleList.size(); titles++) {
		    if (titleList.get(titles).getSheet() == ns)
			if (!titleList.get(titles).getEditedContent().equals(""))
			    xmlResult += "<title sheet=\"" + (titleList.get(titles).getSheet() + 1) + "\" x=\"" + (titleList.get(titles).getJxlCol() + 1)
				    + "\" y=\"" + (titleList.get(titles).getJxlRow() + 1) + "\">"
				    + StringEscapeUtils.escapeXml(titleList.get(titles).getEditedContent()).trim() + "</title>";
			else
			    xmlResult += "<title sheet=\"" + (titleList.get(titles).getSheet() + 1) + "\" x=\"" + (titleList.get(titles).getJxlCol() + 1)
				    + "\" y=\"" + (titleList.get(titles).getJxlRow() + 1) + "\">"
				    + StringEscapeUtils.escapeXml(titleList.get(titles).getContent()).trim() + "</title>";
		}

		L.Debug(this.getClass().getSimpleName(), "\tSheet " + ns, true);
		xmlResult += "</titles>";
		xmlResult += "<tables>";

		// boolean demo = false;

		for (int ncc = 0; ncc < tmpSheet.getConnexComps().size(); ncc++) {

		    ConnexComposant cc = tmpSheet.getConnexComps().get(ncc);

		    boolean ccCreated = false;
		    boolean firstCell = true;
		    boolean inLineAttributes = false;

		    // check which cc to keep with attributes
		    for (SimpleCell cell : attributeList) {

			boolean closAttr = true;

			if (cell.getSheet() == ns && cell.getJxlCol() >= cc.getStartX() && cell.getJxlCol() <= cc.getEndX()
				&& cell.getJxlRow() >= cc.getStartY() && cell.getJxlRow() <= cc.getEndY()) {

			    // ok it's in
			    if (!ccCreated) {
				xmlResult += "<table sheetName =\"" + StringEscapeUtils.escapeXml(tmpSheet.getTitle()) + "\" sheet=\"" + (ns + 1)
					+ "\" startX=\"" + (cc.getStartX() + 1) + "\" endX=\"" + (cc.getEndX() + 1) + "\" startY=\"" + (cc.getStartY() + 1)
					+ "\" endY=\"" + (cc.getEndY() + 1) + "\" criteria=\"" + cc.getCriteria() + "\" attrInLine=\"" + cc.isAttrInLines()
					+ "\" biDim=\"" + cc.isBiDimensionnalArray() + "\">";
				xmlResult += "<attributes>";

				ccCreated = true;
			    }

			    // determine attributes in line or col
			    if (firstCell) {
				// more than one attribute
				if (attributeList.size() > 1) {
				    SimpleCell next = attributeList.get(attributeList.size() - 1);
				    if (next.getJxlRow() > cell.getJxlRow() && next.getJxlCol() == cell.getJxlCol()) {
					inLineAttributes = true;
				    }
				} else {
				    if (tmpSheet.getCells()[cell.getJxlRow()].length > 1) {
					inLineAttributes = true;
				    }
				}
				firstCell = false;
			    }

			    String attrFormat = new String();

			    // hack for bidimensionnal arrays
			    if (cc.isBiDimensionnalArray()) {

				boolean demo = false;
				// demo = true;

				// first make the column attribute
				if (cell.getEditedContent().split("\\\\")[0].equals("geo:location") && demo == true)
				    xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
					    + (cell.getJxlRow() + 1) + "\"><dspltype>" + cell.getEditedContent().split("\\\\")[0] + "</dspltype><type>"
					    + cell.getContentType() + "</type><name>" + "zone" + "</name>";

				else
				    xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
					    + (cell.getJxlRow() + 1) + "\"><dspltype>" + cell.getEditedContent().split("\\\\")[0] + "</dspltype><type>"
					    + cell.getContentType() + "</type><format>" + cell.getFormat() + "</format><enginescript>" + cell.getEngineScript()
					    + "</enginescript><name>" + cell.getEditedContent().split("\\\\")[0].split(":")[1] + "</name>";

				int firstCol = cell.getJxlCol() + 1;
				int multiple = cell.getJxlRow() + 1;

				if (withData) {
				    xmlResult += "<datas>";

				    // we have to multiply lines
				    for (int line = cell.getJxlRow() + 1; line <= cc.getEndY(); line++) {
					if (tmpSheet.getCells().length > 0 && tmpSheet.getCells()[line].length > 0) {
					    for (int lineSize = 1; lineSize < tmpSheet.getCells()[cell.getJxlRow()].length; lineSize++) {
						xmlResult += "<data sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
							+ /* (line + 1) */multiple + "\" origX=\"" + (cell.getJxlCol() + 1) + "\" origY=\"" + (line + 1)
							+ "\" script=\""
							+ StringEscapeUtils.escapeXml(tmpSheet.getCells()[line][cell.getJxlCol()].getEngineScript()) + "\">"
							+ StringEscapeUtils.escapeXml(tmpSheet.getCells()[line][cell.getJxlCol()].getContent()).trim()
							+ "</data>";
						multiple++;
					    }
					}
				    }

				    xmlResult += "</datas>";
				}
				xmlResult += "</attribute>";

				// next the line attribute
				if (cell.getEditedContent().split("\\\\")[1].equals("geo:location"))
				    xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
					    + (cell.getJxlRow() + 1) + "\"><dspltype>" + cell.getEditedContent().split("\\\\")[1] + "</dspltype><type>"
					    + cell.getContentType() + "</type><name>" + "zone" + "</name>";

				else {
				    String tmpName = cell.getEditedContent().split("\\\\")[1];
				    tmpName = tmpName.split(":")[1];
				    tmpName = tmpName.substring(0, tmpName.lastIndexOf(" (detected)"));
				    xmlResult += "<attribute sheet=\""
					    + (cell.getSheet() + 1)
					    + "\" x=\""
					    + (cell.getJxlCol() + 1)
					    + "\" y=\""
					    + (cell.getJxlRow() + 1)
					    + "\"><dspltype>"
					    + cell.getEditedContent().split("\\\\")[1].substring(0,
						    cell.getEditedContent().split("\\\\")[1].indexOf(" (detected)")) + "</dspltype><type>"
					    + cell.getContentType() + "</type><format>" + cell.getFormat() + "</format><enginescript>" + cell.getEngineScript()
					    + "</enginescript><name>" + tmpName + "</name>";
				}
				if (withData) {
				    xmlResult += "<datas>";
				    // we have to multiply
				    multiple = cell.getJxlRow() + 1;

				    for (int line = cell.getJxlRow() + 1; line <= cc.getEndY(); line++) {
					if (tmpSheet.getCells()[line].length > 0) {
					    for (int nbCol = cell.getJxlCol() + 1; nbCol <= cc.getEndX(); nbCol++) {
						if(tmpSheet.getCells()[cell.getJxlRow()].length > nbCol+1){
						xmlResult += "<data sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (firstCol + 1) + "\" y=\"" + multiple
							+ "\" origX=\"" + (nbCol + 1) + "\" origY=\"" + (cell.getJxlRow() + 1) + "\" script=\""
							+ StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][nbCol].getEngineScript()) + "\">"
							+ StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][nbCol].getContent()).trim()
							+ "</data>";
						multiple++;
						}
					    }
					}
				    }

				    xmlResult += "</datas>";
				}
				xmlResult += "</attribute>";

				// values
				xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + "1" + "\" y=\"" + "9" + "\"><dspltype>" + "undefined"
					+ "</dspltype><type>" + cell.getContentType() + "</type><name>" + StringEscapeUtils.escapeXml(cc.getValueName().trim())
					+ "</name>";

				if (withData) {
				    xmlResult += "<datas>";

				    multiple = cell.getJxlRow() + 1;
				    for (int line = (cell.getJxlRow() + 1); line <= cc.getEndY(); line++) {
					for (int nbCol = (cell.getJxlCol() + 1); nbCol <= cc.getEndX(); nbCol++) {
					    if (tmpSheet.getCells().length > 0 && tmpSheet.getCells()[line].length > nbCol) {
						String content = StringEscapeUtils.escapeXml(tmpSheet.getCells()[line][nbCol].getContent()).trim();
						if (content.equals("src/main"))
						    content = "0";

						xmlResult += "<data sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (firstCol + 2) + "\" y=\"" + multiple
							+ "\" origX=\"" + (nbCol + 1) + "\" origY=\"" + (line + 1) + "\" script=\""
							+ StringEscapeUtils.escapeXml(tmpSheet.getCells()[line][nbCol].getEngineScript()) + "\">"
							+ content.trim() + "</data>";
						multiple++;
					    }
					}
				    }

				    xmlResult += "</datas>";
				}
			    }

			    // attr in lines
			    else if (inLineAttributes) {
				if (cell.getJxlCol() - 1 >= 0 && tmpSheet.getCells().length > cell.getJxlRow()
					&& tmpSheet.getCells()[cell.getJxlRow()].length > cell.getJxlCol() - 1
					&& tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].isAttribute()) {
				    if (!cell.getEditedContent().equals("")) {
					if (cell.getEditedContent().indexOf(tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].getContent()) == -1) {
					    xmlResult += "<attribute sheet=\""
						    + (cell.getSheet() + 1)
						    + "\" x=\""
						    + (cell.getJxlCol() + 1)
						    + "\" y=\""
						    + (cell.getJxlRow() + 1)
						    + "\"><dspltype>"
						    + cell.getCurrentDsplMeta()
						    + "</dspltype><type>"
						    + cell.getContentType()
						    + "</type><format>"
						    + cell.getFormat()
						    + "</format><enginescript>"
						    + cell.getEngineScript()
						    + "</enginescript><name>"
						    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].getContent())
							    .trim() + " - " + StringEscapeUtils.escapeXml(cell.getEditedContent()).trim() + "</name>";
					} else {
					    xmlResult += "<attribute sheet=\""
						    + (cell.getSheet() + 1)
						    + "\" x=\""
						    + (cell.getJxlCol() + 1)
						    + "\" y=\""
						    + (cell.getJxlRow() + 1)
						    + "\"><dspltype>"
						    + cell.getCurrentDsplMeta()
						    + "</dspltype><type>"
						    + cell.getContentType()
						    + "</type><format>"
						    + cell.getFormat()
						    + "</format><enginescript>"
						    + cell.getEngineScript()
						    + "</enginescript><name>"
						    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].getContent())
							    .trim()
						    + " - "
						    + StringEscapeUtils.escapeXml(
							    cell.getEditedContent().substring(
								    cell.getEditedContent().indexOf(
									    tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].getContent()))).trim()
						    + "</name>";
					}
				    } else if (cell.getContent().indexOf(tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].getContent()) == -1) {
					xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
						+ (cell.getJxlRow() + 1) + "\"><dspltype>" + cell.getCurrentDsplMeta() + "</dspltype><type>"
						+ cell.getContentType() + "</type><format>" + cell.getFormat() + "</format><enginescript>"
						+ cell.getEngineScript() + "</enginescript><name>"
						+ StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].getContent()).trim()
						+ " - " + StringEscapeUtils.escapeXml(cell.getContent()).trim() + "</name>";
				    } else {
					xmlResult += "<attribute sheet=\""
						+ (cell.getSheet() + 1)
						+ "\" x=\""
						+ (cell.getJxlCol() + 1)
						+ "\" y=\""
						+ (cell.getJxlRow() + 1)
						+ "\"><dspltype>"
						+ cell.getCurrentDsplMeta()
						+ "</dspltype><type>"
						+ cell.getContentType()
						+ "</type><format>"
						+ cell.getFormat()
						+ "</format><enginescript>"
						+ cell.getEngineScript()
						+ "</enginescript><name>"
						+ StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].getContent()).trim()
						+ " - "
						+ StringEscapeUtils.escapeXml(
							cell.getContent().substring(
								cell.getContent().indexOf(
									tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() - 1].getContent()))).trim()
						+ "</name>";
				    }
				} else {
				    if (tmpSheet.getCells().length > cell.getJxlRow() && tmpSheet.getCells()[cell.getJxlRow()].length > cell.getJxlCol() + 1
					    && tmpSheet.getCells()[cell.getJxlRow()][cell.getJxlCol() + 1].isAttribute()) {
					// do nothing
					closAttr = false;
				    } else {
					if (!cell.getEditedContent().trim().equals(""))
					    xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
						    + (cell.getJxlRow() + 1) + "\"><dspltype>" + cell.getCurrentDsplMeta() + "</dspltype><type>"
						    + cell.getContentType() + "</type><format>" + cell.getFormat() + "</format><enginescript>"
						    + cell.getEngineScript() + "</enginescript><name>"
						    + StringEscapeUtils.escapeXml(cell.getEditedContent()).trim() + "</name>";
					else
					    xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
						    + (cell.getJxlRow() + 1) + "\"><dspltype>" + cell.getCurrentDsplMeta() + "</dspltype><type>"
						    + cell.getContentType() + "</type><format>" + cell.getFormat() + "</format><enginescript>"
						    + cell.getEngineScript() + "</enginescript><name>" + StringEscapeUtils.escapeXml(cell.getContent()).trim()
						    + "</name>";
				    }
				}
			    }
			    // attr in column (classical)
			    else {
				if (cell.getJxlRow() - 1 > -1 && cell.getJxlRow() - 1 < tmpSheet.getCells().length
					&& cell.getJxlCol() < tmpSheet.getCells()[cell.getJxlRow() - 1].length
					&& tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].isAttribute()) {

				    // for quarter transformation
				    attrFormat = cell.getFormat();

				    if (!cell.getEditedContent().trim().equals("")) {
					if (cell.getEditedContent().indexOf(tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].getContent()) == -1)
					    xmlResult += "<attribute sheet=\""
						    + (cell.getSheet() + 1)
						    + "\" x=\""
						    + (cell.getJxlCol() + 1)
						    + "\" y=\""
						    + (cell.getJxlRow() + 1)
						    + "\"><dspltype>"
						    + cell.getCurrentDsplMeta()
						    + "</dspltype><type>"
						    + cell.getContentType()
						    + "</type><format>"
						    + cell.getFormat()
						    + "</format><enginescript>"
						    + cell.getEngineScript()
						    + "</enginescript><name>"
						    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].getContent())
							    .trim() + " - " + StringEscapeUtils.escapeXml(cell.getEditedContent()).trim() + "</name>";
					else
					    xmlResult += "<attribute sheet=\""
						    + (cell.getSheet() + 1)
						    + "\" x=\""
						    + (cell.getJxlCol() + 1)
						    + "\" y=\""
						    + (cell.getJxlRow() + 1)
						    + "\"><dspltype>"
						    + cell.getCurrentDsplMeta()
						    + "</dspltype><type>"
						    + cell.getContentType()
						    + "</type><format>"
						    + cell.getFormat()
						    + "</format><enginescript>"
						    + cell.getEngineScript()
						    + "</enginescript><name>"
						    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].getContent())
							    .trim()
						    + " - "
						    + StringEscapeUtils.escapeXml(
							    cell.getEditedContent().substring(
								    cell.getEditedContent().indexOf(
									    tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].getContent()))).trim()
						    + "</name>";
				    } else {

					// for quarter transformation
					attrFormat = cell.getFormat();

					if (cell.getContent().indexOf(tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].getContent()) == -1)
					    xmlResult += "<attribute sheet=\""
						    + (cell.getSheet() + 1)
						    + "\" x=\""
						    + (cell.getJxlCol() + 1)
						    + "\" y=\""
						    + (cell.getJxlRow() + 1)
						    + "\"><dspltype>"
						    + cell.getCurrentDsplMeta()
						    + "</dspltype><type>"
						    + cell.getContentType()
						    + "</type><format>"
						    + cell.getFormat()
						    + "</format><enginescript>"
						    + cell.getEngineScript()
						    + "</enginescript><name>"
						    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].getContent())
							    .trim() + " - " + StringEscapeUtils.escapeXml(cell.getContent()).trim() + "</name>";
					else
					    xmlResult += "<attribute sheet=\""
						    + (cell.getSheet() + 1)
						    + "\" x=\""
						    + (cell.getJxlCol() + 1)
						    + "\" y=\""
						    + (cell.getJxlRow() + 1)
						    + "\"><dspltype>"
						    + cell.getCurrentDsplMeta()
						    + "</dspltype><type>"
						    + cell.getContentType()
						    + "</type><format>"
						    + cell.getFormat()
						    + "</format><enginescript>"
						    + cell.getEngineScript()
						    + "</enginescript><name>"
						    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].getContent())
							    .trim()
						    + " - "
						    + StringEscapeUtils.escapeXml(
							    cell.getContent().substring(
								    cell.getContent().indexOf(
									    tmpSheet.getCells()[cell.getJxlRow() - 1][cell.getJxlCol()].getContent()))).trim()
						    + "</name>";
				    }
				} else {
				    if (cell.getJxlRow() + 1 < tmpSheet.getCells().length
					    && cell.getJxlCol() < tmpSheet.getCells()[cell.getJxlRow() + 1].length
					    && tmpSheet.getCells()[cell.getJxlRow() + 1][cell.getJxlCol()].isAttribute()) {
					// do nothing
					closAttr = false;
				    } else {
					
					// for quarter transformation
					attrFormat = cell.getFormat();
					
					if (!cell.getEditedContent().trim().equals(""))
					    xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
						    + (cell.getJxlRow() + 1) + "\"><dspltype>" + cell.getCurrentDsplMeta() + "</dspltype><type>"
						    + cell.getContentType() + "</type><format>" + cell.getFormat() + "</format><enginescript>"
						    + cell.getEngineScript() + "</enginescript><name>"
						    + StringEscapeUtils.escapeXml(cell.getEditedContent()).trim() + "</name>";
					else
					    xmlResult += "<attribute sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (cell.getJxlCol() + 1) + "\" y=\""
						    + (cell.getJxlRow() + 1) + "\"><dspltype>" + cell.getCurrentDsplMeta() + "</dspltype><type>"
						    + cell.getContentType() + "</type><format>" + cell.getFormat() + "</format><enginescript>"
						    + cell.getEngineScript() + "</enginescript><name>" + StringEscapeUtils.escapeXml(cell.getContent()).trim()
						    + "</name>";
				    }
				}
			    }

			    if (!cc.isBiDimensionnalArray() && (withData && closAttr)) {
				xmlResult += "<datas>";

				if (!inLineAttributes) {
				    // then we give the associated datas for
				    // attr in
				    // column
				    for (int line = cc.getStartY(); line <= cc.getEndY(); line++) {
					for (int col = cc.getStartX(); col <= cc.getEndX(); col++) {
					    if (col == cell.getJxlCol() && line != cell.getJxlRow()) {
						if (line < tmpSheet.getCells().length && col < tmpSheet.getCells()[line].length) {
						    // not an attr
						    if (!tmpSheet.getCells()[line][col].isAttribute() && cell.getJxlRow() < line) {

							// for quarter
							tmpSheet.getCells()[line][col].generateDateFormatScript(attrFormat);

							if (!tmpSheet.getCells()[line][col].getEditedContent().equals(""))
							    xmlResult += "<data sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (col + 1) + "\" y=\""
								    + (line + 1) + "\" script=\""
								    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[line][col].getEngineScript()) + "\">"
								    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[line][col].getEditedContent()).trim()
								    + "</data>";
							else
							    xmlResult += "<data sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (col + 1) + "\" y=\""
								    + (line + 1) + "\" script=\""
								    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[line][col].getEngineScript()) + "\">"
								    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[line][col].getContent()).trim()
								    + "</data>";
						    }
						}
					    }
					}
				    }
				} else {
				    // then we give the associated datas for
				    // attr in
				    // line
				    for (int col = cc.getStartX() + 1; col <= cc.getEndX(); col++) {
					if (tmpSheet.getCells().length > cell.getJxlRow() && tmpSheet.getCells()[cell.getJxlRow()].length > col
						&& tmpSheet.getCells()[cell.getJxlRow()][col] != null) {
					    if (!tmpSheet.getCells()[cell.getJxlRow()][col].isAttribute() && cell.getJxlCol() < col) {
						if (!tmpSheet.getCells()[cell.getJxlRow()][col].getEditedContent().equals(""))
						    xmlResult += "<data sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (col + 1) + "\" y=\""
							    + (cell.getJxlRow() + 1) + "\" script=\""
							    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][col].getEngineScript()) + "\">"
							    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][col].getEditedContent()).trim()
							    + "</data>";
						else
						    xmlResult += "<data sheet=\"" + (cell.getSheet() + 1) + "\" x=\"" + (col + 1) + "\" y=\""
							    + (cell.getJxlRow() + 1) + "\" script=\""
							    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][col].getEngineScript()) + "\">"
							    + StringEscapeUtils.escapeXml(tmpSheet.getCells()[cell.getJxlRow()][col].getContent()).trim()
							    + "</data>";
					    }
					}
				    }
				}

				xmlResult += "</datas>";
			    }
			    if (closAttr)
				xmlResult += "</attribute>";

			}
		    }

		    if (ccCreated) {
			xmlResult += "</attributes>";
			xmlResult += "</table>";
		    }
		}

		xmlResult += "</tables>";

		xmlResult += "<comments>";

		for (int comments = 0; comments < commentList.size(); comments++) {
		    if (commentList.get(comments).getSheet() == ns)
			if (!commentList.get(comments).getEditedContent().equals(""))
			    xmlResult += "<comment sheet=\"" + (commentList.get(comments).getSheet() + 1) + "\" x=\""
				    + (commentList.get(comments).getJxlCol() + 1) + "\" y=\"" + (commentList.get(comments).getJxlRow() + 1) + "\">"
				    + StringEscapeUtils.escapeXml(commentList.get(comments).getEditedContent()) + "</comment>";
			else
			    xmlResult += "<comment sheet=\"" + (commentList.get(comments).getSheet() + 1) + "\" x=\""
				    + (commentList.get(comments).getJxlCol() + 1) + "\" y=\"" + (commentList.get(comments).getJxlRow() + 1) + "\">"
				    + StringEscapeUtils.escapeXml(commentList.get(comments).getContent()) + "</comment>";
		}

		xmlResult += "</comments>";
	    }
	}

	L.Debug(this.getClass().getSimpleName(), "xmlResult " + xmlResult, true);

	return xmlResult;
    }
}
