package demoPro;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name = "employeeView")
@RequestScoped
public class EmployeeViewBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String LOAD_EMPLOYEES =
        "SELECT id, employee_code, name, father_name, email, gender, salary, employee_photo, employee_photo_type FROM add_emp ORDER BY id ASC";

    private List<EmployeeRecord> employees;

    public List<EmployeeRecord> getEmployees() {
        if (employees == null) {
            loadEmployees();
        }
        return employees;
    }

    public void refresh() {
        employees = null;
        loadEmployees();
    }

    public int getEmployeeCount() {
        return getEmployees().size();
    }

    private void loadEmployees() {

        List<EmployeeRecord> records = new ArrayList<EmployeeRecord>();

        try (
            Connection conn = DataBaseConnection.getDefaultConnection();
            PreparedStatement ps = conn.prepareStatement(LOAD_EMPLOYEES);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                records.add(
                    new EmployeeRecord(
                        rs.getInt("id"),
                        rs.getString("employee_code"),
                        rs.getString("name"),
                        rs.getString("father_name"),
                        rs.getString("email"),
                        rs.getString("gender"),
                        rs.getBytes("employee_photo"),
                        rs.getString("employee_photo_type"),
                        rs.getDouble("salary")
                    )
                );

            }

        } catch (Exception e) {

            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Load Failed",
                    e.getMessage()
                )
            );

            e.printStackTrace();
        }

        employees = records;
    }
}
