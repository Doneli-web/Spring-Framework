package framework.servlet;

import framework.exception.UrlNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;

import framework.reflection.Utilitaire;

import static framework.reflection.Utilitaire.getMethodByUrl;

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
            throws Exception {

        response.setContentType("text/html;charset=UTF-8");

        @SuppressWarnings("unchecked")
        List<Class<?>> controllers =
                (List<Class<?>>) getServletContext().getAttribute("controllers");

        if (controllers == null) {
            throw new ServletException("Aucun contrôleur n'a été chargé.");
        }

        String contextPath = request.getContextPath();
        String url = request.getRequestURI().substring(contextPath.length());

        Method method = getMethodByUrl(url, controllers);

        try (PrintWriter out = response.getWriter()) {

            out.println("<p>" + request.getRequestURI() + "</p>");

//            out.println("<p>Liste des controllers :</p>");
//
//            for (Class<?> controller : controllers) {
//                out.println("<p>" + controller.getSimpleName() + "</p>");
//            }
            out.println("<p>Methode associe :</p>");
            out.println("<p>" + method.getName() + "</p>");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (UrlNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (UrlNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}