package org.mitre.harmony.matchers.matchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotoh;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotohWindowedAffine;
import yam.simlib.general.IMetric;
import yam.simlib.label.MultiLevelMatcher;
import yam.simlib.label.SoftTFIDFWordNet;
import yam.simlib.label.SoftTFIDFWrapper;
import yam.simlib.name.edit.Stoilos_JW;
import yam.simlib.name.edit.wrapper.SMEditWrapper;
import yam.simlib.name.hybrid.WNWrapper;
import yam.simlib.name.token.SMTokenWrapper;
import yam.simlib.wn.Lin;
import yam.simlib.wn.WuPalmer;

/**
 * @author ngoduyhoa
 * factory of YAM similarity metrics wrapper
 */
public class YAMMatcherWrapperFactory 
{
	private	static YAMMatcherWrapperFactory	instance	=	null;
	
	// store matchers taken by wrapping YAM metrics
	public Map<String, Matcher>	matchers;
	
	private YAMMatcherWrapperFactory()
	{
		this.matchers	=	new HashMap<String, Matcher>();
	}
	
	public static YAMMatcherWrapperFactory getInstance()
	{
		if(instance == null)
		{
			instance	=	new YAMMatcherWrapperFactory();		
			
			instance.addWrappedYAMMatcher();
		}
		
		return instance;
	}
	
	// get matcher by name
	public Matcher getMatcherByName(String name)
	{
		if(matchers.containsKey(name))
			return matchers.get(name);
		
		return null;
	}
	
	private	void addWrappedYAMMetric(IMetric metric)
	{
	    YAMMatcherWrapper matcher = new YAMMatcherWrapper();
	    matcher.setMetric(metric);
		matchers.put(metric.getMetricName(), matcher);
	}
	
	public void addWrappedYAMMatcher()
	{		
		List<IMetric>	metrics	=	new ArrayList<IMetric>();
		
		// Group 1: Based on edit distance approaches for NAME
		
		metrics.add(new SMEditWrapper(new Levenshtein()));
		
		metrics.add(new SMEditWrapper(new SmithWaterman()));
		
		metrics.add(new SMEditWrapper(new SmithWatermanGotoh()));
		metrics.add(new SMEditWrapper(new SmithWatermanGotohWindowedAffine()));
		
		metrics.add(new SMEditWrapper(new Jaro()));
		metrics.add(new SMEditWrapper(new JaroWinkler()));		
		metrics.add(new Stoilos_JW());
		
		// Group 2: Based on Bag-of-tokens approaches for NAME
		
		metrics.add(new SMTokenWrapper(new QGramsDistance()));
		metrics.add(new SMTokenWrapper(new MongeElkan()));
		
		// Group 3: Hybrid - based approaches (integrating with WordNet) for NAME
		
		metrics.add(new WNWrapper(new WuPalmer()));
		metrics.add(new WNWrapper(new Lin()));
		
		// Group 3: SoftTFIDF- based approaches (integrating with WordNet) for LABEL
		
		metrics.add(new MultiLevelMatcher());
		metrics.add(new SoftTFIDFWrapper());
		metrics.add(new SoftTFIDFWordNet());
		
		// add to map
		for(IMetric metric : metrics)
		{
			//System.out.println(metric.getMetricName());
			addWrappedYAMMetric(metric);
		}
	}
	
	// get all metrics name
	public static void main(String[] args) 
	{
		// YAMMatcherWrapperFactory	instance	=	YAMMatcherWrapperFactory.getInstance();
	}
}

/*
Levenshtein
SmithWaterman
SmithWatermanGotoh
SmithWatermanGotohWindowedAffine
Jaro
JaroWinkler
Stoilos_JW
QGramsDistance
MongeElkan
WuPalmer
Lin
MultiLevelMatcher
SoftTFIDF
SoftTFIDFWordNet
*/
