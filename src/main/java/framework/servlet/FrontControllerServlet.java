package framework.servlet;

import framework.exception.UrlNotFoundException;
import framework.route.RouteMapping;
import framework.route.UrlMethod;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import framework.reflection.Utilitaire;

public class FrontControllerServlet extends HttpServlet {

    String packageName;
    String prefixe;
    String suffixe;
    private List<String> controllerNames;
    private Map<UrlMethod, RouteMapping> urlsMethodes;
    public void init() throws ServletException {
        try {
            this.urlsMethodes = (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("urlsMethodes");
            this.prefixe = (String) getServletContext().getAttribute("prefixe");
            this.suffixe = (String) getServletContext().getAttribute("suffixe");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws UrlNotFoundException, ServletException, IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {

        response.setContentType("text/html;charset=UTF-8");

        String contextPath = request.getContextPath();
        String url = request.getRequestURI().substring(contextPath.length());
        UrlMethod urlMethod = new UrlMethod(url, request.getMethod());

        // Résolution AVANT d'ouvrir le writer
        RouteMapping route = Utilitaire.getByUrlAndMethode(urlMethod, urlsMethodes);
        Object controller = route.getClazz().getDeclaredConstructor().newInstance();
        ModelAndView modelAndView = (ModelAndView) route.getMethod().invoke(controller);
        for(Map.Entry<String, Object> entry: modelAndView.getAttribute().entrySet()){
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        String view = prefixe + modelAndView.getView() + suffixe;
        RequestDispatcher requestDispatcher = request.getRequestDispatcher(view);
        requestDispatcher.forward(request, response);

        try (PrintWriter out = response.getWriter()) {
            out.println("<h1> Url : " + request.getRequestURI() + "</h1>");
            out.println("<h3>Méthode associée : <span style='font-weight:normal;'>"
                    + route.getMethod().getName() + "</span></h3>");
            out.println("<h4>Methode(Post ou get) : <span style='font-weight:normal;'>"
                    + urlMethod.getMethode() + "</span></h4>");
            out.println("<h3>Class associe : <span style='font-weight:normal;'>"
                    + route.getClazz().getSimpleName() + "</span></h3>");
            out.println("<h3>Execution de la methode : <span style='font-weight:normal;'>"
                    + route.getMethod().invoke(route) + "</span></h3>");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (UrlNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (UrlNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 InstantiationException e) {
                throw new RuntimeException(e);
        }
    }
}