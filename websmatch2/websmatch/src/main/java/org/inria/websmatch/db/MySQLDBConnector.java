package org.inria.websmatch.db;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.utils.L;

public class MySQLDBConnector implements DBConnector{

	private java.sql.Connection conn = null;
	private java.sql.Statement stat = null;

	// db
	private String dbuser;// = "matcher";
	private String pass;// = "matcher";
	//private String dbHost = "localhost";
	private String dbHost;// = "193.49.106.32";
	private int dbPort;// = 3306;
	private String dbName;// = "matching_results";

	public MySQLDBConnector() {

		MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

		dbuser = loader.getDbuser();
		pass = loader.getPass();
		dbHost = loader.getDbHost();
		dbPort = loader.getDbPort();
		dbName = loader.getDbName();

	}

	public MySQLDBConnector(String user, String pw, String host, int port, String db){

		this.dbuser = user;
		this.pass = pw;
		this.dbHost = host;
		this.dbPort = port;
		this.dbName = db;

	}

	public void connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			L.Error(e.getMessage(),e);
		}

		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName, dbuser, pass);
		} catch (SQLException e) {
			L.Error(e.getMessage(),e);
		}
		try {
			// stat = conn.createStatement();
			stat = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stat.setFetchSize(Integer.MIN_VALUE);
		} catch (SQLException e) {
			L.Error(e.getMessage(),e);
		}
	}

	public void close() {

		// close
		try {
			if (stat != null)
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

	/**
	 * Get the XML schema stored in the database
	 *
	 * @param id
	 * @return
	 */
	public String getXML(Integer id){

		String res = "";

		this.connect();

		String req = "SELECT xml FROM stored_schemas WHERE id='"+id+"';";
		try {
			ResultSet set = this.getStat().executeQuery(req);
			while(set.next()){
				res = set.getString("xml");
			}
			set.close();
		} catch (SQLException e) {
			L.Error(e.getMessage(),e);
		}

		this.close();

		return res;
	}

	public java.sql.Connection getConn() {
		return conn;
	}

	public void setConn(java.sql.Connection conn) {
		this.conn = conn;
	}

	public java.sql.Statement getStat() {
		return stat;
	}

	public void setStat(java.sql.Statement stat) {
		this.stat = stat;
	}

	@Override
	public boolean insertXML(String doc) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getEditedXML(String object_id, String dbName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String insertXML(String auto_xml, String edit_xml, String fileCompleteURI, String name, String source, String user, String description, boolean ccDetectPb, boolean attrDetectPb, String detectDesc, boolean trashed, String dbName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchemaData> getSchemas(boolean onlyEdited, String dbName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DetectionQualityData> getDetectionQualityList(boolean onlyEdited, String dbName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String updateXML(String objectId, String edit_xml, String name, String description, boolean ccDetectionPb, boolean attrDetectionPb, String detectPb, boolean trashed, String dbName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DetectionQualityData getDetectionQualityData(String objectId, String dbName) {
		// TODO Auto-generated method stub
		return null;
	}

}
