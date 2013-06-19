package com.setantamedia.fulcrum.models.core;

/**
 * A class that represents a project. How a project is actually structured is dependent
 * on the relevant DAM and/or Workflow system.
 *
 * @author Colin Manning
 */
public class Project {

   private String id = null;
   private String name = null;
   private String database = null;
   private String root = null;

   public Project() {

   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDatabase() {
      return database;
   }

   public void setDatabase(String database) {
      this.database = database;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getRoot() {
      return root;
   }

   public void setRoot(String root) {
      this.root = root;
   }


}
