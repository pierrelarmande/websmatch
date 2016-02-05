package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SpreadsheetViewer;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageService;
import org.inria.websmatch.gwt.spreadsheet.client.XMLStorageServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.matcher.popup.CSVOboMapperPopup;
import org.inria.websmatch.gwt.spreadsheet.client.composites.matcher.popup.N3AnnotatorPopup;
import org.inria.websmatch.gwt.spreadsheet.client.composites.matcher.popup.OntoMatcherPopup;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSchema;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.CustomProgressBar;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.DistancesListing;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.EvaluationPanel;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.IntegrationExportPopup;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.SchemaCellTable;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.WaitingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.ProgressBar;
import com.google.gwt.widgetideas.client.ProgressBar.TextFormatter;

public class MainFrame extends Composite {

    private FlowPanel mainPanel;
    private PopupPanel popup;
    private String userName;
    private MatchingResultsServiceAsync mresService = (MatchingResultsServiceAsync) GWT.create(MatchingResultsService.class);

    private MatchingServiceAsync matchService = (MatchingServiceAsync) GWT.create(MatchingService.class);

    // new service for storage
    private XMLStorageServiceAsync storeService = (XMLStorageServiceAsync) GWT.create(XMLStorageService.class);

    private int barSize = 20;

    private int borderToRemove = 110;

    private String matchingTech = null;

    public MainFrame(final String username, final String sid) {

	FlowPanel flowPanel = new FlowPanel();
	flowPanel.setStyleName("loginPanel");
	initWidget(flowPanel);
	flowPanel.setSize("98%", "96%");

	this.setUserName(username);

	CaptionPanel cptnpnlWebsmatch = new CaptionPanel("WebSmatch");
	flowPanel.add(cptnpnlWebsmatch);
	cptnpnlWebsmatch.setSize("98%", "");

	FlowPanel flowPanel_1 = new FlowPanel();
	cptnpnlWebsmatch.setContentWidget(flowPanel_1);
	flowPanel_1.setSize("100%", "100%");
	flowPanel_1.addStyleName("body");

	MenuBar menuBar = new MenuBar(false);
	flowPanel_1.add(menuBar);
	MenuBar menuBar_1 = new MenuBar(true);

	MenuItem mntmFile = new MenuItem("File", false, menuBar_1);

	final MainFrame mainF = this;

	MenuItem mntmLoadSchemaFrom = new MenuItem("Load schema from store", false, new Command() {

	    public void execute() {
		storeService.getDocuments(false, SpreadsheetViewer.username, new AsyncCallback<List<SchemaData>>() {
		    @Override
		    public void onFailure(Throwable caught) {
			caught.printStackTrace();
			Window.alert("Can't load schemas.");
		    }

		    @Override
		    public void onSuccess(List<SchemaData> result) {
			showSchemas(result);
		    }
		});
	    }
	});
	menuBar_1.addItem(mntmLoadSchemaFrom);

	MenuItemSeparator separator = new MenuItemSeparator();
	menuBar_1.addSeparator(separator);

	MenuItem mntmImportFile = new MenuItem("Import file", false, new Command() {

	    public void execute() {

		popup = new PopupPanel(true);
		popup.setGlassEnabled(true);
		popup.setPopupPosition(100, 100);
		popup.setAnimationEnabled(true);

		ScrollPanel scroll = new ScrollPanel(new FileUploadComposite(username, mainF));

		popup.add(scroll);
		popup.setSize("800px", "93px");

		popup.show();
	    }
	});
	menuBar_1.addItem(mntmImportFile);

	MenuItem mntmExportFile = new MenuItem("Export file", false, new Command() {
	    public void execute() {
	    }
	});
	mntmExportFile.setEnabled(false);
	menuBar_1.addItem(mntmExportFile);
	menuBar.addItem(mntmFile);
	MenuBar menuBar_2 = new MenuBar(true);
	
	MenuItem mntmMatching = new MenuItem("Matching", false, menuBar_2);

	// TODO make the as a list for mongo version
	/*
	 * MenuItem mntmAsList = new MenuItem("As a list", false, new Command()
	 * { public void execute() { mresService.getMatchedSchemas(new
	 * AsyncCallback<List<SimpleSchema>>() {
	 * 
	 * @Override public void onFailure(Throwable arg0) {
	 * 
	 * }
	 * 
	 * @Override public void onSuccess(List<SimpleSchema> arg0) {
	 * 
	 * setMainWidget(new SchemaResultsListers(arg0, arg0, mainF));
	 * 
	 * } }); } });
	 */

	// FIXME useless for mongo version?
	/*
	 * MenuItem mntmConfigureYamMatcher = new
	 * MenuItem("Configure YAM Matcher", false, new Command() { public void
	 * execute() { YAMConfigurationPopup popup = new
	 * YAMConfigurationPopup(mainF); popup.show(); } });
	 * menuBar_2.addItem(mntmConfigureYamMatcher);
	 */

	/*
	 * MenuItemSeparator separator_1 = new MenuItemSeparator();
	 * menuBar_2.addSeparator(separator_1);
	 */

	MenuItem mntmYamMatcher = new MenuItem("Launch matching", false, new Command() {
	    public void execute() {
		// setMainWidget(new SchemaViewer(mainF));

		WaitingPopup.getInstance().show();

		matchService.completeMatching(SpreadsheetViewer.username, new AsyncCallback<Void>() {

		    @Override
		    public void onFailure(Throwable caught) {
			WaitingPopup.getInstance().hide();
		    }

		    @Override
		    public void onSuccess(Void result) {
			WaitingPopup.getInstance().hide();
		    }
		});
	    }
	});
	menuBar_2.addItem(mntmYamMatcher);
	menuBar.addItem(mntmMatching);
	MenuBar menuBar_3 = new MenuBar(true);

	MenuItem mntmViewResults = new MenuItem("View results", false, menuBar_3);

	// FIXME removed above
	// menuBar_3.addItem(mntmAsList);

	// now we had the chart results view for oeai user only for now
	if (username.equals("oaei")) {

	    MenuItem mntmAsAGraph = new MenuItem("As Fmeasure graph", false, new Command() {
		public void execute() {
		    EvaluationPanel eval = new EvaluationPanel(mainF);
		    eval.show();
		    // setMainWidget(new GraphFrame());
		}
	    });
	    menuBar_3.addItem(mntmAsAGraph);
	}
	//

	menuBar.addItem(mntmViewResults);

	// TODO make it work on mongo version
	/*
	 * MenuItem twoWay = new MenuItem("Two way view", false, new Command() {
	 * public void execute() { mresService.getMatchedSchemas(new
	 * AsyncCallback<List<SimpleSchema>>() {
	 * 
	 * @Override public void onFailure(Throwable arg0) {
	 * 
	 * }
	 * 
	 * @Override public void onSuccess(List<SimpleSchema> arg0) {
	 * 
	 * setMainWidget(new TwoWayViewer(arg0, arg0, mainF));
	 * 
	 * } }); } }); menuBar_3.addItem(twoWay);
	 */
	MenuBar menuBar_4 = new MenuBar(true);

	MenuItem mntmInteg = new MenuItem("Tools", false, menuBar_4);
	menuBar.addItem(mntmInteg);
	
	// add simple alignement tools
	// obo term matcher
	MenuItem mntmRDFMapper = new MenuItem("Ontology (RDF and/or OBO) matcher", false, new Command() {
	    public void execute() {
		new OntoMatcherPopup().show();
	    }
	});
	menuBar_4.addItem(mntmRDFMapper);	
	//
	
	// add csv to obo annotator
	MenuItem mntmCSVOboMapper = new MenuItem("CSV OBO annotator", false, new Command() {
	    public void execute() {
		new CSVOboMapperPopup().show();
	    }
	});
	menuBar_4.addItem(mntmCSVOboMapper);	
	//
	
	// add RDF annotator
	MenuItem mntmRDFAnnotator = new MenuItem("BioSemantic N3 annotator", false, new Command() {
	    public void execute() {
		new N3AnnotatorPopup().show();
	    }
	});
	menuBar_4.addItem(mntmRDFAnnotator);	
	//
	    
	// add menu for Data integration
	if (username.equals("demo")) {
	    menuBar_4.addSeparator();
	    MenuItem exportInt = new MenuItem("Export integrated schemas", false, new Command() {
		public void execute() {
		    IntegrationExportPopup export = new IntegrationExportPopup(/* userName */);
		    export.show();
		}
	    });
	    menuBar_4.addItem(exportInt);   
	}
	// end of integration

	MenuItem exportInt = new MenuItem("Distances listing", false, new Command() {
	    public void execute() {
		DistancesListing export = new DistancesListing();
		export.getScroll().setHeight((Window.getClientHeight() - borderToRemove - 22) + "px");
		setMainWidget(export);
	    }
	});

	menuBar_3.addItem(exportInt);
	
	Label lblYouAreLogged = new Label("You are logged as : " + username);
	lblYouAreLogged.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
	lblYouAreLogged.setSize("100%", "20px");

	mainPanel = new FlowPanel();
	flowPanel_1.add(mainPanel);
	mainPanel.setWidth("100%");
	mainPanel.setHeight(Window.getClientHeight() - borderToRemove + "px");
	Window.addResizeHandler(new ResizeHandler() {

	    public void onResize(ResizeEvent event) {
		int height = event.getHeight() - borderToRemove;
		mainPanel.setHeight(height + "px");
		mainPanel.setWidth("100%");
	    }
	});

	RichTextArea txtrYouAreNow = new RichTextArea();
	txtrYouAreNow.setSize("99%", "95%");
	txtrYouAreNow
		.setHTML("You are now logged in WebSmatch.<br><br>You can import any schema file (XLS, OWL, RDF and so on) using the <b>\"File\"</b> menu.<br>\nAfter important 2 files, you'll be able to try our matcher YAM (Yet Another Matcher) and see how it performs in 2 ways : list and cluster.<br><br>\nTo see the results, use the <b>\"View results\"</b> menu item.\n");

	// ok add the progress bar
	final CustomProgressBar bar = new CustomProgressBar(username, sid);
	bar.setHeight(barSize + "px");

	TextFormatter format = new TextFormatter() {
	    @Override
	    protected String getText(ProgressBar arg0, double arg1) {
		return "Status : No matching process running";
	    }
	};

	bar.setTextFormatter(format);
	bar.setProgress(0);

	MenuItem mntmAsACluster = new MenuItem("Clustering results", false, new Command() {
	    public void execute() {
		setMainWidget(new ClusterFrame(mainF.getOffsetWidth(), mainF.getOffsetHeight() - bar.getOffsetHeight(), mainF));
	    }
	});

	menuBar_3.addItem(mntmAsACluster);
			
	flowPanel_1.add(bar);
    }

    public FlowPanel getMainPanel() {
	return mainPanel;
    }

    public void closeFileImportPopup() {
	if (popup != null && popup.isShowing())
	    popup.setVisible(false);
    }

    public void setMainWidget(Widget wid) {
	mainPanel.clear();
	mainPanel.add(wid);
    }

    public Widget getMainWidget() {
	return mainPanel.getWidget(0);
    }

    public void setUserName(String userName) {
	this.userName = userName;
    }

    public String getUserName() {
	return userName;
    }

    public void setListResult(final String left, final String right) {

	final MainFrame mainF = this;

	mresService.getMatchedSchemas(new AsyncCallback<List<SimpleSchema>>() {

	    @Override
	    public void onFailure(Throwable arg0) {

	    }

	    @Override
	    public void onSuccess(List<SimpleSchema> arg0) {

		SchemaResultsListers listers = new SchemaResultsListers(arg0, arg0, mainF);

		ListBox leftlist = listers.getLeftList();
		for (int i = 0; i < leftlist.getItemCount(); i++) {
		    if (leftlist.getItemText(i).equals(left)) {
			leftlist.setSelectedIndex(i);
			break;
		    }
		}

		ListBox rightlist = listers.getRightList();
		for (int i = 0; i < rightlist.getItemCount(); i++) {
		    if (rightlist.getItemText(i).equals(right)) {
			rightlist.setSelectedIndex(i);
			break;
		    }
		}

		setMainWidget(listers);

	    }

	});
    }

    public void showSchemas(List<SchemaData> list) {

	popup = new PopupPanel(true);
	popup.setGlassEnabled(true);
	popup.setAnimationEnabled(true);
	popup.setPopupPosition(100, 100);

	SchemaCellTable table = new SchemaCellTable(list, this);

	ScrollPanel scroll = new ScrollPanel(table);
	scroll.setWidth("100%");
	table.setWidth("100%");

	popup.add(scroll);
	if (table.getRowCount() > 20)
	    popup.setSize("1000px", "600px");
	else {
	    popup.setSize("1000px", "200px");
	}

	popup.show();
    }

    public void setMatchingTech(String matchingTech) {
	this.matchingTech = matchingTech;
    }

    public String getMatchingTech() {
	return matchingTech;
    }

}
