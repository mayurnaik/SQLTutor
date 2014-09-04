package objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.gatech.sqltutor.IQueryTranslator;

/**
 * The Question class has been implemented as a way to construct English questions from SQL queries which represent answers.
 * 
 * @author		William J. Holton
 * @version		0.0
 */
public class Question implements IQueryTranslator {
	/** The question string will hold the final value of the question after it has been converted  */
	String question;
	/** This represents the database which will hold custom NLP for particular words commonly used to form questions for 
	 * a particular schema.  */
	HashMap<String, String> spellCheckDictionary = new HashMap<String, String>();
	HashMap<String, String> phraseCheckDictionary = new HashMap<String, String>();
	
	String query;
	List<DatabaseTable> schemaMetaData;
	
	/** 
	 * A blank initializer that is used for testing reasons (provides a blank question).
	 */
	public Question(String query) {
		this(query, null);
	}
	
	/** 
	 * An initializer for questions formed by answer queries. It splits the query into an array of words and symbols that
	 * were separated by spaces. From here we send the query to a specific template to be processed into English.
	 */
	public Question(String query, List<DatabaseTable> tables) {
		setQuery(query);
		setSchemaMetaData(tables);
		produceQuestion();
	}
	
	private void produceQuestion() {
		this.question = "";
		
		// Setup the query for being converted to English.
		String[] tokenizedQuery = setupQuery(query);
		if(tokenizedQuery[0].equalsIgnoreCase("SELECT")) {
			convertSelectStatement(tokenizedQuery, schemaMetaData);
		} else {
			question = "Sorry. We were unable to adequately convert this query to English.";
		}
		// test for these hard coded phrases / spell checks. Eventually we'll want to find a more reliable spell checker
		// and have the phrases be able to be added by anyone with teacher or admin access
		spellCheckDictionary.put("citys", "cities");
		spellCheckDictionary.put("addresss", "addresses");
		spellCheckDictionary.put("salarys", "salaries");
		spellCheckDictionary.put("birthdate", "birth date");
		spellCheckDictionary.put("birthdates", "birth dates");
		// if we want to make this sort of formatting common, as it is in English, we'll have to do it
		// on the attribute level. If they have: "SELECT last_name, salary, first_name" the following
		// won't work, but should.
		phraseCheckDictionary.put("first name and last name", "first and last name");
		questionSpellCheck();
		questionPhraseCheck();
	}
	
	
	/** 
	 * This method will step through a select statement and convert it to an English question. The final English template should
	 * look like: "List the {columnNames} of {tableName} whose {constraints}". Spacing within the question is the responsibility 
	 * of the current index and should only take into account what came before.
	 * <p>
	 * Currently this method supports the following syntax: SELECT {any number of columns, or '*'} FROM {one table} WHERE
	 * {constraints using '>', '<', '<>', '=', '<=', '>=' and IN.) AND {additional constraints}
	 * 
	 * @param tokenizedQuery	The split version of the query. Each token represents a table name, column name, SQL syntax, or
	 * a symbol (such as parenthesis) that is used to quantify bounds.
	 */
	public void convertSelectStatement(String[] tokenizedQuery, List<DatabaseTable> tables) {
		int index = 0;
		index = convertSelect(tokenizedQuery, index);
		index = convertAttributesAndEntities(tokenizedQuery, index, tables);
		index = convertConstraints(tokenizedQuery, index, tables);
		question += ".";
		// Removal of underscores; does not touch anything bounded by apostrophes.
		question = question.replaceAll("(?x)_(?=(?:[^']*'[^']*')*[^']*$)", " ");
	}
	
	public String[] setupQuery(String query) {
		// Check for a semicolon ending the query and remove it if it exists.
		if(query.substring(query.length()-1, query.length()).equals(";")) {
			query = query.substring(0, query.length()-1);
		}
		return query.split(" ");
	}
	
	public int convertSelect(String[] tokenizedQuery, int index) {
		/*
		 * Handles all keywords before attributes. 
		 */
		Random random = new Random();
		String[] selectVerbs = { "List the", "Retrieve the", "Output the", "Show the" };
		question += selectVerbs[random.nextInt(selectVerbs.length)];
		index++;
		if(tokenizedQuery[index].equalsIgnoreCase("ALL")) {
			// "ALL" is default, so we ignore this case.
			index++;
		} else if (tokenizedQuery[index].equalsIgnoreCase("DISTINCT")) {
			question += " distinct";
			index++;
		} 		
		return index;
	}
	
	public int convertAttributesAndEntities(String[] tokenizedQuery, int index, List<DatabaseTable> tables) {
		// We're going to format a linked list of linked lists, such that:
		// {entity1} -> {attribute1} -> {attribute2}
		// {entity2} -> {attribute1}
		LinkedList<LinkedList<String>> mainList = new LinkedList<LinkedList<String>>();
		ArrayList<String> mainListKey = new ArrayList<String>();
		// Pull the set of attributes and entities into separate strings for easier handling before breaking them
		// back up.
		String attributes = tokenizedQuery[index];
		index++;
		for( ; !tokenizedQuery[index].equalsIgnoreCase("FROM") ; index++) {
			attributes += " " + tokenizedQuery[index];
		}
		index++;
		String entities = tokenizedQuery[index];
		index++;
		for( ; index < tokenizedQuery.length && !tokenizedQuery[index].equalsIgnoreCase("WHERE") ; index++) {
				entities += " " + tokenizedQuery[index];
		}
		if( tables == null ) {
			if( entities.contains(",") == false && entities.contains(" JOIN ") == false) {
				mainListKey.add(entities);
				LinkedList<String> subList = new LinkedList<String>();
				mainList.add(subList);
			} else {
				throw new IllegalStateException("No schema info, and there is more than one entity.");
			}
		} else {
			Iterator<DatabaseTable> databaseTableIterator = tables.iterator();
			DatabaseTable databaseTable;
			while(databaseTableIterator.hasNext()) {
				databaseTable = databaseTableIterator.next();
				if(entities.contains(databaseTable.getTableName())) {
					if(!mainListKey.contains(databaseTable.getTableName())) {
						mainListKey.add(databaseTable.getTableName());
						LinkedList<String> subList = new LinkedList<String>();
						mainList.add(subList);
					}
				}
			}
		}
		// We bring the attributes to lower case and replace all white spaces.
		attributes = attributes.toLowerCase().replaceAll("\\s+","");
		String[] attributeArray = attributes.split(",");
		// Does not currently handle aggregate or scalar functions at all.
		for(int i = 0; i < attributeArray.length; i++) {
			// Drop AS and everything after it.
			if(attributeArray[i].contains(" as ")) {
				int asIndex = attributeArray[i].indexOf(" as ");
				attributeArray[i] = attributeArray[i].substring(0, asIndex);
			}
			if(attributeArray[i].contains(".")) {
				Pattern pattern = Pattern.compile("[\\w_]+\\.[\\w_]+");
				Matcher matcher = pattern.matcher(attributeArray[i]);
				attributeArray[i] = convertArithmetic(attributeArray[i]);
				while(matcher.find()) {
					String[] match = matcher.group().split("\\.");
					attributeArray[i] = attributeArray[i].replace(matcher.group(), match[1]);
					mainList.get(mainListKey.indexOf(match[0])).add(attributeArray[i]);
				}
			} else {
				if(mainListKey.size() == 1) {
						mainList.get(0).add(attributeArray[i]);
				} else {
					// Any of which that aren't explicit need to be pulled from the schema.
					// This pattern does not handle something like "5id". Maybe "[0-9]*[A-Za-z]\\w+"
					Pattern pattern = Pattern.compile("[a-zA-Z][\\w_]+");
					Matcher matcher = pattern.matcher(attributeArray[i]);
					attributeArray[i] = convertArithmetic(attributeArray[i]);
					while(matcher.find()) {
						Iterator<DatabaseTable> databaseTableIterator = tables.iterator();
						DatabaseTable databaseTable;
						while(databaseTableIterator.hasNext()) {
							databaseTable = databaseTableIterator.next();
							if(mainListKey.contains(databaseTable.getTableName()) && databaseTable.getColumns().contains(matcher.group())) {
								mainList.get(mainListKey.indexOf(databaseTable.getTableName())).add(attributeArray[i]);
								break;
							}
						}
					}
				}
			}
			
		}
		ArrayList<Integer> emptyKeyIndexes = new ArrayList<Integer>();
		for(int i = 0; i < mainListKey.size(); i++) {
			if(mainList.get(i).size() == 0) {
				emptyKeyIndexes.add(i);
			}
		}
		Collections.sort(emptyKeyIndexes, Collections.reverseOrder());
		for(int i = 0; i < emptyKeyIndexes.size(); i++) {
			mainList.remove(i);
			mainListKey.remove(i);
		}
		// Now that the lists are complete, we can format the attributes and entities and add them to the question.
		for(int i = 0; i < mainList.size(); i++) {
			if (i > 0 && i != mainList.size() - 1) {
				question += ", the";
			} else if (i > 0 && i == mainList.size() - 1) {
				question += " and the";
			}
			switch(mainList.get(i).size()) {	
				case 0:
					break;
				case 1:
					if(mainList.get(i).get(0).endsWith(",")) {
						question += " " + mainList.get(i).get(0).substring(0, mainList.get(i).get(0).length()-1);
					} else {
						question += " " + mainList.get(i).get(0);
					}
					break;
				// If there are only two attributes then the comma which separates the two isn't required.
				case 2:
					if(mainList.get(i).get(0).endsWith(",")) {
						question += " " + mainList.get(i).get(0).substring(0, mainList.get(i).get(0).length()-1);
					} else {
						question += " " + mainList.get(i).get(0);
					}
					if(mainList.get(i).get(1).endsWith(",")) {
						question += " and " + mainList.get(i).get(1).substring(0, mainList.get(i).get(0).length()-1);
					} else {
						question += " and " + mainList.get(i).get(1);
					}
					break;
				// If there are more than two, then the commas are required.
				default:
					if(!mainList.get(i).get(0).endsWith(",")) {
						question += " " + mainList.get(i).get(0) + ",";		
					} else {
						question += " " + mainList.get(i).get(0);		
					}
					int j = 1;
					for( ; j < mainList.get(i).size() - 1; j++) {
						if(!mainList.get(i).get(j).endsWith(",")) {
							question += " " + mainList.get(i).get(j) + ",";		
						} else {
							question += " " + mainList.get(i).get(j);	
						}
					}
					question += " and " + mainList.get(i).get(j);
			}
			// checks after FROM for the "AS" keyword to make adjustments here if needed.
			question += " of all " + mainListKey.get(i) + "s";
		}
		return index;
	}
	
	public int convertConstraints(String[] tokenizedQuery, int index, List<DatabaseTable> tables) {
		/*
		 * 
		 * HANDLES "WHERE"
		 * 
		 */
		boolean constraintsExist = false;
		while(index < tokenizedQuery.length) {
			if (tokenizedQuery[index].equalsIgnoreCase("WHERE")) {

				constraintsExist = true;
				break;
			} 
			index++;
		}
		if (constraintsExist) {
			do {
				if (tokenizedQuery[index].equalsIgnoreCase("AND")) {
					question += " and";
				}
				index++;
				if(tokenizedQuery[index].contains(".")) {
					String[] split = tokenizedQuery[index].split("\\.");
					question += " whose " + split[1].toLowerCase();
				} else {
					question += " whose " + tokenizedQuery[index];
				}
				index++;
				if (tokenizedQuery[index].equals("=") || tokenizedQuery[index].equals(">") || tokenizedQuery[index].equals("<") || 
						tokenizedQuery[index].equals("<>") || tokenizedQuery[index].equals(">=") || tokenizedQuery[index].equals("<=")) {
					question += " is";
					if (tokenizedQuery[index].equals(">")) {			// Switch statement in Java 1.7
						question += " greater than";
					} else if (tokenizedQuery[index].equals("<")) {
						question += " less than";
					} else if (tokenizedQuery[index].equals("<>")) {
						question += " not";
					} else if (tokenizedQuery[index].equals(">=")) {
						question += " greater than or is";
					} else if (tokenizedQuery[index].equals("<=")) {
						question += " less than or is";
					}
					index++;
					// check for constraining values enclosed in apostrophes.
					if (tokenizedQuery[index].startsWith("'")) {	
						// if it was a single word surrounded by apostrophes then we simply remove the apostrophes and add it.
						if(tokenizedQuery[index].endsWith("'")) {
							question += " " + tokenizedQuery[index].substring(1, tokenizedQuery[index].length()-1);
							index++;
						} else {	//remove the first apostrophe and search for the next:
							question += " " + tokenizedQuery[index].substring(1, tokenizedQuery[index].length());
							index++;
							while(!tokenizedQuery[index].endsWith("'")) {
								question += " " + tokenizedQuery[index];
								index++;
							}
							// remove last apostrophe and add it
							question += " " + tokenizedQuery[index].substring(0, tokenizedQuery[index].length()-1);
							index++;
						}
						
					} else {
						question += " " + tokenizedQuery[index];
						index++;
					}
				} else if (tokenizedQuery[index].equalsIgnoreCase("IN")) {
					index++;
					question += " would appear in the answer to following sub-question:\n";
					index++;	// the first thing after IN should be the parenthesis, so we skip it
					String subQuery = tokenizedQuery[index];
					index++;
					for ( ; index < tokenizedQuery.length && !tokenizedQuery[index].equals(")"); index++) {
						subQuery += " " + tokenizedQuery[index];
					}	
					Question subQuestion = new Question(subQuery);
					question += " " + subQuestion.getQuestion();
					question = question.substring(0, question.length()-1);	// get rid of the extra period, it will be added after the loop
				}
			} while (index < tokenizedQuery.length && tokenizedQuery[index].equalsIgnoreCase("AND"));
		}
		return index;
	}
	
	public String convertArithmetic(String attribute) {
		// check for and handle arithmetic operations
		if(attribute.contains("+"))
			attribute = attribute.replace("+", " plus ");
		if(attribute.contains("*"))
			attribute = attribute.replace("*", " times ");
		if(attribute.contains("-")) {
			if(attribute.startsWith("-")) {
				attribute = attribute.replace("-", "negative ");
			} else {
				attribute = attribute.replace("-", " minus ");
			}	
		}
		if(attribute.contains("/"))
			attribute = attribute.replace("/", " divided by ");
		if(attribute.contains("%"))
			attribute = attribute.replace("%", " modulo ");
		return attribute;
	}
	
	
	public void questionSpellCheck() {
		// add all dictionary keys to a list
		Iterator<String> phraseKeyIterator = spellCheckDictionary.keySet().iterator();
		ArrayList<String> phraseKeyList = new ArrayList<String>();
		while (phraseKeyIterator.hasNext()) {
			phraseKeyList.add(phraseKeyIterator.next());
		}
		// check if the question contains any of these keys
		for(int i = 0; i < phraseKeyList.size(); i++) {
			if(question.contains(phraseKeyList.get(i))) {
				question = question.replaceAll(phraseKeyList.get(i), spellCheckDictionary.get(phraseKeyList.get(i)));
			}

		}
	}
	
	public void questionPhraseCheck() {
		// add all dictionary keys to a list
		Iterator<String> phraseKeyIterator = phraseCheckDictionary.keySet().iterator();
		ArrayList<String> phraseKeyList = new ArrayList<String>();
		while (phraseKeyIterator.hasNext()) {
			phraseKeyList.add(phraseKeyIterator.next());
		}
		// check if the question contains any of these keys
		for(int i = 0; i < phraseKeyList.size(); i++) {
			if(question.contains(phraseKeyList.get(i))) {
				question = question.replaceAll(phraseKeyList.get(i), phraseCheckDictionary.get(phraseKeyList.get(i)));
			}

		}
	}
	
	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	
	@Override
	public String getQuery() {
		return query;
	}
	
	@Override
	public void setQuery(String sql) {
		this.query = sql;
		this.question = null;
	}
	
	@Override
	public List<DatabaseTable> getSchemaMetaData() {
		return schemaMetaData;
	}
	
	@Override
	public void setSchemaMetaData(List<DatabaseTable> tables) {
		this.schemaMetaData = tables;
	}
	
	@Override
	public Object getTranslatorType() {
		return "Manual Splitter";
	}
	
	@Override
	public String getTranslation() {
		if( question == null || "".equals(question) ) {
			produceQuestion();
		}
		return question;
	}
}
