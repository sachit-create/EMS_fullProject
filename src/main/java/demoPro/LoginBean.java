package demoPro;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_EMPLOYEE = "EMPLOYEE";
	private static final String SESSION_LOGGED_IN = "loginBean.loggedIn";
	private static final String SESSION_ROLE = "loginBean.role";
	private static final String SESSION_DISPLAY_NAME = "loginBean.displayName";
	private static final String SESSION_USERNAME = "loginBean.username";
	private static final String SESSION_EMPLOYEE_CODE = "loginBean.employeeCode";
	private static final String SESSION_EMPLOYEE_EMAIL = "loginBean.employeeEmail";
	private static final String SESSION_EMPLOYEE_NAME = "loginBean.employeeName";
	private static final String SESSION_EMPLOYEE_FATHER_NAME = "loginBean.employeeFatherName";
	private static final String SESSION_EMPLOYEE_GENDER = "loginBean.employeeGender";
	private static final String SESSION_EMPLOYEE_SALARY = "loginBean.employeeSalary";
	private static final String SESSION_EMPLOYEE_PHOTO = "loginBean.employeePhotoDataUri";

	private String username;
	private String password;
	private String displayName;
	private String employeeCode;
	private String employeeEmail;
	private String employeeName;
	private String employeeFatherName;
	private String employeeGender;
	private double employeeSalary;
	private String employeePhotoDataUri;
	private String loginFormRole = "admin";
	private String role;
	private boolean loggedIn;
	private String aiSummary;

	public String login() {
		return loginAdmin();
	}

	public void generateAiSummary() {
		if (!loggedIn || employeeCode == null || employeeCode.trim().isEmpty()) {
			addMessage(FacesMessage.SEVERITY_ERROR, "AI failed", "Session expired or invalid login.");
			return;
		}

		try {
			aiSummary = AiService.generateEmployeeSummary(employeeCode, employeeName, employeeFatherName, employeeEmail, employeeGender, employeeSalary);
			addMessage(FacesMessage.SEVERITY_INFO, "AI summary ready", "Generated your profile summary.");
		} catch (Exception e) {
			aiSummary = "AI summary could not be generated right now. Check AI configuration and Ollama service.";
			addMessage(FacesMessage.SEVERITY_WARN, "AI unavailable", e.getMessage());
			e.printStackTrace();
		}
	}

	public String loginAdmin() {
		try {
			if (AdminAuthService.matches(username, password)) {
				loggedIn = true;
				role = ROLE_ADMIN;
				displayName = username;
				clearEmployeeState();
				storeSessionState();
				password = "";
				return "dashboard?faces-redirect=true";
			}
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Login failed", e.getMessage());
			e.printStackTrace();
			return null;
		}

		password = "";
		addMessage(FacesMessage.SEVERITY_ERROR, "Invalid login", "Check username and password.");
		return null;
	}

	public String loginEmployee() {
		String sql = "SELECT employee_code, name, father_name, email, gender, salary, employee_photo, employee_photo_type FROM add_emp WHERE employee_code = ? OR CAST(id AS CHAR) = ?";
		String code = employeeCode == null ? "" : employeeCode.trim();
		String numericId = code.matches("\\d+") ? code : "";

		try (
			Connection conn = DataBaseConnection.getDefaultConnection();
			PreparedStatement ps = conn.prepareStatement(sql)
		) {
			ps.setString(1, code);
			ps.setString(2, numericId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					loggedIn = true;
					role = ROLE_EMPLOYEE;
					employeeCode = rs.getString("employee_code");
					employeeName = rs.getString("name");
					employeeFatherName = rs.getString("father_name");
					employeeEmail = rs.getString("email");
					employeeGender = rs.getString("gender");
					employeeSalary = rs.getDouble("salary");
					employeePhotoDataUri = buildPhotoDataUri(
						rs.getBytes("employee_photo"),
						rs.getString("employee_photo_type"),
						employeeName
					);
					displayName = employeeName;
					clearAdminState();
					storeSessionState();
					password = "";
					return "employeeProfile?faces-redirect=true";
				}
			}
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Login failed", e.getMessage());
			e.printStackTrace();
			return null;
		}

		addMessage(FacesMessage.SEVERITY_ERROR, "Invalid employee ID", "Check your employee ID and try again.");
		return null;
	}

	public String logout() {
		clearSessionState();
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		username = "";
		password = "";
		displayName = "";
		employeeCode = "";
		employeeEmail = "";
		employeeName = "";
		employeeFatherName = "";
		employeeGender = "";
		employeeSalary = 0.0;
		employeePhotoDataUri = "";
		loginFormRole = "admin";
		role = null;
		loggedIn = false;
		clearSessionState();
		return "loginPage?faces-redirect=true";
	}

	public void ensureAdminLoggedIn() {
		restoreSessionState();
		if (!loggedIn || !ROLE_ADMIN.equals(role)) {
			redirect("loginPage.xhtml");
		}
	}

	public void ensureEmployeeLoggedIn() {
		restoreSessionState();
		if (!loggedIn || !ROLE_EMPLOYEE.equals(role)) {
			redirect("loginPage.xhtml");
		}
	}

	public void redirectIfLoggedIn() {
		restoreSessionState();
		if (loggedIn) {
			if (ROLE_EMPLOYEE.equals(role)) {
				redirect("employeeProfile.xhtml");
				return;
			}
			redirect("dashboard.xhtml");
		}
	}

	public String getCurrentRole() {
		return role;
	}

	public String getLoginFormRole() {
		if (loginFormRole == null || loginFormRole.trim().isEmpty()) {
			loginFormRole = "admin";
		}
		return loginFormRole;
	}

	public void setLoginFormRole(String loginFormRole) {
		if (loginFormRole == null || loginFormRole.trim().isEmpty()) {
			this.loginFormRole = "admin";
			return;
		}
		String normalized = loginFormRole.trim().toLowerCase();
		this.loginFormRole = "employee".equals(normalized) ? "employee" : "admin";
	}

	private void redirect(String page) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();

		try {
			externalContext.redirect(externalContext.getRequestContextPath() + "/" + page);
			context.responseComplete();
		} catch (IOException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Navigation failed", e.getMessage());
			e.printStackTrace();
		}
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	private void storeSessionState() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		externalContext.getSessionMap().put(SESSION_LOGGED_IN, Boolean.valueOf(loggedIn));
		externalContext.getSessionMap().put(SESSION_ROLE, role);
		externalContext.getSessionMap().put(SESSION_DISPLAY_NAME, displayName);
		externalContext.getSessionMap().put(SESSION_USERNAME, username);
		externalContext.getSessionMap().put(SESSION_EMPLOYEE_CODE, employeeCode);
		externalContext.getSessionMap().put(SESSION_EMPLOYEE_EMAIL, employeeEmail);
		externalContext.getSessionMap().put(SESSION_EMPLOYEE_NAME, employeeName);
		externalContext.getSessionMap().put(SESSION_EMPLOYEE_FATHER_NAME, employeeFatherName);
		externalContext.getSessionMap().put(SESSION_EMPLOYEE_GENDER, employeeGender);
		externalContext.getSessionMap().put(SESSION_EMPLOYEE_SALARY, Double.valueOf(employeeSalary));
		externalContext.getSessionMap().put(SESSION_EMPLOYEE_PHOTO, employeePhotoDataUri);
	}

	private void restoreSessionState() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		Object sessionLoggedIn = externalContext.getSessionMap().get(SESSION_LOGGED_IN);
		Object sessionRole = externalContext.getSessionMap().get(SESSION_ROLE);
		if (sessionLoggedIn == null && sessionRole == null) {
			return;
		}
		loggedIn = Boolean.TRUE.equals(sessionLoggedIn);
		role = sessionRole == null ? role : String.valueOf(sessionRole);
		Object sessionDisplayName = externalContext.getSessionMap().get(SESSION_DISPLAY_NAME);
		Object sessionUsername = externalContext.getSessionMap().get(SESSION_USERNAME);
		Object sessionEmployeeCode = externalContext.getSessionMap().get(SESSION_EMPLOYEE_CODE);
		Object sessionEmployeeEmail = externalContext.getSessionMap().get(SESSION_EMPLOYEE_EMAIL);
		Object sessionEmployeeName = externalContext.getSessionMap().get(SESSION_EMPLOYEE_NAME);
		Object sessionEmployeeFatherName = externalContext.getSessionMap().get(SESSION_EMPLOYEE_FATHER_NAME);
		Object sessionEmployeeGender = externalContext.getSessionMap().get(SESSION_EMPLOYEE_GENDER);
		Object sessionEmployeeSalary = externalContext.getSessionMap().get(SESSION_EMPLOYEE_SALARY);
		Object sessionEmployeePhoto = externalContext.getSessionMap().get(SESSION_EMPLOYEE_PHOTO);

		displayName = sessionDisplayName == null ? displayName : String.valueOf(sessionDisplayName);
		username = sessionUsername == null ? username : String.valueOf(sessionUsername);
		employeeCode = sessionEmployeeCode == null ? employeeCode : String.valueOf(sessionEmployeeCode);
		employeeEmail = sessionEmployeeEmail == null ? employeeEmail : String.valueOf(sessionEmployeeEmail);
		employeeName = sessionEmployeeName == null ? employeeName : String.valueOf(sessionEmployeeName);
		employeeFatherName = sessionEmployeeFatherName == null ? employeeFatherName : String.valueOf(sessionEmployeeFatherName);
		employeeGender = sessionEmployeeGender == null ? employeeGender : String.valueOf(sessionEmployeeGender);
		if (sessionEmployeeSalary instanceof Number) {
			employeeSalary = ((Number) sessionEmployeeSalary).doubleValue();
		}
		employeePhotoDataUri = sessionEmployeePhoto == null ? employeePhotoDataUri : String.valueOf(sessionEmployeePhoto);
	}

	private void clearSessionState() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		externalContext.getSessionMap().remove(SESSION_LOGGED_IN);
		externalContext.getSessionMap().remove(SESSION_ROLE);
		externalContext.getSessionMap().remove(SESSION_DISPLAY_NAME);
		externalContext.getSessionMap().remove(SESSION_USERNAME);
		externalContext.getSessionMap().remove(SESSION_EMPLOYEE_CODE);
		externalContext.getSessionMap().remove(SESSION_EMPLOYEE_EMAIL);
		externalContext.getSessionMap().remove(SESSION_EMPLOYEE_NAME);
		externalContext.getSessionMap().remove(SESSION_EMPLOYEE_FATHER_NAME);
		externalContext.getSessionMap().remove(SESSION_EMPLOYEE_GENDER);
		externalContext.getSessionMap().remove(SESSION_EMPLOYEE_SALARY);
		externalContext.getSessionMap().remove(SESSION_EMPLOYEE_PHOTO);
	}

	private void clearAdminState() {
		username = "";
		password = "";
	}

	private void clearEmployeeState() {
		employeeCode = "";
		employeeEmail = "";
		employeeName = "";
		employeeFatherName = "";
		employeeGender = "";
		employeeSalary = 0.0;
		employeePhotoDataUri = "";
	}

	private String buildPhotoDataUri(byte[] photoBytes, String photoType, String employeeName) {
		if (photoBytes != null && photoBytes.length > 0) {
			String mimeType = (photoType == null || photoType.trim().isEmpty()) ? "image/png" : photoType.trim();
			return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(photoBytes);
		}

		String label = (employeeName == null || employeeName.trim().isEmpty())
			? "E"
			: employeeName.trim().substring(0, 1).toUpperCase();

		String svg =
			"<svg xmlns='http://www.w3.org/2000/svg' width='160' height='160' viewBox='0 0 160 160'>" +
			"<rect width='160' height='160' rx='80' fill='#e5e7eb'/>" +
			"<circle cx='80' cy='62' r='28' fill='#9ca3af'/>" +
			"<path d='M36 136c6-28 28-42 44-42s38 14 44 42' fill='#9ca3af'/>" +
			"<text x='80' y='95' text-anchor='middle' font-family='Arial, sans-serif' font-size='34' font-weight='700' fill='white'>" +
			label +
			"</text></svg>";
		return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getEmployeeEmail() {
		return employeeEmail;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public String getEmployeeFatherName() {
		return employeeFatherName;
	}

	public String getEmployeeGender() {
		return employeeGender;
	}

	public double getEmployeeSalary() {
		return employeeSalary;
	}

	public String getEmployeePhotoDataUri() {
		return employeePhotoDataUri;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public String getAiSummary() {
		return aiSummary;
	}
}
