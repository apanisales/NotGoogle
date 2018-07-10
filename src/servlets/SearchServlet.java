import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handles search engine.
 * 
 * @author Anthony Panisales
 */
@SuppressWarnings("serial")
public class SearchServlet extends LoginBaseServlet {

	private static final String TITLE = "Search Engine";
	private HashMap<String, TreeMap<String, String>> historyDB = new HashMap<String, TreeMap<String, String>>();
	private HashMap<String, InvertedIndex> indexDB = new HashMap<String, InvertedIndex>();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		if (getUsername(request) == null) {
			response.sendRedirect("/login");
			return;
		}
		
		if (!indexDB.containsKey(getUsername(request))) {
			InvertedIndex tempIndex = Driver.staticIndex;
			InvertedIndex newIndex = new InvertedIndex();
			try {
				newIndex.addIndextoIndex(tempIndex);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			indexDB.put(getUsername(request), newIndex);
		}
		
		InvertedIndex mainIndex = indexDB.get(getUsername(request));
		
		HttpSession session = request.getSession();
		session.setAttribute("justEntered", "No");
		
		List<QueryHelper.SearchResult> searchResults = new ArrayList<QueryHelper.SearchResult>();

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter out = response.getWriter();
		out.printf("<!DOCTYPE html>%n");
		out.printf("<html lang=\"en\">%n");
		
		// Search Brand
		out.printf("<head>");
		out.printf("<title>%s</title>", TITLE);
		out.printf("<style> "
				+ "body { text-align: center; color: rgb(0, 149, 255); font-style: italic;}"
				+ "img { width:240px; height:240px }"
				+ ".switch { position: relative; display: inline-block; width: 60px; height: 34px; }"
				+ ".switch input {display:none;}"
				+ ".slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0;"
				+ "background-color: #ccc; -webkit-transition: .4s; transition: .4s; }"
				+ ".slider:before { position: absolute; content: ''; height: 26px; width: 26px; left: 4px;"
				+ "bottom: 4px; background-color: white; -webkit-transition: .4s; transition: .4s; }"
				+ "input:checked + .slider { background-color: #2196F3;}"
				+ "input:focus + .slider { box-shadow: 0 0 1px #2196F3; }"
				+ "input:checked + .slider:before { -webkit-transform: translateX(26px); -ms-transform: translateX(26px);"
				+ "transform: translateX(26px);}"
				+ ".slider.round { border-radius: 34px; }"
				+ ".slider.round:before { border-radius: 50vw;}"
				+ "</style>%n");
		out.printf("</head>%n");
		
		out.printf("<body>%n");
		
		out.printf("<img id='butter' src='http://www.ferwerda.nl/wp-content/uploads/2013/12/Butter.jpg'>");
		out.printf("<h1><em>I can't believe it's not <strong>Google!</strong></em></h1>%n");
		
		out.printf("<form> ");
		if (request.getParameter("query") != null) {
			out.printf("Search Query: <input placeholder='Search' type='text' name='query' value='%s'>", request.getParameter("query"));
		} else {
			out.printf("Search Query: <input placeholder='Search' type='text' name='query'>");
		}
		out.printf("<br>");
		out.printf("<br>");
		out.printf("Additional Seed URL (optional): <input placeholder='Additional Seed URL' type='text' name='seed'>");
		out.printf("<br>");
		out.printf("<br>");
		out.printf("<input type='Submit' value='Submit'>");
		out.printf("<button name='history' type='submit' value='Show'>Show History</button>");
		out.printf("<button name='history' type='submit' value='Clear'>Clear History</button>");
		out.printf("<br>");
		
		// Partial Search Toggle
		out.printf("<p>Partial search ON or OFF: </p>%n");
		out.printf("<label class='switch' for='partialSearchToggle'>");
		if (request.getParameter("partialSearch") != null && request.getParameter("partialSearch").equals("ON")) {
			out.printf("<input id='partialSearchToggle' name='partialSearch' type='checkbox' value='ON' checked>");
		} else {
			out.printf("<input id='partialSearchToggle' name='partialSearch' type='checkbox' value='ON'>");
		}
		out.printf("<span class='slider round'></span> </label>%n");
		out.printf("<br>");
		
		// Private Search Options
		out.printf("<p>Private search ON or OFF:</p>%n");
		out.printf("<select name='privateSearch'>");
		
		if (request.getParameter("privateSearch") != null && request.getParameter("privateSearch").equals("OFF")) {
			out.printf("<option value='OFF' selected>Off</option>");
		} else {
			out.printf("<option value='OFF'>Off</option>");
		}
		
		if (request.getParameter("privateSearch") != null && request.getParameter("privateSearch").equals("ON")) {
			out.printf("<option value='ON' selected>On</option>");
		} else {
			out.printf("<option value='ON'>On</option>");
		}
		
		out.printf("</select>");
		out.printf("</form>%n");
		out.printf("<br>");
		
		out.printf("<form action='http://localhost:%d/'>"
				+ "<input type='submit' value='Back to Home' />"
				+ "</form>", Driver.staticPort);
		
		out.printf("<div id='historyDiv'>  </div>%n");
		
		// Search History
		if (request.getParameter("history") != null && request.getParameter("history").equals("Show")) {
			out.printf("<script> var div = document.getElementById('historyDiv');%n");
			out.printf("div.style.display = 'initial';");
			out.printf("div.style.visibility = 'visible';%n");
			TreeMap<String,String> userHistory = historyDB.get(getUsername(request));
			if (userHistory != null && userHistory.size() != 0) {
				out.printf("div.innerHTML += '<br>';%n");
				int i = 1;
				for (String date : userHistory.keySet()) {
					out.printf("div.innerHTML += '%d. %s %s <br><br> ';%n", i++, date, userHistory.get(date));
				}
			}
			out.printf("</script>");
		} else if (request.getParameter("history") != null && request.getParameter("history").equals("Clear")) {
			historyDB.put(getUsername(request), new TreeMap<String,String>());
		} else if (request.getParameter("seed") != null && request.getParameter("seed").length() != 0) {
			
			// New Crawl
			try {
				WebCrawler crawler = new WebCrawler(Driver.staticLimit);
				crawler.threadsBuildIndex(new URL(request.getParameter("seed")), Driver.staticThreads);
				InvertedIndex newIndex = crawler.getIndex();
				mainIndex.addIndextoIndex(newIndex);
				out.printf("<p> Database has been updated with new seed: %s </p>%n", request.getParameter("seed"));
				indexDB.put(getUsername(request), mainIndex);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				out.printf("<p> Invalid seed URL </p>%n");
			}
			
		} else if (request.getParameter("query") != null && request.getParameter("query").length() != 0) {
			
			// Time Stamps
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");  
			LocalDateTime now = LocalDateTime.now(); 
		    
		    if (request.getParameter("privateSearch") != null && request.getParameter("privateSearch").equals("OFF")) {
		    	if (!historyDB.containsKey(getUsername(request))) {
	    			historyDB.put(getUsername(request), new TreeMap<String, String>());
	    		}
		    	TreeMap<String,String> userHistory = historyDB.get(getUsername(request));
		    	if (request.getParameter("partialSearch") != null && request.getParameter("partialSearch").equals("ON")) {
		    		userHistory.put(dtf.format(now).concat(" [partial]: "), request.getParameter("query"));
		    	} else {
		    		userHistory.put(dtf.format(now).concat(" [exact]: "), request.getParameter("query"));
		    	}
		    	historyDB.put(getUsername(request), userHistory);
		    	out.printf("<p> Size of %s's history: %d </p>%n", getUsername(request), userHistory.size());
		    }
		    
			String[] words = WordParser.parseWords(request.getParameter("query"));
			try {
				
				// Search Statistics
				double startTime = System.nanoTime();
				if (request.getParameter("partialSearch") != null && request.getParameter("partialSearch").equals("ON")) {
					searchResults = mainIndex.partialSearch(words);
				} else {
					searchResults = mainIndex.exactSearch(words);
				}
				double endTime = System.nanoTime();
				double totalTime = (endTime - startTime) * 0.000000001;
				
				out.printf("<p> Total number of results: %d </p>%n", searchResults.size());
				out.printf("<p> Time spent fetching search results: %f seconds</p>%n", totalTime);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// List search results
			int i = 1;
			for (QueryHelper.SearchResult result : searchResults) {
				out.printf("<p> %d. <a href='%s'>%s</a> </p>%n", i++, result.getWhere(), result.getWhere());
			}
		}
		
		out.printf("</body>%n");
		out.printf("</html>%n");
		out.flush();

		response.flushBuffer();
	}
}
