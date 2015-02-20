package upsud.students.imi_project.queries;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.util.Log;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import upsud.students.imi_project.R;

public class Queries {

	/** INPUT MODALITIES **/
	public static final int IM_TOUCH = 0;
	public static final int IM_GESTURE = 1;
	public static final int IM_BUTTON = 2;
	public static final int IM_AIR_GESTURE = 3;
	public static final int IM_SPEECH = 4;
	
	/** OUTPUT MODALITIES **/
	public static final int OM_MUSIC = 5;
	public static final int OM_SOUND_ALERT = 6;
	public static final int OM_TEXT_TO_SPEECH = 7;
	public static final int OM_GRAPHICS = 8;
	public static final int OM_NIGHT_GRAPHICS = 9;
	public static final int OM_DAY_GRAPHICS = 10;
	public static final int OM_GRAPHIC_ALERT = 11;
	public static final int OM_VIBRATION = 12;
	
	
	public static HashMap<String, Set<String>> inputModalities = new HashMap<String, Set<String>>();
	public static HashMap<String, Set<String>> outputModalities = new HashMap<String, Set<String>>();
	
	public static final String[] constraints = { "Noise", "Speed", "LineStatus", "Brightness" };
	public static HashMap<String, String> currentConstraints = new HashMap<String, String>();
	
	
	static public ArrayList<ArrayList<Integer>> adaptToScenario(Activity activity, int scenarioID) {
		
		/** RDF PART **/
		// create graph model
		Model graphModel = ModelFactory.createDefaultModel();
		
		// read the graph file
		InputStream in = activity.getResources().openRawResource(R.raw.graph);
		
		if (in == null) {
		    Log.e("Jena", "File: graph.rdf not found");
		}

		// read the RDF/XML file
		graphModel.read(in, null);
		
		
		// load scenario
		Model scenarioModel = ModelFactory.createDefaultModel();
		in = activity.getResources().openRawResource(scenarioID);
		
		if(in == null) {
			Log.e("Jena", "File: scenario.rdf not found");
		}
		scenarioModel.read(in, null);
		
		// merge both graphs together
		Model unionModel = graphModel.union(scenarioModel);
		
		// DEBUG
		// print all statements in the model
		/*StmtIterator it = union.listStatements();
		while(it.hasNext()) {
			Log.d("Statements", it.next().toString());
		}*/
		
		
		/** SPARQL PART **/
		// Create a new query
		String queryPrefix = 
			  "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX imi:   <http://intelligent-multimodal-interaction.org/relations/> \n"
			+ "PREFIX base:  <http://intelligent-multimodal-interaction.org/concepts/> \n";

		
		// INPUT MODALITIES
		String imQuery = queryPrefix + 
				"SELECT ?x ?c WHERE {" +
				"?x imi:constraint-value ?c . "+
				"?x rdf:type base:InputModality } ";
		
		Query query = QueryFactory.create(imQuery);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, unionModel);
		ResultSet results = qe.execSelect();
		
		while(results.hasNext()) {
			QuerySolution solution = results.next();
			
			// DEBUG
			// print all the InputModality types
			// Log.d("Results", solution.toString());
		
			String concept = solution.getResource("x").toString();
			String constraint = solution.getLiteral("c").toString();
			
			// DEBUG
			Log.d("Input Modalities 1 :", concept + " " + constraint);
			
			try { 
				inputModalities.get(concept).add(constraint);
			}
			catch (NullPointerException exn) {
				TreeSet<String> constraints = new TreeSet<String>();
				constraints.add(constraint);
				inputModalities.put(concept, constraints);
			}
		}
		
		// search for IM of second level
		imQuery = queryPrefix + 
				"SELECT ?x ?c WHERE {" +
				"?x imi:constraint-value ?c . "+
				"?x rdf:type ?z ."+
				"?z rdf:type base:InputModality } ";
		
		query = QueryFactory.create(imQuery);

		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, unionModel);
		results = qe.execSelect();

		while(results.hasNext()) {
			QuerySolution solution = results.next();
			
			// DEBUG
			// print all the InputModality types
			// Log.d("Results", solution.toString());
		
			String concept = solution.getResource("x").toString();
			String constraint = solution.getLiteral("c").toString();
			

			// DEBUG
			Log.d("Input Modalities 2 : ", concept + " " + constraint);
			
			try { 
				inputModalities.get(concept).add(constraint);
			}
			catch (NullPointerException exn) {
				TreeSet<String> constraints = new TreeSet<String>();
				constraints.add(constraint);
				inputModalities.put(concept, constraints);
			}
		}

		
		// OUTPUT MODALITIES
		String omQuery = queryPrefix + 
				"SELECT ?x ?c WHERE {" +
				"?x imi:constraint-value ?c . "+
				"?x rdf:type base:OutputModality } ";
				
		query = QueryFactory.create(omQuery);

		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, unionModel);
		results = qe.execSelect();

		while(results.hasNext()) {
			QuerySolution solution = results.next();
			
			// DEBUG
			// print all the InputModality types
			// Log.d("Results", solution.toString());
		
			String concept = solution.getResource("x").toString();
			String constraint = solution.getLiteral("c").toString();
			
			
			// DEBUG
			// Log.d("Output Modalities 1 : ", valueOfX + " " + valueOfY);
			
			try { 
				outputModalities.get(concept).add(constraint);
			}
			catch (NullPointerException exn) {
				TreeSet<String> constraints = new TreeSet<String>();
				constraints.add(constraint);
				outputModalities.put(concept, constraints);
			}
		}
		
		
		// om 2nd level
		omQuery = queryPrefix + 
				"SELECT ?x ?c WHERE {" +
				"?x imi:constraint-value ?c . "+
				"?x rdf:type ?z ."+
				"?z rdf:type base:OutputModality } ";
				
		query = QueryFactory.create(omQuery);

		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, unionModel);
		results = qe.execSelect();

		while(results.hasNext()) {
			QuerySolution solution = results.next();
			
			// DEBUG
			// print all the InputModality types
			// Log.d("Results", solution.toString());
		
			String concept = solution.getResource("x").toString();
			String constraint = solution.getLiteral("c").toString();
			

			// DEBUG
			// Log.d("Output Modalities 2 : ", valueOfX + " " + valueOfY);
			
			try { 
				outputModalities.get(concept).add(constraint);
			}
			catch (NullPointerException exn) {
				TreeSet<String> constraints = new TreeSet<String>();
				constraints.add(constraint);
				outputModalities.put(concept, constraints);
			}
		}
		
		
		// USABLE MODALITIES
		ArrayList<String> usableModalities = new ArrayList<String>();
		for(String concept : constraints) {
			String usableQuery = queryPrefix + 
					"SELECT ?c WHERE {" +
					"?x imi:constraint-value ?c . "+
					"?x rdf:type base:"+concept+" } ";
			
			query = QueryFactory.create(usableQuery);

			// Execute the query and obtain results
			qe = QueryExecutionFactory.create(query, unionModel);
			results = qe.execSelect();

			while(results.hasNext()) {
				QuerySolution solution = results.next();
				
				// DEBUG
				Log.d("Results", solution.toString());
				
				String constraint = solution.getLiteral("c").toString();
			
				// record all scenario's constraints 
				currentConstraints.put(concept, constraint);
				
				// remove constraints from map
				for(String modality : inputModalities.keySet()) {
					inputModalities.get(modality).remove(constraint);
				}
				
				for(String modality : outputModalities.keySet()) {
					outputModalities.get(modality).remove(constraint);
				}
			}
		}

		// Important - free up resources used running the query
		qe.close();

		

		// DEBUG
		// print results => Scenario's Constraints, IM and OM usable
		//for(Map.Entry<String, String> entry : currentConstraints.entrySet()) {
		//	Log.d("Scenario", entry.getKey() + " : " + entry.getValue());
		//}
		

		ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>(); 
		
		ArrayList<Integer> finalIM = new ArrayList<Integer>();
		for(String modality : inputModalities.keySet()) { 
			if(inputModalities.get(modality).size() == 0) { finalIM.add(codeModality(modality)); }
		}
		
		ArrayList<Integer> finalOM = new ArrayList<Integer>();
		for(String modality : outputModalities.keySet()) { 
			if(outputModalities.get(modality).size() == 0) { finalOM.add(codeModality(modality)); } 
		}
		
		// DEBUG
		//ArrayList<Integer> list = new ArrayList<Integer>();
		for(String modality : inputModalities.keySet()) {
			String text = "";
			for(String constraint : inputModalities.get(modality)) { text += constraint + " "; }
			Log.d(modality, text);
		}
		for(String modality : outputModalities.keySet()) { 
			String text = "";
			for(String constraint : outputModalities.get(modality)) { text += constraint + " "; }
			Log.d(modality, text);
		}
		
		//for(Integer i : list) { Log.d("modality number", i+""); }
		
		res.add(finalIM);
		res.add(finalOM);
		
		return res;
	}
	
	// returns the associated number of the given modality
	public static int codeModality(String modality) {
		modality = modality.toLowerCase();
		modality = modality.substring(modality.lastIndexOf('/')+1);
		
		// DEBUG
		// Log.d("modality", modality);
		
		if		(modality.equals("touch")) 			{ return IM_TOUCH; }
		else if (modality.equals("gesture")) 		{ return IM_GESTURE; }
		else if (modality.equals("button")) 		{ return IM_BUTTON; }
		else if (modality.equals("airgesture")) 	{ return IM_AIR_GESTURE; }
		else if (modality.equals("speech")) 		{ return IM_SPEECH; }
		else if (modality.equals("music")) 			{ return OM_MUSIC; }
		else if (modality.equals("soundalert")) 	{ return OM_SOUND_ALERT; }
		else if (modality.equals("texttospeech")) 	{ return OM_TEXT_TO_SPEECH; }
		else if (modality.equals("graphics")) 		{ return OM_GRAPHICS; }
		else if (modality.equals("nightgraphics")) 	{ return OM_NIGHT_GRAPHICS; }
		else if (modality.equals("daygraphics"))	{ return OM_DAY_GRAPHICS; }
		else if (modality.equals("graphicalert")) 	{ return OM_GRAPHIC_ALERT; }
		else 									  	{ return OM_VIBRATION; }
	}
	
	
	// returns the modality associated to the given number
	public static String decodeModality(int modalityNumber) {
		
		switch(modalityNumber) {
			case IM_TOUCH: 			return "Touch";
			case IM_GESTURE: 		return "Gesture";
			case IM_BUTTON: 		return "Button";
			case IM_AIR_GESTURE: 	return "AirGesture";
			case IM_SPEECH: 		return "Speech";
			case OM_MUSIC: 			return "Music";
			case OM_SOUND_ALERT: 	return "SoundAlert";
			case OM_TEXT_TO_SPEECH: return "TextToSpeech";
			case OM_GRAPHICS: 		return "Graphics";
			case OM_NIGHT_GRAPHICS: return "NightGraphics";
			case OM_DAY_GRAPHICS: 	return "DayGraphics";
			case OM_GRAPHIC_ALERT: 	return "GraphicAlert";
			default: 				return "Vibration";
		}
		
	}
}
