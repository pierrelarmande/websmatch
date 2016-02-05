package org.inria.websmatch.db;

import java.io.IOException;
import java.io.InputStream;

import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.L;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class MySQLDBConfLoader {

    private String dbuser;
    private String pass;
    private String dbHost;
    private int dbPort;
    private String dbName;

    private static MySQLDBConfLoader instance;

    public static MySQLDBConfLoader getInstance() {
	if (null == instance) {
	    instance = new MySQLDBConfLoader();
	}
	return instance;
    }

    private MySQLDBConfLoader() {

	InputStream is = getClass().getClassLoader().getResourceAsStream("org/inria/websmatch/db/dbconf.xml");
	SAXBuilder sxb = new SAXBuilder();
	try {
	    Document doc = sxb.build(is);
	    Element root = doc.getRootElement();
	    setDbuser(root.getChildText("dbuser"));
	    setPass(root.getChildText("pass"));
	    setDbHost(root.getChildText("dbHost"));
	    setDbPort(new Integer(root.getChildText("dbPort")).intValue());
	    setDbName(root.getChildText("dbName"));
	} catch (JDOMException e) {
	    L.Error(e.getMessage(), e);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}

    }

    public void setDbuser(String dbuser) {
	this.dbuser = dbuser;
    }

    public String getDbuser() {
	return dbuser;
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

    public void setPass(String pass) {
	this.pass = pass;
    }

    public String getPass() {
	return pass;
    }

    public void setDbName(String dbName) {
	this.dbName = dbName;
    }

    public String getDbName() {
	return dbName;
    }

}
