package org.inria.websmatch.gwt.spreadsheet.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.inria.websmatch.db.MySQLDBConfLoader;
import org.inria.websmatch.gwt.spreadsheet.client.MachineLearningService;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.machineLearning.MLInstanceCompute;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.gwt.spreadsheet.client.MachineLearningService;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.schemaImporters.SpreadsheetImporter;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MachineLearningServiceImpl extends RemoteServiceServlet implements
		MachineLearningService {

    /**
     * 
     */
    private static final long serialVersionUID = -8638450295479005083L;

    private String dbuser = "ml_user";
    private String pass = "ml_pass";
    // Inria private String dbHost = "localhost";
    private String dbHost;// = "193.49.106.32";
    private int dbPort;// = 3306;
    private String dbName = "machine_learning";

    @Override
    public ArrayList<SimpleCell> getAttributeCellsFromML(String user,
	    String filename) {

	ArrayList<SimpleCell> cells = new ArrayList<SimpleCell>();

	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	} catch (InstantiationException e) {
	    L.Error(e.getMessage(),e);
	} catch (IllegalAccessException e) {
	    L.Error(e.getMessage(),e);
	} catch (ClassNotFoundException e) {
	    L.Error(e.getMessage(),e);
	}

	java.sql.Connection conn = null;
	try {
	    
	    // load conf
	    MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

	    dbHost = loader.getDbHost();
	    dbPort = loader.getDbPort();

	    conn = DriverManager.getConnection(
		    "jdbc:mysql://" + this.getDbHost() + ":" + this.getDbPort()
			    + "/" + this.dbName, dbuser, pass);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
	java.sql.Statement stat = null;
	try {
	    stat = conn.createStatement();
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// firstly, we have to see if this file has already an id
	String query = "SELECT id_sheet, x, y, first_cc_col, first_cc_row, type, behind_cell, right_cell, is_attribute FROM doc_list, cells WHERE cells.id_doc = doc_list.id_doc AND is_attribute = '1' AND doc_list.name ='"
		+ filename + "' AND doc_list.user = '" + user + "';";
	
	try {
	    ResultSet result = stat.executeQuery(query);
	    while (result.next()) {
		cells.add(new SimpleCell("", true, result.getInt("y"), result
			.getInt("x"), result.getInt("id_sheet"), result
			.getDouble("first_cc_col"), result
			.getDouble("first_cc_row"), result.getDouble("type"),
			result.getDouble("behind_cell"), result
				.getDouble("right_cell"),result.getDouble("above_cell"),result.getDouble("left_cell"), result
				.getDouble("is_attribute")));
	    }

	    if (cells.size() == 0) {

		// try without username
		query = "SELECT id_sheet, x, y, first_cc_col, first_cc_row, type, behind_cell, right_cell, above_cell, left_cell, is_attribute FROM doc_list, cells WHERE cells.id_doc = doc_list.id_doc AND is_attribute = '1' AND doc_list.name ='"
			+ filename + "';";

		result = stat.executeQuery(query);
		while (result.next()) {
		    cells.add(new SimpleCell("", true, result.getInt("y"),
			    result.getInt("x"), result.getInt("id_sheet"),
			    result.getDouble("first_cc_col"), result
				    .getDouble("first_cc_row"), result
				    .getDouble("type"), result
				    .getDouble("behind_cell"), result
				    .getDouble("right_cell"), result.getDouble("above_cell"),result.getDouble("left_cell"), result
				    .getDouble("is_attribute")));
		}

	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	if (stat != null)
	    try {
		stat.close();
	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }
	if (conn != null)
	    try {
		conn.close();
	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }

	return cells;
    }

    public void setDbHost(String dbHost) {
	this.dbHost = dbHost;
    }

    public String getDbHost() {
	return dbHost;
    }

    public void setDbPort(int dbPort) {
	this.dbPort = dbPort;
    }

    public int getDbPort() {
	return dbPort;
    }

    @Override
    public void addDocumentToML(String user, String filename, String baseDir) {
	
	SpreadsheetImporter xlsImporter = new SpreadsheetImporter();
	try {
	    xlsImporter.getSchemaElements(new URI("file:" + baseDir + "/"
	    	    + filename.replaceAll("\\s", "%20")));
	} catch (ImporterException e) {
	    L.Error(e.getMessage(),e);
	} catch (URISyntaxException e) {
	    L.Error(e.getMessage(),e);
	}
	MLInstanceCompute fact = new MLInstanceCompute(filename.replaceAll("\\s", "%20"),user,xlsImporter);
	fact.insertDatas(true);

    }

    @Override
    public SimpleCell getCellFromML(String user, String filename, int sheet,
	    int x, int y) {

	SimpleCell cell = null;

	try {
	    Class.forName("com.mysql.jdbc.Driver").newInstance();
	} catch (InstantiationException e) {
	    L.Error(e.getMessage(),e);
	} catch (IllegalAccessException e) {
	    L.Error(e.getMessage(),e);
	} catch (ClassNotFoundException e) {
	    L.Error(e.getMessage(),e);
	}

	java.sql.Connection conn = null;
	try {
	    
	    // load conf
	    MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

	    dbHost = loader.getDbHost();
	    dbPort = loader.getDbPort();
	    
	    conn = DriverManager.getConnection(
		    "jdbc:mysql://" + this.getDbHost() + ":" + this.getDbPort()
			    + "/" + this.dbName, dbuser, pass);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
	java.sql.Statement stat = null;
	try {
	    stat = conn.createStatement();
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	// firstly, we have to see if this file has already an id
	String query = "SELECT id_sheet, x, y, first_cc_col, first_cc_row, type, behind_cell, right_cell, above_cell, left_cell, is_attribute FROM doc_list, cells WHERE cells.id_doc = doc_list.id_doc AND doc_list.name ='"
		+ filename + "' AND doc_list.user = '" + user + "' AND id_sheet = '"+sheet+"' AND x = '"+x+"' AND y = '"+y+"';";
	
	try {
	    ResultSet result = stat.executeQuery(query);
	    while (result.next()) {
		cell = new SimpleCell("", true, result.getInt("y"),
			result.getInt("x"), result.getInt("id_sheet"),
			result.getDouble("first_cc_col"),
			result.getDouble("first_cc_row"),
			result.getDouble("type"),
			result.getDouble("behind_cell"),
			result.getDouble("right_cell"),
			result.getDouble("above_cell"),
			result.getDouble("left_cell"),
			result.getDouble("is_attribute"));
	    }

	    if (cell == null) {

		// try without username
		query = "SELECT id_sheet, x, y, first_cc_col, first_cc_row, type, behind_cell, right_cell, above_cell, left_cell, is_attribute FROM doc_list, cells WHERE cells.id_doc = doc_list.id_doc AND doc_list.name ='"
			+ filename + "' AND id_sheet = '"+sheet+"' AND x = '"+x+"' AND y = '"+y+"';";

		result = stat.executeQuery(query);
		while (result.next()) {
		    cell = new SimpleCell("", true, result.getInt("y"),
			    result.getInt("x"), result.getInt("id_sheet"),
			    result.getDouble("first_cc_col"),
			    result.getDouble("first_cc_row"),
			    result.getDouble("type"),
			    result.getDouble("behind_cell"),
			    result.getDouble("right_cell"),
			    result.getDouble("above_cell"),
			    result.getDouble("left_cell"),
			    result.getDouble("is_attribute"));
		}

	    }

	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	if (stat != null)
	    try {
		stat.close();
	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }
	if (conn != null)
	    try {
		conn.close();
	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }

	return cell;

    }

    public void setUser(String dbuser) {
	this.dbuser = dbuser;
    }

    public String getUser() {
	return dbuser;
    }
}
