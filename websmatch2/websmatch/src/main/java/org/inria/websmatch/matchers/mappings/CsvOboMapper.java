package org.inria.websmatch.matchers.mappings;

import org.inria.websmatch.matchers.base.AttributeMatcher;
import org.inria.websmatch.parsers.CSVParser;
import org.inria.websmatch.parsers.OboParser;
import org.inria.websmatch.parsers.Term;

import javax.json.JsonArray;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CsvOboMapper{

	// threshold
	private float threshold = 0.7f;
	// files
	private String csvFile = "";
	private String oboFile = "";
	private int eventCpt = 0;
	private int maxMatch = 0;

	public CsvOboMapper(String lf, String rf) {
		csvFile = lf;
		oboFile = rf;
	}

	public String grammarAndInstancesCSVMapping(int labelPlace, int descPlace, AnnotatorService annotator, String ontoId, int maxLevel, final String scoreMethod) throws UnsupportedEncodingException {

		// results
		List<String[]> results = new ArrayList<>();
		StringBuffer buffered = new StringBuffer();

		// load obo grammar file
		OboParser obo = new OboParser(oboFile);
		if (!oboFile.endsWith("/"))
			obo.parse();

		// load csv file
		CSVParser csv = new CSVParser(csvFile);
		csv.parse();

		// match with collection matcher (instance based)
		// CollectionMatcher matcher = new CollectionMatcher();
		AttributeMatcher matcher = new AttributeMatcher("GeneralBagForLabel");

		// run the matching
		List<Term> oboTerms = obo.getTerms();
		List<String[]> instances = csv.getInstances();
		int matchCount = 0;

		// Event
		eventCpt = 0;
		maxMatch = instances.size();
		//

		for (String[] origInst : instances) {

			synchronized (this) {
				eventCpt++;
			}
			// try to get the annotator proposal
			JsonArray bioAnnotation = null;
			if (annotator!=null)
				bioAnnotation = annotator.annotate(origInst[labelPlace] + " " + origInst[descPlace],
				                                   ontoId, maxLevel, scoreMethod);


			float bestScoreForTerm = 0;
			Term bestTerm = null;

			// for syn of elements
			String[] instance = elementReplacer(origInst);
			//

			for (Term tmpTerm : oboTerms) {

				float score = matcher.match(new String[] { tmpTerm.getName(), tmpTerm.getDef() }, new String[] { instance[labelPlace], instance[descPlace] });

				if (score > bestScoreForTerm || (score == bestScoreForTerm && score > 0 && tmpTerm.getName().length() > bestTerm.getName().length())) {
					bestScoreForTerm = score;
					bestTerm = tmpTerm;
				}
				// if synonyms
				if (tmpTerm.getSynonyms().size() > 0) {
					List<String> synList = tmpTerm.getSynonyms();
					for (String s : synList) {
						score = matcher.match(new String[] { s, tmpTerm.getDef() }, new String[] { instance[labelPlace], instance[descPlace] });
						if (score > bestScoreForTerm) {
							bestScoreForTerm = score;
							bestTerm = tmpTerm;
						}
					}
				}
			}
			// no obo case
			if (oboFile.endsWith("/")) {
				for (int i = 0; i < origInst.length; i++) {
					buffered.append(origInst[i] + ";");
				}

				if (bioAnnotation.size() > 0) {
					buffered.append(bioAnnotation.getJsonObject(0).getJsonObject("annotatedClass").getString("@id"));
					results.add(new String[] { origInst[labelPlace].trim(), bioAnnotation.getJsonObject(0).getJsonObject("annotatedClass").getString("@id") });
				}
				buffered.append("\n");

				matchCount++;
			}
			// obo case
			else if (bestScoreForTerm > threshold) {
				for (String anOrigInst : origInst)
					buffered.append(anOrigInst + ";");

				buffered.append(bestTerm.getId() + ";" + bestTerm.getName());
				if (annotator!=null && bioAnnotation.size()>0)
					buffered.append(";" + bioAnnotation.getJsonObject(0).getJsonObject("annotatedClass").getString("@id"));
				buffered.append("\n");

				matchCount++;
				if (annotator!=null) {
					if (bioAnnotation.size() > 0) {
						results.add(new String[] { origInst[labelPlace].trim(),
								bestTerm.getName().trim() + ";" + bioAnnotation.getJsonObject(0).getJsonObject("annotatedClass").getString("@id") });
					}
				}else{
					results.add(new String[] { origInst[labelPlace].trim(),bestTerm.getName().trim()});
				}
			}
		}
		//L.Debug(this.getClass().getSimpleName(), buffered.toString(), true);
		return buffered.toString();
	}

	public String[] elementReplacer(String[] instance) {

		String[] newInstance = new String[instance.length];
		for (int i = 0; i < newInstance.length; i++) {
			newInstance[i] = instance[i];
		}

		for (int i = 0; i < newInstance.length; i++) {
			if (newInstance[i].indexOf("K+") != -1) {
				newInstance[i] = newInstance[i].replaceAll("K\\+", "potassium");
			}
			if (newInstance[i].indexOf("Na+") != -1)
				newInstance[i] = newInstance[i].replaceAll("Na\\+", "sodium");
		}
		return newInstance;
	}

	public void setLeftFile(String leftFile) {
		this.csvFile = leftFile;
	}

	public String getLeftFile() {
		return csvFile;
	}

	public void setRightFile(String rightFile) {
		this.oboFile = rightFile;
	}

	public String getRightFile() {
		return oboFile;
	}

	public void setEventCpt(int eventCpt) {
		this.eventCpt = eventCpt;
	}

	public int getEventCpt() {
		return eventCpt;
	}

	public void setMaxMatch(int maxMatch) {
		this.maxMatch = maxMatch;
	}

	public int getMaxMatch() {
		return maxMatch;
	}

}
