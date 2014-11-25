// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.mail;

import javax.mail.Authenticator;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.Properties;

import static jodd.util.StringPool.TRUE;

/**
 * Represents simple plain SMTP server for sending emails.
 */
public class SmtpServer<T extends SmtpServer> implements SendMailSessionProvider {

	protected static final String MAIL_HOST = "mail.host";
	protected static final String MAIL_SMTP_HOST = "mail.smtp.host";
	protected static final String MAIL_SMTP_PORT = "mail.smtp.port";
	protected static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
	protected static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";

	protected static final String MAIL_SMTP_CONNECTIONTIMEOUT ="mail.smtp.connectiontimeout";
	protected static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
	protected static final String MAIL_SMTP_WRITETIMEOUT = "mail.smtp.writetimeout";

	protected static final String PROTOCOL_SMTP = "smtp";

	protected static final int DEFAULT_SMTP_PORT = 25;

	protected final String host;
	protected final int port;
	protected Authenticator authenticator;
	protected int timeout = 0;
	private Properties additionalProperties;

	// ---------------------------------------------------------------- create

	public static SmtpServer create(String host) {
		return new SmtpServer(host, DEFAULT_SMTP_PORT);
	}

	public static SmtpServer create(String host, int port) {
		return new SmtpServer(host, port);
	}

	/**
	 * SMTP server defined with its host and default port.
	 */
	public SmtpServer(String host) {
		this.host = host;
		this.port = DEFAULT_SMTP_PORT;
	}
	/**
	 * SMTP server defined with its host and port.
	 */
	public SmtpServer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	// ---------------------------------------------------------------- builder

	public T authenticateWith(String username, String password) {
		this.authenticator = new SimpleAuthenticator(username, password);
		return (T) this;
	}

	public T authenticateWith(Authenticator authenticator) {
		this.authenticator = authenticator;
		return (T) this;
	}

	/**
	 * Defines timeout value in milliseconds for all mail-related operations.
	 */
	public T timeout(int timeout) {
		this.timeout = timeout;
		return (T) this;
	}

	public T properties(Properties properties) {
		this.additionalProperties = properties;
		return (T) this;
	}

	public T property(String name, String value) {
		if (additionalProperties == null) {
			additionalProperties = new Properties();
		}
		this.additionalProperties.put(name, value);
		return (T) this;
	}

	// ---------------------------------------------------------------- properties

	/**
	 * Creates mail session properties.
	 */
	protected Properties createSessionProperties() {
		Properties props = new Properties();

		props.setProperty(MAIL_TRANSPORT_PROTOCOL, PROTOCOL_SMTP);
		props.setProperty(MAIL_HOST, host);
		props.setProperty(MAIL_SMTP_HOST, host);
		props.setProperty(MAIL_SMTP_PORT, String.valueOf(port));

		if (authenticator != null) {
			props.setProperty(MAIL_SMTP_AUTH, TRUE);
		}

		if (timeout > 0) {
			String timeoutValue = String.valueOf(timeout);
			props.put(MAIL_SMTP_CONNECTIONTIMEOUT, timeoutValue);
			props.put(MAIL_SMTP_TIMEOUT, timeoutValue);
			props.put(MAIL_SMTP_WRITETIMEOUT, timeoutValue);
		}

		return props;
	}

	/**
	 * {@inheritDoc}
	 */
	public SendMailSession createSession() {
		Properties sessionProperties = createSessionProperties();

		if (additionalProperties != null) {
			sessionProperties.putAll(additionalProperties);
		}

		Session mailSession = Session.getInstance(sessionProperties, authenticator);
		Transport mailTransport;
		try {
			mailTransport = getTransport(mailSession);
		} catch (NoSuchProviderException nspex) {
			throw new MailException(nspex);
		}
		return new SendMailSession(mailSession, mailTransport);
	}

	/**
	 * Returns mail transport.
	 */
	protected Transport getTransport(Session session) throws NoSuchProviderException {
		return session.getTransport(PROTOCOL_SMTP);
	}

	// ---------------------------------------------------------------- getters

	/**
	 * Returns SMTP host address.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns authenticator.
	 */
	public Authenticator getAuthenticator() {
		return authenticator;
	}

	/**
	 * Returns current port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns timeout in milliseconds.
	 */
	public int getTimeout() {
		return timeout;
	}

}
