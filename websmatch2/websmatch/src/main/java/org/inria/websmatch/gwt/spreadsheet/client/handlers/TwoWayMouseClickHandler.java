package org.inria.websmatch.gwt.spreadsheet.client.handlers;

import java.util.Set;
import java.util.TreeMap;

import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingScores;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleVertex;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.GradientScore;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.TwoWayCanvas;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.widgetideas.graphics.client.Color;

@SuppressWarnings("deprecation")
public class TwoWayMouseClickHandler implements MouseDownHandler {

    private PopupPanel pop;
    private MatchingResultsServiceAsync service = (MatchingResultsServiceAsync) GWT.create(MatchingResultsService.class);

    private SimpleVertex firstClicked = null;
    private TwoWayCanvas currentCanvas = null;

    @Override
    public void onMouseDown(MouseDownEvent moe) {

	final TwoWayCanvas widget = (TwoWayCanvas) moe.getSource();
	currentCanvas = widget;
	//final int clientx = moe.getClientX();
	//final int clienty = moe.getClientY();
	final int centerX = currentCanvas.getCoordWidth()/2 - 150;
	final int centerY = currentCanvas.getCoordHeight()/2;
	final int x = moe.getX();
	final int y = moe.getY();
	// System.out.println("Clicked x : "+x+" y : "+y);
	final SimpleVertex result = widget.getClickableElementAt(x, y);
	if (result != null) {

	    // System.out.println("Clicked on : " + result.getName());

	    if (firstClicked == null) {
		firstClicked = result;

		widget.fillGraphCompText(firstClicked.getName(), firstClicked.getCanvasX(), firstClicked.getCanvasY(), firstClicked.isRtl(), Color.ORANGE);

	    } else {
		
		if(firstClicked.getSchemaId() == result.getSchemaId()){
		    
		    widget.fillGraphCompText(firstClicked.getName(), firstClicked.getCanvasX(), firstClicked.getCanvasY(), firstClicked.isRtl(), Color.YELLOW);
	 	    		    
		    firstClicked = result;
		    widget.fillGraphCompText(firstClicked.getName(), firstClicked.getCanvasX(), firstClicked.getCanvasY(), firstClicked.isRtl(), Color.ORANGE);
 	    
		}
		
		else {

		    // ok get all scores for this couple

		    service.getScores(new Integer(firstClicked.getId()).intValue(), new Integer(firstClicked.getSchemaId()).intValue(), result.getId(), new Integer(result.getSchemaId()),
			    new AsyncCallback<MatchingScores>() {

				@Override
				public void onFailure(Throwable caught) {

				    Window.alert("Can't get scores for this couple.");

				    widget.fillGraphCompText(firstClicked.getName(), firstClicked.getCanvasX(), firstClicked.getCanvasY(),
					    firstClicked.isRtl(), Color.YELLOW);
				    firstClicked = null;
				}

				@Override
				public void onSuccess(MatchingScores scores) {

				    if (pop != null && pop.isShowing())
					pop.hide();
				    // pop up with all scores
				    TreeMap<String, Double> map = scores.getScores();
				    Set<String> keys = map.keySet();

				    VerticalPanel vertical = new VerticalPanel();
				    HTML title = new HTML("<b>" + firstClicked.getName().replaceAll("\\\\'", "'") + "&nbsp;-&nbsp;"
					    + result.getName().replaceAll("\\\\'", "'") + "</b>");
				    vertical.add(title);

				    FlexTable table = new FlexTable();

				    table.setWidget(1, 0, new HTML("<b>Overall : </b>"));
				    table.setWidget(1, 1, new GradientScore(100, map.get("Overall")));

				    int i = 2;
				    for (String key : keys) {
					// vertical.add(new Label(key+" : " +
					// map.get(key)));
					// HorizontalPanel hPanel = new
					// HorizontalPanel();
					// hPanel.add(new Label(key+" : "));
					// hPanel.add(new
					// GradientScore(100,map.get(key)));
					if (!key.equals("Overall")) {
					    table.setWidget(i, 0, new Label(key + " : "));
					    table.setWidget(i, 1, new GradientScore(100, map.get(key)));
					    i++;
					}
				    }

				    vertical.add(table);
				    // vertical.add(new
				    // HTML("<b>Expert&nbsp;:&nbsp;"+scores.isExpert()+"</b>"));

				    // we add the expert validation
				    table.setWidget(i, 0, new HTML("<b>Expert&nbsp;:&nbsp;</b>"));

				    final CheckBox expertCheck = new CheckBox();
				    expertCheck.setValue(scores.isExpert());

				    handleExpertCheck(expertCheck, scores.getId_element1(), scores.getId_element2(),scores.getId_schema1(), scores.getId_schema2());

				    table.setWidget(i, 1, expertCheck);

				    pop = new PopupPanel(true);
				    pop.setGlassEnabled(false);
				    
				    /*int popx = 0;
				    int popy = 0;
				    
				    if(clientx - 100 > 0) popx = clientx - 100;
				    if(clienty - 150 > 0) popy = clienty - 150;
				    
				    pop.setPopupPosition(popx, popy);*/
				    
				    pop.setPopupPosition(centerX, centerY);
				    
				    pop.add(vertical);
				    pop.setAutoHideEnabled(true);
				    pop.show();

				    widget.fillGraphCompText(firstClicked.getName(), firstClicked.getCanvasX(), firstClicked.getCanvasY(),
					    firstClicked.isRtl(), Color.YELLOW);
				    firstClicked = null;
				}
			    });
		}
	    }
	}

    }

    public void handleExpertCheck(final CheckBox expertCheck, final int id1, final int id2, final int sid1, final int sid2) {	
	// we add the update
	expertCheck.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {

		int bool = (expertCheck.getValue()) ? 1 : 0;

		service.updateExpert(id1, id2, bool, sid1, sid2, new AsyncCallback<Void>() {

		    @Override
		    public void onFailure(Throwable arg0) {
			expertCheck.setValue(!expertCheck.getValue());
		    }

		    @Override
		    public void onSuccess(Void arg0) {
			// we need to draw the edge
			if(currentCanvas != null){
			    currentCanvas.getViewer().loadResults();
			}
		    }

		});
	    }
	});
    }

}
