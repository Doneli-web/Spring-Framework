package framework.route;

import java.lang.reflect.Method;

public class RouteMapping {
    private Class<?> Clazz;
    private Method method;

    public RouteMapping(Class<?> Clazz, Method method) {
        this.Clazz = Clazz;
        this.method = method;
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
}
