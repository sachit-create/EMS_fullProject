const state = {
  users: [],
  employees: [],
};

const DEFAULT_USERS = [
  {
    id: 1,
    username: 'admin',
    password: 'admin123',
    email: 'admin@home.dev',
    full_name: 'System Admin',
  },
];

const DEFAULT_EMPLOYEES = [
  {
    id: 1,
    employee_code: 'EMP-0001',
    name: 'Aarav Sharma',
    father_name: 'Rajesh Sharma',
    email: 'aarav.sharma@example.com',
    gender: 'M',
    salary: 65000,
    department: 'Engineering',
    designation: 'Senior Developer',
    photo_data_url: '',
    created_at: new Date().toISOString(),
  },
  {
    id: 2,
    employee_code: 'EMP-0002',
    name: 'Saanvi Patel',
    father_name: 'Vimal Patel',
    email: 'saanvi.patel@example.com',
    gender: 'F',
    salary: 71000,
    department: 'Human Resources',
    designation: 'HR Manager',
    photo_data_url: '',
    created_at: new Date().toISOString(),
  },
  {
    id: 3,
    employee_code: 'EMP-0003',
    name: 'Rohan Mehta',
    father_name: 'Sunil Mehta',
    email: 'rohan.mehta@example.com',
    gender: 'M',
    salary: 59000,
    department: 'Operations',
    designation: 'Operations Lead',
    photo_data_url: '',
    created_at: new Date().toISOString(),
  },
];

function ensureInitialState() {
  if (state.users.length === 0) {
    state.users = DEFAULT_USERS.map((user) => ({ ...user }));
  }

  if (state.employees.length === 0) {
    state.employees = DEFAULT_EMPLOYEES.map((employee) => ({ ...employee }));
  }
}

function getUsers() {
  ensureInitialState();
  return state.users;
}

function getEmployees() {
  ensureInitialState();
  return state.employees;
}

function saveUsers(users) {
  state.users = users;
}

function saveEmployees(employees) {
  state.employees = employees;
}

function buildEmployeeFromRow(row) {
  if (!row) return null;

  return {
    id: row.id,
    employeeCode: row.employee_code,
    name: row.name,
    fatherName: row.father_name,
    email: row.email,
    gender: row.gender,
    salary: Number(row.salary || 0),
    department: row.department,
    designation: row.designation,
    photoDataUrl: row.photo_data_url || '',
    createdAt: row.created_at,
  };
}

function buildEmployeeCode(employees = getEmployees()) {
  const nextNumber = (employees.length || 0) + 1;
  return `EMP-${String(nextNumber).padStart(4, '0')}`;
}

function addEmployee(payload) {
  const employees = getEmployees();
  const nextId = (employees.reduce((max, item) => Math.max(max, Number(item.id || 0)), 0) || 0) + 1;

  const employee = {
    id: nextId,
    employee_code: payload.employee_code,
    name: payload.name,
    father_name: payload.father_name,
    email: payload.email,
    gender: payload.gender,
    salary: Number(payload.salary || 0),
    department: payload.department || 'General',
    designation: payload.designation || 'Employee',
    photo_data_url: payload.photo_data_url || '',
    created_at: new Date().toISOString(),
  };

  employees.unshift(employee);
  saveEmployees(employees);
  return employee;
}

function updateEmployee(id, payload) {
  const employees = getEmployees();
  const index = employees.findIndex((employee) => Number(employee.id) === Number(id));

  if (index === -1) return null;

  employees[index] = {
    ...employees[index],
    employee_code: payload.employee_code || employees[index].employee_code,
    name: payload.name || employees[index].name,
    father_name: payload.father_name || employees[index].father_name,
    email: payload.email || employees[index].email,
    gender: payload.gender || employees[index].gender,
    salary: Number(payload.salary || employees[index].salary),
    department: payload.department || employees[index].department,
    designation: payload.designation || employees[index].designation,
    photo_data_url: payload.photo_data_url || employees[index].photo_data_url,
  };

  saveEmployees(employees);
  return employees[index];
}

function deleteEmployee(id) {
  const employees = getEmployees();
  const updated = employees.filter((employee) => Number(employee.id) !== Number(id));
  saveEmployees(updated);
  return updated;
}

module.exports = {
  state,
  ensureInitialState,
  getUsers,
  getEmployees,
  saveUsers,
  saveEmployees,
  buildEmployeeFromRow,
  buildEmployeeCode,
  addEmployee,
  updateEmployee,
  deleteEmployee,
};
