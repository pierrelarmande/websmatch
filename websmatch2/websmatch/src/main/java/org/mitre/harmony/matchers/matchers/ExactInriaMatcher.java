package org.mitre.harmony.matchers.matchers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.inria.websmatch.utils.L;
import org.mitre.harmony.matchers.MatcherOption;
import org.mitre.harmony.matchers.MatcherOption.OptionType;
import org.mitre.harmony.matchers.MatcherScore;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.schemastore.model.SchemaElement;

/** Exact INRIA Matcher Class */
public class ExactInriaMatcher extends Matcher {

    private HashMap<String, String> couples;

    /** Returns the name of the matcher */
    public String getName() {
	return "Exact Inria Matcher";
    }

    /** Returns the list of options associated with the bag matcher */
    public ArrayList<MatcherOption> getMatcherOptions() {
	ArrayList<MatcherOption> options = new ArrayList<MatcherOption>();
	options.add(new MatcherOption(OptionType.CHECKBOX, NAME, "true"));
	options.add(new MatcherOption(OptionType.CHECKBOX, HIERARCHY, "false"));
	return options;
    }

    /** Generate scores for the exact matches */
    private MatcherScores getExactMatches() {
	// Get the source and target elements
	ArrayList<SchemaElement> sourceElements = schema1.getFilteredElements();
	ArrayList<SchemaElement> targetElements = schema2.getFilteredElements();
	
	//System.out.println(sourceElements.size());
	//System.out.println(targetElements.size());

	// Sets the current and total comparisons
	// used by the progress bar in UI
	completedComparisons = 0;
	totalComparisons = sourceElements.size() * targetElements.size();

	// Find all exact matches
	MatcherScores scores = new MatcherScores(100.0);
	for (SchemaElement sourceElement : sourceElements) {
	    for (SchemaElement targetElement : targetElements) {
		System.out.println("Source : "+sourceElement.getName()+ "\tTarget : "+targetElement.getName());
		
		    if ((couples.get(sourceElement.getName() + ";" + targetElement.getName()) != null) || (couples.get(targetElement.getName() + ";" + sourceElement.getName()) != null))
			scores.setScore(sourceElement.getId(), targetElement.getId(), new MatcherScore(100.0, 100.0));
		   	
	    completedComparisons++;
	    }
	}
	return scores;
    }

    /** Generates scores for the specified elements */
    public MatcherScores match() {
	couples = new HashMap<String, String>();
	// read the file and save couples
	try {
	    BufferedReader in = new BufferedReader(new FileReader("/home/manu/Bureau/desc.txt"));

	    String line = new String();
	    try {
		while ((line = in.readLine()) != null) {

		    couples.put(line, "match");
		    System.out.println(line);
		    
		}
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		L.Error(e.getMessage(),e);
	    }
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}

	// Don't proceed if neither "name" nor "description" option selected
	if (!options.get(NAME).isSelected()) {
	    return new MatcherScores(100.0);
	}

	// Generate the matches
	return getExactMatches();
	
    }
}
