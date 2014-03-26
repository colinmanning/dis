package com.setantamedia.fulcrum.db;

import com.setantamedia.fulcrum.models.core.Person;
import java.sql.Connection;
import org.h2.jdbcx.JdbcConnectionPool;

public class DbSessionData {

   public final static String DB_SESSION_DATA = "DBMANAGER_SESSION_DATA";

   private String sessionId = null;
   private JdbcConnectionPool h2ConnectionPool = null;
   private Connection jdbcConnection;
   private Person person = null;
   private Person damPerson = null;

   public DbSessionData() {

   }

   public String getSessionId() {
      return sessionId;
   }


   public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
   }


   public JdbcConnectionPool getH2ConnectionPool() {
      return h2ConnectionPool;
   }


   public void setH2ConnectionPool(JdbcConnectionPool h2ConnectionPool) {
      this.h2ConnectionPool = h2ConnectionPool;
   }


   public Person getPerson() {
      return person;
   }


   public void setPerson(Person person) {
      this.person = person;
   }

   public Person getDamPerson() {
      return damPerson;
   }

   public void setDamPerson(Person damPerson) {
      this.damPerson = damPerson;
   }

   public void tidyUp() {
      try {
         if (h2ConnectionPool != null) h2ConnectionPool.dispose();
         if (jdbcConnection != null) {
             if (!jdbcConnection.isClosed()) jdbcConnection.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

    public Connection getJdbcConnection() {
        return jdbcConnection;
    }

    public void setJdbcConnection(Connection jdbcConnection) {
        this.jdbcConnection = jdbcConnection;
    }


}
