package com.setantamedia.fulcrum.common;

import java.util.HashMap;

public class Connection {

   protected String name = null;
   protected String server = null;
   protected String username = null;
   protected String password = null;
   protected String database = null;
   protected int poolSize = 0;
   protected boolean readOnly = false;
   protected Dam dam = null;
   protected HashMap<String, String> params = new HashMap<>();
   protected HashMap<String, Service> services = new HashMap<>();
   protected int id = -1;

   public Connection() {
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the id
    */
   public int getId() {
      return id;
   }

   /**
    * @param id the id to set
    */
   public void setId(int id) {
      this.id = id;
   }

   /**
    * @return the readOnly
    */
   public boolean isReadOnly() {
      return readOnly;
   }

   /**
    * @return the readOnly
    */
   public boolean getReadOnly() {
      return readOnly;
   }

   /**
    * @param readOnly the readOnly to set
    */
   public void setReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
   }

   /**
    * @return the server
    */
   public String getServer() {
      return server;
   }

   /**
    * @param server the server to set
    */
   public void setServer(String server) {
      this.server = server;
   }

   /**
    * @return the username
    */
   public String getUsername() {
      return username;
   }

   /**
    * @param username the username to set
    */
   public void setUsername(String username) {
      this.username = username;
   }

   /**
    * @return the password
    */
   public String getPassword() {
      return password;
   }

   /**
    * @param password the password to set
    */
   public void setPassword(String password) {
      this.password = password;
   }

   /**
    * @return the database
    */
   public String getDatabase() {
      return database;
   }

   /**
    * @param database the database to set
    */
   public void setDatabase(String database) {
      this.database = database;
   }

   /**
    * @return the licenses
    */
   public int getPoolSize() {
      return poolSize;
   }

   /**
    * @param licenses the poolSize to set
    */
   public void setPoolSize(int poolSize) {
      this.poolSize = poolSize;
   }

   public Dam getDam() {
      return dam;
   }

   public void setDam(Dam dam) {
      this.dam = dam;
   }

   public HashMap<String, String> getParams() {
      return params;
   }

   public void setParams(HashMap<String, String> params) {
      this.params = params;
   }

   public HashMap<String, Service> getServices() {
      return services;
   }

   public void setServices(HashMap<String, Service> services) {
      this.services = services;
   }

   public void addService(Service service) {
      services.put(service.getName(), service);
   }

   public Service getService(String name) {
      return services.get(name);
   }

   public void removeService(String name) {
      if (services.get(name) != null) {
         services.remove(name);
      }
   }
   
   @Override
   public String toString() {
       return name;
   }
   
}
