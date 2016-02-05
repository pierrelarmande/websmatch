package org.inria.websmatch.gwt.spreadsheet.client.composites;

import java.util.ArrayList;
import java.util.Map;

import org.inria.websmatch.gwt.spreadsheet.client.ClusteringService;
import org.inria.websmatch.gwt.spreadsheet.client.ClusteringServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.handlers.CanvasMouseOverHandler;
import org.inria.websmatch.gwt.spreadsheet.client.models.Node;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.CustomCanvas;
import org.inria.websmatch.gwt.spreadsheet.client.widgets.WaitingPopup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;

public class ClusterFrame extends Composite {

    private ClusteringServiceAsync service = (ClusteringServiceAsync) GWT.create(ClusteringService.class);
    // final GWTCanvas canvas;
    final private CustomCanvas canvas;

    //private PopupPanel waitingPopup;
    //private GfxRessources gfxRes = GfxRessources.INSTANCE;
    //private Image ajaxImage;
    private MainFrame frame = null;

    public ClusterFrame(int width, int height, MainFrame mainF) {

	frame = mainF;

	//ajaxImage = new Image(gfxRes.loader());

	FlowPanel flowPanel = new FlowPanel();
	initWidget(flowPanel);
	flowPanel.setSize("100%", "100%");

	final ClusterTree tree = new ClusterTree();

	int treeWidth = 400;
	int border = 80;

	// int w = 800;
	// int h = 800;
	int w = width;
	int h = height - border;

	WaitingPopup.getInstance().show();

	// ok get what we need
	service.getClusterNodes(w - treeWidth - border, h, false, mainF.getUserName(), new AsyncCallback<Map<Map<Node, double[]>, Map<Node, Integer[]>>>() {

	    @Override
	    public void onFailure(Throwable arg0) {
		WaitingPopup.getInstance().hide();
	    }

	    @Override
	    public void onSuccess(Map<Map<Node, double[]>, Map<Node, Integer[]>> res) {

		for (Map<Node, double[]> nodeToPosition : res.keySet()) {

		    // max min and so on
		    double minX, maxX, minY, maxY;
		    // determine minimum and maximum positions of the nodes
		    minX = Float.MAX_VALUE;
		    maxX = -Float.MAX_VALUE;
		    minY = Float.MAX_VALUE;
		    maxY = -Float.MAX_VALUE;
		    for (Node node : nodeToPosition.keySet()) {
			double[] position = nodeToPosition.get(node);
			double diameter = Math.sqrt(node.weight);
			minX = Math.min(minX, position[0] - diameter / 2);
			maxX = Math.max(maxX, position[0] + diameter / 2);
			minY = Math.min(minY, position[1] - diameter / 2);
			maxY = Math.max(maxY, position[1] + diameter / 2);
		    }

		    // Clusters strings
		    ArrayList<ArrayList<String>> clusterNames = new ArrayList<ArrayList<String>>();

		    // determine maximum cluster of the nodes
		    Map<Node, Integer[]> nodeToCluster = res.get(nodeToPosition);
		    int maxCluster = 0;
		    for (Integer[] cluster : nodeToCluster.values()) {
			maxCluster = Math.max(cluster[0], maxCluster);
			if (maxCluster + 1 > clusterNames.size())
			    clusterNames.add(new ArrayList<String>());
		    }

		    // iterate trough the map and draw
		    double scale = Math.min(canvas.getCoordWidth() / (maxX - minX), canvas.getCoordHeight() / (maxY - minY));

		    // draw nodes as circles
		    // ((Graphics2D)g).setComposite(
		    // AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
		    // );

		    canvas.setLineWidth(1);
		    canvas.setGlobalAlpha(0.5f);
		    canvas.setGlobalCompositeOperation(GWTCanvas.SOURCE_OVER);

		    for (Node node : nodeToPosition.keySet()) {
			/*
			 * float hue = nodeToCluster.get(node) /
			 * (float)(maxCluster+1);
			 * g.setColor(Color.getHSBColor(hue, 1.0f, 1.0f));
			 */
			// canvas.setLineWidth(1);
			// canvas.setStrokeStyle(Color.RED);

			// float hue = nodeToCluster.get(node) /
			// (float)(maxCluster+1);
			// canvas.setStrokeStyle(new
			// Color(nodeToCluster.get(node)[1],
			// nodeToCluster.get(node)[2],
			// nodeToCluster.get(node)[3]));
			canvas.setFillStyle(new Color(nodeToCluster.get(node)[1], nodeToCluster.get(node)[2], nodeToCluster.get(node)[3]));

			int positionX = (int) Math.round((nodeToPosition.get(node)[0] - minX) * scale);
			int positionY = (int) Math.round((nodeToPosition.get(node)[1] - minY) * scale);
			int diameter = (int) Math.round(Math.sqrt(node.weight) * scale);

			// g.fillOval(positionX-diameter/2,
			// positionY-diameter/2,
			// diameter, diameter);
			drawArc(positionX, positionY, diameter / 2, 0, 360, false);
			// canvas.setStrokeStyle(Color.BLACK);
			canvas.setFillStyle(Color.BLACK);
			if (node.name.lastIndexOf(" (SchemaId : ") != -1) {
			    fillText(node.name.substring(0, node.name.lastIndexOf(" (SchemaId : ")), positionX, positionY);
			    // add node to canvas
			    canvas.addNode(new int[] { positionX, positionY, diameter }, node.name.substring(0, node.name.lastIndexOf(" (SchemaId : ")),
				    node.name.substring(node.name.lastIndexOf(" (SchemaId : ") + 13, node.name.length() - 1));
			    // add clusternames
			    clusterNames.get(nodeToCluster.get(node)[0]).add(node.name.substring(0, node.name.lastIndexOf(" (SchemaId : ")));
			} else{
			    fillText(node.name, positionX, positionY);
			    // add node to canvas
			    canvas.addNode(new int[] { positionX, positionY, diameter }, node.name, node.name);
			    // add clusternames
			    clusterNames.get(nodeToCluster.get(node)[0]).add(node.name);
			}

		    }

		    // set tree
		    for (ArrayList<String> names : clusterNames)
			tree.addCluster(names);

		    WaitingPopup.getInstance().hide();
		}

	    }
	});

	/*
	 * CaptionPanel cptnpnlClusteringResults = new CaptionPanel("");
	 * cptnpnlClusteringResults.setStyleName("body");
	 * flowPanel.add(cptnpnlClusteringResults);
	 */

	SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel();
	flowPanel.add(splitLayoutPanel);
	splitLayoutPanel.setSize("100%", "100%");

	canvas = new CustomCanvas(w - treeWidth, h);
	canvas.setFrame(frame);
	splitLayoutPanel.addEast(canvas, w - treeWidth);
	splitLayoutPanel.add(tree);
	canvas.setBackgroundColor(Color.WHITE);

	canvas.addMouseMoveHandler(new CanvasMouseOverHandler());

	setStyleName("body");
    }

    public void fillText(String text, int x, int y) {
	canvas.fillText(text, x, y);
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

    /*public void showWaitingPanel() {
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
    }*/

}
