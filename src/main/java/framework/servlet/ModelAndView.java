package framework.servlet;

import java.util.HashMap;
import java.util.Objects;

public class ModelAndView {
    private String view;
    private HashMap<String, Object> attribute = new HashMap<>();

    public ModelAndView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public HashMap<String, Object> getAttribute() {
        return attribute;
    }

    public void setAttribute(HashMap<String, Object> attribute) {
        this.attribute = attribute;
    }

    public void addObject(String name, Object object) {
        attribute.put(name, object);
    }
}
