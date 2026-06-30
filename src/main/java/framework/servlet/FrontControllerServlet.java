package framework.servlet;

import framework.exception.UrlNotFoundException;
import framework.route.RouteMapping;
import framework.route.UrlMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

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

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws UrlNotFoundException, ServletException {

        response.setContentType("text/html;charset=UTF-8");

        String contextPath = request.getContextPath();
        String url = request.getRequestURI().substring(contextPath.length());
        UrlMethod urlMethod = new UrlMethod(url, request.getMethod());

        @SuppressWarnings("unchecked")
        Map<UrlMethod, RouteMapping> urlsMethodes =
                (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("urlsMethodes");

        // Résolution AVANT d'ouvrir le writer
        RouteMapping route = Utilitaire.getByUrlAndMethode(urlMethod, urlsMethodes);

        try (PrintWriter out = response.getWriter()) {
            out.println("<h1> Url : " + request.getRequestURI() + "</h1>");
            out.println("<h3>Méthode associée : <span style='font-weight:normal;'>"
                    + route.getMethod().getName() + "</span></h3>");
            out.println("<h4>Methode(Post ou get) : <span style='font-weight:normal;'>"
                    + urlMethod.getMethode() + "</span></h4>");
            out.println("<h3>Class associe : <span style='font-weight:normal;'>"
                    + route.getClazz().getSimpleName() + "</span></h3>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (UrlNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (UrlNotFoundException e) {
                throw new RuntimeException(e);
        }
    }
}