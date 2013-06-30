package com.setantamedia.dis.service.requests;

import com.setantamedia.dis.service.DisSession;

public class LogoutRequest {

   DisSession session = null;
   
   public LogoutRequest() {
      
   }

   public DisSession getSession() {
      return session;
   }

   public void setSession(DisSession session) {
      this.session = session;
   }

}
