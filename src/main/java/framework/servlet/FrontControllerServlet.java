package framework.servlet;

import framework.session.MySession;
import jakarta.servlet.annotation.MultipartConfig;
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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import framework.reflection.ObjectBinder;
import framework.reflection.Utilitaire;

@MultipartConfig
public class FrontControllerServlet extends HttpServlet {

    String prefixe;
    String suffixe;
    String authAttributeName;
    private List<String> controllerNames;
    private Map<UrlMethod, RouteMapping> urlsMethodes;
    private ApplicationContext springContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void init() throws ServletException {
        try {
            this.urlsMethodes = (Map<UrlMethod, RouteMapping>) getServletContext().getAttribute("urlsMethodes");
            this.prefixe = (String) getServletContext().getAttribute("prefixe");
            this.suffixe = (String) getServletContext().getAttribute("suffixe");
            this.authAttributeName = (String) getServletContext().getAttribute("authAttributeName");
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
            Method method = route.getMethod();
            method.setAccessible(true);

            Object[] arguments = resolveArguments(method, request);
            Utilitaire.checkAuthentification(route, this.authAttributeName, request.getSession(true));
            result = method.invoke(controller, arguments);
            MySession mySession = checkSession(arguments);
            saveSession(request, mySession);

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw new ServletException( "Erreur dans la méthode : " + route.getMethod().getName(), cause );
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (route.getMethod().getReturnType() == void.class) {
            return;
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

    private Object[] resolveArguments(Method method, HttpServletRequest request)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ServletException, IOException {
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            arguments[i] = ObjectBinder.resolve(
                    parameters[i].getParameterizedType(),
                    parameters[i].getName(),
                    request,
                    true
            );
        }

        return arguments;
    }

    private MySession checkSession(Object[] arguments) {
        if (arguments == null) return null;

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] instanceof MySession) {
                return (MySession) arguments[i];
            }
        }
        return null;
    }

    public static void saveSession(HttpServletRequest request, MySession mySession) {
        if (mySession == null) return;

        HttpSession httpSession = request.getSession(true);
        Map<String, Object> attributesMap = mySession.getAttributes();

        for (Map.Entry<String, Object> entry : attributesMap.entrySet()) {
            httpSession.setAttribute(entry.getKey(), entry.getValue());
        }

        java.util.Enumeration<String> httpNames = httpSession.getAttributeNames();
        List<String> toRemove = new ArrayList<>();

        while (httpNames.hasMoreElements()) {
            String name = httpNames.nextElement();
            if (!attributesMap.containsKey(name)) {
                toRemove.add(name);
            }
        }
        for (String name : toRemove) {
            httpSession.removeAttribute(name);
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