const { getUsers } = require('./db');

const ADMIN_TOKEN = 'ems-admin-local-token';

function validateAdminToken(token) {
  return token === ADMIN_TOKEN;
}

function normalizeEmployeePayload(body = {}) {
  return {
    employee_code: body.employeeCode || body.employee_code || '',
    name: body.name || '',
    father_name: body.fatherName || body.father_name || '',
    email: body.email || '',
    gender: body.gender || 'M',
    salary: Number(body.salary || 0),
    department: body.department || 'General',
    designation: body.designation || 'Employee',
    photo_data_url: body.photoDataUrl || body.photo_data_url || '',
  };
}

module.exports = {
  ADMIN_TOKEN,
  validateAdminToken,
  normalizeEmployeePayload,
  buildEmployeeFromRow: require('./db').buildEmployeeFromRow,
};
