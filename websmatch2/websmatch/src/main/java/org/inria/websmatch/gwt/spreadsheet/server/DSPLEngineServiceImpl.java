package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.inria.websmatch.gwt.spreadsheet.client.DSPLEngineService;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.L;

import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.L;
import yam.system.Configs;
import yam.tools.WordNetHelper;
import de.novanic.eventservice.service.RemoteEventServiceServlet;

public class DSPLEngineServiceImpl extends RemoteEventServiceServlet implements DSPLEngineService {

    /**
     * 
     */
    private static final long serialVersionUID = 6491123553491374660L;
    private String baseXLSDir;
    private HashMap<String, SimpleSheet[]> sheets = new HashMap<String, SimpleSheet[]>();

    public void init() {
	baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");

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
    }

    @Override
    public String createXMLFile(HashMap<String, String> data, SimpleSheet[] editedSheets, String ref, String dataSetName, String dataSetDescription,
	    String editorSetName, String editorSetDescription, String bidimValueName, boolean getZip) {

	String filePath = new String();

	// List<Attribute> attributes = this.updateSheets(data, editedSheets);
	sheets.put(this.getThreadLocalRequest().getSession().getId(), editedSheets);
	// check if dir exists
	File dir = new File(this.baseXLSDir + File.separator + "generatedXML");
	if (!dir.exists())
	    dir.mkdir();
	// String xmlFileName = editedSheets[0].getFilename().substring(0,
	// editedSheets[0].getFilename().lastIndexOf("."))+ "_DSPLE" + ".xml";
	String xmlFileName = ref + "_DSPLE.xml";
	// ok create and fill the file
	File outputFile = new File(this.baseXLSDir + File.separator + "generatedXML" + File.separator + xmlFileName);
	if (outputFile.exists())
	    outputFile.delete();
	L.Debug(this.getClass().getSimpleName(), "Output " + outputFile.getAbsolutePath(), true);
	try {
	    if (outputFile.createNewFile()) {
		// ok continue
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		writer.write(JsonDSPLEngineService.getXMLDoc(data, sheets.get(this.getThreadLocalRequest().getSession().getId()), ref, dataSetName,
			dataSetDescription, editorSetName, editorSetDescription, bidimValueName));
		writer.close();
		if (!getZip)
		    return outputFile.getAbsolutePath();

	    }
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}

	if (getZip) {
	    // generate zip file and return dspl zip file path
	    try {
		L.Debug(this, "Executing java -cp " + this.getServletContext().getRealPath("/") + "WEB-INF/lib" + " -jar "
			+ this.getServletContext().getRealPath("/") + "WEB-INF/lib/dspl-builder.jar " + outputFile.getAbsolutePath(), true);
		final Process res = Runtime.getRuntime().exec(
			"java -cp " + this.getServletContext().getRealPath("/") + "WEB-INF/lib" + " -jar " + this.getServletContext().getRealPath("/")
				+ "WEB-INF/lib/dspl-builder.jar " + outputFile.getAbsolutePath(), new String[]{"JRE_HOME=/usr/lib/jvm/java-8-oracle","LANG=fr_FR.utf8"}, new File("/tmp"));

		//
		new Thread() {
		    public void run() {
			try {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(res.getInputStream()));
			    String line = "";
			    try {
				while ((line = reader.readLine()) != null) {
				    L.Debug(this, line, true);
				}
			    } finally {
				reader.close();
			    }
			} catch (IOException ioe) {
			    L.Error(ioe.getMessage(),ioe);
			}
		    }
		}.start();

		//
		new Thread() {
		    public void run() {
			try {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(res.getErrorStream()));
			    String line = "";
			    try {
				while ((line = reader.readLine()) != null) {
				    L.Debug(this, line, true);
				}
			    } finally {
				reader.close();
			    }
			} catch (IOException ioe) {
			    L.Error(ioe.getMessage(),ioe);
			}
		    }
		}.start();

		try {
		    res.waitFor();
		} catch (InterruptedException e) {
		    L.Error(e.getMessage(),e);
		}

		try {
		    Thread.sleep(3000);
		} catch (InterruptedException e) {
		    L.Error(e.getMessage(),e);
		}

		if (res.exitValue() == 0)
		    return "/tmp/" + ref + ".zip";

	    } catch (IOException e) {
		L.Error(e.getMessage(),e);
	    }
	}

	return filePath;
    }

}
