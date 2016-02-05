package org.inria.websmatch.dspl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.inria.websmatch.gwt.spreadsheet.server.JsonSpreadsheetParsingService;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.FileUtils;
import org.inria.websmatch.xml.WSMatchXMLLoader;
import org.inria.websmatch.xml.WSMatchXMLtoDSPLLoader;
import org.jdom.Element;

public class DSPLExport {

	private String webSMatchFileName;
	private String dataSetName;
	private String dataSetDescription;
	private String targetLangage;

	//private boolean needDefaultConcept = true;
	private boolean needZone = false;

	public DSPLExport(String webSMatchFileName, String dataSetName, String dataSetDescription, String targetLangage) {
		this.webSMatchFileName = webSMatchFileName;
		this.setDataSetName(dataSetName);
		this.setDataSetDescription(dataSetDescription);
		this.setTargetLangage(targetLangage);
	}

	/**
	 * Works only with one file and online
	 *
	 * @param sheets
	 */

    /*
     * public void dsplGenerate(SimpleSheet[] sheets) { LinkedList<DSPLSlice>
     * slices = this.xmlGenerator(""); this.csvGenerator(sheets, slices); }
     */

	/**
	 * Same thing without SimpleSheet[] to be used offline eg for integration
	 */

	public void dsplGenerate(String xmlSchemaDocument) {
		LinkedList<DSPLSlice> slices = this.xmlGenerator("");
		this.csvGenerator(xmlSchemaDocument, slices);
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
		cleaned = cleaned.replaceAll("\\xA0","");

		return cleaned;
	}

	private String xmlHeader(String targetNamespace) {

		String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n<dspl ";
		if (targetNamespace != null && !targetNamespace.equals(""))
			head += "targetNamespace=\"" + targetNamespace + "\"";

		head += "\n\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
		head += "\n\txmlns=\"http://schemas.google.com/dspl/2010\"";
		head += "\n\txmlns:time=\"http://www.google.com/publicdata/dataset/google/time\"";
		head += "\n\txmlns:geo=\"http://www.google.com/publicdata/dataset/google/geo\"";
		head += "\n\txmlns:entity=\"http://www.google.com/publicdata/dataset/google/entity\"";
		head += "\n\txmlns:quantity=\"http://www.google.com/publicdata/dataset/google/quantity\">";

		head += "\n\t<import namespace=\"http://www.google.com/publicdata/dataset/google/time\"/>";
		head += "\n\t<import namespace=\"http://www.google.com/publicdata/dataset/google/entity\"/>";
		head += "\n\t<import namespace=\"http://www.google.com/publicdata/dataset/google/geo\"/>";
		head += "\n\t<import namespace=\"http://www.google.com/publicdata/dataset/google/quantity\"/>";

		return head;
	}

	private String xmlInfo(String name, String description) {
		String info = "";

		info += "\n\t<info>";

		info += "\n\t<name>";
		info += "\n\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + StringEscapeUtils.escapeXml(name) + "</value>";
		info += "\n\t</name>";

		info += "\n\t<description>";
		info += "\n\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + StringEscapeUtils.escapeXml(description) + "</value>";
		info += "\n\t</description>";

		info += "\n\t</info>";

		return info;
	}

	private String xmlProvider(String name) {
		String provider = "";

		provider += "\n\t<provider>";

		provider += "\n\t<name>";
		provider += "\n\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + name + "</value>";
		provider += "\n\t</name>";

		provider += "\n\t</provider>";

		return provider;
	}

	private String xmlSlices(LinkedList<DSPLSlice> slicesList) {

		ArrayList<String> entityTables = new ArrayList<>();

		// check if there is an entity
	/*for (DSPLSlice slice : slicesList) {
	    for (DSPLColumn col : slice.getColumns()) {
		if (col.getType().contains("entity")) {
		    needDefaultConcept = false;
		}
	    }
	}*/

		// concepts
		String concepts = new String();

		concepts += "\n\t<concepts>";

		// slices
		String slices = new String();

		slices += "\n\t<slices>";

		// tables
		String tables = new String();

		tables += "\n\t<tables>";

		for (DSPLSlice slice : slicesList) {

			slices += "\n\t\t<slice id=\"" + slice.getFileName().substring(0, slice.getFileName().lastIndexOf(".")) + "\">";

			tables += "\n\t\t<table id=\"" + slice.getFileName().substring(0, slice.getFileName().lastIndexOf(".")) + "_table" + "\">";

			LinkedList<DSPLColumn> cols = slice.getColumns();

			for (DSPLColumn tmpCol : cols) {

				// DSPL type
				if (tmpCol.getType().contains(":")) {
					if (tmpCol.getType().equals("geo:location")) {
						//System.out.println("Zone needed, default not.");
						//needDefaultConcept = false;
						needZone = true;
					}
				}
			}

			for (DSPLColumn tmpCol : cols) {

				// DSPL type
				if (tmpCol.getType().contains(":")) {

					// it's a ratio
					if (tmpCol.getType().split(":")[0].equals("quantity") && tmpCol.getType().split(":")[1].equals("ratio")) {

						slices += "\n\t\t\t<metric concept=\"" + dsplCleanString(tmpCol.getId()) + "\"/>";

						concepts += "\n\t\t\t<concept id=\"" + dsplCleanString(tmpCol.getId()) + "\" extends=\"" + tmpCol.getType() + "\">";

						concepts += "\n\t\t\t\t<info>";
						concepts += "\n\t\t\t\t\t<name>";
						concepts += "\n\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + StringEscapeUtils.escapeXml(tmpCol.getId())
								+ "</value>";
						concepts += "\n\t\t\t\t\t</name>";
						concepts += "\n\t\t\t\t\t<description>";
						concepts += "\n\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + StringEscapeUtils.escapeXml(tmpCol.getDescription())
								+ "</value>";
						concepts += "\n\t\t\t\t\t</description>";
						concepts += "\n\t\t\t\t</info>";

						// may be another thing
						concepts += "\n\t\t\t\t<type ref=\"float\"/>";

						// attribute
						concepts += "\n\t\t\t\t<attribute id=\"is_percentage\">";
						concepts += "\n\t\t\t\t\t<value>true</value>";
						concepts += "\n\t\t\t\t</attribute>";

						concepts += "\n\t\t\t</concept>";
					}

					// an entity defined by user
					else if (tmpCol.getType().split(":")[0].equals("entity") && tmpCol.getType().split(":")[1].equals("entity")) {
						slices += "\n\t\t\t<dimension concept=\"" + dsplCleanString(tmpCol.getId()) + "\"/>";

						concepts += "\n\t\t\t<concept id=\"" + dsplCleanString(tmpCol.getId()) + "\" extends=\"" + tmpCol.getType() + "\">";

						concepts += "\n\t\t\t\t<info>";
						concepts += "\n\t\t\t\t\t<name>";
						concepts += "\n\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + StringEscapeUtils.escapeXml(tmpCol.getId())
								+ "</value>";
						concepts += "\n\t\t\t\t\t</name>";
						concepts += "\n\t\t\t\t\t<description>";
						concepts += "\n\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + StringEscapeUtils.escapeXml(tmpCol.getDescription())
								+ "</value>";
						concepts += "\n\t\t\t\t\t</description>";
						concepts += "\n\t\t\t\t</info>";

						// may be another thing
						concepts += "\n\t\t\t\t<type ref=\"string\"/>";

						//
						concepts += "\n\t\t\t\t<property id=\"name\">";
						concepts += "\n\t\t\t\t\t<info>";
						concepts += "\n\t\t\t\t\t\t<name>";
						concepts += "\n\t\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">Nom</value>";
						concepts += "\n\t\t\t\t\t\t</name>";
						concepts += "\n\t\t\t\t\t</info>";
						concepts += "\n\t\t\t\t\t<type ref=\"string\"/>";
						concepts += "\n\t\t\t\t</property>";

						concepts += "\n\t\t\t\t<table ref=\"" + dsplCleanString(tmpCol.getId()) + "_table\"/>";

						concepts += "\n\t\t\t</concept>";
					}

					// zone geo
					else if (tmpCol.getType().split(":")[0].equals("geo") && tmpCol.getType().split(":")[1].equals("location")) {

						//needDefaultConcept = false;
						needZone = true;

						slices += "\n\t\t\t<dimension concept=\"zone\"/>";

						concepts += "\n\t\t\t<concept id=\"zone\" extends=\"" + tmpCol.getType() + "\">";

						concepts += "\n\t\t\t\t<info>";
						concepts += "\n\t\t\t\t\t<name>";
						concepts += "\n\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + "zone" + "</value>";
						concepts += "\n\t\t\t\t\t</name>";
						concepts += "\n\t\t\t\t\t<description>";
						concepts += "\n\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + "" + "</value>";
						concepts += "\n\t\t\t\t\t</description>";
						concepts += "\n\t\t\t\t</info>";

						// may be another thing
						concepts += "\n\t\t\t\t<type ref=\"string\"/>";

						//
						concepts += "\n\t\t\t\t<property id=\"name\">";
						concepts += "\n\t\t\t\t\t<info>";
						concepts += "\n\t\t\t\t\t\t<name>";
						concepts += "\n\t\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">zone</value>";
						concepts += "\n\t\t\t\t\t\t</name>";
						concepts += "\n\t\t\t\t\t</info>";
						concepts += "\n\t\t\t\t\t<type ref=\"string\"/>";
						concepts += "\n\t\t\t\t</property>";

						concepts += "\n\t\t\t\t<table ref=\"zone_table\"/>";

						concepts += "\n\t\t\t</concept>";
					}

					else
						slices += "\n\t\t\t<dimension concept=\"" + tmpCol.getType() + "\"/>";

		    /*if (needDefaultConcept && tmpCol.getType().split(":")[0].equals("time")) {
			slices += "\n\t\t\t<dimension concept=\"default\"/>";
		    }*/

				}

				// not a DSPL type
				else {
					slices += "\n\t\t\t<metric concept=\"" + dsplCleanString(tmpCol.getId()) + "\"/>";

					concepts += "\n\t\t\t<concept id=\"" + dsplCleanString(tmpCol.getId()) + "\">";
					concepts += "\n\t\t\t\t<info>";
					concepts += "\n\t\t\t\t\t<name>";
					concepts += "\n\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + StringEscapeUtils.escapeXml(tmpCol.getId()) + "</value>";
					concepts += "\n\t\t\t\t\t</name>";
					concepts += "\n\t\t\t\t\t<description>";
					concepts += "\n\t\t\t\t\t\t<value xml:lang=\"" + this.getTargetLangage() + "\">" + StringEscapeUtils.escapeXml(tmpCol.getDescription())
							+ "</value>";
					concepts += "\n\t\t\t\t\t</description>";
					concepts += "\n\t\t\t\t</info>";
					concepts += "\n\t\t\t\t<type ref=\"" + tmpCol.getType() + "\"/>";
					concepts += "\n\t\t\t</concept>";

				}

				if (tmpCol.getType().contains(":") && tmpCol.getType().split(":")[0].equals("entity") && tmpCol.getType().split(":")[1].equals("entity")) {

					tables += "\n\t\t\t<column id=\"" + dsplCleanString(tmpCol.getId()) + "\" type=\"string\"/>";

					// create the table
					String tmpTable = "";
					tmpTable += "\n\t\t<table id=\"" + dsplCleanString(tmpCol.getId()) + "_table\">";
					tmpTable += "\n\t\t\t<column id=\"" + dsplCleanString(tmpCol.getId()) + "\" type=\"string\"/>";
					tmpTable += "\n\t\t\t<column id=\"name\" type=\"string\"/>";
					tmpTable += "\n\t\t\t<data>";
					tmpTable += "\n\t\t\t\t<file format=\"csv\" encoding=\"utf-8\">" + dsplCleanString(tmpCol.getId()) + ".csv</file>";
					tmpTable += "\n\t\t\t</data>";
					tmpTable += "\n\t\t</table>";
					entityTables.add(tmpTable);
				}

				else if (tmpCol.getType().split(":")[0].equals("geo") && tmpCol.getType().split(":")[1].equals("location")) {

					tables += "\n\t\t\t<column id=\"zone\" type=\"string\"/>";

				}

				else if (tmpCol.getType().contains(":") && !tmpCol.getType().split(":")[0].equals("quantity")) {

		    /*if (needDefaultConcept) {
			tables += "\n\t\t\t<column id=\"default\" type=\"string\" />";
		    }*/

					String[] res = this.convertDSPLType(tmpCol.getType());

					// FIXME added for buggy Data Publica import
					if(tmpCol.getType().split(":")[1].equals("year")) tables += "\n\t\t\t<column id=\"time:year\" type=\"" + res[0] + "\" format=\"" + res[1] + "\"/>";
					else tables += "\n\t\t\t<column id=\"" + tmpCol.getType().split(":")[1] + "\" type=\"" + res[0] + "\" format=\"" + res[1] + "\"/>";
				}

				else
					tables += "\n\t\t\t<column id=\"" + dsplCleanString(tmpCol.getId()) + "\" type=\""+tmpCol.getType()+"\"/>";

			}

			tables += "\n\t\t\t<data>";
			tables += "\n\t\t\t\t<file format=\"csv\" encoding=\"utf-8\">" + slice.getFileName() + "</file>";
			tables += "\n\t\t\t</data>";

			slices += "\n\t\t\t<table ref=\"" + slice.getFileName().substring(0, slice.getFileName().lastIndexOf(".")) + "_table" + "\"/>";
			slices += "\n\t\t</slice>";

			tables += "\n\t\t</table>";

		}

	/*if (needDefaultConcept) {
	    concepts += "\n\t\t\t<concept id=\"default\" extends=\"entity:entity\">";
	    concepts += "\n\t\t\t\t<info>";
	    concepts += "\n\t\t\t\t\t<name>";
	    concepts += "\n\t\t\t\t\t\t<value xml:lang=\"fr\">Afficher</value>";
	    concepts += "\n\t\t\t\t\t</name>";
	    concepts += "\n\t\t\t\t\t<description>";
	    concepts += "\n\t\t\t\t\t\t<value xml:lang=\"fr\"></value>";
	    concepts += "\n\t\t\t\t\t</description>";
	    concepts += "\n\t\t\t\t</info>";
	    concepts += "\n\t\t\t\t<type ref=\"string\"/>";
	    concepts += "\n\t\t\t\t<table ref=\"default_table\"/>";
	    concepts += "\n\t\t\t</concept>";
	}*/

		concepts += "\n\t</concepts>";

		if (needZone) {
			tables += "<table id=\"zone_table\"><column id=\"zone\" type=\"string\"/><column id=\"name\" type=\"string\"/>"
					+ "<column id=\"latitude\" type=\"float\" /><column id=\"longitude\" type=\"float\" />"
					+ "<data><file format=\"csv\" encoding=\"utf-8\">zone.csv</file>" + "</data></table>";
		}

	/*else if (needDefaultConcept) {
	    tables += "\n\t\t<table id=\"default_table\">";
	    tables += "\n\t\t\t<column id=\"default\" type=\"string\"/>";
	    tables += "\n\t\t\t<column id=\"name\" type=\"string\"/>";
	    tables += "\n\t\t\t<data>";
	    tables += "\n\t\t\t\t<file format=\"csv\" encoding=\"utf-8\">default.csv</file>";
	    tables += "\n\t\t\t</data>";
	    tables += "\n\t\t</table>";
	}*/ else {
			for (String tab : entityTables)
				tables += tab;
		}

		tables += "\n\t</tables>";

		slices += "\n\t</slices>";

		return concepts + slices + tables;
	}

	public String zipFiles() {
		String dir = JsonSpreadsheetParsingService.dsplDir + File.separator + webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf("."));
		// String command = "zip -r "+JsonSpreadsheetParsingService.dsplDir +
		// File.separator + webSMatchFileName.substring(0,
		// webSMatchFileName.lastIndexOf("."))+".zip "+JsonSpreadsheetParsingService.dsplDir
		// + File.separator + webSMatchFileName.substring(0,
		// webSMatchFileName.lastIndexOf("."));

		String file = dir + ".zip";

		if (new File(file).exists())
			new File(file).delete();

	/*
	 * try { Process process = Runtime.getRuntime().exec(command); try {
	 * process.waitFor(); } catch (InterruptedException e) {
	 * L.Error(e.getMessage(),e); } } catch (IOException e) { L.Error(e.getMessage(),e);
	 * }
	 */

		return file;
	}

	private LinkedList<DSPLSlice> xmlGenerator(String targetNamespace) {

		// ok go on with the XML file for DSPL
		String xmlFile = "";

		xmlFile += xmlHeader(targetNamespace);

		// info
		xmlFile += xmlInfo(this.getDataSetName(), this.getDataSetDescription());

		// provider
		xmlFile += xmlProvider("");

		// slices
		WSMatchXMLtoDSPLLoader loader = new WSMatchXMLtoDSPLLoader(JsonSpreadsheetParsingService.dsplDir.replace("/dspl", "/xls/generatedXML") + File.separator
				+ webSMatchFileName);
		LinkedList<DSPLSlice> slices = loader.getSlices();
		xmlFile += this.xmlSlices(slices);

		// end
		xmlFile += "\n</dspl>";

		// first create dir
		File dir = new File(JsonSpreadsheetParsingService.dsplDir + File.separator + webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf(".")));
		if (dir.exists()) {
			File[] content = dir.listFiles();
			for (int i = 0; i < content.length; i++)
				content[i].delete();
			dir.delete();
		}

		boolean resDir = dir.mkdir();

		if (!resDir) {
			System.out.println("Problem creating dir : " + dir.getAbsolutePath());
			return null;
		}

		// ok try to create a DSPL file for this
		File file = new File(JsonSpreadsheetParsingService.dsplDir + File.separator + webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf("."))
				+ File.separator + webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf(".")) + "_dspl" + ".xml");
		if (file.exists())
			file.delete();

		boolean resFile = false;
		try {
			resFile = file.createNewFile();
		} catch (IOException e) {
			L.Error(e.getMessage(),e);
		}

		if (!resFile)
			System.out.println("Problem creating file : " + file.getAbsolutePath());

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));

			L.Debug(this.getClass().getSimpleName(),xmlFile,true);

			writer.write(xmlFile);

			writer.flush();
			writer.close();

		} catch (IOException e) {
			L.Error(e.getMessage(),e);
		}

		return slices;

	}

	private void csvGenerator(String xmlSchemaDocument, LinkedList<DSPLSlice> slicesList) {

		L.Debug(this.getClass().getSimpleName(),"Number of slices " + slicesList.size(),true);

		// ok try to get CCs
		L.Debug(this.getClass().getSimpleName(),"Tables ",true);

		WSMatchXMLLoader xmlLoader = new WSMatchXMLLoader(xmlSchemaDocument);

		@SuppressWarnings("unchecked")
		List<Element> tables = xmlLoader.getDocument().getRootElement().getChild("tables").getChildren("table");

		for (int ns = 0; ns < tables.size(); ns++) {
			Element table = tables.get(ns);

			L.Debug(this.getClass().getSimpleName(),"\tTable " + ns,true);

			DSPLSlice slice = slicesList.get(0);

			// search for the good slice
	    /*
	     * for (int slc = 0; slc < slicesList.size(); slc++) { if
	     * (slicesList.get(slc).getSheetNumber() == (ns + 1) &&
	     * slicesList.get(slc).getTableSheetNumber() == (ncc + 1)) { slice =
	     * slicesList.get(slc); } }
	     */

			if (slice == null)
				break;

			// ok try to create a CSV file for this
			File file = new File(JsonSpreadsheetParsingService.dsplDir + File.separator + webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf("."))
					+ File.separator + webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf(".")) + "_s" + (ns + 1) + "_t" + (0 + 1) + ".csv");
			if (file.exists())
				file.delete();

			boolean resFile = false;
			try {
				resFile = file.createNewFile();
			} catch (IOException e) {
				L.Error(e.getMessage(),e);
			}

			if (!resFile)
				System.out.println("Problem creating file : " + file.getAbsolutePath());

			else {

				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(file));

					@SuppressWarnings("unchecked")
					List<Element> attributes = table.getChild("attributes").getChildren("attribute");
					List<Element> datas = new ArrayList<Element>();

					for (int ele = 0; ele < attributes.size(); ele++) {

						Element attribute = attributes.get(ele);

						// set the datas
						datas.add(attribute.getChild("datas"));

						if (attribute.getChildText("dspltype").indexOf("geo:location") != -1) {
							//needDefaultConcept = false;
							needZone = true;
						}

						// if this is an entity and we have to make a specific
						// csv file
						if (attribute != null && attribute.getChildText("dspltype").split(":").length == 2
								&& (!attribute.getChildText("dspltype").split(":")[0].equals("quantity") && !attribute.getChildText("dspltype").split(":")[0].equals("time"))) {

							// need a csv file with
							// datas
							File entityFile = new File(JsonSpreadsheetParsingService.dsplDir + File.separator
									+ webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf(".")) + File.separator
									+ dsplCleanString(attribute.getChildText("name")) + ".csv");
							entityFile.createNewFile();

							BufferedWriter fileWriter = new BufferedWriter(new FileWriter(entityFile));

							fileWriter.write(dsplCleanString(attribute.getChildText("name")) + ",name");
							fileWriter.flush();

							// write data
							for (Element data : datas) {
								for(int vals = 0; vals <(data.getChildren("data")).size(); vals++){
									fileWriter.newLine();
									fileWriter.write(dsplCleanString(((Element) (data.getChildren("data").get(vals))).getText()));
									fileWriter.write(","+dsplCleanString(((Element) (data.getChildren("data").get(vals))).getText()));
								}
							}

							fileWriter.close();
						}
						//

						// firstline is attributes line
						if (attribute.getChildText("dspltype").indexOf("time:year") != -1)
							writer.write("year");
						else if (attribute.getChildText("dspltype").indexOf("geo:location") != -1)
							writer.write("zone");
						else if (attribute.getChildText("dspltype").indexOf("quantity:") != -1)
							writer.write(dsplCleanString(attribute.getChildText("name")));
						else if (attribute.getChildText("dspltype").indexOf(":") == -1)
							writer.write(dsplCleanString(attribute.getChildText("name")));
						else if (attribute.getChildText("dspltype").indexOf("entity:entity") != -1) {
							writer.write(attribute.getChildText("name"));
						} else
							writer.write(dsplCleanString(attribute.getChildText("name")));
						if (ele != attributes.size() - 1) {
							writer.write(",");
						} else {
							writer.newLine();
						}

					}

					int shortestLineSize = -1;

					for (Element data : datas) {
						if (shortestLineSize == -1)
							shortestLineSize = data.getChildren().size();
						if (data.getChildren().size() < shortestLineSize)
							shortestLineSize = data.getChildren().size();
					}

					for (int i = 0; i < shortestLineSize; i++) {

						for (int dataCount = 0; dataCount < datas.size(); dataCount++) {
							Element data = datas.get(dataCount);

							// check for numbers with spaces
							String value = dsplCleanString(((Element) (data.getChildren("data").get(i))).getText());

							try{
								value = new BigDecimal(value).toString();
							}catch(NumberFormatException nfe){
								//nfLog.Error(e.getMessage(),e);
							}
							//

							writer.write(value);
							if (dataCount < datas.size() - 1) {
								writer.write(",");
							} else {
								writer.newLine();
							}
						}
					}

					writer.flush();
					writer.close();

				} catch (IOException ioe) {
					L.Error(ioe.getMessage(),ioe);
				}

			}
			// end of CSV file

			// now, add (if needed) the default
			try {
		/*if (needDefaultConcept) {
		    // create tmp file
		    File tmpFile = new File(file + "_tmp");
		    tmpFile.createNewFile();

		    BufferedReader reader = new BufferedReader(new FileReader(file));
		    BufferedWriter writer = new BufferedWriter(new FileWriter(file + "_tmp"));

		    String toWrite = null;
		    boolean firstLine = true;

		    while ((toWrite = reader.readLine()) != null) {
			if (firstLine) {
			    writer.write("default," + toWrite);
			    firstLine = false;
			} else {
			    writer.write("Afficher," + toWrite);
			}
			writer.newLine();
		    }

		    reader.close();
		    writer.flush();
		    writer.close();

		    file.delete();

		    FileUtils.moveFile(tmpFile, file);

		    FileUtils.copyFile(new File(JsonSpreadsheetParsingService.dsplDir + File.separator + "default.csv"), new File(
			    JsonSpreadsheetParsingService.dsplDir + File.separator + webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf("."))
				    + File.separator + "default.csv"));
		}*/

				if (needZone) {

					FileUtils.copyFile(new File(JsonSpreadsheetParsingService.dsplDir + File.separator + "zone.csv"), new File(
							JsonSpreadsheetParsingService.dsplDir + File.separator + webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf("."))
									+ File.separator + "zone.csv"));
				}

			} catch (IOException e) {
				L.Error(e.getMessage(),e);
			}
		}
	}

    /*
     * private void csvGenerator(SimpleSheet[] sheets, LinkedList<DSPLSlice>
     * slicesList) {
     * 
     * if (_DEBUG) System.out.println("Number of slices : " +
     * slicesList.size());
     * 
     * // ok try to get CCs if (_DEBUG) System.out.println("CCs :"); for (int ns
     * = 0; ns < sheets.length; ns++) { SimpleSheet tmpSheet = sheets[ns];
     * 
     * if (_DEBUG) System.out.println("\tSheet : " + ns);
     * 
     * if(tmpSheet != null && tmpSheet.getConnexComps() != null) for (int ncc =
     * 0; ncc < tmpSheet.getConnexComps().size(); ncc++) {
     * 
     * HashMap<Integer, BufferedWriter> entityFiles = new HashMap<Integer,
     * BufferedWriter>(); HashMap<Integer, ArrayList<String>> values = new
     * HashMap<Integer, ArrayList<String>>();
     * 
     * DSPLSlice slice = null;
     * 
     * ConnexComposant cc = tmpSheet.getConnexComps().get(ncc);
     * 
     * // search for the good slice for (int slc = 0; slc < slicesList.size();
     * slc++) { if (slicesList.get(slc).getSheetNumber() == (ns + 1) &&
     * slicesList.get(slc).getTableSheetNumber() == (ncc + 1)) { slice =
     * slicesList.get(slc); } }
     * 
     * if (slice == null) break;
     * 
     * if (_DEBUG) System.out.println("\n\t\tStartX : " + cc.getStartX()); if
     * (_DEBUG) System.out.println("\t\tEndX : " + cc.getEndX()); if (_DEBUG)
     * System.out.println("\t\tStartY : " + cc.getStartY()); if (_DEBUG)
     * System.out.println("\t\tEndY : " + cc.getEndY());
     * 
     * // ok try to create a CSV file for this File file = new
     * File(JsonSpreadsheetParsingService.dsplDir + File.separator +
     * webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf(".")) +
     * File.separator + webSMatchFileName.substring(0,
     * webSMatchFileName.lastIndexOf(".")) + "_s" + (ns + 1) + "_t" + (ncc + 1)
     * + ".csv"); if (file.exists()) file.delete();
     * 
     * boolean resFile = false; try { resFile = file.createNewFile(); } catch
     * (IOException e) { L.Error(e.getMessage(),e); }
     * 
     * if (!resFile) System.out.println("Problem creating file : " +
     * file.getAbsolutePath());
     * 
     * else {
     * 
     * try { BufferedWriter writer = new BufferedWriter(new FileWriter(file));
     * 
     * boolean neverWrite = true; // boolean firstLine = true;
     * 
     * for (int line = cc.getStartY(); line <= cc.getEndY(); line++) { for (int
     * col = cc.getStartX(); col <= cc.getEndX(); col++) {
     * 
     * if (entityFiles.get(new Integer(col)) != null) {
     * 
     * if (!values.get(new
     * Integer(col)).contains(dsplCleanString(sheets[ns].getCells
     * ()[line][col].getContent()))) { entityFiles.get(new
     * Integer(col)).newLine(); entityFiles.get(new Integer(col)).write(
     * dsplCleanString(sheets[ns].getCells()[line][col].getContent()) + "," +
     * sheets[ns].getCells()[line][col].getContent()); entityFiles.get(new
     * Integer(col)).flush();
     * 
     * // values.get(new
     * Integer(col)).add(dsplCleanString(sheets[ns].getCells()[
     * line][col].getContent()));
     * 
     * } }
     * 
     * // first, replace content by editedContent if // any if
     * (sheets[ns].getCells().length > line &&
     * sheets[ns].getCells()[line].length > col) { if
     * (!sheets[ns].getCells()[line][col].getEditedContent().equals("") &&
     * !sheets
     * [ns].getCells()[line][col].getEditedContent().equals(sheets[ns].getCells
     * ()[line][col].getContent())) {
     * sheets[ns].getCells()[line][col].setContent
     * (sheets[ns].getCells()[line][col].getEditedContent()); } } //
     * 
     * if (sheets[ns].getCells().length > line &&
     * sheets[ns].getCells()[line].length > col) { // test if line has good size
     * if (sheets[ns].getCells()[line].length >= (cc.getEndX() - cc.getStartX())
     * + 1) { if (line <= cc.getEndY() && line > cc.getStartY() && col ==
     * cc.getStartX()) { // check the empty line case boolean isAnEmptyLine =
     * true; for (int colLine = cc.getStartX(); colLine <= cc.getEndX();
     * colLine++) { if
     * (!(sheets[ns].getCells()[line][colLine].getContent().isEmpty())) {
     * isAnEmptyLine = false; break; } } if (!isAnEmptyLine && line >
     * cc.getStartY()) { if (!neverWrite) writer.newLine(); } } if
     * (dsplCleanString
     * (sheets[ns].getCells()[line][col].getContent()).equals("")) { neverWrite
     * = false; writer.write("col" + col); } else {
     * 
     * if (slice != null && slice.getColumns().size() > col) { // get the good
     * col DSPLColumn column = null; for (DSPLColumn tmpCol :
     * slice.getColumns()) { if (tmpCol.getCol() - 1 == col && tmpCol.getLine()
     * - 1 == line) column = tmpCol; } // DSPLColumn column = //
     * slice.getColumns().get(col); //
     * System.out.println("col line : "+column.getLine() // + " line : " + line
     * + // " type : " + // column.getType());
     * 
     * if (column != null && column.getType().split(":").length == 2 &&
     * !column.getType().split(":")[0].equals("quantity")) { if
     * (column.getType().split(":")[0].equals("entity") &&
     * column.getType().split(":")[0].equals("entity")) {
     * writer.write(dsplCleanString(column.getId()));
     * 
     * // need a csv file with // datas File entityFile = new
     * File(JsonSpreadsheetParsingService.dsplDir + File.separator +
     * webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf(".")) +
     * File.separator + dsplCleanString(column.getId()) + ".csv");
     * entityFile.createNewFile();
     * 
     * BufferedWriter fileWriter = new BufferedWriter(new
     * FileWriter(entityFile));
     * 
     * fileWriter.write(dsplCleanString(column.getId()) + ",name");
     * fileWriter.flush();
     * 
     * // add to list entityFiles.put(new Integer(col), fileWriter);
     * 
     * // prepare values ArrayList<String> entVals = new ArrayList<String>();
     * values.put(new Integer(col), entVals);
     * 
     * } else writer.write(column.getType().split(":")[1]); } else {
     * writer.write
     * (dsplCleanString(sheets[ns].getCells()[line][col].getContent())); } }
     * 
     * else {
     * writer.write(dsplCleanString(sheets[ns].getCells()[line][col].getContent
     * ())); }
     * 
     * neverWrite = false; // firstLine = false; } if (col < cc.getEndX()) {
     * neverWrite = false; writer.write(","); } } } } }
     * 
     * for (BufferedWriter wr : entityFiles.values()) { wr.flush(); wr.close();
     * }
     * 
     * writer.flush(); writer.close();
     * 
     * } catch (IOException ioe) { ioLog.Error(e.getMessage(),e); }
     * 
     * } // end of CSV file
     * 
     * // now, add (if needed) the default try { if (needDefaultConcept &&
     * googleViz) { // create tmp file File tmpFile = new File(file + "_tmp");
     * tmpFile.createNewFile();
     * 
     * BufferedReader reader = new BufferedReader(new FileReader(file));
     * BufferedWriter writer = new BufferedWriter(new FileWriter(file +
     * "_tmp"));
     * 
     * String toWrite = null; boolean firstLine = true;
     * 
     * while ((toWrite = reader.readLine()) != null) { if (firstLine) {
     * writer.write("default," + toWrite); firstLine = false; } else {
     * writer.write("Afficher," + toWrite); } writer.newLine(); }
     * 
     * reader.close(); writer.flush(); writer.close();
     * 
     * file.delete();
     * 
     * FileUtils.moveFile(tmpFile, file);
     * 
     * FileUtils.copyFile(new File(JsonSpreadsheetParsingService.dsplDir +
     * File.separator + "default.csv"), new File(
     * JsonSpreadsheetParsingService.dsplDir + File.separator +
     * webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf(".")) +
     * File.separator + "default.csv")); }
     * 
     * if (needZone) {
     * 
     * FileUtils.copyFile(new File(JsonSpreadsheetParsingService.dsplDir +
     * File.separator + "zone.csv"), new File(
     * JsonSpreadsheetParsingService.dsplDir + File.separator +
     * webSMatchFileName.substring(0, webSMatchFileName.lastIndexOf(".")) +
     * File.separator + "zone.csv")); }
     * 
     * } catch (IOException e) { L.Error(e.getMessage(),e); }
     * 
     * } } }
     */

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

	public void setDataSetName(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	public String getDataSetName() {
		return dataSetName;
	}

	public void setDataSetDescription(String dataSetDescription) {
		this.dataSetDescription = dataSetDescription;
	}

	public String getDataSetDescription() {
		return dataSetDescription;
	}

	public void setTargetLangage(String targetLangage) {
		this.targetLangage = targetLangage;
	}

	public String getTargetLangage() {
		return targetLangage;
	}
}
