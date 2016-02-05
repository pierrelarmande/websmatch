package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.utils.L;

public class JsonRecommandationServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -8413471478480118458L;
    private boolean _DEBUG = false;

    private String limit = "10";

    public void init() {
	limit = getServletContext().getInitParameter("recommandationLimit");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	System.out.println(req.getQueryString());

	// req =
	// url?crawl_id=123&&publication_id=456&&limit=20

	String crawl_id = req.getParameter("crawl_id");
	String publication_id = req.getParameter("publication_id");
	String current_limit = req.getParameter("limit");

	if (crawl_id == null && publication_id == null) {
	    PrintWriter out = resp.getWriter();
	    out.println("Can't find required args.");
	    out.flush();
	}

	else {
	    try {
		new Integer(crawl_id);
		// Integer pid = new Integer(publication_id);
		if(current_limit != null) new Integer(current_limit);

		String local_limit;
		if (current_limit != null)
		    local_limit = current_limit;
		else
		    local_limit = limit;

		// we use publication_id if present or crawl_id
		MySQLDBConnector connector = new MySQLDBConnector();
		connector.connect();

		String sql = new String();

		if (crawl_id != null) {
		    // we get the schemastore id of the document
		    sql = "SELECT id FROM datapublica_map WHERE crawl_id ='" + crawl_id + "';";
		}

		try {
		    ResultSet set = connector.getStat().executeQuery(sql);
		    if (set.next()) {
			// get the id
			Integer storeId = set.getInt("id");

			// close
			set.close();

			// then get the ten most relevant documents
			sql = "SELECT * FROM schemas_distance WHERE (id_schema1 = '" + storeId + "' OR id_schema2 = '" + storeId
				+ "') AND distance > '0.0' ORDER BY distance DESC LIMIT " + local_limit + ";";

			set = connector.getStat().executeQuery(sql);

			resp.setContentType("application/xml;charset=UTF-8");
			PrintWriter out = resp.getWriter();
			out.println("<xml>");

			while (set.next()) {
			    if (_DEBUG) {
				out.println("\t<recommandation id_schema1='" + set.getInt("id_schema1") + "' id_schema2='" + set.getInt("id_schema2")
					+ "' distance='" + set.getDouble("distance") + "' />");
			    } else {
				// ok, see if we have a crawl_id for the doc
				Integer schema_id1 = set.getInt("id_schema1");
				Integer schema_id2 = set.getInt("id_schema2");

				Integer target_crawl_id = null;
				Integer target_publication_id = null;

				Integer target_id = null;

				if (schema_id1.intValue() == storeId.intValue()) {
				    target_id = schema_id2.intValue();
				} else {
				    target_id = schema_id1.intValue();
				}

				//
				MySQLDBConnector secondConnector = new MySQLDBConnector();
				secondConnector.connect();
				ResultSet dp_map_set = secondConnector.getStat().executeQuery(
					"SELECT crawl_id, publication_id FROM datapublica_map WHERE id = '" + target_id.intValue() + "';");
				if (dp_map_set.next()) {
				    if (dp_map_set.getInt("crawl_id") != 0)
					target_crawl_id = dp_map_set.getInt("crawl_id");
				    if (dp_map_set.getInt("publication_id") != 0)
					target_publication_id = dp_map_set.getInt("publication_id");
				}
				dp_map_set.close();
				secondConnector.close();
				//

				if (target_crawl_id != null)// &&
							    // target_publication_id
							    // != null)
				    out.println("\t<recommandation source_crawl_id='" + crawl_id + "' source_publication_id='" + publication_id
					    + "' target_crawl_id='" + target_crawl_id + "' target_publication_id='" + target_publication_id + "' distance='"
					    + set.getDouble("distance") + "' />");
			    }
			}

			out.println("</xml>");

			connector.close();

		    } else {
			PrintWriter out = resp.getWriter();
			out.println("Document not found.");
			out.flush();
			connector.close();
		    }
		} catch (SQLException e) {
		    L.Error(e.getMessage(),e);
		    PrintWriter out = resp.getWriter();
		    out.println("Can't access to recommandation database.");
		    out.flush();
		    connector.close();
		}
	    } catch (NumberFormatException nfe) {
		L.Error(nfe.getMessage(),nfe);
		PrintWriter out = resp.getWriter();
		out.println("Bad arguments.");
		out.flush();
	    }
	}
    }
}
