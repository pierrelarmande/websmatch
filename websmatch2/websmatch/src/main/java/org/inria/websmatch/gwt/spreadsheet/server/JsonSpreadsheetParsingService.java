package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.inria.websmatch.auth.DataPublicaAuth;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.exceptions.ParserException;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xls.SheetsSerializer;
import org.inria.websmatch.auth.DataPublicaAuth;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.exceptions.ParserException;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xls.SheetsSerializer;
import org.mitre.schemastore.model.Attribute;

public class JsonSpreadsheetParsingService extends HttpServlet {

    private boolean _DEBUG = false;
    private boolean _DATAPUBLICA_TEST = false;
    // private static boolean _DSPL_TEST = false;

    // to test it
    // xml answer
    // http://127.0.0.1:8888/rest/metadata?user_id=test&&doc_url=http://www.insee.fr/fr/ffc/figure/NATTEF06219.xls&&crawl_id=123&&publication_id=456
    // http://websmatch.inria.fr/WebSmatch/rest/metadata?user_id=test&&password=test&&doc_url=http://www.insee.fr/fr/ffc/figure/NATTEF06219.xls&&crawl_id=123&&publication_id=456
    // gwt usage
    // http://127.0.0.1:8888/rest/metadata?user_id=test&&doc_url=http://www.insee.fr/fr/ffc/figure/NATTEF06219.xls&&crawl_id=123&&publication_id=456&&post_url=http://127.0.0.1:8888/rest/metadata&&callback_url=http://www.google.fr
    // http://websmatch.inria.fr/WebSmatch/rest/metadata?user_id=test&&password=test&&doc_url=http://www.insee.fr/fr/ffc/figure/NATTEF06219.xls&&crawl_id=123&&publication_id=456&&post_url=http://websmatch.inria.fr&&callback_url=http://www.google.fr
    // http://127.0.0.1:8888/SpreadsheetViewer.html?gwt.codesvr=127.0.0.1:9997&&datapublica=true&&user_id=test&&fileName=NATTEF06219.xls&&callback_url=http://127.0.0.1:8888/rest/metadata

    /**
     * 
     */
    private static final long serialVersionUID = -6446171273568794461L;

    private String baseXLSDir;
    private String datapublicaUrl;
    private String storeService;

    // DSPL things
    public static String dsplDir = "/var/www/dspl";

    public void init() {
	baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");
	datapublicaUrl = getServletContext().getInitParameter("datapublicaUrl");
	storeService = getServletContext().getInitParameter("schemaStoreService");
	dsplDir = getServletContext().getInitParameter("dsplDir");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	if (_DEBUG)
	    System.out.println(req.getQueryString());

	// req =
	// url?user_id=toto&&password=toto&&doc_url=tutu&&crawl_id=bidule...

	String user_id = req.getParameter("user_id");
	String password = req.getParameter("password");
	String doc_url = req.getParameter("doc_url");
	String crawl_id = req.getParameter("crawl_id");
	String publication_id = req.getParameter("publication_id");
	String callback_url = req.getParameter("callback_url");
	String post_url = req.getParameter("post_url");
	String withData = req.getParameter("with_data");

	// TODO verify login
	LoginServiceImpl loginService = new LoginServiceImpl();
	String resLog = loginService.login(user_id, password);
	if (resLog == null) {
	    PrintWriter out = resp.getWriter();
	    out.println("Login error.");
	    out.flush();
	}

	else {
	    // download file
	    InputStream input = null;
	    FileOutputStream writeFile = null;
	    String fileName = null;

	    try {
		URL url = new URL(doc_url.replaceAll("\\s", "%20"));
		URLConnection connection = url.openConnection();

		/*
		 * int fileLength = connection.getContentLength();
		 * System.out.println(fileLength);
		 * 
		 * if (fileLength == -1) { PrintWriter out = resp.getWriter();
		 * out.println("Invalid URL or file."); out.flush();
		 * System.out.println("Invalid URL or file."); return; }
		 */

		input = connection.getInputStream();
		fileName = doc_url.substring(doc_url.lastIndexOf('/') + 1);
		
		writeFile = new FileOutputStream(baseXLSDir + File.separator + fileName);

		byte[] buffer = new byte[1024];
		int read;

		while ((read = input.read(buffer)) > -1) {
		    writeFile.write(buffer, 0, read);
		}
		writeFile.flush();

	    } catch (IOException e) {
		PrintWriter out = resp.getWriter();
		out.println("Invalid URL or file.");
		out.flush();
		if (_DEBUG)
		    System.out.println("Error while trying to download the file.");
		//L.Error(e.getMessage(),e);
		return;
	    } finally {
		try {
		    if(writeFile != null) writeFile.close();
		    if(input != null) input.close();
		} catch (IOException e) {
		    L.Error(e.getMessage(), e);
		}
	    }

	    if (post_url == null) {

		// resp.sendRedirect(datapublicaUrl+"&&user_id="+user_id+"&&fileName="+fileName);

		// parse spreadsheet
		SpreadsheetParsingServiceImpl parsing = new SpreadsheetParsingServiceImpl();

		SimpleSheet[] sheets = new SimpleSheet[0];

		try {
		    parsing.setStoreService(storeService);
		    parsing.initWordnet();
		    parsing.setBaseXLSDir(baseXLSDir);
		    sheets = parsing.parseSpreadsheet(user_id, fileName, true, null);

		    // wait for end of thread
		    while (parsing.getThreads().get(user_id) != null && parsing.getThreads().get(user_id).isAlive())
			try {
			    Thread.sleep(100);
			} catch (InterruptedException e) {
			    L.Error(e.getMessage(),e);
			}

		    // if (_DSPL_TEST) new
		    // DSPLExport(fileName).csvGenerator(sheets);

		    // store without ML
		    if (!_DATAPUBLICA_TEST) {

			Integer crawl_id_int = null;
			Integer publication_id_int = null;

			try {
			    crawl_id_int = Integer.parseInt(crawl_id);
			} catch (NumberFormatException fe) {

			}

			try {
			    publication_id_int = Integer.parseInt(publication_id);
			} catch (NumberFormatException fe) {

			}

			parsing.storeSchema(fileName.substring(0, fileName.lastIndexOf(".")), fileName, user_id, "", false, crawl_id_int, publication_id_int, null);
		    }
		} catch (ParserException e) {
		    L.Error(e.getMessage(),e);
		}

		resp.setContentType("application/xml;charset=UTF-8");

		String outputString = new String();

		PrintWriter out = resp.getWriter();

		outputString += "<xml>\n";
		outputString += "\t<user_id>" + user_id + "</user_id>\n";
		outputString += "\t<doc_url>" + doc_url + "</doc_url>\n";
		outputString += "\t<crawl_id>" + crawl_id + "</crawl_id>\n";
		outputString += "\t<publication_id>" + publication_id + "</publication_id>\n";

		// We convert to XML
		SheetsSerializer serializer = new SheetsSerializer(sheets);

		if(withData != null && withData.equals("true")) outputString += serializer.toXML(true) + "\n";
		else outputString += serializer.toXML(false) + "\n";

		outputString += "</xml>\n";

		out.print(outputString);
		out.flush();

		// ok had to insert it into schemastore
		/**
		 * TODO remove this if generalized
		 * 
		 */

		try {
		    Integer crawl_id_int = Integer.parseInt(crawl_id);

		    MySQLDBConnector dbConnector = new MySQLDBConnector();
		    dbConnector.connect();

		    try {
			Statement stat = dbConnector.getStat();
			stat.execute("SELECT id FROM datapublica_map WHERE crawl_id = '" + crawl_id_int.intValue() + "'");

			if (stat.getResultSet() != null) {

			    ResultSet set = stat.getResultSet();
			    int schemaId = set.getInt("id");

			    stat.executeUpdate("UPDATE stored_schemas SET original_xml = '" + outputString + "' WHERE id ='" + schemaId + "'");

			}

		    } catch (SQLException e) {
			// TODO Auto-generated catch block
			L.Error(e.getMessage(),e);
		    }

		} catch (NumberFormatException fe) {

		}
		//
	    }

	    // if there is a callback_url, send the xml with post method to this
	    // url
	    // after editing, so pass to GWT
	    else {

		resp.sendRedirect(datapublicaUrl + "&&doc_url=" + doc_url + "&&user_id=" + user_id + "&&fileName=" + fileName.replaceAll("\\s", "%20") + "&&crawl_id=" + crawl_id
			+ "&&publication_id=" + publication_id + "&&callback_url=" + callback_url + "&&post_url=" + post_url);

	    }
	}
    }

    public static void createAndSendPostRequest(HashMap<String, String> data, List<Attribute> attributeList, SimpleSheet[] sheets, boolean withData) {

	// for datapublica
	// String callback_url = data.get("callback_url");
	String doc_url = data.get("doc_url");
	String crawl_id = data.get("crawl_id");
	String publication_id = data.get("publication_id");
	String user_id = data.get("user_id");
	String post_url = data.get("post_url");

	System.out.println("Post_url : " + post_url);

	// publish it on datapublica
	if (post_url != null) {

	    String doc = "<xml>\n\t<user_id>" + user_id + "</user_id>\n\t<doc_url>" + doc_url + "</doc_url>\n\t<crawl_id>" + crawl_id
		    + "</crawl_id>\n\t<publication_id>" + publication_id + "</publication_id>\n";

	    // ok we had the tables and datas for detected attributes
	    SheetsSerializer serializer = new SheetsSerializer(sheets);

	    doc += serializer.toXML(withData);

	    doc += "</xml>\n";

	    System.out.println(doc);

	    JsonSpreadsheetParsingService.sendPostRequest(post_url, doc);
	    
	    // generate DSPL
	    // if (_DSPL_TEST) new
	    // DSPLExport(sheets[0].getFilename()).csvGenerator(sheets);

	    // save it
	    // ok had to insert it into schemastore
	    /**
	     * TODO remove this if generalized
	     * 
	     */

	    try {
		Integer crawl_id_int = Integer.parseInt(crawl_id);

		MySQLDBConnector dbConnector = new MySQLDBConnector();
		dbConnector.connect();

		try {
		    Statement stat = dbConnector.getStat();
		    stat.execute("SELECT id FROM datapublica_map WHERE crawl_id = '" + crawl_id_int.intValue() + "'");

		    if (stat.getResultSet() != null) {

			ResultSet set = stat.getResultSet();

			if (set.next()) {
			    int schemaId = set.getInt("id");
			    stat.executeUpdate("UPDATE stored_schemas SET xml = '" + doc + "' WHERE id ='" + schemaId + "'");
			}

		    }

		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    L.Error(e.getMessage(),e);
		}

	    } catch (NumberFormatException fe) {

	    }
	    //
	}
    }

    public static String getXMLDoc(HashMap<String, String> data, SimpleSheet[] sheets, boolean withData) {
	// for datapublica
	// String callback_url = data.get("callback_url");
	String doc_url = new String();
	String crawl_id = new String();
	String publication_id = new String();
	String user_id = new String();

	if (data != null) {
	    doc_url = data.get("doc_url");
	    if(data.get("crawl_id")!=null) crawl_id = data.get("crawl_id");
	    if(data.get("publication_id")!=null) publication_id = data.get("publication_id");
	    if(data.get("user_id")!=null) user_id = data.get("user_id");
	}

	String doc = "<xml>\n\t<user_id>" + user_id + "</user_id>\n\t<doc_url>" + doc_url + "</doc_url>\n\t<crawl_id>" + crawl_id
		+ "</crawl_id>\n\t<publication_id>" + publication_id + "</publication_id>\n";

	// ok we had the tables and datas for detected attributes
	SheetsSerializer serializer = new SheetsSerializer(sheets);

	doc += serializer.toXML(withData);

	doc += "</xml>\n";

	return doc;
    }

    private static void sendPostRequest(String post_url, String doc) {

	// Build parameter string
	String data = doc;
	try {

	    // Auth
	    Authenticator.setDefault(new DataPublicaAuth());

	    // Send the request
	    URL url = new URL(post_url);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);

	    //
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type", "application/xml;charset=UTF-8");
	    //

	    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

	    // System.out.println("Sending : " + data);

	    // write parameters
	    writer.write(data);
	    writer.flush();

	    // Get the response
	    StringBuffer answer = new StringBuffer();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;
	    while ((line = reader.readLine()) != null) {
		answer.append(line);
	    }
	    writer.close();
	    reader.close();

	    // Output the response
	    // System.out.println(answer.toString());

	} catch (MalformedURLException ex) {
	    ex.printStackTrace();
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

}
