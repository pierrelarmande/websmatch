package org.inria.websmatch.gwt.spreadsheet.server;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.inria.websmatch.gwt.spreadsheet.client.models.Node;

public class JsonClusterServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 7211576164710844119L;
    private boolean _DEBUG = false;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	if(_DEBUG) System.out.println(req.getQueryString());

	String crawl_id = req.getParameter("crawl_id");
	// String publication_id = req.getParameter("publication_id");
	// not for use by datapublica, override crawl_id and publication_id
	String schema_id = req.getParameter("schema_id");
	//
	String width = req.getParameter("width");
	String height = req.getParameter("height");
	String color = req.getParameter("color");
	String callback_url = req.getParameter("callback_url");
	// override crawl_id/publication_id and schema_id
	String global = req.getParameter("global");
	String test = req.getParameter("test");
	
	if(callback_url != null && !callback_url.endsWith("/")) callback_url += "/";

	if ((crawl_id == null || schema_id == null) && (width == null || height == null || color == null)) {
	    PrintWriter out = resp.getWriter();
	    out.println("Can't find required args.");
	    out.flush();
	}

	else {
	    ClusteringServiceImpl clusterService = new ClusteringServiceImpl();

	    try {
		Integer w = new Integer(width);
		Integer h = new Integer(height);

		// all clusters
		Map<Map<Node, double[]>, Map<Node, Integer[]>> totalClusterNodes;
		if (schema_id != null)
		    totalClusterNodes = clusterService.getClusterNodes(w, h, false,"datapublica");
		else
		    totalClusterNodes = clusterService.getClusterNodes(w, h, true,"datapublica");
		// the one we are interested in
		Map<Map<Node, double[]>, Map<Node, Integer[]>> clusterNodes = new HashMap<Map<Node, double[]>, Map<Node, Integer[]>>();
		Map<Node, double[]> nodes = new HashMap<Node, double[]>();
		Map<Node, Integer[]> clusters = new HashMap<Node, Integer[]>();
		Integer clusterVal = null;
		//

		resp.setContentType("text/html;charset=UTF-8");
		PrintWriter out = resp.getWriter();

		if (_DEBUG || (test != null && test.equals("true")))
		    out.print("<HTML><BODY>");

		out.print("<canvas border=\"0\" width=\"" + w.intValue() + "\" height=\"" + h.intValue()
			+ "\" id=\"clusterCanvas\">Please update your web browser.</canvas>" + "<script type=\"text/javascript\">"
			+ "var canvas=document.getElementById('clusterCanvas');" + "var ctx=canvas.getContext('2d');");

		out.print("ctx.globalAlpha=0.5;");
		out.print("ctx.globalCompositeOperation='source-over';");

		// try to add clickable elements
		out.print("canvas.addEventListener('mousedown', myClick, false);");
		out.print("var elementList = new Array();");

		//
		// now we get the good cluster and scale/resize to fit in the
		// width/height
		if (schema_id == null) {
		    for (Map<Node, double[]> nodeToPosition : totalClusterNodes.keySet()) {
			// search to see if this is the good cluster
			for (Node node : nodeToPosition.keySet()) {
			    if (node.name.indexOf(" (Crawl_id : " + crawl_id + ")") != -1) {
				// if(node.name.indexOf(" (SchemaId : 52613)")
				// != -1){
				// this is the good cluster
				clusterVal = totalClusterNodes.get(nodeToPosition).get(node)[0];
				break;
			    }
			}
		    }
		} else {
		    for (Map<Node, double[]> nodeToPosition : totalClusterNodes.keySet()) {
			// search to see if this is the good cluster
			for (Node node : nodeToPosition.keySet()) {
			    // if(node.name.indexOf(" (Crawl_id : "+crawl_id+")")
			    // != -1){
			    if (node.name.indexOf(" (SchemaId : " + schema_id + ")") != -1) {
				// this is the good cluster
				clusterVal = totalClusterNodes.get(nodeToPosition).get(node)[0];
				break;
			    }
			}
		    }
		}
		// ok construct the structures now
		for (Map<Node, double[]> nodeToPosition : totalClusterNodes.keySet()) {
		    for (Node node : nodeToPosition.keySet()) {
			if (totalClusterNodes.get(nodeToPosition).get(node)[0].intValue() == clusterVal.intValue()) {
			    nodes.put(node, nodeToPosition.get(node));
			    clusters.put(node, totalClusterNodes.get(nodeToPosition).get(node));
			}
		    }
		}
		//
		clusterNodes.put(nodes, clusters);
		//
		// Test with all
		if (global != null && global.equals("true"))
		    clusterNodes = totalClusterNodes;

		// ok for each node
		for (Map<Node, double[]> nodeToPosition : clusterNodes.keySet()) {

		    // max min and so on
		    double minX, maxX, minY, maxY;
		    // determine minimum and maximum positions of the nodes
		    minX = Float.MAX_VALUE;
		    maxX = -Float.MAX_VALUE;
		    minY = Float.MAX_VALUE;
		    maxY = -Float.MAX_VALUE;
		    for (Node node : nodeToPosition.keySet()) {
			double[] position = nodeToPosition.get(node);
			double diameter = Math.sqrt(node.weight);
			minX = Math.min(minX, position[0] - diameter / 2);
			maxX = Math.max(maxX, position[0] + diameter / 2);
			minY = Math.min(minY, position[1] - diameter / 2);
			maxY = Math.max(maxY, position[1] + diameter / 2);
		    }

		    // determine maximum cluster of the nodes
		    Map<Node, Integer[]> nodeToCluster = clusterNodes.get(nodeToPosition);
		    int maxCluster = 0;
		    for (Integer[] cluster : nodeToCluster.values()) {
			maxCluster = Math.max(cluster[0], maxCluster);
		    }

		    // iterate trough the map and draw
		    double scale = Math.min(w / (maxX - minX), h / (maxY - minY));

		    for (Node node : nodeToPosition.keySet()) {

			int positionX = (int) Math.round((nodeToPosition.get(node)[0] - minX) * scale);
			int positionY = (int) Math.round((nodeToPosition.get(node)[1] - minY) * scale);
			int diameter = (int) Math.round(Math.sqrt(node.weight) * scale);

			out.print("ctx.beginPath();");
			if (global != null && global.equals("true")) {
			    if (clusterVal.intValue() == nodeToCluster.get(node)[0])
				out.print("ctx.fillStyle='" + color + "';");
			    else {
				Color colObj = new Color(nodeToCluster.get(node)[1], nodeToCluster.get(node)[2], nodeToCluster.get(node)[3]);
				out.print("ctx.fillStyle='rgb(" + colObj.getRed() + "," + colObj.getGreen() + "," + colObj.getBlue() + ")';");
			    }
			} else
			    out.print("ctx.fillStyle='" + color + "';");

			final double start = Math.PI * 0 / 180;
			final double end = Math.PI * 360 / 180;

			out.print("ctx.arc(" + positionX + "," + positionY + "," + diameter / 2 + "," + start + "," + end + ",false);");

			// add the element to the clickable list
			if(node.name.indexOf("(Publication_id : ") != -1){
			out.print("elementList.push(new Array(" + positionX + "," + positionY + "," + diameter / 2 + ",'"
				+ StringEscapeUtils.escapeJavaScript(node.name.substring(node.name.lastIndexOf("(Publication_id : ")+18,node.name.lastIndexOf(")"))) + "'));");
			}
			
			out.print("ctx.closePath();");
			out.print("ctx.fill();");

			out.print("ctx.fillStyle='black';");
			if (schema_id != null)
			    out.print("ctx.fillText('" + StringEscapeUtils.escapeJavaScript(node.name.substring(0, node.name.lastIndexOf(" (SchemaId : ")))
				    + "'," + positionX + "," + positionY + ");");
			else
			    out.print("ctx.fillText('" + StringEscapeUtils.escapeJavaScript(node.name.substring(0, node.name.lastIndexOf(" (Crawl_id : ")))
				    + "'," + positionX + "," + positionY + ");");

		    }
		}

		// add the click function, 1st case for FF, second for Opera
		// out.print("function myClick(ev){var x, y;if (ev.layerX || ev.layerX == 0) { x = ev.layerX;y = ev.layerY;} else if (ev.offsetX || ev.offsetX == 0) { x = ev.offsetX;y = ev.offsetY;}");
		// out.print("for(var i=0;i<elementList.length;i++){if( ((elementList[i][0] - elementList[i][2]) <= x) && ((elementList[i][0] + elementList[i][2]) >= x) && ((elementList[i][1] + elementList[i][2]) >= y) && ((elementList[i][1] - elementList[i][2]) <= y) ){window.alert (elementList[i][3]);break;}}}");
		out.print("function myClick(ev){var x = ev.layerX-this.offsetLeft; var y = ev.layerY-this.offsetTop;");		
		out.print("for(var i=0;i<elementList.length;i++){dx = x-elementList[i][0];dy = y-elementList[i][1];if( (dx*dx+dy*dy) <= (elementList[i][2]*elementList[i][2]) ){if(elementList[i][3]!=0){window.open ('"+callback_url+"'+elementList[i][3]);break;}}}}");
			
		out.print("</script>");

		if (_DEBUG || (test != null && test.equals("true")))
		    out.print("</BODY></HTML>");

		out.flush();
	    } catch (NumberFormatException nfe) {
		PrintWriter out = resp.getWriter();
		out.println("Width, height and ids must be integer values.");
		out.flush();
	    }
	}
    }

}
