package org.inria.websmatch.machineLearning;

import java.sql.DriverManager;
import java.sql.ResultSet;

import org.inria.websmatch.db.MySQLDBConfLoader;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.utils.L;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

import com.thoughtworks.xstream.XStream;

public class CellClassifier {

	private String user = "ml_user";
	private String pass = "ml_pass";
	// Inria private String dbHost = "localhost";
	private String dbHost;// = "193.49.106.32";
	private int dbPort;// = 3306;
	private String dbName = "machine_learning";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CellClassifier cellClassifier = new CellClassifier();
		System.out.println(cellClassifier.cl.getClass());
		SimpleCell cell = new SimpleCell("blabla", false, 1, 12, 0, 0.2, 0.3, 1.0, 0.7, 0.8, 0, 0, 0.0);
		cellClassifier.classifyCell(cell);
	}

	private Classifier cl;
	private Instances instances;

	public CellClassifier(Classifier cl) throws Exception {
		this.cl = cl;
		if (cl == null) {
	    /*
	     * Class.forName("com.mysql.jdbc.Driver").newInstance(); String url
	     * = "jdbc:mysql://localhost/machine_learning"; Connection conn =
	     * DriverManager.getConnection(url, "ml_user", "ml_pass"); XStream
	     * xStrean = new XStream(); Statement stt = conn.createStatement();
	     */

			XStream xStrean = new XStream();

			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				L.Error(e.getMessage(),e);
			}

			// load conf
			MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

			dbHost = loader.getDbHost();
			dbPort = loader.getDbPort();

			java.sql.Connection conn = DriverManager.getConnection("jdbc:mysql://" + this.dbHost + ":" + this.dbPort + "/" + this.dbName, user, pass);
			java.sql.Statement stt = conn.createStatement();

			// stt = conn.createStatement();
			ResultSet res = stt.executeQuery("SELECT xml FROM classifiers WHERE name = 'J48'");
			if (res.next()) {
				this.cl = (Classifier) xStrean.fromXML(res.getString("xml"));
			}
			res.close();
			stt.close();
			conn.close();
		}
		InstanceQuery query = new InstanceQuery();

		query.setDatabaseURL("jdbc:mysql://" + this.dbHost + ":" + this.dbPort + "/" + this.dbName);

		query.setUsername("ml_user");
		query.setPassword("ml_pass");
		query.setQuery("SELECT first_cc_col,first_cc_row,type,behind_cell, right_cell, above_cell, left_cell, is_attribute  FROM cells LIMIT 0,1");
		instances = query.retrieveInstances();
		instances.setClass(instances.attribute("is_attribute"));
	}

	/*
     * default constructor: load the classifier which minimize the
     * miss-classification rate
     */
	public CellClassifier() throws Exception {
		this(null);
	}

	public SimpleCell classifyCell(SimpleCell cell) {
		Instance instance = new Instance(8);
		instance.setValue(instances.attribute("first_cc_col"), cell.getFirst_cc_col());
		instance.setValue(instances.attribute("first_cc_row"), cell.getFirst_cc_row());
		instance.setValue(instances.attribute("type"), cell.getType());
		instance.setValue(instances.attribute("behind_cell"), cell.getBehind_cell());
		instance.setValue(instances.attribute("right_cell"), cell.getRight_cell());
		instance.setValue(instances.attribute("above_cell"), cell.getAbove_cell());
		instance.setValue(instances.attribute("left_cell"), cell.getLeft_cell());
		// instance.setValue(test.attribute("is_attribute"), 1);
		// instance.setClassMissing();

		instances.delete();
		instances.add(instance);
		instance.setDataset(instances);
		// System.out.println("Cell : "+instance);

		try {
			// add the non void cell
			if (cl.classifyInstance(instances.instance(0)) > 0.5 && !cell.getContent().trim().equals("")) {
				cell.setAttribute(true);
				cell.setIs_attributeML(1.0);
				System.out.println(cell + " is an attribute");
			} else {
				cell.setAttribute(false);
				cell.setIs_attributeML(0.0);
				// System.out.println(cell+" is not an attribute");
			}
			return cell;
		} catch (Exception e) {
			L.Error(e.getMessage(),e);
		}
		return cell;
	}
}
