package org.inria.websmatch.dspl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jxl.Cell;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dspl.entities.EntityMatcher;
import org.inria.websmatch.dspl.entities.collections.Communes;
import org.inria.websmatch.dspl.entities.collections.Dates;
import org.inria.websmatch.dspl.entities.collections.Departements;
import org.inria.websmatch.dspl.entities.collections.Locations;
import org.inria.websmatch.dspl.entities.collections.Payss;
import org.inria.websmatch.dspl.entities.collections.Quarters;
import org.inria.websmatch.dspl.entities.collections.Ratios;
import org.inria.websmatch.dspl.entities.collections.Regions;
import org.inria.websmatch.dspl.entities.collections.Years;
import org.inria.websmatch.dsplEngine.geo.datapublica.Commune;
import org.inria.websmatch.dsplEngine.geo.datapublica.Departement;
import org.inria.websmatch.dsplEngine.geo.datapublica.Loaders;
import org.inria.websmatch.dsplEngine.geo.datapublica.Pays;
import org.inria.websmatch.dsplEngine.geo.datapublica.Region;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleCell;
import org.inria.websmatch.utils.L;

public class EntityMatcherImpl {

	// used matchers
	EntityMatcher matcher;
	List<HashMap<String, Entity>> entities;
	List<String> dsplTypes;

	public EntityMatcherImpl() {
		entities = new ArrayList<HashMap<String, Entity>>();

		Ratios ratios = new Ratios();
		Years years = new Years();
		Dates dates = new Dates();
		Quarters quarters = new Quarters();

		// dp ones
		Payss payss = new Payss();
		Regions regions = new Regions();
		Departements dpts = new Departements();
		Communes coms = new Communes();
		//

		Locations locs = new Locations();

		entities.add(ratios.getRatios());
		entities.add(years.getYears());
		entities.add(dates.getDates());
		entities.add(quarters.getQuarters());

		// we load the locations from data publica engine
		Loaders loaders = Loaders.getInstance();
		// pays
		List<String[]> res = loaders.getPays();
		for (String[] loc : res) {
			if (loc[1].equals(""))
				loc[1] = "0";
			if (loc[2].equals(""))
				loc[2] = "0";
			payss.addPays(new Pays(loc[3].replace("\"", ""), "", new Float(loc[1].replace("\"", "")), new Float(loc[2].replace("\"", "")), loc[0].replace("\"",
					""), loc[4].replace("\"", "")));
		}
		// regions
		res = loaders.getRegions();
		for (String[] loc : res) {
			if (loc[2].equals(""))
				loc[2] = "0";
			if (loc[3].equals(""))
				loc[3] = "0";
			regions.addRegion(new Region(loc[1].replace("\"", ""), "", new Float(loc[2]), new Float(loc[3]), loc[0].replace("\"", ""), loc[4].replace("\"", "")));
		}
		// departements
		res = loaders.getDepartements();
		for (String[] loc : res) {
			if (loc[2].equals(""))
				loc[2] = "0";
			if (loc[3].equals(""))
				loc[3] = "0";
			dpts.addDepartement(new Departement(loc[1].replace("\"", ""), "", new Float(loc[2]), new Float(loc[3]), loc[0].replace("\"", ""), loc[4].replace(
					"\"", "")));
		}
		// communes
		res = loaders.getCommunes();
		for (String[] loc : res) {
			if (loc[2].equals(""))
				loc[2] = "0";
			if (loc[3].equals(""))
				loc[3] = "0";
			coms.addCommune(new Commune(loc[1].replace("\"", ""), "", new Float(loc[2]), new Float(loc[3]), loc[0].replace("\"", ""), loc[4].replace("\"", "")));
		}
		//
		entities.add(payss.getPayssByCode());
		entities.add(payss.getPayssByName());
		entities.add(payss.getPayssByUKName());
		entities.add(regions.getRegionsByCode());
		entities.add(regions.getRegionsByName());
		entities.add(dpts.getDepartementsByCode());
		entities.add(dpts.getDepartementsByName());
		entities.add(coms.getCommunesByCode());
		entities.add(coms.getCommunesByName());
		//

		entities.add(locs.getLocations());

		dsplTypes = new ArrayList<String>();
		dsplTypes.add("quantity:ratio");
		dsplTypes.add("time:year");
		dsplTypes.add("time:time_point");
		dsplTypes.add("time:time_point");

		// the datapublica files
		dsplTypes.add("dp:pays");
		dsplTypes.add("dp:pays");
		dsplTypes.add("dp:pays");
		dsplTypes.add("dp:region");
		dsplTypes.add("dp:region");
		dsplTypes.add("dp:departement");
		dsplTypes.add("dp:departement");
		dsplTypes.add("dp:commune");
		dsplTypes.add("dp:commune");
		//

		// on the last
		dsplTypes.add("geo:location");
	}

	public SimpleCell matchLine(SimpleCell cell, Cell[] line) {

		// we want to set the dspl type now
		for (int i = 0; i < entities.size(); i++) {
			ArrayList<Cell> toMatch = new ArrayList<Cell>();
			matcher = new EntityMatcher(entities.get(i));

			for (Cell c : line) {
				if (c != null && c.getContents() != null) {
					toMatch.add(c);
				}
			}

			cell = matcher.match(cell, toMatch);
			if (cell.isDsplMapped()) {
				L.Debug(this.getClass().getSimpleName(), cell.getContent() + " is a " + dsplTypes.get(i), true);
				cell.setCurrentDsplMeta(dsplTypes.get(i));
				cell.setAttribute(true);
				if (cell.getContent() == null || cell.getContent().equals("")) {
					cell.setEditedContent(dsplTypes.get(i) + " (detected)");
					cell.setContent(dsplTypes.get(i) + " (detected)");
				}
				return cell;
			} else {
				cell.setErrorList(new ArrayList<int[]>());
			}
		}
		//

		return cell;
	}

	public SimpleCell match(SimpleCell cell, ArrayList<ConnexComposant> ccs, ArrayList<List<Cell[]>> datas) {

		boolean inLineAttributes = false;

		int endRow = cell.getJxlRow() + 6;
		int endCol = cell.getJxlCol() + 6;

		for (ConnexComposant cc : ccs) {
			if (cc.getStartX() <= cell.getJxlCol() && cell.getJxlCol() <= cc.getEndX() && cc.getStartY() <= cell.getJxlRow()
					&& cell.getJxlRow() <= cc.getEndY()) {
				inLineAttributes = cc.isAttrInLines();
				if (!inLineAttributes)
					endRow = cc.getEndY() + 1;
				else
					endCol = cc.getEndX() + 1;
			}
		}

		// we want to set the dspl type now
		for (int i = 0; i < entities.size(); i++) {
			ArrayList<Cell> toMatch = new ArrayList<Cell>();
			matcher = new EntityMatcher(entities.get(i));

			if (!inLineAttributes) {
				for (int l = cell.getJxlRow() + 1; l < endRow; l++) {
					if (datas.get(cell.getSheet()).size() > l && datas.get(cell.getSheet()).get(l).length > cell.getJxlCol()
							&& !datas.get(cell.getSheet()).get(l)[cell.getJxlCol()].getContents().trim().equals(""))
						// toMatch.add(datas.get(cell.getSheet()).get(l)[cell.getJxlCol()].getContents());
						toMatch.add(datas.get(cell.getSheet()).get(l)[cell.getJxlCol()]);
				}
			} else {
				for (int c = cell.getJxlCol() + 1; c < endCol; c++) {
					if (datas.get(cell.getSheet()).get(cell.getJxlRow()).length > c
							&& !datas.get(cell.getSheet()).get(cell.getJxlRow())[c].getContents().trim().equals(""))
						// toMatch.add(datas.get(cell.getSheet()).get(cell.getJxlRow())[c].getContents());
						toMatch.add(datas.get(cell.getSheet()).get(cell.getJxlRow())[c]);
				}
			}

			if (toMatch.size() >= 1)
				L.Debug(this.getClass().getSimpleName(), "Match " + cell.getContent() + " with " + dsplTypes.get(i), true);

			cell = matcher.match(cell, toMatch);
			if (cell.isDsplMapped()) {
				L.Debug(this.getClass().getSimpleName(), cell.getContent() + " is a " + dsplTypes.get(i), true);
				// stop at the first type found
				cell.setCurrentDsplMeta(dsplTypes.get(i));
				// stop at the first type found
				return cell;
			} else {
				cell.setErrorList(new ArrayList<int[]>());
			}

		}
		//
		return cell;
	}
}
