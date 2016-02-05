package org.inria.websmatch.tests.bio;

import org.inria.websmatch.matchers.base.AttributeMatcher;
import org.inria.websmatch.matchers.mappings.AnnotatorService;
import org.inria.websmatch.matchers.mappings.CsvOboMapper;
import org.inria.websmatch.parsers.ManualAnnotationParser;
import org.inria.websmatch.parsers.N3Parser;
import org.inria.websmatch.parsers.OboParser;
import org.inria.websmatch.parsers.Term;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class AnnotationTest {

	public void grammarAndRelationnalTest() {
		// threshold
		float threshold = 0.8f;

		// load obo grammar file
		String oboPath = "/home/manu/Documents/BioSemantic/tropgene_test1/gramene_trait.obo.txt";
		OboParser obo = new OboParser(oboPath);
		obo.parse();

		// System.out.println(obo.getTerms().get(0));
		// System.out.println(obo.getTerms().get(obo.getTerms().size()-1));

		// load n3 relationnal file
		String n3Path = "/home/manu/Documents/BioSemantic/tropgene_test1/mapping-MEDOC-TROPGENE_RICE_sample.n3";
		N3Parser n3 = new N3Parser(n3Path);
		n3.parse();

		// System.out.println(n3.getMapObjects().get(0));
		// System.out.println(n3.getMapObjects().get(n3.getMapObjects().size()-1));

		// match with general bag for label
		AttributeMatcher matcher = new AttributeMatcher("GeneralBagForLabel");

		// run the matching
		List<String> n3Objects = n3.getMapObjects();
		List<Term> oboTerms = obo.getTerms();

		for (String map : n3Objects) {
			for (Term term : oboTerms) {
				float score = matcher.match(new String[] { map, "" }, new String[] { term.getName(), "" });
				if (score > threshold) {
					System.out.println(/* "Score : "+score+ */"\tMatched : " + map + "\t" + term.getName() + "\t" + term.getDef());
				}
				// if synonyms
				if (term.getSynonyms().size() > 0) {

				}
			}
		}
	}

	public static void main(String[] args) {

		if(args == null || args.length < 3){
			System.out.println("Usage : java -jar AnnotationTest csvFile oboFile manualMappingFile");
		}

		String oboPath = args[1];
		String csvPath = args[2];
		String manualMappingFile = args[2];

		CsvOboMapper annot = new CsvOboMapper(csvPath, oboPath);

		//String oboPath = "/home/manu/Documents/BioSemantic/tropgene_test1/gramene_trait.obo.txt";
		//String csvPath = "/home/manu/Documents/BioSemantic/tropgene_test1/table_trait.csv";

		// grammarAndRelationnalTest();
		// List<String[]> results = annot.grammarAndInstancesCSVMapping(1,2);
		String results = null;
		try {
			results = annot.grammarAndInstancesCSVMapping(1,2,AnnotatorService.IBC,null,5,null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		//System.out.println("Matches found : " + results.size());
		System.out.println(results);

		// load manual annotation file and check
		//ManualAnnotationParser manual = new ManualAnnotationParser("/home/manu/Documents/BioSemantic/tropgene_test1/mapping_manuel-trait.csv");
		ManualAnnotationParser manual = new ManualAnnotationParser(manualMappingFile);
		manual.parse();
		@SuppressWarnings("unused")
		List<String[]> mappings = manual.getMappings();

		// count number of found mappings
	/*System.out.println("////////");
	System.out.println("Manual mappings : " + mappings.size());
	System.out.println("////////");
	//
	int count = 0;
	for (String[] map : mappings) {
	    String[] newMap = annot.elementReplacer(map);
	    for (String[] match : results) {
		String[] newMatch = annot.elementReplacer(match);
		if (newMap[0].trim().equals(newMatch[0].trim()) && newMap[1].trim().equals(newMatch[1].trim())) {

		    //
		    System.out.println(map[0] + ";" + map[1] + "\t" + match[0] + ";" + match[1]);
		    //

		    count++;
		}
	    }
	}
	System.out.println("Matches found from mappings : " + count);*/

	}
}
