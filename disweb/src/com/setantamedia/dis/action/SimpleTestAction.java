/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.setantamedia.dis.action;

import com.setantamedia.fulcrum.actions.ActionProcessor;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Test workflow actions with simple behaviour
 *
 * @author Colin Manning
 */
public class SimpleTestAction extends ActionProcessor {

    private static Logger logger = Logger.getLogger(SimpleTestAction.class);
    public final static String PARAM_TEXT = "text";
    private String textParam = null;

    public SimpleTestAction() {
        super();
    }

    @Override
    public void init() {
        super.init();
        // params should be ready
        textParam = params.get(PARAM_TEXT);

    }

    @Override
    public JSONObject execute(HashMap<String, String> urlParams) {
        JSONObject result = new JSONObject();
        try {
        result.put("actionName", this.getClass().getName());
            System.out.println("SimpleTestAction on the way.");
            System.out.println("   --- textParam: " + textParam);
            for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                System.out.println("   --- url param: " + entry.getKey() + " has value: " + entry.getValue());
            }
            System.out.println("SimpleTestAction done.");
            result.put("status", "OK");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                result.put("status", "FAILED");
                result.put("errorMessage", e.getMessage());
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return result;
    }
}
