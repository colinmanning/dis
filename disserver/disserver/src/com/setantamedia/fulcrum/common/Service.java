package com.setantamedia.fulcrum.common;

import java.util.ArrayList;

/**
 *
 * @author Colin Manning
 */
public class Service {

   private String name = null;
   private Boolean publicService = false;
   private ArrayList<IPMatcher> ips = new ArrayList<>();
   private ArrayList<String> users = new ArrayList<>();
   private ArrayList<String> roles = new ArrayList<>();

   public Service() {
   }

   public ArrayList<IPMatcher> getIps() {
      return ips;
   }

   public void setIps(ArrayList<IPMatcher> ips) {
      this.ips = ips;
   }

   public ArrayList<String> getUsers() {
      return users;
   }

   public void setUsers(ArrayList<String> users) {
      this.users = users;
   }

   public ArrayList<String> getRoles() {
      return roles;
   }

   public void setRoles(ArrayList<String> roles) {
      this.roles = roles;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Boolean getPublicService() {
      return publicService;
   }

   public Boolean isPublicService() {
      return publicService;
   }

   public void setPublicService(Boolean publicService) {
      this.publicService = publicService;
   }

   public void addIpMatcher(IPMatcher ip) {
      ips.add(ip);
   }

   public void removeIpMatcher(IPMatcher ip) {
      if (ips.contains(ip)) {
         ips.remove(ip);
      }
   }

   public void addUser(String user) {
      users.add(user);
   }

   public void removeUser(String user) {
      if (users.contains(user)) {
         users.remove(user);
      }
   }

   public void addRole(String role) {
      roles.add(role);
   }

   public void removeRole(String role) {
      if (roles.contains(role)) {
         roles.remove(role);
      }
   }

   @Override
   public String toString() {
      return name;
   }
}
