package org.inria.websmatch.gwt.spreadsheet.client.handlers;

import java.util.Set;
import java.util.TreeMap;

import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingResult;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingScores;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.GradientScore;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.TwoWayCanvas;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

@SuppressWarnings("deprecation")
public class TwoWayMouseOverHandler implements MouseMoveHandler {

    private PopupPanel pop;
    private MatchingResultsServiceAsync service = (MatchingResultsServiceAsync) GWT.create(MatchingResultsService.class);
    private TwoWayCanvas currentCanvas = null;
    private MatchingResult lastResult = null;

    @Override
    public void onMouseMove(final MouseMoveEvent moe) {
	final TwoWayCanvas widget = (TwoWayCanvas) moe.getSource();
	currentCanvas = widget;
	final int clientx = moe.getClientX();
	final int clienty = moe.getClientY();
	int x = moe.getX();
	int y = moe.getY();

	final MatchingResult result = widget.getNodeAt(x, y);

	if (result != null && lastResult != result) {
	    // ok get all scores for this couple
	    lastResult = result;
	    
	    service.getScores(result.getId_element1(), result.getId_schema1(), result.getId_element2(), result.getId_schema2(),
		    new AsyncCallback<MatchingScores>() {

			@Override
			public void onFailure(Throwable caught) {

			    if (pop != null && pop.isShowing())
				pop.hide();
			    // pop up with only this score
			    VerticalPanel vertical = new VerticalPanel();
			    vertical.add(new HTML("<b>" + result.getLeftElementName().replaceAll("\\\\'", "'") + "&nbsp;-&nbsp;"
				    + result.getRightElementName().replaceAll("\\\\'", "'") + "</b>"));
			    vertical.add(new Label("Score : " + result.getScore()));
			    // vertical.add(new HTML("<b>Expert&nbsp;:&nbsp;" +
			    // result.isExpert()+"</b>"));

			    HorizontalPanel hp = new HorizontalPanel();
			    hp.add(new HTML("<b>Expert&nbsp;:&nbsp;</b>"));
			    final CheckBox expertCheck = new CheckBox();
			    expertCheck.setValue(result.isExpert());

			    handleExpertCheck(expertCheck, result.getId_element1(), result.getId_element2(), result.getId_schema1(), result.getId_schema2());

			    hp.add(expertCheck);
			    vertical.add(hp);

			    pop = new PopupPanel(true);
			    pop.setGlassEnabled(false);
			    pop.setPopupPosition(clientx, clienty);
			    pop.add(vertical);
			    pop.setAutoHideEnabled(true);
			    pop.show();

			}

			@Override
			public void onSuccess(MatchingScores scores) {

			    if (pop != null && pop.isShowing())
				pop.hide();
			    // pop up with all scores
			    TreeMap<String, Double> map = scores.getScores();
			    Set<String> keys = map.keySet();

			    VerticalPanel vertical = new VerticalPanel();
			    HTML title = new HTML("<b>" + result.getLeftElementName().replaceAll("\\\\'", "'") + "&nbsp;-&nbsp;"
				    + result.getRightElementName().replaceAll("\\\\'", "'") + "</b>");

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

			    handleExpertCheck(expertCheck, scores.getId_element1(), scores.getId_element2(), scores.getId_schema1(), scores.getId_schema2());

			    table.setWidget(i, 1, expertCheck);

			    pop = new PopupPanel(true);
			    pop.setGlassEnabled(false);
			    pop.setPopupPosition(clientx, clienty);
			    pop.add(vertical);
			    pop.setAutoHideEnabled(true);
			    pop.show();
			}
		    });
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
