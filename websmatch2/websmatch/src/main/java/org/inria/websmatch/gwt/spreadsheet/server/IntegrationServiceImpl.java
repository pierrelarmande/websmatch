package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.inria.websmatch.dspl.DSPLExport;
import org.inria.websmatch.dspl.integration.Integration;
import org.inria.websmatch.gwt.spreadsheet.client.IntegrationService;
import org.inria.websmatch.utils.L;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.inria.websmatch.dspl.DSPLExport;
import org.inria.websmatch.gwt.spreadsheet.client.IntegrationService;
import org.inria.websmatch.utils.L;

public class IntegrationServiceImpl extends RemoteServiceServlet implements IntegrationService {

    /**
     * 
     */
    private static final long serialVersionUID = 3120787997696192617L;

    private String baseXLSDir = new String();
    // private String exportUri = "http://websmatch.lirmm.fr/dataprovider";
    private String exportUri = "http://constraint.lirmm.fr:8320";
    
    public void init() {
	baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");
	exportUri = getServletContext().getInitParameter("dataProviderUri");
    }

    @Override
    public String getIntegratedDSPLFile(String id1, String id2, String name, String dbName) {

	String filePath = new String();

	// all is ok get the integration result and output (xml level
	// first) and then convert to DSPL
	Integration integration = new Integration(id1, id2, dbName);
	String document = integration.getIntegratedFile();

	if (JsonSpreadsheetParsingService.dsplDir == null)
	    JsonSpreadsheetParsingService.dsplDir = getServletContext().getInitParameter("dsplDir");

	File file = new File(baseXLSDir + File.separator + "generatedXML" + File.separator + name + ".xml");
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

	    filePath = zipToDownload;

	} catch (IOException e) {
	    L.Error(e.getMessage(), e);
	}

	return filePath;
    }

    @Override
    public void integrateAndPublishDSPLFile(String id1, String id2, String name, String dbName) {
	
	String filePath = this.getIntegratedDSPLFile(id1, id2, name, dbName);
	
	L.Debug(this.getClass().getSimpleName(),"Upload file "+filePath,true);

	// now upload file using post method
	HttpClient httpclient = new DefaultHttpClient();
	httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	
	HttpPost httppost = new HttpPost(this.exportUri+"/api/dspl/load");

	// HttpPost httppost = new HttpPost("http://constraint.lirmm.fr:8320/api/dspl/load");
	// HttpPost httppost = new HttpPost("http://otmedia:8320/api/dspl/load");
	// HttpPost httppost = new HttpPost("http://admin-int.data-publica.com/api/api/dspl/load");
	File file = new File(filePath);
	
	if(file.exists()) file.delete();
	
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
	
	MultipartEntity mpEntity = new MultipartEntity();
	ContentBody cbFile = new FileBody(file, "multipart/form-data");
	mpEntity.addPart("dspl", cbFile);
	try {
	    mpEntity.addPart("reference", new StringBody(name));
	} catch (UnsupportedEncodingException e2) {
	    e2.printStackTrace();
	}

	httppost.setEntity(mpEntity);
	System.out.println("executing request " + httppost.getRequestLine());
	HttpResponse response = null;
	try {
	    response = httpclient.execute(httppost);
	} catch (ClientProtocolException e1) {
	    e1.printStackTrace();
	} catch (IOException e1) {
	    e1.printStackTrace();
	}
	HttpEntity resEntity = response.getEntity();

	System.out.println(response.getStatusLine());
	if (resEntity != null) {
	    try {
		System.out.println(EntityUtils.toString(resEntity));
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
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}
    }
}
