package com.setantamedia.fulcrum.ws.types;

import com.setantamedia.fulcrum.common.Site;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Colin Manning
 */
public class User {
	private String username = "";
	private String firstName = "";
	private String lastName = "";
	private String email = "";
	private Boolean loginActive = false;
   private Boolean adminUser = false;
   private Boolean guestUser = false;
   private String ticket = null;
   private String sessionId = null;
	private String[] roles = new String[0];
	private Map<String, Object> fields = new HashMap<>();
   private Site[] sites =  new Site[0];

	public User() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public Boolean getLoginActive() {
		return loginActive;
	}

	public void setLoginActive(Boolean loginActive) {
		this.loginActive = loginActive;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

   public Boolean getAdminUser() {
      return adminUser;
   }

   public void setAdminUser(Boolean adminUser) {
      this.adminUser = adminUser;
   }

   public Boolean getGuestUser() {
      return guestUser;
   }

   public void setGuestUser(Boolean guestUser) {
      this.guestUser = guestUser;
   }

   public String getTicket() {
      return ticket;
   }

   public void setTicket(String ticket) {
      this.ticket = ticket;
   }

   public String getSessionId() {
      return sessionId;
   }

   public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
   }

   public Site[] getSites() {
      return sites;
   }

   public void setSites(Site[] sites) {
      this.sites = sites;
   }

}

