package com.setantamedia.dis.service;

@SuppressWarnings("serial")
public class InvalidSessionException extends Exception {

   public final static String DEFAULT_MESSAGE = "Invalid DIS Session";
   
   public InvalidSessionException() {
      super();
   }
 
   @Override
   public String getMessage() {
      return DEFAULT_MESSAGE;
   }
}
