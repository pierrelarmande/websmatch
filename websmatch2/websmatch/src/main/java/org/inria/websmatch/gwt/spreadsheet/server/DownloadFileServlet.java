package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DownloadFileServlet extends HttpServlet implements Servlet {

    /**
     * 
     */
    private static final long serialVersionUID = 445429772964154180L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	String fileName = req.getParameter("filename");

	File dl = new File(fileName);
	
	if(fileName.endsWith(".csv")){
	    BufferedReader reader = new BufferedReader(new FileReader(dl));

	    resp.setContentType("text/csv;charset=UTF-8");
	    resp.setHeader("Content-disposition", "attachment; filename=" + fileName.substring(fileName.lastIndexOf(File.separator)));

	    ServletOutputStream outputStream = resp.getOutputStream();

	    String toRead = null;

	    while ((toRead = reader.readLine()) != null) {
		outputStream.write(toRead.getBytes("UTF-8"));
		outputStream.println();
	    }
	    outputStream.close();
	}
	
	else if(fileName.endsWith(".n3")){
	    BufferedReader reader = new BufferedReader(new FileReader(dl));

	    resp.setContentType("text/n3;charset=UTF-8");
	    resp.setHeader("Content-disposition", "attachment; filename=" + fileName.substring(fileName.lastIndexOf(File.separator)));

	    ServletOutputStream outputStream = resp.getOutputStream();

	    String toRead = null;

	    while ((toRead = reader.readLine()) != null) {
		outputStream.write(toRead.getBytes("UTF-8"));
		outputStream.println();
	    }
	    outputStream.close();
	}

	else if (fileName.endsWith(".xml")) {
	    BufferedReader reader = new BufferedReader(new FileReader(dl));

	    resp.setContentType("application/xml;charset=UTF-8");
	    resp.setHeader("Content-disposition", "attachment; filename=" + fileName.substring(fileName.lastIndexOf(File.separator)));

	    ServletOutputStream outputStream = resp.getOutputStream();

	    String toRead = null;

	    while ((toRead = reader.readLine()) != null) {
		outputStream.write(toRead.getBytes("UTF-8"));
	    }
	    outputStream.close();
	} else if (fileName.endsWith(".zip")){

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ZipOutputStream zos = new ZipOutputStream(baos);
	    byte bytes[] = new byte[2048];
    
	    File dir = new File(fileName.substring(0, fileName.lastIndexOf(".")));

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
	    resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.substring(fileName.lastIndexOf(File.separator)) + "\"");

	    sos.write(zip);
	    sos.flush();
	}
    }
}
