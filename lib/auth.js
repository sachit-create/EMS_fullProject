const { db } = require('./db');

const ADMIN_TOKEN = 'ems-admin-local-token';

function validateAdminToken(token) {
  return token === ADMIN_TOKEN;
}

function normalizeEmployeePayload(body = {}) {
  return {
    employeeCode: body.employeeCode || body.employee_code || '',
    name: body.name || '',
    fatherName: body.fatherName || body.father_name || '',
    email: body.email || '',
    gender: body.gender || 'M',
    salary: Number(body.salary || 0),
    department: body.department || 'General',
    designation: body.designation || 'Employee',
    photoDataUrl: body.photoDataUrl || '',
  };
}

function buildEmployeeFromRow(row) {
  return {
    id: row.id,
    employeeCode: row.employee_code,
    name: row.name,
    fatherName: row.father_name,
    email: row.email,
    gender: row.gender,
    salary: Number(row.salary),
    department: row.department,
    designation: row.designation,
    photoDataUrl: row.photo_data_url || '',
    createdAt: row.created_at,
  };
}

function buildEmployeeCode() {
  const row = db.prepare('SELECT MAX(id) AS maxId FROM employees').get();
  const nextId = Number(row?.maxId || 0) + 1;
  return `EMP-${String(nextId).padStart(4, '0')}`;
}

module.exports = {
  ADMIN_TOKEN,
  validateAdminToken,
  normalizeEmployeePayload,
  buildEmployeeFromRow,
  buildEmployeeCode,
};
