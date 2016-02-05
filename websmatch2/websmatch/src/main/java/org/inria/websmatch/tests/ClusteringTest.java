package org.inria.websmatch.tests;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.inria.websmatch.gwt.spreadsheet.server.MatchingResultsServiceImpl;
import org.inria.websmatch.utils.L;
import org.mitre.schemastore.model.Schema;
import org.mitre.schemastore.servlet.SchemaStoreObject;
import org.mitre.schemastore.servlet.SchemaStoreServiceLocator;

import yam.system.Configs;
import yam.tools.WordNetHelper;

public class ClusteringTest {

    /**
     * @param args
     */
    public static void main(String[] args) {

	// init WordNet
	yam.system.Configs.WNTMP = "WNTemplate.xml";
	yam.system.Configs.WNPROP = "file_properties.xml";

	try {
	    WordNetHelper.getInstance().initializeWN(Configs.WNDIR, Configs.WNVER);
	    WordNetHelper.getInstance().initializeIC(Configs.WNIC);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    L.Error(e.getMessage(),e);
	}

	// int sourceId = 9864;
	// int[] targetsId = {
	// 14823,15891,17289,17430,18128,18384,19082,19553,19809,20256,20331,22244,23642,25246,25294,26529,26652,26719,26935,27307,28139,28535,28676,29072,29120,29168,29566,29936,30634,30682,30823,31225,31366,31738,31813,31954,32002,32702,32769,33091,33166,33536,33611,34007,34082,34782,35102,35474,35857,36259,36655,36703,37025,37154,37363,37761,38157,38555,38951,39347,39719,39767,39815,39882,40202,40902,41084,41456,41776,42476,42551,42626,42996,43137,43835,44231,44306,44373,44743,45139,45206,45608,46006,46378,46774,46849,47247,47567,47634,47956,48276,48598,48994,49694,49769,50165,50232,50554,50924
	// };

	// int[] oaei = {
	// 9864,14823,15891,17289,17430,18128,18384,19082,19553,19809,20256,20331,22244,23642,25246,25294,26529,26652,26719,26935,27307,28139,28535,28676,29072,29120,29168,29566,29936,30634,30682,30823,31225,31366,31738,31813,31954,32002,32702,32769,33091,33166,33536,33611,34007,34082,34782,35102,35474,35857,36259,36655,36703,37025,37154,37363,37761,38157,38555,38951,39347,39719,39767,39815,39882,40202,40902,41084,41456,41776,42476,42551,42626,42996,43137,43835,44231,44306,44373,44743,45139,45206,45608,46006,46378,46774,46849,47247,47567,47634,47956,48276,48598,48994,49694,49769,50165,50232,50554,50924
	// };
	/*List<Integer> oaeiList = Arrays.asList(new Integer[] { 9864, 11704, 14823, 15891, 17289, 17430, 18128, 18384, 19082, 19553, 19809, 20256, 20331, 22244,
		23642, 25246, 25294, 26529, 26652, 26719, 26935, 27307, 28139, 28535, 28676, 29072, 29120, 29168, 29566, 29936, 30634, 30682, 30823, 31225,
		31366, 31738, 31813, 31954, 32002, 32702, 32769, 33091, 33166, 33536, 33611, 34007, 34082, 34782, 35102, 35474, 35857, 36259, 36655, 36703,
		37025, 37154, 37363, 37761, 38157, 38555, 38951, 39347, 39719, 39767, 39815, 39882, 40202, 40902, 41084, 41456, 41776, 42476, 42551, 42626,
		42996, 43137, 43835, 44231, 44306, 44373, 44743, 45139, 45206, 45608, 46006, 46378, 46774, 46849, 47247, 47567, 47634, 47956, 48276, 48598,
		48994, 49694, 49769, 50165, 50232, 50554, 50924 });*/

	Schema[] schemas = null;

	// we get all schemas and matchs all
	SchemaStoreServiceLocator serviceLoc = new SchemaStoreServiceLocator();

	try {
	    SchemaStoreObject sc = null;
	    try {
		sc = serviceLoc.getSchemaStore(new URL("http://constraint.lirmm.fr/SchemaStore/services/SchemaStore"));

		schemas = sc.getSchemas();

	    } catch (MalformedURLException e1) {
		e1.printStackTrace();
		return;
	    }

	} catch (ServiceException e) {
	    L.Error(e.getMessage(),e);
	} catch (RemoteException e) {
	    L.Error(e.getMessage(),e);
	}

	/*
	 * if (sourceId == -1 || targetsId == null) return;
	 */

	MatchingResultsServiceImpl service = new MatchingResultsServiceImpl();	
	
	if (schemas == null || schemas.length == 0)
	    return;

	// for (int target = 0; target < targetsId.length; target++){
	for (int source = 0; source < (schemas.length - 1); source++) {
	    for (int target = source + 1; target < schemas.length; target++) {
		// if (!schemas[source].getAuthor().equals("oaei") && schemas[source].getAuthor().equals(schemas[target].getAuthor())) {
		if (schemas[source].getAuthor().equals("rigaowl") && schemas[source].getAuthor().equals(schemas[target].getAuthor())) {

			service.insertProbaDistance(schemas[source].getId(), schemas[target].getId());
		    
		    }

		}   
	}
    }
}
