package org.inria.websmatch.xls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.inria.websmatch.connexComposant.ConnexComposantDetector;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;
import org.inria.websmatch.utils.DateUtils;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.connexComposant.ConnexComposantDetector;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleSheet;

public class XLSParser {
    
    private String fileURI;
    
    /**
     * 
     * @param fileURI The total URI
     */
    
    public XLSParser(String fileURI){
	
	this.fileURI = fileURI;
	
    }
    
    /**
     * 
     * @param userName Username to set for the document
     * @param fileName The filename
     * @return
     */
    
    public SimpleSheet[] parseFile(String userName, String fileName){
	
	ArrayList<SimpleSheet> results = new ArrayList<SimpleSheet>();
	    ArrayList<ArrayList<ConnexComposant>> connexComps = new ArrayList<ArrayList<ConnexComposant>>();
	    ConnexComposantDetector ccDetect = new ConnexComposantDetector();
	
	    try {
		InputStream fileStream = null;

		try {
		    fileStream = new URI("file:"+this.fileURI.replaceAll("\\s", "%20")).toURL().openStream();

		    WorkbookSettings ws = new WorkbookSettings();
		    ws.setEncoding("ISO-8859-1");
		    ws.setIgnoreBlanks(true);
		    Workbook workbook = Workbook.getWorkbook(new File(this.fileURI), ws);

		    int numSheets = workbook.getNumberOfSheets();

		    ArrayList<ArrayList<Cell[]>> datas = new ArrayList<ArrayList<Cell[]>>();
		    ArrayList<String> sheetNames = new ArrayList<String>();

		    for (int s = 0; s < numSheets; s++) {
			Sheet sheet = workbook.getSheet(s);
			String sheetName = sheet.getName();
			sheetNames.add(sheetName);

			// for each sheet, we will search for the tables in it
			ArrayList<Cell[]> sheetCells = new ArrayList<Cell[]>();
			for (int i = 0; i < sheet.getRows(); i++)
			    sheetCells.add(sheet.getRow(i));

			datas.add(sheetCells);
			
			// now we search for the zones
			connexComps.add(ccDetect.connexDetection(sheetCells, sheet.getColumns()));

		    }

		    ArrayList<SimpleCell[]> cells = new ArrayList<SimpleCell[]>();

		    for (int sheet = 0; sheet < datas.size(); sheet++) {

			int maxX = 0;

			cells = new ArrayList<SimpleCell[]>();

			// now we iterate to get the cells/attributes
			ListIterator<Cell[]> it = datas.get(sheet).listIterator();

			int line = 0;

			while (it.hasNext()) {

			    Cell[] rowCells = it.next();

			    if (rowCells.length - 1 > maxX)
				maxX = rowCells.length - 1;

			    SimpleCell[] tmpSC = new SimpleCell[rowCells.length];

			    for (int i = 0; i < rowCells.length; i++) {

				boolean isAttribute = false;

				if (rowCells[i] != null) {
				    
				    // handle the dateformat problem
				    if(rowCells[i].getType().equals(CellType.DATE)){
					//					
					String formattedDate = DateUtils.convertDate(((DateCell)rowCells[i]).getDate().toString());
					if(formattedDate == null){
					    formattedDate = rowCells[i].getContents();
					}
					//
					tmpSC[i] = new SimpleCell(formattedDate, isAttribute, rowCells[i].getRow(), rowCells[i].getColumn(), sheet);					    
					tmpSC[i].setFormat("dd/MM/yyyy");
				    }
				    //
				    else tmpSC[i] = new SimpleCell(rowCells[i].getContents(), isAttribute, rowCells[i].getRow(), rowCells[i].getColumn(), sheet);
				    
				    
				} else
				    tmpSC[i] = new SimpleCell(new String(), isAttribute, -1, -1, sheet);
				
				tmpSC[i].setUsername(userName);
				tmpSC[i].setFilename(fileName);

			    }

			    cells.add(tmpSC);
			    line++;

			}

			if (cells.size() > 0) {
			    results.add(new SimpleSheet());
			    results.get(results.size() - 1).setFilename(fileName); // set
									      // the
									      // title
			    results.get(results.size() - 1).setTitle(sheetNames.get(sheet));

			    results.get(results.size() - 1).setCells(cells.toArray(new SimpleCell[cells.size()][]));

			    results.get(results.size() - 1).setConnexComps(connexComps.get(sheet));
			}
		    }

		} catch (BiffException e) {
		    L.Error(e.getMessage(),e);
		    if (fileStream != null)
			    fileStream.close();
		    return null;
		}
		if (fileStream != null)
		    fileStream.close();
		
	    } catch (IOException e) {
		L.Error(e.getMessage(),e);
	    } catch (URISyntaxException e1) {
		e1.printStackTrace();
	    }
	    	     
	    // remove last lines if void lines
	    ArrayList<SimpleSheet> cleanedResults = new ArrayList<SimpleSheet>();
	    
	    for(int i = 0; i < results.size(); i++){	
		SimpleSheet s = results.get(i);
		SimpleCell[][] c = s.getCells();
		
		int lastNonVoidLine = -1;
		
		for(int j = c.length - 1; j >= 0; j--){
		    if(c[j].length == 0) lastNonVoidLine = j;
		    else break;
		}
		
		if(lastNonVoidLine == -1) lastNonVoidLine = c.length;
		
		SimpleCell[][] cleanedCells = Arrays.copyOf(c, lastNonVoidLine);
		s.setCells(cleanedCells);
		
		cleanedResults.add(s);			
	    }
	    
	    return cleanedResults.toArray(new SimpleSheet[cleanedResults.size()]);	
	    //return results.toArray(new SimpleSheet[results.size()]);	
    }

}
