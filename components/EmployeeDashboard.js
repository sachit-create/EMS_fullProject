'use client';

import { useEffect, useState } from 'react';

const emptyForm = {
  employeeCode: '',
  name: '',
  fatherName: '',
  email: '',
  gender: 'M',
  salary: 0,
  department: 'General',
  designation: 'Employee',
  photoDataUrl: '',
};

export default function EmployeeDashboard() {
  const [employees, setEmployees] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editId, setEditId] = useState(null);
  const [summary, setSummary] = useState('');
  const [notice, setNotice] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('ems_admin_token');
    if (!token) {
      window.location.href = '/';
      return;
    }

    fetchEmployees();
  }, []);

  async function fetchEmployees() {
    const token = localStorage.getItem('ems_admin_token');
    const response = await fetch('/api/employees', {
      headers: { Authorization: token || '' },
    });

    if (response.status === 401) {
      localStorage.removeItem('ems_admin_token');
      window.location.href = '/';
      return;
    }

    const data = await response.json();
    setEmployees(data.employees || []);
  }

  function handleLogout() {
    localStorage.removeItem('ems_admin_token');
    window.location.href = '/';
  }

  function handleFieldChange(key, value) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);

    const token = localStorage.getItem('ems_admin_token');
    const url = editId ? `/api/employees/${editId}` : '/api/employees';
    const method = editId ? 'PUT' : 'POST';

    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        Authorization: token || '',
      },
      body: JSON.stringify(form),
    });

    const data = await response.json();

    if (!response.ok) {
      setNotice({ type: 'error', text: data.message || 'Operation failed' });
      setLoading(false);
      return;
    }

    setNotice({ type: 'success', text: data.message || 'Saved successfully' });
    setForm(emptyForm);
    setEditId(null);
    await fetchEmployees();
    setLoading(false);
  }

  async function handleEdit(employee) {
    setEditId(employee.id);
    setForm({
      employeeCode: employee.employeeCode,
      name: employee.name,
      fatherName: employee.fatherName,
      email: employee.email,
      gender: employee.gender,
      salary: employee.salary,
      department: employee.department,
      designation: employee.designation,
      photoDataUrl: employee.photoDataUrl || '',
    });
  }

  async function handleDelete(id) {
    const confirmDelete = window.confirm('Delete this employee record?');
    if (!confirmDelete) return;

    const token = localStorage.getItem('ems_admin_token');
    const response = await fetch(`/api/employees/${id}`, {
      method: 'DELETE',
      headers: { Authorization: token || '' },
    });

    const data = await response.json();
    if (!response.ok) {
      setNotice({ type: 'error', text: data.message || 'Delete failed' });
      return;
    }

    setNotice({ type: 'success', text: data.message || 'Employee deleted' });
    await fetchEmployees();
  }

  async function handleGenerateSummary(employee) {
    const token = localStorage.getItem('ems_admin_token');
    const response = await fetch('/api/employees/summary', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token || '',
      },
      body: JSON.stringify({ employee }),
    });

    const data = await response.json();
    if (!response.ok) {
      setNotice({ type: 'error', text: data.message || 'Summary generation failed' });
      return;
    }

    setSummary(data.summary || '');
    setNotice({ type: 'success', text: 'AI summary generated successfully' });
  }

  const totalSalary = employees.reduce((sum, emp) => sum + Number(emp.salary || 0), 0);
  const totalEmployees = employees.length;

  return (
    <div className="page-shell">
      <div className="dashboard-wrap">
        <div className="topbar">
          <div>
            <h2>Employee Management System</h2>
          </div>
          <div className="topbar-actions">
            <span className="user-pill">admin@home.dev</span>
            <button className="secondary-btn" onClick={handleLogout}>Logout</button>
          </div>
        </div>

        {notice.text ? (
          <div className={`notice ${notice.type === 'error' ? 'error' : 'success'}`}>
            {notice.text}
          </div>
        ) : null}

        <div className="stats-grid">
          <div className="stat-card">
            <span className="stat-label">Total Employees</span>
            <span className="stat-value">{totalEmployees}</span>
          </div>
          <div className="stat-card">
            <span className="stat-label">Monthly Payroll</span>
            <span className="stat-value">₹{totalSalary.toLocaleString('en-IN')}</span>
          </div>
          <div className="stat-card">
            <span className="stat-label">Departments</span>
            <span className="stat-value">{new Set(employees.map((emp) => emp.department)).size}</span>
          </div>
          <div className="stat-card">
            <span className="stat-label">Active Admin</span>
            <span className="stat-value">Admin</span>
          </div>
        </div>

        <div className="dashboard-grid">
          <div className="panel">
            <div className="panel-header">
              <h3>Employee Directory</h3>
              <button className="primary-btn" onClick={() => {
                setEditId(null);
                setForm(emptyForm);
              }}>
                + Add Employee
              </button>
            </div>

            <div className="table-wrap">
              {employees.length === 0 ? (
                <div className="empty-state">No employees registered yet.</div>
              ) : (
                <table className="employee-table">
                  <thead>
                    <tr>
                      <th>Code</th>
                      <th>Name</th>
                      <th>Department</th>
                      <th>Salary</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {employees.map((employee) => (
                      <tr key={employee.id}>
                        <td>{employee.employeeCode}</td>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <img
                              className="avatar"
                              src={employee.photoDataUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(employee.name) + '&background=E2E8F0&color=0F172A'}
                              alt={employee.name}
                            />
                            <div>
                              <div style={{ fontWeight: 700 }}>{employee.name}</div>
                              <small style={{ color: '#64748b' }}>{employee.email}</small>
                            </div>
                          </div>
                        </td>
                        <td>{employee.department}</td>
                        <td>₹{Number(employee.salary || 0).toLocaleString('en-IN')}</td>
                        <td>
                          <div className="inline-actions">
                            <button className="small-btn" onClick={() => handleEdit(employee)}>Edit</button>
                            <button className="small-btn" onClick={() => handleGenerateSummary(employee)}>Summary</button>
                            <button className="danger-btn" onClick={() => handleDelete(employee.id)}>Delete</button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          <div className="panel">
            <div className="panel-header">
              <h3>{editId ? 'Edit Employee' : 'Add Employee'}</h3>
            </div>

            <form onSubmit={handleSubmit} className="form-card">
              <div className="two-col">
                <div className="field">
                  <label>Employee Code</label>
                  <input
                    type="text"
                    value={form.employeeCode}
                    onChange={(e) => handleFieldChange('employeeCode', e.target.value)}
                    placeholder="EMP-1006"
                  />
                </div>
                <div className="field">
                  <label>Salary</label>
                  <input
                    type="number"
                    value={form.salary}
                    onChange={(e) => handleFieldChange('salary', Number(e.target.value || 0))}
                  />
                </div>
              </div>

              <div className="two-col">
                <div className="field">
                  <label>Name</label>
                  <input
                    type="text"
                    value={form.name}
                    onChange={(e) => handleFieldChange('name', e.target.value)}
                    required
                  />
                </div>
                <div className="field">
                  <label>Father Name</label>
                  <input
                    type="text"
                    value={form.fatherName}
                    onChange={(e) => handleFieldChange('fatherName', e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="two-col">
                <div className="field">
                  <label>Email</label>
                  <input
                    type="email"
                    value={form.email}
                    onChange={(e) => handleFieldChange('email', e.target.value)}
                    required
                  />
                </div>
                <div className="field">
                  <label>Gender</label>
                  <select value={form.gender} onChange={(e) => handleFieldChange('gender', e.target.value)}>
                    <option value="M">Male</option>
                    <option value="F">Female</option>
                  </select>
                </div>
              </div>

              <div className="two-col">
                <div className="field">
                  <label>Department</label>
                  <input
                    type="text"
                    value={form.department}
                    onChange={(e) => handleFieldChange('department', e.target.value)}
                  />
                </div>
                <div className="field">
                  <label>Designation</label>
                  <input
                    type="text"
                    value={form.designation}
                    onChange={(e) => handleFieldChange('designation', e.target.value)}
                  />
                </div>
              </div>

              <div className="field">
                <label>Photo URL (optional)</label>
                <input
                  type="text"
                  value={form.photoDataUrl}
                  onChange={(e) => handleFieldChange('photoDataUrl', e.target.value)}
                  placeholder="https://images.example.com/employee.jpg"
                />
              </div>

              <div className="form-actions">
                {editId ? (
                  <button type="button" className="secondary-btn" onClick={() => {
                    setEditId(null);
                    setForm(emptyForm);
                  }}>
                    Cancel
                  </button>
                ) : null}
                <button type="submit" className="primary-btn" disabled={loading}>
                  {loading ? 'Saving...' : editId ? 'Update Employee' : 'Save Employee'}
                </button>
              </div>
            </form>

            <div style={{ marginTop: '20px' }}>
              <div className="panel-header" style={{ marginBottom: '10px' }}>
                <h3>AI Summary</h3>
              </div>
              <div className="summary-box">
                {summary ? (
                  <ul>
                    {summary.split('\n').filter(Boolean).map((line, index) => (
                      <li key={index}>{line.replace(/^[\-•\s]+/, '')}</li>
                    ))}
                  </ul>
                ) : (
                  <p>Select an employee and generate a summary.</p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
