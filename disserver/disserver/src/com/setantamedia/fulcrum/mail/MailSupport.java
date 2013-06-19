package com.setantamedia.fulcrum.mail;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.log4j.Logger;

public class MailSupport {

	private static Logger logger = Logger.getLogger(MailSupport.class);

	public final static String X_MAILER = "DisMailer";

	public MailSupport() {
	}

	public void sendMessage(SMTPServer smtpServer, String to, String from, String subject, String body) {
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpServer.getHost());
			props.put("mail.smtp.port", smtpServer.getPort());
			props.put("mail.smtp.user", smtpServer.getUsername());
			props.put("mail.smtp.password", smtpServer.getPassword());
			props.put("mail.smtp.auth", "true");
			SMTPPasswordAuthenticator smtpAuthenticator = new SMTPPasswordAuthenticator();
			smtpAuthenticator.setServer(smtpServer);

			Session session = Session.getDefaultInstance(props, smtpAuthenticator);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			message.setSubject(subject);
			message.setContent(body, "text/html");
			message.setHeader("X-Mailer", X_MAILER);
			message.setSentDate(new Date());
			Transport.send(message);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean sendMessageWithAttachments(SMTPServer smtpServer, String to, String from, String subject, String body, File[] attachments) {
		boolean result = false;
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpServer.getHost());
			props.put("mail.smtp.port", smtpServer.getPort());
			props.put("mail.smtp.user", smtpServer.getUsername());
			props.put("mail.smtp.password", smtpServer.getPassword());
			props.put("mail.smtp.auth", "true");
			SMTPPasswordAuthenticator smtpAuthenticator = new SMTPPasswordAuthenticator();
			smtpAuthenticator.setServer(smtpServer);

			Session session = Session.getDefaultInstance(props, smtpAuthenticator);

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			message.setSubject(subject);
			message.setHeader("X-Mailer", X_MAILER);

			// create the message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(body, "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// add attachments
			for (File attachment: attachments) {
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(attachment);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(attachment.getName());
				multipart.addBodyPart(messageBodyPart);
			}

			// Put parts in message
			message.setContent(multipart);
			message.setSentDate(new Date());

			// Send the message
			Transport.send(message);
			result = true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
