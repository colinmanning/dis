package com.setantamedia.dis.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.setantamedia.dis.service.requests.LoginRequest;

public class DisRequestHandler extends Thread {

   private final static Logger logger = Logger.getLogger(DisRequestHandler.class);

   private Socket socket = null;
   private DisService disService = null;

   public DisRequestHandler(DisService disService, Socket socket) {
      this.disService = disService;
      this.socket = socket;
   }

   /**
    * Services this thread's client by first sending the client a welcome
    * message then repeatedly reading strings and sending back the capitalized
    * version of the string.
    */
   public void run() {
      BufferedReader in = null;
      PrintWriter out = null;
      try {

         /*
          * Decorate the streams so we can send characters and not just bytes.
          * Ensure output is flushed after every newline.
          */
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         out = new PrintWriter(socket.getOutputStream(), true);

         // Send a welcome message to the client.
         // out.println(DisService.MESSAGE_READY);

         /*
          * For now we do not handle multi-line input String input = ""; while
          * (true) { input = in.readLine(); if (input == null ||
          * input.equals(".")) { break; } }
          */
         String input = in.readLine();
         String[] commandParts = input.split(":");
         switch (commandParts[0]) {
         case DisService.OPERATION_LOGIN:
            LoginRequest loginRequest = new LoginRequest();
            for (String commandPart : commandParts) {
               String[] argParts = commandPart.split("=");
               if (argParts.length == 2) {
                  switch (argParts[0]) {
                  case DisService.PARAMETER_USERNAME:
                     loginRequest.setUsername(argParts[1]);
                     break;
                  case DisService.PARAMETER_PASSWORD:
                     loginRequest.setPassword(argParts[1]);
                     break;
                  }
               }
            }
            DisSession session = disService.doLogin(loginRequest);
            if (session != null) {
               out.println(DisService.STATUS_OK + ":" + DisService.PARAMETER_SESSIONID + "=" + session.getId());
            } else {
               out.println(DisService.STATUS_NOT_AUTHORISED);
            }
            break;
         case DisService.OPERATION_LOGOUT:
            String sessionId = null;
            for (String commandPart : commandParts) {
               String[] argParts = commandPart.split("=");
               if (argParts.length == 2) {
                  switch (argParts[0]) {
                  case DisService.PARAMETER_SESSIONID:
                     sessionId = argParts[1];
                     ;
                     break;
                  }
               }
            }
            String returnStatus = disService.doLogout(sessionId);
            if (sessionId != null) {
               out.println(returnStatus);
            } else {
               out.println(DisService.STATUS_NOT_AUTHORISED);
            }
            break;
         case DisService.OPERATION_RUN_ACTION:
            String actionName = null;
            sessionId = null;
            HashMap<String, String> params = new HashMap<>();
            for (String commandPart : commandParts) {
               String[] argParts = commandPart.split("=");
               if (argParts.length == 2) {
                  switch (argParts[0]) {
                  case DisService.PARAMETER_SESSIONID:
                     sessionId = argParts[1];
                     break;
                  case DisService.PARAMETER_NAME:
                     actionName = argParts[1];
                     break;
                  default:
                     params.put(argParts[0], argParts[1]);
                     break;
                  }
               }
            }
            JSONObject result = disService.doRunAction(sessionId, actionName, params);
            if (result == null) {
               result = new JSONObject();
            }
            out.println(result);
            break;
         case DisService.OPERATION_DESCRIBE:
            sessionId = null;
            String viewName = null;
            String outputFormat = null;
            String connectionName = null;
            for (String commandPart : commandParts) {
               String[] argParts = commandPart.split("=");
               if (argParts.length == 2) {
                  switch (argParts[0]) {
                  case DisService.PARAMETER_SESSIONID:
                     sessionId = argParts[1];
                     break;
                  case DisService.PARAMETER_VIEW:
                     viewName = argParts[1];
                     break;
                  case DisService.PARAMETER_FORMAT:
                     outputFormat = argParts[1];
                     break;
                  case DisService.PARAMETER_CONNECTION:
                     connectionName = argParts[1];
                  default:
                     break;
                  }
               }
            }
            result = disService.doDescribe(sessionId, connectionName, viewName, outputFormat);
            if (result == null) {
               result = new JSONObject();
            }
            out.println(result);
            break;
         default:
            logger.error("Ignoring invalid DIS operation '" + input + "'");
         }
      } catch (IOException e) {
         out.println(DisService.STATUS_ERROR + ":" + e.getMessage());
         logger.debug("Error handling request");
         e.printStackTrace();
      } finally {
         try {
            socket.close();
         } catch (IOException e) {
            logger.debug("Couldn't close a socket, what's going on?");
         }
      }
   }

}
