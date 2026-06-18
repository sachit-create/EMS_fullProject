package demoPro;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EmployeeRecord {

	private final int id;
	private final String employeeCode;
	private final String name;
	private final String fatherName;
	private final String email;
	private final String gender;
	private final double salary;
	private final String photoDataUri;

	public EmployeeRecord(int id, String employeeCode, String name, String fatherName, String email, String gender, byte[] photoBytes, String photoType, double salary) {
		this.id = id;
		this.employeeCode = employeeCode;
		this.name = name;
		this.fatherName = fatherName;
		this.email = email;
		this.gender = gender;
		this.salary = salary;
		this.photoDataUri = buildPhotoDataUri(photoBytes, photoType, name);
	}

	public int getId() {
		return id;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public String getName() {
		return name;
	}

	public String getFatherName() {
		return fatherName;
	}

	public String getEmail() {
		return email;
	}

	public String getGender() {
		return gender;
	}

	public double getSalary() {
		return salary;
	}

	public String getPhotoDataUri() {
		return photoDataUri;
	}

	public String getGenderLabel() {
		if ("M".equalsIgnoreCase(gender)) {
			return "Male";
		}
		if ("F".equalsIgnoreCase(gender)) {
			return "Female";
		}
		return gender;
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
}
