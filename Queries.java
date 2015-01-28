package queries;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
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

public class Queries {

	public static HashMap<String, ArrayList<String>> inputModalities = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, ArrayList<String>> outputModalities = new HashMap<String, ArrayList<String>>();
	
	public static final String[] constraints = { "Noise", "Speed", "LineStatus", "Brightness" };
	public static HashMap<String, String> currentConstraints = new HashMap<String, String>();
	
	public static void main(String [] args) throws Exception {
		/* test0(); */
		example();
	}
	
	/* example RDF + SPARQL */
	private static void example() throws Exception {

		/* repository */
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection con = repo.getConnection();
		
		/* parser */
		URL doc = (new File("graph.rdf")).toURI().toURL();
		InputStream inputStream = doc.openStream();
		RDFParser rdfParser = new RDFXMLParser(); // or TurtleParser if you have a turtle RDF file !
		ArrayList<Statement> myList = new ArrayList<Statement>();
		StatementCollector collector = new StatementCollector(myList);
		rdfParser.setRDFHandler(collector);
		
		/* parse */
		rdfParser.parse(inputStream, doc.toString());
		/* import in repository */
		for (Statement statement: collector.getStatements())
			con.add(statement);
		
		/* load scenario file */
		myList = new ArrayList<Statement>();
		collector = new StatementCollector(myList);
		rdfParser.setRDFHandler(collector);
		
		doc = (new File("scenario.rdf")).toURI().toURL();
		inputStream = doc.openStream();
		rdfParser.parse(inputStream,  doc.toString());

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
		
		System.out.println();
		System.out.println("Scenario : ");
		for(Map.Entry<String, String> entry : currentConstraints.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		
		System.out.println();
		System.out.println("Usable input modalities for the given scenario : ");
		for(String modality : inputModalities.keySet()) { 
			if(inputModalities.get(modality).size() == 0) {System.out.println(modality);} }
		
		System.out.println();
		System.out.println("Usable output modalities for the given scenario : ");
		for(String modality : outputModalities.keySet()) { 
			if(outputModalities.get(modality).size() == 0) {System.out.println(modality);} }
	}
	
	/* The following method contains some example of statement manipulation in a secondary repository */
	/* This could be used, for instance, to store the results of a query and then perform queries on these */
	/* Or to combine query results together */
	private static void test0() throws Exception {
		/* create repository */
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		/* build a connection to manipulate the repository */
		RepositoryConnection con = repo.getConnection();
		/* build a factory to build statements */
		ValueFactory factory = repo.getValueFactory();
		/* building statements */
		URI bob = factory.createURI("http://example.org/bob");
		URI name = factory.createURI("http://example.org/name");
		URI person = factory.createURI("http://example.org/person");
		Literal bobsName = factory.createLiteral("Bob");
		Statement nameStatement = factory.createStatement(bob, name, bobsName);
		Statement typeStatement = factory.createStatement(bob, RDF.TYPE, person);
		/* adding statements */
		con.add(nameStatement);
		con.add(typeStatement);
		/* going through statements */
		for (Statement statement: con.getStatements(null, null, null, true).asList()) {
			System.out.println(statement);
		}
		/* querying */
		String queryString = "SELECT ?x ?y WHERE { ?x ?p ?y } ";
		TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult result = tupleQuery.evaluate();
		/* manipulating result of query */
		BindingSet bindingSet = result.next();
		Value valueOfX = bindingSet.getValue("x");
		//Value valueOfY = bindingSet.getValue("y");
		System.out.println(valueOfX);
		/* end */
		result.close();
		con.close();
	}
}
