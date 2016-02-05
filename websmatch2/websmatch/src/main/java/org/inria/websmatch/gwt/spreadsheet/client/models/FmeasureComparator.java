package org.inria.websmatch.gwt.spreadsheet.client.models;

import java.util.Comparator;

public class FmeasureComparator implements Comparator<DetectionQualityData> {

    public FmeasureComparator() {
    }

    @Override
    public int compare(DetectionQualityData arg0, DetectionQualityData arg1) {
	/*int result = new Double(arg0.getFmeasure()).compareTo(arg1.getFmeasure());

	if (result == 0)
	    result = new Double(arg0.getFmeasure()).compareTo(arg1.getFmeasure());*/
	
	int result = new Double(arg1.getFmeasure()).compareTo(arg0.getFmeasure());

	if (result == 0)
	    result = new Double(arg1.getFmeasure()).compareTo(arg0.getFmeasure());

	return result;
    }
}
