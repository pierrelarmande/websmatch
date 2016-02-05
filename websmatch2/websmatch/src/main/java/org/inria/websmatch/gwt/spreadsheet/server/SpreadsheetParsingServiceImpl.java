package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.rpc.ServiceException;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.dspl.DSPLExport;
import org.inria.websmatch.dspl.EntityMatcherImpl;
import org.inria.websmatch.evaluate.ProbaMatching;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetParsingService;
import org.inria.websmatch.gwt.spreadsheet.client.exceptions.ParserException;
import org.inria.websmatch.gwt.spreadsheet.client.listeners.MatchingProgressEvent;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;
import org.inria.websmatch.machineLearning.MLInstanceCompute;
import org.inria.websmatch.utils.DateUtils;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xls.SheetsSerializer;
import org.inria.websmatch.xls.XLSParser;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.dspl.DSPLExport;
import org.inria.websmatch.dspl.EntityMatcherImpl;
import org.inria.websmatch.gwt.spreadsheet.client.exceptions.ParserException;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;
import org.inria.websmatch.xls.SheetsSerializer;
import org.mitre.harmony.Harmony;
import org.mitre.harmony.matchers.ElementPair;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.YAMMatcherWrapper;
import org.mitre.schemastore.model.Alias;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Containment;
import org.mitre.schemastore.model.DomainValue;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Relationship;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.SchemaElementList;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.schemaImporters.SchemaImporter;
import org.mitre.schemastore.porters.schemaImporters.SpreadsheetImporter;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

import yam.system.Configs;
import yam.tools.WordNetHelper;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.RemoteEventServiceServlet;

public class SpreadsheetParsingServiceImpl extends RemoteEventServiceServlet implements SpreadsheetParsingService {

	/**
	 * TODO change the usage of username for key... it can't work if 2 users at
	 * same time (datapublica only)
	 */

	private HashMap<String, String> lastFile = new HashMap<String, String>();
	private HashMap<String, Schema> lastSchema = new HashMap<String, Schema>();
	private HashMap<String, SchemaElementList> list = new HashMap<String, SchemaElementList>();
	private HashMap<String, Boolean> modified = new HashMap<String, Boolean>();
	private HashMap<String, SpreadsheetImporter> importers = new HashMap<String, SpreadsheetImporter>();
	private HashMap<String, SimpleSheet[]> sheets = new HashMap<String, SimpleSheet[]>();
	// for each user
	private HashMap<String, String> originalXmls = new HashMap<String, String>();

	// store the thread for each user
	private HashMap<String, Thread> threads = new HashMap<String, Thread>();

	private String baseXLSDir = new String();// =
	// getServletContext().getInitParameter("xlsStorageDir");
	private String storeService;// =
	// getServletContext().getInitParameter("schemaStoreService");

	// private String baseXLSDir = "/tmp";
	// private String storeSerivce =
	// "http://193.49.106.32:8080/SchemaStore/services/SchemaStore";

	private String exportUri = "http://constraint.lirmm.fr:8320";
	// private String exportUri = "http://websmatch.lirmm.fr/dataprovider";

	/**
	 *
	 */
	private static final long serialVersionUID = -4743232521969724467L;

	//
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());

	}

	//

	public void init() {
		baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");
		storeService = getServletContext().getInitParameter("schemaStoreService");
		exportUri = getServletContext().getInitParameter("dataProviderUri");

		initWordnet();
	}

	public void initWordnet() {

		// init WordNet
		yam.system.Configs.WNTMP = System.getProperty("user.dir") + "/webapps/WebSmatch/WNTemplate.xml";
		yam.system.Configs.WNPROP = System.getProperty("user.dir") + "/webapps/WebSmatch/file_properties.xml";

		yam.system.Configs.WNDIR = System.getProperty("user.dir") + "/webapps/WebSmatch/WordNet/2.1/dict";

		try {
			WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
			WordNetHelper.getInstance().initializeIC(System.getProperty("user.dir") + "/webapps/WebSmatch/" + Configs.WNIC);
		} catch (Exception e) {
			L.Error(e.getMessage(),e);
		}
	}

	@Override
	public SimpleSheet[] parseSpreadsheet(final String userName, final String URI, boolean withAttrDetect, String schemaId) throws ParserException {
		// stop current running thread
		if (this.getThreadLocalRequest() == null) {
			if (threads.get(userName) != null)
				threads.get(userName).interrupt();
		} else if (threads.get(this.getThreadLocalRequest().getSession().getId()) != null) {
			threads.get(this.getThreadLocalRequest().getSession().getId()).interrupt();
		}

		// first step
		// write fix date problem
		Workbook workbook = null;
		try {
			workbook = Workbook.getWorkbook(new File(baseXLSDir + "/" + URI));
		} catch (BiffException | IOException e3) {
			e3.printStackTrace();
		}
		WritableWorkbook outWorkbook = null;
		try {
			outWorkbook = Workbook.createWorkbook(new File(baseXLSDir + "/" + URI + ".cleaned"), workbook);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		for (int sheet = 0; sheet < outWorkbook.getNumberOfSheets(); sheet++) {
			WritableSheet wSheet = null;
			if (outWorkbook != null) {
				wSheet = outWorkbook.getSheet(sheet);
			}

			List<Cell[]> allCells = new ArrayList<Cell[]>();
			for(int i = 0; i < wSheet.getRows(); i++){
				Cell[] tmpCells = wSheet.getRow(i);
				allCells.add(tmpCells);
			}

			// now we iterate to get the cells/attributes
			ListIterator<Cell[]> it = allCells.listIterator();
			int line = 0;

			// handle dateformat problem
			while (it.hasNext()) {
				Cell[] rowCells = it.next();
				for (int i = 0; i < rowCells.length; i++) {
					if (rowCells[i] != null && rowCells[i].getType().equals(CellType.DATE) && rowCells[i].getContents() != null) {
						DateFormat customDateFormat = new DateFormat("dd/MM/yyyy");
						WritableCellFormat dateFormat = new WritableCellFormat(customDateFormat);
						DateTime dateCell = new DateTime(i, line, ((DateCell) rowCells[i]).getDate(), dateFormat);
						Label label = new Label(i, line, DateUtils.convertDate(dateCell.getContents()));
						rowCells[i] = label;
						//
						try {
							wSheet.addCell(label);
						} catch (WriteException e) {
							L.Error(e.getMessage(),e);
						}
						//
					}
				}
				//datas.get(sheet).set(line, rowCells);
				line++;
			}
			// write modif
			if (outWorkbook != null) {
				try {
					outWorkbook.write();
				} catch (IOException e) {
					L.Error(e.getMessage(),e);
				}
			}
		}
		//
		try {
			outWorkbook.close();
		} catch (WriteException | IOException e) {
			L.Error(e.getMessage(),e);
		}
		//
		File deleteFile = new File(baseXLSDir + "/" + URI);
		deleteFile.delete();

		File oldFile = new File(baseXLSDir + "/" + URI + ".cleaned");

		try {
			FileUtils.moveFile(oldFile, deleteFile);
		} catch (IOException e) {
			L.Error(e.getMessage(),e);
		}
		// end
		//

		/**
		 * TODO reload using serialized XML!
		 */
		if (!withAttrDetect) {

			XLSParser xlsParser = new XLSParser(this.baseXLSDir + "/" + URI.replaceAll("\\s", "%20"));

			final SpreadsheetImporter xlsImporter = new SpreadsheetImporter();
			importers.put(this.getThreadLocalRequest().getSession().getId(), xlsImporter);
			lastFile.put(this.getThreadLocalRequest().getSession().getId(), URI);

			// needed for getDatas in ML update
			new Thread() {
				@Override
				public void run() {
					try {
						xlsImporter.getSchemaElements(new URI("file:" + baseXLSDir + File.separator + URI.replaceAll("\\s", "%20")));
					} catch (ImporterException e) {
						L.Error(e.getMessage(),e);
					} catch (URISyntaxException e) {
						L.Error(e.getMessage(),e);
					}
				}
			}.start();
			//

			SimpleSheet[] loadedsheets = xlsParser.parseFile(userName, URI.replaceAll("\\s", "%20"));

			// we need to store results for reloading
			if (this.getThreadLocalRequest() == null)
				sheets.put(userName, loadedsheets);
			else
				sheets.put(this.getThreadLocalRequest().getSession().getId(), loadedsheets);

			HashMap<String, String> data = new HashMap<>();
			data.put("user_id", userName);
			data.put("doc_url", URI);

			originalXmls.put(userName, JsonSpreadsheetParsingService.getXMLDoc(null, loadedsheets, false));

			return loadedsheets;

		}
		// end of the hack

		SimpleSheet[] results = new SimpleSheet[0];

		// we need to store results for reloading
		if (this.getThreadLocalRequest() == null)
			sheets.put(userName, results);
		else
			sheets.put(this.getThreadLocalRequest().getSession().getId(), results);

		// trying to find entities in all sheets of the spreadsheet
		final SpreadsheetImporter xlsImporter = new SpreadsheetImporter();

		if (this.getThreadLocalRequest() == null)
			importers.put(userName, xlsImporter);
		else
			importers.put(this.getThreadLocalRequest().getSession().getId(), xlsImporter);

		if (this.getThreadLocalRequest() == null)
			lastFile.put(userName, URI);
		else
			lastFile.put(this.getThreadLocalRequest().getSession().getId(), URI);

		//
		L.Debug(this.getClass().getSimpleName(), "Prepare for loading.", true);

		ArrayList<SchemaElement> se;
		try {
			se = xlsImporter.getSchemaElements(new URI("file:" + this.baseXLSDir + File.separator + URI.replaceAll("\\s", "%20")));
		} catch (ImporterException e) {
			L.Error(e.getMessage(),e);
			return results;
		} catch (URISyntaxException e) {
			L.Error(e.getMessage(),e);
			return results;
		}

		L.Debug(this.getClass().getSimpleName(), "getSchemaElements ended.", true);
		//

		// set modified
		if (this.getThreadLocalRequest() == null)
			modified.put(userName, new Boolean(false));
		else
			modified.put(this.getThreadLocalRequest().getSession().getId(), new Boolean(false));// =
		// false;

		// get the list... for storage
		/**
		 * @todo Change this shitty thing
		 */
		if (this.getThreadLocalRequest() == null)
			list.put(userName, new SchemaElementList());
		else
			list.put(this.getThreadLocalRequest().getSession().getId(), new SchemaElementList());

		SchemaElement[] sea = new SchemaElement[se.size()];

		for (int i = 0; i < se.size(); i++) {
			sea[i] = se.get(i);
		}

		if (this.getThreadLocalRequest() == null)
			list.put(userName, new SchemaElementList(sea));
		else
			list.put(this.getThreadLocalRequest().getSession().getId(), new SchemaElementList(sea));

		final ArrayList<List<Cell[]>> datas = xlsImporter.getDatas();

		final ArrayList<ArrayList<Cell>> attr = xlsImporter.getAttribCells();

		// init titles size
		results = new SimpleSheet[datas.size()];

		// ok save them it's not a reload
		if (this.getThreadLocalRequest() == null)
			sheets.put(userName, results);
		else
			sheets.put(this.getThreadLocalRequest().getSession().getId(), results);

		String currentSessionId = null;
		if (this.getThreadLocalRequest() != null)
			currentSessionId = this.getThreadLocalRequest().getSession().getId();
		else
			currentSessionId = userName;

		final String fCurrentSessionId = currentSessionId;

		for (int i = 0; i < results.length; i++) {
			results[i] = new SimpleSheet();
			results[i].setFilename(URI);
		}

		// ok first set the titles only
		for (int sheet = 0; sheet < datas.size(); sheet++) {
			// set the title
			results[sheet].setTitle(xlsImporter.getSheetNames().get(sheet));
		}

		final SimpleSheet[] localResults = results;
		int tmpSchemaId = -1;

		if (schemaId != null)
			tmpSchemaId = new Integer(schemaId);

		final int fschemaId = tmpSchemaId;

		Thread t = null;

		// then thread for ML detection
		t = new Thread() {

			@Override
			public void run() {

				for (int sheet = 0; sheet < datas.size(); sheet++) {

					ArrayList<SimpleCell[]> cells = new ArrayList<SimpleCell[]>();

					// now we iterate to get the cells/attributes
					ListIterator<Cell[]> it = datas.get(sheet).listIterator();

					int line = 0;

					while (it.hasNext()) {

						Cell[] rowCells = it.next();
						SimpleCell[] tmpSC = new SimpleCell[rowCells.length];

						for (int i = 0; i < rowCells.length; i++) {

							boolean isAttribute = false;

							if (attr.get(sheet).contains(rowCells[i]))
								isAttribute = true;

							if (rowCells[i] != null) {
								tmpSC[i] = new SimpleCell(rowCells[i].getContents(), isAttribute, rowCells[i].getRow(), rowCells[i].getColumn(), sheet);
								tmpSC[i].setSheetName(localResults[sheet].getTitle());

								// to set entityId and name, go with schema
								// elements
								Iterator<Attribute> ita = xlsImporter.getAttributes().values().iterator();

								while (ita.hasNext()) {

									Attribute attribute = ita.next();

									if (attribute.getName().equals(tmpSC[i].getContent())
											&& attribute.getDescription().equals(
											"x:" + tmpSC[i].getJxlCol() + "|y:" + tmpSC[i].getJxlRow() + "|sheet:" + tmpSC[i].getSheet())) {

										// ok this is the good one, get entity
										// id
										tmpSC[i].setEntityId(attribute.getEntityID());

										// TODO add type
										L.Debug(this.getClass().getSimpleName(), attribute.getName() + " domain id is " + attribute.getDomainID(),
												true);
										if (attribute.getDomainID().intValue() == 3)
											tmpSC[i].setContentType("string");
										else
											tmpSC[i].setContentType("numeric");

										// search entity
										Iterator<Entity> ite = xlsImporter.getEntities().values().iterator();

										while (ite.hasNext()) {

											Entity entity = ite.next();

											if (entity.getId().intValue() == tmpSC[i].getEntityId())
												tmpSC[i].setEntityName(entity.getName());

										}
									}
								}

							} else
								tmpSC[i] = new SimpleCell(new String(), isAttribute, -1, -1, sheet);

							// set file name
							tmpSC[i].setFilename(URI);
							tmpSC[i].setUsername(userName);

							// if attribute, check if we can find an entity type
							if (tmpSC[i].isAttribute()) {
								// get the CC for this cell
								ArrayList<ConnexComposant> ccs = xlsImporter.getConnexComps().get(sheet);

								EntityMatcherImpl entMatch = new EntityMatcherImpl();

								tmpSC[i] = entMatch.match(tmpSC[i], ccs, datas);
							}
							// end of dspl types matching
						}
						cells.add(tmpSC);
						line++;
					}

					// shitty case, first line contains data but not
					// attribute name
					// badly coded have to be rewrited
					ArrayList<ConnexComposant> ccs = xlsImporter.getConnexComps().get(sheet);

					for (ConnexComposant cc : ccs) {

						// hack
						// TODO remove this hack
						if (cc.isBiDimensionnalArray()) {

							Cell cell = datas.get(sheet).get(cc.getStartY())[0];
							SimpleCell sCell = new SimpleCell(cell.getContents(), true, cell.getRow(), cell.getColumn(), sheet);
							if (sCell.getContent() == null || sCell.getContent().equals("")) {
								sCell.setEditedContent("geo:location\\time:year (detected)");
								sCell.setContent("geo:location\\time:year (detected)");
							}

							// check the types and tag it
							EntityMatcherImpl entMatch = new EntityMatcherImpl();

							Cell[] array = Arrays.copyOfRange(xlsImporter.getDatas().get(sheet).get(cc.getStartY()), cc.getStartX(),
									cc.getEndX() - cc.getStartX() + 1);
							sCell = entMatch.matchLine(sCell, array);
							if (sCell.isDsplMapped()) {
								String lineType = sCell.getCurrentDsplMeta();
								List<Cell[]> localDatas = xlsImporter.getDatas().get(sheet);
								// load the values
								ArrayList<Cell> tmp = new ArrayList<Cell>();
								for (int i = cc.getStartY() + 1; i <= cc.getEndY(); i++) {
									if (localDatas.size() > i && localDatas.get(i).length > cc.getStartX())
										tmp.add(localDatas.get(i)[cc.getStartX()]);
								}
								//
								Cell[] subArray = null;
								// hack
								// TODO fix this
								if (cc.getStartY() > cc.getEndY() - cc.getStartY() + 1) {
									subArray = new Cell[1];
									subArray[0] = tmp.get(0);
									// Arrays.copyOfRange(tmp.toArray(new
									// Cell[tmp.size()]), cc.getStartY(),
									// cc.getStartY());
								} else
									subArray = Arrays.copyOfRange(tmp.toArray(new Cell[tmp.size()]), cc.getStartY(), cc.getEndY() - cc.getStartY() + 1);
								ArrayList<int[]> currentErrors = sCell.getErrorList();
								sCell = entMatch.matchLine(sCell, subArray);
								if (sCell.isDsplMapped()) {
									// ok bidim
									ArrayList<int[]> errors = sCell.getErrorList();
									for (int[] err : currentErrors) {
										errors.add(err);
									}
									sCell.setErrorList(errors);
									String colType = sCell.getCurrentDsplMeta();
									sCell.setEditedContent(colType + "\\" + lineType + " (detected)");
									sCell.setContent(colType + "\\" + lineType + " (detected)");
								}
							}
							//
							//

							SchemaElementList sel = list.get(fCurrentSessionId);
							sCell.setEntityId(sel.getEntities()[0].getId());
							sCell.setEntityName(sel.getEntities()[0].getName());

							SimpleCell[] tmpCells = cells.get(sCell.getJxlRow());
							tmpCells[sCell.getJxlCol()] = sCell;
							cells.set(sCell.getJxlRow(), tmpCells);

						}
						//

						else if (cc.isAttrInLines()) {
							// get first line of this cc
							Cell[] firstCCLine = datas.get(sheet).get(cc.getStartY());
							if (firstCCLine.length > 2) {
								if (firstCCLine[0].getContents().trim().equals("")) {
									SimpleCell sCell = new SimpleCell(firstCCLine[0].getContents(), false, firstCCLine[0].getRow(), firstCCLine[0].getColumn(),
											sheet);

									EntityMatcherImpl entMatch = new EntityMatcherImpl();

									sCell = entMatch.matchLine(sCell, firstCCLine);

									// ok make an attribute
									Attribute[] attr = new Attribute[list.get(fCurrentSessionId).getAttributes().length + 1];
									Attribute[] oldAttr = list.get(fCurrentSessionId).getAttributes();
									Attribute neighboorAttribute = null;
									// we need to find the common entity
									for (int each = 0; each < oldAttr.length; each++) {
										attr[each] = oldAttr[each];
										if (neighboorAttribute == null) {
											int neighboorX = new Integer(oldAttr[each].getDescription().split("\\p{Punct}")[1]).intValue();
											int neighboorY = new Integer(oldAttr[each].getDescription().split("\\p{Punct}")[3]).intValue();
											int neighboorS = new Integer(oldAttr[each].getDescription().split("\\p{Punct}")[5]).intValue();

											// good sheet?
											if (neighboorS == sheet && cc.containsPoint(neighboorX, neighboorY)) {
												Attribute attribute = new Attribute(SchemaImporter.nextId(), sCell.getContent(), "x:" + sCell.getJxlCol()
														+ "|y:" + sCell.getJxlRow() + "|sheet:" + sheet, oldAttr[each].getEntityID(), list.get(
														fCurrentSessionId).getDomains()[0].getId(), null, null, false, 0);
												sCell.setEntityId(oldAttr[each].getEntityID());
												attr[attr.length - 1] = attribute;
											}
										}
									}

									SimpleCell[] tmpCells = cells.get(sCell.getJxlRow());
									tmpCells[sCell.getJxlCol()] = sCell;
									cells.set(sCell.getJxlRow(), tmpCells);

									SchemaElementList sel = list.get(fCurrentSessionId);
									sel.setAttributes(attr);
									list.put(fCurrentSessionId, sel);
									//
								}
							}
						}
						// attributes are in column
						else {
							// get first line of this cc
							Cell[] firstCCColumn = new Cell[cc.getEndY() - cc.getStartY() + 1];
							int cpt = 0;
							for (int i = cc.getStartY(); i <= cc.getEndY(); i++) {
								if (cpt < firstCCColumn.length && datas.get(sheet).size() > i && datas.get(sheet).get(i).length > cc.getStartX()) {
									firstCCColumn[cpt] = datas.get(sheet).get(i)[cc.getStartX()];
									cpt++;
								}
							}
							if (firstCCColumn.length > 2) {
								if (firstCCColumn[0].getContents().trim().equals("")) {
									SimpleCell sCell = new SimpleCell(firstCCColumn[0].getContents(), false, firstCCColumn[0].getRow(),
											firstCCColumn[0].getColumn(), sheet);

									EntityMatcherImpl entMatch = new EntityMatcherImpl();
									sCell = entMatch.matchLine(sCell, firstCCColumn);

									// ok make an attribute
									Attribute[] attr = new Attribute[list.get(fCurrentSessionId).getAttributes().length + 1];
									Attribute[] oldAttr = list.get(fCurrentSessionId).getAttributes();
									Attribute neighboorAttribute = null;
									// we need to find the common entity
									for (int each = 0; each < oldAttr.length; each++) {
										attr[each] = oldAttr[each];
										if (neighboorAttribute == null) {
											int neighbourX = Integer.parseInt(oldAttr[each].getDescription().split("\\p{Punct}")[1]);
											int neighbourY = Integer.parseInt(oldAttr[each].getDescription().split("\\p{Punct}")[3]);
											int neighbourS = Integer.parseInt(oldAttr[each].getDescription().split("\\p{Punct}")[5]);

											// good sheet?
											if (neighbourS == sheet && cc.containsPoint(neighbourX, neighbourY)) {
												Attribute attribute = new Attribute(SchemaImporter.nextId(), sCell.getContent(), "x:" + sCell.getJxlCol()
														+ "|y:" + sCell.getJxlRow() + "|sheet:" + sheet, oldAttr[each].getEntityID(), list.get(
														fCurrentSessionId).getDomains()[0].getId(), null, null, false, 0);
												sCell.setEntityId(oldAttr[each].getEntityID());
												attr[attr.length - 1] = attribute;
											}
										}
									}

									SimpleCell[] tmpCells = cells.get(sCell.getJxlRow());
									tmpCells[sCell.getJxlCol()] = sCell;
									cells.set(sCell.getJxlRow(), tmpCells);

									SchemaElementList sel = list.get(fCurrentSessionId);
									sel.setAttributes(attr);
									list.put(fCurrentSessionId, sel);
									//
								}
							}
						}
					}
					//

					// set the merged cells for this sheet
					ArrayList<int[]> mCells = xlsImporter.getMergedCells().get(sheet);

					if (mCells != null) {
						for (int[] r : mCells) {
							if (cells.size() > r[1] && cells.get(r[1]).length > r[0])
								cells.get(r[1])[r[0]].setMerged(true);

							if (cells.size() > r[3] && cells.get(r[3]).length > r[2])
								cells.get(r[3])[r[2]].setMerged(true);
						}
					}
					//

					localResults[sheet].setCells(cells.toArray(new SimpleCell[cells.size()][]));
					localResults[sheet].setConnexComps(xlsImporter.getConnexComps().get(sheet));

					// set title and description for the document
					SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

					SchemaStoreObject sc = null;
					try {
						sc = serviceLoc.getSchemaStore(new URL(storeService));
						try {
							Schema currentSchema = sc.getSchema(fschemaId);
							if (currentSchema != null) {
								if (localResults[sheet] != null) {
									localResults[sheet].setStoredName(currentSchema.getName());
									localResults[sheet].setStoredDescription(currentSchema.getDescription());
								}
							}
						} catch (RemoteException e) {
							L.Error(e.getMessage(),e);
						}
					} catch (ServiceException e) {
						L.Error(e.getMessage(),e);
					} catch (MalformedURLException e1) {
						e1.printStackTrace();
					}
					//

					sheets.put(fCurrentSessionId, localResults);
				}

				HashMap<String, String> data = new HashMap<String, String>();
				data.put("user_id", userName);
				data.put("doc_url", URI);

				originalXmls.put(userName, JsonSpreadsheetParsingService.getXMLDoc(data, localResults, false));
			}
		};

		// add the thread
		if (this.getThreadLocalRequest() == null)
			threads.put(userName, t);
		else
			threads.put(this.getThreadLocalRequest().getSession().getId(), t);

		// start it
		t.start();

		while (t.isAlive()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				L.Error(e.getMessage(),e);
			}
		}

		// set the criters in simple sheet
	/*
	 * for (int s = 0; s < results.length; s++) {
	 * 
	 * HashMap<ConnexComposant, String> tmpMap =
	 * xlsImporter.getCriters().get(new Integer(s)); ArrayList<String>
	 * criters = results[s].getCriters();
	 * 
	 * for (ConnexComposant key : tmpMap.keySet()) {
	 * ArrayList<ConnexComposant> ccs = results[s].getConnexComps(); for
	 * (int i = 0; i < ccs.size(); i++) { if (key.getStartX() ==
	 * ccs.get(i).getStartX() && key.getEndX() == ccs.get(i).getEndX() &&
	 * key.getStartY() == ccs.get(i).getStartY() && key.getEndY() ==
	 * ccs.get(i).getEndY()) { criters.add(tmpMap.get(key)); } else {
	 * criters.add(new String()); } } } results[s].setCriters(criters); }
	 */
		//

		return results;

	}

	@Override
	@Deprecated
	public List<SchemaData> getSchemas(boolean onlyXls, String user) {

		List<SchemaData> res = new ArrayList<SchemaData>();

		if (user == null) {
			System.out.println("User is null.");
			return res;
		}

		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();
		try {
			SchemaStoreObject sc = serviceLoc.getSchemaStore(new URL(this.storeService));

			Schema[] sch = sc.getSchemas();

			for (int i = 0; i < sch.length; i++) {
				if (onlyXls) {
					if (sch[i].getAuthor().equals(user) && sch[i].getType().contains("XLS file import")) {
						res.add(new SchemaData(sch[i].getName(), sch[i].getSource(), sch[i].getAuthor(), sch[i].getDescription(), sch[i].getId().toString()));
					}
				} else {
					if (sch[i].getAuthor().equals(user)) {
						res.add(new SchemaData(sch[i].getName(), sch[i].getSource(), sch[i].getAuthor(), sch[i].getDescription(), sch[i].getId().toString()));
					}
				}
			}

		} catch (ServiceException e) {
			L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
			L.Error(e.getMessage(),e);
		} catch (MalformedURLException e) {
			L.Error(e.getMessage(),e);
		}

		return res;
	}

	@Override
	public List<String> storeSchema(String name, String source, final String author, String description, boolean withML, Integer crawl_id,
									Integer publication_id, SimpleSheet[] editedSheets) {

		// original list, used for comparison (delete/add)
		SchemaElementList originalList = null;

		// shemaId
		int xmlSchemaId = -1;

		List<String> attributes = new ArrayList<String>();

		List<SchemaData> schemas = new ArrayList<SchemaData>();

		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();
		try {
			SchemaStoreObject sc = serviceLoc.getSchemaStore(new URL(this.storeService));

			Schema[] sch = sc.getSchemas();

			if (sch != null) {
				for (int i = 0; i < sch.length; i++) {
					if (sch[i].getAuthor().equals(author)) {
						schemas.add(new SchemaData(sch[i].getName(), sch[i].getSource(), sch[i].getAuthor(), sch[i].getDescription(), sch[i].getId().toString()));
					}
				}
			}

		} catch (ServiceException e) {
			L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
			L.Error(e.getMessage(),e);
		} catch (MalformedURLException e) {
			L.Error(e.getMessage(),e);
		}

		// before storing schema, we have to see if the couple (source/author)
		// already exist, and update it instead of creating a new schema
		int idToDel = -1;

		String uriToTest = new String();

		if (source == null || source.equals("")) {
			if (this.getThreadLocalRequest() == null)
				uriToTest = lastFile.get(author);
			uriToTest = lastFile.get(this.getThreadLocalRequest().getSession().getId());
		}

		else
			uriToTest = source;

		// then search it
		ListIterator<SchemaData> it = schemas.listIterator();

		while (it.hasNext()) {

			SchemaData sd = it.next();
			if (uriToTest.equals(sd.getSource()))
				idToDel = new Integer(sd.getId()).intValue();

		}
		// end of testing/deleting

		Schema sch = new Schema();
		sch.setName(name);
		sch.setAuthor(author);

		if (this.getThreadLocalRequest() == null)
			sch.setSource(lastFile.get(author));

		else if ((source == null || source.equals("")) && lastFile.get(this.getThreadLocalRequest().getSession().getId()) != null)
			sch.setSource(lastFile.get(this.getThreadLocalRequest().getSession().getId()));
		else
			sch.setSource("");
		sch.setDescription(description);
		// set a local id
		sch.setId(SpreadsheetImporter.nextId());

		if (this.getThreadLocalRequest() == null)
			sch.setType("XLS file import");

		else if (modified.containsKey(this.getThreadLocalRequest().getSession().getId())
				&& modified.get(this.getThreadLocalRequest().getSession().getId()).booleanValue() == false)
			sch.setType("XLS file import");
		else
			sch.setType("XLS file import (user modified)");

		// ok, so the problem is that some entity may not have attributes now...
		// so that's not entities, only bad cc detection
		if (this.getThreadLocalRequest() != null && modified.containsKey(this.getThreadLocalRequest().getSession().getId())
				&& modified.get(this.getThreadLocalRequest().getSession().getId()).booleanValue() == true) {

			Attribute[] attrs = list.get(this.getThreadLocalRequest().getSession().getId()).getAttributes();
			Entity[] enti = list.get(this.getThreadLocalRequest().getSession().getId()).getEntities();

			L.Debug(this.getClass().getSimpleName(), "Store attr size : " + attrs.length, true);

			for (int i = 0; i < attrs.length; i++) {
				L.Debug(this.getClass().getSimpleName(),
						"Attr " + attrs[i].getName() + " Id " + attrs[i].getId() + " EntId " + attrs[i].getEntityID(), true);
			}

			for (int i = 0; i < enti.length; i++) {
				L.Debug(this.getClass().getSimpleName(), "Ent " + enti[i].getName() + " Id " + enti[i].getId(), true);
			}

			ArrayList<Entity> newEnti = new ArrayList<Entity>();

			for (int i = 0; i < enti.length; i++) {
				for (int j = 0; j < attrs.length; j++) {
					if (attrs[j].getEntityID() == enti[i].getId()) {
						newEnti.add(enti[i]);
						break;
					}
				}
			}

			list.get(this.getThreadLocalRequest().getSession().getId()).setEntities(newEnti.toArray(new Entity[newEnti.size()]));
		}

		if (this.getThreadLocalRequest() == null)
			this.lastSchema.put(author, sch);
		else
			this.lastSchema.put(this.getThreadLocalRequest().getSession().getId(), sch);

		serviceLoc = new SchemaStoreServiceLocator();

		try {
			SchemaStoreObject sc = null;
			try {
				sc = serviceLoc.getSchemaStore(new URL(this.storeService));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}

			try {

				// ok get the id and match with all schemas from this group,
				// threaded
				final Integer schemaId;

				// test if we update
				if (idToDel != -1) {

					originalList = sc.getSchemaElements(idToDel);

					SchemaElementList toUpdateSEL = null;

					if (this.getThreadLocalRequest() == null)
						toUpdateSEL = list.get(author);
					else
						toUpdateSEL = list.get(this.getThreadLocalRequest().getSession().getId());

					if (toUpdateSEL != null) {
						SchemaElement[] originalEles = originalList.geetSchemaElements();
						SchemaElement[] eles = toUpdateSEL.geetSchemaElements();

						// add first
						for (SchemaElement ele : eles) {
							boolean toAdd = true;
							for (SchemaElement originalEle : originalEles) {
								if (ele.getId().intValue() == originalEle.getId().intValue())
									toAdd = false;
							}
							if (toAdd) {
								L.Debug(this.getClass().getSimpleName(), "Add " + ele + " " + ele.getId(), true);
								// Updates an entity
								if (ele instanceof Entity) {
									sc.addEntity((Entity) ele);
								}

								// Updates an attribute
								if (ele instanceof Attribute) {
									sc.addAttribute((Attribute) ele);
								}

								// Updates a domain
								if (ele instanceof Domain) {
									sc.addDomain((org.mitre.schemastore.model.Domain) ele);
								}

								// Updates an domain value
								if (ele instanceof DomainValue) {
									sc.addDomainValue((DomainValue) ele);
								}

								// Updates a relationship
								if (ele instanceof Relationship) {
									sc.addRelationship((Relationship) ele);
								}

								// Updates a containment relationship
								if (ele instanceof Containment) {
									sc.addContainment((Containment) ele);
								}

								// Updates a subset relationship
								if (ele instanceof Subtype) {
									sc.addSubtype((Subtype) ele);
								}

								// Updates an alias
								if (ele instanceof Alias) {
									sc.addAlias((Alias) ele);
								}
							}
						}

						// remove now
						for (SchemaElement ele : originalEles) {
							boolean toRemove = true;
							for (SchemaElement originalEle : eles) {
								if (ele.getId().intValue() == originalEle.getId().intValue())
									toRemove = false;
							}
							if (toRemove) {
								if (!(ele instanceof Entity))
									L.Debug(this.getClass().getSimpleName(), "Remove " + ele + " " + ele.getId(), true);
								// Updates an entity
				/*
				 * if(ele instanceof Entity) {
				 * sc.deleteEntity(((Entity) ele).getId()); }
				 */

								// Updates an attribute
								if (ele instanceof Attribute) {
									sc.deleteAttribute(((Attribute) ele).getId());
								}

								// Updates a domain
								if (ele instanceof Domain) {
									sc.deleteDomain(((org.mitre.schemastore.model.Domain) ele).getId());
								}

								// Updates an domain value
								if (ele instanceof DomainValue) {
									sc.deleteDomainValue(((DomainValue) ele).getId());
								}

								// Updates a relationship
								if (ele instanceof Relationship) {
									sc.deleteRelationship(((Relationship) ele).getId());
								}

								// Updates a containment relationship
								if (ele instanceof Containment) {
									sc.deleteContainment(((Containment) ele).getId());
								}

								// Updates a subset relationship
								if (ele instanceof Subtype) {
									sc.deleteSubtype(((Subtype) ele).getId());
								}

								// Updates an alias
								if (ele instanceof Alias) {
									sc.deleteAlias(((Alias) ele).getId());
								}
							}
						}
					}

					// sc.deleteSchema(idToDel);
					sch.setId(idToDel);
					boolean updateOk = sc.updateSchema(sch);
					L.Debug(this.getClass().getSimpleName(), "Update ok " + updateOk, true);
					schemaId = idToDel;

				}

				else if (this.getThreadLocalRequest() == null)
					schemaId = sc.importSchema(sch, list.get(author));
				else
					schemaId = sc.importSchema(sch, list.get(this.getThreadLocalRequest().getSession().getId()));

				L.Debug(this.getClass().getSimpleName(), "Store id " + schemaId, true);
				L.Debug(this.getClass().getSimpleName(), "Attr size "
						+ list.get(this.getThreadLocalRequest().getSession().getId()).getAttributes().length, true);

				if (schemaId == null)
					return attributes;

				xmlSchemaId = schemaId.intValue();

				// group
				final Integer groupId;
				// TODO fix this, bad login
				if (author.equals("datapublica"))
					groupId = new Integer(6);
				else if (author.equals("leo"))
					groupId = new Integer(7);
				else if (this.getThreadLocalRequest() == null)
					groupId = new Integer(0);
				else
					groupId = LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId());

				// store the document if not exists
				MySQLDBConnector sqlconnector = new MySQLDBConnector();
				sqlconnector.connect();
				java.sql.Statement stat = sqlconnector.getStat();

				// firstly, we have to see if this file has already an id
				String query = "SELECT id FROM stored_schemas WHERE id ='" + schemaId.intValue() + "';";
				ResultSet result;
				try {
					result = stat.executeQuery(query);

					// if next is false, no doc, else get the id
					if (result.next()) {
						query = "UPDATE stored_schemas SET name ='" + name + "', original_xml = '" + originalXmls.get(author) + "' WHERE id ='"
								+ schemaId.intValue() + "';";
						stat.executeUpdate(query);
					} else {
						// so we insert the doc name and get the id
						if (groupId != null) {
							query = "INSERT INTO stored_schemas (id, name, id_group, xml, original_xml) VALUES ('" + schemaId.intValue() + "','" + name + "','"
									+ groupId.intValue() + "','','" + originalXmls.get(author) + "');";
						} else {
							query = "INSERT INTO stored_schemas (id, name, id_group, xml, original_xml) VALUES ('" + schemaId.intValue() + "','" + name
									+ "','0','','" + originalXmls.get(author) + "');";
						}
						stat.executeUpdate(query);
					}
					stat.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				sqlconnector.close();
				//

				// test if datapublica, insert into map
				if (crawl_id != null || publication_id != null) {
					MySQLDBConnector connector = new MySQLDBConnector();
					connector.connect();

					try {
						if (publication_id != null)
							connector.getStat().executeUpdate(
									"INSERT INTO datapublica_map VALUES (" + schemaId + "," + crawl_id + "," + publication_id + ")"
											+ " ON DUPLICATE KEY UPDATE crawl_id='" + crawl_id + "', publication_id='" + publication_id + "'");
						else
							connector.getStat().executeUpdate(
									"INSERT INTO datapublica_map VALUES (" + schemaId + "," + crawl_id + "," + publication_id + ")"
											+ " ON DUPLICATE KEY UPDATE crawl_id='" + crawl_id + "';");
					} catch (SQLException e) {
						L.Error(e.getMessage(),e);
					}

					connector.close();
				}

				final SchemaStoreObject scc = sc;

				final String tmpSid;
				if (this.getThreadLocalRequest() == null)
					tmpSid = author;
				else
					tmpSid = this.getThreadLocalRequest().getSession().getId();

				final String schAuthor = author;
				// ok get the list of matching technics
				MatchingResultsServiceImpl serviceImpl = new MatchingResultsServiceImpl();
				final List<SimpleMatchTech> techs = serviceImpl.getMatchingTechs();

				new Thread("MatchingThread") {
					public void run() {

						//
						Domain dom = DomainFactory.getDomain(tmpSid);
						MatchingProgressEvent event = new MatchingProgressEvent();
						//

						try {
							Schema[] schstmp = scc.getSchemas();

							// ok we filter by user before matching
							ArrayList<Schema> schslist = new ArrayList<Schema>();
							for (Schema s : schstmp) {
								if (s.getAuthor().trim().equals(schAuthor.trim()))
									schslist.add(s);
							}

							Schema[] schs = schslist.toArray(new Schema[] {});
							//

							event.setMaxMatch((schs.length - 1) * (techs.size() + 1));
							int cpt = 0;

							ProbaMatching proba = new ProbaMatching();
							MatchingResultsServiceImpl matchService = new MatchingResultsServiceImpl();

							if (schs != null && schs.length > 1)
								for (Schema s : schs) {
									if (!s.getId().equals(schemaId)) {

										// we now have to store only proba
										// matching high enough results
										HashMap<String, MatcherScores> scoresByTech = new HashMap<String, MatcherScores>();

										for (SimpleMatchTech tech : techs) {
											cpt++;
											event.setMatchCount(cpt);
											event.setMsg("Matching " + (int) (((double) cpt / (double) event.getMaxMatch()) * 100.0)
													+ "% complete. Currently matching : " + s.getName() + " using " + tech.getName() + ".");
											addEvent(dom, event);

											// TODO fix the groupid problem with
											// datapublica
											// we get the scores, as they will
											// not be inserted in DB (see
											// matchSchemas code)
											if (author.equals("datapublica"))
												scoresByTech.put(tech.getName(), localMatchSchemas(schemaId, s.getId(), 6, tech.getName()));
											else if (author.equals("leo"))
												scoresByTech.put(tech.getName(), localMatchSchemas(schemaId, s.getId(), 7, tech.getName()));

											else if (groupId == null)
												scoresByTech.put(tech.getName(), localMatchSchemas(schemaId, s.getId(), 0, tech.getName()));
											else
												scoresByTech.put(tech.getName(), localMatchSchemas(schemaId, s.getId(), groupId, tech.getName()));

											try {
												sleep(50);
											} catch (InterruptedException e) {
												L.Error(e.getMessage(),e);
											}
										}

										// ok make the same thing for probas
										cpt++;
										event.setMatchCount(cpt);
										event.setMsg("Matching " + (int) (((double) cpt / (double) event.getMaxMatch()) * 100.0)
												+ "% complete. Currently matching : " + s.getName() + " using probabilistic combination.");
										addEvent(dom, event);

										// proba.matchSchemas(schemaId,
										// s.getId());
										proba.matchSchemas(schemaId, s.getId(), scoresByTech, MatchingResultsServiceImpl.avgProba);

										// we insert probas
										matchService.insertProbaDistance(s.getId(), schemaId);

									}
								}

							proba.close();

							// ok update cluster now
							event.setMsg("Processing clustering calculation.");
							addEvent(dom, event);

							event.setMatchCount(0);
							event.setMaxMatch(0);
							event.setMsg("Processing terminated, clustering up to date");
							addEvent(dom, event);

							// TODO remove the old one
							// ClusteringServiceImpl service = new
							// ClusteringServiceImpl();
							// service.updateClusters(schemaId);

						} catch (RemoteException e) {
							L.Error(e.getMessage(),e);
						}
					}
				}.start();
				// end of matching thread
			} catch (RemoteException e) {

				L.Error(e.getMessage(),e);
			}

		} catch (ServiceException e) {

			L.Error(e.getMessage(),e);
		}

		if (withML) {
			// ok now we import the results in ML db
			MLInstanceCompute fact = new MLInstanceCompute(sch.getSource().replaceAll("\\s", "%20"), author, importers.get(this.getThreadLocalRequest()
					.getSession().getId()));
			fact.insertDatas(true);
		}

		// ok add attr to the list and send them back
		SchemaElementList eleListToSend = null;
		if (this.getThreadLocalRequest() == null)
			eleListToSend = list.get(author);
		else
			eleListToSend = list.get(this.getThreadLocalRequest().getSession().getId());
		Attribute[] attArray = eleListToSend.getAttributes();

		// TODO encode for xml special char
		for (int i = 0; i < attArray.length; i++) {
			attributes.add(attArray[i].getName());
		}

		// insert or update xml
		// TODO fix this
		if (editedSheets != null) {

			HashMap<String, String> data = new HashMap<String, String>();
			data.put("user_id", author);
			data.put("doc_url", sch.getSource());

			String xmlDoc = JsonSpreadsheetParsingService.getXMLDoc(data, editedSheets, true);

			MySQLDBConnector connector = new MySQLDBConnector();
			connector.connect();

			try {
				connector.getStat().executeUpdate("UPDATE stored_schemas SET xml = '" + xmlDoc + "' WHERE id = '" + xmlSchemaId + "';");
			} catch (SQLException e) {
				L.Error(e.getMessage(),e);
			}

			connector.close();
		}

		if (originalXmls.get(author) != null) {
			MySQLDBConnector connector = new MySQLDBConnector();
			connector.connect();

			try {
				connector.getStat().executeUpdate(
						"UPDATE stored_schemas SET original_xml = '" + originalXmls.get(author) + "' WHERE id = '" + xmlSchemaId + "';");
			} catch (SQLException e) {
				L.Error(e.getMessage(),e);
			}

			connector.close();
		}
		//

		return attributes;
	}

	@Override
	@Deprecated
	public List<SchemaData> getSchemas(boolean onlyXls) {
		return this.getSchemas(onlyXls, null);
	}

	// we need to match only one element
	public MatcherScores localMatchElements(Integer sourceId, Integer targetId, Integer ele1, Integer ele2, Integer group, String tech) {

		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

		try {
			SchemaStoreObject sc = null;
			try {
				sc = serviceLoc.getSchemaStore(new URL(this.storeService));
				Schema sourceSchema = sc.getSchema(sourceId);
				Schema targetSchema = sc.getSchema(targetId);

				// get elements
				ArrayList<SchemaElement> sourceElements = new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(sourceId).geetSchemaElements()));
				ArrayList<SchemaElement> targetElements = new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(targetId).geetSchemaElements()));

				// get elements
				SchemaInfo sourceInfo = new SchemaInfo(sourceSchema, null, sourceElements);
				SchemaInfo targetInfo = new SchemaInfo(targetSchema, null, targetElements);

				HierarchicalSchemaInfo hsourceInfo = new HierarchicalSchemaInfo(sourceInfo);
				HierarchicalSchemaInfo htargetInfo = new HierarchicalSchemaInfo(targetInfo);

				// finally filtered
				FilteredSchemaInfo fsourceInfo = new FilteredSchemaInfo(hsourceInfo);
				FilteredSchemaInfo ftargetInfo = new FilteredSchemaInfo(htargetInfo);

				// ExactInriaMatcher matcher = new ExactInriaMatcher();
				YAMMatcherWrapper matcher = new YAMMatcherWrapper();

				matcher.initialize(fsourceInfo, ftargetInfo);
				if (tech == null)
					matcher.setChoosenTech("Stoilos_JW");
				else
					matcher.setChoosenTech(tech);
				// TODO fix this
				if (group != null)
					matcher.setUserGroup(group);
				else
					matcher.setUserGroup(0);

				// TODO fix this, now it inserts only elements
				Harmony.yamDB = false;

				// we get the scores
				MatcherScores scores = matcher.match();

				MatcherScores choosenScore = new MatcherScores(0.0);

				// ok print scores

				for (ElementPair pair : scores.getElementPairs()) {
					// System.out.println(scores.getScore(pair).getPositiveEvidence());
					if ((pair.getSourceElement().intValue() == ele1.intValue() && pair.getTargetElement().intValue() == ele2.intValue())
							|| (pair.getSourceElement().intValue() == ele2.intValue() && pair.getTargetElement().intValue() == ele1.intValue())) {
						choosenScore.setScore(pair.getSourceElement(), pair.getTargetElement(), scores.getScore(pair));
					}
				}

				return choosenScore;

			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				// return null;
			}

		} catch (ServiceException e) {
			L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
			L.Error(e.getMessage(),e);
		}
		return null;
	}

	// specific for local use
	public MatcherScores localMatchSchemas(Integer sourceId, Integer targetId, Integer group, String tech) {

		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

		try {
			SchemaStoreObject sc = null;
			try {
				sc = serviceLoc.getSchemaStore(new URL(this.storeService));
				Schema sourceSchema = sc.getSchema(sourceId);
				Schema targetSchema = sc.getSchema(targetId);

				// get elements
				SchemaInfo sourceInfo = new SchemaInfo(sourceSchema, null, new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(sourceId)
						.geetSchemaElements())));
				SchemaInfo targetInfo = new SchemaInfo(targetSchema, null, new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(targetId)
						.geetSchemaElements())));

				HierarchicalSchemaInfo hsourceInfo = new HierarchicalSchemaInfo(sourceInfo);
				HierarchicalSchemaInfo htargetInfo = new HierarchicalSchemaInfo(targetInfo);

				// finally filtered
				FilteredSchemaInfo fsourceInfo = new FilteredSchemaInfo(hsourceInfo);
				FilteredSchemaInfo ftargetInfo = new FilteredSchemaInfo(htargetInfo);

				// ExactInriaMatcher matcher = new ExactInriaMatcher();
				YAMMatcherWrapper matcher = new YAMMatcherWrapper();

				matcher.initialize(fsourceInfo, ftargetInfo);
				if (tech == null)
					matcher.setChoosenTech("Stoilos_JW");
				else
					matcher.setChoosenTech(tech);
				matcher.setUserGroup(group);

				// TODO fix this, now it inserts only elements
				Harmony.yamDB = true;

				// we get the scores
				MatcherScores scores = matcher.match();

				return scores;

				// ok print scores
		/*
		 * for(ElementPair pair : scores.getElementPairs()){
		 * System.out.println
		 * (scores.getScore(pair).getPositiveEvidence()); }
		 */

			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				// return null;
			}

		} catch (ServiceException e) {
			L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
			L.Error(e.getMessage(),e);
		}
		return null;

	}

	@Override
	public void matchSchemas(Integer sourceId, Integer targetId, Integer group, String tech) {

		this.localMatchSchemas(sourceId, targetId, group, tech);

	}

	@Override
	public ArrayList<SimpleEntity> getEntityItem() {

		ArrayList<SimpleEntity> items = new ArrayList<SimpleEntity>();

		// search the entity name and id
		Entity[] ents = list.get(this.getThreadLocalRequest().getSession().getId()).getEntities();
		for (int ent = 0; ent < ents.length; ent++) {
			items.add(new SimpleEntity(ents[ent].getId(), ents[ent].getName()));
		}

		return items;
	}

	private List<Attribute> updateSheets(HashMap<String, String> data, SimpleSheet[] editedSheets) {

		List<Attribute> attributes = new ArrayList<Attribute>();
		// ok add attr to the list and send them back
		SchemaElementList eleListToSend = list.get(this.getThreadLocalRequest().getSession().getId());
		Attribute[] attArray = eleListToSend.getAttributes();

		// TODO encode for xml special char
		for (int i = 0; i < attArray.length; i++) {
			attributes.add(attArray[i]);
		}

		// replace sheets on server... if they were edited
		SimpleSheet[] currentSheets = sheets.get(this.getThreadLocalRequest().getSession().getId());

		for (int i = 0; i < editedSheets.length; i++) {
			if (editedSheets[i] != null)
				currentSheets[i] = editedSheets[i];
		}

		sheets.put(this.getThreadLocalRequest().getSession().getId(), currentSheets);

		return attributes;
	}

	public String createXMLFile(HashMap<String, String> data, SimpleSheet[] editedSheets, boolean withData) {

		String filePath = new String();
		// List<Attribute> attributes = this.updateSheets(data, editedSheets);
		sheets.put(this.getThreadLocalRequest().getSession().getId(), editedSheets);

		// check if dir exists
		File dir = new File(this.baseXLSDir + File.separator + "generatedXML");

		if (!dir.exists())
			dir.mkdir();

		String xmlFileName = editedSheets[0].getFilename().substring(0, editedSheets[0].getFilename().lastIndexOf(".")) + ".xml";

		// ok create and fill the file
		File outputFile = new File(this.baseXLSDir + File.separator + "generatedXML" + File.separator + xmlFileName);

		if (outputFile.exists())
			outputFile.delete();

		L.Debug(this.getClass().getSimpleName(), "Output " + outputFile.getAbsolutePath(), true);

		try {
			if (outputFile.createNewFile()) {

				// ok continue
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
				writer.write(JsonSpreadsheetParsingService.getXMLDoc(data, sheets.get(this.getThreadLocalRequest().getSession().getId()), withData));
				writer.close();
				return outputFile.getAbsolutePath();

			}
		} catch (IOException e) {
			L.Error(e.getMessage(),e);
		}

		return filePath;
	}

	public String createDSPLFile(String webSMatchFileName, SimpleSheet[] sheets, String dataSetName, String dataSetDescription) {
		String zipToDownload = new String();

		if (JsonSpreadsheetParsingService.dsplDir == null)
			JsonSpreadsheetParsingService.dsplDir = getServletContext().getInitParameter("dsplDir");

		DSPLExport exporter = new DSPLExport(webSMatchFileName, dataSetName, dataSetDescription, "fr");

		exporter.dsplGenerate("<xml>" + new SheetsSerializer(sheets).toXML(true) + "</xml>");

		// zip
		zipToDownload = exporter.zipFiles();

		return zipToDownload;
	}

	public void sendToDataPublica(HashMap<String, String> data, SimpleSheet[] editedSheets, boolean withData) {
		List<Attribute> attributes = this.updateSheets(data, editedSheets);
		JsonSpreadsheetParsingService.createAndSendPostRequest(data, attributes, sheets.get(this.getThreadLocalRequest().getSession().getId()), withData);
	}

	public String getStoreService() {
		return storeService;
	}

	public void setStoreService(String storeService) {
		this.storeService = storeService;
	}

	public String getBaseXLSDir() {
		return baseXLSDir;
	}

	public void setBaseXLSDir(String baseXLSDir) {
		this.baseXLSDir = baseXLSDir;
	}

	@Override
	public SimpleSheet getSheet(String userName, int place) {

		SimpleSheet sheet = new SimpleSheet();

		// just wait for the end of the thread
		if (this.getThreadLocalRequest() == null) {
			while (threads.get(userName) != null && threads.get(userName).isAlive())
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					L.Error(e.getMessage(),e);
				}
		} else {
			while (threads.get(this.getThreadLocalRequest().getSession().getId()) != null
					&& threads.get(this.getThreadLocalRequest().getSession().getId()).isAlive())
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					L.Error(e.getMessage(),e);
				}
		}

		// it ended, so get the sheet
		if (this.getThreadLocalRequest() == null) {
			if (sheets.get(userName).length > place)
				sheet = sheets.get(userName)[place];
		} else {
			if (sheets.get(this.getThreadLocalRequest().getSession().getId()).length > place)
				sheet = sheets.get(this.getThreadLocalRequest().getSession().getId())[place];
		}

		return sheet;

	}

	@Override
	public ArrayList<SimpleCell> getTree(String userName, int place) {

		SimpleSheet[] localsheets = new SimpleSheet[0];

		// just wait for the end of the thread
		if (this.getThreadLocalRequest() == null) {
			while (threads.get(userName).isAlive())
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					L.Error(e.getMessage(),e);
				}
		} else {
			while (threads.get(this.getThreadLocalRequest().getSession().getId()).isAlive())
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					L.Error(e.getMessage(),e);
				}
		}

		// it ended, so get the sheet
		if (this.getThreadLocalRequest() == null) {
			localsheets = sheets.get(userName);
		} else {
			localsheets = sheets.get(this.getThreadLocalRequest().getSession().getId());
		}

		// add the cells
		SimpleCell[][] cells = localsheets[place].getCells();

		ArrayList<SimpleCell> attrCells = new ArrayList<SimpleCell>();

		for (int row = 0; row < cells.length; row++) {

			for (int col = 0; col < cells[row].length; col++) {
				if (cells[row][col].isAttribute()) {

					ListIterator<ConnexComposant> it = localsheets[place].getConnexComps().listIterator();

					while (it.hasNext()) {
						ConnexComposant cc = it.next();
						if (cc.containsPoint(cells[row][col].getJxlCol(), cells[row][col].getJxlRow())) {
							cells[row][col].setCcStartX(cc.getStartX());
							cells[row][col].setCcStartY(cc.getStartY());
							break;
						}
					}

					cells[row][col].setEntityName(localsheets[place].getTitle());

					attrCells.add(cells[row][col]);
				}
			}
		}

		return attrCells;

	}

	public HashMap<String, Thread> getThreads() {
		return threads;
	}

	public void setThreads(HashMap<String, Thread> threads) {
		this.threads = threads;
	}

	@Override
	@Deprecated
	public String getXMLFile(String schemaId) {

		String xmlDoc = new String();

		System.out.println("Id : " + schemaId);

		if (schemaId != null && new Integer(schemaId).intValue() != -1) {

			// get by schema id
			MySQLDBConnector connector = new MySQLDBConnector();
			connector.connect();

			Statement stat = connector.getStat();

			try {
				stat.execute("SELECT xml FROM stored_schemas WHERE id = '" + new Integer(schemaId).intValue() + "';");
				if (stat.getResultSet().next())
					xmlDoc = stat.getResultSet().getString("xml");
			} catch (SQLException e) {
				L.Error(e.getMessage(),e);
			}

		}

		return xmlDoc;

	}

	@Override
	public void visualizeSpreadsheet(String filePath, String name) {

		L.Debug(this.getClass().getSimpleName(), "Upload file " + filePath, true);

		// now upload file using post method
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		HttpPost httppost = new HttpPost(this.exportUri + "/api/dspl/load");
		// HttpPost httppost = new
		// HttpPost("http://constraint.lirmm.fr:8320/api/dspl/load");
		// HttpPost httppost = new
		// HttpPost("http://admin-int.data-publica.com/api/api/dspl/load");
		// HttpPost httppost = new
		// HttpPost("http://otmedia.lirmm.fr:8320/api/dspl/load");

		File file = new File(filePath);

		// not dsplengine case
		if (!filePath.endsWith(".zip")) {
			if (file.exists())
				file.delete();

			try {
				// file
				FileOutputStream fos = new FileOutputStream(file);
				ZipOutputStream zos = new ZipOutputStream(fos);
				byte bytes[] = new byte[2048];

				File dir = new File(filePath.substring(0, filePath.lastIndexOf(".")));

				File[] files = dir.listFiles();

				for (int i = 0; i < files.length; i++) {
					FileInputStream fis = new FileInputStream(files[i].getAbsolutePath());
					BufferedInputStream bis = new BufferedInputStream(fis);

					zos.putNextEntry(new ZipEntry(files[i].getName()));

					int bytesRead;
					while ((bytesRead = bis.read(bytes)) != -1) {
						zos.write(bytes, 0, bytesRead);
					}
					zos.closeEntry();
					bis.close();
				}

				zos.flush();
				fos.flush();
				zos.close();
				fos.close();

			} catch (IOException e) {
				L.Error(e.getMessage(),e);
			}
		}

		MultipartEntity mpEntity = new MultipartEntity();
		ContentBody cbFile = new FileBody(file, "multipart/form-data");
		mpEntity.addPart("dspl", cbFile);
		try {
			mpEntity.addPart("reference", new StringBody(name));
			mpEntity.addPart("directLoading", new StringBody("true"));
			mpEntity.addPart("overwrite", new StringBody("true"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		httppost.setEntity(mpEntity);

		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		HttpEntity resEntity = response.getEntity();

		if (resEntity != null) {
			try {
				L.Debug(this, EntityUtils.toString(resEntity), true);
			} catch (ParseException e) {
				L.Error(e.getMessage(),e);
			} catch (IOException e) {
				L.Error(e.getMessage(),e);
			}
		}
		if (resEntity != null) {
			try {
				resEntity.consumeContent();
			} catch (IOException e) {
				L.Error(e.getMessage(),e);
			}
		}

		httpclient.getConnectionManager().shutdown();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			L.Error(e.getMessage(),e);
		}

		// clean
		if (file.exists())
			file.delete();
	}
}
