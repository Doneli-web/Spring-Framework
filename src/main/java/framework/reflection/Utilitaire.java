package framework.reflection;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


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
    public static List<String> getClassNamesWithAnnotation(String packageName, Class<? extends Annotation> annotationClass) throws Exception {

        List<Class<?>> classes = getClasses(packageName);
        List<String> result = new ArrayList<>();

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(annotationClass)) {
                result.add(clazz.getSimpleName());
            }
        }

        return result;
    }
}