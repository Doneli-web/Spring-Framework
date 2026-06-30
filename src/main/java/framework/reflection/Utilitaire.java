package framework.reflection;

import framework.annotation.UrlMapping;
import framework.exception.UrlNotFoundException;
import framework.route.RouteMapping;
import framework.route.UrlMethod;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Utilitaire {
    public static List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();

        String path = packageName.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            throw new ClassNotFoundException("Package introuvable : " + packageName);
        }

        File directory = new File(resource.toURI());

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + "."
                            + file.getName().replace(".class", "");

                    classes.add(Class.forName(className));
                }
            }
        }

        return classes;
    }
//    public static List<String> getClassNamesWithAnnotation(String packageName, Class<? extends Annotation> annotationClass) throws Exception {
//
//        List<Class<?>> classes = getClasses(packageName);
//        List<String> result = new ArrayList<>();
//
//        for (Class<?> clazz : classes) {
//            if (clazz.isAnnotationPresent(annotationClass)) {
//                result.add(clazz.getSimpleName());
//            }
//        }
//
//        return result;
//    }
    public static List<Class<?>> getClassWithAnnotation(String packageName, Class<? extends Annotation> annotationClass) throws Exception {

        List<Class<?>> classes = getClasses(packageName);
        List<Class<?>> result = new ArrayList<>();

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotationClass)) {
                result.add(clazz);
            }
        }

        return result;
    }
    public static Map<String, Method> getUrlMethods(Class<?> classe) {

        Map<String, Method> routes = new HashMap<>();

        Method[] methods = classe.getDeclaredMethods();

        for (Method method : methods) {

            if (method.isAnnotationPresent(UrlMapping.class)) {

                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);

                routes.put(urlMapping.value(), method);
            }
        }

        return routes;
    }
    public static Method getMethodByUrl(String url, List<Class<?>> classes)
            throws Exception {

        StringBuilder supportedUrls = new StringBuilder();

        for (Class<?> clazz : classes) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                    if (annotation.value().equals(url)) {
                        return method;
                    }
                    supportedUrls.append(annotation.value())
                            .append(" -> ")
                            .append(clazz.getSimpleName())
                            .append(".")
                            .append(method.getName())
                            .append("\n");
                }
            }
        }

        throw new UrlNotFoundException(
                "Aucune méthode ne correspond à l'URL : " + url +
                        "\n\nURLs supportées :\n" +
                        supportedUrls
        );
    }
    public static Map<String, RouteMapping> urlDetection(List<Class<?>> classes){
        Map<String, RouteMapping> result = new HashMap<>();
        for (Class<?> clazz : classes) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                    result.put(annotation.value(), new RouteMapping(clazz, method));
                }
            }
        }

        return result;
    }
    public static RouteMapping getByUrl(String url, Map<String, RouteMapping> map) throws UrlNotFoundException{
        RouteMapping route = map.get(url);
        StringBuilder message = new StringBuilder("Aucun methode associer a l'url : " + url + "\n");
        message.append("Listes des urls supporter : \n");
        for(Map.Entry<String, RouteMapping> entry: map.entrySet()){
            message.append("Url : ").append(entry.getKey()).append(" ---> Methode : ").append(entry.getValue().getMethod().getName()).append(" ---> Class : ").append(entry.getValue().getClazz().getSimpleName()).append("\n");
        }
        if(route != null){
            return route;
        }
        throw new UrlNotFoundException(
                message.toString()
        );
    }
    public static Map<UrlMethod, RouteMapping> getAllUrlMethode(List<Class<?>> classes) throws Exception {
        Map<UrlMethod, RouteMapping> result = new HashMap<>();
        for (Class<?> clazz : classes) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                    UrlMethod urlMethod = new UrlMethod(annotation.value(), annotation.methode());
                    if(result.containsKey(urlMethod)){
                        RouteMapping route = result.get(urlMethod);
                        throw new Exception(
                                "Erreur : l'url : " + urlMethod.getUrl() + "' et la methode : '"
                                        + urlMethod.getMethode() + "' existe deja dans la class : '"
                                        + route.getClazz().getSimpleName() + "'et la methode : "
                                        + route.getMethod().getName() + "'"
                        );
                    }
                    result.put(urlMethod, new RouteMapping(clazz, method));
                }
            }
        }

        return result;
    }
    public static RouteMapping getByUrlAndMethode(UrlMethod urlMethod, Map<UrlMethod, RouteMapping> map) throws UrlNotFoundException {
        RouteMapping route = map.get(urlMethod);
        StringBuilder message = new StringBuilder("Aucun methode associer a l'url : " + urlMethod.getUrl() + "\n");
        message.append("Listes des urls supporter : \n");
        for(Map.Entry<UrlMethod, RouteMapping> entry: map.entrySet()){
            message.append("Url : ").append(entry.getKey().getUrl()).append(" ---> Methode : ").append(entry.getValue().getMethod().getName()).append("---> Type de requete : ").append(entry.getKey().getMethode()).append(" ---> Class : ").append(entry.getValue().getClazz().getSimpleName()).append("\n");
        }
        if(route == null){
            throw new UrlNotFoundException(
                    message.toString()
            );
        }

        return route;
    }
}