package org.inria.websmatch.machineLearning;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesianLogisticRegression;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ConjunctiveRule;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.trees.ADTree;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

import com.thoughtworks.xstream.XStream;

public class BuildClassifiers {

    public static final String tmpFile = "/tmp/classifier.tmp"; //FIXME
    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        String url = "jdbc:mysql://localhost/machine_learning";
        Connection conn = DriverManager.getConnection(url, "ml_user", "ml_pass");
        Statement stt = conn.createStatement();

        ResultSet rs = stt.executeQuery("SELECT * FROM doc_list WHERE user = 'ml_user'");// id_doc NOT IN ('5','7','8','58','67','77','91','99','100','130','136')");// where user = 'ml_user'");
        while (rs.next()) {
            // String name = rs.getString("name");
            int id = rs.getInt("id_doc");
            ids.add(id);
        }
        stt.close();

        InstanceQuery query = new InstanceQuery();
        query.setUsername("ml_user");
        query.setPassword("ml_pass");
        query.setQuery("SELECT  first_cc_col,first_cc_row,type,behind_cell, right_cell, above_cell, left_cell, is_attribute FROM cells");// WHERE id_doc < 6");
        Instances train = query.retrieveInstances();
        train.setClass(train.attribute("is_attribute"));
        
        Map<Integer,Instances> tests = new HashMap<>();
        for(int id : ids) {
            query = new InstanceQuery();
            query.setUsername("ml_user");
            query.setPassword("ml_pass");
            query.setQuery("SELECT first_cc_col,first_cc_row,type,behind_cell, right_cell, above_cell, left_cell, is_attribute  FROM cells WHERE id_doc ="+id);
            Instances test = query.retrieveInstances();
            test.setClass(test.attribute("is_attribute"));
            tests.put(id, test);
        }
        
        //Use a cost matrix ? -> not sure !
        CostMatrix matrix = new CostMatrix(2);
        matrix.initialize();
        matrix.setElement(0, 1, 21655.0/167.0); //167 positives 21655 negatives 
        //matrix.applyCostMatrix(train, new Random(1));
        
        //classifiers to be tested 
        Classifier cls[] = {new ADTree(),new BayesianLogisticRegression(),new ConjunctiveRule(),
                            new DecisionTable(),new IBk(),
                            new J48()};/*, new JRip(),   
                            new Logistic(),  new MultilayerPerceptron(),
                            new NaiveBayes(),new SMO()}; */
                            //new AODE(),new GaussianProcesses(), new Id3(),new LibLINEAR(),
                            //new LibSVM(),new LinearRegression(),new M5Rules(),new MISVM(),new PLSClassifier()
        
        //Classifier cls[] = {new J48()};
        
        //Delete previous results from database
        stt = conn.createStatement();
        stt.execute("DELETE FROM classifier_results");
        stt.close();
        stt = conn.createStatement();
        stt.execute("DELETE FROM classifiers");
        stt.close();
        
        XStream xStream = new XStream();
        for(Classifier cl : cls) {
            cl.buildClassifier(train);
            
            //Serialize classifier
            File file = new File(tmpFile);
            file.createNewFile();        
            FileOutputStream out = new FileOutputStream(tmpFile);
            xStream.toXML(cl,out);
            out.close();
            //Store it in database
            Reader in = new FileReader(file);
            PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO classifiers VALUES (?,?)");
            ps.setString(1, cl.getClass().getSimpleName());
            ps.setCharacterStream(2, in, (int) file.length());
            ps.executeUpdate();
            in.close();
            ps.close();
            
            //evaluate each classifier on each spreadsheet
            for(int id : ids) {
                Evaluation eval = new Evaluation(train);
                Instances test = tests.get(id);
                eval.evaluateModel(cl, test);
                int true_negative = (int) eval.numTrueNegatives(1);
                int true_positive = (int) eval.numTruePositives(1);
                int false_negative = (int) eval.numFalseNegatives(1);
                int false_positive = (int) eval.numFalsePositives(1);
                stt = conn.createStatement();
                //store classification quality in the database
                String sql ="INSERT INTO classifier_results VALUES ('"
                    +cl.getClass().getSimpleName()+"',"+id+","+true_negative+","+true_positive
                    +","+false_negative+","+false_positive+");";
                System.out.println("sql "+sql);
                stt.execute(sql);
                stt.close();
            }
        }
        conn.close();
        
        // System.out.println(cls[0].toString());
    }    
}
