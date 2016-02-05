package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.inria.websmatch.gwt.spreadsheet.client.MachineLearningService;
import org.inria.websmatch.gwt.spreadsheet.client.MachineLearningServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SchemaCellTreeComposite;
import org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup.BidimEditorPopup;
import org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup.EditContentPopup;
import org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup.EditDescriptionPopup;
import org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup.EditScriptPopup;
import org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup.ReplaceScriptPopup;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.inria.websmatch.gwt.spreadsheet.client.MachineLearningService;
import org.inria.websmatch.gwt.spreadsheet.client.MachineLearningServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.composites.SchemaCellTreeComposite;
import org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup.BidimEditorPopup;
import org.inria.websmatch.gwt.spreadsheet.client.composites.editor.popup.ReplaceScriptPopup;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

public class SpreadsheetEditor extends NoMouseOverCellTable<SimpleCell[]> {

    private SimpleSheet sheet;
    private SchemaCellTreeComposite listener = null;
    private ArrayList<SimpleCell> attrCells;

    // last selection values
    private String lastStyleName = null;
    private int lastRow = -1;
    private int lastCol = -1;

    // ml pop
    private PopupPanel mlPop;
    private MachineLearningServiceAsync service = (MachineLearningServiceAsync) GWT.create(MachineLearningService.class);

    final boolean[] firstTime = new boolean[1];

    public SpreadsheetEditor() {
	super();
	attrCells = new ArrayList<SimpleCell>();
	this.setSize("100%", "100%");
	firstTime[0] = true;
    }

    public void updateSheet(SimpleSheet sheet, boolean showCC, boolean useML) {

	this.sheet = sheet;
	SimpleCell[][] cells = this.sheet.getCells();

	GWT.log(this.getClass().getName() + " Row count : " + cells.length);

	// ok then count the columns
	int maxCol = 0;
	for (int i = 0; i < cells.length; i++) {
	    maxCol = Math.max(maxCol, cells[i].length);
	}

	GWT.log(this.getClass().getName() + " MaxCol : " + maxCol);

	// now we add the cols
	for (int i = 0; i < maxCol; i++) {

	    final int localCol = i;

	    ClickableTextCell cCell = new ClickableTextCell();

	    Column<SimpleCell[], String> column = new Column<SimpleCell[], String>(cCell) {
		@Override
		public String getValue(SimpleCell[] sch) {
		    if (sch.length > localCol)
			return sch[localCol].getContent();
		    else
			return "";
		}

		@Override
		public void onBrowserEvent(Context context, Element elem, SimpleCell[] object, NativeEvent event) {

		    EventTarget eventTarget = event.getEventTarget();
		    if (!Element.is(eventTarget)) {
			return;
		    }

		    Element currentElement = eventTarget.cast();

		    // Find the cell where the event occurred.
		    TableCellElement tableCell = findNearestParentCell(currentElement);
		    if (tableCell == null) {
			return;
		    }

		    Element trElem = tableCell.getParentElement();
		    if (trElem == null) {
			return;
		    }

		    TableRowElement tr = TableRowElement.as(trElem);
		    Element sectionElem = tr.getParentElement();
		    if (sectionElem == null) {
			return;
		    }

		    // int x = tableCell.getCellIndex();
		    // int y = tr.getRowIndex() - 1;

		    // TODO You can check which event you want to catch
		    // if (elem != null) {
		    if (event.getButton() == NativeEvent.BUTTON_LEFT) {
			if (localCol < object.length) {
			    showEditMenu(event.getClientX(), event.getClientY(), object[localCol], currentElement);
			}
		    }
		    // }
		}
	    };

	    this.addColumn(column);
	}

	List<SimpleCell[]> list = new ArrayList<SimpleCell[]>();
	for (int i = 0; i < cells.length; i++)
	    list.add(cells[i]);

	GWT.log(this.getClass().getName() + " List size : " + list.size());

	this.setPageSize(list.size());
	this.setRowCount(list.size(), true);
	this.setRowData(0, list);

	// be carefull, order is important
	this.setTablesCSS();
	this.setAttributesCSS();

	// this.setMergedCellsCSS();
    }

    @SuppressWarnings("unused")
    private void setMergedCellsCSS() {
	SimpleCell[][] cells = this.sheet.getCells();
	for (int row = 0; row < cells.length; row++) {
	    for (int col = 0; col < cells[row].length; col++) {
		if (cells[row][col].isMerged()) {
		    GWT.log("Merged : " + cells[row][col].getContent());
		    if (((Element) this.getRowElement(row).getCells().getItem(col)).getClassName() != null)
			((Element) this.getRowElement(row).getCells().getItem(col)).addClassName("mergedCell");
		    else
			((Element) this.getRowElement(row).getCells().getItem(col)).setClassName("mergedCell");
		}
	    }
	}
    }

    public void setAttributesCSS() {

	SimpleCell[][] cells = this.sheet.getCells();

	// color the attributes
	// and add the tables limits on the cells (using css)
	for (int row = 0; row < cells.length; row++) {
	    for (int col = 0; col < cells[row].length; col++) {
		// is attribute?
		if (cells[row][col].isAttribute()) {

		    if(cells[row][col].getContent() == null || cells[row][col].getContent().equals("")){
			cells[row][col].setEditedContent("Unnamed");
			((Element) this.getRowElement(row).getCells().getItem(col).getChild(0)).setInnerText("Unnamed");
			listener.updateElement(cells[row][col]);
		    }
		    // if matched DSPL type, put errors in red
		    if (cells[row][col].isDsplMapped()) {
			ArrayList<int[]> errors = cells[row][col].getErrorList();
			for (int[] err : errors) {
			    if(cells[err[1]][err[0]].getEngineScript().indexOf("formatter.str().replace") == -1){
				((Element) this.getRowElement(err[1]).getCells().getItem(err[0])).addClassName("errorCell");
				cells[err[1]][err[0]].setCurrentDsplMeta(cells[row][col].getCurrentDsplMeta());
			    }
			}
		    }
		    
		    ((Element) this.getRowElement(row).getCells().getItem(col).getChild(0)).addClassName("attributeCell");

		    // add the attr list
		    ListIterator<ConnexComposant> it = sheet.getConnexComps().listIterator();

		    while (it.hasNext()) {
			ConnexComposant cc = it.next();

			if (cc.containsPoint(cells[row][col].getJxlCol(), cells[row][col].getJxlRow())) {
			    cells[row][col].setCcStartX(cc.getStartX());
			    cells[row][col].setCcStartY(cc.getStartY());
			    break;
			}
		    }

		    this.attrCells.add(cells[row][col]);
		    // GWT.log("Size : " + this.attrCells.size());
		}
	    }
	}
    }

    public void removeTableCSS() {
	// TODO has to be optimized... brute algo
	ListIterator<ConnexComposant> it = sheet.getConnexComps().listIterator();

	while (it.hasNext()) {
	    ConnexComposant cc = it.next();
	    // case of cells is null
	    int startX = cc.getStartX();
	    int endX = cc.getEndX();
	    int startY = cc.getStartY();
	    int endY = cc.getEndY();

	    // draw!
	    for (int eleN = startX; eleN <= endX; eleN++) {
		if (this.getRowCount() > startY)
		    this.getRowElement(startY).getCells().getItem(eleN).setClassName("GALD-WOHC");
	    }

	    for (int eleN = startX; eleN <= endX; eleN++) {
		if (this.getRowCount() > endY)
		    this.getRowElement(endY).getCells().getItem(eleN).setClassName("GALD-WOHC");
	    }

	    for (int row = startY; row <= endY; row++) {

		// tab with only one col...
		if (startX == endX) {
		    if (row == startY || row == endY) {
			if (this.getRowCount() > row) {
			    this.getRowElement(row).getCells().getItem(startX).addClassName("GALD-WOHC");
			    this.getRowElement(row).getCells().getItem(endX).addClassName("GALD-WOHC");
			}
		    } else {
			if (this.getRowCount() > row) {
			    this.getRowElement(row).getCells().getItem(startX).setClassName("GALD-WOHC");
			    this.getRowElement(row).getCells().getItem(endX).addClassName("GALD-WOHC");
			}
		    }
		}
		//
		else if (row == startY || row == endY) {
		    if (this.getRowCount() > row) {
			this.getRowElement(row).getCells().getItem(startX).addClassName("GALD-WOHC");
			this.getRowElement(row).getCells().getItem(endX).addClassName("GALD-WOHC");
		    }
		}

		else {
		    if (this.getRowCount() > row) {
			this.getRowElement(row).getCells().getItem(startX).setClassName("GALD-WOHC");
			this.getRowElement(row).getCells().getItem(endX).setClassName("GALD-WOHC");
		    }
		}
	    }
	}
    }

    public void setTablesCSS() {
	// is in connexcomp?
	// TODO has to be optimized... brute algo
	ListIterator<ConnexComposant> it = sheet.getConnexComps().listIterator();

	while (it.hasNext()) {
	    ConnexComposant cc = it.next();
	    // case of cells is null
	    int startX = cc.getStartX();
	    int endX = cc.getEndX();
	    int startY = cc.getStartY();
	    int endY = cc.getEndY();

	    // draw!
	    for (int eleN = startX; eleN <= endX; eleN++) {
		if (this.getRowCount() > startY)
		    this.getRowElement(startY).getCells().getItem(eleN).setClassName("topTableCell");
	    }

	    for (int eleN = startX; eleN <= endX; eleN++) {
		if (this.getRowCount() > endY)
		    this.getRowElement(endY).getCells().getItem(eleN).setClassName("bottomTableCell");
		if (startY == endY) {
		    this.getRowElement(endY).getCells().getItem(eleN).addClassName("topTableCell");
		}

	    }

	    for (int row = startY; row <= endY; row++) {

		// tab with only one col...
		if (startX == endX) {
		    if (row == startY || row == endY) {
			if (this.getRowCount() > row) {
			    this.getRowElement(row).getCells().getItem(startX).addClassName("leftTableCell");
			    this.getRowElement(row).getCells().getItem(endX).addClassName("rightTableCell");
			}
		    } else {
			if (this.getRowCount() > row) {
			    this.getRowElement(row).getCells().getItem(startX).setClassName("leftTableCell");
			    this.getRowElement(row).getCells().getItem(endX).addClassName("rightTableCell");
			}
		    }
		}
		//
		else if (row == startY || row == endY) {
		    if (this.getRowCount() > row) {
			this.getRowElement(row).getCells().getItem(startX).addClassName("leftTableCell");
			this.getRowElement(row).getCells().getItem(endX).addClassName("rightTableCell");
		    }
		}

		else {
		    if (this.getRowCount() > row) {
			this.getRowElement(row).getCells().getItem(startX).setClassName("leftTableCell");
			this.getRowElement(row).getCells().getItem(endX).setClassName("rightTableCell");
		    }
		}

	    }
	}
    }

    public void showSelectedCell(int row, int col) {
	this.unselectAll();
	if (lastRow != -1) {
	    ((Element) this.getRowElement(row).getCells().getItem(col).getChild(0)).setClassName(lastStyleName);
	}
	lastStyleName = ((Element) this.getRowElement(row).getCells().getItem(col).getChild(0)).getClassName();
	lastRow = row;
	lastCol = col;
	if (((Element) this.getRowElement(row).getCells().getItem(col).getChild(0)).getClassName() != null)
	    ((Element) this.getRowElement(row).getCells().getItem(col).getChild(0)).addClassName("selectedSheetCell");
	else
	    ((Element) this.getRowElement(row).getCells().getItem(col).getChild(0)).setClassName("selectedSheetCell");
    }

    public void unselectAll() {
	if (lastRow != -1) {
	    // this.getCellFormatter().setStyleName(lastRow,lastCol,
	    // lastStyleName);
	    ((Element) this.getRowElement(lastRow).getCells().getItem(lastCol).getChild(0)).setClassName(lastStyleName);
	}
	lastRow = -1;
	lastCol = -1;
	lastStyleName = null;
    }

    public void setListener(SchemaCellTreeComposite w) {
	listener = w;
    }

    public ArrayList<SimpleCell> getAttrCells() {
	return attrCells;
    }

    /**
     * Return update cell or null
     * 
     * @param add
     * @param cell
     * @return
     */

    public SimpleCell updateListener(boolean add, SimpleCell cell) {
	if (listener != null) {
	    if (add) {
		return listener.addElement(cell);
	    } else {
		listener.removeElement(cell);
		return null;
	    }
	}
	return null;
    }

    public void updateElement(SimpleCell cell) {
	if (listener != null) {
	    listener.updateElement(cell);
	}
    }

    public void updateCellContent(SimpleCell cell) {
	this.getRowElement(cell.getJxlRow()).getCells().getItem(cell.getJxlCol()).setInnerText(cell.getEditedContent());
    }

    /**
     * Method used to validate or unvalidate attributes
     * 
     * @param cell
     *            The cell to edit
     * @param elem
     *            The HTML element to edit (CSS changing)
     */

    private void validateCell(final SimpleCell cell, final Element elem) {

	this.unselectAll();

	// not null
	if (cell != null) {

	    boolean add = false;

	    // is attribute
	    if (cell.isAttribute()) {
		// update the listener
		if (this.listener != null) {
		    this.updateListener(false, cell);
		}

		cell.setAttribute(false);
		this.getAttrCells().remove(cell);

		// set the style
		elem.setClassName("");

	    }

	    // is not attribute
	    else {

		add = true;
		cell.setAttribute(true);
		// put in the onSuccess

	    }

	    final boolean fadd = add;

	    // new
	    // add it
	    if (fadd) {

		// update the listener
		if (listener != null) {
		    updateListener(true, cell);
		}

		cell.setAttribute(true);
		getAttrCells().add(cell);

		// set the style
		if (elem.getClassName() != null)
		    elem.addClassName("attributeCell");
		else
		    elem.setClassName("attributeCell");

	    }
	}
    }

    private void showEditMenu(final int x, final int y, final SimpleCell cell, final Element elem) {

	final PopupPanel popup = new PopupPanel();
	// logic to create context menu depending on which cell was clicked
	MenuBar menu = new MenuBar(true);
	// add menu items
	// display menu where cell was clicked

	final Command validateCmd = new Command() {

	    public void execute() {
		validateCell(cell, elem);
		cell.setCurrentMeta("");
		if (cell.isAttribute()) {
		    elem.setClassName("attributeCell");
		}
		if (popup != null)
		    popup.hide(true);
	    }

	};

	class LocalCommand implements Command {

	    private final MenuItem item;

	    public LocalCommand(MenuItem item) {
		this.item = item;
	    }

	    @Override
	    public void execute() {

		if (cell.isAttribute() && cell.getCurrentMeta().equals("")) {
		    validateCell(cell, elem);
		}

		// not an attribute if comment or title
		if (!cell.getCurrentMeta().equals(item.getText().substring(item.getText().lastIndexOf(" ") + 1))) {
		    cell.setCurrentMeta(item.getText().substring(item.getText().lastIndexOf(" ") + 1));
		    item.setStyleName(item.getText().substring(item.getText().lastIndexOf(" ") + 1) + "Cell");
		    elem.setClassName(item.getText().substring(item.getText().lastIndexOf(" ") + 1) + "Cell");
		} else {
		    cell.setCurrentMeta("");
		    elem.setClassName("");
		}

		if (popup != null)
		    popup.hide(true);
	    }
	}

	MenuItem validateAttribute = new MenuItem("Define as attribute", validateCmd);

	if (cell.isAttribute()) {
	    validateAttribute.setStyleName("attributeCell");
	    elem.setClassName("attributeCell");
	}

	menu.addItem(validateAttribute);

	for (int i = 0; i < cell.getMetaInfos().length; i++) {

	    MenuItem tempItem = new MenuItem("Define as " + cell.getMetaInfos()[i], (Command) null);

	    if (!cell.getCurrentMeta().equals("") && cell.getMetaInfos()[i].equals(cell.getCurrentMeta()))
		tempItem.setStyleName(cell.getCurrentMeta() + "Cell");
	    tempItem.setCommand(new LocalCommand(tempItem));

	    menu.addItem(tempItem);
	}

	//
	List<ConnexComposant> ccs = this.getSheet().getConnexComps();
	ConnexComposant tmpGoodCc = null;
	for (ConnexComposant cc : ccs) {
	    if (cc.containsPoint(cell.getJxlCol(), cell.getJxlRow()))
		tmpGoodCc = cc;
	}

	final ConnexComposant goodCc = tmpGoodCc;

	Command validateBidimCmd = new Command() {
	    public void execute() {
		BidimEditorPopup bidim = new BidimEditorPopup(x, y, goodCc, cell, elem, listener);
		bidim.show();
		if (popup != null)
		    popup.hide(true);
	    }
	};

	MenuItem validateBidimAttribute = new MenuItem("Define as bidimensionnal attribute", validateBidimCmd);

	menu.addItem(validateBidimAttribute);

	//
	Command editCmd = new Command() {
	    public void execute() {
		showEditContentPopup(x, y, cell, elem);
		if (popup != null)
		    popup.hide(true);
	    }
	};

	Command addDescriptionCmd = new Command() {
	    public void execute() {
		showEditDescriptionPopup(x, y, cell);
		if (popup != null)
		    popup.hide(true);
	    }
	};

	MenuItem editContent = new MenuItem("Edit content", editCmd);
	MenuItem addDescription = new MenuItem("Edit description", addDescriptionCmd);

	menu.addSeparator();
	menu.addItem(editContent);
	menu.addItem(addDescription);
	menu.addSeparator();

	Command mlShowCmd = new Command() {
	    public void execute() {
		showMlPop(x, y, cell);
	    }
	};

	MenuItem mlShow = new MenuItem("Show machine learning", mlShowCmd);
	menu.addItem(mlShow);

	menu.addSeparator();

	// add the dspl concepts
	if (cell.isAttribute()) {
	    MenuBar dsplConcepts = new MenuBar(true);

	    dsplConcepts.setAnimationEnabled(true);
	    dsplConcepts.setAutoOpen(true);

	    class DSPLLocalCommand implements Command {

		private final MenuItem item;

		public DSPLLocalCommand(MenuItem item) {
		    this.item = item;
		}

		@Override
		public void execute() {
		    cell.setCurrentDsplMeta(item.getText());
		    if (popup != null)
			popup.hide(true);
		}
	    }

	    for (int i = 0; i < cell.getDsplMetas().length; i++) {
		MenuItem tempItem = new MenuItem(cell.getDsplMetas()[i], (Command) null);
		tempItem.setCommand(new DSPLLocalCommand(tempItem));
		if (cell.getDsplMetas()[i].equals(cell.getCurrentDsplMeta()))
		    tempItem.setStyleName("attributeCell");
		dsplConcepts.addItem(tempItem);
	    }

	    menu.addItem("DSPL concept", dsplConcepts);
	}
	//

	// new item to switch attributes
	menu.addSeparator();

	class SwitchAttrsLocalCommand implements Command {
	    @Override
	    public void execute() {
		// switch
		// find the good CC first
		List<ConnexComposant> ccs = sheet.getConnexComps();
		for (ConnexComposant cc : ccs) {
		    // set attrInLines if needed
		    if (firstTime[0]) {
			for (int i = 0; i < attrCells.size() - 1; i++) {
			    if (cc.containsPoint(attrCells.get(i).getJxlCol(), attrCells.get(i).getJxlRow())
				    && cc.containsPoint(attrCells.get(i + 1).getJxlCol(), attrCells.get(i + 1).getJxlRow())
				    && attrCells.get(i).getJxlCol() == attrCells.get(i + 1).getJxlCol()) {
				cc.setAttrInLines(true);
				firstTime[0] = false;
				GWT.log(firstTime[0] + "");
				break;
			    }
			}
		    }

		    if (cc.containsPoint(cell.getJxlCol(), cell.getJxlRow())) {
			// good one
			GWT.log(cc.toString());

			// attributes in line
			if (cc.isAttrInLines()) {
			    GWT.log("in line");
			    int selectedCol = cell.getJxlCol();
			    int selectedRow = cell.getJxlRow();

			    // unselect the col
			    for (int line = selectedRow; line < cc.getEndY() + 1; line++) {
				if (sheet.getCells().length > line && sheet.getCells()[line].length > selectedCol) {
				    SimpleCell sCell = sheet.getCells()[line][selectedCol];
				    if (attrCells.remove(sCell)) {
					updateListener(false, sCell);
					sCell.setAttribute(false);
					sheet.getCells()[line][selectedCol] = sCell;
					((Element) getRowElement(line).getCells().getItem(selectedCol).getChild(0)).setClassName("");
				    }
				}
			    }

			    // select the line
			    for (int col = cc.getStartX(); col < cc.getEndX() + 1; col++) {
				if (sheet.getCells().length > selectedRow && sheet.getCells()[selectedRow].length > col) {
				    SimpleCell sCell = sheet.getCells()[selectedRow][col];
				    if (sCell.getContent() != null && !sCell.getContent().equals("")) {
					sCell.setAttribute(true);
					if (attrCells.add(sCell)) {
					    updateListener(true, sCell);
					    sheet.getCells()[selectedRow][col] = sCell;
					    if (((Element) getRowElement(selectedRow).getCells().getItem(col).getChild(0)) != null
						    && ((Element) getRowElement(selectedRow).getCells().getItem(col).getChild(0)).getClassName() != null) {
						((Element) getRowElement(selectedRow).getCells().getItem(col).getChild(0)).addClassName("attributeCell");
					    }
					}
				    }
				}
			    }
			    cc.setAttrInLines(false);
			}
			// attributes in col
			else {
			    GWT.log("in col");
			    int selectedCol = cell.getJxlCol();
			    int selectedRow = cell.getJxlRow();

			    // unselect the col
			    for (int col = selectedCol; col < cc.getEndY() + 1; col++) {
				if (sheet.getCells().length > selectedRow && sheet.getCells()[selectedRow].length > col) {
				    SimpleCell sCell = sheet.getCells()[selectedRow][col];
				    if (attrCells.remove(sCell)) {
					updateListener(false, sCell);
					sCell.setAttribute(false);
					sheet.getCells()[selectedRow][col] = sCell;
					((Element) getRowElement(selectedRow).getCells().getItem(col).getChild(0)).setClassName("");
				    }
				}
			    }

			    // select the line
			    for (int line = cc.getStartY(); line < cc.getEndY() + 1; line++) {
				if (sheet.getCells().length > line && sheet.getCells()[line].length > selectedCol) {
				    SimpleCell sCell = sheet.getCells()[line][selectedCol];
				    if (sCell.getContent() != null && !sCell.getContent().equals("")) {
					sCell.setAttribute(true);
					if (attrCells.add(sCell)) {
					    updateListener(true, sCell);
					    sheet.getCells()[line][selectedCol] = sCell;
					    if (((Element) getRowElement(line).getCells().getItem(selectedCol).getChild(0)) != null
						    && ((Element) getRowElement(line).getCells().getItem(selectedCol).getChild(0)).getClassName() != null) {
						((Element) getRowElement(line).getCells().getItem(selectedCol).getChild(0)).addClassName("attributeCell");
					    }
					}
				    }
				}
			    }
			    cc.setAttrInLines(true);
			}
		    }
		}
		//
		redraw();
		setTablesCSS();
		setAttributesCSS();
		//
		if (popup != null)
		    popup.hide(true);
	    }
	}

	MenuItem switchAttrs = new MenuItem("Switch attributes", new SwitchAttrsLocalCommand());
	menu.addItem(switchAttrs);
	//

	// Add the DSPLEngine script editor
	if (!cell.isAttribute()) {
	    menu.addSeparator();

	    Command editScriptCmd = new Command() {
		public void execute() {
		    showEditScriptPopup(x, y, cell);
		    if (popup != null)
			popup.hide(true);
		}
	    };

	    MenuItem editScript = new MenuItem("Edit DSPLEngine script", editScriptCmd);
	    menu.addItem(editScript);
	    // }
	    //

	    // Add item for use of replace in DSPLEngine scripting
	    Command replaceScriptCmd = new Command() {
		public void execute() {
		    showReplaceScriptPopup(x, y, cell, elem);
		    if (popup != null)
			popup.hide(true);
		}
	    };

	    MenuItem replaceScript = new MenuItem("Use formatter.str().replace() script", replaceScriptCmd);
	    menu.addItem(replaceScript);
	}

	// Add a fake for linked between docs
	/*
	 * menu.addSeparator();
	 * 
	 * // add the dspl concepts if (cell.isAttribute()) { MenuBar
	 * joinConcepts = new MenuBar(true);
	 * 
	 * joinConcepts.setAnimationEnabled(true);
	 * joinConcepts.setAutoOpen(true);
	 * 
	 * class DSPLLocalCommand implements Command {
	 * 
	 * private final MenuItem item;
	 * 
	 * public DSPLLocalCommand(MenuItem item) { this.item = item; }
	 * 
	 * @Override public void execute() { if (popup != null)
	 * popup.hide(true); } }
	 * 
	 * String[] names = new String[5]; names[0] =
	 * "<b>Document :</b> Taux de chômage France (y compris Dom) de 1996 à 2011 </br><b>Attribut :</b> Tx chom."
	 * ; names[1] =
	 * "<b>Document :</b> Halo du chômage et du sous emploi en 2010 </br><b>Attribut :</b> Valeur en %"
	 * ; names[2] =
	 * "<b>Document :</b> Taux de chômage de la population active selon le niveau de diplôme </br><b>Attribut :</b> Taux de chômage"
	 * ; names[3] =
	 * "<b>Document :</b> Taux de chômage en France de 2000 à 2010 </br><b>Attribut :</b> Tx"
	 * ; names[4] =
	 * "<b>Document :</b> Données sur le chômage des jeunes en phase d'insertion professionnelle en 2010 </br><b>Attribut :</b> Taux (en %)"
	 * ;
	 * 
	 * for (int i = 0; i < names.length; i++) {
	 * joinConcepts.addItem(names[i],true,(Command)null); }
	 * 
	 * menu.addItem("Join with", joinConcepts); }
	 */
	//

	//

	popup.add(menu);

	popup.setAnimationEnabled(true);
	popup.setAutoHideEnabled(true);

	// check if popup is in the window
	if (Window.getClientWidth() < x + 220)
	    popup.setPopupPosition(Window.getClientWidth() - 220, y);
	else
	    popup.setPopupPosition(x, y);

	popup.show();

    }

    private void showMlPop(final int x, final int y, final SimpleCell cell) {

	if (cell != null) {

	    // make the popup
	    if (mlPop != null && mlPop.isShowing())
		mlPop.hide();
	    // pop up with all scores

	    VerticalPanel vertical = new VerticalPanel();
	    HTML title = new HTML("<b>Cell ML criterium</b>");
	    vertical.add(title);

	    final int localCol = cell.getJxlCol();
	    final int localRow = cell.getJxlRow();
	    final SpreadsheetEditor ftable = this;

	    // if cell not setted, get it
	    if (cell.getIs_attributeML() == -1.0) {

		service.getCellFromML(cell.getUsername(), cell.getFilename(), cell.getSheet(), cell.getJxlCol(), cell.getJxlRow(),
			new AsyncCallback<SimpleCell>() {

			    @Override
			    public void onFailure(Throwable caught) {

			    }

			    @Override
			    public void onSuccess(SimpleCell result) {

				if (result != null) {

				    // set the cell
				    ftable.getCells()[localRow][localCol].setAbove_cell(result.getAbove_cell());
				    ftable.getCells()[localRow][localCol].setBehind_cell(result.getBehind_cell());
				    ftable.getCells()[localRow][localCol].setFirst_cc_col(result.getFirst_cc_col());
				    ftable.getCells()[localRow][localCol].setFirst_cc_row(result.getFirst_cc_row());
				    ftable.getCells()[localRow][localCol].setLeft_cell(result.getLeft_cell());
				    ftable.getCells()[localRow][localCol].setRight_cell(result.getRight_cell());
				    ftable.getCells()[localRow][localCol].setType(result.getType());
				    ftable.getCells()[localRow][localCol].setIs_attributeML(result.getIs_attributeML());

				    if (mlPop != null && mlPop.isShowing())
					mlPop.hide();
				    // pop up with all scores

				    VerticalPanel vertical = new VerticalPanel();
				    HTML title = new HTML("<b>Cell ML criterium</b>");
				    vertical.add(title);

				    FlexTable table = new FlexTable();

				    table.setWidget(0, 0, new Label("Above cell : "));
				    table.setWidget(0, 1, new GradientScore(100, result.getAbove_cell()));

				    table.setWidget(1, 0, new Label("Behind cell : "));
				    table.setWidget(1, 1, new GradientScore(100, result.getBehind_cell()));

				    table.setWidget(2, 0, new Label("First cc col : "));
				    table.setWidget(2, 1, new GradientScore(100, result.getFirst_cc_col()));

				    table.setWidget(3, 0, new Label("First cc row : "));
				    table.setWidget(3, 1, new GradientScore(100, result.getFirst_cc_row()));

				    table.setWidget(4, 0, new Label("Left cell : "));
				    table.setWidget(4, 1, new GradientScore(100, result.getLeft_cell()));

				    table.setWidget(5, 0, new Label("Right cell : "));
				    table.setWidget(5, 1, new GradientScore(100, result.getRight_cell()));

				    table.setWidget(6, 0, new Label("Type : "));
				    table.setWidget(6, 1, new GradientScore(100, result.getType()));

				    table.setWidget(7, 0, new HTML("<b>Is attribute for ML : </b>"));
				    if (result.getIs_attributeML() == 1.0)
					table.setWidget(7, 1, new HTML("<b>yes</b>"));
				    else
					table.setWidget(7, 1, new HTML("<b>no</b>"));

				    table.setWidget(8, 0, new HTML("<b>Is attribute for expert : </b>"));
				    if (cell.isAttribute())
					table.setWidget(8, 1, new HTML("<b>yes</b>"));
				    else
					table.setWidget(8, 1, new HTML("<b>no</b>"));

				    vertical.add(table);

				    mlPop = new PopupPanel(true);
				    mlPop.setGlassEnabled(false);
				    mlPop.setPopupPosition(x, y);
				    mlPop.add(vertical);
				    mlPop.setAutoHideEnabled(true);
				    mlPop.show();
				}
			    }

			});

	    } else {
		FlexTable table = new FlexTable();

		table.setWidget(0, 0, new Label("Above cell : "));
		table.setWidget(0, 1, new GradientScore(100, cell.getAbove_cell()));

		table.setWidget(1, 0, new Label("Behind cell : "));
		table.setWidget(1, 1, new GradientScore(100, cell.getBehind_cell()));

		table.setWidget(2, 0, new Label("First cc col : "));
		table.setWidget(2, 1, new GradientScore(100, cell.getFirst_cc_col()));

		table.setWidget(3, 0, new Label("First cc row : "));
		table.setWidget(3, 1, new GradientScore(100, cell.getFirst_cc_row()));

		table.setWidget(4, 0, new Label("Left cell : "));
		table.setWidget(4, 1, new GradientScore(100, cell.getLeft_cell()));

		table.setWidget(5, 0, new Label("Right cell : "));
		table.setWidget(5, 1, new GradientScore(100, cell.getRight_cell()));

		table.setWidget(6, 0, new Label("Type : "));
		table.setWidget(6, 1, new GradientScore(100, cell.getType()));

		table.setWidget(7, 0, new HTML("<b>Is attribute for ML : </b>"));
		if (cell.getIs_attributeML() == 1.0)
		    table.setWidget(7, 1, new HTML("<b>yes</b>"));
		else
		    table.setWidget(7, 1, new HTML("<b>no</b>"));

		table.setWidget(8, 0, new HTML("<b>Is attribute for expert : </b>"));
		if (cell.isAttribute())
		    table.setWidget(8, 1, new HTML("<b>yes</b>"));
		else
		    table.setWidget(8, 1, new HTML("<b>no</b>"));

		vertical.add(table);

		mlPop = new PopupPanel(true);
		mlPop.setGlassEnabled(false);
		mlPop.setPopupPosition(x, y);
		mlPop.add(vertical);
		mlPop.setAutoHideEnabled(true);
		mlPop.show();
	    }
	}
    }

    public SimpleCell[][] getCells() {
	if (sheet != null)
	    return sheet.getCells();
	else
	    return null;
    }

    private void showEditDescriptionPopup(int x, int y, SimpleCell cell) {
	EditDescriptionPopup descPop = new EditDescriptionPopup(cell);
	descPop.setGlassEnabled(true);
	descPop.setPopupPosition(x, y);

	descPop.show();
    }

    private void showEditContentPopup(int x, int y, SimpleCell cell, Element elem) {
	EditContentPopup descPop = new EditContentPopup(cell, elem, listener);
	descPop.setGlassEnabled(true);
	descPop.setPopupPosition(x, y);

	descPop.show();
    }

    private void showEditScriptPopup(int x, int y, SimpleCell cell) {
	EditScriptPopup descPop = new EditScriptPopup(cell);
	descPop.setGlassEnabled(true);
	if (x > 100)
	    descPop.setPopupPosition(x - 200, y + 20);
	else
	    descPop.setPopupPosition(x, y + 20);

	descPop.show();
    }

    private void showReplaceScriptPopup(int x, int y, SimpleCell cell, Element elem) {
	new ReplaceScriptPopup(cell, elem, listener, x, y);
    }

    public SimpleSheet getSheet() {
	return sheet;
    }

    public void setSheet(SimpleSheet sheet) {
	this.sheet = sheet;
    }
}
