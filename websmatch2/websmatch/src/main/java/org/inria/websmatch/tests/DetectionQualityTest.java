package org.inria.websmatch.tests;

import java.util.List;

import org.inria.websmatch.db.MongoDBConnector;
import org.inria.websmatch.gwt.spreadsheet.client.models.ConnexComposant;
import org.inria.websmatch.gwt.spreadsheet.client.models.DetectionQualityData;

import weka.core.*;

public class DetectionQualityTest {

    private static int NB_CRITERIA = 11;
    
    /*
     * See http://seraja.ics.uci.edu/svn/emme/yelp-crawler/trunk/src/test/java/weka/KMeansTest.java
     */
    public static void main(String[] args){
        
        MongoDBConnector conn = MongoDBConnector.getInstance();
        List<DetectionQualityData> data = conn.getDetectionQualityList(true,"datapublica"); //true -> valid�
        int size = data.size();
        System.out.println("Size: "+size);
        
        FastVector fvAttributes = new FastVector(NB_CRITERIA);
        for(int i=0; i<NB_CRITERIA; i++) {
            Attribute attr = new Attribute("a_"+i);
            fvAttributes.addElement(attr);
        }
        Attribute classe = new Attribute("classe");
        fvAttributes.addElement(classe);
        Instances dataset = new Instances("data", fvAttributes, size);
        dataset.setClass(classe);
        
        for(DetectionQualityData d : data) {
            System.out.println(d.getSource());
            for(ConnexComposant cc : d.getConnexComposants()) {
                System.out.println(cc.getCriteria());
                System.out.println(cc.getFmeas());
                //String[] criteria = cc.getCriteria().split(";");
                /*for(String st : criteria) {
                    System.out.println(st);
                }
                assert criteria.length == NB_CRITERIA; //FIXME
                System.out.println(criteria.length);

                Instance instance = new Instance(NB_CRITERIA);
                for(int i=0; i<NB_CRITERIA; i++) {
                    double value = Double.parseDouble(criteria[i].substring(criteria[i].indexOf("=")));
                    instance.setValue(i, value);
                }
                instance.setValue(classe, cc.getFmeas());
                dataset.add(instance);
                */
            }
            //System.out.println(d.getName()+ "\tFmea : "+d.getFmeasure());
        }
    }
    
    public float getPredictedFmeas(DetectionQualityData data) {
        return 0;
    }
    

}
