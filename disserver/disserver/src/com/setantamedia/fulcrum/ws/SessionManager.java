package com.setantamedia.fulcrum.ws;

import com.setantamedia.fulcrum.db.DbSessionData;
import com.setantamedia.fulcrum.db.impl.H2DbManager;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionManager implements HttpSessionListener {

   @Override
   public void sessionCreated(HttpSessionEvent event) {
   }

   @Override
   public void sessionDestroyed(HttpSessionEvent event) {
      HttpSession session = event.getSession();
      Object dbSessionData = session.getAttribute(H2DbManager.DB_SESSION_DATA);
      if (dbSessionData != null && (dbSessionData instanceof DbSessionData)) {
         // ensure connection pool etc are disposed
         ((DbSessionData) dbSessionData).tidyUp();
      }
   }
}
