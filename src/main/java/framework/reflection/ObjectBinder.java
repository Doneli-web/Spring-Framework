package framework.reflection;

import framework.servlet.MapFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;


public class ObjectBinder {

    public static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || Number.class.isAssignableFrom(type)
                || type == Boolean.class
                || type == Character.class
                || type == LocalDate.class
                || type == LocalDateTime.class
                || type == LocalTime.class
                || type.isEnum();
    }

    public static Object resolve(Type type, String name, HttpServletRequest request, boolean always)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ServletException, IOException {

        Class<?> raw = rawClass(type);

        if (isSimpleType(raw)) {
            String value = request.getParameter(name);
            if (value == null) {
                return always ? Utilitaire.convert(null, raw) : null;
            }
            return Utilitaire.convert(value, raw);
        }
        if( raw == MapFile.class) {
            return resolveFile(name, request, always);
        }
        if (raw.isArray()) {
            return resolveArray(type, raw, name, request, always);
        }
        if (Collection.class.isAssignableFrom(raw)) {
            return resolveCollection(type, raw, name, request, always);
        }
        if (Map.class.isAssignableFrom(raw)) {
            return resolveMap(type, raw, name, request, always);
        }
        return resolveObject(raw, name, request, always);
    }

    private static Object resolveArray(Type type, Class<?> raw, String name, HttpServletRequest request, boolean always)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ServletException, IOException {

        Type componentType = (type instanceof GenericArrayType)
                ? ((GenericArrayType) type).getGenericComponentType()
                : raw.getComponentType();

        List<Object> items = resolveItems(componentType, name, request);
        if (items == null) {
            return always ? Array.newInstance(raw.getComponentType(), 0) : null;
        }

        Object array = Array.newInstance(raw.getComponentType(), items.size());
        for (int i = 0; i < items.size(); i++) {
            Array.set(array, i, items.get(i));
        }
        return array;
    }


    @SuppressWarnings("unchecked")
    private static Object resolveCollection(Type type, Class<?> raw, String name, HttpServletRequest request, boolean always)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ServletException, IOException {

        List<Object> items = resolveItems(typeArgument(type, 0), name, request);
        if (items == null && !always) {
            return null;
        }

        Collection<Object> collection;
        if (raw.isInterface() || Modifier.isAbstract(raw.getModifiers())) {
            if (SortedSet.class.isAssignableFrom(raw)) collection = new TreeSet<>();
            else if (Set.class.isAssignableFrom(raw)) collection = new LinkedHashSet<>();
            else if (Queue.class.isAssignableFrom(raw)) collection = new LinkedList<>();
            else collection = new ArrayList<>();
        } else {
            collection = (Collection<Object>) raw.getDeclaredConstructor().newInstance();
        }

        if (items != null) {
            collection.addAll(items);
        }
        return collection;
    }

    private static List<Object> resolveItems(Type elementType, String name, HttpServletRequest request)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ServletException, IOException {

        Class<?> elementClass = rawClass(elementType);
        List<Object> items = new ArrayList<>();

        if (isSimpleType(elementClass)) {
            String[] values = request.getParameterValues(name);
            if (values != null) {
                for (String value : values) {
                    items.add(Utilitaire.convert(value, elementClass));
                }
                return items;
            }
        }

        TreeSet<Integer> indexes = new TreeSet<>();
        for (String key : keysUnder(request, name)) {
            try {
                indexes.add(Integer.parseInt(key));
            } catch (NumberFormatException ignored) {
            }
        }
        if (indexes.isEmpty()) {
            return null;
        }

        for (int index : indexes) {
            items.add(resolve(elementType, name + "[" + index + "]", request,true));
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private static Object resolveMap(Type type, Class<?> raw, String name, HttpServletRequest request, boolean always)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ServletException, IOException {

        Set<String> keys = keysUnder(request, name);
        if (keys.isEmpty() && !always) {
            return null;
        }

        Map<Object, Object> map;
        if (raw.isInterface() || Modifier.isAbstract(raw.getModifiers())) {
            map = SortedMap.class.isAssignableFrom(raw) ? new TreeMap<>() : new LinkedHashMap<>();
        } else {
            map = (Map<Object, Object>) raw.getDeclaredConstructor().newInstance();
        }

        Type keyType = typeArgument(type, 0);
        Type valueType = typeArgument(type, 1);

        for (String key : keys) {
            Object value = resolve(valueType, name + "[" + key + "]", request, true);
            map.put(Utilitaire.convert(key, rawClass(keyType)), value);
        }
        return map;
    }

    private static Object resolveObject(Class<?> raw, String name, HttpServletRequest request, boolean always)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ServletException, IOException {

        if (!always && !hasParameterStartingWith(request, name + ".")) {
            return null;
        }

        Object instance = raw.getDeclaredConstructor().newInstance();

        for (Class<?> c = raw; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {

                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }

                Object value = resolve(field.getGenericType(), name + "." + field.getName(), request, false);
                if (value == null) {
                    continue;
                }

                field.setAccessible(true);
                field.set(instance, value);
            }
        }
        return instance;
    }


    private static MapFile resolveFile(String name, HttpServletRequest request, boolean always
    ) throws ServletException, IOException {
        Part filePart = request.getPart(name);
        if (filePart == null
                || filePart.getSubmittedFileName() == null
                || filePart.getSubmittedFileName().isBlank()) {
            if (always) {
                throw new ServletException(
                        "Aucun fichier n'a été envoyé pour le paramètre : " + name
                );
            }

            return null;
        }

        return getMultipartFile(filePart);
    }


    private static MapFile getMultipartFile(Part filePart) throws IOException {
        MapFile mapFile = new MapFile();

        String originalFilename = filePart.getSubmittedFileName();

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }

        InputStream fileStream = filePart.getInputStream();
        byte[] fileBytes = fileStream.readAllBytes();

        mapFile.setFileName(originalFilename);
        mapFile.setExtension(extension);
        mapFile.setBytes(fileBytes);
        
        return mapFile;
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        if (type instanceof GenericArrayType) {
            Class<?> component = rawClass(((GenericArrayType) type).getGenericComponentType());
            return Array.newInstance(component, 0).getClass();
        }
        return Object.class;
    }

    private static Type typeArgument(Type type, int position) {
        if (type instanceof ParameterizedType) {
            Type[] arguments = ((ParameterizedType) type).getActualTypeArguments();
            if (position < arguments.length) {
                Type argument = arguments[position];
                if ((argument instanceof Class && argument != Object.class)
                        || argument instanceof ParameterizedType
                        || argument instanceof GenericArrayType) {
                    return argument;
                }
            }
        }
        return String.class;
    }

    private static Set<String> keysUnder(HttpServletRequest request, String name) {
        Set<String> keys = new LinkedHashSet<>();
        String start = name + "[";
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String parameter = names.nextElement();
            if (parameter.startsWith(start)) {
                int end = parameter.indexOf(']', start.length());
                if (end > start.length()) {
                    keys.add(parameter.substring(start.length(), end));
                }
            }
        }
        return keys;
    }

    private static boolean hasParameterStartingWith(HttpServletRequest request, String start) {
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String parameter = names.nextElement();
            if (parameter.startsWith(start)) {
                return true;
            }
        }
        return false;
    }
}