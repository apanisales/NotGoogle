import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeSet;


/**
 * Helper class for performing searches on an inverted index
 * 
 * @author Anthony Panisales
 */
public class QueryHelper {

	/**
	 * Holds the data required in a search result
	 * 
	 * @author Anthony Panisales
	 */
	public class SearchResult implements Comparable<SearchResult> {
		
		private String where;
		private int count;
		private int firstPos;

		/**
		 * Initializes the search result.
		 * 
		 * @param w
		 *         filepath where occurrences are located
		 */
		public SearchResult(String w) {
			where = w;
			count = 0;
	 		firstPos = Integer.MAX_VALUE;
		}

		/**
		 * Returns the filepath where occurences are located
		 * 
		 * @return path where occurrences are located as String
		 */
		public String getWhere() {
			return where;
		}

		/**
		 * Returns the number of occurrences
		 *
		 * @return number of occurrences
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Returns the position of first occurrence
		 *
		 * @return position of first occurrence
		 */
		public int getFirstPos() {
			return firstPos;
		}

		/**
		 * Sets the number of occurrences
		 * 
		 * @param newC
		 *           new number of occurrences
		 */
		public void setCount(int newC) {
			count = newC;
		}

		/**
		 * Sets the position of first occurrence
		 * 
		 * @param newfp 
		 *            new position of first occurrence
		 */
		public void setFirstPos(int newfp) {
			firstPos = newfp;
		}

		/**
		 * Contains the criteria to sort search results. Locations 
		 * where the query word(s) are more frequent should be ranked 
		 * above others. For locations that have the same frequency of 
		 * query word(s), locations where the words appear in earlier 
		 * positions should be ranked above others. For locations that 
		 * have the same frequency and position, the results should be 
		 * sorted by path in case-insensitive order.
		 * 
		 * @param other
		 *            the search result to be compared to
		 * @return int indicating whether the two search results are equal
		 *         or if they are not, which one is greater than the other
		 */
		@Override 
		public int compareTo(SearchResult other) {
			if (this.count < other.getCount()) {
				return 1;
			} else if (this.count > other.getCount()) {
				return -1;
			}
			
			if (this.firstPos < other.getFirstPos()) {
				return -1;
			} else if (this.firstPos > other.getFirstPos()) {
				return 1;
			}
			
			return this.where.toLowerCase().compareTo(other.getWhere().toLowerCase());
		}
	}
	
	/**
	 * Writes the content and results of search queries as a nested JSON object using 
	 * a "pretty" format with tab characters for indentation to a file.
	 * 
	 * @author Anthony Panisales
	 *
	 */
	private class QueryResultsJSONWriter {

		/**
		 * Returns a String with the specified number of tab characters.
		 *
		 * @param times
		 *            number of tab characters to include
		 * @return tab characters repeated the specified number of times
		 */
		public String indent(int times) {
			char[] tabs = new char[times];
			Arrays.fill(tabs, '\t');
			return String.valueOf(tabs);
		}

		/**
		 * Returns a quoted version of the provided text.
		 *
		 * @param text
		 *            text to surround in quotes
		 * @return text surrounded by quotes
		 */
		public String quote(String text) {
			return String.format("\"%s\"", text);
		}

		/**
		 * Writes the list of results as a JSON array at the specified indent level.
		 *
		 * @param writer
		 *            writer to use for output
		 * @param results
		 *            list of search results to write as JSON array
		 * @param level
		 *            number of times to indent the array itself
		 * @throws IOException
		 */
		private void asArray(Writer writer, List<SearchResult> results, int level) throws IOException {
			writer.write("[\n");
			int index = 0;
			for (SearchResult result : results) {
				writer.write(indent(level+1) + "{\n");
				writer.write(indent(level+2) + quote("where") + ": " + quote(result.getWhere().toString()) + ",\n");
				writer.write(indent(level+2) + quote("count") + ": " + result.getCount() + ",\n");
				writer.write(indent(level+2) + quote("index") + ": " + result.getFirstPos() + "\n");
				if (index != results.size()-1) {
					writer.write(indent(level+1) + "},\n");
				} else {
					writer.write(indent(level+1) + "}\n");
				}
				index++;
			}
			writer.write(indent(level) + "]\n");
		}

		/**
		 * Writes search queries and their results as a JSON object to the path using UTF8.
		 *
		 * @param queryResults
		 *            HashMap containing search queries and their results to write as a JSON object
		 * @param path
		 *            path to write file
		 * @throws IOException
		 */
		public void asObject(HashMap<String[], List<SearchResult>> queryResults, Path path) throws IOException {
			int index = 0;
			TreeSet<String[]> queries = new TreeSet<String[]>(new Comparator<String[]>() {
				@Override
				public int compare(String[] a, String[] b) {
					String aString = Arrays.toString(a);
					String bString = Arrays.toString(b);
					aString = aString.substring(1, aString.length()-1).replaceAll(",","");
					bString = bString.substring(1, bString.length()-1).replaceAll(",","");
					if (a.length < b.length) {
						if (aString.equals(bString.substring(0, a.length))) {
							return -1;
						}
					} else if (a.length > b.length) {
						if (bString.equals(aString.substring(0, b.length))) {
							return 1;
						}
					}
					return aString.compareTo(bString);
				}
			});
			
			try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);) {
				writer.write("[\n");
				for (String[] query : queryResults.keySet()) {
					Arrays.sort(query);
					queries.add(query);
				}
				for (String[] query : queries) {
					String queryString = Arrays.toString(query);
					queryString = queryString.substring(1, queryString.length()-1).replaceAll(",","");
					if (queryString.equals("")) {
						index++;
						continue;
					}
					writer.write(indent(1) + "{\n");
					writer.write(indent(2) + quote("queries") + ": " + quote(queryString) + ",\n");
					writer.write(indent(2) + quote("results") + ": ");
					asArray(writer, queryResults.get(query), 2);
					if (index != queries.size()-1) {
						writer.write(indent(1) + "},\n");
					} else {
						writer.write(indent(1) + "}\n");
					}
					index++;
				}
				writer.write("]");
				writer.flush();
			}
		}
	}
	
	private List<String[]> queries;
	private HashMap<String[],List<SearchResult>> queryResults;
	
	public QueryHelper() {
		queries = new ArrayList<String[]>();
		queryResults = new HashMap<String[],List<SearchResult>>();
	}
	
	/**
	 * Parses the search query file line by line
	 * 
	 * @param searchInput
	 *                 path of search query file
	 * @throws FileNotFoundException
	 */
	public void parseFile(Path searchInput) throws FileNotFoundException {
		File queryFile = new File(searchInput.toString());
		Scanner filescan = new Scanner(queryFile);
		while (filescan.hasNextLine()) {
			queries.add(WordParser.parseWords(filescan.nextLine()));
		}
		filescan.close();
	}
	
	/**
	 * Returns exact search results from the inverted index, such that any 
	 * word in the inverted index that exactly matches a query word is taken
	 * into account.
	 * 
	 * @param index
	 *            the inverted index to perform a exact search on
	 * @throws InterruptedException 
	 */
	public void exactSearch(InvertedIndex index) throws InterruptedException {
		for (String[] query : queries) {
			queryResults.put(query, index.exactSearch(query));
		}
	}
	
	/**
	 * Returns partial search results from the inverted index, such that any 
	 * word in the inverted index that starts with a query word is taken into
	 * account.
	 * 
	 * @param index
	 *            the inverted index to perform a partial search on
	 * @throws InterruptedException 
	 */
	public void partialSearch(InvertedIndex index) throws InterruptedException {
		for (String[] query : queries) {
			queryResults.put(query, index.partialSearch(query));
		}
	}
	
	/**
	 * Writes the content and results of search queries as a nested JSON object using 
	 * a "pretty" format with tab characters for indentation to a file.
	 * 
	 * @param searchOutput
	 *              path to write the results to
	 */
	public void resultsAsObject(Path searchOutput) throws IOException {
		Files.createFile(searchOutput);
		new QueryResultsJSONWriter().asObject(queryResults, searchOutput);
	}
	
	
	/**
	 * Returns a map containing mappings of a search query and its results.
	 * 
	 * @return HashMap of search queries and their results
	 */
	public HashMap<String[],List<SearchResult>> getQueryResults() {
		return queryResults;
	}
	
	/**
	 * Fills a work queue with searching tasks to execute.
	 * 
	 * @param queue
	 *            work queue to fill
	 * @param exact
	 *            indicates whether to perform an exact search or not
	 * @param queryResults
	 *            a map that is going to be filled by the threads and will contain
	 *            mappings of a search query and its results
	 * @param index
	 *            an inverted index that is going to be searched through by the threads
	 */
	public void threadsSearch(WorkQueue queue, boolean exact, 
			HashMap<String[],List<SearchResult>> queryResults, InvertedIndex index) {
		for (String[] query : queries) {
			queue.execute(new QueryTask(query, exact, queryResults, index));
		}
	}
	
	/**
	 * Runnable task that performs an exact or partial search on an inverted index 
	 * using a single, multiple word query.
	 */
	private static class QueryTask implements Runnable {
		private final String[] query;
		private final boolean exact;
		private final HashMap<String[],List<SearchResult>> localQueryResults;
		private final HashMap<String[],List<SearchResult>> globalQueryResults;
		private final InvertedIndex index;
		
		public QueryTask(String[] query, boolean exact, 
				HashMap<String[],List<SearchResult>> globalQueryResults, InvertedIndex index) {
			this.query = query;
			this.exact = exact;
			this.localQueryResults = new HashMap<String[],List<SearchResult>>();
			this.globalQueryResults = globalQueryResults;
			this.index = index;
		}

		@Override
		public void run() {
			try {
				if (exact) {
					localQueryResults.put(query, index.exactSearch(query));
				} else {
					localQueryResults.put(query, index.partialSearch(query));
				}
					
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			synchronized(globalQueryResults) {
				for (Entry<String[],List<SearchResult>> entry : localQueryResults.entrySet()) {
					globalQueryResults.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
}
