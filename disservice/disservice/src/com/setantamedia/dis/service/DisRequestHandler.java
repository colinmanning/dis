package com.setantamedia.dis.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

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
      try {

         /*
          * Decorate the streams so we can send characters and not just bytes.
          * Ensure output is flushed after every newline.
          */
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

         // Send a welcome message to the client.
         out.println(DisService.MESSAGE_READY);

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
               out.println(DisService.STATUS_OK + ":" + DisService.PARAMETER_SESSIONID + session.getId());
            } else {
               out.println(DisService.STATUS_NOT_AUTHORISED);
            }
            break;
         case DisService.OPERATION_LOGOUT:
            break;
         case DisService.OPERATION_RUN_ACTION:
            break;
         default:
            logger.error("Ignoring invalid DIS operation '" + input + "'");
         }
      } catch (IOException e) {
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
