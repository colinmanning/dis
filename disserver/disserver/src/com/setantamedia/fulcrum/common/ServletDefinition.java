package com.setantamedia.fulcrum.common;

import java.util.HashMap;

public class ServletDefinition {

	private String name = null;
	private String contextPath = null;
	private Class servletClass = null;
	private HashMap<String, String> params = new HashMap<String, String>();
	
	public ServletDefinition() {
		params = new HashMap<String, String>();
	}

	public Class getServletClass() {
		return servletClass;
	}

	public void setServletClass(Class servletClass) {
		this.servletClass = servletClass;
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}
	
	public void setParam(String name, String value) {
		
	}
	
	public String getParam(String name) { 
		return params.get(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
}
