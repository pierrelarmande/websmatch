package org.inria.websmatch.tests;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.inria.websmatch.db.MySQLDBConnector;
import org.inria.websmatch.evaluate.ProbaMatching;
import org.inria.websmatch.utils.L;
import org.mitre.harmony.Harmony;
import org.mitre.harmony.matchers.matchers.YAMMatcherWrapper;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.model.SchemaElement;
import org.mitre.schemastore.model.schemaInfo.FilteredSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.HierarchicalSchemaInfo;
import org.mitre.schemastore.model.schemaInfo.SchemaInfo;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

import yam.system.Configs;
import yam.tools.WordNetHelper;

@Deprecated
public class OldMatchTest {

    
      private static String[] techs = { "Stoilos_JW", "Levenshtein",
      "SmithWaterman", "SmithWatermanGotoh",
      "SmithWatermanGotohWindowedAffine", "Jaro", "JaroWinkler",
      "QGramsDistance", "MongeElkan", "WuPalmer", "Lin", "MultiLevelMatcher",
     "SoftTFIDF", "SoftTFIDFWordNet" };
     
      // if rigaowl
    //private static String[] techs = { "SoftTFIDF" };

    private static SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

    private static URL storeUrl;

    /**
     * @param args
     */
    public static void main(String[] args) {

	MySQLDBConnector connector = new MySQLDBConnector();
	connector.connect();

	try {
	    storeUrl = new URL("http://constraint.lirmm.fr/SchemaStore/services/SchemaStore");
	} catch (MalformedURLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	// init WordNet
	yam.system.Configs.WNTMP = "WNTemplate.xml";
	yam.system.Configs.WNPROP = "file_properties.xml";

	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(), e);
	}

	// test 2 schemas
	// matchSchemas(60744, 79258);
	// System.exit(0);

	// int sourceId = 9864;
	// int[] targetsId = {
	// 14823,15891,17289,17430,18128,18384,19082,19553,19809,20256,20331,22244,23642,25246,25294,26529,26652,26719,26935,27307,28139,28535,28676,29072,29120,29168,29566,29936,30634,30682,30823,31225,31366,31738,31813,31954,32002,32702,32769,33091,33166,33536,33611,34007,34082,34782,35102,35474,35857,36259,36655,36703,37025,37154,37363,37761,38157,38555,38951,39347,39719,39767,39815,39882,40202,40902,41084,41456,41776,42476,42551,42626,42996,43137,43835,44231,44306,44373,44743,45139,45206,45608,46006,46378,46774,46849,47247,47567,47634,47956,48276,48598,48994,49694,49769,50165,50232,50554,50924
	// };

	// int[] oaei = {
	// 9864,14823,15891,17289,17430,18128,18384,19082,19553,19809,20256,20331,22244,23642,25246,25294,26529,26652,26719,26935,27307,28139,28535,28676,29072,29120,29168,29566,29936,30634,30682,30823,31225,31366,31738,31813,31954,32002,32702,32769,33091,33166,33536,33611,34007,34082,34782,35102,35474,35857,36259,36655,36703,37025,37154,37363,37761,38157,38555,38951,39347,39719,39767,39815,39882,40202,40902,41084,41456,41776,42476,42551,42626,42996,43137,43835,44231,44306,44373,44743,45139,45206,45608,46006,46378,46774,46849,47247,47567,47634,47956,48276,48598,48994,49694,49769,50165,50232,50554,50924
	// };
	/*
	 * List<Integer> oaeiList = Arrays.asList(new Integer[] { 9864, 11704,
	 * 14823, 15891, 17289, 17430, 18128, 18384, 19082, 19553, 19809, 20256,
	 * 20331, 22244, 23642, 25246, 25294, 26529, 26652, 26719, 26935, 27307,
	 * 28139, 28535, 28676, 29072, 29120, 29168, 29566, 29936, 30634, 30682,
	 * 30823, 31225, 31366, 31738, 31813, 31954, 32002, 32702, 32769, 33091,
	 * 33166, 33536, 33611, 34007, 34082, 34782, 35102, 35474, 35857, 36259,
	 * 36655, 36703, 37025, 37154, 37363, 37761, 38157, 38555, 38951, 39347,
	 * 39719, 39767, 39815, 39882, 40202, 40902, 41084, 41456, 41776, 42476,
	 * 42551, 42626, 42996, 43137, 43835, 44231, 44306, 44373, 44743, 45139,
	 * 45206, 45608, 46006, 46378, 46774, 46849, 47247, 47567, 47634, 47956,
	 * 48276, 48598, 48994, 49694, 49769, 50165, 50232, 50554, 50924 });
	 */
	
	List<Integer> rigaList = Arrays.asList(new Integer[] {103959,105037,105435,106197,106415,106921});

	Schema[] schemas = null;

	// we get all schemas and matchs all

	try {
	    SchemaStoreObject sc = null;

	    sc = serviceLoc.getSchemaStore(storeUrl);

	    schemas = sc.getSchemas();

	} catch (ServiceException e) {
	    L.Error(e.getMessage(),e);
	} catch (RemoteException e) {
	    L.Error(e.getMessage(),e);
	}

	/*
	 * if (sourceId == -1 || targetsId == null) return;
	 */

	if (schemas == null || schemas.length == 0)
	    return;

	// for (int target = 0; target < targetsId.length; target++){
	for (int source = 0; source < (schemas.length - 1); source++) {
	    for (int target = source + 1; target < schemas.length; target++) {
		 if (schemas[source].getAuthor().trim().equals("riga") && schemas[target].getAuthor().trim().equals("riga")) {
		     if(rigaList.contains(schemas[source].getId()) || rigaList.contains(schemas[target].getId())){
		// if (schemas[source].getAuthor().trim().equals("rigaowl") && schemas[target].getAuthor().trim().equals("rigaowl")) {
		    // if (schemas[source].getId() >= 7407 &&
		    // (!oaeiList.contains(schemas[source].getId()) &&
		    // !oaeiList.contains(schemas[target].getId()))) {
		    for (int tech = 0; tech < techs.length; tech++) {

			try {
			    SchemaStoreObject sc = null;
			    sc = serviceLoc.getSchemaStore(storeUrl);
			    // Schema sourceSchema = sc.getSchema(sourceId);
			    // Schema targetSchema =
			    // sc.getSchema(targetsId[target]);

			    Schema sourceSchema = sc.getSchema(schemas[source].getId());
			    Schema targetSchema = sc.getSchema(schemas[target].getId());

			    if (sourceSchema != null && targetSchema != null) {

				// get elements
				SchemaInfo sourceInfo = new SchemaInfo(sourceSchema, null, new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(
					sourceSchema.getId()).geetSchemaElements())));
				SchemaInfo targetInfo = new SchemaInfo(targetSchema, null, new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(
					targetSchema.getId()).geetSchemaElements())));

				HierarchicalSchemaInfo hsourceInfo = new HierarchicalSchemaInfo(sourceInfo);
				HierarchicalSchemaInfo htargetInfo = new HierarchicalSchemaInfo(targetInfo);

				// finally filtered
				FilteredSchemaInfo fsourceInfo = new FilteredSchemaInfo(hsourceInfo);
				FilteredSchemaInfo ftargetInfo = new FilteredSchemaInfo(htargetInfo);

				// ExactInriaMatcher matcher = new
				// ExactInriaMatcher();
				YAMMatcherWrapper matcher = new YAMMatcherWrapper();

				matcher.initialize(fsourceInfo, ftargetInfo);
				matcher.setChoosenTech(techs[tech]);
				
				matcher.setUserGroup(1);
				
				// rigaowl
				//matcher.setUserGroup(10);

				Harmony.yamDB = true;

				/*MatcherScores scores =*/ matcher.match();

				// ok print scores

				/*for (ElementPair pair : scores.getElementPairs()) {
				    if (scores.getScore(pair).getPositiveEvidence() > 0.6) System.out.println(scores.getScore(pair).getPositiveEvidence());
				}*/

			    }

			} catch (ServiceException e) {
			    L.Error(e.getMessage(),e);
			} catch (RemoteException e) {
			    L.Error(e.getMessage(),e);
			}
		    }

		    // match proba
		    ProbaMatching proba = new ProbaMatching();
		    proba.matchSchemas(schemas[source].getId(), schemas[target].getId());
		    proba.close();
		    
		    System.out.println("End of matching : "+schemas[source].getId()+ " "+schemas[target].getId());
		    
		}
		 }
	    }
	}
    }

    @SuppressWarnings("unused")
    private static void matchSchemas(int source, int target) {

	for (int tech = 0; tech < techs.length; tech++) {
	    System.out.println(techs[tech]);
	    try {
		SchemaStoreObject sc = null;
		sc = serviceLoc.getSchemaStore(storeUrl);
		// Schema sourceSchema = sc.getSchema(sourceId);
		// Schema targetSchema =
		// sc.getSchema(targetsId[target]);

		Schema sourceSchema = sc.getSchema(source);
		Schema targetSchema = sc.getSchema(target);

		if (sourceSchema != null && targetSchema != null) {

		    // get elements
		    SchemaInfo sourceInfo = new SchemaInfo(sourceSchema, null, new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(
			    sourceSchema.getId()).geetSchemaElements())));
		    SchemaInfo targetInfo = new SchemaInfo(targetSchema, null, new ArrayList<SchemaElement>(Arrays.asList(sc.getSchemaElements(
			    targetSchema.getId()).geetSchemaElements())));

		    HierarchicalSchemaInfo hsourceInfo = new HierarchicalSchemaInfo(sourceInfo);
		    HierarchicalSchemaInfo htargetInfo = new HierarchicalSchemaInfo(targetInfo);

		    // finally filtered
		    FilteredSchemaInfo fsourceInfo = new FilteredSchemaInfo(hsourceInfo);
		    FilteredSchemaInfo ftargetInfo = new FilteredSchemaInfo(htargetInfo);

		    // ExactInriaMatcher matcher = new
		    // ExactInriaMatcher();
		    YAMMatcherWrapper matcher = new YAMMatcherWrapper();

		    matcher.initialize(fsourceInfo, ftargetInfo);
		    matcher.setChoosenTech(techs[tech]);
		    matcher.setUserGroup(0);

			Harmony.yamDB = true;

		    /* MatcherScores scores = */
		    matcher.match();

		    // ok print scores
		    /*
		     * for(ElementPair pair : scores.getElementPairs()){
		     * System.out.println (scores.getScore(pair).
		     * getPositiveEvidence()); }
		     */

		}

	    } catch (ServiceException e) {
		L.Error(e.getMessage(),e);
	    } catch (RemoteException e) {
		L.Error(e.getMessage(),e);
	    }
	}

	// match proba
	ProbaMatching proba = new ProbaMatching();
	proba.matchSchemas(source, target);
	proba.close();

    }
}
