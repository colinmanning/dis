package com.setantamedia.fulcrum.common;

import java.util.HashMap;

public class MessageBrokerDefinition {
	
	private String name = null;
	private String protocol = null;
	private String port = null;
	private HashMap<String, String> params = new HashMap<String, String>();
	private HashMap<String, Class> topics = new HashMap<String, Class>();
	
	public MessageBrokerDefinition() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, String> getParams() {
		return params;
	}

	public void setParams(HashMap<String, String> params) {
		this.params = params;
	}

	public HashMap<String, Class> getTopics() {
		return topics;
	}

	public void setTopics(HashMap<String, Class> topics) {
		this.topics = topics;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
}
