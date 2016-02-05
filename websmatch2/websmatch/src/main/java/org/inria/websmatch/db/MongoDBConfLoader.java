package org.inria.websmatch.db;

import org.inria.websmatch.utils.L;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MongoDBConfLoader {

	private String dbHost;
	private int dbPort;
	private String dbName;

	private static MongoDBConfLoader instance;

	public static MongoDBConfLoader getInstance() {
		if (null == instance) {
			instance = new MongoDBConfLoader();
		}
		return instance;
	}

	private MongoDBConfLoader() {

		InputStream is = getClass().getClassLoader().getResourceAsStream("org/inria/websmatch/db/mongodbconf.xml");
		SAXBuilder sxb = new SAXBuilder();
		try {
			Document doc = sxb.build(is);
			Element root = doc.getRootElement();
			setDbHost(root.getChildText("dbHost"));
			setDbPort(new Integer(root.getChildText("dbPort")).intValue());
			setDbName(root.getChildText("dbName"));
		} catch (JDOMException | IOException e) {
			L.Error(e.getMessage(), e);
		}

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

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbName() {
		return dbName;
	}

}
