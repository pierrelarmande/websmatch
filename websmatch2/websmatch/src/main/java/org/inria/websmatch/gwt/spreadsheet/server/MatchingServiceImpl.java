package org.inria.websmatch.gwt.spreadsheet.server;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.RemoteEventServiceServlet;
import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingService;
import org.inria.websmatch.gwt.spreadsheet.client.listeners.MatchingProgressEvent;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.matchers.base.DocumentMatcher;
import org.inria.websmatch.matchers.mappings.AnnotatorService;
import org.inria.websmatch.matchers.mappings.CsvOboMapper;
import org.inria.websmatch.matchers.mappings.N3Annotator;
import org.inria.websmatch.matchers.mappings.OntoAlignmentProposal;
import org.inria.websmatch.utils.L;
import yam.system.Configs;
import yam.tools.WordNetHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchingServiceImpl extends RemoteEventServiceServlet implements MatchingService {

	/**
	 *
	 */
	private static final long serialVersionUID = 6823861244966936080L;

	private String baseFileStorageDir = "";

	public void init() {

		// init WordNet
	/*
	 * yam.system.Configs.WNTMP = "WNTemplate.xml";
	 * yam.system.Configs.WNPROP = "file_properties.xml";
	 * 
	 * try { WordNetHelper.getInstance().initializeWN(Configs.WNDIR,
	 * Configs.WNVER);
	 * WordNetHelper.getInstance().initializeIC(Configs.WNIC); } catch
	 * (Exception e) { L.Error(e.getMessage(),e); }
	 */
		L.Debug(this.getClass().getSimpleName(), "Current dir : " + System.getProperty("user.dir"), true);
		// init WordNet
		yam.system.Configs.WNTMP = System.getProperty("user.dir") + "/webapps/WebSmatch/WNTemplate.xml";
		yam.system.Configs.WNPROP = System.getProperty("user.dir") + "/webapps/WebSmatch/file_properties.xml";

		yam.system.Configs.WNDIR = System.getProperty("user.dir") + "/webapps/WebSmatch/WordNet/2.1/dict";

		try {
			WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
			WordNetHelper.getInstance().initializeIC(System.getProperty("user.dir") + "/webapps/WebSmatch/" + Configs.WNIC);
		} catch (Exception e) {
			L.Error(e.getMessage(), e);
		}

		baseFileStorageDir = getServletContext().getInitParameter("xlsStorageDir");
	}

	@Override
	public void matchDocuments(String docId1, String docId2, String dbName) {

		// get the 2 documents
		MongoDBConnector connector = MongoDBConnector.getInstance();

		String strDoc1 = connector.getEditedXML(docId1, dbName);
		String strDoc2 = connector.getEditedXML(docId2, dbName);
		//

		DocumentMatcher docMatch = new DocumentMatcher();
		docMatch.matchDocuments(strDoc1, strDoc2);

	}

	@Override
	public void completeMatching(final String dbName) {

		//
		final MatchingProgressEvent event = new MatchingProgressEvent();
		Domain dom = DomainFactory.getDomain(this.getThreadLocalRequest().getSession().getId());
		//
		final int[] cpt = new int[1];
		cpt[0] = 0;
		final int[] maxMatch = new int[1];
		maxMatch[0] = 0;

		Thread t = new Thread() {
			@Override
			public void run() {
				// load files and match them
				MongoDBConnector connector = MongoDBConnector.getInstance();

				List<SchemaData> list = connector.getSchemas(false, dbName);

				SchemaData[] schemas = list.toArray(new SchemaData[list.size()]);

				//
				maxMatch[0] = schemas.length;
				//

				Map<String, Boolean> used = new HashMap<>();
				cpt[0] = 0;

				for (int i = 0; i < schemas.length - 1; i++) {

					SchemaData s1 = schemas[i];

					for (int j = i + 1; j < schemas.length; j++) {

						SchemaData s2 = schemas[j];

						if (!s1.getId().equals(s2.getId()) && used.get(s1.getId() + s2.getId()) == null && used.get(s2.getId() + s1.getId()) == null) {
							String strDoc1 = connector.getEditedXML(s1.getId(), dbName);
							String strDoc2 = connector.getEditedXML(s2.getId(), dbName);

							DocumentMatcher dMatcher = new DocumentMatcher();
							if (strDoc1 != null && strDoc2 != null && !strDoc1.equals("") && !strDoc2.equals("")) {
								try {
									float dist = dMatcher.computeDistance(dMatcher.matchDocuments(strDoc1, strDoc2));

									// insert distance
									if (!Float.isNaN(dist) && dist > 0)
										connector.insertOrUpdateDistance(s1.getId(), s1.getSource(), s2.getId(), s2.getSource(), dist, "", dbName);
								} catch (Exception e) {
									L.Error(e.getMessage(), e);
								}
							}

							used.put(s1.getId() + s2.getId(), true);

			    /*
			     * if (cpt % 500 == 0) System.out.println(new Date()
			     * + " Cpt insert dist : " + cpt);
			     */

						}
					}
					cpt[0]++;
				}
			}
		};
		t.start();

		// event handling
		while (t.isAlive()) {
			try {
				Thread.sleep(500);
				if (maxMatch[0] > 0 && event.getMaxMatch() == 0)
					event.setMaxMatch(maxMatch[0]);
				event.setMatchCount(cpt[0]);
				event.setMsg("Matching " + (int) ((double) cpt[0] / (double) maxMatch[0] * 100.0) + "% complete.");
				addEvent(dom, event);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		// ended
		event.setMatchCount(0);
		event.setMaxMatch(0);
		event.setMsg("Processing terminated, matching up to date.");
		addEvent(dom, event);
		//
	}

	@Override
	public String matchOntoFiles(String file1, String file2, final boolean withDef, boolean generateFile) {

		//
		final MatchingProgressEvent event = new MatchingProgressEvent();
		Domain dom = DomainFactory.getDomain(this.getThreadLocalRequest().getSession().getId());
		//

		final OntoAlignmentProposal align = new OntoAlignmentProposal(baseFileStorageDir + File.separator + file1, baseFileStorageDir + File.separator + file2);
		final String[] results = new String[1];

		Thread t = new Thread() {
			@Override
			public void run() {
				results[0] = align.alginFiles(withDef);
			}
		};
		t.start();

		// event handling
		while (t.isAlive()) {
			try {
				Thread.sleep(1000);
				if (align.getMaxMatch() > 0 && event.getMaxMatch() == 0)
					event.setMaxMatch(align.getMaxMatch());
				event.setMatchCount(align.getEventCpt());
				event.setMsg("Matching " + (int) (((double) align.getEventCpt() / (double) align.getMaxMatch()) * 100.0) + "% complete.");
				addEvent(dom, event);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		// ended
		event.setMatchCount(0);
		event.setMaxMatch(0);
		event.setMsg("Processing terminated, matching up to date.");
		addEvent(dom, event);
		//

		if (generateFile) {
			File outputFile = new File("/tmp/" + file1.substring(0, file1.lastIndexOf('.')) + "_" + file2.substring(0, file2.lastIndexOf('.')) + ".csv");
			if (outputFile.exists())
				outputFile.delete();
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(outputFile));
				writer.write(results[0]);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				L.Error(e.getMessage(), e);
			}
			return outputFile.getAbsolutePath();
		} else
			return results[0];
	}

	@Override
	public String matchCSVOboFiles(String file1, String file2, final String annotator, final String score, final String ontoId, boolean generateFile, final int labelPlace,
								   final int descPlace, final int maxLevel) {

		//
		final MatchingProgressEvent event = new MatchingProgressEvent();
		Domain dom = DomainFactory.getDomain(this.getThreadLocalRequest().getSession().getId());
		//

		final CsvOboMapper mapper = new CsvOboMapper(baseFileStorageDir + File.separator + file1,
				                                     baseFileStorageDir + File.separator + file2);
		final AnnotatorService annotatorService = AnnotatorService.valueOf(annotator);
		final String[] results = new String[1];

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					results[0] = mapper.grammarAndInstancesCSVMapping(labelPlace, descPlace, annotatorService, ontoId, maxLevel, score);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();

		// event handling
		while (t.isAlive()) {
			try {
				Thread.sleep(1000);
				if (mapper.getMaxMatch() > 0 && event.getMaxMatch() == 0)
					event.setMaxMatch(mapper.getMaxMatch());
				event.setMatchCount(mapper.getEventCpt());
				event.setMsg("Matching " + (int) (((double) mapper.getEventCpt() / (double) mapper.getMaxMatch()) * 100.0) + "% complete.");
				addEvent(dom, event);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		// ended
		event.setMatchCount(0);
		event.setMaxMatch(0);
		event.setMsg("Processing terminated, matching up to date.");
		addEvent(dom, event);


		// write result to output file, or return it
		if (generateFile) {
			File outputFile;
			if (file2 == null || file2.equals(""))
				outputFile = new File("/tmp/" + file1.substring(0, file1.lastIndexOf('.')) + "_annotated.csv");
				//outputFile = new File("/tmp/" +  "_annotated.csv");
			else
				outputFile = new File("/tmp/" + file1.substring(0, file1.lastIndexOf('.')) + "_" + file2.substring(0, file2.lastIndexOf('.')) + ".csv");
			if (outputFile.exists())
				outputFile.delete();
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(outputFile));
				writer.write(results[0]);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				L.Error(e.getMessage(), e);
			}
			return outputFile.getAbsolutePath();
		} else
			return results[0];
	}

	@Override
	public String annotateN3WithObo(String n3File, String oboFile, final boolean withDef, final String annotator, final String ontoId, boolean generateFile) {

		//
		final MatchingProgressEvent event = new MatchingProgressEvent();
		Domain dom = DomainFactory.getDomain(this.getThreadLocalRequest().getSession().getId());
		//

		final N3Annotator align = new N3Annotator(baseFileStorageDir + File.separator + n3File, baseFileStorageDir + File.separator + oboFile);
		final List<String[]> results = new ArrayList<>();

		// string to replace + replacement in each res

		final AnnotatorService annotatorService = AnnotatorService.valueOf(annotator);
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					results.addAll(align.annotateN3File(withDef, annotatorService, ontoId));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();

		// event handling
		while (t.isAlive()) {
			try {
				Thread.sleep(1000);
				if (align.getMaxMatch() > 0 && event.getMaxMatch() == 0)
					event.setMaxMatch(align.getMaxMatch());
				event.setMatchCount(align.getEventCpt());
				event.setMsg("Matching " + (int) (((double) align.getEventCpt() / (double) align.getMaxMatch()) * 100.0) + "% complete.");
				addEvent(dom, event);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		// ended
		event.setMatchCount(0);
		event.setMaxMatch(0);
		event.setMsg("Processing terminated, matching up to date.");
		addEvent(dom, event);
		//

		if (generateFile) {
			BufferedReader br;
			File outputFile = new File("/tmp/" + n3File.substring(0, n3File.lastIndexOf('.')) + "_annotated.n3");
			if (outputFile.exists())
				outputFile.delete();
			BufferedWriter writer;

			try {
				br = new BufferedReader(new FileReader(baseFileStorageDir + File.separator + n3File));
				writer = new BufferedWriter(new FileWriter(outputFile));

				String currentLine;

				while ((currentLine = br.readLine()) != null) {
					for (String[] tmpStr : results) {
						if (currentLine.trim().contains(tmpStr[0])) {
							currentLine = currentLine + "\n\td2rq:property " + tmpStr[1] + ";";
							break;
						}
					}
					writer.write(currentLine + "\n");
				}
				writer.flush();
				writer.close();
			} catch (IOException e) {
				L.Error(e.getMessage(), e);
			}

			return outputFile.getAbsolutePath();
		} else {
			StringBuilder buff = new StringBuilder();
			for (String[] tmp : results) {
				buff.append(tmp[0]).append("|").append(tmp[1]);
			}
			return buff.toString();
		}
	}

	@Override
	public List<String[]> getOntologiesWithIds(String annotator){
		AnnotatorService annotatorService = AnnotatorService.valueOf(annotator);
		return annotatorService.getOntologiesWithIds();
	}

}
