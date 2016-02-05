package org.inria.websmatch.matchers.mappings;

import org.inria.websmatch.matchers.base.AttributeMatcher;
import org.inria.websmatch.parsers.N3Parser;
import org.inria.websmatch.parsers.OboParser;
import org.inria.websmatch.parsers.Term;

import javax.json.JsonArray;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class N3Annotator{

	// threshold
	private float threshold = 0.8f;
	// files
	private String n3File = "";
	private String oboFile = "";
	private int eventCpt = 0;
	private int maxMatch = 0;

	public N3Annotator(String lf, String rf) {
		n3File = lf;
		oboFile = rf;
	}

	public List<String[]> annotateN3File(boolean withDef, AnnotatorService annotator, String ontoId) throws UnsupportedEncodingException {

		// results
		List<String[]> results = new ArrayList<>();

		// load n3 file
		N3Parser n3 = new N3Parser(n3File);
		n3.parse();
		List<String> labels = n3.getMapObjects();

		// match with collection matcher (instance based)
		// CollectionMatcher matcher = new CollectionMatcher();
		AttributeMatcher matcher = new AttributeMatcher("GeneralBagForLabel");

		// load obo grammar file if needed
		List<Term> oboTerms = new ArrayList<Term>();
		if (annotator==null) {
			OboParser obo = new OboParser(oboFile);
			obo.parse();
			oboTerms = obo.getTerms();
		}

		// run the matching
		//     Event
		eventCpt = 0;
		maxMatch = labels.size();

		for (String label : labels) {

			synchronized (this) {
				eventCpt++;
			}
			// try to get the annotator proposal
			JsonArray bioAnnotation = null;
			if (annotator!=null) {
				bioAnnotation = annotator.annotate(label, ontoId, 5, null);
				if (bioAnnotation.size() > 0) {
					String[] tmpResToAdd = new String[2];
					tmpResToAdd[0] = label;
					tmpResToAdd[1] = bioAnnotation.getJsonObject(0).getJsonObject("annotatedClass").getString("@id");
					results.add(tmpResToAdd);
				}
			}
			//

			else {
				float bestScoreForTerm = 0;
				Term bestTerm = null;

				for (Term tmpTerm : oboTerms) {

					float score = 0.0f;

					if (withDef)
						score = matcher.match(new String[] { tmpTerm.getName(), tmpTerm.getDef() }, new String[] { label, "" });
					else
						score = matcher.match(new String[] { tmpTerm.getName(), "" }, new String[] { label, "" });

					if (score > bestScoreForTerm || (score == bestScoreForTerm && score > 0 && tmpTerm.getName().length() > bestTerm.getName().length())) {
						bestScoreForTerm = score;
						bestTerm = tmpTerm;
					}
					// if synonyms
					if (tmpTerm.getSynonyms().size() > 0) {
						List<String> synList = tmpTerm.getSynonyms();
						for (String s : synList) {
							if (withDef)
								score = matcher.match(new String[] { s, tmpTerm.getDef() }, new String[] { label, "" });
							else
								score = matcher.match(new String[] { s, "" }, new String[] { label, "" });
							if (score > bestScoreForTerm) {
								bestScoreForTerm = score;
								bestTerm = tmpTerm;
							}
						}
					}
				}

				if (bestScoreForTerm > threshold) {
					String[] tmpResToAdd = new String[2];
					tmpResToAdd[0] = label;
					tmpResToAdd[1] = bestTerm.getId();

					results.add(tmpResToAdd);
				}
			}
		}
		return results;
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
