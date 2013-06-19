package com.setantamedia.fulcrum.common;

/**
 *
 * @author Colin Manning
 */
public class Site {

   private String name = null;
   private String url = null;
   private String title = null;
   private String description = null;
   private String adminUsername = null;
   private Boolean publicSite = false;

   public Site() {

   }

   public String getAdminUsername() {
      return adminUsername;
   }

   public void setAdminUsername(String adminUsername) {
      this.adminUsername = adminUsername;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Boolean getPublicSite() {
      return publicSite;
   }

   public void setPublicSite(Boolean publicSite) {
      this.publicSite = publicSite;
   }

   public String getUrl() {
      return url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

}
