package demoPro;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Base64;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.UploadedFile;

@ManagedBean(name = "employeeAdmin")
@RequestScoped
public class EmployeeAdminBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String code;
	private int id;
	private String employeeCode;
	private String name;
	private String fatherName;
	private String email;
	private String gender;
	private double salary;
	private String photoDataUri;
	private UploadedFile employeePhoto;
	private boolean loaded;
	private String aiSummary;

	public void loadEmployee() {
		if (loaded) {
			return;
		}

		String resolvedCode = resolveCode();
		if (resolvedCode == null || resolvedCode.isEmpty()) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Missing employee code", "Employee code is required.");
			return;
		}

		String sql = "SELECT id, employee_code, name, father_name, email, gender, salary, employee_photo, employee_photo_type FROM add_emp WHERE employee_code = ? OR CAST(id AS CHAR) = ?";

		try (
			Connection conn = DataBaseConnection.getDefaultConnection();
			PreparedStatement ps = conn.prepareStatement(sql)
		) {
			ps.setString(1, resolvedCode);
			ps.setString(2, resolvedCode.matches("\\d+") ? resolvedCode : "");

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					id = rs.getInt("id");
					employeeCode = rs.getString("employee_code");
					name = rs.getString("name");
					fatherName = rs.getString("father_name");
					email = rs.getString("email");
					gender = rs.getString("gender");
					salary = rs.getDouble("salary");
					photoDataUri = buildPhotoDataUri(
						rs.getBytes("employee_photo"),
						rs.getString("employee_photo_type"),
						name
					);
					loaded = true;
					return;
				}
			}
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Load failed", e.getMessage());
			e.printStackTrace();
			return;
		}

		addMessage(FacesMessage.SEVERITY_ERROR, "Not found", "No employee found for that code.");
	}

	public String updateEmployee() {
		String resolvedCode = resolveCode();
		if (resolvedCode == null || resolvedCode.isEmpty()) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Update failed", "Employee code is missing.");
			return null;
		}

		String sql = "UPDATE add_emp SET name = ?, father_name = ?, email = ?, gender = ?, salary = ?, employee_photo = COALESCE(?, employee_photo), employee_photo_type = COALESCE(?, employee_photo_type) WHERE employee_code = ?";

		try (
			Connection conn = DataBaseConnection.getDefaultConnection();
			PreparedStatement ps = conn.prepareStatement(sql)
		) {
			ps.setString(1, name);
			ps.setString(2, fatherName);
			ps.setString(3, email);
			ps.setString(4, gender);
			ps.setDouble(5, salary);
			byte[] photoBytes = getPhotoBytes();
			if (photoBytes != null) {
				ps.setBytes(6, photoBytes);
			} else {
				ps.setNull(6, Types.BLOB);
			}
			String photoType = getPhotoType();
			if (photoType != null && !photoType.trim().isEmpty()) {
				ps.setString(7, photoType.trim());
			} else {
				ps.setNull(7, Types.VARCHAR);
			}
			ps.setString(8, resolvedCode);

			int rows = ps.executeUpdate();
			if (rows > 0) {
				loaded = false;
				photoDataUri = null;
				addMessage(FacesMessage.SEVERITY_INFO, "Updated", "Employee profile updated successfully.");
				return "employeeAdminProfile?faces-redirect=true&code=" + resolvedCode;
			}
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Update failed", e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public String deleteEmployee() {
		String resolvedCode = resolveCode();
		if (resolvedCode == null || resolvedCode.isEmpty()) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Delete failed", "Employee code is missing.");
			return null;
		}

		String sql = "DELETE FROM add_emp WHERE employee_code = ?";

		try (
			Connection conn = DataBaseConnection.getDefaultConnection();
			PreparedStatement ps = conn.prepareStatement(sql)
		) {
			ps.setString(1, resolvedCode);
			int rows = ps.executeUpdate();
			if (rows > 0) {
				addMessage(FacesMessage.SEVERITY_INFO, "Deleted", "Employee deleted successfully.");
				return "showEmployees?faces-redirect=true";
			}
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Delete failed", e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public String generateAiSummary() {
		String resolvedCode = resolveCode();
		if (resolvedCode == null || resolvedCode.isEmpty()) {
			addMessage(FacesMessage.SEVERITY_ERROR, "AI failed", "Employee code is missing.");
			return null;
		}

		// Ensure data is loaded from DB if bean was just recreated (RequestScoped)
		if (name == null || name.trim().isEmpty()) {
			loadEmployee();
		}

		try {
			aiSummary = AiService.generateEmployeeSummary(resolvedCode, name, fatherName, email, gender, salary);
			addMessage(FacesMessage.SEVERITY_INFO, "AI summary ready", "Generated a short profile summary.");
		} catch (Exception e) {
			aiSummary = "AI summary could not be generated right now. Check AI configuration and Ollama service.";
			addMessage(FacesMessage.SEVERITY_WARN, "AI unavailable", e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	private String resolveCode() {
		if (employeeCode != null && !employeeCode.trim().isEmpty()) {
			return employeeCode.trim();
		}
		if (code != null && !code.trim().isEmpty()) {
			return code.trim();
		}
		return null;
	}

	private byte[] getPhotoBytes() {
		if (employeePhoto == null) {
			return null;
		}
		byte[] contents = employeePhoto.getContents();
		return contents != null && contents.length > 0 ? contents : null;
	}

	private String getPhotoType() {
		if (employeePhoto == null) {
			return null;
		}
		return employeePhoto.getContentType();
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
		return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getId() {
		return id;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFatherName() {
		return fatherName;
	}

	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	public String getPhotoDataUri() {
		return photoDataUri;
	}

	public UploadedFile getEmployeePhoto() {
		return employeePhoto;
	}

	public void setEmployeePhoto(UploadedFile employeePhoto) {
		this.employeePhoto = employeePhoto;
	}

	public String getAiSummary() {
		return aiSummary;
	}

	public void setAiSummary(String aiSummary) {
		this.aiSummary = aiSummary;
	}
}
