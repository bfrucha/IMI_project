package upsud.students.imi_project.queries;

/**
 * Created by Bruno on 04/02/2015.
 */
import android.app.Activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.sail.memory.MemoryStore;

import upsud.students.imi_project.R;

public class Queries {

    public static HashMap<String, ArrayList<String>> inputModalities = new HashMap<String, ArrayList<String>>();
    public static HashMap<String, ArrayList<String>> outputModalities = new HashMap<String, ArrayList<String>>();

    public static final String[] constraints = { "Noise", "Speed", "LineStatus", "Brightness" };
    public static HashMap<String, String> currentConstraints = new HashMap<String, String>();


    /* example RDF + SPARQL */
    public static String adaptToScenario(Activity activity, int scenarioID) throws Exception {

		/* repository */
        Repository repo = new SailRepository(new MemoryStore());
        repo.initialize();
        RepositoryConnection con = repo.getConnection();

		/* parser */
        // URL doc = (new File("graph.rdf")).toURI().toURL();
        InputStream inputStream = activity.getResources().openRawResource(R.raw.graph);
        RDFParser rdfParser = new RDFXMLParser(); // or TurtleParser if you have a turtle RDF file !
        ArrayList<Statement> myList = new ArrayList<Statement>();
        StatementCollector collector = new StatementCollector(myList);
        rdfParser.setRDFHandler(collector);

		/* parse */
        rdfParser.parse(inputStream, "graph");
		/* import in repository */
        for (Statement statement: collector.getStatements())
            con.add(statement);

		/* load scenario file */
        myList = new ArrayList<Statement>();
        collector = new StatementCollector(myList);
        rdfParser.setRDFHandler(collector);

        // doc = (new File("scenario.rdf")).toURI().toURL();
        inputStream = activity.getResources().openRawResource(scenarioID);
        rdfParser.parse(inputStream, "currentScenario");

        for(Statement statement: collector.getStatements()) {
            con.add(statement);
        }

		/* print repository */
		/*for (Statement statement: con.getStatements(null, null, null, true).asList())
			System.out.println(statement);
		*/

        System.out.println("\n\nQUERY\n\n");

		/* SPARQL Queries */
        String prefix = "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                + "PREFIX imi:   <http://intelligent-multimodal-interaction.org/relations/> \n"
                + "PREFIX base:  <http://intelligent-multimodal-interaction.org/concepts/> \n";


		/*
		 * INPUT MODALITIES
		 */

		/* search an input modality and its associated constraint domain */
        String queryString = prefix+"SELECT ?x ?y WHERE {" +
                "?x imi:constraint-value ?y . "+
                "?x rdf:type base:InputModality } ";

        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        TupleQueryResult result = tupleQuery.evaluate();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value valueOfX = bindingSet.getValue("x");
            Value valueOfY = bindingSet.getValue("y");
            // System.out.println(valueOfX + " " + valueOfY);

            try { inputModalities.get(valueOfX.toString()).add(valueOfY.toString()); }
            catch (NullPointerException exn) {
                ArrayList<String> values = new ArrayList<String>();
                values.add(valueOfY.toString());
                inputModalities.put(valueOfX.toString(), values);
            }
        }

		/* search for second level input modalities */
        queryString = prefix+"SELECT ?x ?y WHERE {" +
                "?x imi:constraint-value ?y . "+
                "?x rdf:type ?z ."+
                "?z rdf:type base:InputModality } ";

        tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        result = tupleQuery.evaluate();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value valueOfX = bindingSet.getValue("x");
            Value valueOfY = bindingSet.getValue("y");
            // System.out.println(valueOfX + " " + valueOfY);

            try { inputModalities.get(valueOfX.toString()).add(valueOfY.toString()); }
            catch (NullPointerException exn) {
                ArrayList<String> values = new ArrayList<String>();
                values.add(valueOfY.toString());
                inputModalities.put(valueOfX.toString(), values);
            }
        }




		/*
		 * OUTPUT MODALITIES
		 */
        queryString = prefix+"SELECT ?x ?y WHERE {" +
                "?x imi:constraint-value ?y . "+
                "?x rdf:type base:OutputModality } ";

        tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        result = tupleQuery.evaluate();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value valueOfX = bindingSet.getValue("x");
            Value valueOfY = bindingSet.getValue("y");
            // System.out.println(valueOfX + " " + valueOfY);

            try { outputModalities.get(valueOfX.toString()).add(valueOfY.toString()); }
            catch (NullPointerException exn) {
                ArrayList<String> values = new ArrayList<String>();
                values.add(valueOfY.toString());
                outputModalities.put(valueOfX.toString(), values);
            }
        }

        queryString = prefix+"SELECT ?x ?y WHERE {" +
                "?x imi:constraint-value ?y . "+
                "?x rdf:type ?z ."+
                "?z rdf:type base:OutputModality } ";

        tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        result = tupleQuery.evaluate();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value valueOfX = bindingSet.getValue("x");
            Value valueOfY = bindingSet.getValue("y");
            // System.out.println(valueOfX + " " + valueOfY);

            try { outputModalities.get(valueOfX.toString()).add(valueOfY.toString()); }
            catch (NullPointerException exn) {
                ArrayList<String> values = new ArrayList<String>();
                values.add(valueOfY.toString());
                outputModalities.put(valueOfX.toString(), values);
            }
        }



		/* PRINT MODALITIES */
		/*System.out.println("Output modalities");
		for(String key : outputModalities.keySet()) {
			System.out.println(key + " : ");
			for(String value : outputModalities.get(key)) {
				System.out.println(value);
			}
			System.out.println();
		}*/


		/* get all usable modalities */
        ArrayList<String> usableModalities = new ArrayList<String>();
        for(String concept : constraints) {
            queryString = prefix+"SELECT ?c WHERE {" +
                    "?x imi:constraint-value ?c . "+
                    "?x rdf:type base:"+concept+" } ";

            tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            result = tupleQuery.evaluate();
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                String constraint = bindingSet.getValue("c").toString();

                currentConstraints.put(concept, constraint);

                for(String modality : inputModalities.keySet()) {
                    inputModalities.get(modality).remove(constraint);
                }

                for(String modality : outputModalities.keySet()) {
                    outputModalities.get(modality).remove(constraint);
                }
            }
        }

        String res = "Scenario : ";

        for(Map.Entry<String, String> entry : currentConstraints.entrySet()) {
            res += entry.getKey() + " : " + entry.getValue();
        }

        res += "\nUsable input modalities for the given scenario : ";
        for(String modality : inputModalities.keySet()) {
            if(inputModalities.get(modality).size() == 0) {res += modality;}
        }

        res += "\nUsable output modalities for the given scenario : ";
        for(String modality : outputModalities.keySet()) {
            if(outputModalities.get(modality).size() == 0) {res += modality;} }

        return res;
    }
}
