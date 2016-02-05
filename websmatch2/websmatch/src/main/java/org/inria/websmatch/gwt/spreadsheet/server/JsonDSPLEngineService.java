package org.inria.websmatch.gwt.spreadsheet.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.http.HttpServlet;

import org.apache.commons.lang.StringEscapeUtils;
import org.inria.websmatch.dspl.DSPLSlice;
import org.inria.websmatch.dsplEngine.DSPLEngineSerializer;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xml.WSMatchXMLtoDSPLLoader;
import org.inria.websmatch.dspl.DSPLSlice;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

public class JsonDSPLEngineService extends HttpServlet {
    
    /**
     * 
     */
    private static final long serialVersionUID = 4834589751711740761L;


    private static String baseXLSDir = "/var/www/xls";

    // DSPL things
    public static String dsplDir = "/var/www/dspl";

    public void init() {
	//baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");
	//dsplDir = getServletContext().getInitParameter("dsplDir");
    }

    public static String getXMLDoc(HashMap<String, String> data, SimpleSheet[] sheets, String dataSetRef, String dataSetName, String dataSetDescription,
	    String editorDataSetName, String editorDataSetDescription, String bidimValueName) {

	String doc_url = new String();
	@SuppressWarnings("unused")
	String crawl_id = new String();
	@SuppressWarnings("unused")
	String publication_id = new String();
	@SuppressWarnings("unused")
	String user_id = new String();

	if (data != null) {
	    doc_url = data.get("doc_url");
	    if(data.get("crawl_id")!=null) crawl_id = data.get("crawl_id");
	    if(data.get("publication_id")!=null) publication_id = data.get("publication_id");
	    if(data.get("user_id")!=null) user_id = data.get("user_id");
	}
	
	// first get the XML document in WebSmatch format (needed to get slices from DSPL)
	// check if dir exists
	File dir = new File(baseXLSDir + File.separator + "generatedXML");
	String filePath = new String();
	
	if (!dir.exists())
	    dir.mkdir();

	String xmlFileName = sheets[0].getFilename().substring(0, sheets[0].getFilename().lastIndexOf(".")) + ".xml";

	// ok create and fill the file
	File outputFile = new File(baseXLSDir + File.separator + "generatedXML" + File.separator + xmlFileName);

	if (outputFile.exists())
	    outputFile.delete();

	L.Debug(JsonDSPLEngineService.class.getSimpleName(),outputFile.getAbsolutePath(),true);

	try {
	    if (outputFile.createNewFile()) {

		// ok continue
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		writer.write(JsonSpreadsheetParsingService.getXMLDoc(data, sheets, true));
		writer.close();
		filePath = outputFile.getAbsolutePath();

	    }
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
	// end XML creation
	

	String doc = "<?xml version=\"1.0\" ?><config reference=\""+StringEscapeUtils.escapeXml(dataSetRef)+"\"><descriptor filename=\"dataset.xml\">";
	doc += "<dataset><name>"+StringEscapeUtils.escapeXml(dataSetName)+"</name>";
        doc += "<description>"+StringEscapeUtils.escapeXml(dataSetDescription)+"</description>";
        doc += "<url>"+doc_url+"</url></dataset>";
            
        doc += "<editor><name>"+StringEscapeUtils.escapeXml(editorDataSetName)+"</name>";
        doc += "<description>"+StringEscapeUtils.escapeXml(editorDataSetDescription)+"</description>";
        L.Debug(JsonDSPLEngineService.class.getSimpleName(),"Doc url is "+doc_url,true);
        if(doc_url.indexOf("http://") != -1) doc += "<url>"+doc_url.substring(0,doc_url.indexOf("/", 8))+"</url></editor>";
        else doc += "<url></url></editor>";
        
        // get slices
        WSMatchXMLtoDSPLLoader DSPLLoader = new WSMatchXMLtoDSPLLoader(filePath);
        LinkedList<DSPLSlice> slices = DSPLLoader.getSlices();
        if(bidimValueName != null && !bidimValueName.equals("")){
            slices.getFirst().getColumns().getLast().setId(bidimValueName);
        }

	// ok we had the tables and datas for detected attributes
        // needed to know if attr in lines or by dim

        if(doc_url.indexOf("http://") != -1){
            DSPLEngineSerializer serializer = new DSPLEngineSerializer(doc_url, slices);
            doc += serializer.toXML();
        }else{
            DSPLEngineSerializer serializer = new DSPLEngineSerializer(baseXLSDir+File.separator+doc_url, slices);
            doc += serializer.toXML();
        }
	
	return doc;
    }
   
}
