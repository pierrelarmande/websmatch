package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.inria.websmatch.auth.DataPublicaAuth;
import org.inria.websmatch.auth.DataPublicaAuth;

public class VisualisationAccessServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -3597759976635979106L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	String post_url = "http://admin-int.data-publica.com/api/visualizator.html";

	// Build parameter string
	String publicRef = req.getParameter("publicationReference");
	String api = req.getParameter("apiBaseURL");

	try {

	    // Auth
	    Authenticator.setDefault(new DataPublicaAuth());

	    // Send the request
	    URL url = new URL(post_url);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);

	    //
	    conn.setRequestMethod("GET");
	    conn.setRequestProperty("publicationReference", publicRef);
	    conn.setRequestProperty("apiBaseURL", api);
	    //

	    // response for GWT
	    resp.setContentType("text/html");

	    OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());

	    // Get the response
	    StringBuffer answer = new StringBuffer();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;
	    while ((line = reader.readLine()) != null) {
		answer.append(line);
		writer.append(line);
	    }
	    writer.flush();
	    writer.close();
	    reader.close();

	    // Output the response
	    System.out.println(answer.toString());

	} catch (MalformedURLException ex) {
	    ex.printStackTrace();
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

}
