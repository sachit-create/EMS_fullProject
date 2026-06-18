package demoPro;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;

@ManagedBean(name = "employeeIdRecovery")
@RequestScoped
public class EmployeeIdRecoveryBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String email;

	public String sendEmployeeId() {
		String sql = "SELECT employee_code, name, email FROM add_emp WHERE email = ?";

		try (
			Connection conn = DataBaseConnection.getDefaultConnection();
			PreparedStatement ps = conn.prepareStatement(sql)
		) {
			ps.setString(1, email);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					EmailService.sendEmployeeCode(
						rs.getString("email"),
						rs.getString("employee_code"),
						rs.getString("name")
					);
					addMessage(FacesMessage.SEVERITY_INFO, "Mail sent", "Employee ID was sent to your inbox.");
					email = "";
					return null;
				}
			}
		} catch (MessagingException e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Mail failed", e.getMessage());
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			addMessage(FacesMessage.SEVERITY_ERROR, "Recovery failed", e.getMessage());
			e.printStackTrace();
			return null;
		}

		addMessage(FacesMessage.SEVERITY_WARN, "Not found", "No employee found with that email address.");
		return null;
	}

	private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
