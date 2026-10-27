const { db } = require('./db');

function seedDefaults() {
  const adminExists = db.prepare('SELECT 1 FROM users WHERE username = ?').get('admin');

  if (!adminExists) {
    db.prepare(`
      INSERT INTO users (username, password, email, full_name)
      VALUES (?, ?, ?, ?)
    `).run('admin', 'admin123', 'admin@home.dev', 'System Admin');
  }

  const employeeCount = db.prepare('SELECT COUNT(*) AS total FROM employees').get().total;

  if (employeeCount === 0) {
    const seedEmployees = [
      {
        employee_code: 'EMP-1001',
        name: 'Aarav Sharma',
        father_name: 'Rajesh Sharma',
        email: 'aarav.sharma@example.com',
        gender: 'M',
        salary: 65000,
        department: 'Engineering',
        designation: 'Senior Developer',
        photo_data_url: '',
      },
      {
        employee_code: 'EMP-1002',
        name: 'Saanvi Patel',
        father_name: 'Vimal Patel',
        email: 'saanvi.patel@example.com',
        gender: 'F',
        salary: 71000,
        department: 'Human Resources',
        designation: 'HR Manager',
        photo_data_url: '',
      },
      {
        employee_code: 'EMP-1003',
        name: 'Rohan Mehta',
        father_name: 'Sunil Mehta',
        email: 'rohan.mehta@example.com',
        gender: 'M',
        salary: 59000,
        department: 'Operations',
        designation: 'Operations Lead',
        photo_data_url: '',
      },
      {
        employee_code: 'EMP-1004',
        name: 'Ishita Verma',
        father_name: 'Karan Verma',
        email: 'ishita.verma@example.com',
        gender: 'F',
        salary: 76000,
        department: 'Finance',
        designation: 'Financial Analyst',
        photo_data_url: '',
      },
      {
        employee_code: 'EMP-1005',
        name: 'Kabir Singh',
        father_name: 'Harpreet Singh',
        email: 'kabir.singh@example.com',
        gender: 'M',
        salary: 83000,
        department: 'Product',
        designation: 'Product Manager',
        photo_data_url: '',
      },
    ];

    const insert = db.prepare(`
      INSERT INTO employees (employee_code, name, father_name, email, gender, salary, department, designation, photo_data_url)
      VALUES (@employee_code, @name, @father_name, @email, @gender, @salary, @department, @designation, @photo_data_url)
    `);

    const transaction = db.transaction((rows) => {
      for (const row of rows) insert.run(row);
    });

    transaction(seedEmployees);
  }
}

seedDefaults();

module.exports = { seedDefaults };
