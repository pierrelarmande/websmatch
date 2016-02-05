package org.inria.websmatch.gwt.spreadsheet.server;

import java.awt.Color;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.db.MySQLDBConfLoader;
import org.inria.websmatch.gwt.spreadsheet.client.ClusteringService;
import org.inria.websmatch.gwt.spreadsheet.client.models.Node;
import org.inria.websmatch.linlog.Edge;
import org.inria.websmatch.linlog.LinLogLayout;
import org.inria.websmatch.linlog.MinimizerBarnesHut;
import org.inria.websmatch.linlog.OptimizerModularity;
import org.inria.websmatch.utils.L;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.ClusteringService;
import org.inria.websmatch.linlog.Edge;
import org.inria.websmatch.linlog.MinimizerBarnesHut;
import org.inria.websmatch.linlog.OptimizerModularity;
import org.inria.websmatch.utils.L;

public class ClusteringServiceImpl extends RemoteServiceServlet implements ClusteringService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3072451627701756019L;

	private String dbuser;// = "matcher";
	private String pass;// = "matcher";
	// Inria private String dbHost = "localhost";
	private String dbHost;// = "193.49.106.32";
	private int dbPort;// = 3306;
	private String dbName;// = "matching_results";

	//
	private java.sql.Connection conn = null;
	private java.sql.Statement stat = null;

	@Deprecated
	public void updateClusters(int schemaId) {
		// String query1 = "DROP TABLE view_schemas_distance_9;";
		// String query2 =
		// "CREATE TABLE view_schemas_distance_9 AS SELECT * FROM view_schemas_distance_9_2;";

		// used for Riga OWL only
		String query1 = "DROP TABLE schemas_distance_riga;";
		String query2 = "CREATE TABLE schemas_distance_riga AS SELECT * FROM view_schemas_distance_riga_9;";

		try {

			if (conn == null) {
				connect();
			}

			stat.executeUpdate(query1);
			stat.executeUpdate(query2);
			close();
		} catch (SQLException e) {
			L.Error(e.getMessage(), e);
		}
	}

	@Override
	public Map<Map<Node, double[]>, Map<Node, Integer[]>> getClusterNodes(int w, int h, boolean datapublica, String dbName) {

		Map<Map<Node, double[]>, Map<Node, Integer[]>> res = new HashMap<Map<Node, double[]>, Map<Node, Integer[]>>();

		Map<String, Map<String, Double>> graph = new HashMap<String, Map<String, Double>>();

		// now work with mongo
		Map<String[],Double> distances = new HashMap<String[], Double>();

		MongoDBConnector connector = MongoDBConnector.getInstance();
		distances = connector.getDistances(dbName);

		L.Debug(this.getClass().getSimpleName(),"Size of dist matrix is "+distances.size(),true);

		Set<String[]> keys = distances.keySet();
		for(String[] key : keys){

			graph.put(key[1]+" (SchemaId : "+key[0]+")", new HashMap<String, Double>());    
			graph.get(key[1]+" (SchemaId : "+key[0]+")").put(key[3]+" (SchemaId : "+key[2]+")", distances.get(key));
			L.Debug(this.getClass().getSimpleName(), key[0]+" | "+key[2]+" | "+distances.get(key), true);

		}

		// old mysql code
		//connect();

		// ok get infos from data base
		/*String query = new String();
	if (!datapublica) {
	    if (this.getThreadLocalRequest() != null) {
		Integer groupId = LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId());
		query = "SELECT * FROM schemas_distance WHERE id_group1 = '" + groupId + "' AND id_group2 = '" + groupId + "' AND distance > '0';";
		// riga owl
		if (groupId.intValue() == 10 || groupId.intValue() == 11 || groupId.intValue() == 12) {
		    query = "SELECT * FROM schemas_distance_riga WHERE id_group1 = '" + groupId + "' AND id_group2 = '" + groupId + "' AND distance > '0';";
		}
	    } else {
		query = "SELECT * FROM schemas_distance WHERE id_group1 = '0' AND id_group2 = '0' AND distance > '0';";
	    }
	} else {
	    query = "SELECT * FROM schemas_distance, datapublica_map dm1, datapublica_map dm2 WHERE (id_schema1 IN (SELECT id FROM datapublica_map) AND"
		    + " id_schema2 IN (SELECT id FROM datapublica_map)) AND (schemas_distance.id_schema1 = dm1.id AND schemas_distance.id_schema2 = dm2.id) AND distance > '0';";
	}

	try {
	    if (!datapublica) {
		ResultSet result = stat.executeQuery(query);
		while (result.next()) {
		    graph.put(result.getString("name1") + " (SchemaId : " + result.getInt("id_schema1") + ")", new HashMap<String, Double>());
		    graph.get(result.getString("name1") + " (SchemaId : " + result.getInt("id_schema1") + ")").put(
			    result.getString("name2") + " (SchemaId : " + result.getInt("id_schema2") + ")", result.getDouble("distance") * 10.0);
		}
	    } else {
		ResultSet result = stat.executeQuery(query);
		while (result.next()) {
		    graph.put(
			    result.getString("name1") + " (Crawl_id : " + result.getInt("dm1.crawl_id") + ")(Publication_id : "
				    + result.getInt("dm1.publication_id") + ")", new HashMap<String, Double>());
		    graph.get(
			    result.getString("name1") + " (Crawl_id : " + result.getInt("dm1.crawl_id") + ")(Publication_id : "
				    + result.getInt("dm1.publication_id") + ")").put(
			    result.getString("name2") + " (Crawl_id : " + result.getInt("dm2.crawl_id") + ")(Publication_id : "
				    + result.getInt("dm2.publication_id") + ")", result.getDouble("distance") * 10.0);
		}
	    }
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}*/

		// close();


		L.Debug(this.getClass().getSimpleName(),"Starting clustering.",true);
		// ok now work with linlog
		graph = LinLogLayout.makeSymmetricGraph(graph);
		Map<String, Node> nameToNode = LinLogLayout.makeNodes(graph);
		List<Node> nodes = new ArrayList<Node>(nameToNode.values());
		List<Edge> edges = LinLogLayout.makeEdges(graph, nameToNode);

		Map<Node, double[]> nodeToPosition = LinLogLayout.makeInitialPositions(nodes, false);
		new MinimizerBarnesHut(nodes, edges, -1.0, 2.0, 0.25).minimizeEnergy(nodeToPosition, 100);
		Map<Node, Integer> nodeToCluster = new OptimizerModularity().execute(nodes, edges, true);

		// for debug
		//LinLogLayout.writePositions(nodeToPosition, nodeToCluster, "/tmp/out.clusters");

		Map<Node, Integer[]> coloredNodeToCluster = new HashMap<Node, Integer[]>();

		// ok now replace integer by RGB color
		int maxCluster = 0;
		for (int cluster : nodeToCluster.values()) {
			maxCluster = Math.max(cluster, maxCluster);
		}

		for (Node node : nodeToPosition.keySet()) {
			float hue = nodeToCluster.get(node) / (float) (maxCluster + 1);
			int icol = Color.HSBtoRGB(hue, 1.0f, 1.0f);
			Color color = new Color(icol);

			coloredNodeToCluster.put(node, new Integer[] { nodeToCluster.get(node), color.getRed(), color.getGreen(), color.getBlue() });
		}
		//

		res.put(nodeToPosition, coloredNodeToCluster);
		L.Debug(this.getClass().getSimpleName(),"End of clustering",true);

		return res;
	}

	@Deprecated
	private void connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			L.Error(e.getMessage(),e);
		} catch (IllegalAccessException e) {
			L.Error(e.getMessage(),e);
		} catch (ClassNotFoundException e) {
			L.Error(e.getMessage(),e);
		}

		try {

			// load conf
			MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

			dbuser = loader.getDbuser();
			pass = loader.getPass();
			dbHost = loader.getDbHost();
			dbPort = loader.getDbPort();
			dbName = loader.getDbName();

			conn = DriverManager.getConnection("jdbc:mysql://" + this.dbHost + ":" + this.dbPort + "/" + this.dbName, dbuser, pass);
		} catch (SQLException e) {
			L.Error(e.getMessage(),e);
		}
		try {
			stat = conn.createStatement();
		} catch (SQLException e) {
			L.Error(e.getMessage(),e);
		}
	}

	@Deprecated
	private void close() {
		try {
			stat.close();
		} catch (SQLException e) {
			L.Error(e.getMessage(),e);
		}
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			L.Error(e.getMessage(),e);
		}
	}

	/*@Override
    public Map<Map<Node, double[]>, Map<Node, Integer[]>> getStringClusterNodes(int w, int h) {
	Map<Map<Node, double[]>, Map<Node, Integer[]>> res = new HashMap<Map<Node, double[]>, Map<Node, Integer[]>>();

	Map<String, Map<String, Double>> graph = new HashMap<String, Map<String, Double>>();

	connect();

	// ok get infos from data base
	Integer groupId = LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId());
	String query = "SELECT * FROM view_schemas_distance_9 WHERE id_group1 = '" + groupId + "' AND id_group2 = '" + groupId + "';";

	try {
	    ResultSet result = stat.executeQuery(query);
	    while (result.next()) {
		graph.put(result.getString("name1"), new HashMap<String, Double>());
		graph.get(result.getString("name1")).put(result.getString("name2"), result.getDouble("distance") * 10.0);
	    }
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	close();

	// ok now work with linlog
	graph = LinLogLayout.makeSymmetricGraph(graph);
	Map<String, Node> nameToNode = LinLogLayout.makeNodes(graph);
	List<Node> nodes = new ArrayList<Node>(nameToNode.values());
	List<Edge> edges = LinLogLayout.makeEdges(graph, nameToNode);
	Map<Node, double[]> nodeToPosition = LinLogLayout.makeInitialPositions(nodes, false);
	// new MinimizerBarnesHut(nodes, edges, -1.0, 2.0,
	// 0.05).minimizeEnergy(nodeToPosition, 100);
	new MinimizerBarnesHut(nodes, edges, -1.0, 2.0, 0.25).minimizeEnergy(nodeToPosition, 100);
	// new MinimizerBarnesHut(nodes, edges, 0.0, 1.0,
	// 0.05).minimizeEnergy(nodeToPosition, 100);
	Map<Node, Integer> nodeToCluster = new OptimizerModularity().execute(nodes, edges, false);
	Map<Node, Integer[]> coloredNodeToCluster = new HashMap<Node, Integer[]>();

	// ok now replace integer by RGB color
	int maxCluster = 0;
	for (int cluster : nodeToCluster.values()) {
	    maxCluster = Math.max(cluster, maxCluster);
	}

	for (Node node : nodeToPosition.keySet()) {
	    float hue = nodeToCluster.get(node) / (float) (maxCluster + 1);
	    int icol = Color.HSBtoRGB(hue, 1.0f, 1.0f);
	    Color color = new Color(icol);

	    coloredNodeToCluster.put(node, new Integer[] { nodeToCluster.get(node), color.getRed(), color.getGreen(), color.getBlue() });
	}
	//

	res.put(nodeToPosition, coloredNodeToCluster);
	// return nodeToPosition;
	return res;
    }*/

}
