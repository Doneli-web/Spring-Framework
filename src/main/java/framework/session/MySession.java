package framework.session;

import java.util.HashMap;
import java.util.Map;

public class MySession {
    private Map<String, Object> attributes = new HashMap<>();

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = (attributes != null) ? attributes : new HashMap<>();
    }

    public Object getAttributeByName(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object attribute) {
        attributes.put(name, attribute);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public void invalidate() {
        attributes.clear();
    }

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public int size() {
        return attributes.size();
    }
}
