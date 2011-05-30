/**
 * 
 */
package com.pjaol.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

/**
 * @author pjaol
 * 
 */
public class EntityExtractor {

	private static final String modelsDir = "data/models/opennlp.sourceforge.net/models-1.5/";
	private static final String personModel = "en-ner-person.bin";
	private static final String locationModel = "en-ner-location.bin";
	private static final String dateModel = "en-ner-date.bin";

	NameFinderME NFinder, LFinder;
	
	private enum NLPModel {
		personModel, locationModel, dateModel
	};

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EntityExtractor ee = new EntityExtractor();
		try {
			ee.extractNames("data/test/california_ammunition.txt");
			System.out.println("------------------------------");
			ee.extractNames("data/test/foodies_in_LA.txt");
			System.out.println("------------------------------");
			ee.extractNames("data/test/whats_next_keith_olbermann.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public EntityExtractor() {
	
		try {
			NFinder= loadModel(NLPModel.personModel);
			LFinder= loadModel(NLPModel.locationModel);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	public void extractNames(String filename) throws IOException {

		String[] sentences = readFile(filename).split("\\.");
		
		HashMap<String, Integer> NUniques = new HashMap<String, Integer>();
		HashMap<String, Integer> LUniques = new HashMap<String, Integer>();
		
		Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
		for (int si = 0; si < sentences.length; si++) {
			String[] tokens = tokenizer.tokenize(sentences[si]);
			Span[] names = NFinder.find(tokens);
			Span[] locations = LFinder.find(tokens);
			
			displayTokens(NUniques, names, tokens);
			displayTokens(LUniques, locations, tokens);
		}
		NFinder.clearAdaptiveData();
		LFinder.clearAdaptiveData();
	}

	private void displayTokens( Map<String, Integer> uniques, Span[] names, String[] tokens) {
		
		for (int si = 0; si < names.length; si++) {
			StringBuilder cb = new StringBuilder();
			for (int ti = names[si].getStart(); ti < names[si].getEnd(); ti++) {
				cb.append(tokens[ti]).append(" ");
			}
			String k = cb.substring(0, cb.length() - 1);
			
			if (!uniques.containsKey(k)){
				System.out.println(names[si].getType()+": "+ k);
				uniques.put(k, 1);
			}
			
		}
	}
	
	
	public NameFinderME loadModel(NLPModel modelName) throws FileNotFoundException {
		String module = null;
		switch (modelName) {
		case personModel:
			module = personModel;
			break;
		case locationModel:
			module = locationModel;
			break;
		case dateModel:
			module = dateModel;
			break;
		}
		InputStream modelIn = new FileInputStream(modelsDir+module);
		TokenNameFinderModel model = null;
		try {
			model = new TokenNameFinderModel(modelIn);
		} catch (InvalidFormatException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		NameFinderME finder = new NameFinderME(model);
		return finder;
	}

	private String readFile(String filename) throws FileNotFoundException {

		StringBuilder text = new StringBuilder();
		String NL = System.getProperty("line.separator");
		Scanner scanner = new Scanner(new FileInputStream(filename), "UTF-8");
		try {
			while (scanner.hasNextLine()) {
				text.append(scanner.nextLine() + NL);
			}
		} finally {
			scanner.close();
		}
		return text.toString();
	}

}
