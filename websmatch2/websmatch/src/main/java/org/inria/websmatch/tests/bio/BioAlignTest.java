package org.inria.websmatch.tests.bio;

import org.inria.websmatch.matchers.mappings.OntoAlignmentProposal;
import org.inria.websmatch.matchers.mappings.OntoAlignmentProposal;


public class BioAlignTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
	
	if(args == null || args.length < 3){
	    System.out.println("Usage : java -jar BioAlignTest file1.rdf file2.rdf booleanWithDef");
	}
	
	//String beanPath = "/home/manu/Documents/BioAlign/cowpea.rdf";
	//String chickpeaPath = "/home/manu/Documents/BioAlign/pigeonpea.rdf";

	String lPath = args[0];
	String rPath = args[1];

	boolean withDef = false;
	if(args[2].equals("true")) withDef = true;
	
	OntoAlignmentProposal align = new OntoAlignmentProposal(lPath, rPath, 0.8f);
	System.out.println(align.alginFiles(withDef));
    }
}
