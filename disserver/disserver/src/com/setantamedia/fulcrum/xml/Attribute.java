package com.setantamedia.fulcrum.xml;

import org.json.JSONObject;

public class Attribute {

    // class variables
    private Element element = null;
    private String name = null;
    private Object value;

    public Attribute(Element element, String name, Object value) {
        this.element = element;
        this.name = name;
        this.value = value;
     }

    public String toPrettyString() {
        return toString();
    }

    @Override
    public String toString() {
        return name + "=\"" + value + "\"";
    }

    public JSONObject toJson() {
        JSONObject result = null;
        return result;
    }

    public Element getElement() {
        return element;
    }

    public String getName() {
        return name;
    }
}
