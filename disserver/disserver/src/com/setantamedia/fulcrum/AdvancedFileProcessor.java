package com.setantamedia.fulcrum;

import com.setantamedia.fulcrum.config.MailServer;
import com.setantamedia.fulcrum.db.DbManager;
import com.setantamedia.fulcrum.locationmonitor.FileProcessor;
import com.setantamedia.fulcrum.mail.SMTPServer;
import java.nio.file.Path;

public class AdvancedFileProcessor extends FileProcessor {

	public final static String PARAM_EMAIL_FROM = "emailFrom";

	protected SMTPServer smtpServer = null;
	protected String sessionId = null;

	protected DbManager dbManager = null;

	public AdvancedFileProcessor() {
		super();
	}

	@Override
	public void init() {
		super.init();

		MailServer mailConfig = fulcrumConfig.getMailServer();
		smtpServer = new SMTPServer();
		smtpServer.setHost(mailConfig.getAddress());
		smtpServer.setPort(mailConfig.getPort());
		smtpServer.setUsername(mailConfig.getUsername());
		smtpServer.setPassword(mailConfig.getPassword());
		dbManager = databases.get(dbName).getManager();
	}

	@Override
	public void fileCreated(Path file) {
		if (ignoreFile(file)) return;
		try {
			// logic handled in sub classes
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SMTPServer getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(SMTPServer smtpServer) {
		this.smtpServer = smtpServer;
	}

   @Override
   public void directoryModified(Path directory) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void terminate() {

   }
}
