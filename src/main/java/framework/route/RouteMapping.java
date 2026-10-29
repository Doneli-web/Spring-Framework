package framework.route;

import framework.annotation.Auth;
import framework.annotation.HasRole;

import java.lang.reflect.Method;
import java.util.List;

public class RouteMapping {
    private Class<?> Clazz;
    private Method method;
    private final boolean requiresAuth;
    private final boolean requireRole;

    public RouteMapping(Class<?> Clazz, Method method) {
        this.Clazz = Clazz;
        this.method = method;
        this.requiresAuth = method.isAnnotationPresent(Auth.class);
        this.requireRole = method.isAnnotationPresent(HasRole.class);
    }

    public Class<?> getClazz() {
        return Clazz;
    }

    public void setClass(Class<?> Clazz) {
        this.Clazz = Clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public boolean isRequiresAuth() {
        return requiresAuth;
    }

    public boolean isRequireRole() {
        return requireRole;
    }

    public boolean hasTheRole(String role){
        HasRole hasRole = method.getAnnotation(HasRole.class);
        String[] roles = hasRole.value();
        return List.of(roles).contains(role);
    }
}
