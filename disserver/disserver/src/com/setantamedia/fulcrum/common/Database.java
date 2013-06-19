package com.setantamedia.fulcrum.common;

import com.setantamedia.fulcrum.db.DbManager;

public class Database {

   private String name = null;
   private String driver = null;
   private DbManager manager = null;
   
   public Database() {
      
   }
   
   public String getName() {
      return name;
   }
  
   public void setName(String name) {
      this.name = name;
   }

   
   public String getDriver() {
      return driver;
   }

   
   public void setDriver(String driver) {
      this.driver = driver;
   }

    public DbManager getManager() {
        return manager;
    }

    public void setManager(DbManager manager) {
        this.manager = manager;
    }
}
