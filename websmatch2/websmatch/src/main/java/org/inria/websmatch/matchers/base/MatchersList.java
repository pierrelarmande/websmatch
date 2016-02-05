package org.inria.websmatch.matchers.base;

import java.io.IOException;
import java.io.InputStream;

import org.inria.websmatch.utils.L;
import org.inria.websmatch.utils.L;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class MatchersList {
    
    private String[] matchers;

    private static MatchersList instance;

    public static MatchersList getInstance() {
	if (null == instance) {
	    instance = new MatchersList();
	}
	return instance;
    }

    private MatchersList() {

	InputStream is = getClass().getClassLoader().getResourceAsStream("org/inria/websmatch/matchers/matchersList.xml");
	SAXBuilder sxb = new SAXBuilder();
	try {
	    Document doc = sxb.build(is);
	    Element root = doc.getRootElement();
	    setMatchers(root.getChildText("matchers").split(";"));	    
	} catch (JDOMException e) {
	    L.Error(e.getMessage(), e);
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}

    }

    public void setMatchers(String[] matchers) {
	this.matchers = matchers;
    }

    public String[] getMatchers() {
	return matchers;
    }

}
