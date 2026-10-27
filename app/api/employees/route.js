const { db } = require('@/lib/db');
const { ADMIN_TOKEN, validateAdminToken, normalizeEmployeePayload, buildEmployeeFromRow, buildEmployeeCode } = require('@/lib/auth');

function parseAuthHeader(request) {
  const header = request.headers.get('authorization') || '';
  return header.trim();
}

function isAuthorized(request) {
  return validateAdminToken(parseAuthHeader(request));
}

export async function GET(request) {
  if (!isAuthorized(request)) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  const rows = db.prepare('SELECT * FROM employees ORDER BY created_at DESC').all();
  return Response.json({ employees: rows.map(buildEmployeeFromRow) });
}

export async function POST(request) {
  if (!isAuthorized(request)) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  try {
    const payload = await request.json();
    const normalized = normalizeEmployeePayload(payload);

    if (!normalized.name || !normalized.fatherName || !normalized.email) {
      return Response.json({ message: 'Name, father name and email are required' }, { status: 400 });
    }

    const employeeCode = normalized.employeeCode || buildEmployeeCode();
    const insert = db.prepare(`
      INSERT INTO employees (employee_code, name, father_name, email, gender, salary, department, designation, photo_data_url)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    const result = insert.run(
      employeeCode,
      normalized.name,
      normalized.fatherName,
      normalized.email,
      normalized.gender,
      normalized.salary,
      normalized.department,
      normalized.designation,
      normalized.photoDataUrl
    );

    const created = db.prepare('SELECT * FROM employees WHERE id = ?').get(result.lastInsertRowid);
    return Response.json({
      message: 'Employee created successfully',
      employee: buildEmployeeFromRow(created),
    }, { status: 201 });
  } catch (error) {
    return Response.json({ message: error.message || 'Could not create employee' }, { status: 500 });
  }
}
