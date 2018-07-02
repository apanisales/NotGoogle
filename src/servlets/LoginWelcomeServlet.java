import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handles display of user information.
 *
 * @see LoginServer
 */
@SuppressWarnings("serial")
public class LoginWelcomeServlet extends LoginBaseServlet {
	
	private HashMap<String, String> lastLoginTimeDB = new HashMap<String, String>();
	private TreeMap<String, String> lastFiveUsers = new TreeMap<String, String>();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String user = getUsername(request);
				
		if (user != null) {
			prepareResponse("Welcome", response);

			PrintWriter out = response.getWriter();
			out.println("<p>Hi " + user + "!</p>");
			
			HttpSession session = request.getSession();
			
			// Last Login Time (5 points)
			if (lastLoginTimeDB.get(getUsername(request)) != null) {
				out.printf("<p> Last login time: %s </p>%n", lastLoginTimeDB.get(getUsername(request)));
			}
			
			if (session.getAttribute("justEntered") == null || !session.getAttribute("justEntered").equals("No")) {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");  
				LocalDateTime now = LocalDateTime.now(); 
				lastLoginTimeDB.put(getUsername(request), dtf.format(now));
				lastFiveUsers.put(dtf.format(now), getUsername(request));
			}
			
			session.setAttribute("justEntered", "No");
			
			out.printf("<form action='http://localhost:%d/search'>"
					+ "<input type='submit' value='Search' />"
					+ "</form>", Driver.staticPort);
			
			out.printf("<br>");
			
			// Logged In Users (5 points)
			out.printf("<form>"
					+ "<button name='seeUsers' type='submit' value='Show'>See Last Five Users</button>"
					+ "<button id='closeUsers' style='display:none;' name='seeUsers' type='submit' value='Close'>Close</button>"
					+ "</form>");
			if (request.getParameter("seeUsers") != null && request.getParameter("seeUsers").equals("Show")) {
				if (lastFiveUsers != null && lastFiveUsers.size() != 0) {
					int i = 1;
					for (String date : lastFiveUsers.keySet()) {
						if (i == lastFiveUsers.size()) {
							break;
						}
						out.printf("<p> %d. %s %s </p>%n", i++, date, lastFiveUsers.get(date));
					}
				}
				out.printf("<script>");
				out.printf("var closeButton = document.getElementById('closeUsers');");
				out.printf("closeButton.style.display = 'initial';");
				out.printf("closeButton.style.visibility = 'visible';%n");
				out.printf("</script>");
				if (lastFiveUsers.size() > 5) {
					lastFiveUsers.pollFirstEntry();
				}
			}
			
			out.printf("<br>");
			
			out.printf("<form action='http://localhost:%d/login'>"
					+ "<input name ='logout' type='submit' value='Logout' />"
					+ "</form>", Driver.staticPort);
			
			finishResponse(response);
		}
		else {
			response.sendRedirect("/login");
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}
}