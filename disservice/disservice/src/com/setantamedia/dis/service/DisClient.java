package com.setantamedia.dis.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * Provides functions from DIS that can be accessed via the DIS Socket interface
 * 
 * @author Colin Manning
 * 
 */
public class DisClient {

   private static Logger logger = Logger.getLogger(DisClient.class);
   private Socket socket = null;
   private String host = "127.0.0.1";
   private String port = null;

   public DisClient() {

   }
   public DisClient(String host, String port) {
      init();
      if (connectToServer(host, port)) {
         logger.info("Connected to DIS Service on host: '"+host+"' and port: '"+port+"'");
      } else {
         logger.info("Failed to connect to DIS Service on host: '"+host+"' and port: '"+port+"'");
         
      }
   }

   public void init() {

   }

   public Boolean connectToServer(String host, String port) {
      Boolean result = false;
      this.host = host;
      this.port = port;
      try {
         socket = new Socket(this.host, Integer.valueOf(this.port));
      } catch (Exception e) {
         e.printStackTrace();
      }
      result = true;
      return result;
   }

   public DisSession login(String username, String password) {
      DisSession result = null;
      String request = DisService.OPERATION_LOGIN + ":" +
            DisService.PARAMETER_USERNAME + "=" + username + ":" +
            DisService.PARAMETER_PASSWORD + "=" + password;
      try {
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
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
                     result = new DisSession();
                     if (responseParts.length > 1) {
                        for (String responsePart:responseParts) {
                           String[] responseParam = responsePart.split("=");
                           switch (responseParam[0]) {
                           
                           }
                        }
                     }
                     break;
                  case DisService.STATUS_NOT_AUTHORISED:
                     logger.info("Unahtohrused login request for username: '"+username+"'");
                     break;
                  default:
                     logger.error("Invalid socket response: '" + response + "' to request: '" + request + "'");
                  }
               }
            }
         } catch (IOException ex) {
            ex.printStackTrace();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return result;
   }

   public void logout(DisSession session) {

   }

   public void runAction(DisSession session, String actionName) {

   }

}
