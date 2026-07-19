package framework.listener;

import framework.annotation.Controller;
import framework.reflection.Utilitaire;
import framework.route.RouteMapping;
import framework.route.UrlMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


import java.util.HashMap;
import java.util.Map;

@WebListener
public class FrameworkListener implements ServletContextListener {

    private ApplicationContext springContext;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        ServletContext context = servletContextEvent.getServletContext();

        String packageName = context.getInitParameter("packageName");
        String prefixe = context.getInitParameter("prefixe");
        String suffixe = context.getInitParameter("suffixe");
        String springConfigClass = context.getInitParameter("springConfigClass");

        try {

            springContext = new AnnotationConfigApplicationContext(Class.forName(springConfigClass));
            context.setAttribute("springContext", springContext);

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
        if (springContext != null) {
            springContext.close();
        }
    }
}