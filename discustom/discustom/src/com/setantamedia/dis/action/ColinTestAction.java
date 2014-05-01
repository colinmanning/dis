package com.setantamedia.dis.action;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.setantamedia.fulcrum.actions.ActionProcessor;

public class ColinTestAction extends ActionProcessor {
   private static Logger logger = Logger.getLogger(ColinTestAction.class);
   public final static String PARAM_NAME = "name";

   @Override
   public JSONObject execute(HashMap<String, String> params) {
      JSONObject result = new JSONObject(params);
      String name = params.get(PARAM_NAME);
      logger.info("Called test action with name: '" + name + "'");
      return result;
   }

}
