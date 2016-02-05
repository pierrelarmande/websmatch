package org.inria.websmatch.matchers.base;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.inria.websmatch.utils.L;

import com.google.common.collect.Maps;

import svd.VectorUtils;
import system.IRModel;
import system.Configs.WeightTypes;

public class CollectionMatcher {

    public CollectionMatcher() {
	
    }

    /**
     * Match 2 columns (name + values) using instance based matcher
     * 
     * @param nameAndData1 First column
     * @param nameAndData2 Second column
     * @return Score between 0 and 1
     */
    public float match(String[] nameAndData1, String[] nameAndData2) {

	Map<String, String> table1 = Maps.newHashMap();
	table1.put(nameAndData1[0], nameAndData1[1]);

	Map<String, String> table2 = Maps.newHashMap();
	table2.put(nameAndData2[0], nameAndData2[1]);

	IRModel model = new IRModel(true);//.getInstance(true);

	Iterator<Map.Entry<String, String>> it1 = table1.entrySet().iterator();
	while (it1.hasNext()) {
	    Map.Entry<String, String> entry1 = (Map.Entry<String, String>) it1.next();
	    model.addDocument(entry1.getKey(), entry1.getValue());
	}

	Iterator<Map.Entry<String, String>> it2 = table2.entrySet().iterator();
	while (it2.hasNext()) {
	    Map.Entry<String, String> entry2 = (Map.Entry<String, String>) it2.next();
	    model.addDocument(entry2.getKey(), entry2.getValue());
	}

	model.optimize();

	try {
	    // built term-document matrix from index directory
	    // it will be used for finding document vector for each concept uri
	    model.buildMatrix(WeightTypes.TFIDF);
	} catch (Exception e) {
	    L.Error(e.getMessage(),e);
	    return 0;
	}

	Set<String> keys1 = table1.keySet();
	Set<String> keys2 = table2.keySet();

	for (String key1 : keys1) {
	    // get document vector for the element
	    float[] vector1 = model.getTermVector(key1);

	    for (String key2 : keys2) {
		// get document vector for the element
		float[] vector2 = model.getTermVector(key2);

		if (vector1 != null && vector2 != null) {
		   float res = VectorUtils.CosineMeasure(vector1, vector2);
		   if(res > 1) res = 1; 
		   return res;
		}
	    }
	}
	return 0;
    }
}
