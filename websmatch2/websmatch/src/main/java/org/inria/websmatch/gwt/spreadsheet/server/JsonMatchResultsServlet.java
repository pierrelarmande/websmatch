package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringEscapeUtils;
import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.utils.L;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.SchemaElementList;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

public class JsonMatchResultsServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 7144675584461631201L;

    private String limit = "10";

    public void init() {
	limit = getServletContext().getInitParameter("resultsLimit");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	// L.Debug(req.getQueryString());

	// req =
	// url?source_id=123&&target_id=456&&limit=20

	String source_id = req.getParameter("source_id");
	String target_id = req.getParameter("target_id");
	String current_limit = req.getParameter("limit");

	if (source_id == null && target_id == null) {
	    PrintWriter out = resp.getWriter();
	    out.println("Can't find required args.");
	    out.flush();
	} else if (source_id.equals(target_id)) {
	    PrintWriter out = resp.getWriter();
	    out.println("Source and destination can't be the same.");
	    out.flush();
	} else {
	    try {

		Integer sid = new Integer(source_id);
		Integer tid = new Integer(target_id);
		// Integer l = new Integer(current_limit);

		String local_limit;
		if (current_limit != null)
		    local_limit = current_limit;
		else
		    local_limit = limit;

		// we use publication_id if present or crawl_id
		MySQLDBConnector connector = new MySQLDBConnector();
		connector.connect();

		String sql = new String();
		String technic = "Proba_Prod";

		try {

		    // check if group 10 - rigaowl
		    sql = "SELECT id_group FROM stored_schemas WHERE id = '" + sid + "';";

		    ResultSet set = connector.getStat().executeQuery(sql);
		    boolean riga = false;

		    if (set.next()) {
			if (set.getInt("id_group") == 10 || set.getInt("id_group") == 11 || set.getInt("id_group") == 12){
			    technic = "SoftTFIDF";
			    riga = true;
			}else if(set.getInt("id_group") == 1){
			    riga = true;
			}
		    }

		    set.close();

		    // then get the ten most relevant matches
		    if (sid.intValue() < tid.intValue())
			sql = "SELECT * FROM match_results, elements ele1, elements ele2 WHERE id_schema1 =" + "'" + sid.intValue() + "' AND id_schema2 = '"
				+ tid.intValue() + "' AND ele1.id = id_element1 AND ele2.id = id_element2 AND " + technic + " > 0 ORDER BY expert DESC, "
				+ technic + " DESC LIMIT " + local_limit + ";";

		    else
			sql = "SELECT * FROM match_results, elements ele1, elements ele2 WHERE id_schema1 =" + "'" + tid.intValue() + "' AND id_schema2 = '"
				+ sid.intValue() + "' AND ele1.id = id_element1 AND ele2.id = id_element2 AND " + technic + " > 0 ORDER BY expert DESC, "
				+ technic + " DESC LIMIT " + local_limit + ";";

		    set = connector.getStat().executeQuery(sql);

		    resp.setContentType("application/xml;charset=UTF-8");
		    PrintWriter out = resp.getWriter();
		    out.println("<xml>");

		    // if riga get schemaElements
		    SchemaElementList sidList = null;
		    SchemaElementList tidList = null;

		    if(riga){
		    //if (sid.intValue() <= 98245 && sid.intValue() >= 95342 && tid.intValue() <= 98245 && tid.intValue() >= 95342) {
			SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

			try {
			    SchemaStoreObject sc = null;
			    try {
				sc = serviceLoc.getSchemaStore(new URL("http://constraint/SchemaStore/services/SchemaStore"));
			    } catch (MalformedURLException e1) {
				e1.printStackTrace();
			    }

			    if (sc != null) {
		
				    sidList = sc.getSchemaElements(new Integer(sid));
				    tidList = sc.getSchemaElements(new Integer(tid));
				
			    }

			} catch (ServiceException e) {
			    L.Error(e.getMessage(),e);
			} catch (RemoteException e) {
			    L.Error(e.getMessage(),e);
			}
		    }

		    while (set.next()) {

			double score = set.getDouble(technic);
			if (score > 1)
			    score = 1;

			if (sid.intValue() < tid.intValue()) {

			    // try to get parents
			    if(riga){
			    // if (sid.intValue() <= 98245 && sid.intValue() >= 95342 && tid.intValue() <= 98245 && tid.intValue() >= 95342) {

				out.print("\t<result source_id='" + sid.intValue() + "' source_element_id='" + set.getInt("ele1.id")
					+ "' source_element_name='" + StringEscapeUtils.escapeXml(set.getString("ele1.name"))+"'");
				
				Subtype[] sidAttr = sidList.getSubtypes();
				for (Subtype attr : sidAttr) {
				    if (attr.getChildID().intValue() == set.getInt("ele1.id")) {					
						out.print(" source_element_parent_id='" + attr.getParentID() + "' source_element_parent_name='");

					Entity[] sidEnt = sidList.getEntities();
					for (Entity ent : sidEnt) {
					    if (ent.getId().intValue() == attr.getParentID().intValue()) {
						out.print(StringEscapeUtils.escapeXml(ent.getName())+"'");
						break;
					    }
					}
					break;
				    }
				}

				out.print(" target_id='" + tid + "' target_element_id='" + set.getInt("ele2.id") + "' target_element_name='"
					+ StringEscapeUtils.escapeXml(set.getString("ele2.name"))+"'");
				
				Subtype[] tidAttr = tidList.getSubtypes();
				for (Subtype attr : tidAttr) {
				    if (attr.getChildID().intValue() == set.getInt("ele2.id")) {
					out.print(" target_element_parent_id='" + attr.getParentID()
						+ "' target_element_parent_name='");

					Entity[] tidEnt = tidList.getEntities();
					for (Entity ent : tidEnt) {
					    if (ent.getId().intValue() == attr.getParentID().intValue()) {
						out.print(StringEscapeUtils.escapeXml(ent.getName())+"'");
						break;
					    }
					}
					break;
				    }
				}
				out.println(" expert='" + set.getInt("expert") + "' score='" + score + "' />");
			    }

			    else
				out.println("\t<result source_id='" + sid.intValue() + "' source_element_id='" + set.getInt("ele1.id")
					+ "' source_element_name='" + StringEscapeUtils.escapeXml(set.getString("ele1.name")) + "' target_id='" + tid
					+ "' target_element_id='" + set.getInt("ele2.id") + "' target_element_name='"
					+ StringEscapeUtils.escapeXml(set.getString("ele2.name")) + "' expert='" + set.getInt("expert") + "' score='" + score
					+ "' />");
			} else {

			    // try to get parents
			    if(riga){
			    //if (sid.intValue() <= 98245 && sid.intValue() >= 95342 && tid.intValue() <= 98245 && tid.intValue() >= 95342) {

				out.print("\t<result source_id='" + sid.intValue() + "' source_element_id='" + set.getInt("ele2.id")
					+ "' source_element_name='" + StringEscapeUtils.escapeXml(set.getString("ele2.name"))
					+ "'");
				
				Subtype[] sidAttr = sidList.getSubtypes();
				for (Subtype attr : sidAttr) {
				    if (attr.getChildID().intValue() == set.getInt("ele2.id")) {
					out.print(" source_element_parent_id='" + attr.getParentID() + "' source_element_parent_name='");

					Entity[] sidEnt = sidList.getEntities();
					for (Entity ent : sidEnt) {
					    if (ent.getId().intValue() == attr.getParentID().intValue()) {
						out.print(StringEscapeUtils.escapeXml(ent.getName())+"'");
						break;
					    }
					}
					break;
				    }
				}

				out.print(" target_id='" + tid + "' target_element_id='" + set.getInt("ele1.id") + "' target_element_name='"
					+ StringEscapeUtils.escapeXml(set.getString("ele1.name")) + "'");
				
				Subtype[] tidAttr = tidList.getSubtypes();
				for (Subtype attr : tidAttr) {
				    if (attr.getChildID().intValue() == set.getInt("ele1.id")) {
					out.print(" target_element_parent_id='" + attr.getParentID()
						+ "' target_element_parent_name='");

					Entity[] tidEnt = tidList.getEntities();
					for (Entity ent : tidEnt) {
					    if (ent.getId().intValue() == attr.getParentID().intValue()) {
						out.print(StringEscapeUtils.escapeXml(ent.getName())+"'");
						break;
					    }
					}
					break;
				    }
				}
				out.println(" expert='" + set.getInt("expert") + "' score='" + score + "' />");
			    }

			    else
				out.println("\t<result source_id='" + sid.intValue() + "' source_element_id='" + set.getInt("ele2.id")
					+ "' source_element_name='" + StringEscapeUtils.escapeXml(set.getString("ele2.name")) + "' target_id='" + tid
					+ "' target_element_id='" + set.getInt("ele1.id") + "' target_element_name='"
					+ StringEscapeUtils.escapeXml(set.getString("ele1.name")) + "' expert='" + set.getInt("expert") + "' score='" + score
					+ "' />");
			}
		    }

		    out.println("</xml>");

		    connector.close();

		} catch (SQLException e) {
		    L.Error(e.getMessage(),e);
		    PrintWriter out = resp.getWriter();
		    out.println("Can't access to results database.");
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
