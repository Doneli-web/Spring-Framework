package framework.reflection;

import framework.annotation.APIRest;
import framework.annotation.Auth;
import framework.annotation.UrlMapping;
import framework.exception.UrlNotFoundException;
import framework.route.RouteMapping;
import framework.route.UrlMethod;
import framework.session.MySession;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public static void checkAuthentification(RouteMapping route, String authAttributeName, String role, HttpSession session) throws Exception {
        if (route.isRequiresAuth()) {
            boolean authenticated = false;
            boolean hasTherole = false;

            if(session.getAttribute(authAttributeName) != null){
                authenticated = true;
                if(route.isRequireRole()){
                    if (session.getAttribute(role) != null) {
                        hasTherole = route.hasTheRole((String) session.getAttribute(role));
                    } else {
                        throw new Exception(
                                "Erreur : Veuillez d'abord definir votre role"
                        );
                    }

                    if (!hasTherole) {
                        throw new Exception(
                                "Erreur : Votre role qui est de " + (String) session.getAttribute(role) + " n'est pas eligible a executer la methode"
                        );
                    }
                }
            }

            if (!authenticated) {
                throw new Exception(
                        "Erreur : Vous devez vous authentifier pour pouvoir acceder a cette methode"
                );
            }
        }
    }


    public static Map<String, Method> getUrlMethods(Class<?> classe) {
        Map<String, Method> routes = new HashMap<>();

        for (Method method : classe.getDeclaredMethods()) {
            if (method.isAnnotationPresent(UrlMapping.class)) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                routes.put(urlMapping.value(), method);
            }
        }

        return routes;
    }

    public static Method getMethodByUrl(String url, List<Class<?>> classes) throws Exception {
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

    public static boolean isApiRest(Method method) {
        return method.isAnnotationPresent(APIRest.class);
    }

    public static void scaningMethodesClass(Map<UrlMethod, RouteMapping> map, Class<?> clazz) throws Exception {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(UrlMapping.class)) {
                UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                UrlMethod urlMethod = new UrlMethod(annotation.value(), annotation.methode());
                if (map.containsKey(urlMethod)) {
                    RouteMapping route = map.get(urlMethod);
                    throw new Exception(
                            "Erreur : l'url : " + urlMethod.getUrl() + "' et la methode : '"
                                    + urlMethod.getMethode() + "' existe deja dans la class : '"
                                    + route.getClazz().getSimpleName() + "'et la methode : "
                                    + route.getMethod().getName() + "'"
                    );
                }
                map.put(urlMethod, new RouteMapping(clazz, method));
            }
        }
    }

    public static void getAllUrlMethode(Map<UrlMethod, RouteMapping> map, String packageName, Class<? extends Annotation> annotationClass) throws Exception {
        List<Class<?>> classes = getClasses(packageName);

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotationClass)) {
                scaningMethodesClass(map, clazz);
            }
        }
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
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object convert(String value, Class<?> type) {
        boolean vide = (value == null || value.trim().isEmpty());

        if (type == String.class) {
            return value;
        }

        if (vide) {
            return defaultValue(type);
        }

        value = value.trim();

        try {
            if (type == int.class || type == Integer.class) return Integer.parseInt(value);
            if (type == long.class || type == Long.class) return Long.parseLong(value);
            if (type == double.class || type == Double.class) return Double.parseDouble(value);
            if (type == float.class || type == Float.class) return Float.parseFloat(value);
            if (type == short.class || type == Short.class) return Short.parseShort(value);
            if (type == byte.class || type == Byte.class) return Byte.parseByte(value);
            if (type == boolean.class || type == Boolean.class) {
                return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on")
                        || value.equals("1") || value.equalsIgnoreCase("yes");
            }
            if (type == char.class || type == Character.class) {
                if (value.length() != 1) {
                    throw new IllegalArgumentException("un seul caractère attendu");
                }
                return value.charAt(0);
            }
            if (type == BigDecimal.class) return new BigDecimal(value);
            if (type == BigInteger.class) return new BigInteger(value);
            if (type == LocalDate.class) return LocalDate.parse(value);
            if (type == LocalDateTime.class) return LocalDateTime.parse(value);
            if (type == LocalTime.class) return LocalTime.parse(value);
            if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "impossible de convertir '" + value + "' en " + type.getSimpleName(), e);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "impossible de convertir '" + value + "' en " + type.getSimpleName(), e);
        }

        throw new IllegalArgumentException("type non supporté : " + type.getName());
    }

    private static Object defaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0f;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return null;
    }
}