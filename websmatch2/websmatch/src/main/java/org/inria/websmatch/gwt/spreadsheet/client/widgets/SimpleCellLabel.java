package org.inria.websmatch.gwt.spreadsheet.client.widgets;

import org.inria.websmatch.gwt.spreadsheet.client.MachineLearningService;
import org.inria.websmatch.gwt.spreadsheet.client.MachineLearningServiceAsync;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;

import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class SimpleCellLabel extends Label {

    private SimpleCell cell;

    public SimpleCellLabel(SimpleCell cell, boolean useML) {
	super(cell.getContent());
	this.cell = cell;

	if (useML) {
	    final SimpleCellLabel scl = this;

	    this.addMouseOverHandler(new MouseOverHandler() {

		@Override
		public void onMouseOver(MouseOverEvent event) {
		    if (scl.cell != null) {
			if (scl.cell.isAttribute()) {
			    scl.setTitle("Machine learning values : (first_cc_col : "
				    + scl.cell.getFirst_cc_col()
				    + ") "
				    + "(fisrt_cc_row : "
				    + scl.cell.getFirst_cc_row()
				    + ") (type : "
				    + scl.cell.getType()
				    + ") (behind_cell : "
				    + scl.cell.getBehind_cell()
				    + ") (right_cell : "
				    + scl.cell.getRight_cell()
				    + ") (is_attribute : "
				    + scl.cell.getIs_attributeML() + ")");
			}

			else {

			    // get ML attributes
			    MachineLearningServiceAsync mlservice = MachineLearningService.Util
				    .getInstance();

			    AsyncCallback<SimpleCell> callback = new AsyncCallback<SimpleCell>() {

				@Override
				public void onFailure(Throwable caught) {

				}

				@Override
				public void onSuccess(SimpleCell result) {
				    if(result != null){
				    scl.cell.setFirst_cc_col(result.getFirst_cc_col());
				    scl.cell.setFirst_cc_row(result
					    .getFirst_cc_row());
				    scl.cell.setType(result.getType());
				    scl.cell.setBehind_cell(result
					    .getBehind_cell());
				    scl.cell.setRight_cell(result
					    .getRight_cell());
				    scl.cell.setIs_attributeML(result
					    .getIs_attributeML());

				    scl.setTitle("Machine learning values : (first_cc_col : "
						    + scl.cell
							    .getFirst_cc_col()
						    + ") "
						    + "(fisrt_cc_row : "
						    + scl.cell
							    .getFirst_cc_row()
						    + ") (type : "
						    + scl.cell.getType()
						    + ") (behind_cell : "
						    + scl.cell.getBehind_cell()
						    + ") (right_cell : "
						    + scl.cell.getRight_cell()
						    + ") (is_attribute : "
						    + scl.cell
							    .getIs_attributeML()
						    + ")");
				    }
				}
			    };

			    mlservice.getCellFromML(scl.cell.getUsername(),
				    scl.cell.getFilename(),
				    scl.cell.getSheet(), scl.cell.getJxlCol(),
				    scl.cell.getJxlRow(), callback);
			}
		    }
		}
	    });
	}
    }
}
