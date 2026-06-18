package demoPro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {

	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String DB_HOST = getConfig("DB_HOST", "127.0.0.1");
	private static final String DB_PORT = getConfig("DB_PORT", "3306");
	private static final String DB_NAME = getConfig("DB_NAME", "employee_db");
	private static final String DB_USER = getConfig("DB_USER", "employee_app");
	private static final String DB_PASSWORD = getConfig("DB_PASSWORD", "employee_app_123");
	private static final String JDBC_URL =
		"jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
			+ "?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true&connectTimeout=10000";

	static {
		try {
			Class.forName(DRIVER);
			System.out.println("ECCS: JDBC Driver Loaded");
			System.out.println("ECCS: Connecting to: " + JDBC_URL);
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError("MySQL JDBC driver not found: " + e.getMessage());
		}
	}

	private DataBaseConnection() {
	}

	private static String getConfig(String key, String defaultValue) {
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

	public static Connection getDefaultConnection() throws SQLException {
		return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
	}
}
