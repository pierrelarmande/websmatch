package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import com.blogspot.qbeukes.gwt.html5canvas.client.HTML5Canvas;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.widgetideas.graphics.client.CanvasGradient;
import com.google.gwt.widgetideas.graphics.client.Color;

public class GradientScore extends Composite {

    private CanvasGradient gradient;
    private HTML5Canvas canvas;

    public GradientScore(int width, double value) {

	FlowPanel fp = new FlowPanel();
	initWidget(fp);

	canvas = new HTML5Canvas(width, 20);
	canvas.setBackgroundColor(Color.WHITE);

	gradient = canvas.createLinearGradient(2, 2, width - 4, 16);

	gradient.addColorStop(0.0, Color.RED);
	gradient.addColorStop(0.5, Color.YELLOW);
	gradient.addColorStop(1.0, Color.GREEN);

	canvas.setFillStyle(gradient);
	canvas.fillRect(2, 2, width - 4, 16);

	// calculate the position
	double pos = ((double) width - 4.0) * value;

	canvas.setFillStyle(Color.BLACK);
	if(pos < 0) pos = 0.0;
	canvas.fillRect(pos, 1, 3, 18);

	fp.add(canvas);
    }

}
