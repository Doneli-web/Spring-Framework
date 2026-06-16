package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;

public class FrontControllerServlet extends HttpServlet {

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String url = request.getRequestURI();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<h1>URL demandée</h1>");
        out.println("<p>" + url + "</p>");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}