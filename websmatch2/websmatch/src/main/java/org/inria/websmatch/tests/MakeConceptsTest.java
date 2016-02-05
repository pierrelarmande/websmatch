package org.inria.websmatch.tests;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.matchers.base.AttributeMatcher;
import org.inria.websmatch.matchers.base.MatchersList;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.xml.WSMatchXMLLoader;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

import system.Configs;
import tools.wordnet.WordNetHelper;

public class MakeConceptsTest {

    /**
     * @param args
     */
    public static void main(String[] args) {

	System.out.println("Begin : " + new Date());

	// init
	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	}
	try {
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	}
	//

	// load files and match them with concepts
	MongoDBConnector connector = MongoDBConnector.getInstance();

	List<SchemaData> list = connector.getSchemas(false, "datapublica");

	// use a sublist of 100 docs
	SchemaData[] schemas = list.toArray(new SchemaData[/* list.size() */1000]);

	// now we load concepts
	//ArrayList<Concept> concepts = connector.getConcepts("datapublica",5);

	// treshold
	float treshold = 0.6f;

	int cpt = 0;

	for (int i = 0; i < schemas.length-1; i++) {

	    SchemaData s1 = schemas[i];
	    SchemaData s2 = schemas[i + 1];

	    String strDoc1 = connector.getEditedXML(s1.getId(), "datapublica");
	    String strDoc2 = connector.getEditedXML(s2.getId(), "datapublica");

	    // next, match the attributes
	    WSMatchXMLLoader docLoader1 = new WSMatchXMLLoader(strDoc1);
	    Document doc1 = docLoader1.getDocument();

	    WSMatchXMLLoader docLoader2 = new WSMatchXMLLoader(strDoc2);
	    Document doc2 = docLoader2.getDocument();

	    // match attribute names
	    String[] matchers = MatchersList.getInstance().getMatchers();

	    // go throught attributes
	    Filter attributeFilter = new ElementFilter("attribute", null);
	    @SuppressWarnings("unchecked")
	    Iterator<Element> doc1Attr = doc1.getRootElement().getDescendants(attributeFilter);

	    // storage for scores
	    //HashMap<Element[], Float> scores = new HashMap<Element[], Float>();

	    // iterate through attributes
	    // we use an average... to be enhanced
	    while (doc1Attr.hasNext()) {
		Element attr1 = doc1Attr.next();
		@SuppressWarnings("unchecked")
		Iterator<Element> doc2Attr = doc2.getRootElement().getDescendants(attributeFilter);
		while (doc2Attr.hasNext()) {
		    Element attr2 = doc2Attr.next();
		    //
		    float avg = 0;
		    //
		    for (int m = 0; m < matchers.length; m++) {
			AttributeMatcher matcher = new AttributeMatcher(matchers[m]);
			avg = avg + matcher.match(new String[]{attr1.getChildText("name").trim(),""}, new String[]{attr2.getChildText("name").trim(),""});
		    }
		    avg = avg / (float) matchers.length;

		    if (avg > treshold) {
			System.out.println("Same concept : " +attr1.getChildText("name").trim()+ " || " +attr2.getChildText("name").trim());
		    }
		}
	    }

	    cpt++;
	    if (cpt % 10 == 0)
		System.out.println(new Date() + " Cpt match docs : " + cpt);

	}

    }

}
