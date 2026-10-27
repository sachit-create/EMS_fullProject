const { db } = require('@/lib/db');
const { validateAdminToken, buildEmployeeFromRow, normalizeEmployeePayload } = require('@/lib/auth');

function parseAuthHeader(request) {
  return request.headers.get('authorization') || '';
}

export async function GET(request, { params }) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  const employee = db.prepare('SELECT * FROM employees WHERE id = ?').get(Number(params.id));
  if (!employee) {
    return Response.json({ message: 'Employee not found' }, { status: 404 });
  }

  return Response.json({ employee: buildEmployeeFromRow(employee) });
}

export async function PUT(request, { params }) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  try {
    const existing = db.prepare('SELECT * FROM employees WHERE id = ?').get(Number(params.id));
    if (!existing) {
      return Response.json({ message: 'Employee not found' }, { status: 404 });
    }

    const payload = normalizeEmployeePayload(await request.json());
    const update = db.prepare(`
      UPDATE employees
      SET employee_code = ?, name = ?, father_name = ?, email = ?, gender = ?, salary = ?, department = ?, designation = ?, photo_data_url = ?
      WHERE id = ?
    `);

    update.run(
      payload.employeeCode || existing.employee_code,
      payload.name || existing.name,
      payload.fatherName || existing.father_name,
      payload.email || existing.email,
      payload.gender || existing.gender,
      Number(payload.salary || existing.salary),
      payload.department || existing.department,
      payload.designation || existing.designation,
      payload.photoDataUrl || existing.photo_data_url,
      Number(params.id)
    );

    const updated = db.prepare('SELECT * FROM employees WHERE id = ?').get(Number(params.id));
    return Response.json({ message: 'Employee updated successfully', employee: buildEmployeeFromRow(updated) });
  } catch (error) {
    return Response.json({ message: error.message || 'Could not update employee' }, { status: 500 });
  }
}

export async function DELETE(request, { params }) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  const employee = db.prepare('SELECT * FROM employees WHERE id = ?').get(Number(params.id));
  if (!employee) {
    return Response.json({ message: 'Employee not found' }, { status: 404 });
  }

  db.prepare('DELETE FROM employees WHERE id = ?').run(Number(params.id));
  return Response.json({ message: 'Employee deleted successfully' });
}
