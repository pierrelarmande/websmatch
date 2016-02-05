package org.inria.websmatch.gwt.spreadsheet.server;

import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import javax.xml.rpc.ServiceException;

import org.inria.websmatch.evaluate.ProbaMatching;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaService;
import org.inria.websmatch.gwt.spreadsheet.client.listeners.MatchingProgressEvent;
import org.inria.websmatch.gwt.spreadsheet.client.models.SchemaData;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleEdge;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleGraphComponent;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleVertex;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleAttribute;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSubtype;
import org.inria.websmatch.utils.L;
import org.inria.websmatch.gwt.spreadsheet.client.SchemaService;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleEdge;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleGraphComponent;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleMatchTech;
import org.inria.websmatch.gwt.spreadsheet.client.models.SimpleVertex;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleEntity;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSchemaElement;
import org.inria.websmatch.gwt.spreadsheet.client.models.generic.SimpleSubtype;
import org.inria.websmatch.utils.L;
import org.mitre.harmony.Harmony;
import org.mitre.harmony.matchers.ElementPair;
import org.mitre.harmony.matchers.MatcherScores;
import org.mitre.harmony.matchers.matchers.YAMMatcherWrapper;
import org.mitre.schemastore.model.Attribute;
import org.mitre.schemastore.model.Entity;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.SchemaElementList;
import org.mitre.schemastore.model.Subtype;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.porters.ImporterException;
import org.mitre.schemastore.porters.schemaImporters.IOWLImporter;
import org.mitre.schemastore.porters.schemaImporters.OsmozeImporter;
import org.mitre.schemastore.porters.schemaImporters.SchemaImporter;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

import yam.system.Configs;
import yam.tools.WordNetHelper;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.RemoteEventServiceServlet;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.util.Pair;

public class SchemaServiceImpl extends RemoteEventServiceServlet implements SchemaService {

	/**
	 *
	 */
	private static final long serialVersionUID = 5420496800348266765L;
	private String storeService;
	private String baseXLSDir;

	public void init() {
		storeService = getServletContext().getInitParameter("schemaStoreService");
		baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");

		// init WordNet
	/*yam.system.Configs.WNTMP = "WNTemplate.xml";
	yam.system.Configs.WNPROP = "file_properties.xml";

	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}*/
		L.Debug(this.getClass().getSimpleName(), "Current dir : " + System.getProperty("user.dir"), true);
		// init WordNet
		yam.system.Configs.WNTMP = System.getProperty("user.dir") + "/webapps/WebSmatch/WNTemplate.xml";
		yam.system.Configs.WNPROP = System.getProperty("user.dir") + "/webapps/WebSmatch/file_properties.xml";

		yam.system.Configs.WNDIR = System.getProperty("user.dir") + "/webapps/WebSmatch/WordNet/2.1/dict";

		try {
			WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
			WordNetHelper.getInstance().initializeIC(System.getProperty("user.dir") + "/webapps/WebSmatch/" + Configs.WNIC);
		} catch (Exception e) {
			L.Error(e.getMessage(),e);
		}
	}

	@Override
	public ArrayList<SimpleSchemaElement> getSchemaElements(String schemaId) {

		ArrayList<SimpleSchemaElement> res = new ArrayList<SimpleSchemaElement>();

		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

		try {
			SchemaStoreObject sc = null;
			try {
				sc = serviceLoc.getSchemaStore(new URL(this.storeService));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				return null;
			}

			if (sc != null) {

				SchemaElementList list = sc.getSchemaElements(new Integer(schemaId));
				Entity[] elements = list.getEntities();
				Subtype[] sub = list.getSubtypes();
				Attribute[] attr = list.getAttributes();

				for (int i = 0; i < elements.length; i++) {
					res.add(new SimpleEntity(elements[i].getId(), elements[i].getName()));
				}

				for (int i = 0; i < sub.length; i++) {
					res.add(new SimpleSubtype(sub[i].getId(), sub[i].getParentID(), sub[i].getChildID()));
				}

				for (int i = 0; i < attr.length; i++) {
					// get name of entity
					String name = new String();
					for(Entity ent : elements){
						if(ent.getId().intValue() == attr[i].getEntityID().intValue()) name = ent.getName();
					}
					res.add(new SimpleAttribute(attr[i].getId(), attr[i].getName(), attr[i].getEntityID(), name));
				}

			}
		} catch (ServiceException e) {
			L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
			L.Error(e.getMessage(),e);
		}
		return res;
	}

	// we need to match only one element
	public MatcherScores localMatchElements(Integer sourceId, Integer targetId, Integer ele1, Integer ele2, Integer group, String tech) {

		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

		try {
			SchemaStoreObject sc = null;
			try {
				sc = serviceLoc.getSchemaStore(new URL(this.storeService));
				Schema sourceSchema = sc.getSchema(sourceId);
				Schema targetSchema = sc.getSchema(targetId);

				// get elements
				ArrayList<SchemaElement> sourceElements = new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(sourceId).geetSchemaElements()));
				ArrayList<SchemaElement> targetElements = new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(targetId).geetSchemaElements()));

				// get elements
				SchemaInfo sourceInfo = new SchemaInfo(sourceSchema, null, sourceElements);
				SchemaInfo targetInfo = new SchemaInfo(targetSchema, null, targetElements);

				HierarchicalSchemaInfo hsourceInfo = new HierarchicalSchemaInfo(sourceInfo);
				HierarchicalSchemaInfo htargetInfo = new HierarchicalSchemaInfo(targetInfo);

				// finally filtered
				FilteredSchemaInfo fsourceInfo = new FilteredSchemaInfo(hsourceInfo);
				FilteredSchemaInfo ftargetInfo = new FilteredSchemaInfo(htargetInfo);

				// ExactInriaMatcher matcher = new ExactInriaMatcher();
				YAMMatcherWrapper matcher = new YAMMatcherWrapper();

				matcher.initialize(fsourceInfo, ftargetInfo);
				if (tech == null)
					matcher.setChoosenTech("Stoilos_JW");
				else
					matcher.setChoosenTech(tech);
				// TODO fix this
				if (group != null)
					matcher.setUserGroup(group);
				else
					matcher.setUserGroup(0);

				// TODO fix this, now it inserts only elements
				Harmony.yamDB = false;

				// we get the scores
				MatcherScores scores = matcher.match();

				MatcherScores choosenScore = new MatcherScores(0.0);

				// ok print scores
				for (ElementPair pair : scores.getElementPairs()) {
					// System.out.println(scores.getScore(pair).getPositiveEvidence());
					if ((pair.getSourceElement().intValue() == ele1.intValue() && pair.getTargetElement().intValue() == ele2.intValue())
							|| (pair.getSourceElement().intValue() == ele2.intValue() && pair.getTargetElement().intValue() == ele1.intValue())) {
						choosenScore.setScore(pair.getSourceElement(), pair.getTargetElement(), scores.getScore(pair));
					}
				}

				return choosenScore;

			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				// return null;
			}

		} catch (ServiceException e) {
			L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
			L.Error(e.getMessage(),e);
		}
		return null;
	}

	// specific for local use
	public MatcherScores localMatchSchemas(Integer sourceId, Integer targetId, Integer group, String tech) {

		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

		try {
			SchemaStoreObject sc = null;
			try {
				sc = serviceLoc.getSchemaStore(new URL(this.storeService));
				Schema sourceSchema = sc.getSchema(sourceId);
				Schema targetSchema = sc.getSchema(targetId);

				// get elements
				SchemaInfo sourceInfo = new SchemaInfo(sourceSchema, null, new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(sourceId)
						.geetSchemaElements())));
				SchemaInfo targetInfo = new SchemaInfo(targetSchema, null, new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(targetId)
						.geetSchemaElements())));

				HierarchicalSchemaInfo hsourceInfo = new HierarchicalSchemaInfo(sourceInfo);
				HierarchicalSchemaInfo htargetInfo = new HierarchicalSchemaInfo(targetInfo);

				// finally filtered
				FilteredSchemaInfo fsourceInfo = new FilteredSchemaInfo(hsourceInfo);
				FilteredSchemaInfo ftargetInfo = new FilteredSchemaInfo(htargetInfo);

				// ExactInriaMatcher matcher = new ExactInriaMatcher();
				YAMMatcherWrapper matcher = new YAMMatcherWrapper();

				matcher.initialize(fsourceInfo, ftargetInfo);
				if (tech == null)
					matcher.setChoosenTech("Stoilos_JW");
				else
					matcher.setChoosenTech(tech);
				matcher.setUserGroup(group);

				// TODO fix this, now it inserts only elements
				Harmony.yamDB = true;

				// we get the scores
				MatcherScores scores = matcher.match();

				return scores;

				// ok print scores
		/*
		 * for(ElementPair pair : scores.getElementPairs()){
		 * System.out.println
		 * (scores.getScore(pair).getPositiveEvidence()); }
		 */

			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				// return null;
			}

		} catch (ServiceException e) {
			L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
			L.Error(e.getMessage(),e);
		}
		return null;

	}

	@Override
	public void matchSchemas(Integer sourceId, Integer targetId, Integer group, String tech) {

		Integer groupId = -1;
		if (group.intValue() == -1) {
			groupId = LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId());
		}

		this.localMatchSchemas(sourceId, targetId, groupId, tech);

	}

	@Override
	public ArrayList<SimpleSchemaElement> importSchema(String uri, String importer, String userName) {

		List<SchemaData> schemas = new ArrayList<SchemaData>();

		// first we need to see if there is a same existing schema
		SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();
		try {
			SchemaStoreObject sc = serviceLoc.getSchemaStore(new URL(this.storeService));

			Schema[] sch = sc.getSchemas();

			if (sch != null) {
				for (int i = 0; i < sch.length; i++) {
					if (sch[i].getAuthor().equals(userName)) {
						schemas.add(new SchemaData(sch[i].getName(), sch[i].getSource(), sch[i].getAuthor(), sch[i].getDescription(), sch[i].getId().toString()));
					}
				}
			}

		} catch (ServiceException e) {
			L.Error(e.getMessage(),e);
		} catch (RemoteException e) {
			L.Error(e.getMessage(),e);
		} catch (MalformedURLException e) {
			L.Error(e.getMessage(),e);
		}

		// before storing schema, we have to see if the couple (source/author)
		// already exist, and update it instead of creating a new schema
		int idToDel = -1;

		String uriToTest = new String();
		uriToTest = uri;

		// then search it
		ListIterator<SchemaData> it = schemas.listIterator();

		while (it.hasNext()) {

			SchemaData sd = it.next();
			if (uriToTest.equals(sd.getSource()))
				idToDel = new Integer(sd.getId()).intValue();
		}
		// end of testing
		// we return the good schema to the user
		if (idToDel != -1)
			return this.getSchemaElements(new Integer(idToDel).toString());

		// else we import
		Schema sch = new Schema();
		SchemaElementList elements = null;

		// first we parse the file
		if (importer.equals("ontology")) {

			IOWLImporter owlImporter = new IOWLImporter();
			ArrayList<SchemaElement> se = new ArrayList<SchemaElement>();
			try {
				se = owlImporter.getSchemaElements(new URI("file:" + this.baseXLSDir + "/" + uri.replaceAll("\\s", "%20")));
				sch.setType(owlImporter.getName());
			} catch (ImporterException e) {
				L.Error(e.getMessage(),e);
			} catch (URISyntaxException e) {
				L.Error(e.getMessage(),e);
			}

			SchemaElement[] sea = new SchemaElement[se.size()];

			for (int i = 0; i < se.size(); i++) {
				sea[i] = se.get(i);
			}

			elements = new SchemaElementList(sea);
		} else if (importer.equals("osmoze")) {
			OsmozeImporter osImporter = new OsmozeImporter();
			ArrayList<SchemaElement> se = new ArrayList<SchemaElement>();
			try {
				se = osImporter.getSchemaElements(new URI("file:" + this.baseXLSDir + "/" + uri.replaceAll("\\s", "%20")));
				sch.setType(osImporter.getName());
			} catch (ImporterException e) {
				L.Error(e.getMessage(),e);
			} catch (URISyntaxException e) {
				L.Error(e.getMessage(),e);
			}

			SchemaElement[] sea = new SchemaElement[se.size()];

			for (int i = 0; i < se.size(); i++) {
				sea[i] = se.get(i);
			}

			elements = new SchemaElementList(sea);
		}

		sch.setName(uri.substring(0, uri.lastIndexOf('.')));
		sch.setAuthor(userName);
		sch.setSource(uri);
		sch.setDescription("");
		// set a local id
		sch.setId(SchemaImporter.nextId());

		try {
			final SchemaStoreObject sc = serviceLoc.getSchemaStore(new URL(this.storeService));

			if (sc != null) {

				try {
					// ok get the id and match with all schemas from this group,
					// threaded
					final Integer schemaId = sc.importSchema(sch, elements);
					final Integer groupId = LoginServiceImpl.groupIds.get(this.getThreadLocalRequest().getSession().getId());
					final String tmpSid = this.getThreadLocalRequest().getSession().getId();
					final String schAuthor = userName;

					// ok get the list of matching technics
					MatchingResultsServiceImpl serviceImpl = new MatchingResultsServiceImpl();
					final List<SimpleMatchTech> techs = serviceImpl.getMatchingTechs();

					Thread t = new Thread("MatchingThread") {
						public void run() {

							//
							Domain dom = DomainFactory.getDomain(tmpSid);
							MatchingProgressEvent event = new MatchingProgressEvent();
							//

							try {
								Schema[] schstmp = sc.getSchemas();

								// ok we filter by user before matching
								ArrayList<Schema> schslist = new ArrayList<Schema>();
								for (Schema s : schstmp) {
									if (s.getAuthor().trim().equals(schAuthor.trim()))
										schslist.add(s);
								}

								Schema[] schs = schslist.toArray(new Schema[] {});
								//

								event.setMaxMatch((schs.length - 1) * (techs.size() + 1));
								int cpt = 0;

								ProbaMatching proba = new ProbaMatching();
								MatchingResultsServiceImpl matchService = new MatchingResultsServiceImpl();

								if (schs != null && schs.length > 1)
									for (Schema s : schs) {
										if (!s.getId().equals(schemaId)) {

											// we now have to store only proba
											// matching high enough results
											HashMap<String, MatcherScores> scoresByTech = new HashMap<String, MatcherScores>();

											// for each tech
											for (SimpleMatchTech tech : techs) {
												// System.out.println("Matching : "+schemaId
												// + " and "+s.getId());
												cpt++;
												event.setMatchCount(cpt);
												event.setMsg("Matching " + (int) (((double) cpt / (double) event.getMaxMatch()) * 100.0)
														+ "% complete. Currently matching : " + s.getName() + " using " + tech.getName() + ".");
												addEvent(dom, event);
												// matchSchemas(schemaId,
												// s.getId(), groupId,
												// tech.getName());
												scoresByTech.put(tech.getName(), localMatchSchemas(schemaId, s.getId(), groupId, tech.getName()));
												try {
													sleep(50);
												} catch (InterruptedException e) {
													L.Error(e.getMessage(),e);
												}
											}

											// ok make the same thing for probas
											cpt++;
											event.setMatchCount(cpt);
											event.setMsg("Matching " + (int) (((double) cpt / (double) event.getMaxMatch()) * 100.0)
													+ "% complete. Currently matching : " + s.getName() + " using probabilistic combination.");
											addEvent(dom, event);

											// proba.matchSchemas(schemaId,
											// s.getId());
											proba.matchSchemas(schemaId, s.getId(), scoresByTech, MatchingResultsServiceImpl.avgProba);

											// we insert probas
											matchService.insertProbaDistance(s.getId(), schemaId);

										}
									}

								proba.close();

								// ok update cluster now
								event.setMsg("Processing clustering calculation.");
								addEvent(dom, event);
				/*
				 * ClusteringServiceImpl service = new
				 * ClusteringServiceImpl();
				 * service.updateClusters(schemaId);
				 */
								event.setMatchCount(0);
								event.setMaxMatch(0);
								event.setMsg("Processing terminated, clustering up to date");
								addEvent(dom, event);

							} catch (RemoteException e) {
								L.Error(e.getMessage(),e);
							}
						}
					};
					// bad
					if (!userName.contains("rigaowl"))
						t.start();

					else if(userName.contains("rigaowl")){

						t = new Thread("MatchingThread") {
							@SuppressWarnings("deprecation")
							public void run() {
								try {
									Schema[] schstmp = sc.getSchemas();

									// ok we filter by user before matching
									ArrayList<Schema> schslist = new ArrayList<Schema>();
									for (Schema s : schstmp) {
										if (s.getAuthor().trim().equals(schAuthor.trim()))
											schslist.add(s);
									}

									Schema[] schs = schslist.toArray(new Schema[] {});

									ProbaMatching proba = new ProbaMatching();
									MatchingResultsServiceImpl matchService = new MatchingResultsServiceImpl();

									if (schs != null && schs.length > 1)
										for (Schema s : schs) {
											if (!s.getId().equals(schemaId)) {

												// we now have to store only
												// proba matching high enough
												// results
												HashMap<String, MatcherScores> scoresByTech = new HashMap<String, MatcherScores>();

												scoresByTech.put("SoftTFIDF", localMatchSchemas(schemaId, s.getId(), groupId, "SoftTFIDF"));

												try {
													sleep(50);
												} catch (InterruptedException e) {
													L.Error(e.getMessage(),e);
												}

												// ok make the same thing for
												// probas
												proba.matchSchemas(schemaId, s.getId(), scoresByTech, MatchingResultsServiceImpl.avgProba);

												// we insert probas
												matchService.insertProbaDistance(s.getId(), schemaId);
											}
										}

									proba.close();

									ClusteringServiceImpl clusteringService = new ClusteringServiceImpl();
									clusteringService.updateClusters(schemaId);

								} catch (RemoteException e) {
									L.Error(e.getMessage(),e);
								}
							}
						};
						t.start();
					}

					// end of matching thread
					return this.getSchemaElements(schemaId.toString());
				} catch (RemoteException e) {
					L.Error(e.getMessage(),e);
				}
			}

		} catch (MalformedURLException | ServiceException e) {
			L.Error(e.getMessage(),e);
		}

		return null;

	}

	@Override
	public ArrayList<SimpleGraphComponent> getSchemaTree(String schemaId, boolean rtl) {

		double angle = Math.PI * -90.0 / 180;

		if (rtl)
			angle = Math.PI * +90.0 / 180;

		int edgesCount = 1;

		ArrayList<SimpleSchemaElement> elements = this.getSchemaElements(schemaId);
		Forest<Integer, Integer> g = new DelegateForest<>();
		HashMap<Integer, SimpleSchemaElement> ents = new HashMap<>();

		for (SimpleSchemaElement element : elements) {
			if (element instanceof SimpleEntity) {
				if (!ents.containsKey(element.getId())) {
					g.addVertex(element.getId());
					ents.put(element.getId(), element);
				}
			}
		}

		// only one time child
		ArrayList<Integer> childs = new ArrayList<>();

		for (SimpleSchemaElement element : elements) {
			if (element instanceof SimpleAttribute) {
				ents.put(element.getId(), element);
				if (!childs.contains(element.getId())) {
					g.addEdge(edgesCount, ((SimpleAttribute) element).getEntityId(), element.getId());
					childs.add(element.getId());
					edgesCount++;
				}
			}
		}

		// ok now edges between elements subtypes
		for (SimpleSchemaElement element : elements) {
			if (element instanceof SimpleSubtype) {
				Integer parent = ((SimpleSubtype) element).getParentId();
				Integer child = ((SimpleSubtype) element).getChildId();
				if (!childs.contains(child)) {
					g.addEdge(new Integer(edgesCount), parent, child);
					childs.add(child);
					edgesCount++;
				}
			}
		}

		TreeLayout<Integer, Integer> layout = new TreeLayout<Integer, Integer>(g, 50, 50);
		int vertCount = g.getVertexCount();

		ArrayList<SimpleGraphComponent> components = new ArrayList<SimpleGraphComponent>();

		// vertices
		Integer[] vertices = g.getVertices().toArray(new Integer[] {});

		for (int i = 0; i < vertCount; i++) {

			SimpleVertex vertex = new SimpleVertex();

			vertex.setId(vertices[i]);
			vertex.setName(ents.get(vertices[i]).getName());

			// which schema for this vertex
			vertex.setSchemaId(schemaId);

			Point vp = this.rotation(new Point((int) layout.transform(vertices[i]).getX(), (int) layout.transform(vertices[i]).getY()), angle);

			vertex.setX(vp.getX());
			vertex.setY(vp.getY());

			components.add(vertex);

		}

		// edges
		int edgCount = g.getEdgeCount();
		Integer[] edges = g.getEdges().toArray(new Integer[] {});

		for (int i = 0; i < edgCount; i++) {
			Integer edgeName = edges[i];

			Pair<Integer> endPoints = g.getEndpoints(edges[i]);

			SimpleEdge edge = new SimpleEdge();

			edge.setParentId(endPoints.getFirst());
			edge.setChildId(endPoints.getSecond());
			edge.setComment(edgeName.toString());

			Point pp = this
					.rotation(new Point((int) layout.transform(endPoints.getFirst()).getX(), (int) layout.transform(endPoints.getFirst()).getY()), angle);

			edge.setParentX(pp.getX());
			edge.setParentY(pp.getY());

			Point cp = this.rotation(new Point((int) layout.transform(endPoints.getSecond()).getX(), (int) layout.transform(endPoints.getSecond()).getY()),
					angle);

			edge.setChildX(cp.getX());
			edge.setChildY(cp.getY());

			components.add(edge);
		}

		return components;

	}

	private Point rotation(Point p, double theta) {
		double x = p.getX() * Math.cos(theta) - p.getY() * Math.sin(theta);
		double y = p.getX() * Math.sin(theta) + p.getY() * Math.cos(theta);
		return new Point((int) x, (int) y);
	}

}
