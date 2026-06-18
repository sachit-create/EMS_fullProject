package demoPro;

import java.io.InputStream;
import java.util.Properties;

import javax.faces.context.FacesContext;

public final class AdminAuthService {

	private static final String ADMIN_CONFIG_PATH = "/WEB-INF/admin.properties";
	private static volatile Properties adminConfig;

	private AdminAuthService() {
	}

	public static boolean matches(String username, String password) {
		String expectedUser = getRequired("ADMIN_USERNAME");
		String expectedPassword = getRequired("ADMIN_PASSWORD");
		
		if (username == null || password == null) {
			return false;
		}
		
		// Direct plain-text comparison using a timing-safe method
		return safeEquals(expectedUser, username) && safeEquals(expectedPassword, password);
	}

	private static boolean safeEquals(String a, String b) {
		if (a.length() != b.length()) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < a.length(); i++) {
			result |= a.charAt(i) ^ b.charAt(i);
		}
		return result == 0;
	}

	private static String getRequired(String key) {
		String value = getConfig(key, null);
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalStateException("Missing admin configuration: " + key);
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
		Properties properties = loadAdminConfig();
		if (properties == null) {
			return null;
		}
		return properties.getProperty(key);
	}

	private static Properties loadAdminConfig() {
		Properties cached = adminConfig;
		if (cached != null) {
			return cached;
		}

		synchronized (AdminAuthService.class) {
			if (adminConfig != null) {
				return adminConfig;
			}

			Properties properties = new Properties();
			try {
				FacesContext facesContext = FacesContext.getCurrentInstance();
				if (facesContext != null) {
					try (InputStream inputStream = facesContext.getExternalContext().getResourceAsStream(ADMIN_CONFIG_PATH)) {
						if (inputStream != null) {
							properties.load(inputStream);
						}
					}
				}
			} catch (Exception e) {
				// Ignore file loading issues and fall back to env/system config.
			}

			adminConfig = properties;
			return adminConfig;
		}
	}
}
