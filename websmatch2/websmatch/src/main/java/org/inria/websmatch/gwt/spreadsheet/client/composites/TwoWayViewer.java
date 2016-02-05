package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.inria.websmatch.gwt.spreadsheet.client.GfxRessources;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsService;
import org.inria.websmatch.gwt.spreadsheet.client.MatchingResultsServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaService;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.MatchingResult;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleEdge;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleGraphComponent;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSchema;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleVertex;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.TwoWayCanvas;

import com.blogspot.qbeukes.gwt.html5canvas.client.Font;
import com.blogspot.qbeukes.gwt.html5canvas.client.FontStyle;
import com.blogspot.qbeukes.gwt.html5canvas.client.FontVariant;
import com.blogspot.qbeukes.gwt.html5canvas.client.FontWeight;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;

@SuppressWarnings("deprecation")
public class TwoWayViewer extends Composite {

    private static final List<String> tresholdValues = Arrays.asList("0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0");

    private MatchingResultsServiceAsync service = (MatchingResultsServiceAsync) GWT.create(MatchingResultsService.class);
    private SchemaServiceAsync schemaService = (SchemaServiceAsync) GWT.create(SchemaService.class);
    private TwoWayCanvas canvas;

    private List<MatchingResult> results;
    final ListBox leftList = new ListBox();
    final ListBox rightList = new ListBox();

    private PopupPanel waitingPopup;
    GfxRessources gfxRes = GfxRessources.INSTANCE;
    private Image ajaxImage;

    private Button btnSubmit;
    private ScrollPanel scrollImage = new ScrollPanel();
    private FlowPanel flowPanel_1;

    private String tableHeight = "200px";

    private HashMap<Integer, SimpleVertex> vertices;
    
    final ListBox listBox = new ListBox();
    final ListBox valuePicker = new ListBox();
    
    // we add a button for probabilistic mode
    final CheckBox probaMode = new CheckBox("Use probabilities");
    
    // lists of schemas
    final List<SimpleSchema> leftSchemas;
    final List<SimpleSchema> rightSchemas;
    
    private boolean firstSelection = true;

    public TwoWayViewer(final List<SimpleSchema> leftSchemas, final List<SimpleSchema> rightSchemas, final MainFrame fr) {

	this.leftSchemas = leftSchemas;
	this.rightSchemas = rightSchemas;
	
	ajaxImage = new Image(gfxRes.loader());

	vertices = new HashMap<Integer, SimpleVertex>();

	FlowPanel fp = new FlowPanel();
	initWidget(fp);
	fp.setSize("100%", "100%");

	CaptionPanel cptnpnlMatchingResults = new CaptionPanel("");
	cptnpnlMatchingResults.setStyleName("body");
	fp.add(cptnpnlMatchingResults);
	cptnpnlMatchingResults.setSize("98%", "100%");

	flowPanel_1 = new FlowPanel();
	flowPanel_1.setStyleName("body");
	cptnpnlMatchingResults.setContentWidget(flowPanel_1);
	flowPanel_1.setSize("100%", "100%");

	FlexTable table = new FlexTable();
	table.setStyleName("body");
	flowPanel_1.add(table);
	table.setSize("100%", "200px");

	Label lblLeftSchema = new Label("Left schema :");
	lblLeftSchema.setHeight("32px");
	table.setWidget(0, 0, lblLeftSchema);

	Label lblRightSchema = new Label("Right schema :");
	lblRightSchema.setHeight("32px");
	table.setWidget(0, 2, lblRightSchema);

	leftList.setHeight("");
	leftList.setVisibleItemCount(1);
	leftList.setWidth("70%");

	table.setWidget(0, 1, leftList);

	rightList.setHeight("");
	rightList.setVisibleItemCount(1);
	rightList.setWidth("70%");

	table.setWidget(0, 3, rightList);

	// add threshold control and submit
	Label treLabel = new Label("Threshold :");
	treLabel.setHeight("32px");
	// table.setWidget(1, 0, treLabel);

	valuePicker.setHeight("");
	valuePicker.setVisibleItemCount(1);

	// table.setWidget(1, 1, valuePicker);

	btnSubmit = new Button("Show results");
	btnSubmit.setSize("150px", "");

	// Label lblMatchingTechnique = new Label("Matching technique :");
	// table.setWidget(1, 2, lblMatchingTechnique);

	// table.setWidget(1, 3, listBox);
	listBox.setVisibleItemCount(1);

	//table.setWidget(1, 4, btnSubmit);

	probaMode.addClickHandler(new ClickHandler() {

	    @Override
	    public void onClick(ClickEvent event) {

		if (probaMode.getValue()) {

		    valuePicker.setEnabled(false);
		    listBox.setEnabled(false);

		} else {
		    valuePicker.setEnabled(true);
		    listBox.setEnabled(true);
		}

	    }

	});

	valuePicker.setEnabled(false);
	listBox.setEnabled(false);
	probaMode.setValue(true);

	// table.setWidget(0, 4, probaMode);

	/*btnSubmit.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent arg0) {

		
	    }
	});*/

	for (SimpleSchema s : leftSchemas)
	    leftList.addItem(s.getName());

	for (SimpleSchema s : rightSchemas)
	    rightList.addItem(s.getName());
	
	class ListingChangeHandler implements ChangeHandler{
		@Override
		public void onChange(ChangeEvent event) {
		    if(!firstSelection) loadResults();
		    firstSelection = false;
		}	
	    }
	
	ListingChangeHandler changes = new ListingChangeHandler();
	
	leftList.addChangeHandler(changes);	
	rightList.addChangeHandler(changes);	
	//
	
	for (String s : tresholdValues)
	    valuePicker.addItem(s);

	valuePicker.setSelectedIndex(5);

	// get the elements
	/*service.getMatchingTechs(new AsyncCallback<List<SimpleMatchTech>>() {

	    @Override
	    public void onFailure(Throwable arg0) {

	    }

	    @Override
	    public void onSuccess(List<SimpleMatchTech> arg0) {

		for (SimpleMatchTech tech : arg0) {
		    listBox.addItem(tech.getName());
		    if (tech.getName().equals(fr.getMatchingTech()))
			listBox.setSelectedIndex(listBox.getItemCount() - 1);
		}

	    }

	});*/

	canvas = new TwoWayCanvas(flowPanel_1.getOffsetWidth(), 650);
	scrollImage.add(canvas);
	// canvas.setWidth("100%");
	flowPanel_1.add(scrollImage);

	setStyleName("body");
    }

    /**
     * Draw an arc.
     * 
     * @param x
     *            int value for centre X
     * @param y
     *            int value for centre Y
     * @param r
     *            int value for radius
     * @param startAngle
     *            int value of start angle in degree 0 - 360
     * @param endAngle
     *            int value of end angle in degree 0 - 360
     * @param antiClock
     *            true value for antiClockwise sense
     */
    public void drawArc(int x, int y, int r, int startAngle, int endAngle, boolean antiClock) {
	canvas.beginPath();
	final double start = Math.PI * startAngle / 180;
	final double end = Math.PI * endAngle / 180;
	canvas.arc(x, y, r, start, end, antiClock);
	canvas.closePath();
	// canvas.stroke();
	canvas.fill();
    }

    public void drawLine(int fx, int fy, int sx, int sy) {
	canvas.saveContext();
	canvas.removeShadow();
	canvas.setGlobalAlpha(1.0f);
	canvas.beginPath();
	canvas.lineTo((int) fx, (int) fy);
	canvas.lineTo((int) sx - (((int) sx - (int) fx) / 2), (int) fy);
	canvas.closePath();
	canvas.stroke();
	canvas.beginPath();
	canvas.lineTo((int) sx - (((int) sx - (int) fx) / 2), (int) fy);
	canvas.lineTo((int) sx - (((int) sx - (int) fx) / 2), (int) sy);
	canvas.closePath();
	canvas.stroke();
	canvas.beginPath();
	canvas.lineTo((int) sx - (((int) sx - (int) fx) / 2), (int) sy);
	canvas.lineTo((int) sx, (int) sy);
	canvas.closePath();
	canvas.stroke();
	canvas.restoreContext();
    }

    public void showWaitingPanel() {
	waitingPopup = new PopupPanel(false);
	waitingPopup.setGlassEnabled(true);
	waitingPopup.center();

	final Grid grid = new Grid(1, 2);
	grid.setWidget(0, 0, ajaxImage);
	grid.setText(0, 1, "Loading...");
	waitingPopup.add(grid);

	waitingPopup.show();
    }

    public void hideWaitingPanel() {
	if (waitingPopup != null && waitingPopup.isShowing())
	    waitingPopup.hide();
    }

    public TwoWayCanvas getCanvas() {
	return canvas;
    }

    public void setCanvas(TwoWayCanvas canvas) {
	this.canvas = canvas;
    }
    
    public void loadResults(String leftId, String rightId){
	
	for(int i = 0; i < leftSchemas.size(); i++){
	    if(leftSchemas.get(i).getId().equals(leftId)){
		leftList.setSelectedIndex(i);
		break;
	    }
	}
	
	for(int i = 0; i < rightSchemas.size(); i++){
	    if(rightSchemas.get(i).getId().equals(rightId)){
		rightList.setSelectedIndex(i);
		break;
	    }
	}
	
	loadResults();
    }
    
    public void loadResults(){
	
	final TwoWayViewer viewer = this;
	
	if(leftList.getSelectedIndex() != rightList.getSelectedIndex()){
	    showWaitingPanel();
		canvas.clear();
		canvas.setBackgroundColor(Color.WHITE);

		// ok first get schemas and draw them
		schemaService.getSchemaTree(leftSchemas.get(leftList.getSelectedIndex()).getId(), false, new AsyncCallback<ArrayList<SimpleGraphComponent>>() {

		    @Override
		    public void onSuccess(ArrayList<SimpleGraphComponent> result) {

			final ArrayList<SimpleGraphComponent> leftResult = result;

			schemaService.getSchemaTree(leftSchemas.get(rightList.getSelectedIndex()).getId(), true,
				new AsyncCallback<ArrayList<SimpleGraphComponent>>() {
				    public void onSuccess(ArrayList<SimpleGraphComponent> result) {

					int maxHeight = 0;
					int firstMaxWidth = 0;
					int maxWidth = 0;
					int translateVal = 0;
					int maxLeftTextWidth = 10;
					int maxRightTextWidth = 10;						

					// we calculate maxHeight for canvas
					// max min and so on
					double lminX, lmaxX, lminY, lmaxY;
					// determine minimum and maximum
					// positions of the nodes
					lminX = Float.MAX_VALUE;
					lmaxX = -Float.MAX_VALUE;
					lminY = Float.MAX_VALUE;
					lmaxY = -Float.MAX_VALUE;

					for (SimpleGraphComponent comp : result) {
					    if (comp instanceof SimpleVertex) {
						double[] position = new double[] { ((SimpleVertex) comp).getX(), ((SimpleVertex) comp).getY() };
						double diameter = 10;
						lminX = Math.min(lminX, position[0] - diameter / 2);
						lmaxX = Math.max(lmaxX, position[0] + diameter / 2);
						lminY = Math.min(lminY, position[1] - diameter / 2);
						lmaxY = Math.max(lmaxY, position[1] + diameter / 2);
					    }
					}

					for (SimpleGraphComponent comp : result) {
					    if (comp instanceof SimpleVertex) {
						int positionY = (int) Math.round((((SimpleVertex) comp).getY() - lminY) * 1.0);
						if ((positionY + 100) > maxHeight)
						    maxHeight = positionY + 100;
						int positionX = (int) Math.round((((SimpleVertex) comp).getX() - lminX) * 1.0);
						if ((positionX) > maxWidth)
						    maxWidth = positionX;
						if (canvas.getTextWidth(((SimpleVertex) comp).getName()) + 10 > maxLeftTextWidth)
						    maxLeftTextWidth = (int) (canvas.getTextWidth(((SimpleVertex) comp).getName()) + 10);
					    }
					}
					
					firstMaxWidth = maxWidth;

					// first we draw the left tree
					if (leftResult != null) {

					    // max min and so on
					    double minX, maxX, minY, maxY;
					    // determine minimum and maximum
					    // positions of the nodes
					    minX = Float.MAX_VALUE;
					    maxX = -Float.MAX_VALUE;
					    minY = Float.MAX_VALUE;
					    maxY = -Float.MAX_VALUE;

					    for (SimpleGraphComponent comp : leftResult) {
						if (comp instanceof SimpleVertex) {
						    double[] position = new double[] { ((SimpleVertex) comp).getX(), ((SimpleVertex) comp).getY() };
						    double diameter = 10;
						    minX = Math.min(minX, position[0] - diameter / 2);
						    maxX = Math.max(maxX, position[0] + diameter / 2);
						    minY = Math.min(minY, position[1] - diameter / 2);
						    maxY = Math.max(maxY, position[1] + diameter / 2);

						    vertices.put(((SimpleVertex) comp).getId(), (SimpleVertex) comp);
						}
					    }

					    for (SimpleGraphComponent comp : leftResult) {
						if (comp instanceof SimpleVertex) {
						    int positionY = (int) Math.round((((SimpleVertex) comp).getY() - minY) * 1.0);
						    if ((positionY + 100) > maxHeight)
							maxHeight = positionY + 100;
						    int positionX = (int) Math.round((((SimpleVertex) comp).getX() - minX) * 1.0);
						    if ((positionX) > maxWidth)
							maxWidth = positionX;
						    if (canvas.getTextWidth(((SimpleVertex) comp).getName()) + 10 > maxRightTextWidth)
							maxRightTextWidth = (int) (canvas.getTextWidth(((SimpleVertex) comp).getName()) + 10);
						}
					    }

					    flowPanel_1.remove(1);
					    // we set the canvas
					    // canvas = new TwoWayCanvas(flowPanel_1.getOffsetWidth() + maxLeftTextWidth, maxHeight);
						
					    int canvasWidth = 0;
					    
					    if(flowPanel_1.getOffsetWidth() > (maxLeftTextWidth+maxRightTextWidth+firstMaxWidth+maxWidth) ){
						canvas = new TwoWayCanvas(flowPanel_1.getOffsetWidth(), maxHeight);
						canvasWidth = flowPanel_1.getOffsetWidth();
					    }
					    else{
						canvas = new TwoWayCanvas(maxLeftTextWidth+maxRightTextWidth+firstMaxWidth+maxWidth+50, maxHeight);
						canvasWidth = maxLeftTextWidth+maxRightTextWidth+firstMaxWidth+maxWidth+50;
					    }
					    
					    canvas.setViewer(viewer);
					    
					    /*System.out.println("FP width : "+flowPanel_1.getOffsetWidth());
					    System.out.println("Max text left width : "+maxLeftTextWidth);
					    System.out.println("Max text right width : "+maxRightTextWidth);
					    System.out.println("Max width : "+firstMaxWidth);
					    System.out.println("Max width : "+maxWidth);*/
					    
					    Font font = new Font("monospace,Courier New", "10px", FontStyle.NORMAL, FontVariant.NORMAL, FontWeight.BOLD);
					    canvas.setFont(font);

					    // ok, calculate the translate value
					    // for right tree
					    // translateVal = flowPanel_1.getOffsetWidth() + maxLeftTextWidth - maxWidth - 10;
					    // System.out.println("Canvas width : "+canvasWidth);
					    
					    if(flowPanel_1.getOffsetWidth() > (maxLeftTextWidth+maxRightTextWidth+firstMaxWidth+maxWidth+150) ){
						translateVal = canvasWidth - (maxWidth + maxRightTextWidth + 10);
						// System.out.println("Translate 1 : "+ translateVal);
					    }else{
						translateVal = maxLeftTextWidth + maxWidth + maxRightTextWidth + 10;
						// System.out.println("Translate 2 : "+ translateVal);
					    }
					    
					    scrollImage = new ScrollPanel();
					    scrollImage.add(canvas);
					    scrollImage.setSize(flowPanel_1.getOffsetWidth() + "px", (flowPanel_1.getOffsetHeight() - 10 - new Integer(
						    tableHeight.substring(0, 3)).intValue()) + "px");

					    flowPanel_1.add(scrollImage);

					    canvas.setBackgroundColor(Color.WHITE);
					    canvas.setLineWidth(0.5);
					    canvas.setGlobalAlpha(0.5f);
					    canvas.setGlobalCompositeOperation(GWTCanvas.SOURCE_OVER);

					    // iterate trough the map and draw
					    // double scale =
					    // Math.min(canvas.getCoordWidth() /
					    // (maxX - minX),
					    // canvas.getCoordHeight() / (maxY -
					    // minY));
					    double scale = 1.0;

					    // ok draw on canvas
					    for (SimpleGraphComponent comp : leftResult) {

						canvas.setFillStyle(new Color(1, 1, 1));

						if (comp instanceof SimpleVertex) {

						    int positionX = (int) Math.round((((SimpleVertex) comp).getX() - minX) * scale);
						    int positionY = (int) Math.round((((SimpleVertex) comp).getY() - minY) * scale);

						    // System.out.println("x: "
						    // + positionX + " y: " +
						    // positionY);

						    int diameter = (int) (10 * scale);

						    drawArc(positionX, positionY, diameter / 2, 0, 360, false);
						    canvas.setFillStyle(Color.BLACK);
						    
						    // set x y for drawing
						    ((SimpleVertex) comp).setCanvasX(positionX);
						    ((SimpleVertex) comp).setCanvasY(positionY);
						    
						    canvas.fillGraphCompText(((SimpleVertex) comp).getName(), positionX, positionY, false, Color.YELLOW);

						    // ok add it to the list of
						    // clickable elements
						    // canvas.addClickableElement(positionX,
						    // positionY,
						    // ((SimpleVertex) comp));
						    for (int x = positionX; x < positionX + canvas.getTextWidth(((SimpleVertex) comp).getName()) + 18; x++) {
							for (int y = positionY; y < positionY + 5; y++) {
							    canvas.addClickableElement(x, y, ((SimpleVertex) comp));
							}
						    }

						    // ((SimpleVertex) comp).setX(positionX);
						    // ((SimpleVertex) comp).setY(positionY);
						    vertices.put(((SimpleVertex) comp).getId(), (SimpleVertex) comp);
						}

						if (comp instanceof SimpleEdge) {

						    int positionX = (int) Math.round((((SimpleEdge) comp).getParentX() - minX) * scale);
						    int positionY = (int) Math.round((((SimpleEdge) comp).getParentY() - minY) * scale);
						    int positionEndX = (int) Math.round((((SimpleEdge) comp).getChildX() - minX) * scale);
						    int positionEndY = (int) Math.round((((SimpleEdge) comp).getChildY() - minY) * scale);

						    // System.out.println("x: "
						    // + positionX + " y: " +
						    // positionY);

						    canvas.setStrokeStyle(Color.BLACK);
						    drawLine(positionX, positionY, positionEndX, positionEndY);
						}
					    }
					}

					// we draw the right tree
					canvas.setLineWidth(0.5);
					canvas.setGlobalAlpha(0.5f);
					canvas.setGlobalCompositeOperation(GWTCanvas.SOURCE_OVER);

					// max min and so on
					double minX, maxX, minY, maxY;
					// determine minimum and maximum
					// positions of the nodes
					minX = Float.MAX_VALUE;
					maxX = -Float.MAX_VALUE;
					minY = Float.MAX_VALUE;
					maxY = -Float.MAX_VALUE;

					for (SimpleGraphComponent comp : result) {
					    if (comp instanceof SimpleVertex) {
						double[] position = new double[] { ((SimpleVertex) comp).getX(), ((SimpleVertex) comp).getY() };
						double diameter = 10;
						minX = Math.min(minX, position[0] - diameter / 2);
						maxX = Math.max(maxX, position[0] + diameter / 2);
						minY = Math.min(minY, position[1] - diameter / 2);
						maxY = Math.max(maxY, position[1] + diameter / 2);
					    }
					}

					// iterate trough the map and draw
					// double scale =
					// Math.min(canvas.getCoordWidth() /
					// (maxX - minX),
					// canvas.getCoordHeight() / (maxY -
					// minY));
					double scale = 1.0;

					// ok draw on canvas
					for (SimpleGraphComponent comp : result) {

					    canvas.setFillStyle(new Color(1, 1, 1));

					    if (comp instanceof SimpleVertex) {

						// set rtl
						((SimpleVertex)comp).setRtl(true);
						
						int positionX = (int) Math.round((((SimpleVertex) comp).getX() - minX) * scale) + translateVal;
						int positionY = (int) Math.round((((SimpleVertex) comp).getY() - minY) * scale);

						// System.out.println("x: " +
						// positionX + " y: " +
						// positionY);

						int diameter = (int) (10 * scale);

						drawArc(positionX, positionY, diameter / 2, 0, 360, false);
						canvas.setFillStyle(Color.BLACK);
						
						// set x y for drawing
						((SimpleVertex) comp).setCanvasX(positionX);
						((SimpleVertex) comp).setCanvasY(positionY);
						
						canvas.fillGraphCompText(((SimpleVertex) comp).getName(), positionX, positionY, true, Color.YELLOW);

						// ok add it to the list of
						// clickable elements
						// canvas.addClickableElement(positionX,
						// positionY, ((SimpleVertex)
						// comp));
						for (int x = positionX; x < positionX + canvas.getTextWidth(((SimpleVertex) comp).getName()) +18; x++) {
						    for (int y = positionY; y < positionY + 5; y++) {
							canvas.addClickableElement(x - (int) canvas.getTextWidth(((SimpleVertex) comp).getName()), y,
								((SimpleVertex) comp));
						    }
						}

						// ((SimpleVertex) comp).setX(positionX);
						// ((SimpleVertex) comp).setY(positionY);
						vertices.put(((SimpleVertex) comp).getId(), (SimpleVertex) comp);
					    }

					    if (comp instanceof SimpleEdge) {

						int positionX = (int) Math.round((((SimpleEdge) comp).getParentX() - minX) * scale) + translateVal;
						int positionY = (int) Math.round((((SimpleEdge) comp).getParentY() - minY) * scale);
						int positionEndX = (int) Math.round((((SimpleEdge) comp).getChildX() - minX) * scale) + translateVal;
						int positionEndY = (int) Math.round((((SimpleEdge) comp).getChildY() - minY) * scale);

						// System.out.println("x: " +
						// positionX + " y: " +
						// positionY);

						canvas.setStrokeStyle(Color.BLACK);
						drawLine(positionX, positionY, positionEndX, positionEndY);
					    }

					}

					AsyncCallback<List<MatchingResult>> callback = new AsyncCallback<List<MatchingResult>>() {

					    @Override
					    public void onFailure(Throwable arg0) {
						arg0.printStackTrace();
						hideWaitingPanel();
					    }

					    @Override
					    public void onSuccess(List<MatchingResult> arg0) {
						results = arg0;

						canvas.saveContext();
						canvas.removeShadow();
						canvas.setLineWidth(2.0);

						// System.out.println("Received results : " + arg0.size());

						for (MatchingResult res : results) {

						    if (vertices.get(res.getId_element1()) != null && vertices.get(res.getId_element2()) != null) {
							if (res.isExpert() || res.getScore() >= 1.0)
							    canvas.setStrokeStyle(Color.GREEN);
							else
							    canvas.setStrokeStyle(Color.ORANGE);

							double fx = vertices.get(res.getId_element1()).getCanvasX();
							double fy = vertices.get(res.getId_element1()).getCanvasY();

							double sx = vertices.get(res.getId_element2()).getCanvasX();
							double sy = vertices.get(res.getId_element2()).getCanvasY();

							boolean invert = false;
							// we need to invert
							if(fx > sx){
							    double tmpx = sx;
							    double tmpy = sy;
							    
							    sx = fx;
							    sy = fy;
							    
							    fx = tmpx;
							    fy = tmpy;
							    invert = true;
							}
							
							// we draw the
							// lines
							canvas.beginPath();
							// System.out.println("fx : "+fx+" Left size : "+canvas.getTextWidth(res.getLeftElementName()));
							// System.out.println("sx : "+sx+" Right size : "+canvas.getTextWidth(res.getRightElementName()));
							if(!invert){
							    canvas.lineTo(fx + 18.0 + canvas.getTextWidth(res.getLeftElementName()), fy);
							    canvas.lineTo(sx - 18.0 - canvas.getTextWidth(res.getRightElementName()), sy);
							}else{
							    canvas.lineTo(fx + 18.0 + canvas.getTextWidth(res.getRightElementName()), fy);
							    canvas.lineTo(sx - 18.0 - canvas.getTextWidth(res.getLeftElementName()), sy);
							}
							canvas.closePath();
							canvas.stroke();

							// System.out.println(res.getScore());
							// ok draw text
							String text = new String();

							if (probaMode.getValue()) {
							    text = " + ";
							}

							else {
							    text = new Double(res.getScore()).toString();
							    if (text.length() > 4)
								text = text.substring(0, 4);
							}

							if(!invert){
							canvas.fillGraphCompText(text, (fx + 18.0 + canvas.getTextWidth(res.getLeftElementName())) + (((sx - 18.0 - canvas.getTextWidth(res.getRightElementName())) - (fx + 18.0 + canvas.getTextWidth(res.getLeftElementName()))) / 2), fy + ((sy - fy) / 2), false,Color.YELLOW);
							// add this
							// score to get
							// the popup
							// working
							canvas.addNode(new int[] { (int) (((fx + 18.0 + canvas.getTextWidth(res.getLeftElementName())) + (((sx - 18.0 - canvas.getTextWidth(res.getRightElementName())) - (fx + 18.0 + canvas.getTextWidth(res.getLeftElementName()))) / 2)) + canvas.getTextWidth(text) / 2),
								(int) (fy + ((sy - fy) / 2)) + 5 }, res);
							}
							else{
							    canvas.fillGraphCompText(text, (fx + 18.0 + canvas.getTextWidth(res.getRightElementName())) + (((sx - 18.0 - canvas.getTextWidth(res.getLeftElementName())) - (fx + 18.0 + canvas.getTextWidth(res.getRightElementName()))) / 2), fy + ((sy - fy) / 2), false,Color.YELLOW);	    
							 // add this
								// score to get
								// the popup
								// working
								canvas.addNode(new int[] { (int) (((fx + 18.0 + canvas.getTextWidth(res.getRightElementName())) + (((sx - 18.0 - canvas.getTextWidth(res.getLeftElementName())) - (fx + 18.0 + canvas.getTextWidth(res.getRightElementName()))) / 2)) + canvas.getTextWidth(text) / 2),
									(int) (fy + ((sy - fy) / 2)) + 5 }, res);
							}
							

						    }
						}

						canvas.restoreContext();

						hideWaitingPanel();
					    }
					};

					// Use the service to get results
					if (probaMode.getValue()) {
					    service.getProbaResults(leftList.getValue(leftList.getSelectedIndex()),
						    rightList.getValue(rightList.getSelectedIndex()), callback);
					} else
					    service.getResults(leftList.getValue(leftList.getSelectedIndex()),
						    rightList.getValue(rightList.getSelectedIndex()), valuePicker.getValue(valuePicker.getSelectedIndex()),
						    listBox.getValue(listBox.getSelectedIndex()), callback);

				    }

				    @Override
				    public void onFailure(Throwable caught) {
					hideWaitingPanel();
				    }
				});
			

		    }

		    @Override
		    public void onFailure(Throwable caught) {
			hideWaitingPanel();
		    }
		});
	}	
	}
       
}
