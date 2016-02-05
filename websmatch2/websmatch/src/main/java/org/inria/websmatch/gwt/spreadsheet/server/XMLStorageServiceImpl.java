package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.File;
import java.util.List;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageService;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xls.SheetsSerializer;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageService;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xls.SheetsSerializer;

public class XMLStorageServiceImpl extends RemoteServiceServlet implements XMLStorageService {

    /**
     * 
     */
    private static final long serialVersionUID = 3166897298689670745L;
    private MongoDBConnector mongo;
    private String baseXLSDir;

    public void init() {
	mongo = MongoDBConnector.getInstance();
	baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");
    }

    @Override
    public String importDocument(SimpleSheet[] original, SimpleSheet[] edited, String name, String source, String user, String description, String dbName,
	    String crawl_id, String publication_id, boolean reloaded, String objectId, boolean ccDetectionPb, boolean attrDetectionPb, String detectPb, boolean trashed) {

	String head = "<xml>\n\t<user_id>" + user + "</user_id>\n\t<doc_url>" + source + "</doc_url>\n\t<crawl_id>" + crawl_id
		+ "</crawl_id>\n\t<publication_id>" + publication_id + "</publication_id>\n";

	String foot = "</xml>\n";

	// first step generate XML using SimpleSheet[]
	SheetsSerializer serializer = new SheetsSerializer(original);
	String auto_xml = serializer.toXML(true);

	auto_xml = head + auto_xml + foot;

	String edit_xml = new String();
	if (edited != null) {
	    serializer = new SheetsSerializer(edited);
	    edit_xml = serializer.toXML(true);
	    edit_xml = head + edit_xml + foot;
	}

	if (reloaded) {
	    return mongo.updateXML(objectId, edit_xml, name, description, ccDetectionPb, attrDetectionPb, detectPb, trashed, dbName);
	} else
		L.Debug(this.getClass().getSimpleName(), "Auto xml " + auto_xml, true);
	    return mongo.insertXML(auto_xml, edit_xml, baseXLSDir + File.separator + source, name, source, user, description, ccDetectionPb, attrDetectionPb, detectPb, trashed, dbName);
    }

    @Override
    public String importDocument(String auto_xml, String edit_xml, String name, String source, String user, String description, String dbName, String crawl_id,
	    String publication_id, boolean reloaded, String objectId, boolean ccDetectionPb, boolean attrDetectionPb, String detectPb, boolean trashed) {

	String head = "<xml>\n\t<user_id>" + user + "</user_id>\n\t<doc_url>" + source + "</doc_url>\n\t<crawl_id>" + crawl_id
		+ "</crawl_id>\n\t<publication_id>" + publication_id + "</publication_id>\n";

	String foot = "</xml>\n";

	auto_xml = head + auto_xml + foot;
	edit_xml = head + edit_xml + foot;

	if (reloaded) {
	    return mongo.updateXML(objectId, edit_xml, name, description, ccDetectionPb, attrDetectionPb, detectPb, trashed, dbName);
	} else
	    return mongo.insertXML(auto_xml, edit_xml, baseXLSDir + File.separator + source, name, source, user, description, ccDetectionPb, attrDetectionPb, detectPb, trashed, dbName);
    }

    @Override
    public String getDocument(String object_id, String dbName) {
	return mongo.getEditedXML(object_id, dbName);
    }

    @Override
    public List<SchemaData> getDocuments(boolean onlyEdited, String dbName) {
	return mongo.getSchemas(onlyEdited, dbName);
    }

    @Override
    public String getFileName(String object_id, String dbName) {
	String fileName = new String();

	// get the inpustream and create the file
	fileName = mongo.getFileNameForObject(object_id, baseXLSDir, dbName);
	//
	
	return fileName;
    }

    public MongoDBConnector getMongo() {
        return mongo;
    }

    public void setMongo(MongoDBConnector mongo) {
        this.mongo = mongo;
    }

    public String getBaseXLSDir() {
        return baseXLSDir;
    }

    public void setBaseXLSDir(String baseXLSDir) {
        this.baseXLSDir = baseXLSDir;
    }
}
