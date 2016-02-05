package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.EvaluationService;
import org.inria.websmatch.gwt.spreadsheet.client.EvaluationServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.GraphFrame;
import org.inria.websmatch.gwt.spreadsheet.client.composites.MainFrame;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;

public class EvaluationPanel extends PopupPanel {
	public EvaluationPanel(final MainFrame fr) {
	    super(true);
	    setPopupPosition(100, 100);
		setGlassEnabled(true);
		setAnimationEnabled(true);
		
		FlowPanel flowPanel = new FlowPanel();
		setWidget(flowPanel);
		flowPanel.setSize("455px", "115px");
		
		Grid grid = new Grid(3, 2);
		
		Label lblChooseTheView = new Label("Choose the view mode : ");
		lblChooseTheView.setWidth("195px");
		
		final ListBox listBox = new ListBox();
		listBox.setVisibleItemCount(1);
		
		listBox.addItem("By matching technique");
		listBox.addItem("By documents alignment");
		
		grid.setWidget(0, 0, lblChooseTheView);
		grid.setWidget(0, 1, listBox);
		
		Label matchTech = new Label("Choose the matching technique : ");
		
		final ListBox techList = new ListBox();
		techList.setVisibleItemCount(1);
		
		techList.addItem("Stoilos_JW");
		
		// add the techs to listbox
		MatchingResultsServiceAsync service = (MatchingResultsServiceAsync) GWT.create(MatchingResultsService.class);
		    			
		service.getMatchingTechs(new AsyncCallback<List<SimpleMatchTech>>(){

		    @Override
		    public void onFailure(Throwable arg0) {
			// TODO Auto-generated method stub
			
		    }

		    @Override
		    public void onSuccess(List<SimpleMatchTech> arg0) {
			
			for(SimpleMatchTech tech : arg0){
			    if(!tech.getName().equals("Stoilos_JW")) techList.addItem(tech.getName());
			}
			
			techList.addItem("Proba_Max");
			techList.addItem("Proba_Prod");
			techList.setSelectedIndex(0);
			
		    }
		    
		});
		
		grid.setWidget(1, 0, matchTech);
		grid.setWidget(1, 1, techList);
		
		Label documentLabel = new Label("Choose target document for 101 : ");
		
		final ListBox docList = new ListBox();
		docList.setVisibleItemCount(1);
		
		String[] indexes = new String[] { "103", "104", "201", "201-2", "201-4", "201-6", "201-8", "202", "202-2", "202-4", "202-6", "202-8", "203", "204",
			    "205", "206", "207", "208", "209", "210", "221", "222", "223", "224", "225", "228", "230", "231", "232", "233", "236", "237", "238", "239", "240",
			    "241", "246", "247", "248", "248-2", "248-4", "248-6", "248-8", "249", "249-2", "249-4", "249-6", "249-8", "250", "250-2", "250-4", "250-6",
			    "250-8", "251", "251-2", "251-4", "251-6", "251-8", "252", "252-2", "252-4", "252-6", "252-8", "253", "253-2", "253-4", "253-6", "253-8", "254",
			    "254-2", "254-4", "254-6", "254-8", "257", "257-2", "257-4", "257-6", "257-8", "258", "258-2", "258-4", "258-6", "258-8", "259", "259-2", "259-4",
			    "259-6", "259-8", "260", "260-2", "260-4", "260-6", "260-8", "261", "261-2", "261-4", "261-6", "261-8", "262", "262-2", "262-4", "262-6", "262-8",
			    "265", "266", "301", "302", "303", "304" };
		
		for(String index : indexes){
		    docList.addItem(index);
		}
		
		docList.setSelectedIndex(0);
		docList.setEnabled(false);
		
		grid.setWidget(2, 0, documentLabel);
		grid.setWidget(2, 1, docList);
		
		final EvaluationPanel fPan = this;
		
		Button generateGraph = new Button("Generate charts");
		generateGraph.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent arg0) {
			    
			    fPan.hide();
			    final GraphFrame gFrame = new GraphFrame();
			    fr.setMainWidget(gFrame);
			    gFrame.showWaitingPanel();
			    
			    EvaluationServiceAsync service = (EvaluationServiceAsync) GWT.create(EvaluationService.class);
			    
			    boolean byTech = false;
			    boolean byDoc = false;
			    
			    if(listBox.getSelectedIndex() == 0){
				byTech = true;
			    }else{
				byDoc = true;
			    }
			    
			    String tech = new String();
			    String target = new String();
			    
			    if(byTech){
				tech = techList.getItemText(techList.getSelectedIndex());
			    }else{
				target = docList.getItemText(docList.getSelectedIndex());
			    }
			    
			    service.getEvaluationResults(byTech, tech, byDoc, target, new AsyncCallback<ArrayList<ArrayList<String>>>() {
				
				    @Override
				    public void onSuccess(ArrayList<ArrayList<String>> arg0) {
								
					//boolean first = true;

					for (ArrayList<String> res : arg0) {
					    /*EvaluationChart chart = new EvaluationChart(res,first);
					    chart.setVisible(true);
					    gFrame.addChart(chart);*/
					    //if(first) first = false;
					    gFrame.addRes(res);
					}
					gFrame.drawCharts();
					
					gFrame.hideWaitingPanel();

				    }

				    @Override
				    public void onFailure(Throwable arg0) {
					gFrame.hideWaitingPanel();
					Window.alert("Can generate charts.");
				    }
				});
			    
			}
		});
			
		listBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent arg0) {
			    if(listBox.getSelectedIndex() == 0){
				docList.setEnabled(false);
				techList.setEnabled(true);
			    }else{
				docList.setEnabled(true);
				techList.setEnabled(false);
			    }
			}
		});
		
		
		flowPanel.add(grid);
		flowPanel.add(generateGraph);
		
		grid.setWidth("100%");
	}

}
