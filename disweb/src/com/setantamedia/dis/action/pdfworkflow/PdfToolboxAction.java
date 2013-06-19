/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.setantamedia.dis.action.pdfworkflow;

import com.setantamedia.fulcrum.actions.ActionProcessor;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An action to drive Callas Toolbox Server
 *
 * @author Colin Manning
 */
public class PdfToolboxAction extends ActionProcessor {

    private static Logger logger = Logger.getLogger(PdfToolboxAction.class);
    public final static String PARAM_TEXT = "text";
    private String textParam = null;

    public PdfToolboxAction() {
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
            System.out.println("PdfToolboxAction on the way.");
            for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                System.out.println("   --- url param: " + entry.getKey() + " has value: " + entry.getValue());
            }
            System.out.println("PdfToolboxAction done.");
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
