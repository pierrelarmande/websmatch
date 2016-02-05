package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;

import com.googlecode.gchart.client.GChart;

public class EvaluationChart extends GChart {

    final String[] barLabels = { "Pre", "Rec", "Fme" };
    final String[] legendLabels = { "Precision", "Recall", "Fmeasure" };
    final String[] barColors = { "red", "blue", "green" };

    final double MAX_SCORE = 1.0;
    final int WIDTH = 200;
    final int HEIGHT = 150;

    public EvaluationChart(ArrayList<String> res, boolean legend) {

	setChartSize(WIDTH, HEIGHT);
	setChartTitle("");
	setChartFootnotes("<b><big>" + res.get(0) + "</big></b></br>");

	for (int iCurve = 0; iCurve < barLabels.length; iCurve++) {
	    addCurve(); // one curve per quarter
	    getCurve().getSymbol().setSymbolType(SymbolType.VBAR_SOUTHWEST);
	    getCurve().getSymbol().setBackgroundColor(barColors[iCurve]);
	    getCurve().setLegendLabel(legendLabels[iCurve]);
	    getCurve().getSymbol().setHovertextTemplate(GChart.formatAsHovertext(barLabels[iCurve] + " = ${y}"));
	    getCurve().getSymbol().setModelWidth(1.0);
	    getCurve().getSymbol().setBorderColor("black");
	    getCurve().getSymbol().setBorderWidth(1);

	    double value = 0.0;
	    
	    try{
		value = Double.parseDouble(res.get(iCurve+1));	
	    }catch(NumberFormatException e){
		
	    }
    
	    getCurve().addPoint(iCurve+2, value);
	    getCurve().getPoint().setAnnotationText(barLabels[iCurve]);
	    getCurve().getPoint().setAnnotationLocation(AnnotationLocation.SOUTH);
	}
	
	getCurve().addPoint(5,0);

	getXAxis().clearTicks();
	getYAxis().setTickCount(6);
	getXAxis().setAxisMin(0);

	getYAxis().setAxisMin(0);
	getYAxis().setAxisMax(MAX_SCORE);
	getYAxis().setTickCount(11);
	getYAxis().setHasGridlines(true);
	
	if(legend){
	    setLegendLocation(LegendLocation.OUTSIDE_LEFT);
	    setLegendVisible(true);
	    setChartTitle("<b><big><big>" + res.get(res.size()-1)+ "</big></big></b></br>");
	}
	else setLegendVisible(false);
	addStyleName("chart");
	
	update();

    }

}
