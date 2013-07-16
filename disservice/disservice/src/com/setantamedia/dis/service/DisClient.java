package com.setantamedia.dis.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.setantamedia.fulcrum.common.SearchDescriptor;

/**
 * Provides functions from DIS that can be accessed via the DIS Socket interface
 * 
 * @author Colin Manning
 * 
 */
public class DisClient {

   private static Logger logger = Logger.getLogger(DisClient.class);
   private String host = "127.0.0.1";
   private String port = "8080";

   public DisClient() {

   }

   public DisClient(String host, String port) {
      init();
      this.host = host;
      this.port = port;
   }

   public void init() {

   }

   /**
    * 
    * @param username
    * @param password
    * @return session id if login is successful, null if not
    */
   public String login(String username, String password) {
      String result = null;
      String request = DisService.OPERATION_LOGIN + ":" +
            DisService.PARAMETER_USERNAME + "=" + username + ":" +
            DisService.PARAMETER_PASSWORD + "=" + password;
      try {
         Socket s = new Socket(this.host, Integer.valueOf(this.port));
         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         PrintWriter out = new PrintWriter(s.getOutputStream(), true);
         out.println(request);
         try {
            String response = in.readLine();
            if (response == null || response.equals("")) {
               logger.error("Invalid response to request: '" + request + "'");
            } else {
               String[] responseParts = response.split(":");
               if (responseParts.length > 0) {
                  switch (responseParts[0]) {
                  case DisService.STATUS_OK:
                     if (responseParts.length > 1) {
                        for (String responsePart : responseParts) {
                           String[] responseParam = responsePart.split("=");
                           switch (responseParam[0]) {
                           case DisService.PARAMETER_SESSIONID:
                              result = responseParam[1];
                              break;
                           default:
                              break;
                           }
                        }
                     }
                     break;
                  case DisService.STATUS_NOT_AUTHORISED:
                     logger.info("Unautohrused login request for username: '" + username + "'");
                     break;
                  case DisService.STATUS_ERROR:
                     logger.error(response);
                     break;
                  default:
                     logger.error("Invalid socket response: '" + response + "' to request: '" + request + "'");
                  }
               }
            }
         } catch (IOException ex) {
            ex.printStackTrace();
         } finally {
            s.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return result;
   }

   public String logout(String sessionId) {
      String result = null;
      String request = DisService.OPERATION_LOGOUT + ":sessionid=" + sessionId;
      try {
         Socket s = new Socket(this.host, Integer.valueOf(this.port));
         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         PrintWriter out = new PrintWriter(s.getOutputStream(), true);
         out.println(request);
         try {
            String response = in.readLine();
            if (response == null || response.equals("")) {
               logger.error("Invalid response to request: '" + request + "'");
            } else {
               String[] responseParts = response.split(":");
               if (responseParts.length > 0) {
                  result = responseParts[0];
               }
            }
         } catch (IOException ex) {
            ex.printStackTrace();
         } finally {
            s.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }

   public JSONObject runAction(String sessionId, String actionName, HashMap<String, String> params) {
      JSONObject result = null;
      String request = DisService.OPERATION_RUN_ACTION + ":sessionid=" + sessionId + ":name=" + actionName;
      if (params != null) {
         for (Map.Entry<String, String> entry : params.entrySet()) {
            request += ":" + entry.getKey()+"="+entry.getValue();
         }
      }
      try {
         Socket s = new Socket(this.host, Integer.valueOf(this.port));
         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         PrintWriter out = new PrintWriter(s.getOutputStream(), true);
         out.println(request);
         try {
            String response = in.readLine();
            result = new JSONObject(response);
         } catch (IOException ex) {
            ex.printStackTrace();
         } finally {
            s.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return result;
   }
   
   public JSONObject describe(String sessionId, String connectionName, String viewName, String outputFormat) {
      JSONObject result = new JSONObject();
      String request = DisService.OPERATION_DESCRIBE + ":sessionid=" + sessionId +
       ":" + DisService.PARAMETER_CONNECTION + "=" + connectionName;
      if (viewName != null && !"".equals(viewName)) {
       request += ":" + DisService.PARAMETER_VIEW + "=" + viewName;
      }      
      if (outputFormat != null && !"".equals(outputFormat)) {
       request += ":" + DisService.PARAMETER_FORMAT + "=" + outputFormat;
      }
      try {
         Socket s = new Socket(this.host, Integer.valueOf(this.port));
         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         PrintWriter out = new PrintWriter(s.getOutputStream(), true);
         out.println(request);
         try {
            String response = in.readLine();
            if (response != null) {
               result = new JSONObject(response);
            }
         } catch (IOException ex) {
            ex.printStackTrace();
         } finally {
            s.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }
   
   public JSONObject categorySearch(String sessionId, String connectionName, String categoryPath) {
      String request = DisService.OPERATION_SEARCH + ":sessionid=" + sessionId + 
      ":" + DisService.PARAMETER_CONNECTION + "=" + connectionName +
      ":" + DisService.PARAMETER_PATH + "=" + categoryPath;
      return sendRequest(request);
   }
   
   private JSONObject sendRequest(String request) {
      JSONObject result = new JSONObject();
      try {
         Socket s = new Socket(this.host, Integer.valueOf(this.port));
         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
         PrintWriter out = new PrintWriter(s.getOutputStream(), true);
         out.println(request);
         try {
            String response = in.readLine();
            if (response != null) {
               result = new JSONObject(response);
            }
         } catch (IOException ex) {
            ex.printStackTrace();
         } finally {
            s.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return result;
   }



}
