package com.setantamedia.fulcrum.common;

import com.setantamedia.fulcrum.ws.types.User;

/**
 *
 * @author Colin Manning
 */
public class Session {

   private Connection connection = null;
   private User user = null;
   private String ticket = null;
   private boolean valid = false;
   private String sessionId = null;

   public Session() {
   }

   public Connection getConnection() {
      return connection;
   }

   public void setConnection(Connection connection) {
      this.connection = connection;
   }

   public User getUser() {
      return user;
   }

   public void setUser(User user) {
      this.user = user;
   }

   public String getTicket() {
      return ticket;
   }

   public void setTicket(String ticket) {
      this.ticket = ticket;
   }

   public boolean isValid() {
      return valid;
   }

   public void setValid(boolean valid) {
      this.valid = valid;
   }

   public String getSessionId() {
      return sessionId;
   }

   public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
   }

}
