package framework.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import framework.reflection.Utilitaire;

public class FrontControllerServlet extends HttpServlet {

    String packageName;
    private List<String> controllerNames;
//    public void init() throws ServletException {
//        try {
//            packageName = this.getInitParameter("packageName");
//            this.controllerNames = Utilitaire.getClassNamesWithAnnotation("controller", framework.annotation.Controller.class);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void processRequest(HttpServletRequest request,
                               HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        @SuppressWarnings("unchecked")
        List<String> controllerNames =
                (List<String>) getServletContext().getAttribute("controllers");

        if (controllerNames == null) {
            throw new ServletException("Aucun contrôleur n'a été chargé.");
        }

        try (PrintWriter out = response.getWriter()) {

            out.println("<p>" + request.getRequestURI() + "</p>");

            out.println("<p>Liste des controllers :</p>");

            for (String controllerName : controllerNames) {
                out.println("<p>" + controllerName + "</p>");
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}