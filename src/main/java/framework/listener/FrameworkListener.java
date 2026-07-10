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
        String prefixe = context.getInitParameter("prefixe");
        String suffixe = context.getInitParameter("suffixe");

        try {
            Map<UrlMethod, RouteMapping> urlsMethodes = new HashMap<>();
            Utilitaire.getAllUrlMethode(urlsMethodes, packageName, Controller.class);
            context.setAttribute("urlsMethodes", urlsMethodes);
            context.setAttribute("prefixe", prefixe);
            context.setAttribute("suffixe", suffixe);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}