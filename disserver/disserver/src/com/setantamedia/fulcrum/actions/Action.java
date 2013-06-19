package com.setantamedia.fulcrum.actions;

import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Colin Manning
 */
public class Action {

    private static Logger logger = Logger.getLogger(Action.class);

    private String name = "";
    private HashMap<String, Object> params = new HashMap<>();
    
    public Action() {
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }
    
    public void addParam(String key, Object value) {
        params.put(key, value);
    }
    
    public void removeParam(String key) {
        if (params.containsKey(key)) params.remove(key);

    }
    public Object getParam(String key) {
        return params.containsKey(key) ? params.get(key) : null;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        Action.logger = logger;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
