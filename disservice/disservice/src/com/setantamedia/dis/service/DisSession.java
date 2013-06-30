package com.setantamedia.dis.service;

import java.util.Date;
import java.util.HashMap;

import com.setantamedia.fulcrum.common.Utilities;
import com.setantamedia.fulcrum.db.DbSessionData;

/**
 * Maintain a session for a connection to the DIS Server
 * 
 * @author Colin Manning
 *
 */
public class DisSession {

   public final static int SESSION_TIMEOUT = 30 * 60 * 1000; // 3o minutes
	private String username = null;
	private String id = null;
	private Date createTime = null;
	private Date validUntil = null;
	private HashMap<String, Object> attributes = null;
	private Boolean valid = false;
	private DbSessionData dbSessionData = null;
	
	public DisSession() {
		attributes = new HashMap<>();
		valid = false;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		if (username != null && ! "".equals(username)) {
			id = Utilities.generateGuid();
			createTime = new Date();
			validUntil = new Date();
			validUntil.setTime(createTime.getTime() + SESSION_TIMEOUT);
			valid = true;
		}
	}
	
	private void invalidate() {
		valid = false;
	}

	public String getId() {
		return id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public HashMap<String, Object> getAttributes() throws InvalidSessionException  {
      if (!valid) throw new InvalidSessionException();
		return attributes;
	}

	public void setAttributes(HashMap<String, Object> attributes) throws InvalidSessionException {
      if (!valid) throw new InvalidSessionException();
		this.attributes = attributes;
	}
	
	public void addAttribute(String key, Object value) throws InvalidSessionException  {
      if (!valid) throw new InvalidSessionException();
		attributes.put(key, value);
	}
	
	public void removeAttribute(String key) throws InvalidSessionException  {
      if (!valid) throw new InvalidSessionException();
		attributes.remove(key);
	}
	
	public Object getAttribute(String key) throws InvalidSessionException {
	   if (!valid) throw new InvalidSessionException();
		return attributes.get(key);
	}
	
	public Boolean hasAttribute(String key) throws InvalidSessionException  {
      if (!valid) throw new InvalidSessionException();
		return (attributes.get(key) != null);
	}
	
	public Boolean isValid() {
	   // first check for timeout
	   if (new Date().getTime() > validUntil.getTime()) {
	      invalidate();
	   }
		return valid;
	}

   public DbSessionData getDbSessionData() {
      return dbSessionData;
   }

   public void setDbSessionData(DbSessionData dbSessionData) {
      this.dbSessionData = dbSessionData;
   }

   public Date getValidUntil() {
      return validUntil;
   }

   public Boolean getValid() {
      return valid;
   }
	
}
