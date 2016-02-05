package org.inria.websmatch.tests.bio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.inria.websmatch.parsers.OboParser;
import org.inria.websmatch.parsers.Term;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.parsers.OboParser;
import org.inria.websmatch.parsers.Term;
import org.inria.websmatch.utils.L;

public class ExportTermToCSV {

    public static void main(String[] args) {

	// load an obo terminology
	String oboPath = "/home/manu/Bureau/tropgene_test1/plant_ontology.obo.txt";
	OboParser obo = new OboParser(oboPath);
	obo.parse();

	String csvPath = "/home/manu/Bureau/tropgene_test1/plant_ontology.obo.csv";
	File csvFile = new File(csvPath);
	try {
	    if (!csvFile.createNewFile()) {
		csvFile.delete();
		csvFile.createNewFile();
	    }
	} catch (IOException e) {
	    L.Error(e.getMessage(), e);
	}

	BufferedWriter writer = null;

	try {
	    writer = new BufferedWriter(new FileWriter(csvFile));
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}

		System.out.println("Terms size : "+obo.getTerms().size());
	
	List<Term> terms = obo.getTerms();
	for (Term t : terms) {
	    try {
		writer.write("\"" + t.getId() + "\";\"" + t.getName() + "\";\"" + t.getDef().replaceAll("\"", "") + "\";\"plant\"\n");
		if (t.getSynonyms() != null && t.getSynonyms().size() > 0) {
		    List<String> syns = t.getSynonyms();
		    for (int i = 0;i < syns.size();i++) {
			String s = syns.get(i);
			writer.write("\"" + t.getId() + "_"+(i+1)+"\";\"" + s + "\";\"" + t.getDef().replaceAll("\"", "") + "\";\"plant\"\n");
		    }
		}
	    } catch (IOException e) {
		L.Error(e.getMessage(),e);
	    }
	}
	try {
	    writer.flush();
	    writer.close();
	} catch (IOException e) {
	    L.Error(e.getMessage(),e);
	}
    }
}
