package framework.servlet;

import framework.exception.UrlNotFoundException;
import framework.route.RouteMapping;
import framework.route.UrlMethod;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
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
    private ApplicationContext springContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void init() throws ServletException {
        try {
            
            this.urlsMethodes = (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("urlsMethodes");
            this.prefixe = (String) getServletContext().getAttribute("prefixe");
            this.suffixe = (String) getServletContext().getAttribute("suffixe");

            this.springContext = (ApplicationContext) getServletContext().getAttribute("springContext");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws UrlNotFoundException, ServletException, IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {

        String contextPath = request.getContextPath();
        String url = request.getRequestURI().substring(contextPath.length());
        String methode = request.getMethod();
        UrlMethod urlMethod = new UrlMethod(url, methode);

        RouteMapping route = Utilitaire.getByUrlAndMethode(urlMethod, urlsMethodes);
        Object controller = route.getClazz().getDeclaredConstructor().newInstance();

        Map<String, String[]> requestParams = request.getParameterMap();

        if (springContext != null) {
            springContext.getAutowireCapableBeanFactory().autowireBean(controller);
        }

        Object result = null;
        try {
            route.getMethod().setAccessible(true);
            if(route.getMethod().getParameterCount() != 0){
                Parameter[] parameters = route.getMethod().getParameters();
                Object[] arguments = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    String parameterName = parameters[i].getName();
                    String parameterValue = request.getParameter(parameterName);

                    arguments[i] = Utilitaire.convert(
                            parameterValue,
                            parameters[i].getType()
                    );
                }

                if(route.getMethod().getReturnType() == void.class){
                    route.getMethod().invoke(controller, arguments);

                    return;
                } else {
                    result = route.getMethod().invoke(controller, arguments);
                }

            } else {
                result = route.getMethod().invoke(controller);
            }

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw new ServletException( "Erreur dans la méthode : " + route.getMethod().getName(), cause );
            }
            throw e;
        }

        if (Utilitaire.isApiRest(route.getMethod())) {

            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                String json = objectMapper.writeValueAsString(result);
                out.println(json);
            }

            return;
        }

        if (result instanceof ModelAndView) {
            ModelAndView modelAndView = (ModelAndView) result;
            for (Map.Entry<String, Object> entry : modelAndView.getAttribute().entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
            String view = prefixe + modelAndView.getView() + suffixe;
            RequestDispatcher requestDispatcher = request.getRequestDispatcher(view);
            requestDispatcher.forward(request, response);

            return;
        }

        response.setContentType("text/plain;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            out.println(String.valueOf(result));
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