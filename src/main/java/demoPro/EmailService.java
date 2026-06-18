package demoPro;

import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.faces.context.FacesContext;

public final class EmailService {

	private static final String SMTP_CONFIG_PATH = "/WEB-INF/smtp.properties";
	private static volatile Properties smtpConfig;

	private EmailService() {
	}

	public static void sendEmployeeCode(String toEmail, String employeeCode, String employeeName) throws MessagingException {
		Session session = Session.getInstance(buildProperties(), buildAuthenticator());
		session.setDebug(Boolean.parseBoolean(getConfig("SMTP_DEBUG", "false")));

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(getRequired("SMTP_FROM")));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
		message.setSubject("Your Employee ID");
		message.setText(
			"Hello " + employeeName + ",\n\n"
				+ "Your employee ID is: " + employeeCode + "\n\n"
				+ "Keep this ID safe. You can use it later to access your employee portal.\n"
		);

		Transport.send(message);
	}

	private static Properties buildProperties() {
		Properties properties = new Properties();
		String host = getRequired("SMTP_HOST");
		String port = getRequired("SMTP_PORT");
		boolean sslEnabled = Boolean.parseBoolean(getConfig("SMTP_SSL", "false"));
		boolean startTlsEnabled = Boolean.parseBoolean(getConfig("SMTP_TLS", "true"));

		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.auth", getConfig("SMTP_AUTH", "true"));
		properties.put("mail.smtp.ssl.enable", String.valueOf(sslEnabled));
		properties.put("mail.smtp.ssl.trust", host);
		properties.put("mail.smtp.ssl.protocols", getConfig("SMTP_SSL_PROTOCOLS", "TLSv1.2"));
		properties.put("mail.smtp.starttls.enable", String.valueOf(!sslEnabled && startTlsEnabled));
		properties.put("mail.smtp.starttls.required", String.valueOf(!sslEnabled && startTlsEnabled));
		properties.put("mail.debug", getConfig("SMTP_DEBUG", "false"));
		return properties;
	}

	private static Authenticator buildAuthenticator() {
		final String username = getRequired("SMTP_USER");
		final String password = getRequired("SMTP_PASSWORD");

		return new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		};
	}

	private static String getRequired(String key) {
		String value = getConfig(key, null);
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalStateException("Missing SMTP configuration: " + key);
		}
		return value.trim();
	}

	private static String getConfig(String key, String defaultValue) {
		String fileValue = getFileConfig(key);
		if (fileValue != null && !fileValue.trim().isEmpty()) {
			return fileValue.trim();
		}

		String systemValue = System.getProperty(key);
		if (systemValue != null && !systemValue.trim().isEmpty()) {
			return systemValue.trim();
		}

		String envValue = System.getenv(key);
		if (envValue != null && !envValue.trim().isEmpty()) {
			return envValue.trim();
		}

		return defaultValue;
	}

	private static String getFileConfig(String key) {
		Properties properties = loadSmtpConfig();
		if (properties == null) {
			return null;
		}
		return properties.getProperty(key);
	}

	private static Properties loadSmtpConfig() {
		Properties cached = smtpConfig;
		if (cached != null) {
			return cached;
		}

		synchronized (EmailService.class) {
			if (smtpConfig != null) {
				return smtpConfig;
			}

			Properties properties = new Properties();
			try {
				FacesContext facesContext = FacesContext.getCurrentInstance();
				if (facesContext != null) {
					try (InputStream inputStream = facesContext.getExternalContext().getResourceAsStream(SMTP_CONFIG_PATH)) {
						if (inputStream != null) {
							properties.load(inputStream);
						}
					}
				}
			} catch (Exception e) {
				// Ignore file loading issues and fall back to env/system config.
			}

			smtpConfig = properties;
			return smtpConfig;
		}
	}
}
