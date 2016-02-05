package org.inria.websmatch.matchers.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Normalizer;

import org.inria.websmatch.utils.L;

import tools.wordnet.WordNetHelper;

public class AttributeMatcher {

    private String matcherName;

    public AttributeMatcher(String matcherName) {
	this.matcherName = matcherName;
	// FIXME
	if(matcherName.equals("LinStoiloisBagForLabel")) WordNetHelper.getInstance().initializeWN("file_properties.xml");
	if(matcherName.equals("Lin")) WordNetHelper.getInstance().initializeWN("file_properties.xml");
    }

    /**
     * Match 2 labels + description
     * 
     * @param att1
     *            Label 1
     * @param att2
     *            Label 2
     * @return The score for the chosen matching technique
     */

    @SuppressWarnings("unchecked")
    public float match(String[] att1, String[] att2) {
	// we normalize strings to remove accents
	String[] label1s = { Normalizer.normalize(att1[0], Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""),
		Normalizer.normalize(att1[1], Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "") };
	String[] label2s = { Normalizer.normalize(att2[0], Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""),
		Normalizer.normalize(att2[1], Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "") };

	@SuppressWarnings("rawtypes")
	Class cl;
	try {
	    if(matcherName.equals("Lin")) cl = Class.forName("simlib.wn." + matcherName);
	    else cl = Class.forName("simlib.label." + matcherName);
	    @SuppressWarnings("rawtypes")
	    Class partypes[] = new Class[2];
	    partypes[0] = String[].class;
	    partypes[1] = String[].class;
	    Method meth;
	    try {
		meth = cl.getMethod("getSimScore", partypes);
	    } catch (SecurityException e1) {
		e1.printStackTrace();
		return 0;
	    } catch (NoSuchMethodException e1) {
		e1.printStackTrace();
		return 0;
	    }
	    Object arglist[] = new Object[2];
	    arglist[0] = label1s;
	    arglist[1] = label2s;
	    try {
		try {
		    float res = (Float) meth.invoke(cl.newInstance(), arglist);
		    if (res > 1)
			res = 1;
		    return res;
		} catch (IllegalArgumentException e) {
		    L.Error(e.getMessage(),e);
		    return 0;
		} catch (InvocationTargetException e) {
		    L.Error(e.getMessage(),e);
		    return 0;
		}
	    } catch (InstantiationException e) {
		L.Error(e.getMessage(),e);
		return 0;
	    } catch (IllegalAccessException e) {
		L.Error(e.getMessage(),e);
		return 0;
	    }
	} catch (ClassNotFoundException e) {
	    L.Error(e.getMessage(),e);
	    return 0;
	}
    }

}
