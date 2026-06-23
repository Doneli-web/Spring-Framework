package framework.reflection;

import framework.annotation.UrlMapping;
import framework.exception.UrlNotFoundException;

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
}