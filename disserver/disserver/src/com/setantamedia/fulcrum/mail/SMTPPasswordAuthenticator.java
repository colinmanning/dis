package com.setantamedia.fulcrum.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


public class SMTPPasswordAuthenticator extends Authenticator {

	private SMTPServer server = null;

	public SMTPPasswordAuthenticator() {

	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(server.getUsername(), server.getPassword());
	}

	public SMTPServer getServer() {
		return server;
	}

	public void setServer(SMTPServer server) {
		this.server = server;
	}
}
