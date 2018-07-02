import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Processes all HTML files in a directory and its subdirectories, or 
 * HTML from a URL, cleans and parses the HTML into words, and builds an 
 * inverted index to store the mapping from words to the documents and 
 * positions within those documents where those words were found. Is also 
 * able to parse a query file, generate a sorted list of search results from 
 * the inverted index, and support writing those results to a JSON file. 
 * Supports single-threading and multithreading. Able to generate a
 * search engine web application.
 * 
 * @author Anthony Panisales
 * 
 */
public class Driver {
	
	public static InvertedIndex staticIndex = null;
	public static int staticThreads = 1;
	public static int staticLimit = 50;
	public static int staticPort = 8080;

	public static void main(String[] args) throws Exception {
		ArgumentMap argMap = new ArgumentMap(args);
		InvertedIndex index = new InvertedIndex();
		QueryHelper queryHelper = new QueryHelper();
		int numOfThreads = 1;
		int limit = 50;
		
		if (argMap.hasFlag("-threads")) {
			numOfThreads = argMap.getInteger("-threads", 5);
			if (numOfThreads <= 0) {
				numOfThreads = 5;
			}
		}
		
		// Creates an inverted index using a URLs and a web crawler
		if (argMap.hasFlag("-url")) {
			URL seed;
			WebCrawler crawler;
			
			if (argMap.hasFlag("-limit")) {
				limit = argMap.getInteger("-limit", 50);
				if (limit <= 0) {
					limit = 50;
				}
			}
			
			crawler = new WebCrawler(limit);
			
 			if (argMap.hasValue("-url")) {
				seed = new URL(argMap.getString("-url"));
			} else {
				// No URL seed argument
				return;
			}
 			
 			seed = LinkParser.clean(seed);
 			
 			crawler.threadsBuildIndex(seed, numOfThreads);
 			
 			index = crawler.getIndex();
		}
		
		// Creates an inverted index using paths
		if (argMap.hasFlag("-path")) {
			Path indexInput;
			
			if (argMap.hasValue("-path")) {
				indexInput = Paths.get(argMap.getString("-path"));
				indexInput = indexInput.normalize();
			} else {
				// No path name argument
				return;
			}
			
			// File or directory entered for -path does not exist
			if (Files.notExists(indexInput)) {
				return;
			}
			
			if (argMap.hasFlag("-threads")) {
				// Uses threads to build the inverted index
				WorkQueue queue = new WorkQueue(numOfThreads);
				InvertedIndex.threadsBuildIndex(indexInput, index, queue);
				queue.finish();
				queue.shutdown();
			} else {
				if (Files.isDirectory(indexInput)) {
					index.checkDirectory(indexInput);
				} else {
					index.checkAndParseFile(indexInput);
				}
			}
		}
		
		// Outputs the contents of the inverted index to a file
		if (argMap.hasFlag("-index")) {
			Path indexOutput = Paths.get(argMap.getString("-index", "index.json"));
			indexOutput = indexOutput.normalize();
			index.asObject(indexOutput);
		}
		
		// Performs search queries on the inverted index
		if (argMap.hasFlag("-query")) {
			Path searchInput;
			
			if (argMap.hasValue("-query")) {
				searchInput = Paths.get(argMap.getString("-query"));
				searchInput = searchInput.normalize();
			} else {
				return;
			}
			
			// File entered for -query does not exist
			if (Files.notExists(searchInput)) {
				return;
			}
			
			queryHelper.parseFile(searchInput);
			if (argMap.hasFlag("-threads")) {
				// Uses threads to perform searches
				WorkQueue queue = new WorkQueue(numOfThreads);
				boolean exact = argMap.hasFlag("-exact");
				queryHelper.threadsSearch(queue, exact, queryHelper.getQueryResults(), index);
				queue.finish();
				queue.shutdown();
			} else {
				if (argMap.hasFlag("-exact")) {
					queryHelper.exactSearch(index);
				} else {
					queryHelper.partialSearch(index);
				}
			}
		}
		
		// Outputs the results of search queries on the inverted index to a file
		if (argMap.hasFlag("-results")) {
			Path searchOutput = Paths.get(argMap.getString("-results", "results.json"));
			searchOutput = searchOutput.normalize();
			queryHelper.resultsAsObject(searchOutput);
		}
		
		// Search Engine
		if (argMap.hasFlag("-port")) {
			staticIndex = index;
			staticThreads = numOfThreads;
			staticLimit = limit;
			int port = argMap.getInteger("-port", 8080);
			
			if (port < 0 || port > 65535) {
				port = 8080;
			}
			
			staticPort = port;

			// turn on sessions and set context
			ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
			servletContext.setContextPath("/");
			servletContext.addServlet(SearchServlet.class, "/search");
			servletContext.addServlet(LoginUserServlet.class, "/login");
			servletContext.addServlet(LoginRegisterServlet.class, "/register");
			servletContext.addServlet(LoginWelcomeServlet.class, "/welcome");
			servletContext.addServlet(LoginRedirectServlet.class, "/*");
			
			Server server = new Server(port);
			server.setHandler(servletContext);
			server.start();
			server.join();
		}
	}
}