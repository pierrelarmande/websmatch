package org.inria.websmatch.matchers.mappings;

import java.util.List;

import org.inria.websmatch.matchers.base.AttributeMatcher;
import org.inria.websmatch.parsers.OntoParser;
import org.inria.websmatch.parsers.OntoParserFactory;
import org.inria.websmatch.parsers.Term;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.matchers.base.AttributeMatcher;

/**
 * This class is used to propose alignement on 2 RDFs using Attributes and Defs
 * with GeneralBagForLabel matcher
 * 
 * @author manu
 * 
 */
public class OntoAlignmentProposal {

    // threshold
    private float threshold = 0.8f;
    // files
    private String leftFile = "";
    private String rightFile = "";

    private int eventCpt = 0;
    private int maxMatch = 0;
    
    public OntoAlignmentProposal(String lf, String rf) {
	setLeftFile(lf);
	setRightFile(rf);
    }

    public OntoAlignmentProposal(String lf, String rf, float threshold) {
	this(lf, rf);
	this.threshold = threshold;
    }

    public float getThreshold() {
	return threshold;
    }

    public void setThreshold(float threshold) {
	this.threshold = threshold;
    }

    public void setLeftFile(String leftFile) {
	this.leftFile = leftFile;
    }

    public String getLeftFile() {
	return leftFile;
    }

    public void setRightFile(String rightFile) {
	this.rightFile = rightFile;
    }

    public String getRightFile() {
	return rightFile;
    }

    /**
     * Try to propose alignement between 2 RDF files
     * 
     * @param withDef
     *            Use definition + label (false, only labels)
     * @return Return a String formatted as CSV : Id;Label;Id;Label
     */

    public String alginFiles(boolean withDef) {
	StringBuffer buffered = new StringBuffer();

	OntoParser leftFileParser = OntoParserFactory.createOntoParser(leftFile);
	OntoParser rightFileParser = OntoParserFactory.createOntoParser(rightFile);
	leftFileParser.parse();
	rightFileParser.parse();
	
	AttributeMatcher matcher = new AttributeMatcher("GeneralBagForLabel");

	// run the matching
	List<Term> leftTerms = leftFileParser.getTerms();
	List<Term> rightTerms = rightFileParser.getTerms();

	// Event	
	eventCpt = 0;
	maxMatch = leftTerms.size();
	//
	
	L.Debug(this.getClass().getSimpleName(), "Left terms : "+leftTerms.size(), true);
	L.Debug(this.getClass().getSimpleName(), "Right terms : "+rightTerms.size(), true);
	
	buffered.append("File " + leftFile.substring(leftFile.lastIndexOf('/') + 1) + ";;File " + rightFile.substring(rightFile.lastIndexOf('/') + 1) + ";\n");
	buffered.append("Id;Label;Id;Label\n");

	for (Term lterm : leftTerms) {
	    
	    synchronized(this){
		eventCpt++;
	    }
	    
	    Term[] currentRes = new Term[2];
	    float curScore = 0.0f;
	    for (Term rterm : rightTerms) {
		float score = 0.0f;
		if (withDef)
		    score = matcher.match(new String[] { lterm.getName(), lterm.getDef() }, new String[] { rterm.getName(), rterm.getDef() });
		else
		    score = matcher.match(new String[] { lterm.getName(), "" }, new String[] { rterm.getName(), "" });
		// simple equal case with high score
		if(!withDef && score > threshold && lterm.getName().trim().toLowerCase().equals(rterm.getName().trim().toLowerCase())){
		    curScore = 0.9f;
		    currentRes[0] = lterm;
		    currentRes[1] = rterm;
		}
		//
		else if (score > threshold) {
		    if (curScore < score) {
			curScore = score;
			currentRes[0] = lterm;
			currentRes[1] = rterm;
		    }
		}
	    }
	    if (currentRes[0] != null) {
		buffered.append(currentRes[0].getId() + ";" + currentRes[0].getName() + ";" + currentRes[1].getId() + ";" + currentRes[1].getName()+"\n");
	    }
	}
	//
	return buffered.toString();
    }

    public void setMaxMatch(int maxMatch) {
	this.maxMatch = maxMatch;
    }

    public int getMaxMatch() {
	return maxMatch;
    }

    public void setEventCpt(int eventCpt) {
	this.eventCpt = eventCpt;
    }

    public int getEventCpt() {
	return eventCpt;
    }  
}
