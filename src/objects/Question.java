package objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * The Question class has been implemented as a way to construct English questions from SQL queries which represent answers.
 * 
 * @author		William J. Holton
 * @version		0.0
 */
public class Question {
	/** The question string will hold the final value of the question after it has been converted  */
	String question = "";
	/** This represents the database which will hold custom NLP for particular words commonly used to form questions for 
	 * a particular schema.  */
	HashMap<String, String> spellcheckDictionary = new HashMap<String, String>();
	HashMap<String, String> phrasecheckDictionary = new HashMap<String, String>();
	
	/** 
	 * A blank initializer that is used for testing reasons (provides a blank question).
	 */
	public Question() {}
	
	/** 
	 * An initializer for questions formed by answer queries. It splits the query into an array of words and symbols that
	 * were separated by spaces. From here we send the query to a specific template to be processed into English.
	 */
	public Question(String query) {
		// we want to break here if they try to use something that is unavailable.
		if(query.toLowerCase().contains(" as ")) {
			question = "Sorry. We were unable to adequately convert this query to English.";
			return;
		}
		// this is going on the idea that it won't show up as actually needed, as in 'John;'. It would be oddity if it did.
		query = query.replaceAll(";", "");
		String[] answerQuery = query.split(" ");
		
		if(answerQuery[0].equalsIgnoreCase("SELECT")) {		// we can change to switch statement here in java 1.7
			int indexOfFrom = 0;
			while(!answerQuery[indexOfFrom].equalsIgnoreCase("FROM")) {
				indexOfFrom++;
			}
			// This is a simple check for joins. Only works on the default: "FROM {entity}, {entity}"
			int entityCount = 0;
			for(int index = indexOfFrom + 1; index < answerQuery.length; index++) {
				if(answerQuery[index].equalsIgnoreCase("WHERE")) {
					break;
				} else {
					entityCount++;
				}
			}
			if(entityCount == 1) {
				question = selectQueryToEnglish(answerQuery);
			} else {
				question = joinQueryToEnglish(answerQuery);
			}
		}
		else {
			question = "Sorry. We were unable to adequately convert this query to English.";
		}
		// test for these hard coded phrases / spell checks. Eventually we'll want to find a more reliable spell checker
		// and have the phrases be able to be added by anyone with teacher or admin access
		spellcheckDictionary.put("citys", "cities");
		spellcheckDictionary.put("addresss", "addresses");
		spellcheckDictionary.put("salarys", "salaries");
		spellcheckDictionary.put("birthdate", "birth date");
		spellcheckDictionary.put("birthdates", "birth dates");
		phrasecheckDictionary.put("first names and last names", "first and last names");
		// still has the problem of these possibly butting heads.
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
	 * @param answerQuery	The split version of the query. Each token represents a table name, column name, SQL syntax, or
	 * a symbol (such as parenthesis) that is used to quantify bounds.
	 */
	public String selectQueryToEnglish(String[] answerQuery) {
		/*
		 * 
		 * HANDLES "SELECT"
		 * 
		 */
		Random random = new Random();
		String[] selectVerbs = { "List the", "Retrieve the", "Output the" };
		question += selectVerbs[random.nextInt(3)];
		int index = 1;
		if(answerQuery[index].equalsIgnoreCase("ALL")) {
			index++;
		} else if (answerQuery[index].equalsIgnoreCase("DISTINCT")) {
			question += " distinct";
			index++;
		} else if (answerQuery[index].equals("*")) {	
			question += " attributes";
			index++;
		}
		// find the number of attributes to be returned by the select statement
		int attributeCount = 0;
		boolean tempIndexPointsToFrom = false;
		for(int tempIndex = index; tempIndexPointsToFrom != true; tempIndex++) {
			if(answerQuery[tempIndex].equalsIgnoreCase("FROM")) {
				tempIndexPointsToFrom = true;
			} else {
				attributeCount++;
			}
		}
		switch(attributeCount) {	
			// Each case should end with the index pointing to FROM
			case 0:
				break;
			case 1:
				question += " " + answerQuery[index].toLowerCase() + "s";
				index++;
				break;
			// If there are only two attributes then the comma which separates the two isn't required.
			case 2:
				question += " " + answerQuery[index].substring(0, answerQuery[index].length()-1).toLowerCase() + "s";
				index++;
				question += " and " + answerQuery[index].toLowerCase() + "s";
				index++;
				break;
			// If there is more than two, then the commas are required.
			default:
				question += " " + answerQuery[index++].toLowerCase();				
				for( ; index < attributeCount; index++) {
					question += " " + answerQuery[index].toLowerCase();			
				}
				question += " and " + answerQuery[index++].toLowerCase() + "s";
		}
		index++;
		/*
		 * 
		 * HANDLES "FROM"
		 * 
		 */
		if (index == answerQuery.length - 1) {	// if so, we are at the end of the query. No constraints exist.
			question += " of all " + answerQuery[index].toLowerCase() + "s";
		} else {	// if not, constraints exist.
			question += " of the " + answerQuery[index++].toLowerCase() + "s";
			do {
				if (answerQuery[index++].equalsIgnoreCase("AND")) {
					question += " and";
				}
				question += " whose " + answerQuery[index++].toLowerCase();
				if (answerQuery[index].equals("=") || answerQuery[index].equals(">") || answerQuery[index].equals("<") || 
						answerQuery[index].equals("<>") || answerQuery[index].equals(">=") || answerQuery[index].equals("<=")) {
					question += " is";
					if (answerQuery[index].equals(">")) {			// Switch statement in Java 1.7
						question += " greater than";
					} else if (answerQuery[index].equals("<")) {
						question += " less than";
					} else if (answerQuery[index].equals("<>")) {
						question += " not";
					} else if (answerQuery[index].equals(">=")) {
						question += " greater than or is";
					} else if (answerQuery[index].equals("<=")) {
						question += " less than or is";
					}
					index++;
					// check for constraining values enclosed in apostrophes.
					if (answerQuery[index].substring(0, 1).equals("'")) {	
						// if it was a single word surrounded by apostrophes then we simply remove the apostrophes and add it.
						if(answerQuery[index].substring(answerQuery[index].length()-1, answerQuery[index].length()).equals("'")) {
							question += " " + answerQuery[index].substring(1, answerQuery[index].length()-1);
							index++;
						} else {	//remove the first apostrophe and search for the next:
							question += " " + answerQuery[index].substring(1, answerQuery[index].length());
							index++;
							while(!answerQuery[index].substring(answerQuery[index].length()-1, answerQuery[index].length()).equals("'")) {
								question += " " + answerQuery[index++];
							}
							// remove last apostrophe and add it
							question += " " + answerQuery[index].substring(0, answerQuery[index].length()-1);
							index++;
						}
						
					} else {
						question += " " + answerQuery[index++];
					}
				} else if (answerQuery[index].equalsIgnoreCase("IN")) {
					index++;
					question += " would appear in the answer to following sub-question:\n";
					index++;	// the first thing after IN should be the parenthesis, so we skip it
					String subQuery = answerQuery[index++];
					for ( ; index < answerQuery.length && !answerQuery[index].equals(")"); index++) {
						subQuery += " " + answerQuery[index];
					}	
					Question subQuestion = new Question(subQuery);
					question += " " + subQuestion.getQuestion();
					question = question.substring(0, question.length()-1);	// get rid of the extra period, it will be added after the loop
				}
			} while (index < answerQuery.length && answerQuery[index].equalsIgnoreCase("AND"));
		}
		question += ".";
		// Here I remove any underscores. Sometimes they could be valuable, for instance during a constraint like
		// "WHERE code = '123_456'. However, this is rare and this is a temporary solution only.
		question = question.replaceAll("_", " ");
		return question;
	}
	
	public String joinQueryToEnglish(String[] answerQuery) {
		/*
		 * 
		 * HANDLES "SELECT"
		 * 
		 */
		Random random = new Random();
		String[] selectVerbs = { "List the", "Retrieve the", "Output the" };
		question += selectVerbs[random.nextInt(3)];
		int index = 1;
		if(answerQuery[index].equalsIgnoreCase("ALL")) {
			index++;
		} else if (answerQuery[index].equalsIgnoreCase("DISTINCT")) {
			question += " distinct";
			index++;
		} else if (answerQuery[index].equals("*")) {	
			question += " attributes";
			index++;
		}
		// find the number of attributes to be returned by the select statement
		ArrayList<String> attributeList = new ArrayList<String>();
		boolean indexPointsToFrom = false;
		for( ; indexPointsToFrom != true; index++) {
			if(answerQuery[index].equalsIgnoreCase("FROM")) {
				indexPointsToFrom = true;
			} else {
				attributeList.add(answerQuery[index]);
			}
		}
		// An example of what the following structure looks like for query "SELECT employee.first_name, 
		// employee.last_name, department.id"
		// employee -> first_name -> last_name
		// department -> id
		if (attributeList.size() == 0) {
			// this should occur when we have "*"
			// NOT YET IMPLEMENTED.
		} else {
			LinkedList<LinkedList<String>> mainList = new LinkedList<LinkedList<String>>();
			ArrayList<String> mainListKey = new ArrayList<String>();
			for(int i = 0; i < attributeList.size(); i++){
				String[] split = (attributeList.get(i)).split("\\.");
				if(!mainListKey.contains(split[0])) {
					mainListKey.add(split[0]);
					LinkedList<String> subList = new LinkedList<String>();
					subList.add(split[1]);
					mainList.add(subList);
				} else {
					mainList.get(mainListKey.indexOf(split[0])).add(split[1]);
				}
			}
	
			for(int i = 0; i < mainList.size(); i++) {
				if (i > 0 && i != mainList.size() - 1) {
					question += ",";
				} else if (i > 0 && i == mainList.size() - 1) {
					question += " and";
				}
				switch(mainList.get(i).size()) {	
					case 1:
						if(mainList.get(i).get(0).substring(mainList.get(i).get(0).length()-1, mainList.get(i).get(0).length()).equals(",")) {
							question += " " + mainList.get(i).get(0).substring(0, mainList.get(i).get(0).length()-1).toLowerCase() + "s";
						} else {
							question += " " + mainList.get(i).get(0).toLowerCase() + "s";
						}
						break;
					// If there are only two attributes then the comma which separates the two isn't required.
					case 2:
						question += " " + mainList.get(i).get(0).substring(0, mainList.get(i).get(0).length()-1).toLowerCase() + "s";
						if(mainList.get(i).get(1).substring(mainList.get(i).get(1).length()-1, mainList.get(i).get(1).length()).equals(",")) {
							question += " and " + mainList.get(i).get(1).substring(mainList.get(i).get(1).length()-1, mainList.get(i).get(1).length()).toLowerCase() + "s";
						} else {
							question += " and " + mainList.get(i).get(1).toLowerCase() + "s";
						}
						break;
					// If there is more than two, then the commas are required.
					default:
						question += " " + mainList.get(i).get(0).toLowerCase();		
						int j = 1;
						for( ; j < mainList.get(i).size() - 1; j++) {
							question += " " + mainList.get(i).get(j).toLowerCase();			
						}
						question += " and " + mainList.get(i).get(j).toLowerCase() + "s";
				}
				question += " of all " + mainListKey.get(i).toLowerCase() + "s";
			}
		}
		/*
		 * 
		 * HANDLES "WHERE"
		 * 
		 */
		boolean constraintsExist = false;
		while(index < answerQuery.length) {
			if (answerQuery[index].equalsIgnoreCase("WHERE")) {
				constraintsExist = true;
				break;
			} 
			index++;
		} //index will point either past the query or at WHERE when the loop breaks.
		if (constraintsExist == true) {	// constraints exist
			do {
				if (answerQuery[index++].equalsIgnoreCase("AND")) {
					question += " and";
				}
				String[] split = answerQuery[index++].split("\\.");
				question += " whose " + split[0].toLowerCase() + " " + split[1].toLowerCase();
				if (answerQuery[index].equals("=") || answerQuery[index].equals(">") || answerQuery[index].equals("<") || 
						answerQuery[index].equals("<>") || answerQuery[index].equals(">=") || answerQuery[index].equals("<=")) {
					question += " is";
					if (answerQuery[index].equals(">")) {			// Switch statement in Java 1.7
						question += " greater than";
					} else if (answerQuery[index].equals("<")) {
						question += " less than";
					} else if (answerQuery[index].equals("<>")) {
						question += " not";
					} else if (answerQuery[index].equals(">=")) {
						question += " greater than or is";
					} else if (answerQuery[index].equals("<=")) {
						question += " less than or is";
					}
					index++;
					// check for constraining values enclosed in apostrophes.
					if (answerQuery[index].substring(0, 1).equals("'")) {	
						// if it was a single word surrounded by apostrophes then we simply remove the apostrophes and add it.
						if(answerQuery[index].substring(answerQuery[index].length()-1, answerQuery[index].length()).equals("'")) {
							question += " " + answerQuery[index].substring(1, answerQuery[index].length()-1);
							index++;
						} else {	//remove the first apostrophe and search for the next:
							question += " " + answerQuery[index].substring(1, answerQuery[index].length());
							index++;
							while(!answerQuery[index].substring(answerQuery[index].length()-1, answerQuery[index].length()).equals("'")) {
								question += " " + answerQuery[index++];
							}
							// remove last apostrophe and add it
							question += " " + answerQuery[index].substring(0, answerQuery[index].length()-1);
							index++;
						}
						
					} else {
						question += " " + answerQuery[index++];
					}
				} else if (answerQuery[index].equalsIgnoreCase("IN")) {
					index++;
					question += " would appear in the answer to following sub-question:\n";
					index++;	// the first thing after IN should be the parenthesis, so we skip it
					String subQuery = answerQuery[index++];
					for ( ; index < answerQuery.length && !answerQuery[index].equals(")"); index++) {
						subQuery += " " + answerQuery[index];
					}	
					Question subQuestion = new Question(subQuery);
					question += " " + subQuestion.getQuestion();
					question = question.substring(0, question.length()-1);	// get rid of the extra period, it will be added after the loop
				}
			} while (index < answerQuery.length && answerQuery[index].equalsIgnoreCase("AND"));
		}
		question += ".";
		// Here I remove any underscores. Sometimes they could be valuable, for instance during a constraint like
		// "WHERE code = '123_456'. However, this is rare and this is a temporary solution only.
		question = question.replaceAll("_", " ");
		return question;
	}
	
	public void questionSpellCheck() {
		// add all dictionary keys to a list
		Iterator phraseKeyIterator = spellcheckDictionary.keySet().iterator();
		ArrayList<String> phraseKeyList = new ArrayList<String>();
		while (phraseKeyIterator.hasNext()) {
			phraseKeyList.add((String)phraseKeyIterator.next());
		}
		// check if the question contains any of these keys
		for(int i = 0; i < phraseKeyList.size(); i++) {
			if(question.contains(phraseKeyList.get(i))) {
				question = question.replaceAll(phraseKeyList.get(i), spellcheckDictionary.get(phraseKeyList.get(i)));
			}

		}
	}
	
	public void questionPhraseCheck() {
		// add all dictionary keys to a list
		Iterator phraseKeyIterator = phrasecheckDictionary.keySet().iterator();
		ArrayList<String> phraseKeyList = new ArrayList<String>();
		while (phraseKeyIterator.hasNext()) {
			phraseKeyList.add((String)phraseKeyIterator.next());
		}
		// check if the question contains any of these keys
		for(int i = 0; i < phraseKeyList.size(); i++) {
			if(question.contains(phraseKeyList.get(i))) {
				question = question.replaceAll(phraseKeyList.get(i), phrasecheckDictionary.get(phraseKeyList.get(i)));
			}

		}
	}
	
	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
}
