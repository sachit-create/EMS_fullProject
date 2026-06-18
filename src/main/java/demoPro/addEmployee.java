package demoPro;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;

import org.primefaces.model.UploadedFile;

public class addEmployee implements Serializable{
	private static final long serialVersionUID = 1L;
		
	    int id;
		String employeeCode, name, fatherName, email, gender;
		double salary;
		private UploadedFile employeePhoto;
		
       
		public void addEmployee()
		{
			try(Connection conn= DataBaseConnection.getDefaultConnection())
			{
				    String query =
				        "insert into add_emp (employee_code,name,father_name,email,gender,salary,employee_photo,employee_photo_type) values (?,?,?,?,?,?,?,?)";

				    try (PreparedStatement ps = conn.prepareStatement(query)) {
				        String generatedCode = generateEmployeeCode();
				        ps.setString(1, generatedCode);
				        ps.setString(2, name);
				        ps.setString(3, fatherName);
				        ps.setString(4, email);
				        ps.setString(5, gender);
				        ps.setDouble(6, salary);
				        byte[] photoBytes = getEmployeePhotoBytes();
				        if (photoBytes != null) {
				        	ps.setBytes(7, photoBytes);
				        } else {
				        	ps.setNull(7, Types.BLOB);
				        }
				        String photoType = getEmployeePhotoType();
				        if (photoType != null && !photoType.trim().isEmpty()) {
				        	ps.setString(8, photoType.trim());
				        } else {
				        	ps.setNull(8, Types.VARCHAR);
				        }

				        int rows = ps.executeUpdate();
				        System.out.println(rows + " row inserted");
				        sendEmployeeCodeMail(generatedCode);
				        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
				        	FacesMessage.SEVERITY_INFO,
				        	"Employee saved",
				        	name + " was added successfully. ID: " + generatedCode
				        ));
				        name = "";
				        fatherName = "";
				        email = "";
				        gender = "";
				        salary = 0.0;
				        employeeCode = generatedCode;
				        employeePhoto = null;
				    }
				}
			
			catch(Exception e)
			{
				FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Save failed",
						e.getMessage()
					)
				);
				e.printStackTrace();
			}
		}

		private void sendEmployeeCodeMail(String generatedCode) {
			try {
				EmailService.sendEmployeeCode(email, generatedCode, name);
			} catch (MessagingException e) {
				FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(
						FacesMessage.SEVERITY_WARN,
						"Mail not sent",
						"Employee saved, but the ID email could not be sent: " + e.getMessage()
					)
				);
			} catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(
						FacesMessage.SEVERITY_WARN,
						"Mail not sent",
						"Employee saved, but the ID email could not be sent: " + e.getMessage()
					)
				);
			}
		}

		private String generateEmployeeCode() {
			if (employeeCode != null && !employeeCode.trim().isEmpty()) {
				return employeeCode.trim();
			}
			try (
				Connection conn = DataBaseConnection.getDefaultConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM add_emp");
				ResultSet rs = ps.executeQuery()
			) {
				if (rs.next()) {
					int nextId = rs.getInt("next_id");
					return String.format("EMP-%06d", nextId);
				}
			} catch (Exception e) {
				// Log but don't crash; fallback to unique enough ID
				System.err.println("Error generating ID: " + e.getMessage());
			}
			return "EMP-" + (System.currentTimeMillis() % 1000000);
		}

		private byte[] getEmployeePhotoBytes() {
			if (employeePhoto == null) {
				return null;
			}
			byte[] contents = employeePhoto.getContents();
			return contents != null && contents.length > 0 ? contents : null;
		}

		private String getEmployeePhotoType() {
			if (employeePhoto == null) {
				return null;
			}
			return employeePhoto.getContentType();
		}
		


//		public void addEmployee()
//		{
//		    String sql = "INSERT INTO add_emp (name, father_name, gender) VALUES (?, ?, ?)";
//
//		    try (Connection conn = DataBaseConnection.getDefaultConnection();
//		         PreparedStatement ps = conn.prepareStatement(sql)) {
//
//		        ps.setString(1, name);
//		        ps.setString(2, fatherName);
//		        ps.setString(3, gender);
//
//		        int rows = ps.executeUpdate();
//		        System.out.println(rows + " row(s) inserted.");
//
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		    }
//		}

		
		//getter and setter
		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}
        
		public void setName(String name) {
			this.name = name;
		}

		public String getEmployeeCode() {
			return employeeCode;
		}

		public void setEmployeeCode(String employeeCode) {
			this.employeeCode = employeeCode;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public double getSalary() {
			return salary;
		}

		public void setSalary(double salary) {
			this.salary = salary;
		}

		public UploadedFile getEmployeePhoto() {
			return employeePhoto;
		}

		public void setEmployeePhoto(UploadedFile employeePhoto) {
			this.employeePhoto = employeePhoto;
		}
	

		public static long getSerialversionuid() {
			return serialVersionUID;
		}

		public String getGender() {
			return gender;
		}

		public String getFatherName() {
			return fatherName;
		}


		public void setFatherName(String fatherName) {
			this.fatherName = fatherName;
		}
		public void setGender(String gender) {
			this.gender = gender;
		}


	


		
		
	
	
	
	
}
