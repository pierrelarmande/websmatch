package org.inria.websmatch.machineLearning;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import jxl.Cell;
import jxl.CellType;

import org.inria.websmatch.db.MySQLDBConfLoader;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.utils.L;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElementList;
import org.mitre.schemastore.porters.schemaImporters.SpreadsheetImporter;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

public class MLInstanceCompute {

    private String fileName;
    private String fileUser;
    // the importer
    private SpreadsheetImporter importer;

    private String user = "ml_user";
    private String pass = "ml_pass";
    // Inria private String dbHost = "localhost";
    private String dbHost;// = "193.49.106.32";
    private int dbPort;// = 3306;
    private String dbName = "machine_learning";
    // private String storeService =
    // "http://localhost:8080/SchemaStore/services/SchemaStore";
    /**
     * @todo Make a pref file for this
     */
    // private String storeService = "http://websmatch.inria.fr/SchemaStore/services/SchemaStore";
    private String storeService = "http://constraint.lirmm.fr/SchemaStore/services/SchemaStore";

    // private String baseXLSDir = "/tmp";
    private String baseXLSDir = "/var/www/xls";

    /**
     * Constructor
     */

    public MLInstanceCompute(String fileName, String fileUser, SpreadsheetImporter imp) {

	this.setFileName(fileName);
	this.setFileUser(fileUser);
	this.setImporter(imp);

    }

    public MLInstanceCompute(SpreadsheetImporter imp) {

	this.setFileName(null);
	this.setFileUser(null);
	this.setImporter(imp);

    }

    /**
     * First step we insert data with criterium facts
     */

    public void insertDatas(boolean insertIfExist) {

	// if false, verify if exist
	if (!insertIfExist) {

	    try {
		try {
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
		    L.Error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
		    L.Error(e.getMessage(),e);
		} catch (ClassNotFoundException e) {
		    L.Error(e.getMessage(),e);
		}
		
		 // load conf
		    MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

		    dbHost = loader.getDbHost();
		    dbPort = loader.getDbPort();
		
		java.sql.Connection conn = DriverManager.getConnection("jdbc:mysql://" + this.getDbHost() + ":" + this.getDbPort() + "/" + this.dbName, user,
			pass);
		java.sql.Statement stat = conn.createStatement();

		// firstly, we have to see if this file has already an id
		String query = "SELECT id_doc FROM doc_list WHERE name ='" + this.getFileName() + "' AND user = '" + this.getFileUser() + "';";
		ResultSet result = stat.executeQuery(query);

		int doc_id = -1;

		// if next is false, no doc, else get the id
		if (result.next()) {
		    doc_id = result.getInt("id_doc");
		}

		if(doc_id == -1) insertIfExist = true;
		
		stat.close();
		conn.close();

	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }
	}

	if (insertIfExist) {
	    // now we go on and insert each cell with the good values
	    try {
		try {
		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
		    L.Error(e.getMessage(),e);
		} catch (IllegalAccessException e) {
		    L.Error(e.getMessage(),e);
		} catch (ClassNotFoundException e) {
		    L.Error(e.getMessage(),e);
		}
		
		 // load conf
		    MySQLDBConfLoader loader = MySQLDBConfLoader.getInstance();

		    dbHost = loader.getDbHost();
		    dbPort = loader.getDbPort();
		
		java.sql.Connection conn = DriverManager.getConnection("jdbc:mysql://" + this.getDbHost() + ":" + this.getDbPort() + "/" + this.dbName, user,
			pass);
		java.sql.Statement stat = conn.createStatement();

		// firstly, we have to see if this file has already an id
		String query = "SELECT id_doc FROM doc_list WHERE name ='" + this.getFileName() + "' AND user = '" + this.getFileUser() + "';";
		ResultSet result = stat.executeQuery(query);

		int doc_id = -1;

		// if next is false, no doc, else get the id
		if (result.next()) {
		    doc_id = result.getInt("id_doc");
		} else {
		    // so we insert the doc name and get the id
		    query = "INSERT INTO doc_list (name, user) VALUES ('" + this.getFileName() + "','" + this.getFileUser() + "');";
		    stat.executeUpdate(query);

		    // get the id
		    query = "SELECT id_doc FROM doc_list WHERE name ='" + this.getFileName() + "' AND user = '" + this.getFileUser() + "';";
		    result = stat.executeQuery(query);

		    if (result.next()) {
			doc_id = result.getInt("id_doc");
		    } else {
			// big problem, escape
			return;
		    }

		}

		// we have the doc_id, we can insert all cells
		// iterate through the cells
		ArrayList<List<Cell[]>> datas = importer.getDatas();

		// first sheet
		for (int sheet = 0; sheet < datas.size(); sheet++) {

		    // get the ccs of this sheet
		    ArrayList<ConnexComposant> ccs = importer.getConnexComps().get(sheet);

		    // go for the cells
		    List<Cell[]> cells = datas.get(sheet);

		    // go with rows
		    for (int row = 0; row < cells.size(); row++) {

			// go with cols
			for (int col = 0; col < cells.get(row).length; col++) {

			    // get the cell
			    Cell cell = cells.get(row)[col];

			    SimpleCell sCell = this.computeValue(cell, cells, ccs, sheet, row, col);

			    // we have all the critters, insert it in db

			    query = "REPLACE INTO cells (x,y,id_doc,id_sheet,first_cc_col,first_cc_row,type,behind_cell,right_cell,above_cell, left_cell, is_attribute) "
				    + "VALUES ('"
				    + col
				    + "','"
				    + row
				    + "','"
				    + doc_id
				    + "','"
				    + sheet
				    + "','"
				    + sCell.getFirst_cc_col()
				    + "','"
				    + sCell.getFirst_cc_row()
				    + "','"
				    + sCell.getType()
				    + "','"
				    + sCell.getBehind_cell()
				    + "','"
				    + sCell.getRight_cell()
				    + "','"
				    + sCell.getAbove_cell()
				    + "','" + sCell.getLeft_cell() + "','0');";
			    stat.executeUpdate(query);

			}

		    }

		}

		// now have to get real attributes from schemastore
		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();
		try {
		    SchemaStoreObject sc = serviceLoc.getSchemaStore(new URL(this.storeService));

		    Schema[] sch = sc.getSchemas();

		    // get the attributes for the good schema
		    for (int schCount = 0; schCount < sch.length; schCount++) {
			if (sch[schCount].getSource().equals(fileName) && sch[schCount].getAuthor().equals(fileUser)) {

			    // get the attr
			    SchemaElementList list = sc.getSchemaElements(sch[schCount].getId());
			    Attribute[] attr = list.getAttributes();

			    for (int attrCount = 0; attrCount < attr.length; attrCount++) {

				String[] splitted = attr[attrCount].getDescription().split("\\p{Punct}");

				// we have to update the cell attribute value
				query = "UPDATE cells SET is_attribute = '1' WHERE id_doc = '" + doc_id + "' AND id_sheet ='" + splitted[5] + "' AND x = '"
					+ splitted[1] + "' AND y = '" + splitted[3] + "';";
				stat.executeUpdate(query);
			    }

			}
		    }

		} catch (ServiceException e) {
		    L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
		    L.Error(e.getMessage(),e);
		} catch (MalformedURLException e) {
		    L.Error(e.getMessage(),e);
		}

		stat.close();
		conn.close();

	    } catch (SQLException e) {
		L.Error(e.getMessage(),e);
	    }
	}

    }

    /**
     * Create the SimpleCell with good values from Cell JXL
     */

    public SimpleCell computeValue(Cell cell, List<Cell[]> localDatas, ArrayList<ConnexComposant> ccs, int sheet, int row, int col) {

	SimpleCell sCell = new SimpleCell();
	sCell.setAttribute(false);
	sCell.setIs_attributeML(0);
	sCell.setContent(cell.getContents());
	sCell.setJxlCol(col);
	sCell.setJxlRow(row);
	sCell.setSheet(sheet);
	// critters

	// cc critters
	double isFirtLineOfCc = this.isOnFirstLineOfCc(cell, ccs);
	double isFirstColOfCc = this.isOnFirstColOfCc(cell, ccs);

	sCell.setFirst_cc_row(isFirtLineOfCc);
	sCell.setFirst_cc_col(isFirstColOfCc);

	// type critter
	double type;
	if (importer.isCellVoid(cell))
	    type = 0;
	else {
	    type = this.getType(cell);
	}

	sCell.setType(type);

	// neighborhood critters
	double behind;

	// we are on last row...
	if (row == (localDatas.size() - 1))
	    behind = 0;
	// get the cell behind
	else {
	    // if there is nothing behind at this col
	    if ((localDatas.get(row + 1).length - 1) < col) {
		behind = 0;
	    }
	    // then compare
	    else {
		Cell cb = localDatas.get(row + 1)[col];
		behind = this.getBehind(cell, cb);
	    }
	}

	sCell.setBehind_cell(behind);

	// next
	double right;

	// we are on the last col...
	if (col == (localDatas.get(row).length - 1))
	    right = 0;
	// get the cell on the right
	else {
	    // if there is nothing at this row at col+1
	    if ((localDatas.get(row).length - 1) < (col + 1)) {
		right = 0;
	    }
	    // then compare
	    else {
		Cell cr = localDatas.get(row)[col + 1];
		right = this.getRight(cell, cr);
	    }
	}

	sCell.setRight_cell(right);

	// above cell
	double above;

	// we are the first line
	if (row == 0)
	    above = 1;
	// get the cell behind
	else {
	    // if there is nothing above at this col
	    if ((localDatas.get(row - 1).length - 1) < col) {
		above = 1;
	    }
	    // then compare
	    else {
		Cell cb = localDatas.get(row - 1)[col];
		above = this.getAbove(cell, cb);
	    }
	}
	sCell.setAbove_cell(above);

	// next
	double left;

	// we are on the first col...
	if (col == 0)
	    left = 1;
	// get the cell on the left
	else {
	    // if there is nothing at this row at col-1
	    if ((localDatas.get(row).length - 1) < (col - 1)) {
		left = 1;
	    }
	    // then compare
	    else {
		Cell cl = localDatas.get(row)[col - 1];
		left = this.getLeft(cell, cl);
	    }
	}

	sCell.setLeft_cell(left);

	// we have all the critters
	return sCell;

    }

    /**
     * Is this cell on the first line of a Cc?
     * 
     * @param c
     * @param ccs
     * @return
     */

    public double isOnFirstLineOfCc(Cell c, ArrayList<ConnexComposant> ccs) {

	for (int ccCount = 0; ccCount < ccs.size(); ccCount++) {

	    ConnexComposant cc = ccs.get(ccCount);

	    // search if this cell is in this connex comp
	    // if (c.getRow() == cc.getStartY())
	    if (c.getRow() >= cc.getStartY() && c.getRow() <= cc.getEndY() && c.getColumn() >= cc.getStartX() && c.getColumn() <= cc.getEndX()) {
		if (c.getRow() == cc.getStartY())
		    return 1;
		else {
		    return Math.pow(0.95, c.getRow() - cc.getStartY());
		}
	    }
	}
	return 0;
    }

    public double isOnFirstColOfCc(Cell c, ArrayList<ConnexComposant> ccs) {

	for (int ccCount = 0; ccCount < ccs.size(); ccCount++) {

	    ConnexComposant cc = ccs.get(ccCount);
	    if (c.getRow() >= cc.getStartY() && c.getRow() <= cc.getEndY() && c.getColumn() >= cc.getStartX() && c.getColumn() <= cc.getEndX()) {
		if (c.getColumn() == cc.getStartX())
		    return 1;
		else {
		    return Math.pow(0.95, c.getColumn() - cc.getStartX());
		}
	    }
	}

	return 0;

    }

    public double getType(Cell c) {
	
	if(importer.isCellVoid(c)) return 0;

	if (c.getType().equals(CellType.LABEL))
	    return 1;

	else if (c.getType().equals(CellType.NUMBER) || c.getType().equals(CellType.NUMBER_FORMULA))
	    return 0.5;
	else
	    return 0;

    }

    public double getBehind(Cell c, Cell cb) {

	if (c.getType().equals(CellType.LABEL)
		&& (cb.getType().equals(CellType.NUMBER) || cb.getType().equals(CellType.NUMBER_FORMULA) || cb.getType().equals(CellType.DATE))) {
	    return 1;
	}

	else if (c.getType().equals(CellType.LABEL) && (cb.getType().equals(CellType.LABEL))) {
	    return 0.5;
	}

	else
	    return 0;

    }

    public double getAbove(Cell c, Cell ca) {

	if (importer.isCellVoid(ca)) {
	    return 1;
	}

	else if (c.getType().equals(CellType.LABEL) && (ca.getType().equals(CellType.NUMBER) || ca.getType().equals(CellType.NUMBER_FORMULA))) {
	    return 1;
	}

	else if (c.getType().equals(CellType.LABEL) && (ca.getType().equals(CellType.LABEL))) {
	    return 0.5;
	}

	else
	    return 0;

    }

    public double getRight(Cell c, Cell cr) {

	if (c.getType().equals(CellType.LABEL) && (cr.getType().equals(CellType.NUMBER) || cr.getType().equals(CellType.NUMBER_FORMULA))) {
	    return 1;
	} else

	if (c.getType().equals(CellType.LABEL) && (cr.getType().equals(CellType.LABEL))) {
	    return 0.5;
	}

	else
	    return 0;

    }

    public double getLeft(Cell c, Cell cl) {

	if (importer.isCellVoid(cl)) {
	    return 1;
	}

	else if (c.getType().equals(CellType.LABEL) && (cl.getType().equals(CellType.NUMBER) || cl.getType().equals(CellType.NUMBER_FORMULA))) {
	    return 1;
	} else

	if (c.getType().equals(CellType.LABEL) && (cl.getType().equals(CellType.LABEL))) {
	    return 0.5;
	}

	else
	    return 0;

    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    public String getFileName() {
	return fileName;
    }

    public void setUser(String user) {
	this.user = user;
    }

    public String getUser() {
	return user;
    }

    public void setDbName(String dbName) {
	this.dbName = dbName;
    }

    public String getDbName() {
	return dbName;
    }

    public void setPass(String pass) {
	this.pass = pass;
    }

    public String getPass() {
	return pass;
    }

    public void setDbPort(int dbPort) {
	this.dbPort = dbPort;
    }

    public int getDbPort() {
	return dbPort;
    }

    public void setDbHost(String dbHost) {
	this.dbHost = dbHost;
    }

    public String getDbHost() {
	return dbHost;
    }

    public void setImporter(SpreadsheetImporter importer) {
	this.importer = importer;
    }

    public SpreadsheetImporter getImporter() {
	return importer;
    }

    public void setStoreSerivce(String storeSerivce) {
	this.storeService = storeSerivce;
    }

    public String getStoreSerivce() {
	return storeService;
    }

    public void setBaseXLSDir(String baseXLSDir) {
	this.baseXLSDir = baseXLSDir;
    }

    public String getBaseXLSDir() {
	return baseXLSDir;
    }

    public void setFileUser(String fileUser) {
	this.fileUser = fileUser;
    }

    public String getFileUser() {
	return fileUser;
    }
}
