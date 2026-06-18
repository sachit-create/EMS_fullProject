package demoPro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.faces.context.FacesContext;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class AiService implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String AI_CONFIG_PATH = "/WEB-INF/ai.properties";
	private static final int RETRY_ATTEMPTS = 3;
	private static final long RETRY_DELAY_MS = 500L;
	private static volatile Properties aiConfig;

	private AiService() {
	}

	public static String generateEmployeeSummary(String code, String name, String fatherName, String email, String gender, double salary) throws IOException, ParseException {
		String provider = getConfig("AI_PROVIDER", "ollama");
		if (!"ollama".equalsIgnoreCase(provider)) {
			throw new IllegalStateException("Unsupported AI provider: " + provider);
		}

		String model = getRequired("AI_MODEL");
		int timeoutSeconds = Integer.parseInt(getConfig("AI_TIMEOUT_SECONDS", "30"));

		String prompt =
			"<|system|>\nYou are an HR Data Bot. Write 4 short bullet points. Use ONLY the data provided. Do not use generic names.<|end|>\n" +
			"<|user|>\nName: " + safe(name) + ", Code: " + safe(code) + ", Father: " + safe(fatherName) + ", Email: " + safe(email) + ", Salary:  ₹" + salary + ". Format: PROFILE, PAYROLL, DATA, ACTION.<|end|>\n" +
			"<|assistant|>\n";

		JSONObject body = new JSONObject();
		body.put("model", model);
		body.put("prompt", prompt);
		body.put("stream", Boolean.FALSE);
		
		JSONObject options = new JSONObject();
		options.put("num_predict", 100); // Stop after 100 tokens to save time
		options.put("temperature", 0.3); // Lower temperature = faster/more stable
		options.put("top_k", 20);
		body.put("options", options);

		List<String> baseUrls = resolveBaseUrls();
		IOException lastIOException = null;

		for (String baseUrl : baseUrls) {
			try {
				return generateWithBaseUrlWithRetry(baseUrl, model, timeoutSeconds, body);
			} catch (IOException e) {
				lastIOException = e;
				System.out.println("ECCS AI: Failed using " + baseUrl + " -> " + e.getMessage());
			}
		}

		if (lastIOException != null) {
			throw lastIOException;
		}
		throw new IOException("AI provider is unavailable.");
	}

	private static String generateWithBaseUrlWithRetry(String baseUrl, String model, int timeoutSeconds, JSONObject body)
		throws IOException, ParseException {
		IOException lastIOException = null;

		for (int attempt = 1; attempt <= RETRY_ATTEMPTS; attempt++) {
			try {
				return generateWithBaseUrl(baseUrl, model, timeoutSeconds, body);
			} catch (IOException e) {
				lastIOException = e;
				System.out.println("ECCS AI: Attempt " + attempt + " failed for " + baseUrl + " -> " + e.getMessage());
				if (attempt < RETRY_ATTEMPTS) {
					sleepQuietly(RETRY_DELAY_MS);
				}
			}
		}

		if (lastIOException != null) {
			throw lastIOException;
		}
		throw new IOException("AI request failed.");
	}

	private static String generateWithBaseUrl(String baseUrl, String model, int timeoutSeconds, JSONObject body)
		throws IOException, ParseException {
		HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl.replaceAll("/$", "") + "/api/generate").openConnection();
		System.out.println("ECCS AI: Connecting to " + connection.getURL());
		connection.setRequestMethod("POST");
		connection.setConnectTimeout(timeoutSeconds * 1000);
		connection.setReadTimeout(timeoutSeconds * 1000);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

		try (OutputStream outputStream = connection.getOutputStream()) {
			outputStream.write(body.toJSONString().getBytes(StandardCharsets.UTF_8));
		}

		int status = connection.getResponseCode();
		InputStream responseStream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
		String response = readAll(responseStream);
		if (status < 200 || status >= 300) {
			throw new IOException("AI provider returned error status: " + status + " from " + baseUrl);
		}

		Object parsed = new JSONParser().parse(response);
		if (!(parsed instanceof JSONObject)) {
			throw new IOException("Unexpected AI response format.");
		}

		Object generated = ((JSONObject) parsed).get("response");
		if (generated == null) {
			throw new IOException("AI response missing content.");
		}

		return String.valueOf(generated).trim();
	}

	private static List<String> resolveBaseUrls() {
		String configuredBaseUrl = getRequired("AI_BASE_URL");
		String fallbackBaseUrl = getConfig("AI_BASE_URL_FALLBACK", null);
		List<String> candidates = new ArrayList<String>();
		candidates.add(configuredBaseUrl);

		if (fallbackBaseUrl != null && !fallbackBaseUrl.trim().isEmpty()) {
			candidates.add(fallbackBaseUrl.trim());
		}

		for (String builtInFallback : Arrays.asList("http://ollama:11434", "http://eccs-ai:11434", "http://localhost:11434")) {
			if (!candidates.contains(builtInFallback)) {
				candidates.add(builtInFallback);
			}
		}

		return candidates;
	}

	private static String readAll(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		return builder.toString();
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}

	private static void sleepQuietly(long delayMs) {
		try {
			Thread.sleep(delayMs);
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
		}
	}

	private static String getRequired(String key) {
		String value = getConfig(key, null);
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalStateException("Missing AI configuration: " + key);
		}
		return value.trim();
	}

	private static String getConfig(String key, String defaultValue) {
		// 1. Check Environment Variables first (Best for Docker/AWS)
		String envValue = System.getenv(key);
		if (envValue != null && !envValue.trim().isEmpty()) {
			return envValue.trim();
		}

		// 2. Check System Properties
		String systemValue = System.getProperty(key);
		if (systemValue != null && !systemValue.trim().isEmpty()) {
			return systemValue.trim();
		}

		// 3. Fallback to properties file
		String fileValue = getFileConfig(key);
		if (fileValue != null && !fileValue.trim().isEmpty()) {
			return fileValue.trim();
		}

		return defaultValue;
	}

	private static String getFileConfig(String key) {
		Properties properties = loadAiConfig();
		if (properties == null) {
			return null;
		}
		return properties.getProperty(key);
	}

	private static Properties loadAiConfig() {
		Properties cached = aiConfig;
		if (cached != null) {
			return cached;
		}

		synchronized (AiService.class) {
			if (aiConfig != null) {
				return aiConfig;
			}

			Properties properties = new Properties();
			try {
				FacesContext facesContext = FacesContext.getCurrentInstance();
				if (facesContext != null) {
					try (InputStream inputStream = facesContext.getExternalContext().getResourceAsStream(AI_CONFIG_PATH)) {
						if (inputStream != null) {
							properties.load(inputStream);
						}
					}
				}
			} catch (Exception e) {
				// Ignore file loading issues and fall back to env/system config.
			}

			aiConfig = properties;
			return aiConfig;
		}
	}
}
