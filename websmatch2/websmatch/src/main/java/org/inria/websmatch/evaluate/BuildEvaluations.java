package org.inria.websmatch.evaluate;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.inria.websmatch.utils.L;

import loader.alignment.AlignmentParserFactory;
import loader.alignment.IAlignmentParser;
import system.Configs;
import tools.Supports;
import yam.tools.WordNetHelper;
import datatypes.mapping.GMapping;
import datatypes.mapping.GMappingTable;
import datatypes.scenario.Scenario;

public class BuildEvaluations {

    /**
     * @param args
     */
    public static void main(String[] args) {

	// init WordNet
	yam.system.Configs.WNTMP = "WNTemplate.xml";
	yam.system.Configs.WNPROP = "file_properties.xml";
	/*
	 * yam.system.Configs.OAEI_ROOT =
	 * "/home/manu/workspace/OMSystem/data/ontology/newoaei/";
	 * yam.system.Configs.ORIGINAL =
	 * "/home/manu/workspace/OMSystem/data/ontology/newoaei/original.rdf";
	 */

	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}

	// db
	String dbuser = "matcher";
	String pass = "matcher";
	String dbHost = "localhost";
	int dbPort = 3306;
	String dbName = "matching_results";

	//
	java.sql.Connection conn = null;
	java.sql.Statement stat = null;

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
	    conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName, dbuser, pass);
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}
	try {
	    stat = conn.createStatement();
	} catch (SQLException e) {
	    L.Error(e.getMessage(),e);
	}

	File ontoDrawer = new File("/home/manu/workspace/WebSmatch/data/ontology/newoaei");
	System.out.println(ontoDrawer.exists());
	String[] paths = ontoDrawer.list();

	for (String path : paths) {

	    path = "/home/manu/workspace/WebSmatch/data/ontology/newoaei/" + path;

	    System.out.println(path);

	    File dir = new File(path);
	    String fileName = dir.getName() + ".rdf";

	    System.out.println(fileName);

	    if (dir.isDirectory() && !fileName.equals("101.rdf") && !fileName.equals(".svn.rdf")) {
		// we have to open an align file
		// 101
		// 103, 208, 209, 202
		String index = dir.getName();

		Scenario scenario = Supports.getOtherScenario(index);

		Configs.PRINT_SIMPLE = true;

		// get expert alignment
		IAlignmentParser align = AlignmentParserFactory.createParser(scenario.getAlignFN(), scenario.getAlignmentType());
		GMappingTable<String> experts = align.getMappings();

		Iterator<GMapping<String>> it = experts.getIterator();
		System.out.println("Experts mapping size : " + experts.getSize());

		// we need the doc ids
		int leftId = -1;
		int rightId = -1;

		String query = "SELECT id FROM stored_schemas WHERE name = '101' and id_group = '4'";

		try {
		    ResultSet res = stat.executeQuery(query);

		    res.next();
		    leftId = res.getInt("id");

		    query = "SELECT id FROM stored_schemas WHERE name = '" + index + "' and id_group = '4'";

		    res = stat.executeQuery(query);

		    res.next();
		    rightId = res.getInt("id");

		} catch (SQLException e1) {
		    e1.printStackTrace();
		}

		if (leftId == -1 || rightId == -1)
		    return;

		System.out.println("Left id : " + leftId + " Right id : " + rightId);

		while (it.hasNext()) {

		    GMapping<String> map = it.next();

		    /*
		     * query =
		     * "SELECT * FROM combined_elements WHERE left_node = (SELECT MIN(id) FROM elements WHERE schema_id = '"
		     * +leftId+"' AND name = '"
		     * +Supports.getLocalName(map.getEl2())+
		     * "') AND right_node = (SELECT MIN(id) FROM elements WHERE schema_id = '"
		     * +rightId+"' AND name = '"
		     * +Supports.getLocalName(map.getEl1())+"')";
		     */
		    query = "SELECT * FROM match_results WHERE id_element1 = (SELECT MIN(id) FROM elements WHERE schema_id = '" + leftId + "' AND name = '"
			    + Supports.getLocalName(map.getEl1()) + "') AND id_element2 = (SELECT MIN(id) FROM elements WHERE schema_id = '" + rightId
			    + "' AND name = '" + Supports.getLocalName(map.getEl2()) + "')";

		    try {
			ResultSet res = stat.executeQuery(query);

			if (!res.next()) {
			    System.out.println("Can't align " + query);
			    System.out.println("Map el1 : " + Supports.getLocalName(map.getEl1()) + "\t el2: " + Supports.getLocalName(map.getEl2()));
			}

		    } catch (SQLException e) {
			L.Error(e.getMessage(),e);
		    }

		    // for each couple, update expert value
		    /*
		     * query =
		     * "UPDATE combined_elements SET expert = '1' WHERE left_node = (SELECT MIN(id) FROM elements WHERE schema_id = '"
		     * +leftId+"' AND name = '"
		     * +Supports.getLocalName(map.getEl2())+
		     * "') AND right_node = (SELECT MIN(id) FROM elements WHERE schema_id = '"
		     * +rightId+"' AND name = '"
		     * +Supports.getLocalName(map.getEl1())+"')";
		     */
		    query = "UPDATE match_results SET expert = '1' WHERE id_element1 = (SELECT MIN(id) FROM elements WHERE schema_id = '" + leftId
			    + "' AND name = '" + Supports.getLocalName(map.getEl1()) + "') AND id_element2 = (SELECT MIN(id) FROM elements WHERE schema_id = '"
			    + rightId + "' AND name = '" + Supports.getLocalName(map.getEl2()) + "')";

		    // System.out.println(query);

		    try {
			stat.executeUpdate(query);
		    } catch (SQLException e) {
			L.Error(e.getMessage(),e);
		    }
		    		
		}
		
		// now insert in aligned schemas
		    query = "INSERT INTO aligned_schemas (id_schema1,id_schema2) VALUES ('"+leftId+"','"+rightId+"')";
		    
		    try {
			stat.executeUpdate(query);
		    } catch (SQLException e) {
			L.Error(e.getMessage(),e);
		    }

		
	    }
	}
	// close
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
}
