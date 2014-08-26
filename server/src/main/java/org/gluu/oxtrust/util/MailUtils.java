package org.gluu.oxtrust.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class MailUtils {

	private static final Logger log = Logger.getLogger(MailUtils.class);

	private String hostName;
	private int port = 25;
	private boolean requiresSsl = false;
	private boolean requiresAuthentication = false;
	private String userName;
	private String password;
	private long connectionTimeout = 5000;

	public MailUtils(String hostName, int port, boolean requiresSsl, boolean requiresAuthentication, String userName, String password) {
		this.hostName = hostName;
		this.port = port;
		this.requiresSsl = requiresSsl;
		this.requiresAuthentication = requiresAuthentication;
		this.userName = userName;
		this.password = password;
	}

	public MailUtils(String hostName, String port, boolean requiresSsl, boolean requiresAuthentication, String userName, String password) {
		this.hostName = hostName;
		try {
			this.port = Integer.parseInt(port);
		} catch (Exception ex) {
			this.port = 25;
		}
		this.requiresSsl = requiresSsl;
		this.requiresAuthentication = requiresAuthentication;
		this.userName = userName;
		this.password = password;
	}

	public long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void sendMail(String from, String to, String subject, String message) throws MessagingException {
		log.debug("HostName: " + this.hostName + " Port: " + this.port + " ConnectionTimeOut: " + this.connectionTimeout);
		log.debug("UserName: " + this.userName + " Password: " + this.password);
		Properties props = new Properties();
		props.put("mail.smtp.host", this.hostName);
		props.put("mail.smtp.port", this.port);
		props.put("mail.from", from);
		props.put("mail.smtp.connectiontimeout", this.connectionTimeout);
		props.put("mail.smtp.timeout", this.connectionTimeout);

		if (requiresSsl) {
			// props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}

		Session session = null;
		if (requiresAuthentication) {
			props.put("mail.smtp.auth", "true");

			final String userName = this.userName;
			final String password = this.password;

			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
			});
		} else {
			Session.getInstance(props, null);
		}

		MimeMessage msg = new MimeMessage(session);
		msg.setFrom();
		msg.setRecipients(Message.RecipientType.TO, to);
		msg.setSubject(subject);
		msg.setSentDate(new Date());
		msg.setText(message + "\n");
		Transport.send(msg);
	}

	public static void main(String args[]) {
		System.out.println("Start....");
		int active = 4;
		MailUtils sendMail = null;
		try {
			switch (active) {
			case 1:
				sendMail = new MailUtils("localhost", 25, false, false, "", "");
				sendMail.sendMail("abc@def.com", "def@ghi.com", "Test Subject", "Test Message");
				break;

			case 2:
				sendMail = new MailUtils("smtp.gmail.com", 465, false, false, "", "");
				sendMail.sendMail("abc@def.com", "def@ghi.com", "Test Subject", "Test Message");
				break;

			case 3:
				sendMail = new MailUtils("smtp.gmail.com", 465, false, true, "", "");
				sendMail.sendMail("abc@def.com", "def@ghi.com", "Test Subject", "Test Message");
				break;

			default:
				sendMail = new MailUtils("smtp.gmail.com", 465, true, true, "", "");
				sendMail.sendMail("abc@def.com", "def@ghi.com", "Test Subject", "Test Message");
			}

		} catch (AuthenticationFailedException mex) {
			System.out.println("Authentication failed, please check your user name and password");
			System.out.println("send failed, exception: " + mex); // TODO: Log4J
		} catch (MessagingException mex) {
			System.out.println("Could not connect to SMTP host.");
			System.out.println("send failed, exception: " + mex);
		}
		System.out.println("...End");
	}
}
