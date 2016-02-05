package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.inria.websmatch.dspl.DSPLExport;
import org.inria.websmatch.dspl.integration.Integration;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.dspl.DSPLExport;
import org.inria.websmatch.dspl.integration.Integration;
import org.inria.websmatch.utils.L;

public class JsonIntegrationServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -2926822649685646175L;
    private String baseXLSDir = new String();

    public void init() {
	baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	String sid1 = null;
	String sid2 = null;
	String name = "default" + System.currentTimeMillis();
	String asDSPL = "false";

	try {
	    sid1 = req.getParameter("schema_id1");
	    sid2 = req.getParameter("schema_id2");
	    asDSPL = req.getParameter("asDSPL");
	    if(req.getParameter("name") != null) name = req.getParameter("name");
	    
	    if (sid1 == null || sid1 == null) {
		PrintWriter out = resp.getWriter();
		out.println("Can't find required args.");
		out.flush();
	    } else if (sid1.equals(sid2)) {
		PrintWriter out = resp.getWriter();
		out.println("Source and destination can't be the same.");
		out.flush();
	    } else {
		// all is ok get the integration result and output (xml level
		// first) and then convert to DSPL
		Integration integration = new Integration(sid1, sid2,req.getParameter("user"));
		String document = integration.getIntegratedFile();

		if (asDSPL.equals("false")) {
		    resp.setContentType("application/xml;charset=UTF-8");
		    PrintWriter out = resp.getWriter();
		    out.println(document);
		    out.flush();
		} else {

		    if (JsonSpreadsheetParsingService.dsplDir == null)
			JsonSpreadsheetParsingService.dsplDir = getServletContext().getInitParameter("dsplDir");

		    File file = new File(baseXLSDir+ File.separator+"generatedXML"+File.separator+name + ".xml");
		    try {
			if (file.exists())
			    file.delete();
			file.createNewFile();
			FileWriter output = new FileWriter(file);
			BufferedWriter writer = new BufferedWriter(output);
			writer.write(document);
			writer.flush();
			writer.close();

			DSPLExport exporter = new DSPLExport(file.getName(), name, name, "fr");
			exporter.dsplGenerate(document);

			// zip
			String zipToDownload = exporter.zipFiles();

			// file
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			byte bytes[] = new byte[2048];

			File dir = new File(zipToDownload.substring(0, zipToDownload.lastIndexOf(".")));

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
			baos.flush();
			zos.close();
			baos.close();

			byte[] zip = baos.toByteArray();

			ServletOutputStream sos = resp.getOutputStream();
			resp.setContentType("application/zip");
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + zipToDownload.substring(zipToDownload.lastIndexOf(File.separator))
				+ "\"");

			sos.write(zip);
			sos.flush();

		    } catch (IOException e) {
			L.Error(e.getMessage(), e);
		    }
		}
	    }

	} catch (NumberFormatException nfe) {

	    PrintWriter out = resp.getWriter();
	    out.println("Bad ids.");
	    out.flush();

	}
    }
}
