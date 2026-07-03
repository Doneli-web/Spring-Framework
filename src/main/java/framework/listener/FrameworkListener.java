package framework.listener;

import framework.annotation.Controller;
import framework.reflection.Utilitaire;

import framework.route.RouteMapping;
import framework.route.UrlMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebListener
public class FrameworkListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        ServletContext context = servletContextEvent.getServletContext();

        String packageName = context.getInitParameter("packageName");

        try {
//            List<Class<?>> controllers =
//                    Utilitaire.getClassWithAnnotation(
//                            packageName,
//                            Controller.class);
//
//            Map<String, RouteMapping> urls = Utilitaire.urlDetection(controllers);
            Map<UrlMethod, RouteMapping> urlsMethodes = new HashMap<>();
            Utilitaire.getAllUrlMethode(urlsMethodes, packageName, Controller.class);
//            context.setAttribute("controllers", controllers);
//            context.setAttribute("urls", urls);
            context.setAttribute("urlsMethodes", urlsMethodes);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}