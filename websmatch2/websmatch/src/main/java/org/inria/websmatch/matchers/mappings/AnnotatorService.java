package org.inria.websmatch.matchers.mappings;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.inria.websmatch.utils.L;

import javax.json.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements interface to an external ontology annotator web-service
 */
public enum AnnotatorService {
    IBC("http://tubo.lirmm.fr:8080/annotators/ibc_annotator",
        //"http://bioportal.lirmm.fr:8082/ontologies",
        "http://data.agroportal.lirmm.fr/ontologies",
        //"aa5b0e2c-5a2e-4a01-b3a2-32cf024f3f27");
        "1de0a270-29c5-4dda-b043-7c3580628cd5");
    // TODO: *** add other annotators here ***

    private String annotatorURI;
    private String ontologyURI;
    private String apiKey;

    private AnnotatorService(String annotatorURI, String ontologyURI, String apiKey){
        this.annotatorURI = annotatorURI;
        this.ontologyURI  = ontologyURI;
        this.apiKey = apiKey;
    }

    /**
     * @param text: the text to annotate
     * @param ontoId: Id of ontology to be used
     * @param maxLevel: max hierarchy level to use
     * @param scoreMethod: score method to use for scoring - if null does not score
     *
     * @return JsonArray of annotation
     *
     * @throws UnsupportedEncodingException if {@code text} cannot be encoded in the request url
     */
    public JsonArray annotate(String text, String ontoId, int maxLevel, String scoreMethod) throws UnsupportedEncodingException {
        JsonArray result = null;
        HttpClient client = new DefaultHttpClient();

        //annotatorURI = "http://bioportal.lirmm.fr:8082/annotator";
        String requestURL = annotatorURI+"?apikey="+apiKey+
                "&text=" + URLEncoder.encode(text, "UTF-8")+
                "&ontologies="+ontoId+
                "&minimum_match_length=5&longest_only=true&max_level=" + maxLevel;
        if(scoreMethod!=null)
            requestURL += "&score="+scoreMethod;
///
        //L.Debug(AnnotatorService, "annotator request:"+requestURL);
///
        // Execute the POST method
        HttpResponse response = null;
        try {
            response = client.execute(new HttpGet(requestURL));
        } catch (IOException e) {
            L.Error(e.getMessage(),e);
        }

        try {
            InputStreamReader is = new InputStreamReader(response.getEntity().getContent());
            JsonReader rdr = Json.createReader(is);
            result = rdr.readArray();
        } catch (IllegalStateException | IOException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    /**
     * @return the list of ontologies that can be requested from
     */
    public List<String[]> getOntologiesWithIds() {
        List<String[]> ontologies = new ArrayList<>();

        // query annotator
        JsonArray results = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet method    = new HttpGet(ontologyURI + "?apikey=" + apiKey);

        // Execute the POST method
        HttpResponse response = null;
        try {
            response = client.execute(method);
        } catch (IOException e) {
            L.Error(e.getMessage(), e);
        }

        try {
            InputStreamReader is = new InputStreamReader(response.getEntity().getContent());

	    /*
	     * BufferedReader in = new BufferedReader(is); String inputLine;
	     * while ((inputLine = in.readLine()) != null)
	     * System.out.println(inputLine); in.close();
	     */

            JsonReader rdr = Json.createReader(is);
            results = rdr.readArray();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            L.Error(e.getMessage(), e);
        }
        try {
            response.getEntity().consumeContent();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        //

        for (JsonObject result : results.getValuesAs(JsonObject.class)) {

            // filter, get only IBC group ontologies
            // L.Debug(MatchingResultsServiceImpl.class.getSimpleName(),
            // "Group uri : "+result.getJsonObject("links").getString("groups"),
            // true);
            JsonStructure groupResult = null;
            method = new HttpGet(result.getJsonObject("links").getString("groups") + "?apikey=" + apiKey);
            // Execute the POST method
            response = null;
            try {
                response = client.execute(method);
            } catch (IOException e) {
                L.Error(e.getMessage(), e);
            }

            try {
                InputStreamReader is = new InputStreamReader(response.getEntity().getContent());

                JsonReader rdr = Json.createReader(is);
                groupResult = rdr.read();
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                L.Error(e.getMessage(), e);
            }
            try {
                response.getEntity().consumeContent();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (groupResult instanceof JsonObject) {
                if (((JsonObject) groupResult).getString("acronym").equals("IBC"))
                    ontologies.add(new String[] { result.getString("name"), result.getString("@id").substring(result.getString("@id").lastIndexOf('/') + 1) });
            }
            else if(groupResult instanceof JsonArray){
                JsonArray arr = (JsonArray) groupResult;
                for(int i = 0; i < arr.size();i++){
                    //if(arr.getJsonObject(i).getString("acronym").equals("IBC")) 
                        ontologies.add(new String[] { result.getString("name"), result.getString("@id").substring(result.getString("@id").lastIndexOf('/') + 1) });
                }
            }
        }

        return ontologies;
    }

}
