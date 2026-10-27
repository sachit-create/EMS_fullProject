const { db } = require('@/lib/db');
const { validateAdminToken, normalizeEmployeePayload, buildEmployeeFromRow } = require('@/lib/auth');

function parseAuthHeader(request) {
  return request.headers.get('authorization') || '';
}

export async function GET(request) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  const rows = db.prepare('SELECT * FROM employees ORDER BY created_at DESC').all();
  return Response.json({ employees: rows.map(buildEmployeeFromRow) });
}

export async function POST(request) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  try {
    const payload = await request.json();
    const normalized = normalizeEmployeePayload(payload);

    if (!normalized.name || !normalized.fatherName || !normalized.email) {
      return Response.json({ message: 'Name, father name and email are required.' }, { status: 400 });
    }

    const code = normalized.employeeCode || `EMP-${String((db.prepare('SELECT COUNT(*) AS total FROM employees').get().total || 0) + 1).padStart(4, '0')}`;

    const insert = db.prepare(`
      INSERT INTO employees (employee_code, name, father_name, email, gender, salary, department, designation, photo_data_url)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    const result = insert.run(
      code,
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
    return Response.json({ message: 'Employee created successfully', employee: buildEmployeeFromRow(created) }, { status: 201 });
  } catch (error) {
    return Response.json({ message: error.message || 'Could not create employee' }, { status: 500 });
  }
}
